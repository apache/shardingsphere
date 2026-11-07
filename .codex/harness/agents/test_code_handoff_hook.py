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
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
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
                "Task Status: Completed\n" + self._mergeable_handoff(),
            ), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_prose_patch_requires_complete_handoff(self) -> None:
        for filename in ("README.md", "guide.adoc", "guide.rst"):
            with self.subTest(filename=filename), tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
                repository_path = self._initialize_repository(Path(repository), filename)
                state_directory = Path(state)
                event = self._shell_event("PreToolUse", repository_path)
                event["tool_name"] = "apply_patch"
                code_handoff_hook.process_event(event, state_directory)
                (repository_path / filename).write_text("Changed documentation.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory)
                self.assertEqual("block", actual["decision"])
                self.assertNotIn("Commit Message", actual["reason"])
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(
                    self._mergeable_handoff(commit_message=None),
                ), state_directory))

    def test_unknown_apply_patch_payload_fails_closed(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            event = self._post_tool_event("*** Update File: docs/foo.md")
            event["tool_input"] = {}
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory)
            self.assertEqual("block", actual["decision"])
            actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertIn("nonempty Commit Message", actual["reason"])

    def test_completed_handoff_requires_full_report_and_commit_message(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            "Task Status: Completed\nReview Result: Mergeable\n"
            "Evidence: Focused tests passed; Formal Review covered the original baseline and task delta."
        )
        self.assertEqual([
            "one complete review report in a markdown-labelled fenced block", "nonempty Commit Message",
        ], actual)

    def test_passing_formal_review_requires_evidence(self) -> None:
        actual = code_handoff_hook.validate_handoff(self._mergeable_handoff(
            evidence="", commit_message="",
        ))
        self.assertEqual(["review Evidence", "nonempty Commit Message"], actual)

    def test_formal_review_requires_markdown_fence(self) -> None:
        handoff = self._mergeable_handoff()
        invalid_messages = (
            handoff.replace("```markdown", "```"), handoff.replace("```markdown", "```text"),
            handoff.replace("```markdown\n", "").replace("```text\n", "").replace("```bash\n", "").replace("```", ""),
            handoff[:handoff.index("```\n\n```text")],
            handoff.replace("```markdown", "````markdown", 1),
            "```text\n" + handoff + "\n```",
        )
        for message in invalid_messages:
            with self.subTest(message=message):
                self.assertIn("one complete review report in a markdown-labelled fenced block", code_handoff_hook.validate_handoff(message))

    def test_formal_review_accepts_no_git_commands(self) -> None:
        handoff = self._mergeable_handoff()
        actual = code_handoff_hook.validate_handoff(handoff[:handoff.index("```bash")])
        self.assertEqual([], actual)

    def test_formal_review_accepts_standard_fence_lengths_and_indentation(self) -> None:
        handoff = self._mergeable_handoff()
        report, artifacts = handoff.split("```\n\n```text", 1)
        for opening, closing in (("````markdown", "````"), ("```markdown", "````"), ("~~~markdown", "~~~"), ("   ```markdown", "   ```")):
            with self.subTest(opening=opening, closing=closing):
                message = report.replace("```markdown", opening, 1) + closing + "\n\n```text" + artifacts
                self.assertEqual([], code_handoff_hook.validate_handoff(message))

    def test_review_sections_cannot_be_supplied_outside_report(self) -> None:
        handoff = self._mergeable_handoff()
        for section, content in (
            ("Result", "**Review Result: Mergeable**\n\nThe complete standalone Code Correctness Review found no blocking issue."),
            ("Evidence", "- Focused tests and task-delta inspection passed."),
            ("Coverage", "Standalone local candidate; original baseline and every attributed task file were reviewed."),
        ):
            with self.subTest(section=section):
                message = handoff.replace(f"### {section}\n\n{content}", "") + f"\n### {section}\n\n{content}"
                self.assertTrue(code_handoff_hook.validate_handoff(message))

    def test_completed_handoff_requires_separate_nonempty_commit_message(self) -> None:
        handoff = self._mergeable_handoff()
        invalid_messages = (
            self._mergeable_handoff(commit_message=""), self._mergeable_handoff(commit_message="```"),
            self._mergeable_handoff(commit_message="~~~markdown"), handoff[:handoff.index("```text")],
            handoff[:handoff.index("```text")].replace("### Coverage", "Commit Message: Complete implementation\n\n### Coverage"),
        )
        for message in invalid_messages:
            with self.subTest(message=message):
                self.assertTrue(code_handoff_hook.validate_handoff(message))

    def test_missing_review_keeps_pending_until_handoff_is_complete(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
            message = "Task Status: Completed\n" + self._mergeable_handoff().replace("**Review Result: Mergeable**", "")
            actual = code_handoff_hook.process_event(self._stop_event(message), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertIn("exactly one Review Result: Mergeable", actual["reason"])
            self.assertEqual(1, len(list(state_directory.glob("*.pending"))))
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_missing_commit_message_keeps_pending_until_handoff_is_complete(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: Foo.java"), state_directory)
            actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message="")), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertIn("nonempty Commit Message", actual["reason"])
            self.assertEqual(1, len(list(state_directory.glob("*.pending"))))
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_legacy_formal_review_accepts_triple_emphasis(self) -> None:
        for result_line in ("Review Result: Mergeable", "***Review Result: Mergeable***", "- ***Review Result: Mergeable***", "* ***Review Result: Mergeable***"):
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
                message = f"Task Status: Completed\n{self._mergeable_handoff()}\n{field}"
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
                    message += f"\n```markdown\n### Result\nReview Result: {result}\n```"
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
                    f"{status_line}\n```markdown\n### Result\nReview Result: Mergeable\n```\n"
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
                    f"{status_line}\n```markdown\n### Result\nReview Result: Mergeable\n```\n"
                    "Evidence: Verification and Formal Review passed; publishing needs authority.\n"
                    "Required User Action: Authorize publishing the reviewed candidate."
                )
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(message), state_directory))
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))

    def test_user_wait_requires_markdown_for_declared_review(self) -> None:
        for result in ("Mergeable", "Not Mergeable", "Review Incomplete"):
            with self.subTest(result=result):
                actual = code_handoff_hook.validate_paused_handoff(
                    "Task Status: Awaiting User\nEvidence: Publishing needs the user's authority.\n"
                    "Required User Action: Authorize publishing the reviewed candidate.\n"
                    f"Review Result: {result}",
                )
                self.assertEqual(["one complete review report in a markdown-labelled fenced block"], actual)

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

    def test_shell_prose_change_requires_complete_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "README.md")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            code_handoff_hook.process_event(event, state_directory)
            (repository_path / "README.md").write_text("Changed documentation.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(
                self._mergeable_handoff(commit_message=None),
            ), state_directory))

    def test_code_commit_requirement_stays_pending_through_prose_and_restore(self) -> None:
        for changes in (("README.md", "Foo.java"), ("Foo.java", "README.md"), ("Foo.java", "README.md", "restore")):
            with self.subTest(changes=changes), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                (repository / "Foo.java").write_text("Original source.\n", encoding="utf-8")
                state_directory = parent / "state"
                for change in changes:
                    event = self._shell_event("PreToolUse", repository)
                    code_handoff_hook.process_event(event, state_directory)
                    target = repository / ("Foo.java" if change == "restore" else change)
                    target.write_text("Original source.\n" if change == "restore" else "Changed content.\n", encoding="utf-8")
                    event["hook_event_name"] = "PostToolUse"
                    code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory)
                self.assertEqual("block", actual["decision"])
                self.assertIn("nonempty Commit Message", actual["reason"])
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))

    def test_legacy_empty_pending_marker_requires_commit_message(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: README.md"), state_directory)
            next(state_directory.glob("*.pending")).write_bytes(b"")
            actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertIn("nonempty Commit Message", actual["reason"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))

    def test_stop_derives_commit_requirement_from_registered_changed_paths(self) -> None:
        for filename, requires_commit in (("AGENTS.md", False), ("hook.py", True)):
            with self.subTest(filename=filename), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                target = parent / filename
                target.write_text("Original content.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
                state_directory = parent / "state"
                code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                target.write_text("Changed content.\n", encoding="utf-8")
                actual = code_handoff_hook.process_event(self._stop_event("Content updated."), state_directory)
                self.assertEqual("block", actual["decision"])
                actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory)
                if requires_commit:
                    self.assertEqual("block", actual["decision"])
                    self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
                else:
                    self.assertEqual({}, actual)

    def test_unchanged_patch_requires_no_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "README.md")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            event["tool_name"] = "apply_patch"
            code_handoff_hook.process_event(event, state_directory)
            (repository_path / "README.md").write_text("Initial content.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No change was needed."), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_registered_shell_change_outside_git_requires_handoff(self) -> None:
        for use_repository in (True, False):
            with self.subTest(use_repository=use_repository), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                report = parent / "report.md"
                report.write_text("Original report.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(report)], manifest)
                state_directory = parent / "state"
                code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                event = self._shell_event("PreToolUse", repository if use_repository else parent)
                code_handoff_hook.process_event(event, state_directory)
                report.write_text("Changed report.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Report updated."), state_directory)
                self.assertEqual("block", actual["decision"])

    def test_registered_non_git_unchanged_target_requires_no_handoff(self) -> None:
        for rewrite in (False, True):
            with self.subTest(rewrite=rewrite), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                target = parent / "report.md"
                target.write_text("Original report.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
                state_directory = parent / "state"
                code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                event = self._shell_event("PreToolUse", parent)
                code_handoff_hook.process_event(event, state_directory)
                if rewrite:
                    target.write_text("Original report.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No change was needed."), state_directory))
                self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_stop_detects_registered_changes_from_other_tools(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "SKILL.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            target.write_text("Changed instructions.\n", encoding="utf-8")
            code_handoff_hook.process_event({"hook_event_name": "PostToolUse", "tool_name": "another_writer"}, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Instructions updated."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_private_review_material_requires_no_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            repository = self._initialize_repository(parent / "repository", "README.md")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(repository / "README.md")], manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            event = self._shell_event("PreToolUse", repository)
            code_handoff_hook.process_event(event, state_directory)
            (parent / "private-review.md").write_text("Internal review notes.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Read-only review complete."), state_directory))
            self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_restore_keeps_an_existing_pending_review(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            event = self._shell_event("PreToolUse", parent)
            code_handoff_hook.process_event(event, state_directory)
            target.write_text("Changed instructions.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            target.write_text("Original instructions.\n", encoding="utf-8")
            actual = code_handoff_hook.process_event(self._stop_event("Task Status: Completed\nThe change was restored."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
            self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_registered_review_tracks_later_changes_without_rebasing(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "report.md"
            target.write_text("Original report.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            original_manifest = manifest.read_bytes()
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            target.write_text("Reviewed report.\n", encoding="utf-8")
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No further changes."), state_directory))
            target.write_text("Later report.\n", encoding="utf-8")
            actual = code_handoff_hook.process_event(self._stop_event("Follow-up complete."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual(original_manifest, manifest.read_bytes())

    def test_registration_preserves_the_original_baseline(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            target.write_text("Changed instructions.\n", encoding="utf-8")
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Instructions updated."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_registration_rejects_a_replacement_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            original = parent / "original.json"
            replacement = parent / "replacement.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], original)
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], replacement)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", original, state_directory)
            with self.assertRaisesRegex(ValueError, "already has an original deliverable manifest"):
                code_handoff_hook.register_targets("foo_session", replacement, state_directory)

    def test_stop_rejects_a_changed_original_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            manifest.write_bytes(manifest.read_bytes() + b"\n")
            with self.assertRaisesRegex(ValueError, "original deliverable manifest changed"):
                code_handoff_hook.process_event(self._stop_event("Review complete."), state_directory)

    def test_unchanged_registered_task_closes_only_with_completed_status(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            following_manifest = parent / "following.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], following_manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Task Status: Awaiting User"), state_directory))
            with self.assertRaisesRegex(ValueError, "completed prior handoff with no pending review"):
                code_handoff_hook.register_targets("foo_session", following_manifest, state_directory, new_task=True)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(
                "Task Status: Completed\nEvidence: No deliverable content changed, so no review was required.",
            ), state_directory))
            code_handoff_hook.register_targets("foo_session", following_manifest, state_directory, new_task=True)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No changes."), state_directory))

    def test_new_task_registration_requires_a_completed_handoff(self) -> None:
        for register_first in (False, True):
            with self.subTest(register_first=register_first), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                target = parent / "AGENTS.md"
                target.write_text("Original instructions.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
                state_directory = parent / "state"
                if register_first:
                    code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                with self.assertRaisesRegex(ValueError, "completed prior handoff with no pending review"):
                    code_handoff_hook.register_targets("foo_session", manifest, state_directory, new_task=True)

    def test_new_task_registration_preserves_a_pending_follow_up(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            target.write_text("Reviewed instructions.\n", encoding="utf-8")
            code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory)
            event = self._shell_event("PreToolUse", parent)
            code_handoff_hook.process_event(event, state_directory)
            target.write_text("Follow-up instructions.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            following_manifest = parent / "following.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], following_manifest)
            with self.assertRaisesRegex(ValueError, "completed prior handoff with no pending review"):
                code_handoff_hook.register_targets("foo_session", following_manifest, state_directory, new_task=True)

    def test_new_task_registration_captures_later_user_state(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            original_manifest = parent / "original.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], original_manifest)
            original_bytes = original_manifest.read_bytes()
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", original_manifest, state_directory)
            target.write_text("Reviewed instructions.\n", encoding="utf-8")
            code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory)
            target.write_text("User's later instructions.\n", encoding="utf-8")
            following_manifest = parent / "following.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], following_manifest)
            with self.assertRaisesRegex(ValueError, "already has an original deliverable manifest"):
                code_handoff_hook.register_targets("foo_session", following_manifest, state_directory)
            code_handoff_hook.register_targets("foo_session", following_manifest, state_directory, new_task=True)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No further changes."), state_directory))
            target.write_text("New task instructions.\n", encoding="utf-8")
            actual = code_handoff_hook.process_event(self._stop_event("New task complete."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual(original_bytes, original_manifest.read_bytes())

    def test_append_targets_preserves_pending_code_review_and_originals(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            source = parent / "hook.py"
            source.write_text("Original source.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(source)], manifest)
            original_manifest = manifest.read_bytes()
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            source.write_text("Changed source.\n", encoding="utf-8")
            code_handoff_hook.process_event(self._stop_event("Source updated."), state_directory)
            pending = next(state_directory.glob("*.pending")).read_bytes()
            addition = parent / "AGENTS.md"
            addition.write_text("Original instructions.\n", encoding="utf-8")
            extension = parent / "extension.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(addition)], extension, extend=manifest)
            code_handoff_hook.register_targets("foo_session", extension, state_directory, append_targets=True)
            self.assertEqual(pending, next(state_directory.glob("*.pending")).read_bytes())
            source.write_text("Original source.\n", encoding="utf-8")
            addition.write_text("Changed instructions.\n", encoding="utf-8")
            actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertIn("nonempty Commit Message", actual["reason"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
            self.assertEqual(original_manifest, manifest.read_bytes())

    def test_append_targets_preserves_prior_reviewed_content(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            original = parent / "AGENTS.md"
            original.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(original)], manifest)
            original_manifest = manifest.read_bytes()
            state_directory = parent / "state"
            code_handoff_hook.register_targets("foo_session", manifest, state_directory)
            original.write_text("Reviewed instructions.\n", encoding="utf-8")
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))
            addition = parent / "report.md"
            addition.write_text("Original report.\n", encoding="utf-8")
            extension = parent / "extension.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(addition)], extension, extend=manifest)
            code_handoff_hook.register_targets("foo_session", extension, state_directory, append_targets=True)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No new content changes."), state_directory))
            addition.write_text("Changed report.\n", encoding="utf-8")
            actual = code_handoff_hook.process_event(self._stop_event("Report updated."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))
            self.assertEqual(original_manifest, manifest.read_bytes())

    def test_append_targets_requires_the_current_direct_parent(self) -> None:
        for use_extension in (False, True):
            with self.subTest(use_extension=use_extension), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                original = parent / "AGENTS.md"
                original.write_text("Original instructions.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                other_manifest = parent / "other.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(original)], manifest)
                code_handoff_hook.review_scope.capture_file_snapshots([str(original)], other_manifest)
                state_directory = parent / "state"
                code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                candidate = other_manifest
                if use_extension:
                    addition = parent / "report.md"
                    addition.write_text("Original report.\n", encoding="utf-8")
                    candidate = parent / "extension.json"
                    code_handoff_hook.review_scope.capture_file_snapshots([str(addition)], candidate, extend=other_manifest)
                with self.assertRaisesRegex(ValueError, "directly reference the current task manifest"):
                    code_handoff_hook.register_targets("foo_session", candidate, state_directory, append_targets=True)

    def test_append_targets_requires_existing_registration(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            with self.assertRaisesRegex(ValueError, "existing task registration"):
                code_handoff_hook.register_targets("foo_session", manifest, parent / "state", append_targets=True)

    def test_registration_cli_rejects_conflicting_modes_without_mutation(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            target = parent / "AGENTS.md"
            target.write_text("Original instructions.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            actual = subprocess.run(
                [sys.executable, str(Path(__file__).with_name("code_handoff_hook.py")), "--register-targets", str(manifest),
                 "--session-id", "foo_session", "--new-task", "--append-targets"],
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                env={**os.environ, "TMPDIR": str(parent), "PYTHONDONTWRITEBYTECODE": "1"}, check=False,
            )
            self.assertEqual(2, actual.returncode)
            self.assertEqual("", actual.stdout)
            self.assertFalse((parent / "shardingsphere-codex-code-handoff").exists())

    def test_event_wrapped_first_registration_requires_no_review(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            repository = self._initialize_repository(parent / "repository", "README.md")
            target = parent / "report.md"
            target.write_text("Original report.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            state_directory = parent / "shardingsphere-codex-code-handoff"
            event = self._shell_event("PreToolUse", repository)
            code_handoff_hook.process_event(event, state_directory)
            actual = subprocess.run(
                [sys.executable, str(Path(__file__).with_name("code_handoff_hook.py")),
                 "--register-targets", str(manifest), "--session-id", "foo_session"],
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                env={**os.environ, "TMPDIR": str(parent), "PYTHONDONTWRITEBYTECODE": "1"}, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual([], list(state_directory.glob("*.pending")))
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Task Status: Completed"), state_directory))

    def test_event_wrapped_registration_scope_changes_require_no_review(self) -> None:
        for mode in ("--append-targets", "--new-task"):
            with self.subTest(mode=mode), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                target = parent / "report.md"
                target.write_text("Original report.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
                state_directory = parent / "shardingsphere-codex-code-handoff"
                code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                code_handoff_hook.process_event(self._stop_event("Task Status: Completed"), state_directory)
                addition = parent / "other-report.md"
                addition.write_text("Original additional report.\n", encoding="utf-8")
                following = parent / "following.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(addition)], following, extend=manifest if mode == "--append-targets" else None)
                event = self._shell_event("PreToolUse", repository)
                code_handoff_hook.process_event(event, state_directory)
                if mode == "--new-task":
                    before_path = next(state_directory.glob("*.before.json"))
                    before = json.loads(before_path.read_text(encoding="utf-8"))
                    before.pop("originals")
                    before_path.write_text(json.dumps(before), encoding="utf-8")
                actual = subprocess.run(
                    [sys.executable, str(Path(__file__).with_name("code_handoff_hook.py")),
                     "--register-targets", str(following), "--session-id", "foo_session", mode],
                    text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                    env={**os.environ, "TMPDIR": str(parent), "PYTHONDONTWRITEBYTECODE": "1"}, check=False,
                )
                self.assertEqual(0, actual.returncode, actual.stderr)
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual([], list(state_directory.glob("*.pending")))
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Task Status: Completed"), state_directory))

    def test_event_wrapped_registration_keeps_real_content_changes_pending(self) -> None:
        for change in ("registered-addition", "registered-removal", "git-addition", "git-deletion"):
            with self.subTest(change=change), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "original.py")
                target = parent / "report.md"
                target.write_text("Original report.\n", encoding="utf-8")
                manifest = parent / "snapshots.json"
                code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
                state_directory = parent / "state"
                following = manifest
                if change == "registered-removal":
                    code_handoff_hook.register_targets("foo_session", manifest, state_directory)
                    code_handoff_hook.process_event(self._stop_event("Task Status: Completed"), state_directory)
                    addition = parent / "other-report.md"
                    addition.write_text("Original additional report.\n", encoding="utf-8")
                    following = parent / "following.json"
                    code_handoff_hook.review_scope.capture_file_snapshots([str(addition)], following)
                event = self._shell_event("PreToolUse", repository)
                code_handoff_hook.process_event(event, state_directory)
                code_handoff_hook.register_targets("foo_session", following, state_directory, new_task=change == "registered-removal")
                if change in ("registered-addition", "registered-removal"):
                    target.write_text("Changed report.\n", encoding="utf-8")
                elif change == "git-addition":
                    (repository / "addition.py").write_text("New source.\n", encoding="utf-8")
                else:
                    (repository / "original.py").unlink()
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual(1, len(list(state_directory.glob("*.pending"))))
                actual = code_handoff_hook.process_event(self._stop_event("Task Status: Completed"), state_directory)
                self.assertEqual("block", actual["decision"])

    def test_registration_cli_does_not_write_bytecode(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            hook = parent / ".codex/harness/agents/code_handoff_hook.py"
            helper = parent / ".codex/skills/review-pr/scripts/resolve_review_scope.py"
            hook.parent.mkdir(parents=True)
            helper.parent.mkdir(parents=True)
            shutil.copyfile(Path(__file__).with_name("code_handoff_hook.py"), hook)
            shutil.copyfile(code_handoff_hook.review_scope.__file__, helper)
            environment = dict(os.environ)
            environment.pop("PYTHONDONTWRITEBYTECODE", None)
            environment["TMPDIR"] = str(parent)
            target = parent / "report.md"
            target.write_text("Original report.\n", encoding="utf-8")
            manifest = parent / "snapshots.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], manifest)
            actual = subprocess.run(
                [sys.executable, str(hook), "--register-targets", str(manifest), "--session-id", "foo_session"],
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            self.assertEqual({}, json.loads(actual.stdout))
            actual = subprocess.run(
                [sys.executable, str(hook)], input=json.dumps(self._stop_event("Read-only review complete.")),
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            self.assertEqual({}, json.loads(actual.stdout))
            target.write_text("Changed report.\n", encoding="utf-8")
            actual = subprocess.run(
                [sys.executable, str(hook)], input=json.dumps(self._stop_event("Report updated.")),
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            self.assertEqual("block", json.loads(actual.stdout)["decision"])
            actual = subprocess.run(
                [sys.executable, str(hook)], input=json.dumps(self._stop_event(self._mergeable_handoff())),
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            self.assertEqual({}, json.loads(actual.stdout))
            following_manifest = parent / "following.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(target)], following_manifest)
            actual = subprocess.run(
                [sys.executable, str(hook), "--register-targets", str(following_manifest), "--session-id", "foo_session", "--new-task"],
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            self.assertEqual({}, json.loads(actual.stdout))
            addition = parent / "additional-report.md"
            addition.write_text("Original additional report.\n", encoding="utf-8")
            extension = parent / "extension.json"
            code_handoff_hook.review_scope.capture_file_snapshots([str(addition)], extension, extend=following_manifest)
            actual = subprocess.run(
                [sys.executable, str(hook), "--register-targets", str(extension), "--session-id", "foo_session", "--append-targets"],
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, env=environment, check=False,
            )
            self.assertEqual(0, actual.returncode, actual.stderr)
            self.assertEqual({}, json.loads(actual.stdout))
            self.assertEqual([], list(parent.rglob("__pycache__")))

    def test_registration_cli_reports_invalid_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            manifest = parent / "snapshots.json"
            manifest.write_text('{"version": 2, "files": []}', encoding="utf-8")
            actual = subprocess.run(
                [sys.executable, str(Path(__file__).with_name("code_handoff_hook.py")),
                 "--register-targets", str(manifest), "--session-id", "invalid_session"],
                text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                env={**os.environ, "TMPDIR": str(parent), "PYTHONDONTWRITEBYTECODE": "1"}, check=False,
            )
            self.assertEqual(1, actual.returncode)
            self.assertEqual({
                "decision": "block",
                "reason": "The code handoff validator failed closed. Inspect the project hook before stopping.",
            }, json.loads(actual.stdout))

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
        self.assertEqual("^(Bash|apply_patch|exec_command|write_stdin)$", hooks["PreToolUse"][0]["matcher"])
        self.assertIn("code_handoff_hook.py", hooks["PreToolUse"][0]["hooks"][0]["command"])
        self.assertEqual("^(Bash|apply_patch|exec_command|write_stdin)$", hooks["PostToolUse"][0]["matcher"])
        self.assertIn("code_handoff_hook.py", hooks["PostToolUse"][0]["hooks"][0]["command"])
        self.assertIn("code_handoff_hook.py", hooks["Stop"][0]["hooks"][0]["command"])

    @staticmethod
    def _post_tool_event(patch: str) -> dict[str, object]:
        return {
            "hook_event_name": "PostToolUse",
            "session_id": "foo_session",
            "tool_use_id": "foo_tool",
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
                           commit_message: str | None = "Enforce formal code review handoff") -> str:
        report = (
            "```markdown\n"
            "### Result\n\n"
            "**Review Result: Mergeable**\n\n"
            "The complete standalone Code Correctness Review found no blocking issue.\n\n"
            "### Evidence\n\n"
            f"{evidence}\n\n"
            "### Coverage\n\n"
            "Standalone local candidate; original baseline and every attributed task file were reviewed.\n"
            "```\n\n"
        )
        if commit_message is None:
            return report
        return (
            report +
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
