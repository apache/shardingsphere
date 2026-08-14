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

"""Test the policy harness runner."""

from __future__ import annotations

import copy
import importlib.util
import json
from pathlib import Path
import tempfile
from typing import Any
import unittest
from unittest.mock import patch

RUN_SPEC = importlib.util.spec_from_file_location("agents_harness_run", Path(__file__).with_name("run.py"))
if RUN_SPEC is None or RUN_SPEC.loader is None:
    raise RuntimeError("Unable to load agents harness run.py")
run = importlib.util.module_from_spec(RUN_SPEC)
RUN_SPEC.loader.exec_module(run)


class RunTest(unittest.TestCase):
    """Test the agents harness runner."""

    def setUp(self) -> None:
        self.case = {
            "id": "test_case",
            "group": "test",
            "description": "Valid test case.",
            "phase": "test",
            "ordinary_loop": False,
            "prompt": "Proceed.",
            "decision": "proceed",
            "required_actions": [],
            "allowed_actions": [],
            "forbidden_actions": [],
            "required_reasons": [],
            "critical": True,
        }

    def test_duplicate_action(self) -> None:
        with patch.object(run, "ACTIONS", [*run.ACTIONS, run.ACTIONS[0]]), self.assertRaisesRegex(ValueError, "Duplicate actions"):
            run.validate_cases([self.case])

    def test_duplicate_reason(self) -> None:
        with patch.object(run, "REASONS", [*run.REASONS, run.REASONS[0]]), self.assertRaisesRegex(ValueError, "Duplicate reasons"):
            run.validate_cases([self.case])

    def test_unknown_action(self) -> None:
        for field in ("required_actions", "allowed_actions", "forbidden_actions"):
            with self.subTest(field=field):
                case = copy.deepcopy(self.case)
                case[field] = ["unknown_action"]
                with self.assertRaisesRegex(ValueError, "Unknown actions"):
                    run.validate_cases([case])

    def test_unknown_reason(self) -> None:
        self.case["required_reasons"] = ["unknown_reason"]
        with self.assertRaisesRegex(ValueError, "Unknown reasons"):
            run.validate_cases([self.case])

    def test_required_action_must_be_allowed(self) -> None:
        self.case["required_actions"] = ["inspect_local"]
        with self.assertRaisesRegex(ValueError, "Required actions must be allowed"):
            run.validate_cases([self.case])

    def test_allowed_and_forbidden_actions_must_not_overlap(self) -> None:
        self.case["allowed_actions"] = ["inspect_local"]
        self.case["forbidden_actions"] = ["inspect_local"]
        with self.assertRaisesRegex(ValueError, "Allowed and forbidden actions overlap"):
            run.validate_cases([self.case])

    def test_run_cases_confirms_only_failed_classifications(self) -> None:
        failed_case = copy.deepcopy(self.case)
        failed_case["id"] = "failed_case"
        failed_case["required_actions"] = ["inspect_local"]
        failed_case["allowed_actions"] = ["inspect_local"]
        cases = [self.case, failed_case]
        evaluated_case_ids = []

        def fake_run_codex(
                policy: bytes, codex_home: Path, output_dir: Path, schema_path: Path,
                prompt: str, timeout: int, evaluation_number: int) -> tuple[int, float, dict[str, Any]]:
            del policy, codex_home, output_dir, prompt, timeout
            schema = json.loads(schema_path.read_text(encoding="utf-8"))
            case_ids = schema["properties"]["results"]["items"]["properties"]["case_id"]["enum"]
            evaluated_case_ids.append(case_ids)
            return 0, float(evaluation_number), {"results": [{
                "case_id": each,
                "decision": "proceed",
                "actions": ["inspect_local"] if 1 < evaluation_number else [],
                "reasons": [],
                "summary": "Proceed.",
                "response_style": "concise",
            } for each in case_ids]}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_codex", side_effect=fake_run_codex):
            exit_code, duration, actual, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), cases, "policy_sha256", 1
            )
        self.assertEqual(0, exit_code)
        self.assertEqual(3.0, duration)
        self.assertEqual([[self.case["id"], failed_case["id"]], [failed_case["id"]]], evaluated_case_ids)
        self.assertTrue(all(each["passed"] for each in run.grade(cases, actual)))
        self.assertEqual([[failed_case["id"]], []], [each["failed_case_ids"] for each in evaluations])

    def test_run_cases_preserves_failed_case(self) -> None:
        self.case["required_actions"] = ["inspect_local"]
        self.case["allowed_actions"] = ["inspect_local"]
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": [],
            "reasons": [],
            "summary": "Proceed.",
            "response_style": "concise",
        }]}
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_codex", return_value=(0, 1.0, actual)) as run_codex:
            exit_code, _, result, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), [self.case], "policy_sha256", 1
            )
        self.assertEqual(0, exit_code)
        self.assertFalse(run.grade([self.case], result)[0]["passed"])
        self.assertEqual(run.MAX_EVALUATIONS, run_codex.call_count)
        self.assertEqual([[self.case["id"]], [self.case["id"]]], [each["failed_case_ids"] for each in evaluations])

    def test_run_cases_stops_on_runner_failure(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_codex", return_value=(124, 1.0, {})) as run_codex:
            exit_code, duration, actual, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), [self.case], "policy_sha256", 1
            )
        self.assertEqual(124, exit_code)
        self.assertEqual(1.0, duration)
        self.assertEqual({}, actual)
        self.assertEqual(1, run_codex.call_count)
        self.assertEqual(124, evaluations[0]["runner_exit_code"])

    def test_read_usage_aggregates_evaluations(self) -> None:
        events = [
            {"type": "turn.completed", "usage": {"input_tokens": 10, "cached_input_tokens": 3, "output_tokens": 2}},
            {"type": "turn.completed", "usage": {"input_tokens": 20, "cached_input_tokens": 5, "output_tokens": 4}},
        ]
        with tempfile.TemporaryDirectory() as output_directory:
            events_path = Path(output_directory) / "events.jsonl"
            events_path.write_text("".join(f"{json.dumps(each)}\n" for each in events), encoding="utf-8")
            self.assertEqual({
                "input_tokens": 30,
                "cached_input_tokens": 8,
                "uncached_input_tokens": 22,
                "output_tokens": 6,
            }, run.read_usage(events_path))


if __name__ == "__main__":
    unittest.main()
