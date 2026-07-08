import argparse
import time

import requests
from requests.exceptions import RequestException


def normalize_url(url: str) -> str:
    if not url.startswith(("http://", "https://")):
        return f"https://{url}"
    return url


def run_requests(url: str, count: int, verify: bool):
    session = requests.Session()
    headers = {
        "User-Agent": "python-requests/2.31.0",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
    }

    url = normalize_url(url)
    print(f"requests: url={url} count={count} verify={verify}")
    total_elapsed = 0.0
    body_length = None
    success_count = 0

    for index in range(1, count + 1):
        try:
            start = time.perf_counter()
            resp = session.get(url, headers=headers, timeout=15, verify=verify)
            elapsed = time.perf_counter() - start
            total_elapsed += elapsed
            body_length = len(resp.content)
            success_count += 1

            print(f"  run {index:02}: status={resp.status_code} length={body_length} elapsed={elapsed:.3f}s")
            resp.raise_for_status()
        except RequestException as err:
            print(f"  run {index:02}: request failed: {err}")

    if success_count > 0:
        avg_elapsed = total_elapsed / success_count
        print(f"average elapsed: {avg_elapsed:.3f}s")
        print(f"body length: {body_length}")
        print(f"successful runs: {success_count}/{count}")
    else:
        print("No successful requests were completed.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Compare requests performance with repeated runs.")
    parser.add_argument("url", nargs="?", default="cn.bing.com", help="Request URL")
    parser.add_argument("--count", type=int, default=5, help="Number of requests to send")
    parser.add_argument("--insecure", action="store_true", help="Disable SSL certificate verification")
    args = parser.parse_args()
    run_requests(args.url, args.count, verify=not args.insecure)
