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

"""Test the Codex code handoff hook."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import subprocess
import tempfile
import tomllib
import unittest

HOOK_SPEC = importlib.util.spec_from_file_location("code_handoff_hook", Path(__file__).with_name("code_handoff_hook.py"))
if HOOK_SPEC is None or HOOK_SPEC.loader is None:
    raise RuntimeError("Unable to load code handoff hook")
code_handoff_hook = importlib.util.module_from_spec(HOOK_SPEC)
HOOK_SPEC.loader.exec_module(code_handoff_hook)


class CodeHandoffHookTest(unittest.TestCase):
    """Test the Codex code handoff hook."""

    def test_implementation_patch_requires_complete_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: src/main/Foo.java"), state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Implementation complete."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertIn("$review-pr Formal Review", actual["reason"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_prose_patch_does_not_require_code_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: docs/foo.md"), state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory))

    def test_unknown_apply_patch_payload_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            event = self._post_tool_event("*** Update File: docs/foo.md")
            event["tool_input"] = {}
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_bounded_review_summary_is_rejected(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            "Review Result: Mergeable\nBlocking Issues: 0\nRequired Changes: None.\n"
            "Review Recommendations: No changes recommended.\nCommit Message: Fix metadata"
        )
        self.assertEqual([
            "exactly one fenced markdown Formal Review", "Commit Message",
            "git commit --dry-run --only", "git commit --only",
        ], actual)

    def test_passing_formal_review_requires_evidence_and_commit_message(self) -> None:
        actual = code_handoff_hook.validate_handoff(self._mergeable_handoff(
            evidence="", commit_message="",
        ))
        self.assertEqual(["### Evidence", "Commit Message"], actual)

    def test_passing_formal_review_requires_bold_result(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            self._mergeable_handoff().replace("**Review Result: Mergeable**", "Review Result: Mergeable"),
        )
        self.assertIn("exactly one bold allowed Review Result", actual)

    def test_commit_message_requires_separate_text_block(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            self._mergeable_handoff().replace("```text\n", "").removesuffix("```").rstrip(),
        )
        self.assertEqual(["Commit Message"], actual)

    def test_passing_formal_review_requires_manual_git_commands(self) -> None:
        handoff = self._mergeable_handoff()
        actual = code_handoff_hook.validate_handoff(handoff[:handoff.rfind("```bash")])
        self.assertEqual(["git commit --dry-run --only", "git commit --only"], actual)

    def test_manual_git_commands_must_start_command_lines(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            self._mergeable_handoff().replace("git commit", "Run git commit"),
        )
        self.assertEqual(["git commit --dry-run --only", "git commit --only"], actual)

    def test_non_passing_review_blocks_successful_handoff(self) -> None:
        actual = code_handoff_hook.validate_handoff(self._incomplete_handoff(awaiting_user=False))
        self.assertIn("exactly one bold allowed Review Result", actual)

    def test_not_mergeable_pause_requires_formal_result_fields(self) -> None:
        actual = code_handoff_hook.validate_paused_handoff(
            self._not_mergeable_handoff(blocking_issues="Blocking Issues: 1"),
        )
        self.assertIn("positive bold Blocking Issues count", actual)

    def test_user_blocked_handoff_requires_separate_text_block(self) -> None:
        actual = code_handoff_hook.validate_paused_handoff(
            self._incomplete_handoff(awaiting_user=True).replace("```text\n", "").removesuffix("```").rstrip(),
        )
        self.assertIn("Task Status: Awaiting User", actual)
        self.assertIn("Required User Action", actual)

    def test_user_blocked_handoff_yields_without_clearing_pending_marker(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Add File: scripts/foo.py"), state_directory)
            actual = code_handoff_hook.process_event(
                self._stop_event(self._incomplete_handoff(awaiting_user=True)), state_directory,
            )
            self.assertEqual({}, actual)
            self.assertEqual(1, len(list(state_directory.glob("*.pending"))))
            actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory)
            self.assertEqual({}, actual)
            self.assertEqual([], list(state_directory.iterdir()))

    def test_later_code_write_requires_a_new_passing_formal_review(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: src/main/Foo.java"), state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(
                self._stop_event(self._mergeable_handoff()), state_directory,
            ))
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: src/main/Bar.java"), state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("A follow-up edit is complete."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_shell_code_change_requires_formal_review(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "Foo.java")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            code_handoff_hook.process_event(event, state_directory)
            (repository_path / "Foo.java").write_text("class ChangedFoo {}\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Implementation complete."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_read_only_shell_command_does_not_require_formal_review(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "Foo.java")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            code_handoff_hook.process_event(event, state_directory)
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Inspection complete."), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_shell_prose_change_does_not_require_formal_review(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "README.md")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            code_handoff_hook.process_event(event, state_directory)
            (repository_path / "README.md").write_text("Changed documentation.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_project_config_registers_code_handoff_hooks(self) -> None:
        config_path = Path(__file__).parents[2] / "config.toml"
        with config_path.open("rb") as config_file:
            hooks = tomllib.load(config_file)["hooks"]
        self.assertEqual("^(Bash|exec_command|write_stdin)$", hooks["PreToolUse"][0]["matcher"])
        self.assertIn("code_handoff_hook.py", hooks["PreToolUse"][0]["hooks"][0]["command"])
        self.assertEqual("^(Bash|apply_patch|exec_command|write_stdin)$", hooks["PostToolUse"][0]["matcher"])
        self.assertIn("code_handoff_hook.py", hooks["PostToolUse"][0]["hooks"][0]["command"])
        self.assertIn("code_handoff_hook.py", hooks["Stop"][0]["hooks"][0]["command"])

    @staticmethod
    def _post_tool_event(patch: str) -> dict[str, object]:
        return {
            "hook_event_name": "PostToolUse",
            "session_id": "foo_session",
            "tool_name": "apply_patch",
            "tool_input": {"command": f"*** Begin Patch\n{patch}\n*** End Patch"},
        }

    @staticmethod
    def _shell_event(event_name: str, repository: Path) -> dict[str, object]:
        return {
            "hook_event_name": event_name,
            "session_id": "foo_session",
            "tool_use_id": "foo_tool",
            "tool_name": "exec_command",
            "cwd": str(repository),
            "tool_input": {"cmd": "ignored"},
        }

    @staticmethod
    def _stop_event(message: str) -> dict[str, object]:
        return {
            "hook_event_name": "Stop",
            "session_id": "foo_session",
            "last_assistant_message": message,
        }

    @staticmethod
    def _initialize_repository(repository: Path, filename: str) -> Path:
        subprocess.run(["git", "init", "--quiet", str(repository)], check=True)
        (repository / filename).write_text("Initial content.\n", encoding="utf-8")
        return repository

    @staticmethod
    def _mergeable_handoff(evidence: str = "- Focused tests and task-delta inspection passed.",
                           commit_message: str = "Enforce formal code review handoff") -> str:
        return (
            "```markdown\n"
            "### Result\n\n"
            "**Review Result: Mergeable**\n\n"
            "The complete standalone Code Correctness Review found no blocking issue.\n\n"
            "### Evidence\n\n"
            f"{evidence}\n\n"
            "### Coverage\n\n"
            "Standalone local candidate; original baseline and every attributed task file were reviewed.\n"
            "```\n\n"
            "```text\n"
            f"Commit Message: {commit_message}\n"
            "```\n\n"
            "```bash\n"
            f"git commit --dry-run --only -m '{commit_message}' -- src/main/Foo.java\n"
            f"git commit --only -m '{commit_message}' -- src/main/Foo.java\n"
            "```"
        )

    @staticmethod
    def _incomplete_handoff(awaiting_user: bool) -> str:
        handoff = (
            "```markdown\n"
            "### Result\n\n"
            "**Review Result: Review Incomplete**\n\n"
            "A required authorization is unavailable.\n\n"
            "### Verified Facts\n\n"
            "- The current task-owned delta is known.\n\n"
            "### Required Evidence\n\n"
            "- Authorization for the exact additional path.\n\n"
            "### Coverage\n\n"
            "Standalone local candidate; known task files were inspected.\n"
            "```"
        )
        if awaiting_user:
            handoff += (
                "\n\n```text\n"
                "Task Status: Awaiting User\n"
                "Required User Action: Authorize the exact additional path.\n"
                "```"
            )
        return handoff

    @staticmethod
    def _not_mergeable_handoff(blocking_issues: str) -> str:
        return (
            "```markdown\n"
            "### Result\n\n"
            "**Review Result: Not Mergeable**\n\n"
            "Feedback Mode: Change Request\n\n"
            f"{blocking_issues}\n\n"
            "A confirmed issue must be fixed.\n\n"
            "### Blocking Issues\n\n"
            "- The candidate does not satisfy its contract.\n\n"
            "### Coverage\n\n"
            "Standalone local candidate; original baseline and task delta were reviewed.\n"
            "```\n\n"
            "```text\n"
            "Task Status: Awaiting User\n"
            "Required User Action: Authorize the required scope.\n"
            "```"
        )


if __name__ == "__main__":
    unittest.main()
