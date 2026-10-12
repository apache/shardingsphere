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
from unittest.mock import patch

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
            self.assertIn("Task Status: Completed", actual["reason"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(
                "Task Status: Completed\nEvidence: Focused tests passed; bounded self-review found no issue.",
            ), state_directory))
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

    def test_bounded_formal_review_summary_is_accepted(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            "Task Status: Completed\nReview Result: Mergeable\n"
            "Evidence: Focused tests passed; Formal Review covered the original baseline and task delta."
        )
        self.assertEqual([], actual)

    def test_passing_formal_review_requires_evidence(self) -> None:
        actual = code_handoff_hook.validate_handoff(self._mergeable_handoff(
            evidence="", commit_message="",
        ))
        self.assertEqual(["Evidence"], actual)

    def test_legacy_formal_review_accepts_unfenced_unbold_report(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            self._mergeable_handoff().replace("**Review Result: Mergeable**", "Review Result: Mergeable")
            .replace("```markdown\n", "").replace("```text\n", "").replace("```bash\n", "").replace("```", ""),
        )
        self.assertEqual([], actual)

    def test_legacy_formal_review_accepts_no_git_artifacts(self) -> None:
        handoff = self._mergeable_handoff()
        actual = code_handoff_hook.validate_handoff(handoff[:handoff.index("```text")])
        self.assertEqual([], actual)

    def test_legacy_formal_review_accepts_triple_emphasis(self) -> None:
        for result_line in ("***Review Result: Mergeable***", "- ***Review Result: Mergeable***", "* ***Review Result: Mergeable***"):
            with self.subTest(result_line=result_line):
                actual = code_handoff_hook.validate_handoff(
                    self._mergeable_handoff().replace("**Review Result: Mergeable**", result_line),
                )
                self.assertEqual([], actual)

    def test_invalid_or_ambiguous_status_and_verdict_keep_pending(self) -> None:
        invalid_fields = (
            "Task Status: Completed", "Task Status: Awaiting User", "Task Status:", "Task Status: Done",
            "Review Result:", "Review Result: Passed", "### Review Result: Unknown",
            "Review Result: Mergeable\nReview Result: Mergeable",
            "Review Result: Mergeable\n**Review Result: Not Mergeable**",
            "***Review Result: Not Mergeable***", "***Review Result: Review Incomplete***",
            "***Review Result: Passed***", "***Review Result:***", "Review Result: Mergeable*",
            "Review Result: Mergeable**", "***Task Status: Done***", "***Task Status:***",
            "***Task Status: Awaiting User***", "### ***Task Status: Completed***",
            "***Review Result: Mergeable***\n***Review Result: Mergeable***",
            "Review Result: Mergeable\n***Review Result: Not Mergeable***",
        )
        for field in invalid_fields:
            with self.subTest(field=field), tempfile.TemporaryDirectory() as directory:
                state_directory = Path(directory)
                code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
                message = f"Task Status: Completed\nEvidence: Focused tests passed.\n{field}"
                actual = code_handoff_hook.process_event(self._stop_event(message), state_directory)
                self.assertEqual("block", actual["decision"])
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_non_passing_review_blocks_successful_handoff(self) -> None:
        for result in ("Not Mergeable", "Review Incomplete"):
            with self.subTest(result=result):
                actual = code_handoff_hook.validate_handoff(
                    f"Task Status: Completed\nReview Result: {result}\nEvidence: A required check failed.",
                )
                self.assertIn("exactly one allowed Review Result when declared", actual)

    def test_non_passing_review_without_user_wait_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
            actual = code_handoff_hook.process_event(self._stop_event(
                self._incomplete_handoff(awaiting_user=False),
            ), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_user_blocked_handoff_requires_evidence_action_and_no_commit_message(self) -> None:
        handoff = (
            "Task Status: Awaiting User\nEvidence: The additional path is outside current authority.\n"
            "Required User Action: Authorize the exact additional path."
        )
        invalid_messages = (
            handoff.replace("Evidence: The additional path is outside current authority.\n", ""),
            handoff.replace("Required User Action: Authorize the exact additional path.", "Required User Action:"),
            handoff.replace("Required User Action: Authorize the exact additional path.", "Required User Action: ```"),
            handoff.replace("Required User Action: Authorize the exact additional path.", "Required User Action: ```markdown"),
            handoff.replace("Required User Action: Authorize the exact additional path.", "Required User Action: ~~~"),
            handoff.replace("Required User Action: Authorize the exact additional path.", "**Required User Action:** ```"),
            handoff + "\nCommit Message: Complete implementation", handoff + "\nCommit Message:",
            handoff + "\nTask Status: Completed", handoff + "\nReview Result: Unknown",
        )
        for message in invalid_messages:
            with self.subTest(message=message):
                self.assertTrue(code_handoff_hook.validate_paused_handoff(message))

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

    def test_user_wait_takes_priority_over_every_declared_review_result(self) -> None:
        for result in (None, "Mergeable", "Not Mergeable", "Review Incomplete"):
            with self.subTest(result=result), tempfile.TemporaryDirectory() as directory:
                state_directory = Path(directory)
                code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
                message = (
                    "Task Status: Awaiting User\nEvidence: Publishing needs the user's authorization.\n"
                    "Required User Action: Authorize publishing the reviewed candidate."
                )
                if result is not None:
                    message += f"\nReview Result: {result}"
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(message), state_directory))
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_emphasized_user_wait_keeps_pending_with_passing_review(self) -> None:
        status_lines = (
            "***Task Status: Awaiting User***", "- ***Task Status: Awaiting User***",
            "* ***Task Status: Awaiting User***", "### ***Task Status: Awaiting User***",
            "- ### ***Task Status: Awaiting User***",
        )
        for status_line in status_lines:
            with self.subTest(status_line=status_line), tempfile.TemporaryDirectory() as directory:
                state_directory = Path(directory)
                code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
                message = (
                    f"{status_line}\nReview Result: Mergeable\n"
                    "***Evidence: Verification and Formal Review passed; publishing needs authority.***\n"
                    "***Required User Action: Authorize publishing the reviewed candidate.***"
                )
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(message), state_directory))
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_emphasized_commit_message_blocks_user_wait(self) -> None:
        commit_lines = (
            "***Commit Message: Publish reviewed candidate***", "### ***Commit Message: Publish reviewed candidate***",
            "- ### ***Commit Message: Publish reviewed candidate***", "***Commit Message:***",
            "**Commit Message:** Publish reviewed candidate", "- ### **Commit Message:** Publish reviewed candidate",
        )
        for commit_line in commit_lines:
            with self.subTest(commit_line=commit_line), tempfile.TemporaryDirectory() as directory:
                state_directory = Path(directory)
                code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
                message = (
                    "Task Status: Awaiting User\nEvidence: Publishing needs the user's authority.\n"
                    f"Required User Action: Authorize publishing the reviewed candidate.\n{commit_line}"
                )
                actual = code_handoff_hook.process_event(self._stop_event(message), state_directory)
                self.assertEqual("block", actual["decision"])
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_label_emphasis_user_wait_keeps_pending_with_passing_review(self) -> None:
        for status_line in ("**Task Status:** Awaiting User", "- ### **Task Status:** Awaiting User"):
            with self.subTest(status_line=status_line), tempfile.TemporaryDirectory() as directory:
                state_directory = Path(directory)
                code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
                message = (
                    f"{status_line}\nReview Result: Mergeable\n"
                    "Evidence: Verification and Formal Review passed; publishing needs authority.\n"
                    "Required User Action: Authorize publishing the reviewed candidate."
                )
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(message), state_directory))
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_handoff_without_completed_status_or_legacy_passing_result_is_rejected(self) -> None:
        invalid_messages = (
            None, "", "Evidence: Focused tests passed.", "Task Status: Completed",
            "Task Status: Completed\n### Evidence\n```",
            "```markdown\nReview Result: Mergeable\n### Evidence\n```",
            "Task Status: Completed\nEvidence: ```", "Task Status: Completed\nEvidence: ```markdown",
            "Task Status: Completed\nEvidence: ~~~", "Task Status: Completed\n### Evidence\n~~~",
            "Task Status: Completed\n**Evidence:** ```", "Task Status: Completed\n**Evidence:** ~~~",
            "Task Status: Completed\n### Evidence\n~~~markdown\n~~~",
        )
        for message in invalid_messages:
            with self.subTest(message=message), tempfile.TemporaryDirectory() as directory:
                state_directory = Path(directory)
                code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
                event = self._stop_event("")
                event["last_assistant_message"] = message
                actual = code_handoff_hook.process_event(event, state_directory)
                self.assertEqual("block", actual["decision"])

    def test_no_pending_write_requires_no_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            actual = code_handoff_hook.process_event(self._stop_event("Read-only analysis complete."), Path(directory))
            self.assertEqual({}, actual)

    def test_later_code_write_requires_a_new_completed_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: src/main/Foo.java"), state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(
                self._stop_event(self._mergeable_handoff()), state_directory,
            ))
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: src/main/Bar.java"), state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("A follow-up edit is complete."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_shell_code_change_requires_completed_handoff(self) -> None:
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

    def test_read_only_shell_command_does_not_require_code_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "Foo.java")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            code_handoff_hook.process_event(event, state_directory)
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Inspection complete."), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_shell_prose_change_does_not_require_code_handoff(self) -> None:
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

    def test_shell_workdir_selects_the_repository_that_changes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            original = parent / "original"
            target = parent / "target"
            self._initialize_repository(original, "Foo.java")
            self._initialize_repository(target, "Bar.java")
            for workdir in (str(target), "../target"):
                with self.subTest(workdir=workdir), tempfile.TemporaryDirectory() as state:
                    state_directory = Path(state)
                    event = self._shell_event("PreToolUse", original)
                    event["tool_input"] = {"cmd": "ignored", "workdir": workdir}
                    code_handoff_hook.process_event(event, state_directory)
                    (target / "Bar.java").write_text(f"Changed in {workdir}.\n", encoding="utf-8")
                    event["hook_event_name"] = "PostToolUse"
                    code_handoff_hook.process_event(event, state_directory)
                    actual = code_handoff_hook.process_event(self._stop_event("Implementation complete."), state_directory)
                    self.assertEqual("block", actual["decision"])

    def test_read_only_shell_with_another_workdir_requires_no_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory, tempfile.TemporaryDirectory() as state:
            parent = Path(directory)
            original = self._initialize_repository(parent / "original", "Foo.java")
            target = self._initialize_repository(parent / "target", "Bar.java")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", original)
            event["tool_input"] = {"cmd": "ignored", "workdir": str(target)}
            code_handoff_hook.process_event(event, state_directory)
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Inspection complete."), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_shell_workdir_preserves_repository_root_trailing_space(self) -> None:
        with tempfile.TemporaryDirectory() as directory, tempfile.TemporaryDirectory() as state:
            parent = Path(directory)
            original = self._initialize_repository(parent / "owner", "Foo.java")
            target = self._initialize_repository(parent / "owner ", "Bar.java")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", original)
            event["tool_input"] = {"cmd": "ignored", "workdir": str(target)}
            code_handoff_hook.process_event(event, state_directory)
            (target / "Bar.java").write_text("Changed implementation.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Implementation complete."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_shell_workdir_defaults_to_event_or_process_cwd(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            repository = self._initialize_repository(Path(directory), "Foo.java")
            inputs = ((True, None), (True, ""), (True, 7), (False, None), (False, "."))
            for index, (has_cwd, workdir) in enumerate(inputs):
                with self.subTest(has_cwd=has_cwd, workdir=workdir), tempfile.TemporaryDirectory() as state:
                    state_directory = Path(state)
                    event = self._shell_event("PreToolUse", repository)
                    if not has_cwd:
                        event.pop("cwd")
                    event["tool_input"] = {"cmd": "ignored"} if workdir is None else {"cmd": "ignored", "workdir": workdir}
                    with patch.object(code_handoff_hook.os, "getcwd", return_value=str(repository)):
                        code_handoff_hook.process_event(event, state_directory)
                        (repository / "Foo.java").write_text(f"Changed {index}.\n", encoding="utf-8")
                        event["hook_event_name"] = "PostToolUse"
                        code_handoff_hook.process_event(event, state_directory)
                    actual = code_handoff_hook.process_event(self._stop_event("Implementation complete."), state_directory)
                    self.assertEqual("block", actual["decision"])

    def test_invalid_declared_workdir_does_not_fall_back_to_another_repository(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as outside:
            original = self._initialize_repository(Path(repository), "Foo.java")
            for workdir in ("missing-directory", outside):
                with self.subTest(workdir=workdir), tempfile.TemporaryDirectory() as state:
                    state_directory = Path(state)
                    event = self._shell_event("PreToolUse", original)
                    event["tool_input"] = {"cmd": "ignored", "workdir": workdir}
                    code_handoff_hook.process_event(event, state_directory)
                    event["hook_event_name"] = "PostToolUse"
                    code_handoff_hook.process_event(event, state_directory)
                    actual = code_handoff_hook.process_event(self._stop_event("Inspection complete."), state_directory)
                    self.assertEqual("block", actual["decision"])
                    self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

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
            "```\n\n"
            "Evidence: The current task delta is known; additional path authorization is unavailable."
        )
        if awaiting_user:
            handoff += (
                "\n\n```text\n"
                "Task Status: Awaiting User\n"
                "Required User Action: Authorize the exact additional path.\n"
                "```"
            )
        return handoff


if __name__ == "__main__":
    unittest.main()
