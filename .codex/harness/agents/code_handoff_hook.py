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
FIELD_PATTERN = r"(?P<emphasis>\*\*)?{label}:(?(emphasis)(?:\*\*)?)[ \t]*(?P<value>.*?)"
FIELD_PREFIX_PATTERN = re.compile(r"^[ \t]*(?:[-*][ \t]+)?(?:#{1,6}[ \t]+)?")
FENCE_PATTERN = re.compile(r"(?:`{3,}[^`]*|~{3,}.*)")
FENCE_OPEN_PATTERN = re.compile(r"^ {0,3}(?P<fence>`{3,}|~{3,})(?P<info>.*)$")
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
    base_directory = Path(cwd) if isinstance(cwd, str) and cwd else None
    tool_input = event.get("tool_input")
    workdir = tool_input.get("workdir") if isinstance(tool_input, dict) else None
    if isinstance(workdir, str) and workdir:
        directory = Path(workdir)
        if not directory.is_absolute():
            directory = (base_directory or Path(os.getcwd())) / directory
    else:
        directory = base_directory or Path(os.getcwd())
    root_result = _run_git(directory, "rev-parse", "--show-toplevel")
    if root_result is None:
        return None
    line_ending = b"\r\n" if os.name == "nt" and root_result.endswith(b"\r\n") else b"\n"
    root = Path(root_result.removesuffix(line_ending).decode("utf-8", errors="surrogateescape"))
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
    failures = _validate_handoff_evidence(message, frozenset({PASSING_RESULT}), require_review=True)
    if not isinstance(message, str):
        return failures
    statuses = _field_values(message, "Task Status")
    results = _field_values(message, "Review Result")
    legacy_passing_review = not statuses and results == [PASSING_RESULT]
    if ["Completed"] != statuses and not legacy_passing_review:
        failures.append("exactly one Task Status: Completed or legacy Review Result: Mergeable")
    if not _field_value(message, "Commit Message"):
        failures.append("nonempty Commit Message")
    return failures


def validate_paused_handoff(message: Any) -> list[str]:
    """Return every missing or invalid user-blocked handoff element."""
    failures = _validate_handoff_evidence(message, PAUSED_RESULTS | {PASSING_RESULT})
    if not isinstance(message, str):
        return failures
    statuses = _field_values(message, "Task Status")
    if ["Awaiting User"] != statuses:
        failures.append("exactly one Task Status: Awaiting User")
    if not _field_value(message, "Required User Action"):
        failures.append("Required User Action")
    if _field_values(message, "Commit Message"):
        failures.append("Commit Message must be omitted while awaiting the user")
    return failures


def _validate_handoff_evidence(message: Any, allowed_results: frozenset[str], require_review: bool = False) -> list[str]:
    if not isinstance(message, str) or not message.strip():
        return ["nonempty handoff"]
    failures = []
    results = _field_values(message, "Review Result")
    if results and (len(results) != 1 or results[0] not in allowed_results):
        failures.append("exactly one allowed Review Result when declared")
    if require_review and not results:
        failures.append("exactly one Review Result: Mergeable")
    reports = _markdown_review_reports(message)
    if results and len(reports) != 1:
        failures.append("one complete review report in a markdown-labelled fenced block")
    if require_review and len(reports) == 1:
        for section in ("Result", "Evidence", "Coverage"):
            if not _section_content(reports[0], f"### {section}"):
                failures.append(f"review {section}")
        if _field_values(reports[0], "Commit Message"):
            failures.append("Commit Message must be separate from the review report")
    if not require_review and not _field_value(message, "Evidence") and not _section_content(message, "### Evidence"):
        failures.append("Evidence")
    return failures


def _markdown_review_reports(message: str) -> list[str]:
    result = []
    fence = ""
    language = ""
    content = []
    for line in message.splitlines():
        if fence:
            if re.fullmatch(rf" {{0,3}}{re.escape(fence[0])}{{{len(fence)},}}[ \t]*", line):
                report = "\n".join(content)
                if language == "markdown" and _field_values(report, "Review Result"):
                    result.append(report)
                fence = ""
            else:
                content.append(line)
            continue
        opening = FENCE_OPEN_PATTERN.fullmatch(line)
        if opening and not (opening.group("fence").startswith("`") and "`" in opening.group("info")):
            fence = opening.group("fence")
            language = opening.group("info").strip()
            content = []
    return result


def _field_values(message: str, label: str) -> list[str]:
    result: list[str] = []
    pattern = re.compile(FIELD_PATTERN.format(label=re.escape(label)), re.IGNORECASE)
    for line in message.splitlines():
        normalized = FIELD_PREFIX_PATTERN.sub("", line, count=1).strip()
        emphasis_size = len(normalized) - len(normalized.lstrip("*"))
        if emphasis_size and normalized.endswith("*" * emphasis_size):
            normalized = normalized[emphasis_size:-emphasis_size].strip()
        match = pattern.fullmatch(normalized)
        if match:
            result.append(match.group("value").strip())
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
        if line.strip() and not FENCE_PATTERN.fullmatch(line.strip()):
            content.append(line.strip())
    return "\n".join(content)


def _field_value(message: str, label: str) -> str:
    values = _field_values(message, label)
    value = values[0] if values else ""
    return "" if FENCE_PATTERN.fullmatch(value) else value


def _continuation_reason(passing_failures: list[str], paused_failures: list[str]) -> str:
    passing = ", ".join(passing_failures)
    paused = ", ".join(paused_failures)
    return (
        "The code-writing task has an unresolved handoff. Report Task Status: Completed with a nonempty Commit Message after required checks and full review. "
        f"Passing handoff failures: {passing}. "
        "Completion requires one actual Review Result: Mergeable report in a markdown-labelled fenced block with Result, Evidence and Coverage. "
        "If an exact user decision, authority, or unavailable evidence blocks continuation, report "
        f"Task Status: Awaiting User with Evidence and Required User Action; pause failures: {paused}."
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
