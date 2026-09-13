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
            self.assertIn("Review Recommendations", actual["reason"])
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event(
                "Review Result: Mergeable\nBlocking Issues: 0\nRequired Changes: None.\n"
                "Review Recommendations: No changes recommended.\nCommit Message: Fix metadata"
            ), state_directory))
            self.assertEqual([], list(state_directory.iterdir()))

    def test_prose_patch_does_not_require_code_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Update File: docs/foo.md"), state_directory)
            self.assertEqual({}, code_handoff_hook.process_event(self._stop_event("Documentation updated."), state_directory))

    def test_zero_blocking_issues_requires_no_changes_recommendation(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            "Review Result: Mergeable\nBlocking Issues: 0\nRequired Changes: None.\nReview Recommendations: None.\nCommit Message: Fix metadata"
        )
        self.assertEqual(['Review Recommendations must state "No changes recommended"'], actual)

    def test_zero_blocking_issues_requires_commit_message(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            "Review Result: Mergeable\nBlocking Issues: 0\nRequired Changes: None.\nReview Recommendations: No changes recommended."
        )
        self.assertEqual(["Commit Message"], actual)

    def test_blocking_handoff_does_not_require_commit_message(self) -> None:
        actual = code_handoff_hook.validate_handoff(
            "Review Result: Review Incomplete\nBlocking Issues: 1\nRequired Changes: Run the unavailable check.\nReview Recommendations: Complete verification."
        )
        self.assertEqual([], actual)

    def test_complete_handoff_clears_pending_marker(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            state_directory = Path(directory)
            code_handoff_hook.process_event(self._post_tool_event("*** Add File: scripts/foo.py"), state_directory)
            actual = code_handoff_hook.process_event(self._stop_event(
                "**Review Result: Mergeable**\n- Blocking Issues: 0\n- Required Changes: None.\n"
                "- Review Recommendations: No changes recommended.\n- Commit Message: Fix metadata"
            ), state_directory)
            self.assertEqual({}, actual)
            self.assertEqual([], list(state_directory.iterdir()))

    def test_project_config_registers_code_handoff_hooks(self) -> None:
        config_path = Path(__file__).parents[2] / "config.toml"
        with config_path.open("rb") as config_file:
            hooks = tomllib.load(config_file)["hooks"]
        self.assertEqual("^apply_patch$", hooks["PostToolUse"][0]["matcher"])
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
    def _stop_event(message: str) -> dict[str, object]:
        return {
            "hook_event_name": "Stop",
            "session_id": "foo_session",
            "last_assistant_message": message,
        }


if __name__ == "__main__":
    unittest.main()
