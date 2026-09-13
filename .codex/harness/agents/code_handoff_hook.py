#!/usr/bin/env python3

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements. See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Require an explicit review handoff after Codex changes implementation files."""

from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
from typing import Any

PROSE_SUFFIXES = frozenset({".adoc", ".md", ".rst"})
PATCH_FILE_PATTERN = re.compile(r"^\*\*\* (?:Add|Delete|Update) File: (.+)$", re.MULTILINE)
FIELD_PATTERN = r"^[ \t]*(?:[-*][ \t]+)?(?:#{{1,6}}[ \t]+)?(?:\*\*)?{label}:[ \t]*(.*?)(?:\*\*)?[ \t]*$"
BOLD_FIELD_PATTERN = r"^[ \t]*(?:[-*][ \t]+)?\*\*{label}:[ \t]*(.*?)\*\*[ \t]*$"
FORMAL_REVIEW_PATTERN = re.compile(r"^```markdown\s*$\n(.*?)^```\s*$", re.IGNORECASE | re.MULTILINE | re.DOTALL)
TEXT_BLOCK_PATTERN = re.compile(r"^```text\s*$\n(.*?)^```\s*$", re.IGNORECASE | re.MULTILINE | re.DOTALL)
SHELL_TOOL_NAMES = frozenset({"Bash", "exec_command", "write_stdin"})
PASSING_RESULT = "Mergeable"
PAUSED_RESULTS = frozenset({"Not Mergeable", "Review Incomplete"})


def process_event(event: dict[str, Any], state_directory: Path) -> dict[str, str]:
    """Process one Codex hook event."""
    event_name = event.get("hook_event_name")
    if "PreToolUse" == event_name:
        _record_pre_tool_state(event, state_directory)
        return {}
    if "PostToolUse" == event_name:
        _record_implementation_change(event, state_directory)
        return {}
    if "Stop" != event_name:
        return {}
    marker_path = _marker_path(event, state_directory)
    if not marker_path.exists():
        return {}
    message = event.get("last_assistant_message")
    passing_failures = validate_handoff(message)
    if not passing_failures:
        marker_path.unlink()
        return {}
    paused_failures = validate_paused_handoff(message)
    if not paused_failures:
        return {}
    return {"decision": "block", "reason": _continuation_reason(passing_failures, paused_failures)}


def _record_pre_tool_state(event: dict[str, Any], state_directory: Path) -> None:
    if event.get("tool_name") not in SHELL_TOOL_NAMES:
        return
    snapshot_path = _snapshot_path(event, state_directory)
    snapshot_path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    snapshot = _repository_snapshot(event)
    snapshot_path.write_text(json.dumps(snapshot, sort_keys=True), encoding="utf-8")


def _record_implementation_change(event: dict[str, Any], state_directory: Path) -> None:
    tool_name = event.get("tool_name")
    if tool_name in SHELL_TOOL_NAMES:
        _record_shell_change(event, state_directory)
        return
    if "apply_patch" != tool_name:
        return
    tool_input = event.get("tool_input")
    patch = None
    if isinstance(tool_input, dict):
        patch = tool_input.get("command") or tool_input.get("patch")
    if not isinstance(patch, str):
        _mark_pending(event, state_directory)
        return
    if not _changes_implementation_file(patch):
        return
    _mark_pending(event, state_directory)


def _record_shell_change(event: dict[str, Any], state_directory: Path) -> None:
    snapshot_path = _snapshot_path(event, state_directory)
    try:
        before = json.loads(snapshot_path.read_text(encoding="utf-8"))
    except (OSError, TypeError, ValueError, json.JSONDecodeError):
        _mark_pending(event, state_directory)
        return
    finally:
        snapshot_path.unlink(missing_ok=True)
    after = _repository_snapshot(event)
    if not isinstance(before, dict) or after is None or before.get("root") != after.get("root"):
        _mark_pending(event, state_directory)
        return
    before_files = before.get("files", {})
    after_files = after.get("files", {})
    if not isinstance(before_files, dict) or not isinstance(after_files, dict):
        _mark_pending(event, state_directory)
        return
    changed_paths = {
        each for each in before_files.keys() | after_files.keys()
        if before_files.get(each) != after_files.get(each)
    }
    if any(Path(each).suffix.lower() not in PROSE_SUFFIXES for each in changed_paths):
        _mark_pending(event, state_directory)


def _mark_pending(event: dict[str, Any], state_directory: Path) -> None:
    marker_path = _marker_path(event, state_directory)
    marker_path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    marker_path.touch(exist_ok=True)


def _changes_implementation_file(patch: str) -> bool:
    paths = PATCH_FILE_PATTERN.findall(patch)
    return not paths or any(Path(each).suffix.lower() not in PROSE_SUFFIXES for each in paths)


def _marker_path(event: dict[str, Any], state_directory: Path) -> Path:
    session_id = event.get("session_id")
    if not isinstance(session_id, str) or not session_id:
        raise ValueError("Codex hook input is missing session_id")
    marker_name = hashlib.sha256(session_id.encode("utf-8")).hexdigest()
    return state_directory / f"{marker_name}.pending"


def _snapshot_path(event: dict[str, Any], state_directory: Path) -> Path:
    tool_use_id = event.get("tool_use_id")
    if not isinstance(tool_use_id, str) or not tool_use_id:
        raise ValueError("Codex hook input is missing tool_use_id")
    session_name = _marker_path(event, state_directory).stem
    tool_name = hashlib.sha256(tool_use_id.encode("utf-8")).hexdigest()
    return state_directory / f"{session_name}.{tool_name}.before.json"


def _repository_snapshot(event: dict[str, Any]) -> dict[str, Any] | None:
    cwd = event.get("cwd")
    tool_input = event.get("tool_input")
    if not isinstance(cwd, str) or not cwd:
        cwd = tool_input.get("workdir") if isinstance(tool_input, dict) else None
    if not isinstance(cwd, str) or not cwd:
        cwd = os.getcwd()
    root_result = _run_git(Path(cwd), "rev-parse", "--show-toplevel")
    if root_result is None:
        return None
    root = Path(root_result.decode("utf-8", errors="surrogateescape").strip())
    status = _run_git(root, "status", "--porcelain=v1", "-z", "--untracked-files=all")
    if status is None:
        return None
    entries = _status_entries(status)
    files = {
        path: f"{state}:{_path_digest(root / path)}"
        for path, state in entries.items()
    }
    return {"root": str(root.resolve()), "files": files}


def _run_git(cwd: Path, *arguments: str) -> bytes | None:
    try:
        result = subprocess.run(
            ["git", "-C", str(cwd), *arguments], check=False, stdout=subprocess.PIPE,
            stderr=subprocess.DEVNULL, timeout=3,
        )
    except (OSError, subprocess.TimeoutExpired):
        return None
    return result.stdout if 0 == result.returncode else None


def _status_entries(status: bytes) -> dict[str, str]:
    records = status.split(b"\0")
    result: dict[str, str] = {}
    index = 0
    while index < len(records):
        record = records[index]
        index += 1
        if len(record) < 4:
            continue
        state = record[:2].decode("ascii", errors="replace")
        path = record[3:].decode("utf-8", errors="surrogateescape")
        result[path] = state
        if ("R" in state or "C" in state) and index < len(records):
            original = records[index].decode("utf-8", errors="surrogateescape")
            index += 1
            result[original] = state
    return result


def _path_digest(path: Path) -> str:
    try:
        mode = path.lstat().st_mode
        if path.is_symlink():
            value = os.readlink(path).encode("utf-8", errors="surrogateescape")
        elif path.is_file():
            value = path.read_bytes()
        else:
            value = b"<missing>"
    except OSError:
        mode = 0
        value = b"<unreadable>"
    return hashlib.sha256(f"{mode}:".encode("ascii") + value).hexdigest()


def validate_handoff(message: Any) -> list[str]:
    """Return every missing or invalid successful code handoff element."""
    failures = _validate_formal_review(message, frozenset({PASSING_RESULT}))
    text_blocks = TEXT_BLOCK_PATTERN.findall(message) if isinstance(message, str) else []
    if not any(_field_value(each, "Commit Message") for each in text_blocks):
        failures.append("Commit Message")
    return failures


def validate_paused_handoff(message: Any) -> list[str]:
    """Return every missing or invalid user-blocked handoff element."""
    failures = _validate_formal_review(message, PAUSED_RESULTS)
    if not isinstance(message, str):
        return failures
    text_blocks = TEXT_BLOCK_PATTERN.findall(message)
    handoff = next((each for each in text_blocks if _field_value(each, "Task Status")), "")
    if "Awaiting User" != _field_value(handoff, "Task Status"):
        failures.append("Task Status: Awaiting User")
    if not _field_value(handoff, "Required User Action"):
        failures.append("Required User Action")
    if _field_value(message, "Commit Message"):
        failures.append("Commit Message must be omitted while awaiting the user")
    return failures


def _validate_formal_review(message: Any, allowed_results: frozenset[str]) -> list[str]:
    if not isinstance(message, str) or not message.strip():
        return ["fenced markdown Formal Review"]
    blocks = FORMAL_REVIEW_PATTERN.findall(message)
    formal_blocks = [each for each in blocks if "### Result" in each]
    if 1 != len(formal_blocks):
        return ["exactly one fenced markdown Formal Review"]
    review = formal_blocks[0]
    failures = []
    results = _review_results(review)
    if 1 != len(results) or results[0][0] not in allowed_results or not results[0][1]:
        failures.append("exactly one bold allowed Review Result")
        return failures
    result = results[0][0]
    required_sections = ["### Result", "### Coverage"]
    if PASSING_RESULT == result:
        required_sections.append("### Evidence")
    elif "Not Mergeable" == result:
        required_sections.append("### Blocking Issues")
        feedback_mode = _field_value(review, "Feedback Mode")
        if feedback_mode not in {"Change Request", "Needs Discussion"}:
            failures.append("Feedback Mode")
        blocking_issues = _bold_field_value(review, "Blocking Issues")
        if not blocking_issues.isdigit() or 0 == int(blocking_issues):
            failures.append("positive bold Blocking Issues count")
    else:
        required_sections.extend(("### Verified Facts", "### Required Evidence"))
    failures.extend(each for each in required_sections if not _section_content(review, each))
    result_content = _section_content(review, "### Result")
    if not any("review result:" not in each.casefold() for each in result_content.splitlines()):
        failures.append("concise Result reason")
    return failures


def _review_results(review: str) -> list[tuple[str, bool]]:
    result: list[tuple[str, bool]] = []
    for line in review.splitlines():
        stripped = line.strip()
        if stripped.startswith(("- ", "* ")):
            stripped = stripped[2:].strip()
        is_bold = stripped.startswith("**") and stripped.endswith("**")
        normalized = stripped.strip("*").strip()
        if normalized.lower().startswith("review result:"):
            result.append((normalized.split(":", 1)[1].strip(), is_bold))
    return result


def _section_content(review: str, heading: str) -> str:
    lines = review.splitlines()
    try:
        start = next(index for index, line in enumerate(lines) if line.strip() == heading) + 1
    except StopIteration:
        return ""
    content = []
    for line in lines[start:]:
        if line.strip().startswith("### "):
            break
        if line.strip():
            content.append(line.strip())
    return "\n".join(content)


def _field_value(message: str, label: str) -> str:
    pattern = re.compile(FIELD_PATTERN.format(label=re.escape(label)), re.IGNORECASE | re.MULTILINE)
    match = pattern.search(message)
    return match.group(1).strip() if match else ""


def _bold_field_value(message: str, label: str) -> str:
    pattern = re.compile(BOLD_FIELD_PATTERN.format(label=re.escape(label)), re.IGNORECASE | re.MULTILINE)
    match = pattern.search(message)
    return match.group(1).strip() if match else ""


def _continuation_reason(passing_failures: list[str], paused_failures: list[str]) -> str:
    passing = ", ".join(passing_failures)
    paused = ", ".join(paused_failures)
    return (
        "The code-writing task cannot stop successfully without the latest passing $review-pr Formal Review. "
        f"Passing handoff failures: {passing}. "
        "Fix safe in-scope findings, rerun invalidated checks, and repeat Formal Review until Mergeable. "
        "If an exact user decision, authority, or unavailable evidence blocks continuation, return the complete non-passing "
        f"Formal Review with Task Status: Awaiting User and Required User Action; pause failures: {paused}."
    )


def main() -> int:
    """Read a Codex hook event and write its JSON result."""
    try:
        event = json.load(sys.stdin)
        if not isinstance(event, dict):
            raise ValueError("Codex hook input must be a JSON object")
        state_directory = Path(tempfile.gettempdir()) / "shardingsphere-codex-code-handoff"
        result = process_event(event, state_directory)
    except (OSError, TypeError, ValueError, json.JSONDecodeError):
        result = {
            "decision": "block",
            "reason": "The code handoff validator failed closed. Inspect the project hook before stopping.",
        }
    json.dump(result, sys.stdout)
    return 0


if __name__ == "__main__":
    sys.exit(main())
