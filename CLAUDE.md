# CLAUDE.md

Guidance for AI assistants (Claude Code and others) working in this repository.

## What this repo is

This is a **browser-automation project**. Its purpose is to drive a *real* Google
Chrome Beta instance — not a Playwright-managed browser — so that automated
sessions are indistinguishable from a human user. This matters for sites that
actively detect and block automation (LinkedIn, job boards, etc.) and for
running many parallel browser sessions that share one real login state.

The connection model, gotchas, and known bugs are documented below (see
"The real-browser model" and "Reliability conventions") — read those before
doing any browser work; they are the source of truth for the exact commands.

The Python package `voicefuck` (under `src/`) is a thin, timeout-guarded wrapper
over the `agent-browser` CLI + Chrome's CDP HTTP endpoint. It enforces these
invariants in code (always `--cdp --session`, unique session ids, hard
timeouts, background-tab opening). See [`README.md`](README.md) for usage.

## Repository layout

```
.
├── CLAUDE.md                          # This file
├── README.md                          # User-facing usage + quick start
├── pyproject.toml                     # Package metadata; runtime is dependency-free
├── src/
│   └── voicefuck/
│       ├── __init__.py                # Public API re-exports
│       ├── cdp.py                     # CDP HTTP helpers: launch, version, background tabs
│       ├── session.py                 # BrowserSession: named tab over --cdp --session
│       ├── parallel.py                # batch_open_tabs, run_parallel
│       └── cli.py                     # `voicefuck` CLI (status/launch/open/check)
└── tests/                             # pytest; no running Chrome required (stubbed)
    ├── test_session.py
    └── test_cdp.py
```

## Development

- **Install:** `pip install -e ".[dev]"` (Python 3.10+).
- **Test:** `pytest` — unit tests stub `subprocess`/network, so no running Chrome
  is needed. Keep it that way: tests must not require a live browser.
- **Runtime deps:** none. The package shells out to `agent-browser` and uses only
  the standard library for CDP HTTP. Don't add runtime dependencies without cause.
- **Public API:** re-exported from `voicefuck/__init__.py`; keep it in sync when
  adding modules.

## The real-browser model (essentials)

The whole approach hinges on attaching to a real Chrome via the Chrome DevTools
Protocol (CDP) instead of launching an automation-flagged browser. Keep these
invariants — most bugs come from breaking one of them:

- **Never launch Chrome with `agent-browser open` / `--headed`.** That produces a
  Playwright-controlled browser that sites detect. Launch Chrome Beta yourself
  with `--remote-debugging-port=9222` and a dedicated `--user-data-dir`.
- **Attach with `--cdp 9222 --session <name>` on *every* `agent-browser`
  command.** `--cdp` attaches to the running Chrome; `--session` isolates a named
  tab. There is no separate `connect` step.
- **Do not use `agent-browser --session <name> connect 9222`.** The `connect`
  subcommand opens a blank `about:blank` tab instead of attaching (known
  Rust-daemon bug, verified v0.22.3).
- **Generate a unique 6-char session id per agent run.** Never reuse `main` or a
  hardcoded name — two agents on the same session name clobber each other's tab
  silently.
- **`--user-data-dir` is required** for remote debugging to enable. The
  persistent profile at `~/.chrome-beta-profile` keeps cookies/logins across
  restarts, which is how parallel sessions share one login.
- **Never quit/kill/restart Chrome unless the user explicitly asks.** The user
  may be driving the same browser manually. If Chrome needs the debug port and
  was started without it, ask the user to relaunch — don't kill it yourself.

Verify you're clean with `agent-browser eval 'navigator.webdriver'` → expect
`false`/`undefined`.

## Reliability conventions

- **Hard-timeout every browser command** (`timeout 15 agent-browser …`). The main
  failure mode is an unbounded wait; on timeout, snapshot last-known state and
  decide retry vs. skip rather than tweaking config.
- **Checkpoint multi-step flows.** Break long flows (e.g. form fill → upload →
  submit) into discrete steps and snapshot after each, so you can resume from the
  failed step instead of restarting.
- **Batch tab opening.** Opening tabs steals OS focus. Prefer CDP background tabs
  (`curl "http://localhost:9222/json/new?<url>"`) for zero focus steal; otherwise
  open all tabs in one tight loop before doing any work. Never interleave
  open→scrape→open→scrape.
- **After upgrading `agent-browser`, kill stale daemons first**
  (`pkill -f agent-browser`, clear sockets, reinstall). A new CLI talking to an
  old daemon fails silently (blank pages, missing cookies) with no version
  warning.

## Working in this repo

- **Branch:** develop on the branch assigned for the session
  (`claude/session-*`); create it from the latest `main` if needed. Do not push
  to `main` or any other branch without explicit permission.
- **Commits:** clear, descriptive messages; commit and push only when the work is
  complete or the user asks.
- **Pull requests:** do not open a PR unless the user explicitly asks for one.
- **Temporary files:** write scratch files to a temp/scratch directory, not into
  the repo tree.

## Environment notes

- This may run in an ephemeral remote container (fresh clone per session).
  Anything worth keeping must be committed and pushed.
- The launch/quit commands assume **macOS Chrome Beta paths**
  (`/Applications/Google Chrome Beta.app`, `osascript`). On other platforms they
  need adapting; the CDP model (`--cdp 9222 --session`) is platform-independent.
