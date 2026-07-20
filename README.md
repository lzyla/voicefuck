# voicefuck

Detection-resistant, parallel browser automation. `voicefuck` drives a **real
Google Chrome Beta** instance over the Chrome DevTools Protocol (CDP) via the
[`agent-browser`](https://github.com/) CLI — not a Playwright-managed browser —
so automated sessions are indistinguishable from a human user. This is what lets
it work against sites that actively detect and block automation (LinkedIn, job
boards, …) and run many parallel sessions that share one real login.

The connection model, gotchas, and known bugs live in
[`.claude/skills/real-browser/SKILL.md`](.claude/skills/real-browser/SKILL.md).
Read it before doing browser work.

## Install

```bash
pip install -e ".[dev]"
```

Python 3.10+. The runtime has **no dependencies** — it shells out to the
`agent-browser` CLI and talks to Chrome's CDP HTTP endpoint using only the
standard library. You must have `agent-browser` on your `PATH` and Chrome Beta
installed separately.

## Prerequisites: launch real Chrome yourself

Never let `agent-browser` launch the browser. Start Chrome Beta with the debug
port and a dedicated profile:

```bash
"/Applications/Google Chrome Beta.app/Contents/MacOS/Google Chrome Beta" \
  --remote-debugging-port=9222 --user-data-dir="$HOME/.chrome-beta-profile"
```

`--user-data-dir` is required for remote debugging to enable; the persistent
profile keeps cookies/logins across restarts.

## Quick start

```python
from voicefuck import BrowserSession, batch_open_tabs

# Open several tabs up front with zero OS-focus steal (CDP background tabs).
batch_open_tabs([
    "https://example.com/a",
    "https://example.com/b",
])

# Each session is a named tab attached over CDP with a unique 6-char id.
s = BrowserSession()                 # port 9222, hard 15s timeout per command
s.open("https://example.com")
print(s.snapshot(interactive=True))
assert s.webdriver_flag() in ("false", "undefined", "")   # not automation-flagged
s.close_tab()
```

Run tasks concurrently, each with its own session (all sharing one login):

```python
from voicefuck import run_parallel

def apply(session):
    session.snapshot(interactive=True)
    session.fill("@e1", "Jane Doe")
    return session.session_id

results = run_parallel([apply, apply, apply])
```

## CLI

```bash
voicefuck status                     # is the CDP debug port up?
voicefuck launch                     # launch Chrome Beta with the debug port
voicefuck open https://a https://b   # batch-open background tabs
voicefuck check                      # report navigator.webdriver on a fresh session
```

## Design invariants

These are enforced in code and must not be broken (see the skill for why):

- **Never launch Chrome via `agent-browser`** — start the real binary yourself.
- **Every command carries `--cdp <port> --session <id>`** (`BrowserSession` does
  this); there is no separate `connect` step, and `connect` is buggy.
- **Unique 6-char session id per run** — never `main` or a hardcoded name.
- **Hard timeout on every command** — the main failure mode is an unbounded wait.
- **Batch-open tabs up front** — prefer CDP background tabs; never interleave
  open→scrape→open→scrape.
- **Never kill/restart Chrome unless the user asks** — they may be driving it.

## Development

```bash
pytest            # unit tests; no running Chrome required (subprocess/network stubbed)
```

## Platform note

The `agent-browser` skill and the default launch path assume **macOS Chrome
Beta**. The CDP model (`--cdp <port> --session`) is platform-independent; on
other platforms pass `chrome_binary=...` to `launch_chrome()` and adapt the
launch/quit commands.
