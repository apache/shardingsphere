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

"""Require an explicit review handoff after Codex changes task deliverables."""

from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import tempfile
from typing import Any

sys.dont_write_bytecode = True
SCOPE_SPEC = importlib.util.spec_from_file_location(
    "code_handoff_review_scope", Path(__file__).parents[2] / "skills/review-pr/scripts/resolve_review_scope.py",
)
review_scope = importlib.util.module_from_spec(SCOPE_SPEC)
sys.modules[SCOPE_SPEC.name] = review_scope
SCOPE_SPEC.loader.exec_module(review_scope)

FIELD_PATTERN = r"(?P<emphasis>\*\*)?{label}:(?(emphasis)(?:\*\*)?)[ \t]*(?P<value>.*?)"
FIELD_PREFIX_PATTERN = re.compile(r"^[ \t]*(?:[-*][ \t]+)?(?:#{1,6}[ \t]+)?")
FENCE_PATTERN = re.compile(r"(?:`{3,}[^`]*|~{3,}.*)")
FENCE_OPEN_PATTERN = re.compile(r"^ {0,3}(?P<fence>`{3,}|~{3,})(?P<info>.*)$")
SNAPSHOT_TOOL_NAMES = frozenset({"Bash", "apply_patch", "exec_command", "write_stdin"})
COMMIT_EXEMPT_SUFFIXES = frozenset({".adoc", ".md", ".rst"})
PASSING_RESULT = "Mergeable"
PAUSED_RESULTS = frozenset({"Not Mergeable", "Review Incomplete"})


def process_event(event: dict[str, Any], state_directory: Path) -> dict[str, str]:
    """Process one Codex hook event."""
    event_name = event.get("hook_event_name")
    if "PreToolUse" == event_name:
        _record_pre_tool_state(event, state_directory)
        return {}
    if "PostToolUse" == event_name:
        _record_tool_change(event, state_directory)
        return {}
    if "Stop" != event_name:
        return {}
    registration = _load_registration(event, state_directory)
    current_targets = _current_targets(registration[1]) if registration else {}
    changed_targets = {
        path for path, metadata in current_targets.items()
        if metadata != registration[0]["reviewed"].get(path)
    } if registration else set()
    if changed_targets:
        _mark_pending(event, state_directory, require_commit=_requires_commit_message(changed_targets))
    marker_path = _marker_path(event, state_directory)
    message = event.get("last_assistant_message")
    if not marker_path.exists():
        if registration and isinstance(message, str) and _field_values(message, "Task Status") == ["Completed"]:
            registration[0]["completed"] = True
            _registration_path(event, state_directory).write_text(json.dumps(registration[0], sort_keys=True), encoding="utf-8")
        return {}
    require_commit = _pending_requires_commit(marker_path)
    passing_failures = validate_handoff(message, require_commit=require_commit)
    if not passing_failures:
        if registration:
            registration[0]["reviewed"] = current_targets
            registration[0]["completed"] = True
            _registration_path(event, state_directory).write_text(json.dumps(registration[0], sort_keys=True), encoding="utf-8")
        marker_path.unlink()
        return {}
    paused_failures = validate_paused_handoff(message)
    if not paused_failures:
        return {}
    return {"decision": "block", "reason": _continuation_reason(passing_failures, paused_failures, require_commit)}


def _record_pre_tool_state(event: dict[str, Any], state_directory: Path) -> None:
    if event.get("tool_name") not in SNAPSHOT_TOOL_NAMES:
        return
    snapshot_path = _snapshot_path(event, state_directory)
    snapshot_path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    snapshot = _change_snapshot(event, state_directory)
    snapshot_path.write_text(json.dumps(snapshot, sort_keys=True), encoding="utf-8")


def _record_tool_change(event: dict[str, Any], state_directory: Path) -> None:
    if event.get("tool_name") not in SNAPSHOT_TOOL_NAMES:
        return
    snapshot_path = _snapshot_path(event, state_directory)
    try:
        before = json.loads(snapshot_path.read_text(encoding="utf-8"))
    except (OSError, TypeError, ValueError, json.JSONDecodeError):
        _mark_pending(event, state_directory)
        return
    finally:
        snapshot_path.unlink(missing_ok=True)
    after = _change_snapshot(event, state_directory)
    if not isinstance(before, dict) or after is None or before.get("root") != after.get("root"):
        _mark_pending(event, state_directory)
        return
    before_files = before.get("files", {})
    after_files = after.get("files", {})
    if not isinstance(before_files, dict) or not isinstance(after_files, dict):
        _mark_pending(event, state_directory)
        return
    originals = after["originals"]
    for path in after_files.keys() - before_files.keys():
        if path in originals:
            before_files[path] = originals[path]
    for path in before_files.keys() - after_files.keys():
        if Path(path).is_absolute():
            after_files[path] = review_scope.read_file_object(Path(path))[0]
    changed_paths = {
        each for each in before_files.keys() | after_files.keys()
        if before_files.get(each) != after_files.get(each)
    }
    if changed_paths:
        _mark_pending(event, state_directory, require_commit=_requires_commit_message(changed_paths))


def _change_snapshot(event: dict[str, Any], state_directory: Path) -> dict[str, Any] | None:
    repository = _repository_snapshot(event)
    registration = _load_registration(event, state_directory)
    if repository is None and registration is None:
        return None
    files = repository["files"] if repository else {}
    originals = {}
    if registration:
        files.update(_current_targets(registration[1]))
        originals = {path: value[0] for path, value in registration[1].items()}
    return {"root": repository["root"] if repository else None, "files": files, "originals": originals}


def _requires_commit_message(paths: set[str]) -> bool:
    return any(Path(path).suffix.lower() not in COMMIT_EXEMPT_SUFFIXES for path in paths)


def _mark_pending(event: dict[str, Any], state_directory: Path, require_commit: bool = True) -> None:
    marker_path = _marker_path(event, state_directory)
    marker_path.parent.mkdir(mode=0o700, parents=True, exist_ok=True)
    require_commit = require_commit or (marker_path.exists() and _pending_requires_commit(marker_path))
    marker_path.write_text(json.dumps({"require_commit": require_commit}), encoding="utf-8")


def _pending_requires_commit(marker_path: Path) -> bool:
    content = marker_path.read_text(encoding="utf-8")
    return not content or json.loads(content)["require_commit"]


def register_targets(session_id: str, manifest_path: Path, state_directory: Path,
                     new_task: bool = False, append_targets: bool = False) -> None:
    """Bind a session to its original deliverable snapshots without replacing its baseline."""
    event = {"session_id": session_id}
    manifest_path = manifest_path.absolute()
    snapshots = review_scope.load_file_snapshots(manifest_path)
    manifest_hash = hashlib.sha256(manifest_path.read_bytes()).hexdigest()
    existing = _load_registration(event, state_directory)
    if append_targets:
        if existing is None:
            raise ValueError("Appending targets requires an existing task registration")
        if review_scope.file_snapshot_parent(manifest_path) != {"path": existing[0]["manifest"], "sha256": existing[0]["sha256"]}:
            raise ValueError("The extension must directly reference the current task manifest")
        reviewed = existing[0]["reviewed"]
        reviewed.update({path: value[0] for path, value in snapshots.items() if path not in reviewed})
    elif new_task:
        if existing is None or not existing[0]["completed"] or _marker_path(event, state_directory).exists():
            raise ValueError("A new task requires a completed prior handoff with no pending review")
    elif existing:
        if existing[0]["manifest"] != str(manifest_path) or existing[0]["sha256"] != manifest_hash:
            raise ValueError("The session already has an original deliverable manifest")
        return
    if not append_targets:
        reviewed = {path: value[0] for path, value in snapshots.items()}
    registration = {
        "manifest": str(manifest_path), "sha256": manifest_hash,
        "reviewed": reviewed,
        "completed": False,
    }
    state_directory.mkdir(mode=0o700, parents=True, exist_ok=True)
    _registration_path(event, state_directory).write_text(json.dumps(registration, sort_keys=True), encoding="utf-8")


def _load_registration(event: dict[str, Any], state_directory: Path) -> tuple[dict[str, Any], dict[str, Any]] | None:
    registration_path = _registration_path(event, state_directory)
    if not registration_path.exists():
        return None
    registration = json.loads(registration_path.read_text(encoding="utf-8"))
    manifest = Path(registration["manifest"])
    if registration["sha256"] != hashlib.sha256(manifest.read_bytes()).hexdigest():
        raise ValueError("The original deliverable manifest changed after registration")
    return registration, review_scope.load_file_snapshots(manifest)


def _current_targets(snapshots: dict[str, Any]) -> dict[str, Any]:
    return {path: review_scope.read_file_object(Path(path))[0] for path in snapshots}


def _registration_path(event: dict[str, Any], state_directory: Path) -> Path:
    return _marker_path(event, state_directory).with_suffix(".targets.json")


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


def validate_handoff(message: Any, require_commit: bool = True) -> list[str]:
    """Return every missing or invalid successful handoff element."""
    failures = _validate_handoff_evidence(message, frozenset({PASSING_RESULT}), require_review=True)
    if not isinstance(message, str):
        return failures
    statuses = _field_values(message, "Task Status")
    results = _field_values(message, "Review Result")
    legacy_passing_review = not statuses and results == [PASSING_RESULT]
    if ["Completed"] != statuses and not legacy_passing_review:
        failures.append("exactly one Task Status: Completed or legacy Review Result: Mergeable")
    if require_commit and not _field_value(message, "Commit Message"):
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


def _continuation_reason(passing_failures: list[str], paused_failures: list[str], require_commit: bool) -> str:
    passing = ", ".join(passing_failures)
    paused = ", ".join(paused_failures)
    commit_requirement = " with a nonempty Commit Message" if require_commit else ""
    return (
        f"The file-writing task has an unresolved handoff. Report Task Status: Completed{commit_requirement} after required checks and full review. "
        f"Passing handoff failures: {passing}. "
        "Completion requires one actual Review Result: Mergeable report in a markdown-labelled fenced block with Result, Evidence and Coverage. "
        "If an exact user decision, authority, or unavailable evidence blocks continuation, report "
        f"Task Status: Awaiting User with Evidence and Required User Action; pause failures: {paused}."
    )


def main() -> int:
    """Read a Codex hook event and write its JSON result."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--register-targets", type=Path, help="Original file-snapshots manifest for the current task.")
    parser.add_argument("--session-id", help="Session to bind to the original file-snapshots manifest.")
    registration_mode = parser.add_mutually_exclusive_group()
    registration_mode.add_argument("--new-task", action="store_true", help="Register an independent user-requested task after the prior completed handoff.")
    registration_mode.add_argument("--append-targets", action="store_true", help="Append authorized new targets through an immutable direct-parent manifest.")
    arguments = parser.parse_args()
    try:
        state_directory = Path(tempfile.gettempdir()) / "shardingsphere-codex-code-handoff"
        if arguments.register_targets:
            if not arguments.session_id:
                parser.error("--register-targets requires --session-id")
            register_targets(arguments.session_id, arguments.register_targets, state_directory,
                             new_task=arguments.new_task, append_targets=arguments.append_targets)
            json.dump({}, sys.stdout)
            return 0
        if arguments.session_id or arguments.new_task or arguments.append_targets:
            parser.error("--session-id, --new-task and --append-targets require --register-targets")
        event = json.load(sys.stdin)
        if not isinstance(event, dict):
            raise ValueError("Codex hook input must be a JSON object")
        result = process_event(event, state_directory)
    except (OSError, RuntimeError, TypeError, ValueError, json.JSONDecodeError):
        result = {
            "decision": "block",
            "reason": "The code handoff validator failed closed. Inspect the project hook before stopping.",
        }
    json.dump(result, sys.stdout)
    return 1 if arguments.register_targets and result.get("decision") == "block" else 0


if __name__ == "__main__":
    sys.exit(main())
