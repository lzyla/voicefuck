"""Small CLI for common voicefuck operations.

Usage::

    voicefuck status                 # is Chrome's debug port up?
    voicefuck launch                 # launch Chrome Beta with the debug port
    voicefuck open <url> [url ...]   # batch-open background tabs
    voicefuck check                  # report navigator.webdriver on a fresh session
"""

from __future__ import annotations

import argparse
import sys

from . import __version__
from .cdp import CDP_PORT, ChromeNotRunningError, chrome_version, launch_chrome
from .parallel import batch_open_tabs
from .session import BrowserSession


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(prog="voicefuck", description=__doc__)
    parser.add_argument("--version", action="version", version=f"voicefuck {__version__}")
    parser.add_argument("--port", type=int, default=CDP_PORT, help="CDP debug port")
    sub = parser.add_subparsers(dest="cmd", required=True)

    sub.add_parser("status", help="check whether the CDP debug port is up")
    sub.add_parser("launch", help="launch Chrome Beta with remote debugging")
    p_open = sub.add_parser("open", help="batch-open background tabs")
    p_open.add_argument("urls", nargs="+")
    sub.add_parser("check", help="report navigator.webdriver on a fresh session")

    args = parser.parse_args(argv)

    if args.cmd == "status":
        try:
            ver = chrome_version(args.port)
        except ChromeNotRunningError as exc:
            print(f"down: {exc}", file=sys.stderr)
            return 1
        print(f"up: {ver.get('Browser', 'unknown')}")
        return 0

    if args.cmd == "launch":
        proc = launch_chrome(port=args.port)
        print("already running" if proc is None else "launched")
        return 0

    if args.cmd == "open":
        tabs = batch_open_tabs(args.urls, port=args.port)
        for t in tabs:
            print(t.get("id", "?"), t.get("url", ""))
        return 0

    if args.cmd == "check":
        flag = BrowserSession(port=args.port).webdriver_flag()
        print(f"navigator.webdriver = {flag!r}")
        return 0 if flag in ("false", "undefined", "") else 2

    return 0  # pragma: no cover


if __name__ == "__main__":  # pragma: no cover
    raise SystemExit(main())
