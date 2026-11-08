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
from itertools import product
import json
from pathlib import Path
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

    def test_ignored_untracked_patch_changes_require_no_handoff(self) -> None:
        for operation, suffix, input_field in (("Add", ".md", "command"), ("Update", ".java", "patch"), ("Delete", ".md", "command")):
            with self.subTest(operation=operation), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                (repository / ".gitignore").write_text("target/\n", encoding="utf-8")
                workdir = repository / "work"
                workdir.mkdir()
                target = repository / "target" / ("generated report" + suffix)
                target.parent.mkdir()
                if operation != "Add":
                    target.write_text("Original content.\n", encoding="utf-8")
                body = {"Add": "+Changed content.\n", "Update": "@@\n-Original content.\n+Changed content.\n", "Delete": ""}[operation]
                event = self._post_tool_event(f"*** {operation} File: ../target/{target.name}\n{body}")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(workdir)})
                event["tool_input"] = {input_field: event["tool_input"]["command"]}
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                if operation == "Delete":
                    target.unlink()
                else:
                    target.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Ignored file updated."), state_directory))
                self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_bulk_patch_pre_tool_deadline(self) -> None:
        worker = (
            "import importlib.util,json,sys; from pathlib import Path; "
            "spec=importlib.util.spec_from_file_location('measured_hook',sys.argv[1]); "
            "module=importlib.util.module_from_spec(spec); spec.loader.exec_module(module); "
            "print(json.dumps(module.process_event(json.load(sys.stdin),Path(sys.argv[2]))))"
        )
        for layout in ("same-parent", "distinct-parents"):
            with self.subTest(layout=layout), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                patches = []
                for index in range(1500):
                    relative = f"package-{index}/report.md" if layout == "distinct-parents" else f"report-{index}.md"
                    target = repository / relative
                    target.parent.mkdir(parents=True, exist_ok=True)
                    target.write_text("Original content.\n", encoding="utf-8")
                    patches.append(f"*** Update File: {relative}\n@@\n-Original content.\n+Changed content.\n")
                event = self._post_tool_event("".join(patches))
                event.update({"hook_event_name": "PreToolUse", "cwd": str(repository)})
                process = subprocess.run([sys.executable, "-B", "-c", worker, str(Path(__file__).with_name("code_handoff_hook.py")), str(parent / "state")],
                                         input=json.dumps(event), text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True, timeout=5)
                self.assertEqual("{}\n", process.stdout)

    def test_tracked_patch_paths_matching_ignore_patterns(self) -> None:
        for operation, suffix in product(("Update", "Delete"), (".java", ".md")):
            with self.subTest(operation=operation, suffix=suffix), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                owner = self._initialize_repository(parent / "owner", "README.md")
                repository = self._initialize_repository(parent / "target", "README.md")
                (repository / ".gitignore").write_text("target/\n", encoding="utf-8")
                target = repository / "target" / ("report" + suffix)
                target.parent.mkdir()
                target.write_text("Original content.\n", encoding="utf-8")
                subprocess.run(["git", "-C", str(repository), "add", "--force", "--", str(target)], check=True)
                body = "@@\n-Original content.\n+Changed content.\n" if operation == "Update" else ""
                event = self._post_tool_event(f"*** {operation} File: {target}\n{body}")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(owner)})
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                if operation == "Delete":
                    target.unlink()
                else:
                    target.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Tracked file updated."), state_directory)
                self.assertEqual("block", actual["decision"])
                handoff = self._mergeable_handoff(commit_message=None) if suffix == ".md" else self._mergeable_handoff()
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(handoff), state_directory))

    def test_patch_move_into_repository_requires_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            repository = self._initialize_repository(parent / "repository", "README.md")
            source = parent / "external.java"
            source.write_text("Original content.\n", encoding="utf-8")
            target = repository / "reports/report.md"
            target.parent.mkdir()
            event = self._post_tool_event(f"*** Update File: {source}\n*** Move to: reports/report.md\n@@\n-Original content.\n+Moved content.\n")
            event.update({"hook_event_name": "PreToolUse", "cwd": str(repository)})
            state_directory = parent / "state"
            code_handoff_hook.process_event(event, state_directory)
            source.replace(target)
            target.write_text("Moved content.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Repository file added."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))

    def test_patch_external_paths_require_no_handoff(self) -> None:
        for path_form, input_field in (("absolute", "command"), ("traversal", "patch"), ("symlink-parent", "command")):
            with self.subTest(path_form=path_form), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                target = parent / "outside/report.java"
                target.parent.mkdir()
                target.write_text("Original content.\n", encoding="utf-8")
                if path_form == "symlink-parent":
                    (repository / "external-link").symlink_to(target.parent, target_is_directory=True)
                    declared_path = "external-link/report.java"
                else:
                    declared_path = str(target) if path_form == "absolute" else "../outside/report.java"
                event = self._post_tool_event(f"*** Update File: {declared_path}\n@@\n-Original content.\n+Changed content.\n")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(repository)})
                event["tool_input"] = {input_field: event["tool_input"]["command"]}
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                target.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("External file updated."), state_directory))
                self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_patch_symlink_ownership(self) -> None:
        for change in ("repository-entry", "external-target"):
            with self.subTest(change=change), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                original, replacement = parent / "original.java", parent / "replacement.java"
                original.write_text("Original content.\n", encoding="utf-8")
                replacement.write_text("Replacement content.\n", encoding="utf-8")
                target = repository / "reports/report.java"
                target.parent.mkdir()
                target.symlink_to(original)
                event = self._post_tool_event("*** Update File: reports/report.java\n@@\n-Original content.\n+Changed content.\n")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(repository)})
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                if change == "repository-entry":
                    target.unlink()
                    target.symlink_to(replacement)
                else:
                    original.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Patch complete."), state_directory)
                if change == "repository-entry":
                    self.assertEqual("block", actual["decision"])
                else:
                    self.assertEqual({}, actual)

    def test_patch_targets_in_other_repositories(self) -> None:
        for repository_cwd, suffix, operation in product((True, False), (".java", ".md"), ("Update", "Add")):
            with self.subTest(repository_cwd=repository_cwd, suffix=suffix, operation=operation), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                owner = self._initialize_repository(parent / "owner", "README.md")
                repository = self._initialize_repository(parent / "target", "README.md")
                target = repository / ("new/deep/report" + suffix if operation == "Add" else "report" + suffix)
                if operation == "Update":
                    target.write_text("Original content.\n", encoding="utf-8")
                body = "+Changed content.\n" if operation == "Add" else "@@\n-Original content.\n+Changed content.\n"
                event = self._post_tool_event(f"*** {operation} File: {target}\n{body}")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(owner if repository_cwd else parent)})
                if not repository_cwd:
                    event["tool_input"] = {"patch": event["tool_input"]["command"]}
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Repository file updated."), state_directory)
                self.assertEqual("block", actual["decision"])
                handoff = self._mergeable_handoff(commit_message=None) if suffix == ".md" else self._mergeable_handoff()
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(handoff), state_directory))

    def test_patch_no_op_in_other_repository(self) -> None:
        for repository_cwd in (True, False):
            with self.subTest(repository_cwd=repository_cwd), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                owner = self._initialize_repository(parent / "owner", "README.md")
                repository = self._initialize_repository(parent / "target", "report.java")
                target = repository / "report.java"
                event = self._post_tool_event(f"*** Update File: {target}\n@@\n-Initial content.\n+Initial content.\n")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(owner if repository_cwd else parent)})
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                target.write_text("Initial content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("No change was needed."), state_directory))

    def test_patch_same_basename_in_different_repositories(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            owner = self._initialize_repository(parent / "owner", "report.java")
            repository = self._initialize_repository(parent / "target", "report.java")
            original, other = owner / "report.java", repository / "report.java"
            event = self._post_tool_event(
                f"*** Update File: {original}\n@@\n-Initial content.\n+Changed content.\n"
                f"*** Update File: {other}\n@@\n-Initial content.\n+Initial content.\n",
            )
            event.update({"hook_event_name": "PreToolUse", "cwd": str(owner)})
            state_directory = parent / "state"
            code_handoff_hook.process_event(event, state_directory)
            original.write_text("Changed content.\n", encoding="utf-8")
            other.write_text("Initial content.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Repository file updated."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_patch_symlink_write_target_ownership(self) -> None:
        for repository_cwd, repository_target in product((True, False), (True, False)):
            with self.subTest(repository_cwd=repository_cwd, repository_target=repository_target), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                owner = self._initialize_repository(parent / "owner", "README.md") if repository_cwd else parent
                target_directory = self._initialize_repository(parent / "target", "README.md") if repository_target else parent
                target = target_directory / "report.java"
                target.write_text("Original content.\n", encoding="utf-8")
                link = owner / "link.md"
                link.symlink_to(target)
                event = self._post_tool_event(f"*** Update File: {link}\n@@\n-Original content.\n+Changed content.\n")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(owner)})
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                link.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Patch complete."), state_directory)
                if repository_target:
                    self.assertEqual("block", actual["decision"])
                    actual = code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory)
                    self.assertEqual("block", actual["decision"])
                    self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff()), state_directory))
                else:
                    self.assertEqual({}, actual)

    def test_patch_symlink_deletion_preserves_target_identity(self) -> None:
        for repository_cwd in (True, False):
            with self.subTest(repository_cwd=repository_cwd), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                owner = self._initialize_repository(parent / "owner", "README.md") if repository_cwd else parent
                repository = self._initialize_repository(parent / "target", "report.java")
                link = owner / "link.md"
                link.symlink_to(repository / "report.java")
                event = self._post_tool_event(f"*** Delete File: {link}")
                event.update({"hook_event_name": "PreToolUse", "cwd": str(owner)})
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                link.unlink()
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Link removed."), state_directory)
                if repository_cwd:
                    self.assertEqual("block", actual["decision"])
                    self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))
                else:
                    self.assertEqual({}, actual)

    def test_status_change_without_content_requires_no_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "Foo.java")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            code_handoff_hook.process_event(event, state_directory)
            subprocess.run(["git", "-C", str(repository_path), "add", "--", "Foo.java"], check=True)
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Content is unchanged."), state_directory))

    def test_external_change_requires_no_handoff(self) -> None:
        for use_repository in (True, False):
            with self.subTest(use_repository=use_repository), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                repository = self._initialize_repository(parent / "repository", "README.md")
                target = parent / "external.py"
                target.write_text("Original content.\n", encoding="utf-8")
                state_directory = parent / "state"
                event = self._shell_event("PreToolUse", repository if use_repository else parent)
                code_handoff_hook.process_event(event, state_directory)
                target.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("External file updated."), state_directory))
                self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_mixed_change_requires_repository_review(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            repository = self._initialize_repository(parent / "repository", "README.md")
            target = parent / "external.py"
            target.write_text("Original content.\n", encoding="utf-8")
            state_directory = parent / "state"
            event = self._shell_event("PreToolUse", repository)
            code_handoff_hook.process_event(event, state_directory)
            (repository / "README.md").write_text("Changed documentation.\n", encoding="utf-8")
            target.write_text("Changed content.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Files updated."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))

    def test_declared_git_inspection_failure_requires_handoff(self) -> None:
        for failure in ("index", "config", "selected-config"):
            with self.subTest(failure=failure), tempfile.TemporaryDirectory() as directory:
                parent = Path(directory)
                owner = self._initialize_repository(parent / "owner", "README.md")
                repository = self._initialize_repository(parent / "target", "report.md")
                target = repository / "report.md"
                subprocess.run(["git", "-C", str(repository), "add", "--", "report.md"], check=True)
                (repository / ".git" / ("index" if failure == "index" else "config")).write_bytes(b"broken-index" if failure == "index" else b"[broken\n")
                arguments = ["ls-files", "--cached", "--others", "--exclude-standard", "--full-name", "-z"] if failure == "index" else ["rev-parse", "--show-toplevel"]
                failed = subprocess.run(["git", "-C", str(repository), *arguments], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
                self.assertNotEqual(0, failed.returncode)
                subprocess.run(["git", "-C", str(owner), "status", "--porcelain=v1"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
                if failure == "selected-config":
                    event = self._shell_event("PreToolUse", owner)
                    event["tool_input"] = {"cmd": "write report.md", "workdir": str(repository)}
                else:
                    event = self._post_tool_event(f"*** Update File: {target}\n@@\n-Initial content.\n+Changed content.\n")
                    event.update({"hook_event_name": "PreToolUse", "cwd": str(owner)})
                state_directory = parent / "state"
                code_handoff_hook.process_event(event, state_directory)
                target.write_text("Changed content.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Repository file updated."), state_directory)
                self.assertEqual("block", actual["decision"])

    def test_failed_git_status_requires_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as state:
            repository_path = self._initialize_repository(Path(repository), "README.md")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", repository_path)
            run_git = code_handoff_hook._run_git

            def fail_status(directory: Path, *arguments: str):
                return None if arguments[0] == "status" else run_git(directory, *arguments)

            with patch.object(code_handoff_hook, "_run_git", side_effect=fail_status):
                code_handoff_hook.process_event(event, state_directory)
                (repository_path / "README.md").write_text("Changed documentation.\n", encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory)
            self.assertEqual("block", actual.get("decision"))

    def test_invalid_pre_tool_snapshot_requires_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            state_directory = parent / "state"
            event = self._shell_event("PreToolUse", parent)
            code_handoff_hook.process_event(event, state_directory)
            next(state_directory.glob("*.before.json")).write_text("{", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("External work completed."), state_directory)
            self.assertEqual("block", actual["decision"])

    def test_restore_keeps_an_existing_pending_review(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            repository = self._initialize_repository(parent / "repository", "AGENTS.md")
            state_directory = parent / "state"
            for content in ("Changed instructions.\n", "Initial content.\n"):
                event = self._shell_event("PreToolUse", repository)
                code_handoff_hook.process_event(event, state_directory)
                (repository / "AGENTS.md").write_text(content, encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
            actual = code_handoff_hook.process_event(self._stop_event("Task Status: Completed\nThe change was restored."), state_directory)
            self.assertEqual("block", actual["decision"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))
            self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_later_prose_write_requires_a_new_completed_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            parent = Path(directory)
            repository = self._initialize_repository(parent / "repository", "README.md")
            state_directory = parent / "state"
            for content in ("Reviewed documentation.\n", "Later documentation.\n"):
                event = self._shell_event("PreToolUse", repository)
                code_handoff_hook.process_event(event, state_directory)
                (repository / "README.md").write_text(content, encoding="utf-8")
                event["hook_event_name"] = "PostToolUse"
                code_handoff_hook.process_event(event, state_directory)
                actual = code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory)
                self.assertEqual("block", actual["decision"])
                self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(self._mergeable_handoff(commit_message=None)), state_directory))

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

    def test_external_change_from_non_git_workdir_requires_no_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as outside, tempfile.TemporaryDirectory() as state:
            original = self._initialize_repository(Path(repository), "Foo.java")
            target = Path(outside) / "external.py"
            target.write_text("Original content.\n", encoding="utf-8")
            state_directory = Path(state)
            event = self._shell_event("PreToolUse", original)
            event["tool_input"] = {"cmd": "ignored", "workdir": outside}
            code_handoff_hook.process_event(event, state_directory)
            target.write_text("Changed content.\n", encoding="utf-8")
            event["hook_event_name"] = "PostToolUse"
            code_handoff_hook.process_event(event, state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("External file updated."), state_directory))
            self.assertEqual([], list(state_directory.glob("*.pending")))

    def test_repository_change_from_non_git_workdir_requires_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as repository, tempfile.TemporaryDirectory() as outside:
            original = self._initialize_repository(Path(repository), "Foo.java")
            for workdir in ("missing-directory", outside):
                with self.subTest(workdir=workdir), tempfile.TemporaryDirectory() as state:
                    state_directory = Path(state)
                    event = self._shell_event("PreToolUse", original)
                    event["tool_input"] = {"cmd": "ignored", "workdir": workdir}
                    code_handoff_hook.process_event(event, state_directory)
                    (original / "Foo.java").write_text(f"Changed in {workdir}.\n", encoding="utf-8")
                    event["hook_event_name"] = "PostToolUse"
                    code_handoff_hook.process_event(event, state_directory)
                    actual = code_handoff_hook.process_event(self._stop_event("Repository file updated."), state_directory)
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
