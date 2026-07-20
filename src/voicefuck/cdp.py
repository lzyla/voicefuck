"""Chrome DevTools Protocol (CDP) helpers.

These functions talk to Chrome's HTTP debugging endpoint (``/json/*`` on the
remote-debugging port) using only the standard library. They cover the parts of
the workflow that must *not* go through ``agent-browser``: checking whether the
real Chrome is up, launching it, and creating background tabs without stealing
OS focus.
"""

from __future__ import annotations

import json
import os
import platform
import subprocess
import urllib.error
import urllib.parse
import urllib.request
from typing import Any

CDP_PORT = 9222
"""Default remote-debugging port. One Chrome instance + many named sessions."""

DEFAULT_PROFILE = os.path.expanduser("~/.chrome-beta-profile")
"""Persistent profile dir. Required for remote debugging; keeps cookies/logins."""

# macOS Chrome Beta binary. The CDP model is platform-independent, but the launch
# path is not — override via ``chrome_binary`` on other platforms.
_MACOS_CHROME_BETA = (
    "/Applications/Google Chrome Beta.app/Contents/MacOS/Google Chrome Beta"
)


class ChromeNotRunningError(RuntimeError):
    """Raised when the CDP debugging endpoint is not reachable."""


def _cdp_get(path: str, port: int, timeout: float) -> Any:
    url = f"http://localhost:{port}{path}"
    try:
        with urllib.request.urlopen(url, timeout=timeout) as resp:  # noqa: S310
            body = resp.read().decode("utf-8")
    except (urllib.error.URLError, OSError) as exc:  # connection refused, etc.
        raise ChromeNotRunningError(
            f"CDP endpoint not reachable at {url}: {exc}"
        ) from exc
    return json.loads(body) if body.strip() else None


def is_debugger_up(port: int = CDP_PORT, timeout: float = 2.0) -> bool:
    """Return True if a Chrome remote-debugging endpoint answers on ``port``."""
    try:
        chrome_version(port, timeout=timeout)
        return True
    except ChromeNotRunningError:
        return False


def chrome_version(port: int = CDP_PORT, timeout: float = 2.0) -> dict[str, Any]:
    """Return Chrome's ``/json/version`` payload, or raise ChromeNotRunningError."""
    data = _cdp_get("/json/version", port=port, timeout=timeout)
    if not isinstance(data, dict):
        raise ChromeNotRunningError("Unexpected /json/version response")
    return data


def list_targets(port: int = CDP_PORT, timeout: float = 2.0) -> list[dict[str, Any]]:
    """Return the list of open targets (tabs) from ``/json/list``."""
    data = _cdp_get("/json/list", port=port, timeout=timeout)
    return data if isinstance(data, list) else []


def new_background_tab(
    url: str, port: int = CDP_PORT, timeout: float = 5.0
) -> dict[str, Any]:
    """Open a new tab **without stealing OS focus**.

    Uses ``GET /json/new?<url>``, which creates the tab in the background — the
    preferred way to open many tabs before doing any work. Returns the new
    target's metadata (includes ``webSocketDebuggerUrl``).
    """
    encoded = urllib.parse.quote(url, safe="")
    data = _cdp_get(f"/json/new?{encoded}", port=port, timeout=timeout)
    if not isinstance(data, dict):
        raise ChromeNotRunningError("Unexpected /json/new response")
    return data


def launch_chrome(
    profile: str = DEFAULT_PROFILE,
    port: int = CDP_PORT,
    chrome_binary: str | None = None,
    wait_seconds: float = 4.0,
    extra_args: list[str] | None = None,
) -> subprocess.Popen[bytes]:
    """Launch Chrome Beta with remote debugging enabled and wait for it to come up.

    Never launch Chrome via ``agent-browser`` — that yields an automation-flagged
    browser. This starts the real binary with ``--remote-debugging-port`` and a
    dedicated ``--user-data-dir`` (both required for debugging to enable).

    If a debugger is *already* up on ``port``, no new process is started and
    ``None`` is returned — do not kill the running Chrome; the user may be
    driving it manually.
    """
    if is_debugger_up(port):
        return None  # type: ignore[return-value]

    binary = chrome_binary or _default_chrome_binary()
    args = [
        binary,
        f"--remote-debugging-port={port}",
        f"--user-data-dir={profile}",
        *(extra_args or []),
    ]
    proc = subprocess.Popen(args, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

    _wait_for_debugger(port, wait_seconds)
    return proc


def _default_chrome_binary() -> str:
    system = platform.system()
    if system == "Darwin":
        return _MACOS_CHROME_BETA
    # Best-effort fallbacks; override explicitly on non-macOS hosts.
    raise ChromeNotRunningError(
        f"No default Chrome Beta path for platform {system!r}; "
        "pass chrome_binary=... explicitly."
    )


def _wait_for_debugger(port: int, wait_seconds: float) -> None:
    import time

    deadline = time.monotonic() + wait_seconds
    while time.monotonic() < deadline:
        if is_debugger_up(port):
            return
        time.sleep(0.25)
    raise ChromeNotRunningError(
        f"Chrome did not expose a debugger on port {port} within {wait_seconds}s"
    )
