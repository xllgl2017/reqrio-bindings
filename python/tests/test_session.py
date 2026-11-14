import argparse
import threading
import time
import traceback
from concurrent.futures import ThreadPoolExecutor, as_completed
from reqrio import ALPN
from example.req import *


def test_get():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_get_matrix(Session(alpn=alpn), alpn)


def test_method():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_method(Session(alpn=alpn))


def test_status():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_status(Session(alpn=alpn))


def test_auth():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_auth(Session(alpn=alpn))


def test_query_param():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        query_param(Session(alpn=alpn))


def test_redirect():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_redirect(Session(alpn=alpn))


def test_cookie():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_cookie(Session(alpn=alpn))


def test_headers():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_header(Session(alpn=alpn))


def test_stream_req():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_stream(Session(alpn=alpn))


def test_body():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_body(Session(alpn=alpn))


def test_concurrency():
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        http_concurrency(Session(alpn=alpn))


def test_large_body():
    """big body"""
    for alpn in (ALPN.HTTP11, ALPN.HTTP20):
        session = Session(alpn=alpn)
        resp = session.get("https://httpbin.reqrio.com/bytes/65536")
        assert resp.status_code == 200, resp.status_code
        assert len(resp.bytes()) == 65536, len(resp.bytes())


def test_timeout():
    """超时测试"""
    s = Session(alpn=ALPN.HTTP11)
    try:
        s.set_timeout(connect=3000, read=3000, write=3000, handle=3000, connect_times=1, handle_times=1)
        start = time.time()
        try:
            s.get("https://192.0.2.1/")
            raise AssertionError("should timeout")
        except Exception:
            pass
        elapsed = (time.time() - start) * 1000
        assert 3000 <= elapsed < 6000, elapsed

        start = time.time()
        sid = s.send(Method.GET, "https://httpbin.reqrio.com/delay/10")
        try:
            s.recv(sid, False)
            raise AssertionError("should timeout")
        except Exception:
            pass
        elapsed = (time.time() - start) * 1000
        assert elapsed >= 3000, elapsed
    finally:
        s.close()


CASES = [
    test_get,
    test_method,
    test_status,
    test_auth,
    test_query_param,
    test_redirect,
    test_cookie,
    test_headers,
    test_stream_req,
    test_body,
    test_concurrency,
    http_one_shot_api,
    test_large_body,
    test_timeout,
]


def _run_case(case):
    """在线程中执行单个用例，返回 (name, ok, detail)。"""
    name = case.__name__
    try:
        case()
        return name, True, ""
    except Exception as e:
        return name, False, f"{type(e).__name__}: {e}\n{traceback.format_exc(limit=3)}"


def main(argv=None):
    parser = argparse.ArgumentParser(description="reqrio python binding HTTP tests")
    parser.add_argument("-j", "--jobs", type=int, default=8,
                        help="并发执行用例数（默认 4，1 为串行）")
    args = parser.parse_args(argv)
    jobs = max(1, min(args.jobs, len(CASES)))

    result = []
    print_lock = threading.Lock()

    if jobs == 1:
        for case in CASES:
            name, ok, detail = _run_case(case)
            result.append((name, ok, detail))
            print(f"PASS {name}" if ok else f"FAIL {name}: {detail.splitlines()[0]}")
    else:
        with ThreadPoolExecutor(max_workers=jobs, thread_name_prefix="reqrio-test") as pool:
            futures = [pool.submit(_run_case, case) for case in CASES]
            for fut in as_completed(futures):
                name, ok, detail = fut.result()
                result.append((name, ok, detail))
                with print_lock:
                    if ok:
                        print(f"PASS {name}")
                    else:
                        print(f"FAIL {name}: {detail.splitlines()[0]}")

    failed = [r for r in result if not r[1]]
    for name, _, detail in failed:
        print(f"\n===== {name} =====\n{detail}")
    print(f"\n{len(result) - len(failed)}/{len(result)} passed (jobs={jobs})")
    return 0 if not failed else 1


if __name__ == "__main__":
    raise SystemExit(main())
