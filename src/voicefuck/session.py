"""Named ``agent-browser`` sessions attached to a running Chrome via CDP.

``BrowserSession`` is a thin, timeout-guarded wrapper over the ``agent-browser``
CLI. It guarantees the two invariants that cause the most silent breakage:

* every command carries ``--cdp <port> --session <id>``;
* a unique 6-char session id is generated per instance unless one is supplied.
"""

from __future__ import annotations

import secrets
import string
import subprocess
from dataclasses import dataclass, field

from .cdp import CDP_PORT

_ALPHABET = string.ascii_lowercase + string.digits


class BrowserCommandError(RuntimeError):
    """Raised when an ``agent-browser`` command exits non-zero."""

    def __init__(self, cmd: list[str], returncode: int, stderr: str):
        self.cmd = cmd
        self.returncode = returncode
        self.stderr = stderr
        super().__init__(
            f"agent-browser exited {returncode}: {' '.join(cmd)}\n{stderr.strip()}"
        )


class BrowserTimeout(RuntimeError):
    """Raised when an ``agent-browser`` command exceeds its hard timeout."""


def gen_session_id(length: int = 6) -> str:
    """Return a unique lowercase-alphanumeric session id.

    Never reuse ``main`` or a hardcoded name — two agents on the same session
    name clobber each other's tab silently.
    """
    return "".join(secrets.choice(_ALPHABET) for _ in range(length))


@dataclass
class BrowserSession:
    """A single named tab, driven over CDP.

    Example::

        s = BrowserSession()          # unique 6-char id, port 9222
        s.open("https://example.com")
        s.snapshot(interactive=True)
        assert s.webdriver_flag() in ("false", "undefined", "")
    """

    session_id: str = field(default_factory=gen_session_id)
    port: int = CDP_PORT
    default_timeout: float = 15.0
    binary: str = "agent-browser"

    # -- low-level ---------------------------------------------------------

    def _base(self) -> list[str]:
        # --cdp attaches to the running Chrome; --session isolates this tab.
        # There is no separate `connect` step (and `connect` is buggy).
        return [self.binary, "--cdp", str(self.port), "--session", self.session_id]

    def run(self, *args: str, timeout: float | None = None) -> str:
        """Run one ``agent-browser`` subcommand and return its stdout.

        Every command is hard-timed-out — an unbounded wait is the main failure
        mode of browser automation.
        """
        cmd = [*self._base(), *args]
        try:
            proc = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=timeout or self.default_timeout,
            )
        except subprocess.TimeoutExpired as exc:
            raise BrowserTimeout(f"Timed out: {' '.join(cmd)}") from exc
        if proc.returncode != 0:
            raise BrowserCommandError(cmd, proc.returncode, proc.stderr)
        return proc.stdout

    # -- high-level convenience -------------------------------------------

    def open(self, url: str, timeout: float | None = None) -> str:
        return self.run("open", url, timeout=timeout)

    def snapshot(self, interactive: bool = False, timeout: float | None = None) -> str:
        args = ["snapshot", "-i"] if interactive else ["snapshot"]
        return self.run(*args, timeout=timeout)

    def click(self, ref: str, timeout: float | None = None) -> str:
        return self.run("click", ref, timeout=timeout)

    def fill(self, ref: str, value: str, timeout: float | None = None) -> str:
        return self.run("fill", ref, value, timeout=timeout)

    def screenshot(self, path: str, timeout: float | None = None) -> str:
        return self.run("screenshot", path, timeout=timeout)

    def eval(self, expression: str, timeout: float | None = None) -> str:
        return self.run("eval", expression, timeout=timeout).strip()

    def goto(self, url: str, timeout: float | None = None) -> str:
        """Navigate the current tab via JS (useful for recovery after a stall)."""
        return self.eval(f"window.location.href={url!r}", timeout=timeout)

    def close_tab(self, timeout: float | None = None) -> str:
        return self.run("tab", "close", timeout=timeout)

    def webdriver_flag(self, timeout: float | None = None) -> str:
        """Return ``navigator.webdriver`` — expect ``false``/``undefined``."""
        return self.eval("navigator.webdriver", timeout=timeout)
