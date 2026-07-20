"""Parallel-session helpers.

Multiple ``BrowserSession`` instances share one Chrome instance (and its login
state), each in its own tab. Two rules keep this smooth:

* Batch-open all tabs up front — opening tabs one-by-one interleaved with work
  repeatedly steals OS focus. CDP background tabs steal none.
* Give every session a unique id (``BrowserSession`` does this by default).
"""

from __future__ import annotations

from concurrent.futures import ThreadPoolExecutor
from typing import Callable, Iterable, TypeVar

from .cdp import CDP_PORT, new_background_tab
from .session import BrowserSession

T = TypeVar("T")


def batch_open_tabs(
    urls: Iterable[str], port: int = CDP_PORT
) -> list[dict]:
    """Open every URL as a background tab up front (zero focus steal).

    Returns the CDP target metadata for each created tab, in input order.
    """
    return [new_background_tab(url, port=port) for url in urls]


def run_parallel(
    tasks: Iterable[Callable[[BrowserSession], T]],
    port: int = CDP_PORT,
    max_workers: int | None = None,
) -> list[T]:
    """Run each task against its own fresh ``BrowserSession`` concurrently.

    Each callable receives a ``BrowserSession`` with a unique id. Results are
    returned in task order. Exceptions propagate (per-task isolation is the
    caller's job if partial success is desired).
    """
    task_list = list(tasks)
    sessions = [BrowserSession(port=port) for _ in task_list]

    def _invoke(pair: tuple[Callable[[BrowserSession], T], BrowserSession]) -> T:
        fn, session = pair
        return fn(session)

    with ThreadPoolExecutor(max_workers=max_workers or len(task_list) or 1) as pool:
        return list(pool.map(_invoke, zip(task_list, sessions)))
