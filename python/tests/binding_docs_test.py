"""reqrio Python binding conformance tests (docs.html based).

Run from bindings/python:
    py tests/binding_docs_test.py
"""

import ctypes
import _ctypes
import json
import os
import pathlib
import sys
import traceback

# Python 3.14 no longer exposes _ctypes.POINTER; reqrio.bindings imports it.
if not hasattr(_ctypes, "POINTER"):
    _ctypes.POINTER = ctypes.POINTER

from reqrio import Session, ALPN, Method, Fingerprint, Version, CipherSuite, ExtensionType

ROOT = pathlib.Path(__file__).resolve().parents[3]
TOKEN = (ROOT / "TOKEN").read_text(encoding="utf-8").strip() if (ROOT / "TOKEN").exists() else ""

JA3 = "771,4865-4866-4867-49195-49199-49196-49200-52393-52392-49171-49172-156-157-47-53,18-45-5-10-11-16-23-27-0-13-35-65281-43-51-17613,29-23-24,0"
JA4 = "t13d1515h2_002f,0035,009c,009d,1301,1302,1303,c013,c014,c02b,c02c,c02f,c030,cca8,cca9_0005,000a,000b,000d,0012,0017,001b,0023,002b,002d,0033,44cd,ff01_0804,0403,0401,0303,0402,080b,0201,0203,0202,0302,0502,0301,0503,0602,0806,0809"

RESULTS = []


def check(name):
    def deco(fn):
        try:
            fn()
            RESULTS.append((name, "PASS", ""))
            print(f"PASS {name}")
        except Exception as e:
            RESULTS.append((name, "FAIL", f"{type(e).__name__}: {e}"))
            print(f"FAIL {name}: {type(e).__name__}: {e}")
            traceback.print_exc(limit=3)
        return fn

    return deco


def session(alpn=ALPN.HTTP20):
    s = Session(alpn=alpn)
    s.set_timeout(connect=10000, read=10000, write=10000, handle=20000, connect_times=1, handle_times=1)
    return s


@check("session_get_text")
def session_get_text():
    s = session()
    try:
        r = s.get("https://www.baidu.com")
        assert r.statue_code() == 200, r.statue_code()
        assert len(r.bytes()) > 0
        assert "baidu" in r.text().lower() or "百度" in r.text()
    finally:
        s.close()


@check("query_params")
def query_params():
    s = session()
    try:
        r = s.get("https://httpbin.org/get", params={"q": "reqrio", "page": 1})
        assert r.statue_code() == 200, r.statue_code()
        data = r.json()
        assert data["args"]["q"] == "reqrio", data
        assert data["args"]["page"] == "1", data
    finally:
        s.close()


@check("post_json")
def post_json():
    s = session()
    try:
        r = s.post("https://httpbin.org/post", json={"a": 1, "b": "two"})
        assert r.statue_code() == 200, r.statue_code()
        data = r.json()
        assert data["json"]["a"] == 1, data
        assert "application/json" in data["headers"].get("Content-Type", ""), data
    finally:
        s.close()


@check("post_form")
def post_form():
    s = session()
    try:
        r = s.post("https://httpbin.org/post", data={"a": "1", "b": "x y"})
        data = r.json()
        assert data["form"]["a"] == "1", data
        assert "x-www-form-urlencoded" in data["headers"].get("Content-Type", ""), data
    finally:
        s.close()


@check("post_bytes_text")
def post_bytes_text():
    s = session()
    try:
        r = s.post("https://httpbin.org/post", bytes=b"\x00\x01hello")
        assert r.json()["headers"]["Content-Type"].startswith("application/octet-stream")
        r = s.post("https://httpbin.org/post", text="plain-text")
        assert r.json()["data"] == "plain-text", r.json()
    finally:
        s.close()


@check("send_recv")
def send_recv():
    s = session()
    try:
        sid = s.send(Method.GET, "https://httpbin.org/get?x=1")
        r = s.recv(sid, False)
        assert r.statue_code() == 200
        assert r.json()["args"]["x"] == "1"
    finally:
        s.close()


@check("stream_chunks")
def stream_chunks():
    s = session(alpn=ALPN.HTTP11)
    try:
        r = s.get("https://httpbin.org/bytes/16384", stream=True)
        total = 0
        for chunk in r.chunks():
            total += len(chunk)
        assert total == 16384, total
    finally:
        s.close()


@check("response_headers_cookies")
def response_headers_cookies():
    s = session()
    try:
        r = s.get("https://httpbin.org/cookies/set?k=v", auto_redirect=False)
        cks = r.cookies()
        assert any(c.get("name") == "k" and c.get("value") == "v" for c in cks), cks
        r = s.get("https://httpbin.org/cookies")
        assert r.json()["cookies"]["k"] == "v", r.json()
    finally:
        s.close()


@check("fingerprint_ja3_ja4_random")
def fingerprint_ja3_ja4_random():
    for fp in (
            Fingerprint.from_ja3(JA3, TOKEN),
            Fingerprint.from_ja4(JA4, TOKEN),
            Fingerprint.from_random(TOKEN),
    ):
        s = session()
        try:
            s.set_fingerprint(fp)
            r = s.get("https://m.so.com")
            assert r.statue_code() == 200, r.statue_code()
        finally:
            s.close()


@check("fingerprint_builtin")
def fingerprint_builtin():
    fp = Fingerprint.from_builtin("win-x64-chrome-152", TOKEN)
    s = session()
    try:
        s.set_fingerprint(fp)
        r = s.get("https://m.so.com")
        assert r.statue_code() == 200, r.statue_code()
        os.remove("builtin.dat")
    finally:
        s.close()


@check("http3")
def http3():
    Fingerprint.from_random(TOKEN)
    s = session(alpn=ALPN.HTTP30)
    try:
        r = s.get("https://www.bing.com")
        assert r.statue_code() == 200, r.statue_code()
    finally:
        s.close()


@check("module_level_get")
def module_level_get():
    import reqrio

    r = reqrio.get("https://www.baidu.com", {"User-Agent": "reqrio-test"})
    assert r.statue_code() == 200, r.statue_code()
    assert len(r.bytes()) > 0


@check("invalid_url_raises")
def invalid_url_raises():
    s = session()
    try:
        try:
            s.get("not a valid url")
        except Exception:
            return
        raise AssertionError("invalid URL did not raise")
    finally:
        s.close()


def main():
    if not TOKEN:
        print("WARN: TOKEN is empty, fingerprint tests may fall back to default")
    print("\n=== summary ===")
    failed = [r for r in RESULTS if r[1] == "FAIL"]
    for name, status, msg in RESULTS:
        print(f"{status:4} {name} {msg}")
    print(f"total={len(RESULTS)} pass={len(RESULTS) - len(failed)} fail={len(failed)}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
