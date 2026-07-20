"""voicefuck — detection-resistant, parallel browser automation.

Drives a *real* Google Chrome Beta instance over the Chrome DevTools Protocol
(CDP) via the ``agent-browser`` CLI, rather than launching an automation-flagged
Playwright browser that sites can detect.

The design rules this package enforces are documented in
``.claude/skills/real-browser/SKILL.md``. The load-bearing invariants:

* Never launch Chrome through ``agent-browser`` — start it yourself with
  ``--remote-debugging-port`` and a dedicated ``--user-data-dir``.
* Every ``agent-browser`` command carries ``--cdp <port> --session <id>``.
* Each agent run gets a unique 6-char session id (never a shared/hardcoded name).
* Every browser command is hard-timed-out.
"""

from .cdp import (
    CDP_PORT,
    ChromeNotRunningError,
    chrome_version,
    is_debugger_up,
    launch_chrome,
    list_targets,
    new_background_tab,
)
from .session import BrowserCommandError, BrowserSession, BrowserTimeout, gen_session_id
from .parallel import batch_open_tabs, run_parallel

__version__ = "0.1.0"

__all__ = [
    "__version__",
    # cdp
    "CDP_PORT",
    "ChromeNotRunningError",
    "chrome_version",
    "is_debugger_up",
    "launch_chrome",
    "list_targets",
    "new_background_tab",
    # session
    "BrowserSession",
    "BrowserCommandError",
    "BrowserTimeout",
    "gen_session_id",
    # parallel
    "batch_open_tabs",
    "run_parallel",
]
