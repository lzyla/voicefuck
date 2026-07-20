"""Unit tests for BrowserSession command construction and safety invariants.

These do not require a running Chrome — subprocess is stubbed so we assert on the
exact argv voicefuck builds.
"""

from __future__ import annotations

import subprocess

import pytest

from voicefuck.session import (
    BrowserCommandError,
    BrowserSession,
    BrowserTimeout,
    gen_session_id,
)


def test_gen_session_id_is_unique_and_shaped():
    ids = {gen_session_id() for _ in range(200)}
    assert len(ids) == 200  # effectively no collisions
    for sid in ids:
        assert len(sid) == 6
        assert sid.isalnum() and sid.islower() or sid.isdigit() or sid.isalnum()


def test_default_session_id_is_not_hardcoded():
    a, b = BrowserSession(), BrowserSession()
    assert a.session_id != b.session_id
    assert a.session_id != "main"


def _capture_cmd(monkeypatch):
    seen = {}

    def fake_run(cmd, capture_output, text, timeout):  # noqa: ANN001
        seen["cmd"] = cmd
        seen["timeout"] = timeout
        return subprocess.CompletedProcess(cmd, 0, stdout="ok\n", stderr="")

    monkeypatch.setattr(subprocess, "run", fake_run)
    return seen


def test_every_command_carries_cdp_and_session(monkeypatch):
    seen = _capture_cmd(monkeypatch)
    s = BrowserSession(session_id="abc123", port=9222)
    s.snapshot(interactive=True)
    assert seen["cmd"] == [
        "agent-browser", "--cdp", "9222", "--session", "abc123", "snapshot", "-i",
    ]


def test_fill_builds_expected_argv(monkeypatch):
    seen = _capture_cmd(monkeypatch)
    BrowserSession(session_id="zzz999").fill("@e1", "Jane Doe")
    assert seen["cmd"][-3:] == ["fill", "@e1", "Jane Doe"]


def test_default_timeout_is_applied(monkeypatch):
    seen = _capture_cmd(monkeypatch)
    BrowserSession(session_id="t", default_timeout=15).click("@e1")
    assert seen["timeout"] == 15


def test_per_call_timeout_overrides_default(monkeypatch):
    seen = _capture_cmd(monkeypatch)
    BrowserSession(session_id="t", default_timeout=15).click("@e1", timeout=3)
    assert seen["timeout"] == 3


def test_timeout_raises_browser_timeout(monkeypatch):
    def fake_run(cmd, capture_output, text, timeout):  # noqa: ANN001
        raise subprocess.TimeoutExpired(cmd, timeout)

    monkeypatch.setattr(subprocess, "run", fake_run)
    with pytest.raises(BrowserTimeout):
        BrowserSession(session_id="t").snapshot()


def test_nonzero_exit_raises_command_error(monkeypatch):
    def fake_run(cmd, capture_output, text, timeout):  # noqa: ANN001
        return subprocess.CompletedProcess(cmd, 2, stdout="", stderr="boom")

    monkeypatch.setattr(subprocess, "run", fake_run)
    with pytest.raises(BrowserCommandError) as ei:
        BrowserSession(session_id="t").click("@e1")
    assert ei.value.returncode == 2
    assert "boom" in ei.value.stderr


def test_webdriver_flag_uses_eval(monkeypatch):
    seen = _capture_cmd(monkeypatch)
    BrowserSession(session_id="t").webdriver_flag()
    assert seen["cmd"][-2:] == ["eval", "navigator.webdriver"]
