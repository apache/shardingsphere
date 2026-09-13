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
from pathlib import Path
import re
import sys
import tempfile
from typing import Any

PROSE_SUFFIXES = frozenset({".adoc", ".md", ".rst"})
PATCH_FILE_PATTERN = re.compile(r"^\*\*\* (?:Add|Delete|Update) File: (.+)$", re.MULTILINE)
FIELD_PATTERN = r"^\s*(?:[-*]\s+)?(?:#{{1,6}}\s+)?(?:\*\*)?{label}:\s*(.*?)(?:\*\*)?\s*$"
REQUIRED_FIELDS = ("Review Result", "Blocking Issues", "Required Changes", "Review Recommendations")
NO_CHANGES_RECOMMENDED = "No changes recommended"


def process_event(event: dict[str, Any], state_directory: Path) -> dict[str, str]:
    """Process one Codex hook event."""
    event_name = event.get("hook_event_name")
    if "PostToolUse" == event_name:
        _record_implementation_change(event, state_directory)
        return {}
    if "Stop" != event_name:
        return {}
    marker_path = _marker_path(event, state_directory)
    if not marker_path.exists():
        return {}
    failures = validate_handoff(event.get("last_assistant_message"))
    if failures:
        return {"decision": "block", "reason": _continuation_reason(failures)}
    marker_path.unlink()
    return {}


def _record_implementation_change(event: dict[str, Any], state_directory: Path) -> None:
    if "apply_patch" != event.get("tool_name"):
        return
    tool_input = event.get("tool_input")
    patch = tool_input.get("command") if isinstance(tool_input, dict) else None
    if not isinstance(patch, str) or not _changes_implementation_file(patch):
        return
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


def validate_handoff(message: Any) -> list[str]:
    """Return every missing or invalid code handoff field."""
    if not isinstance(message, str) or not message.strip():
        return list(REQUIRED_FIELDS)
    values = {field: _field_value(message, field) for field in REQUIRED_FIELDS}
    failures = [field for field, value in values.items() if not value]
    blocking_issues = values["Blocking Issues"]
    if blocking_issues and not blocking_issues.isdigit():
        failures.append("Blocking Issues must be a non-negative integer")
        return failures
    if "0" != blocking_issues:
        return failures
    recommendations = values["Review Recommendations"]
    if NO_CHANGES_RECOMMENDED.casefold() not in recommendations.casefold():
        failures.append(f'Review Recommendations must state "{NO_CHANGES_RECOMMENDED}"')
    if not _field_value(message, "Commit Message"):
        failures.append("Commit Message")
    return failures


def _field_value(message: str, label: str) -> str:
    pattern = re.compile(FIELD_PATTERN.format(label=re.escape(label)), re.IGNORECASE | re.MULTILINE)
    match = pattern.search(message)
    return match.group(1).strip() if match else ""


def _continuation_reason(failures: list[str]) -> str:
    missing = ", ".join(failures)
    return (
        "The code-writing handoff is incomplete. Correct these fields before stopping: "
        f"{missing}. Use explicit Review Result, Blocking Issues, Required Changes, and Review Recommendations lines. "
        f'When Blocking Issues is 0, state "{NO_CHANGES_RECOMMENDED}" under Review Recommendations and include Commit Message.'
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
