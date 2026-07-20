"""Unit tests for CDP HTTP helpers (network stubbed)."""

from __future__ import annotations

import json

import pytest

from voicefuck import cdp
from voicefuck.cdp import ChromeNotRunningError


class _FakeResp:
    def __init__(self, body: str):
        self._body = body.encode("utf-8")

    def read(self):
        return self._body

    def __enter__(self):
        return self

    def __exit__(self, *exc):
        return False


def _stub_urlopen(monkeypatch, capture, body):
    def fake_urlopen(url, timeout):  # noqa: ANN001
        capture["url"] = url
        return _FakeResp(body)

    monkeypatch.setattr(cdp.urllib.request, "urlopen", fake_urlopen)


def test_is_debugger_up_true(monkeypatch):
    _stub_urlopen(monkeypatch, {}, json.dumps({"Browser": "Chrome/999"}))
    assert cdp.is_debugger_up(9222) is True


def test_is_debugger_up_false_on_error(monkeypatch):
    def boom(url, timeout):  # noqa: ANN001
        raise OSError("connection refused")

    monkeypatch.setattr(cdp.urllib.request, "urlopen", boom)
    assert cdp.is_debugger_up(9222) is False


def test_chrome_version_raises_when_down(monkeypatch):
    def boom(url, timeout):  # noqa: ANN001
        raise OSError("connection refused")

    monkeypatch.setattr(cdp.urllib.request, "urlopen", boom)
    with pytest.raises(ChromeNotRunningError):
        cdp.chrome_version(9222)


def test_new_background_tab_url_encoded(monkeypatch):
    cap: dict = {}
    _stub_urlopen(monkeypatch, cap, json.dumps({"id": "T1", "url": "about:blank"}))
    tab = cdp.new_background_tab("https://site.com/a?x=1&y=2", port=9222)
    assert tab["id"] == "T1"
    # The target URL must be percent-encoded into the /json/new query.
    assert "%3A%2F%2F" in cap["url"]
    assert "%26" in cap["url"]  # the '&' is encoded, not a second query param


def test_list_targets_returns_list(monkeypatch):
    _stub_urlopen(monkeypatch, {}, json.dumps([{"id": "A"}, {"id": "B"}]))
    assert [t["id"] for t in cdp.list_targets(9222)] == ["A", "B"]
