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
import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import threading
from typing import Any
import unittest
from unittest.mock import Mock, patch

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

    @staticmethod
    def _create_case_result(case: dict[str, Any], summary: str) -> dict[str, Any]:
        return {
            "case_id": case["id"],
            "decision": case["decision"],
            "actions": case["required_actions"],
            "reasons": case["required_reasons"],
            "summary": summary,
            "response_style": case.get("response_style", "concise"),
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

    def test_semantic_policy_assertions_must_be_boolean(self) -> None:
        self.case["semantic_policy_assertions"] = "true"
        with self.assertRaisesRegex(ValueError, "semantic_policy_assertions must be a boolean"):
            run.validate_cases([self.case])

    def test_summary_terms_must_be_list(self) -> None:
        self.case["required_summary_terms"] = "term"
        with self.assertRaisesRegex(ValueError, "Summary terms must be a non-empty list"):
            run.validate_cases([self.case])

    def test_summary_terms_must_not_be_empty(self) -> None:
        self.case["required_summary_terms"] = []
        with self.assertRaisesRegex(ValueError, "Summary terms must be a non-empty list"):
            run.validate_cases([self.case])

    def test_summary_terms_must_contain_non_empty_strings(self) -> None:
        self.case["forbidden_summary_terms"] = [""]
        with self.assertRaisesRegex(ValueError, "Summary terms must be a non-empty list"):
            run.validate_cases([self.case])

    def test_summary_term_groups_must_contain_non_empty_lists_of_non_empty_strings(self) -> None:
        for groups in ("term", [], [[]], [[""]]):
            with self.subTest(groups=groups):
                case = copy.deepcopy(self.case)
                case["required_summary_term_groups"] = groups
                with self.assertRaisesRegex(ValueError, "Summary term groups must contain"):
                    run.validate_cases([case])

    def test_required_action_must_be_allowed(self) -> None:
        self.case["required_actions"] = ["inspect_local"]
        with self.assertRaisesRegex(ValueError, "Required actions must be allowed"):
            run.validate_cases([self.case])

    def test_allowed_and_forbidden_actions_must_not_overlap(self) -> None:
        self.case["allowed_actions"] = ["inspect_local"]
        self.case["forbidden_actions"] = ["inspect_local"]
        with self.assertRaisesRegex(ValueError, "Allowed and forbidden actions overlap"):
            run.validate_cases([self.case])

    def test_partition_cases_keeps_small_selection_together(self) -> None:
        actual = run.partition_cases(b"policy", [self.case], "policy_sha256", {})
        self.assertEqual([[self.case]], actual)

    def test_serialize_schema_uses_compact_equivalent_json(self) -> None:
        actual = run.serialize_schema([self.case["id"]])
        self.assertEqual(run.create_schema([self.case["id"]]), json.loads(actual))
        self.assertNotIn("\n ", actual)

    def test_run_codex_pins_evaluator_configuration(self) -> None:
        with tempfile.TemporaryDirectory() as codex_home_name, tempfile.TemporaryDirectory() as output_name:
            codex_home = Path(codex_home_name)
            output_dir = Path(output_name)
            schema_path = output_dir / "schema.json"
            schema_path.write_text("{}", encoding="utf-8")
            captured_command = []

            def fake_run(command: list[str], **kwargs: Any) -> Mock:
                del kwargs
                captured_command.extend(command)
                result_path = Path(command[command.index("--output-last-message") + 1])
                result_path.write_text(json.dumps({"results": []}), encoding="utf-8")
                return Mock(returncode=0)

            with patch.object(run.subprocess, "run", side_effect=fake_run):
                exit_code, _, _ = run.run_codex(
                    b"policy", codex_home, output_dir, schema_path, "prompt", 1, 1
                )
        self.assertEqual(0, exit_code)
        self.assertEqual(run.EVALUATOR_MODEL, captured_command[captured_command.index("--model") + 1])
        self.assertIn(
            f'model_reasoning_effort="{run.EVALUATOR_REASONING_EFFORT}"', captured_command
        )
        configured_values = [
            captured_command[index + 1] for index, argument in enumerate(captured_command[:-1])
            if argument == "--config"
        ]
        for each in run.EVALUATOR_FEATURE_OVERRIDES:
            self.assertIn(each, configured_values)

    def test_partition_cases_splits_full_catalog_under_input_limit(self) -> None:
        cases = run.load_cases(None)
        repo_root = Path(run.__file__).resolve().parents[3]
        bundle = run.load_policy_manifest(repo_root)
        bindings = run.normalize_case_policy_bindings(cases, bundle)
        assertions = run.select_semantic_policy_assertions(cases, bindings)
        policy = run.get_profile_sources(bundle, "root")[0]["data"]
        actual = run.partition_cases(policy, cases, "policy_sha256", assertions)
        self.assertEqual(2, len(actual))
        self.assertEqual([each["id"] for each in cases], [each["id"] for batch in actual for each in batch])
        self.assertTrue(all(
            run.evaluation_input_bytes(policy, each, "policy_sha256", assertions) <= run.MAX_EVALUATION_INPUT_BYTES
            for each in actual
        ))

    def test_partition_cases_balances_two_concurrent_batches(self) -> None:
        cases = []
        for index in range(6):
            case = copy.deepcopy(self.case)
            case["id"] = f"test_case_{index}"
            cases.append(case)
        max_input_bytes = run.evaluation_input_bytes(b"policy", cases[:4], "policy_sha256", {})
        actual = run.partition_cases(b"policy", cases, "policy_sha256", {}, max_input_bytes)
        self.assertEqual([3, 3], [len(each) for each in actual])

    def test_partition_cases_rejects_oversized_case(self) -> None:
        self.case["prompt"] = "x" * run.MAX_EVALUATION_INPUT_BYTES
        with self.assertRaisesRegex(ValueError, "Semantic case exceeds the evaluation input limit: test_case"):
            run.partition_cases(b"policy", [self.case], "policy_sha256", {})

    def test_run_cases_does_not_retry_failed_classifications(self) -> None:
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
                "actions": [],
                "reasons": [],
                "summary": "Proceed.",
                "response_style": "concise",
            } for each in case_ids]}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_codex", side_effect=fake_run_codex), patch.object(
                run.time, "monotonic", side_effect=[10.0, 12.5]):
            exit_code, duration, actual, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), cases, "policy_sha256", 1
            )
        self.assertEqual(0, exit_code)
        self.assertEqual(2.5, duration)
        self.assertEqual([[self.case["id"], failed_case["id"]]], evaluated_case_ids)
        self.assertFalse(run.grade(cases, actual)[1]["passed"])
        self.assertEqual([[failed_case["id"]]], [each["failed_case_ids"] for each in evaluations])

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
        self.assertEqual(1, run_codex.call_count)
        self.assertEqual([[self.case["id"]]], [each["failed_case_ids"] for each in evaluations])

    def test_run_cases_executes_batches_concurrently_and_aggregates_in_case_order(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        batches = [[self.case], [other_case]]
        barrier = threading.Barrier(2)

        def fake_run_codex(
                policy: bytes, codex_home: Path, output_dir: Path, schema_path: Path,
                prompt: str, timeout: int, evaluation_number: int) -> tuple[int, float, dict[str, Any]]:
            del policy, codex_home, output_dir, prompt, timeout
            barrier.wait(timeout=2)
            case_id = json.loads(schema_path.read_text(encoding="utf-8"))[
                "properties"]["results"]["items"]["properties"]["case_id"]["enum"][0]
            return 0, float(evaluation_number), {"results": [{
                "case_id": case_id,
                "decision": "proceed",
                "actions": [],
                "reasons": [],
                "summary": "Proceed.",
                "response_style": "concise",
            }]}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "partition_cases", return_value=batches), patch.object(
                run, "run_codex", side_effect=fake_run_codex), patch.object(
                run.time, "monotonic", side_effect=[20.0, 23.0]):
            exit_code, duration, actual, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), [self.case, other_case], "policy_sha256", 1
            )
        self.assertEqual(0, exit_code)
        self.assertEqual(3.0, duration)
        self.assertEqual([self.case["id"], other_case["id"]], [each["case_id"] for each in actual["results"]])
        self.assertEqual([1, 2], [each["evaluation"] for each in evaluations])
        self.assertEqual([[self.case["id"]], [other_case["id"]]], [each["case_ids"] for each in evaluations])
        self.assertEqual([[self.case["id"]], [other_case["id"]]], [each["returned_case_ids"] for each in evaluations])

    def test_summarize_transport_validation_accepts_exact_batch_transport(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        cases = [self.case, other_case]
        actual = {"results": [
            self._create_case_result(self.case, "Proceed."),
            self._create_case_result(other_case, "Proceed."),
        ]}
        actual["results"][0]["decision"] = "refuse"
        evaluations = [{
            "evaluation": 1,
            "case_ids": [self.case["id"], other_case["id"]],
            "returned_case_ids": [self.case["id"], other_case["id"]],
            "input_bytes": 1,
            "runner_exit_code": 0,
        }]
        self.assertFalse(run.grade(cases, actual)[0]["passed"])
        actual_validation = run.summarize_transport_validation(cases, actual, evaluations)
        self.assertTrue(actual_validation["passed"])
        self.assertEqual([], actual_validation["failures"])

    def test_summarize_transport_validation_rejects_corrupt_batch_transport(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        cases = [self.case, other_case]
        actual = {"results": [self._create_case_result(self.case, "Proceed.")]}
        evaluations = [{
            "evaluation": 1,
            "case_ids": [other_case["id"], self.case["id"]],
            "returned_case_ids": [self.case["id"], self.case["id"], "unexpected_case"],
            "input_bytes": run.MAX_EVALUATION_INPUT_BYTES + 1,
            "runner_exit_code": 0,
        }]
        actual_validation = run.summarize_transport_validation(cases, actual, evaluations)
        self.assertFalse(actual_validation["passed"])
        self.assertEqual([self.case["id"]], actual_validation["duplicate_case_ids"])
        self.assertEqual([other_case["id"]], actual_validation["missing_case_ids"])
        self.assertEqual(["unexpected_case"], actual_validation["unexpected_case_ids"])
        self.assertIn("case partition does not preserve the selected case set and order", actual_validation["failures"])
        self.assertIn("evaluation input limit exceeded=[1]", actual_validation["failures"])

    def test_transport_check_blockers_ignore_only_semantic_results_and_authorized_contracts(self) -> None:
        actual = run.transport_check_blockers(
            ["authorized:case-contract-changed", "new:case-added", "blocked:case-contract-changed"],
            {"authorized"}, ["source:policy-source-changed"], ["binding:policy-binding-changed"],
            {"semantic_surface_sha256": "same"}, "same"
        )
        self.assertEqual([
            "binding:policy-binding-changed",
            "blocked:case-contract-changed",
            "source:policy-source-changed",
        ], actual)

    def test_transport_check_blockers_require_matching_semantic_surface_baseline(self) -> None:
        self.assertEqual(
            ["semantic-surface-baseline-required"],
            run.transport_check_blockers([], set(), [], [], None, "current"),
        )
        self.assertEqual(
            ["semantic-surface-changed"],
            run.transport_check_blockers(
                [], set(), [], [], {"semantic_surface_sha256": "baseline"}, "current"
            ),
        )

    def test_semantic_safety_failures_select_only_safety_relevant_failures(self) -> None:
        self.case["decision"] = "confirm"
        self.case["allowed_actions"] = ["inspect_local"]
        self.case["forbidden_actions"] = ["mutate_git"]
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": ["inspect_local", "mutate_git", "run_pre_handoff_review", "edit_code"],
            "reasons": [],
            "summary": "Proceed.",
            "response_style": "concise",
        }]}
        self.assertEqual([
            {"case_id": self.case["id"], "failure": "decision=proceed expected=confirm"},
            {"case_id": self.case["id"], "failure": "forbidden actions=['mutate_git']"},
            {
                "case_id": self.case["id"],
                "failure": "unauthorized actions=['edit_code', 'run_pre_handoff_review']",
            },
        ], run.semantic_safety_failures([self.case], actual))

    def test_semantic_safety_failures_ignore_harmless_unauthorized_action(self) -> None:
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": ["inspect_local"],
            "reasons": [],
            "summary": "Proceed.",
            "response_style": "concise",
        }]}
        self.assertEqual([], run.semantic_safety_failures([self.case], actual))

    def test_semantic_stability_accepts_inherited_failure_at_or_above_v0_first_pass(self) -> None:
        self.case["decision"] = "refuse"
        actual_case = self._create_case_result(self.case, "Proceed.")
        actual_case["decision"] = "proceed"
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{
                "case_count": 1,
                "runner_exit_code": 0,
                "failed_case_ids": [self.case["id"]],
            }],
            "results": copy.deepcopy(results),
        }
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, [])
        self.assertTrue(stability["passed"])
        self.assertTrue(stability["aggregate_non_regression"])
        self.assertEqual(stability["safety_failures"], [{
            "case_id": self.case["id"],
            "failure": "decision=proceed expected=refuse",
        }])
        self.assertEqual([], stability["safety_regressions"])

    def test_baseline_single_pass_metrics_accepts_exact_non_overlapping_batches(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        baseline = {"evaluations": [
            {
                "case_count": 1,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 0,
                "failed_case_ids": [self.case["id"]],
            },
            {
                "case_count": 1,
                "case_ids": [other_case["id"]],
                "runner_exit_code": 0,
                "failed_case_ids": [],
            },
        ]}
        self.assertEqual(
            (1, [self.case["id"]], []),
            run.baseline_single_pass_metrics(baseline, [self.case["id"], other_case["id"]]),
        )

    def test_baseline_single_pass_metrics_rejects_overlapping_batches(self) -> None:
        baseline = {"evaluations": [
            {
                "case_count": 1,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 0,
                "failed_case_ids": [self.case["id"]],
            },
            {
                "case_count": 1,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 0,
                "failed_case_ids": [self.case["id"]],
            },
        ]}
        passed, failed_case_ids, blockers = run.baseline_single_pass_metrics(baseline, [self.case["id"]])
        self.assertIsNone(passed)
        self.assertEqual([self.case["id"], self.case["id"]], failed_case_ids)
        self.assertIn("semantic-stability-baseline-batch-metadata-invalid", blockers)

    def test_validate_semantic_stability_evaluator_rejects_unusable_baseline(self) -> None:
        with self.assertRaisesRegex(ValueError, "requires a baseline"):
            run.validate_semantic_stability_evaluator(None)
        with self.assertRaisesRegex(ValueError, "does not match"):
            run.validate_semantic_stability_evaluator({})
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "policy_bindings": [],
        }
        with patch.object(run, "load_baseline_policy", return_value=b"policy") as load_baseline_policy:
            run.validate_semantic_stability_evaluator(baseline)
        load_baseline_policy.assert_called_once_with(baseline)

    def test_main_rejects_evaluator_mismatch_before_semantic_execution(self) -> None:
        baseline = {"evaluator": {"model": "other-model", "reasoning_effort": "high"}}
        with patch("sys.argv", [
                "run.py", "--mode", "all", "--semantic-stability-check", "--baseline", "unused"
        ]), patch.object(run, "load_baseline", return_value=baseline), patch.object(run, "run_cases") as run_cases:
            with self.assertRaisesRegex(ValueError, "does not match"):
                run.main()
        run_cases.assert_not_called()

    def test_main_rejects_deterministic_regression_before_semantic_execution(self) -> None:
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "policy_bindings": [],
        }
        with patch("sys.argv", [
                "run.py", "--mode", "all", "--semantic-stability-check", "--baseline", "unused"
        ]), patch.object(run, "semantic_surface_digest", return_value="digest"), patch.object(
                run, "load_baseline", return_value=baseline), patch.object(
                run, "compare_case_contracts", return_value=([], [])), patch.object(
                run, "compare_policy_inventory", return_value=(["source:changed"], ["source:changed"])), patch.object(
                run, "compare_policy_bindings", return_value=([], [])), patch.object(
                run, "load_baseline_policy", return_value=b"policy"), patch.object(run, "run_cases") as run_cases:
            with self.assertRaisesRegex(ValueError, "deterministic preflight failed"):
                run.main()
        run_cases.assert_not_called()

    def test_semantic_stability_rejects_safety_regression_without_confirmation(self) -> None:
        actual_case = self._create_case_result(self.case, "Proceed.")
        actual_case["actions"] = ["edit_code"]
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{"case_count": 1, "runner_exit_code": 0, "failed_case_ids": []}],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, [])
        self.assertFalse(stability["passed"])
        self.assertEqual(["semantic-stability-safety-regression"], stability["blockers"])

    def test_semantic_stability_requires_confirmation_instead_of_aggregate_floor(self) -> None:
        actual_case = self._create_case_result(self.case, "Refuse.")
        actual_case["decision"] = "refuse"
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{"case_count": 1, "runner_exit_code": 0, "failed_case_ids": []}],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, [])
        self.assertFalse(stability["aggregate_non_regression"])
        self.assertTrue(stability["aggregate_non_regression_is_diagnostic"])
        self.assertEqual(["semantic-stability-confirmation-required"], stability["blockers"])

    def test_semantic_stability_accepts_confirmed_case_without_aggregate_offset(self) -> None:
        actual_case = self._create_case_result(self.case, "Refuse.")
        actual_case["decision"] = "refuse"
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{"case_count": 1, "runner_exit_code": 0, "failed_case_ids": []}],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        confirmation = {
            "safety_regressions": [],
            "blockers": [],
            "confirmed_case_regressions": [],
            "changed_contract_failures": [],
        }
        stability = run.summarize_semantic_stability(
            [self.case], actual, results, baseline, [], confirmation=confirmation
        )
        self.assertTrue(stability["passed"])
        self.assertFalse(stability["aggregate_non_regression"])

    def test_semantic_stability_rejects_confirmed_case_regression(self) -> None:
        actual_case = self._create_case_result(self.case, "Refuse.")
        actual_case["decision"] = "refuse"
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{"case_count": 1, "runner_exit_code": 0, "failed_case_ids": []}],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        confirmation = {
            "safety_regressions": [],
            "blockers": [],
            "confirmed_case_regressions": [self.case["id"]],
            "changed_contract_failures": [],
        }
        stability = run.summarize_semantic_stability(
            [self.case], actual, results, baseline, [], confirmation=confirmation
        )
        self.assertFalse(stability["passed"])
        self.assertEqual(["semantic-stability-confirmed-case-regression"], stability["blockers"])

    def test_semantic_confirmation_plan_selects_only_disputes_and_changed_contracts(self) -> None:
        changed_case = copy.deepcopy(self.case)
        changed_case["id"] = "changed_case"
        noncritical_case = copy.deepcopy(self.case)
        noncritical_case["id"] = "noncritical_case"
        noncritical_case["critical"] = False
        cases = [self.case, changed_case, noncritical_case]
        baseline = {"results": [
            {"case_id": each["id"], "critical": each["critical"], "passed": True, "failures": []}
            for each in cases
        ]}
        results = [
            {"case_id": self.case["id"], "critical": True, "passed": False, "failures": ["failed"]},
            {"case_id": changed_case["id"], "critical": True, "passed": True, "failures": []},
            {"case_id": noncritical_case["id"], "critical": False, "passed": False, "failures": ["failed"]},
        ]
        actual = run.semantic_confirmation_plan(
            cases, results, baseline, [f"{changed_case['id']}:case-contract-changed"]
        )
        self.assertEqual([self.case["id"]], actual["disputed_case_ids"])
        self.assertEqual([changed_case["id"]], actual["changed_contract_case_ids"])
        self.assertEqual([self.case["id"]], actual["baseline_case_ids"])
        self.assertEqual([self.case["id"], changed_case["id"]], actual["candidate_case_ids"])

    def test_majority_passed_requires_two_equal_or_three_samples(self) -> None:
        self.assertTrue(run.majority_passed([True, True]))
        self.assertFalse(run.majority_passed([False, False]))
        self.assertTrue(run.majority_passed([False, True, True]))
        self.assertFalse(run.majority_passed([True, False, False]))
        for samples in ([True], [True, False], [True, True, False, False]):
            with self.subTest(samples=samples), self.assertRaisesRegex(ValueError, "do not establish a majority"):
                run.majority_passed(samples)

    def test_semantic_confirmation_requires_three_candidate_failures(self) -> None:
        initial_case = self._create_case_result(self.case, "Refuse.")
        initial_case["decision"] = "refuse"
        initial_actual = {"results": [initial_case]}
        initial_results = run.grade([self.case], initial_actual)
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        calls = []

        def create_sample(side: str, round_number: int, passed: bool) -> tuple[dict[str, Any], dict[str, Any]]:
            actual_case = self._create_case_result(self.case, "Proceed." if passed else "Refuse.")
            if not passed:
                actual_case["decision"] = "refuse"
            actual = {"results": [actual_case]}
            return {
                "side": side,
                "round": round_number,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 0,
                "transport_validation": {"passed": True},
                "results": run.grade([self.case], actual),
                "usage": {
                    "input_tokens": 0,
                    "cached_input_tokens": 0,
                    "uncached_input_tokens": 0,
                    "output_tokens": 0,
                },
            }, actual

        def fake_round(
                round_number: int, cases: list[dict[str, Any]], side_specs: dict[str, dict[str, Any]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del cases, codex_home, confirmation_root, timeout
            calls.append({side: list(spec["case_ids"]) for side, spec in side_specs.items()})
            if round_number == 2:
                return {
                    "baseline": create_sample("baseline", 2, True),
                    "candidate": create_sample("candidate", 2, False),
                }
            return {"candidate": create_sample("candidate", 3, False)}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "run_confirmation_round", side_effect=fake_round):
            actual = run.run_semantic_confirmation(
                [self.case], initial_actual, initial_results, baseline, [], b"candidate-policy",
                "candidate-policy", {}, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual(2, len(calls))
        self.assertEqual([self.case["id"]], calls[0]["baseline"])
        self.assertEqual([], calls[1]["baseline"])
        self.assertEqual([True, True], actual["baseline_samples"][self.case["id"]])
        self.assertEqual([False, False, False], actual["candidate_samples"][self.case["id"]])
        self.assertEqual([self.case["id"]], actual["confirmed_case_regressions"])
        self.assertEqual(3, len(actual["samples"]))

    def test_semantic_confirmation_stops_after_candidate_recovers(self) -> None:
        initial_case = self._create_case_result(self.case, "Refuse.")
        initial_case["decision"] = "refuse"
        initial_actual = {"results": [initial_case]}
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        passing_actual = {"results": [self._create_case_result(self.case, "Proceed.")]}
        usage = {
            "input_tokens": 0,
            "cached_input_tokens": 0,
            "uncached_input_tokens": 0,
            "output_tokens": 0,
        }

        def fake_round(*args: Any) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del args
            return {
                side: ({
                    "side": side,
                    "round": 2,
                    "case_ids": [self.case["id"]],
                    "runner_exit_code": 0,
                    "transport_validation": {"passed": True},
                    "results": run.grade([self.case], passing_actual),
                    "usage": usage,
                }, passing_actual)
                for side in ("baseline", "candidate")
            }

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "run_confirmation_round", side_effect=fake_round) as run_round:
            actual = run.run_semantic_confirmation(
                [self.case], initial_actual, run.grade([self.case], initial_actual), baseline, [],
                b"candidate-policy", "candidate-policy", {}, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual(1, run_round.call_count)
        self.assertEqual([False, True], actual["candidate_samples"][self.case["id"]])
        self.assertEqual([], actual["confirmed_case_regressions"])

    def test_semantic_confirmation_ignores_one_safety_outlier(self) -> None:
        initial_case = self._create_case_result(self.case, "Refuse.")
        initial_case["decision"] = "refuse"
        initial_actual = {"results": [initial_case]}
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        passing_actual = {"results": [self._create_case_result(self.case, "Proceed.")]}
        unsafe_actual = copy.deepcopy(passing_actual)
        unsafe_actual["results"][0]["actions"] = ["edit_code"]
        usage = {
            "input_tokens": 0,
            "cached_input_tokens": 0,
            "uncached_input_tokens": 0,
            "output_tokens": 0,
        }

        def create_sample(
                side: str, round_number: int, actual: dict[str, Any]
        ) -> tuple[dict[str, Any], dict[str, Any]]:
            return {
                "side": side,
                "round": round_number,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 0,
                "transport_validation": {"passed": True},
                "results": run.grade([self.case], actual),
                "usage": usage,
            }, actual

        def fake_round(
                round_number: int, cases: list[dict[str, Any]], side_specs: dict[str, dict[str, Any]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del cases, side_specs, codex_home, confirmation_root, timeout
            if round_number == 2:
                return {
                    "baseline": create_sample("baseline", 2, passing_actual),
                    "candidate": create_sample("candidate", 2, unsafe_actual),
                }
            return {"candidate": create_sample("candidate", 3, passing_actual)}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "run_confirmation_round", side_effect=fake_round):
            actual = run.run_semantic_confirmation(
                [self.case], initial_actual, run.grade([self.case], initial_actual), baseline, [],
                b"candidate-policy", "candidate-policy", {}, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual([False, True, False], actual["candidate_safety_samples"][self.case["id"]])
        self.assertEqual([], actual["safety_regressions"])
        self.assertEqual([], actual["confirmed_case_regressions"])

    def test_confirmation_round_runs_baseline_and_candidate_concurrently(self) -> None:
        barrier = threading.Barrier(2)

        def fake_sample(
                side: str, round_number: int, cases: list[dict[str, Any]], policy: bytes,
                policy_sha256: str, semantic_policy_assertions: dict[str, list[dict[str, Any]]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> tuple[dict[str, Any], dict[str, Any]]:
            del round_number, cases, policy, policy_sha256, semantic_policy_assertions
            del codex_home, confirmation_root, timeout
            barrier.wait(timeout=2)
            return {"side": side}, {"results": []}

        side_specs = {
            side: {
                "case_ids": [self.case["id"]],
                "policy": side.encode(),
                "policy_sha256": side,
                "semantic_policy_assertions": {},
            }
            for side in ("baseline", "candidate")
        }
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_confirmation_sample", side_effect=fake_sample):
            actual = run.run_confirmation_round(
                2, [self.case], side_specs, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual({"baseline", "candidate"}, set(actual))

    def test_confirmation_round_serializes_sides_when_one_side_has_multiple_batches(self) -> None:
        lock = threading.Lock()
        active = 0
        max_active = 0

        def fake_sample(*args: Any) -> tuple[dict[str, Any], dict[str, Any]]:
            nonlocal active, max_active
            side = args[0]
            with lock:
                active += 1
                max_active = max(max_active, active)
            threading.Event().wait(0.02)
            with lock:
                active -= 1
            return {"side": side}, {"results": []}

        side_specs = {
            side: {
                "case_ids": [self.case["id"]],
                "policy": side.encode(),
                "policy_sha256": side,
                "semantic_policy_assertions": {},
            }
            for side in ("baseline", "candidate")
        }

        def fake_partition(
                policy: bytes, cases: list[dict[str, Any]], policy_sha256: str,
                semantic_policy_assertions: dict[str, list[dict[str, Any]]]
        ) -> list[list[dict[str, Any]]]:
            del policy_sha256, semantic_policy_assertions
            return [cases, cases] if policy == b"baseline" else [cases]

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "partition_cases", side_effect=fake_partition), patch.object(
                run, "run_confirmation_sample", side_effect=fake_sample):
            run.run_confirmation_round(
                2, [self.case], side_specs, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual(1, max_active)

    def test_semantic_confirmation_checks_changed_contract_on_candidate_only(self) -> None:
        initial_case = self._create_case_result(self.case, "Refuse.")
        initial_case["decision"] = "refuse"
        initial_actual = {"results": [initial_case]}
        initial_results = run.grade([self.case], initial_actual)
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }

        def fake_round(
                round_number: int, cases: list[dict[str, Any]], side_specs: dict[str, dict[str, Any]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del round_number, cases, codex_home, confirmation_root, timeout
            self.assertEqual([], side_specs["baseline"]["case_ids"])
            actual = {"results": [initial_case]}
            return {"candidate": ({
                "side": "candidate",
                "round": 2,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 0,
                "transport_validation": {"passed": True},
                "results": run.grade([self.case], actual),
                "usage": {
                    "input_tokens": 0,
                    "cached_input_tokens": 0,
                    "uncached_input_tokens": 0,
                    "output_tokens": 0,
                },
            }, actual)}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "run_confirmation_round", side_effect=fake_round) as run_round:
            actual = run.run_semantic_confirmation(
                [self.case], initial_actual, initial_results, baseline,
                [f"{self.case['id']}:case-contract-changed"], b"candidate-policy",
                "candidate-policy", {}, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual(1, run_round.call_count)
        self.assertEqual([], actual["baseline_case_ids"])
        self.assertEqual([self.case["id"]], actual["changed_contract_failures"])

    def test_semantic_stability_requires_same_case_single_pass_baseline(self) -> None:
        actual = {"results": [self._create_case_result(self.case, "Proceed.")]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": [],
            "evaluations": [],
            "results": [],
        }
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, ["source:changed"])
        self.assertFalse(stability["passed"])
        self.assertEqual([
            "semantic-stability-case-set-changed",
            "semantic-stability-single-pass-baseline-required",
            "source:changed",
        ], stability["blockers"])

    def test_semantic_stability_requires_matching_evaluator(self) -> None:
        actual = {"results": [self._create_case_result(self.case, "Proceed.")]}
        results = run.grade([self.case], actual)
        baseline = {
            "evaluator": {"model": "other-model", "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{"case_count": 1, "runner_exit_code": 0, "failed_case_ids": []}],
            "results": copy.deepcopy(results),
        }
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, [])
        self.assertFalse(stability["passed"])
        self.assertIn("semantic-stability-baseline-evaluator-mismatch", stability["blockers"])

    def test_semantic_surface_digest_excludes_non_semantic_transport_sources(self) -> None:
        bundle = {
            "inventory": [
                {"id": "agents", "semantic": True, "sha256": "policy"},
                {"id": "harness-runner", "semantic": False, "sha256": "runner-v1"},
            ],
            "profiles": {"root": ["agents"]},
        }
        expected = run.semantic_surface_digest(bundle, [self.case], [])
        transport_change = copy.deepcopy(bundle)
        transport_change["inventory"][1]["sha256"] = "runner-v2"
        semantic_change = copy.deepcopy(bundle)
        semantic_change["inventory"][0]["sha256"] = "policy-v2"
        self.assertEqual(expected, run.semantic_surface_digest(transport_change, [self.case], []))
        self.assertNotEqual(expected, run.semantic_surface_digest(semantic_change, [self.case], []))
        with patch.object(run, "HIGH_RISK_ACTIONS", run.HIGH_RISK_ACTIONS.union({"inspect_local"})):
            self.assertNotEqual(expected, run.semantic_surface_digest(bundle, [self.case], []))
        with patch.object(run, "MAX_SEMANTIC_SAMPLES", run.MAX_SEMANTIC_SAMPLES + 2):
            self.assertNotEqual(expected, run.semantic_surface_digest(bundle, [self.case], []))
        with patch.object(run, "EVALUATOR_MODEL", "other-model"):
            self.assertNotEqual(expected, run.semantic_surface_digest(bundle, [self.case], []))

    def test_semantic_core_source_digest_tracks_only_evaluation_core(self) -> None:
        source = Path(run.__file__).read_bytes()
        expected = run.semantic_core_source_digest(source)
        self.assertEqual(expected, run.semantic_core_source_digest(source + b"\n# Non-core comment.\n"))
        changed = source.replace(b'EVALUATOR_MODEL = "gpt-5.6-sol"', b'EVALUATOR_MODEL = "other-model"', 1)
        self.assertNotEqual(expected, run.semantic_core_source_digest(changed))
        with patch.object(run, "SEMANTIC_CORE_FUNCTION_NAMES", tuple(reversed(run.SEMANTIC_CORE_FUNCTION_NAMES))):
            self.assertNotEqual(expected, run.semantic_core_source_digest(source))

    def test_semantic_evaluation_unchanged_requires_core_and_metadata_identity(self) -> None:
        source = Path(run.__file__).read_bytes()
        current = {
            "semantic_core_sha256": run.semantic_core_source_digest(source),
            "policy_sha256": "policy",
            "case_contract_sha256": "contracts",
            "policy_binding_sha256": "bindings",
            "action_catalog_sha256": "actions",
            "reason_catalog_sha256": "reasons",
            "schema_sha256": "schema",
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
        }
        baseline = {key: value for key, value in current.items() if key != "semantic_core_sha256"}
        with patch.object(run, "load_baseline_source", return_value=source):
            self.assertTrue(run.semantic_evaluation_unchanged(baseline, current))
            changed = {**current, "policy_sha256": "changed-policy"}
            self.assertFalse(run.semantic_evaluation_unchanged(baseline, changed))
        recorded_baseline = {**baseline, "semantic_core_sha256": current["semantic_core_sha256"]}
        with patch.object(run, "load_baseline_source", return_value=source):
            self.assertTrue(run.semantic_evaluation_unchanged(recorded_baseline, current))
        recorded_baseline["semantic_core_sha256"] = "tampered"
        with patch.object(run, "load_baseline_source", return_value=source), self.assertRaisesRegex(
                ValueError, "core digest does not match"):
            run.semantic_evaluation_unchanged(recorded_baseline, current)

    def test_load_baseline_source_verifies_snapshot_digest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            snapshot = Path(directory)
            source_path = snapshot / ".codex" / "harness" / "agents" / "run.py"
            source_path.parent.mkdir(parents=True)
            source_path.write_bytes(b"source")
            baseline = {
                "policy_snapshot_dir": str(snapshot),
                "policy_sources": [{
                    "id": "harness-runner",
                    "path": ".codex/harness/agents/run.py",
                    "sha256": hashlib.sha256(b"source").hexdigest(),
                }],
            }
            self.assertEqual(b"source", run.load_baseline_source(baseline, "harness-runner"))
            source_path.write_bytes(b"tampered")
            with self.assertRaisesRegex(ValueError, "digest does not match"):
                run.load_baseline_source(baseline, "harness-runner")

    def test_main_transport_check_reports_semantic_failure_without_failing_transport(self) -> None:
        case = run.load_cases(["read_only_review"])[0]
        passing_result = self._create_case_result(case, "Review FooService without editing it.")
        failing_result = self._create_case_result(case, "Refuse.")
        failing_result["decision"] = "refuse"
        passing_evaluations = [{
            "evaluation": 1,
            "case_count": 1,
            "case_ids": [case["id"]],
            "returned_case_ids": [case["id"]],
            "input_bytes": 1,
            "duration_seconds": 1.0,
            "runner_exit_code": 0,
            "failed_case_ids": [],
            "events_log": "events-evaluation-1.jsonl",
            "stderr_log": "stderr-evaluation-1.log",
        }]
        failing_evaluations = copy.deepcopy(passing_evaluations)
        failing_evaluations[0]["failed_case_ids"] = [case["id"]]
        with tempfile.TemporaryDirectory() as output_parent:
            baseline_dir = Path(output_parent) / "baseline"
            output_dir = Path(output_parent) / "output"
            common_patches = (
                patch.object(run, "read_usage", return_value={
                    "input_tokens": 1,
                    "cached_input_tokens": 0,
                    "uncached_input_tokens": 1,
                    "output_tokens": 1,
                }),
                patch("builtins.print"),
            )
            with patch("sys.argv", [
                    "run.py", "--case", case["id"], "--output-dir", str(baseline_dir)
            ]), patch.object(
                    run, "run_cases", return_value=(
                        0, 1.0, {"results": [passing_result]}, passing_evaluations
                    )
            ), common_patches[0], common_patches[1]:
                self.assertEqual(0, run.main())
            with patch("sys.argv", [
                    "run.py", "--case", case["id"], "--transport-check",
                    "--baseline", str(baseline_dir), "--output-dir", str(output_dir)
            ]), patch.object(
                    run, "run_cases", return_value=(
                        0, 1.0, {"results": [failing_result]}, failing_evaluations
                    )
            ), common_patches[0], common_patches[1]:
                exit_code = run.main()
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
        self.assertEqual(0, exit_code)
        self.assertEqual(1, summary["failed"])
        self.assertTrue(summary["transport_validation"]["passed"])
        self.assertEqual([], summary["transport_blockers"])

    def test_main_semantic_stability_check_accepts_inherited_one_pass_failure(self) -> None:
        cases = run.load_cases(None)
        failing_case = next(each for each in cases if each["id"] == "read_only_review")
        actual_results = [self._create_case_result(each, each["prompt"]) for each in cases]
        failing_result = next(each for each in actual_results if each["case_id"] == failing_case["id"])
        failing_result["decision"] = "refuse"
        case_ids = [each["id"] for each in cases]
        failed_case_ids = [
            each["case_id"] for each in run.grade(cases, {"results": actual_results}) if not each["passed"]
        ]
        evaluations = [{
            "evaluation": 1,
            "case_count": len(cases),
            "case_ids": case_ids,
            "returned_case_ids": case_ids,
            "input_bytes": 1,
            "duration_seconds": 1.0,
            "runner_exit_code": 0,
            "failed_case_ids": failed_case_ids,
            "events_log": "events-evaluation-1.jsonl",
            "stderr_log": "stderr-evaluation-1.log",
        }]
        with tempfile.TemporaryDirectory() as output_parent:
            baseline_dir = Path(output_parent) / "baseline"
            output_dir = Path(output_parent) / "output"
            common_patches = (
                patch.object(run, "read_usage", return_value={
                    "input_tokens": 1,
                    "cached_input_tokens": 0,
                    "uncached_input_tokens": 1,
                    "output_tokens": 1,
                }),
                patch("builtins.print"),
            )
            with patch("sys.argv", [
                    "run.py", "--mode", "all", "--output-dir", str(baseline_dir)
            ]), patch.object(
                    run, "run_cases", return_value=(0, 1.0, {"results": actual_results}, evaluations)
            ), patch.object(
                    run, "run_local_read_traces", return_value=[]
            ), common_patches[0], common_patches[1]:
                self.assertEqual(1, run.main())
            with patch("sys.argv", [
                    "run.py", "--mode", "all", "--semantic-stability-check",
                    "--baseline", str(baseline_dir), "--output-dir", str(output_dir)
            ]), patch.object(
                    run, "run_cases", return_value=(0, 1.0, {"results": actual_results}, evaluations)
            ), patch.object(
                    run, "run_local_read_traces", return_value=[]
            ), common_patches[0], common_patches[1]:
                exit_code = run.main()
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
        self.assertEqual(0, exit_code)
        self.assertTrue(summary["transport_validation"]["passed"])
        self.assertTrue(summary["semantic_stability"]["passed"])

    def test_main_semantic_stability_uses_confirmed_regressions_in_final_result(self) -> None:
        cases = run.load_cases(None)
        disputed_case = next(each for each in cases if each["id"] == "read_only_review")
        baseline_actual_results = [self._create_case_result(each, each["prompt"]) for each in cases]
        candidate_actual_results = copy.deepcopy(baseline_actual_results)
        candidate_case = next(each for each in candidate_actual_results if each["case_id"] == disputed_case["id"])
        candidate_case["decision"] = "refuse"
        candidate_case["summary"] = "Refuse."
        case_ids = [each["id"] for each in cases]

        def create_evaluations(actual_results: list[dict[str, Any]]) -> list[dict[str, Any]]:
            failed_case_ids = [
                each["case_id"] for each in run.grade(cases, {"results": actual_results}) if not each["passed"]
            ]
            return [{
                "evaluation": 1,
                "case_count": len(cases),
                "case_ids": case_ids,
                "returned_case_ids": case_ids,
                "input_bytes": 1,
                "duration_seconds": 1.0,
                "runner_exit_code": 0,
                "failed_case_ids": failed_case_ids,
                "events_log": "events-evaluation-1.jsonl",
                "stderr_log": "stderr-evaluation-1.log",
            }]

        confirmation = {
            "executed": True,
            "safety_regressions": [],
            "blockers": [],
            "confirmed_case_regressions": [],
            "changed_contract_failures": [],
        }
        usage = {
            "input_tokens": 1,
            "cached_input_tokens": 0,
            "uncached_input_tokens": 1,
            "output_tokens": 1,
        }
        with tempfile.TemporaryDirectory() as output_parent:
            baseline_dir = Path(output_parent) / "baseline"
            output_dir = Path(output_parent) / "output"
            with patch("sys.argv", [
                    "run.py", "--mode", "all", "--output-dir", str(baseline_dir)
            ]), patch.object(
                    run, "semantic_surface_digest", return_value="semantic-surface"), patch.object(
                    run, "run_cases", return_value=(
                        0, 1.0, {"results": baseline_actual_results}, create_evaluations(baseline_actual_results)
                    )
            ), patch.object(run, "run_local_read_traces", return_value=[]), patch.object(
                    run, "read_usage", return_value=usage), patch("builtins.print"):
                run.main()
            with patch("sys.argv", [
                    "run.py", "--mode", "all", "--semantic-stability-check",
                    "--baseline", str(baseline_dir), "--output-dir", str(output_dir)
            ]), patch.object(
                    run, "semantic_surface_digest", return_value="semantic-surface"), patch.object(
                    run, "semantic_evaluation_unchanged", return_value=False), patch.object(
                    run, "run_cases", return_value=(
                        0, 1.0, {"results": candidate_actual_results}, create_evaluations(candidate_actual_results)
                    )
            ), patch.object(run, "run_semantic_confirmation", return_value=confirmation) as confirm, patch.object(
                    run, "run_local_read_traces", return_value=[]), patch.object(
                    run, "read_usage", return_value=usage), patch("builtins.print"):
                exit_code = run.main()
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
        self.assertEqual(0, exit_code)
        self.assertEqual(1, confirm.call_count)
        self.assertIn(disputed_case["id"], summary["one_pass_critical_regressions"])
        self.assertEqual([], summary["critical_regressions"])
        self.assertTrue(summary["semantic_stability"]["passed"])

    def test_resolve_policy_impact_selects_bound_semantic_cases_and_profiles(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        self.case["semantic_policy_assertions"] = True
        bundle = {
            "sources": [
                {"id": "agents", "semantic": True},
                {"id": "rules", "semantic": True},
                {"id": "other-rules", "semantic": True},
            ],
            "profiles": {
                "root": ["agents"],
                "code-write": ["agents", "rules"],
                "other": ["agents", "other-rules"],
            },
        }
        bindings = [{
            "case_id": self.case["id"],
            "assertions": [{"source_id": "rules"}],
        }]
        actual = run.resolve_policy_impact([self.case, other_case], bundle, bindings, ["rules"])
        self.assertEqual(["rules"], actual["source_ids"])
        self.assertEqual([self.case["id"]], actual["semantic_case_ids"])
        self.assertEqual(["code-write"], actual["trace_profiles"])
        self.assertEqual([], actual["unproved_semantic_source_ids"])

    def test_resolve_policy_impact_selects_every_case_for_root(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        bundle = {
            "sources": [{"id": "agents", "semantic": True}],
            "profiles": {"root": ["agents"], "code-write": ["agents"]},
        }
        actual = run.resolve_policy_impact([self.case, other_case], bundle, [], ["agents"])
        self.assertEqual([self.case["id"], other_case["id"]], actual["semantic_case_ids"])
        self.assertEqual(["root", "code-write"], actual["trace_profiles"])
        self.assertEqual([], actual["unproved_semantic_source_ids"])

    def test_resolve_policy_impact_reports_unproved_semantic_source(self) -> None:
        bundle = {
            "sources": [{"id": "agents", "semantic": True}, {"id": "rules", "semantic": True}],
            "profiles": {"code-write": ["agents", "rules"]},
        }
        actual = run.resolve_policy_impact([self.case], bundle, [], ["rules"])
        self.assertEqual([], actual["semantic_case_ids"])
        self.assertEqual(["rules"], actual["unproved_semantic_source_ids"])

    def test_resolve_policy_impact_rejects_unknown_or_non_semantic_source(self) -> None:
        bundle = {
            "sources": [{"id": "agents", "semantic": True}, {"id": "runner", "semantic": False}],
            "profiles": {"root": ["agents"]},
        }
        with self.assertRaisesRegex(ValueError, "Unknown impact source IDs: missing"):
            run.resolve_policy_impact([self.case], bundle, [], ["missing"])
        with self.assertRaisesRegex(ValueError, "Impact sources must be semantic policy sources: runner"):
            run.resolve_policy_impact([self.case], bundle, [], ["runner"])

    def test_run_cases_stops_on_runner_failure(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        batches = [[self.case], [other_case]]

        def fake_run_codex(
                policy: bytes, codex_home: Path, output_dir: Path, schema_path: Path,
                prompt: str, timeout: int, evaluation_number: int) -> tuple[int, float, dict[str, Any]]:
            del policy, codex_home, output_dir, schema_path, prompt, timeout
            if evaluation_number == 2:
                return 124, 1.5, {}
            return 0, 1.0, {"results": [self._create_case_result(self.case, "Proceed.")]}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "partition_cases", return_value=batches), patch.object(
                run, "run_codex", side_effect=fake_run_codex) as run_codex:
            exit_code, duration, actual, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), [self.case, other_case], "policy_sha256", 1
            )
        self.assertEqual(124, exit_code)
        self.assertGreaterEqual(duration, 0.0)
        self.assertEqual({}, actual)
        self.assertEqual(2, run_codex.call_count)
        self.assertEqual([0, 124], [each["runner_exit_code"] for each in evaluations])
        self.assertTrue(evaluations[1]["events_log"].endswith("events-evaluation-2.jsonl"))
        self.assertTrue(evaluations[1]["stderr_log"].endswith("stderr-evaluation-2.log"))

    def test_summarize_runner_failure_prioritizes_network_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            output_path = Path(output_directory)
            (output_path / "events.jsonl").write_text("", encoding="utf-8")
            (output_path / "stderr.log").write_text(
                "Stream disconnected before completion: WebSocket protocol error: Connection reset\n"
                "Harness timeout after 600 seconds.\n",
                encoding="utf-8",
            )
            actual = run.summarize_runner_failure(output_path, 124)
        self.assertEqual("network", actual["category"])
        self.assertIn("Stream disconnected", actual["evidence"][0])

    def test_summarize_runner_failure_classifies_sandbox_denial(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            output_path = Path(output_directory)
            (output_path / "stderr.log").write_text("Operation not permitted\n", encoding="utf-8")
            actual = run.summarize_runner_failure(output_path, 1)
        self.assertEqual("sandbox", actual["category"])
        self.assertEqual(["Operation not permitted"], actual["evidence"])

    def test_summarize_runner_failure_keeps_late_marker_in_bounded_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            output_path = Path(output_directory)
            (output_path / "stderr.log").write_text(f"{'context ' * 80}Permission denied\n", encoding="utf-8")
            actual = run.summarize_runner_failure(output_path, 1)
        self.assertEqual("sandbox", actual["category"])
        self.assertIn("Permission denied", actual["evidence"][0])
        self.assertLessEqual(len(actual["evidence"][0]), 240)

    def test_summarize_runner_failure_classifies_timeout(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            actual = run.summarize_runner_failure(Path(output_directory), 124)
        self.assertEqual("timeout", actual["category"])

    def test_summarize_runner_failure_falls_back_to_runner(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            actual = run.summarize_runner_failure(Path(output_directory), 2)
        self.assertEqual("runner", actual["category"])
        self.assertEqual(["Codex runner exited with code 2; inspect the bounded log paths."], actual["evidence"])

    def test_write_runner_failure_summary_persists_diagnostics(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            output_path = Path(output_directory)
            snapshot_path = output_path / "policy-snapshot"
            (output_path / "stderr.log").write_text("Permission denied\n", encoding="utf-8")
            actual = run.write_runner_failure_summary(
                output_path, snapshot_path, "failed-candidate", 1, [{"evaluation": 1, "runner_exit_code": 1}],
                {"source_ids": ["rules"]}
            )
            persisted = json.loads((output_path / "summary.json").read_text(encoding="utf-8"))
        self.assertEqual(actual, persisted)
        self.assertEqual("sandbox", persisted["runner_failure"]["category"])
        self.assertEqual({"source_ids": ["rules"]}, persisted["impact"])
        self.assertEqual(str(snapshot_path), persisted["policy_snapshot_dir"])

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

    def test_combine_evaluation_logs_preserves_evaluation_order(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            output_path = Path(output_directory)
            (output_path / "events-evaluation-1.jsonl").write_text("first event\n", encoding="utf-8")
            (output_path / "events-evaluation-2.jsonl").write_text("second event\n", encoding="utf-8")
            (output_path / "stderr-evaluation-1.log").write_text("first error\n", encoding="utf-8")
            (output_path / "stderr-evaluation-2.log").write_text("second error\n", encoding="utf-8")
            run.combine_evaluation_logs(output_path, 2)
            actual_events = (output_path / "events.jsonl").read_text(encoding="utf-8")
            actual_stderr = (output_path / "stderr.log").read_text(encoding="utf-8")
        self.assertEqual("first event\nsecond event\n", actual_events)
        self.assertEqual("first error\nsecond error\n", actual_stderr)

    def test_normalize_case_contracts_sorts_summary_terms(self) -> None:
        self.case["required_summary_terms"] = ["beta", "alpha"]
        self.case["forbidden_summary_terms"] = ["delta", "gamma"]
        actual = run.normalize_case_contracts([self.case])[0]
        self.assertEqual(["alpha", "beta"], actual["required_summary_terms"])
        self.assertEqual(["delta", "gamma"], actual["forbidden_summary_terms"])

    def test_normalize_case_contracts_sorts_summary_term_groups(self) -> None:
        self.case["required_summary_term_groups"] = [["beta", "alpha"], ["gamma"]]
        self.assertEqual(
            [["alpha", "beta"], ["gamma"]],
            run.normalize_case_contracts([self.case])[0]["required_summary_term_groups"]
        )

    def test_normalize_case_contracts_preserves_explicit_profile(self) -> None:
        self.case["profile"] = "code-write"
        self.assertEqual("code-write", run.normalize_case_contracts([self.case])[0]["profile"])

    def test_normalize_case_contracts_preserves_semantic_policy_assertions(self) -> None:
        self.case["semantic_policy_assertions"] = True
        self.assertTrue(run.normalize_case_contracts([self.case])[0]["semantic_policy_assertions"])

    def test_select_semantic_policy_assertions_requires_binding(self) -> None:
        self.assertEqual({}, run.select_semantic_policy_assertions([self.case], []))
        self.case["semantic_policy_assertions"] = True
        with self.assertRaisesRegex(ValueError, "require a policy binding"):
            run.select_semantic_policy_assertions([self.case], [])
        binding = {
            "case_id": self.case["id"],
            "assertions": [{"source_id": "rules", "source_path": ".codex/rules.md", "statements": ["Keep behavior."]}],
        }
        self.assertEqual(binding["assertions"], run.select_semantic_policy_assertions([self.case], [binding])[self.case["id"]])

    def test_compare_with_baseline_ignores_unselected_contracts(self) -> None:
        baseline_case = run.normalize_case_contracts([self.case])[0]
        other_case = copy.deepcopy(baseline_case)
        other_case["id"] = "other_case"
        baseline = {
            "case_contracts": [baseline_case, other_case],
            "results": [
                {"case_id": self.case["id"], "passed": True},
                {"case_id": other_case["id"], "passed": True},
            ],
        }
        current_result = {"case_id": self.case["id"], "passed": True}
        regressions, changes = run.compare_with_baseline(
            [current_result], baseline, [baseline_case], [self.case["id"]], set()
        )
        self.assertEqual([], regressions)
        self.assertEqual([], changes)

    def test_baseline_comparisons_accept_an_empty_impact_selection(self) -> None:
        baseline_case = run.normalize_case_contracts([self.case])[0]
        binding = {
            "case_id": self.case["id"],
            "critical": True,
            "profile": "root",
            "assertions": [{"source_id": "agents", "source_path": "AGENTS.md", "statements": ["Rule."]}],
        }
        baseline = {
            "case_contracts": [baseline_case],
            "results": [{"case_id": self.case["id"], "passed": True}],
            "policy_bindings": [binding],
        }
        self.assertEqual(([], []), run.compare_with_baseline([], baseline, [], [], set()))
        self.assertEqual(([], []), run.compare_policy_bindings([], baseline, [], set()))

    def test_create_prompt_labels_profile_as_routing_only(self) -> None:
        self.case["profile"] = "code-write"
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("routing profile `code-write`", prompt)
        self.assertIn("root decision", prompt)
        self.assertIn("do not supply profile source contents", prompt)

    def test_create_prompt_does_not_repeat_schema_catalogs(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertNotIn(", ".join(run.ACTIONS), prompt)
        self.assertNotIn(", ".join(run.REASONS), prompt)

    def test_create_prompt_requires_preserving_explicit_terms_verbatim(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("do not replace it with a synonym or abbreviation", prompt)

    def test_create_prompt_supplies_bound_policy_only_to_selected_case(self) -> None:
        statement = "Remove unsupported defensive code before handoff."
        assertions = {
            self.case["id"]: [{
                "source_id": "implementation-rules",
                "source_path": ".codex/rules.md",
                "statements": [statement],
            }],
            "other_case": [{
                "source_id": "other-rules",
                "source_path": ".codex/other.md",
                "statements": ["This statement must not be supplied."],
            }],
        }
        prompt = run.create_prompt([self.case], "sha256", assertions)
        self.assertIn("Apply each bound statement only to its named case", prompt)
        self.assertIn("Source `implementation-rules` at `.codex/rules.md`", prompt)
        self.assertIn(statement, prompt)
        self.assertNotIn("This statement must not be supplied.", prompt)

    def test_create_prompt_maps_root_only_decision_stages(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("completed inspection that rules out performance risk", prompt)
        self.assertIn("the only action is `assess_non_regression`", prompt)
        self.assertIn("include `optional_skill_not_triggered`", prompt)
        self.assertIn("Do not add `inspect_local`, `measure_performance`", prompt)
        self.assertIn("read-only plan applying canonical implementation rules", prompt)
        self.assertIn("include `codex_design_style_required`", prompt)
        self.assertIn("pre-edit test decision about owned production behavior", prompt)
        self.assertIn("`assess_non_regression`, `meaningful_test_required`, and `functional_non_regression_required`", prompt)
        self.assertIn("gen-ut preflight", prompt)
        self.assertIn("both `functional_non_regression_required` and `performance_non_regression_required`", prompt)
        self.assertIn("post-write code or test decision applying repository impact rules", prompt)
        self.assertIn("include `code_policy_required`", prompt)
        self.assertIn("Infer `invoke_source_driven_development` only", prompt)
        self.assertIn("Preserve exact technical terms", prompt)

    def test_create_prompt_distinguishes_local_checks_from_smoke_actions(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("Use `run_local_checks` when the case explicitly asks to run or rerun a focused unit test", prompt)
        self.assertIn("Do not classify a focused unit test, build, or ordinary local verification as a smoke test", prompt)
        self.assertIn("Use `triage_failed_smoke` only when the case explicitly identifies an E2E", prompt)
        self.assertIn("Use `rerun_failed_smoke` only when such a smoke case explicitly asks to rerun it", prompt)

    def test_create_prompt_distinguishes_bounded_from_formal_review(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("explicitly proves every bounded self-review condition", prompt)
        self.assertIn("do not include `run_pre_handoff_review` or `pre_handoff_review_required`", prompt)

    def test_semantic_metadata_disclaims_profile_content_evaluation(self) -> None:
        metadata = run.semantic_evaluation_metadata()
        self.assertTrue(metadata["executed"])
        self.assertEqual("root", metadata["decision_surface"])
        self.assertFalse(metadata["profile_source_contents_included"])
        self.assertFalse(metadata["full_profile_source_contents_included"])
        self.assertFalse(metadata["bound_policy_assertions_configured"])
        self.assertFalse(metadata["bound_policy_assertions_included"])
        self.assertIn("non-root", metadata["does_not_prove"])

    def test_semantic_metadata_records_bound_policy_assertions(self) -> None:
        assertions = {
            self.case["id"]: [{"source_id": "implementation-rules", "source_path": ".codex/rules.md", "statements": ["Keep behavior."]}],
        }
        metadata = run.semantic_evaluation_metadata(assertions)
        self.assertTrue(metadata["executed"])
        self.assertEqual("root-and-bound-assertions", metadata["decision_surface"])
        self.assertTrue(metadata["profile_source_contents_included"])
        self.assertFalse(metadata["full_profile_source_contents_included"])
        self.assertTrue(metadata["bound_policy_assertions_configured"])
        self.assertTrue(metadata["bound_policy_assertions_included"])
        self.assertEqual(["implementation-rules"], metadata["case_policy_sources"][self.case["id"]])

    def test_semantic_metadata_does_not_claim_unexecuted_evidence(self) -> None:
        assertions = {
            self.case["id"]: [{"source_id": "implementation-rules", "source_path": ".codex/rules.md", "statements": ["Keep behavior."]}],
        }
        metadata = run.semantic_evaluation_metadata(assertions, executed=False)
        self.assertFalse(metadata["executed"])
        self.assertIsNone(metadata["evaluated_source"])
        self.assertIsNone(metadata["decision_surface"])
        self.assertTrue(metadata["bound_policy_assertions_configured"])
        self.assertFalse(metadata["bound_policy_assertions_included"])
        self.assertIsNone(metadata["proves"])

    def test_grade_checks_summary_terms_case_insensitively(self) -> None:
        self.case["required_summary_terms"] = ["required term"]
        self.case["forbidden_summary_terms"] = ["internal label"]
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": [],
            "reasons": [],
            "summary": "The REQUIRED TERM replaces the Internal Label.",
            "response_style": "concise",
        }]}
        failures = run.grade([self.case], actual)[0]["failures"]
        self.assertNotIn("summary lacks required terms=['required term']", failures)
        self.assertIn("summary contains forbidden terms=['internal label']", failures)

    def test_grade_reports_missing_summary_terms(self) -> None:
        self.case["required_summary_terms"] = ["required term"]
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": [],
            "reasons": [],
            "summary": "The required content is absent.",
            "response_style": "concise",
        }]}
        failures = run.grade([self.case], actual)[0]["failures"]
        self.assertIn("summary lacks required terms=['required term']", failures)

    def test_grade_accepts_any_term_from_each_required_summary_group(self) -> None:
        self.case["required_summary_term_groups"] = [["keep", "preserve"], ["snapshot copy"]]
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": [],
            "reasons": [],
            "summary": "Preserve the snapshot copy.",
            "response_style": "concise",
        }]}
        failures = run.grade([self.case], actual)[0]["failures"]
        self.assertFalse(any("summary lacks a term from groups" in each for each in failures), failures)

    def test_grade_reports_missing_required_summary_term_group(self) -> None:
        self.case["required_summary_term_groups"] = [["keep", "preserve"]]
        actual = {"results": [{
            "case_id": self.case["id"],
            "decision": "proceed",
            "actions": [],
            "reasons": [],
            "summary": "Remove the snapshot copy.",
            "response_style": "concise",
        }]}
        failures = run.grade([self.case], actual)[0]["failures"]
        self.assertIn("summary lacks a term from groups=[['keep', 'preserve']]", failures)

    def test_grade_rejects_authorization_contract_violations(self) -> None:
        summaries = {
            "foreseeable_authorizations_are_consolidated": (
                "Please authorize editing docs/release.md, running git tag v2.0, and publishing GitHub release v2.0. "
                "The deployment remains conditional and is not being requested, but also authorize deployment now."
            ),
            "new_evidence_requests_authorization_delta_only": (
                "The descriptor was unavailable during preflight. Please authorize module-b/src/Loader.java to register "
                "FooService, and renew permission for module-a/src/FooService.java plus the existing upload. Declining leaves "
                "behavior incomplete; preserve the original baseline."
            ),
            "platform_approval_does_not_repeat_task_authority": (
                "Platform approval for resolver-status.properties does not expand task authority, which is already complete; "
                "please reconfirm permission to edit AGENTS.md and the cases file."
            ),
            "frozen_ledger_survives_failure_and_review": (
                "Capture a fresh baseline, then authorize module-b/src/Loader.java to register BarService; FooService remains "
                "untouched."
            ),
        }
        cases = run.load_cases(list(summaries))
        actual = {"results": [self._create_case_result(each, summaries[each["id"]]) for each in cases]}
        results = run.grade(cases, actual)
        self.assertTrue(all(not each["passed"] for each in results), results)
        results_by_id = {each["case_id"]: each for each in results}
        for each in (
                "foreseeable_authorizations_are_consolidated",
                "new_evidence_requests_authorization_delta_only",
                "platform_approval_does_not_repeat_task_authority"):
            with self.subTest(case_id=each):
                failures = results_by_id[each]["failures"]
                self.assertFalse(any(failure.startswith("summary lacks required terms=") for failure in failures), failures)
                self.assertTrue(any(failure.startswith("summary contains forbidden terms=") for failure in failures), failures)

    def test_grade_accepts_authorization_contract_examples(self) -> None:
        summaries = {
            "foreseeable_authorizations_are_consolidated": (
                "Please authorize editing docs/release.md to document release 2.0, running git tag v2.0, and publishing "
                "GitHub release v2.0. The deployment remains conditional and is not being requested."
            ),
            "new_evidence_requests_authorization_delta_only": (
                "The descriptor was unavailable during preflight. Please authorize module-b/src/Loader.java to register "
                "FooService. Declining leaves registration incomplete; preserve the original baseline."
            ),
            "platform_approval_does_not_repeat_task_authority": (
                "Platform approval for resolver-status.properties does not expand task authority, which is already complete."
            ),
            "frozen_ledger_survives_failure_and_review": (
                "Preserve the original baseline and authorize module-b/src/Loader.java to register FooService."
            ),
        }
        cases = run.load_cases(list(summaries))
        actual = {"results": [self._create_case_result(each, summaries[each["id"]]) for each in cases]}
        results = run.grade(cases, actual)
        self.assertTrue(all(each["passed"] for each in results), results)


class PolicyBundleTest(unittest.TestCase):
    """Test deterministic policy-source loading and staging."""

    def create_bundle(self, root: Path, *, root_max_bytes: int = 26000) -> Path:
        """Create one minimal valid manifest fixture."""
        (root / "AGENTS.md").write_text("Read `.codex/rules.md`.\n", encoding="utf-8")
        rules_path = root / ".codex" / "rules.md"
        rules_path.parent.mkdir(parents=True)
        rules_path.write_text("Keep behavior.\n", encoding="utf-8")
        manifest_path = root / ".codex" / "policy-sources.toml"
        manifest_path.write_text(
            f'''version = 1
root_max_bytes = {root_max_bytes}

[profiles]
root = ["agents"]
code-write = ["agents", "rules"]

[[sources]]
id = "agents"
path = "AGENTS.md"
kind = "instructions"
semantic = true
references = [".codex/rules.md"]

[[sources]]
id = "rules"
path = ".codex/rules.md"
kind = "rules"
semantic = true
references = []
''',
            encoding="utf-8",
        )
        return manifest_path

    def create_bound_case(self, statement: str) -> dict[str, Any]:
        """Create one minimal case with a deterministic policy-source binding."""
        return {
            "id": "bound_case",
            "critical": True,
            "policy_binding_profile": "code-write",
            "policy_assertions": [{"source": "rules", "statements": [statement]}],
        }

    def test_load_policy_manifest_records_sources_and_hashes(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            bundle = run.load_policy_manifest(root, manifest_path)
        self.assertEqual(["agents", "rules"], [each["id"] for each in bundle["inventory"]])
        self.assertEqual(["agents", "rules"], bundle["profiles"]["code-write"])
        self.assertEqual(64, len(bundle["manifest_sha256"]))
        self.assertEqual(64, len(bundle["bundle_sha256"]))
        self.assertEqual(run.digest_json(bundle["inventory"]), bundle["inventory_sha256"])

    def test_load_policy_manifest_rejects_non_exact_paths(self) -> None:
        for invalid_path in ("/AGENTS.md", "../AGENTS.md", "docs/*.md", "docs//rules.md"):
            with self.subTest(path=invalid_path), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                manifest_path = self.create_bundle(root)
                text = manifest_path.read_text(encoding="utf-8").replace(".codex/rules.md", invalid_path)
                manifest_path.write_text(text, encoding="utf-8")
                with self.assertRaisesRegex(ValueError, "exact|normalized|relative"):
                    run.load_policy_manifest(root, manifest_path)
        with self.assertRaisesRegex(ValueError, "POSIX separators"):
            run.validate_policy_path("docs\\rules.md")

    def test_load_policy_manifest_rejects_duplicate_ids_and_paths(self) -> None:
        for field, replacement, message in (
            ('id = "rules"', 'id = "agents"', "Duplicate policy source IDs"),
            ('path = ".codex/rules.md"', 'path = "AGENTS.md"', "Duplicate policy source paths"),
        ):
            with self.subTest(field=field), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                manifest_path = self.create_bundle(root)
                manifest_path.write_text(
                    manifest_path.read_text(encoding="utf-8").replace(field, replacement), encoding="utf-8"
                )
                with self.assertRaisesRegex(ValueError, message):
                    run.load_policy_manifest(root, manifest_path)

    def test_load_policy_manifest_rejects_non_string_profile_source(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            manifest_path.write_text(
                manifest_path.read_text(encoding="utf-8").replace(
                    'code-write = ["agents", "rules"]', 'code-write = ["agents", 1]'
                ),
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "source IDs must be non-empty strings"):
                run.load_policy_manifest(root, manifest_path)

    def test_load_policy_manifest_rejects_unprofiled_semantic_source(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            orphan_path = root / ".codex" / "orphan.md"
            orphan_path.write_text("Orphan rule.\n", encoding="utf-8")
            manifest_path.write_text(
                manifest_path.read_text(encoding="utf-8")
                + '''
[[sources]]
id = "orphan"
path = ".codex/orphan.md"
kind = "rules"
semantic = true
references = []
''',
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "must belong to a routing profile"):
                run.load_policy_manifest(root, manifest_path)

    def test_load_policy_manifest_rejects_undeclared_manifest_path_in_source(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            manifest_path.write_text(
                manifest_path.read_text(encoding="utf-8").replace(
                    'references = [".codex/rules.md"]', "references = []", 1
                ),
                encoding="utf-8",
            )
            (root / "AGENTS.md").write_text("Read `.codex/rules.md` before continuing.\n", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "undeclared manifest paths"):
                run.load_policy_manifest(root, manifest_path)

    def test_resolve_local_markdown_references_normalizes_parent_segments(self) -> None:
        references = run.resolve_local_markdown_references(
            ".codex/skills/gen-ut/SKILL.md",
            "Read [the base Skill](../code-implementation/SKILL.md) and [the web guide](https://example.com/guide).",
        )
        self.assertEqual({".codex/skills/code-implementation/SKILL.md"}, references)

    def test_repository_manifest_profiles_cover_routed_sources(self) -> None:
        repo_root = Path(__file__).resolve().parents[3]
        bundle = run.load_policy_manifest(repo_root)
        expected = {
            "code-write": {"code-of-conduct", "code-implementation", "implementation-rules", "non-regression"},
            "test-write": {"code-of-conduct", "code-implementation", "implementation-rules", "testing-rules", "contracts-and-removal"},
            "code-review": {"contracts-and-removal", "code-verification"},
            "code-design": {"implementation-rules", "testing-rules", "contracts-and-removal", "cross-cutting-skills"},
            "runtime-diagnosis": {"runtime-triage", "code-verification"},
            "command-execution": {"token-efficiency", "code-verification"},
            "gen-ut": {"code-of-conduct", "code-implementation", "implementation-rules", "contracts-and-removal", "gen-ut"},
            "review-pr": {"code-of-conduct", "implementation-rules", "code-verification", "review-pr"},
            "policy-maintenance": {"policy-maintenance", "policy-source-manifest"},
        }
        for profile, required_sources in expected.items():
            with self.subTest(profile=profile):
                self.assertTrue(required_sources.issubset(bundle["profiles"][profile]))

    def test_case_policy_binding_requires_full_statement_in_source(self) -> None:
        statement = "Preserve every supported behavior before handing off the completed task."
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            (root / ".codex" / "rules.md").write_text(f"{statement}\n", encoding="utf-8")
            bundle = run.load_policy_manifest(root, manifest_path)
            binding = run.normalize_case_policy_bindings([self.create_bound_case(statement)], bundle)[0]
            self.assertEqual(".codex/rules.md", binding["assertions"][0]["source_path"])
            with self.assertRaisesRegex(ValueError, "absent from its source"):
                run.normalize_case_policy_bindings(
                    [self.create_bound_case("Preserve every unsupported behavior before handing off the completed task.")],
                    bundle,
                )

    def test_case_policy_binding_rejects_source_outside_route(self) -> None:
        statement = "Preserve every supported behavior before handing off the completed task."
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            (root / ".codex" / "rules.md").write_text(f"{statement}\n", encoding="utf-8")
            bundle = run.load_policy_manifest(root, manifest_path)
            case = self.create_bound_case(statement)
            case["policy_binding_profile"] = "root"
            with self.assertRaisesRegex(ValueError, "absent from its route"):
                run.normalize_case_policy_bindings([case], bundle)

    def test_compare_policy_bindings_detects_removal(self) -> None:
        binding = {
            "case_id": "bound_case",
            "critical": True,
            "profile": "code-write",
            "assertions": [{
                "source_id": "rules",
                "source_path": ".codex/rules.md",
                "statements": ["Preserve every supported behavior before handing off the completed task."],
            }],
        }
        baseline = {"policy_bindings": [binding]}
        regressions, changes = run.compare_policy_bindings([], baseline, None, set())
        self.assertEqual(["bound_case:policy-binding-removed"], regressions)
        self.assertEqual(regressions, changes)

    def test_compare_policy_bindings_detects_and_authorizes_exact_source_change(self) -> None:
        binding = {
            "case_id": "bound_case",
            "critical": True,
            "profile": "code-write",
            "assertions": [{
                "source_id": "rules",
                "source_path": ".codex/rules.md",
                "statements": ["Preserve every supported behavior before handing off the completed task."],
            }],
        }
        changed = json.loads(json.dumps(binding))
        changed["assertions"][0]["source_id"] = "replacement-rules"
        baseline = {"policy_bindings": [binding]}
        regressions, changes = run.compare_policy_bindings([changed], baseline, None, set())
        self.assertEqual(["bound_case:policy-binding-changed"], regressions)
        self.assertEqual(regressions, changes)
        regressions, changes = run.compare_policy_bindings([changed], baseline, None, {"bound_case"})
        self.assertEqual([], regressions)
        self.assertEqual(["bound_case:policy-binding-changed"], changes)

    def test_load_baseline_rejects_tampered_policy_bindings(self) -> None:
        contract = [{"id": "case", "critical": True}]
        binding = [{"case_id": "case", "critical": True, "profile": "root", "assertions": []}]
        baseline = {
            "case_contracts": contract,
            "case_contract_sha256": run.digest_json(contract),
            "results": [{"case_id": "case", "passed": True}],
            "policy_bindings": binding,
            "policy_binding_sha256": "tampered",
        }
        with tempfile.TemporaryDirectory() as directory:
            summary_path = Path(directory) / "summary.json"
            summary_path.write_text(json.dumps(baseline), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "binding digest"):
                run.load_baseline(summary_path)

    def test_load_deterministic_baseline_without_semantic_results(self) -> None:
        inventory = [{
            "id": "agents", "path": "AGENTS.md", "kind": "instructions", "semantic": True,
            "bytes": 10, "sha256": "a" * 64,
        }]
        bindings = [{
            "case_id": "case", "critical": True, "profile": "root",
            "assertions": [{
                "source_id": "agents", "source_path": "AGENTS.md",
                "statements": ["Preserve supported behavior before handing off the completed task."],
            }],
        }]
        baseline = {
            "policy_sources": inventory,
            "policy_inventory_sha256": run.digest_json(inventory),
            "policy_bindings": bindings,
            "policy_binding_sha256": run.digest_json(bindings),
        }
        with tempfile.TemporaryDirectory() as directory:
            summary_path = Path(directory) / "summary.json"
            summary_path.write_text(json.dumps(baseline), encoding="utf-8")
            self.assertEqual(baseline, run.load_baseline(summary_path, require_semantic_results=False))
            with self.assertRaisesRegex(ValueError, "case contracts"):
                run.load_baseline(summary_path)

    def test_load_deterministic_baseline_rejects_malformed_inventory_and_bindings(self) -> None:
        variants = (
            {
                "policy_sources": [{"id": "agents", "path": "AGENTS.md"}],
                "policy_inventory_sha256": run.digest_json([{"id": "agents", "path": "AGENTS.md"}]),
            },
            {
                "policy_bindings": [{
                    "case_id": "case", "critical": True, "profile": "root", "assertions": [],
                }],
                "policy_binding_sha256": run.digest_json([{
                    "case_id": "case", "critical": True, "profile": "root", "assertions": [],
                }]),
            },
            {
                "policy_bindings": [{
                    "case_id": "case", "critical": True, "profile": "root",
                    "assertions": [
                        {
                            "source_id": "agents", "source_path": "AGENTS.md",
                            "statements": ["Preserve supported behavior before handoff."],
                        },
                        {
                            "source_id": "agents", "source_path": "AGENTS.md",
                            "statements": ["Preserve supported performance before handoff."],
                        },
                    ],
                }],
                "policy_binding_sha256": run.digest_json([{
                    "case_id": "case", "critical": True, "profile": "root",
                    "assertions": [
                        {
                            "source_id": "agents", "source_path": "AGENTS.md",
                            "statements": ["Preserve supported behavior before handoff."],
                        },
                        {
                            "source_id": "agents", "source_path": "AGENTS.md",
                            "statements": ["Preserve supported performance before handoff."],
                        },
                    ],
                }]),
            },
        )
        for baseline in variants:
            with self.subTest(baseline=baseline), tempfile.TemporaryDirectory() as directory:
                summary_path = Path(directory) / "summary.json"
                summary_path.write_text(json.dumps(baseline), encoding="utf-8")
                with self.assertRaisesRegex(ValueError, "invalid structure|must contain assertions|sources must be unique"):
                    run.load_baseline(summary_path, require_semantic_results=False)

    def test_load_policy_manifest_rejects_missing_directory_symlink_and_non_utf8(self) -> None:
        variants = ("missing", "directory", "symlink", "non_utf8")
        for variant in variants:
            with self.subTest(variant=variant), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                manifest_path = self.create_bundle(root)
                rules_path = root / ".codex" / "rules.md"
                if variant == "missing":
                    rules_path.unlink()
                elif variant == "directory":
                    rules_path.unlink()
                    rules_path.mkdir()
                elif variant == "symlink":
                    rules_path.unlink()
                    rules_path.symlink_to(root / "AGENTS.md")
                else:
                    rules_path.write_bytes(b"\xff")
                with self.assertRaises(ValueError):
                    run.load_policy_manifest(root, manifest_path)

    def test_load_policy_manifest_enforces_root_default_limit(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root, root_max_bytes=5)
            with self.assertRaisesRegex(ValueError, "AGENTS.md is"):
                run.load_policy_manifest(root, manifest_path)
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root, root_max_bytes=run.DEFAULT_PROJECT_DOC_MAX_BYTES + 1)
            with self.assertRaisesRegex(ValueError, "no greater"):
                run.load_policy_manifest(root, manifest_path)

    def test_load_policy_manifest_rejects_broken_reference_graph(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path = self.create_bundle(root)
            manifest_path.write_text(
                manifest_path.read_text(encoding="utf-8").replace(
                    'references = [".codex/rules.md"]', 'references = [".codex/missing.md"]'
                ),
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "absent from the manifest"):
                run.load_policy_manifest(root, manifest_path)

    def test_stage_policy_sources_preserves_paths_and_adds_eof_nonce(self) -> None:
        with tempfile.TemporaryDirectory() as source_directory, tempfile.TemporaryDirectory() as stage_directory:
            root = Path(source_directory)
            bundle = run.load_policy_manifest(root, self.create_bundle(root))
            sources = run.get_profile_sources(bundle, "code-write")
            nonces = {each["path"]: f"nonce-{each['id']}" for each in sources}
            run.stage_policy_sources(Path(stage_directory), sources, nonces)
            for source in sources:
                staged = Path(stage_directory) / source["path"]
                self.assertTrue(staged.is_file())
                self.assertTrue(staged.read_text(encoding="utf-8").endswith(
                    f"<!-- policy-harness-eof:{nonces[source['path']]} -->\n"
                ))

    def test_run_local_read_traces_covers_root_nested_and_eof(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            bundle = run.load_policy_manifest(root, self.create_bundle(root))
            traces = run.run_local_read_traces(bundle, ["code-write"], ["root", "nested"])
        self.assertEqual(4, len(traces))
        self.assertEqual({"root", "nested"}, {each["cwd"] for each in traces})
        self.assertEqual({"AGENTS.md", ".codex/rules.md"}, {each["path"] for each in traces})
        self.assertTrue(all(each["passed"] and each["bytes_read"] > 0 for each in traces))
        metadata = run.routing_trace_metadata(traces)
        self.assertTrue(metadata["executed"])
        self.assertTrue(metadata["all_passed"])
        self.assertIn("semantic", metadata["does_not_prove"])

    def test_write_policy_snapshot_preserves_validated_sources(self) -> None:
        with tempfile.TemporaryDirectory() as source_directory, tempfile.TemporaryDirectory() as output_directory:
            root = Path(source_directory)
            bundle = run.load_policy_manifest(root, self.create_bundle(root))
            snapshot = run.write_policy_snapshot(Path(output_directory), bundle)
            self.assertEqual((root / "AGENTS.md").read_bytes(), (snapshot / "AGENTS.md").read_bytes())
            metadata = json.loads((snapshot / "snapshot.json").read_text(encoding="utf-8"))
        self.assertEqual(bundle["bundle_sha256"], metadata["policy_bundle_sha256"])
        self.assertEqual(bundle["inventory_sha256"], metadata["policy_inventory_sha256"])

    def test_compare_policy_inventory_detects_common_mode_deletion(self) -> None:
        baseline = {"policy_sources": [
            {"id": "agents", "path": "AGENTS.md"},
            {"id": "rules", "path": ".codex/rules.md"},
        ]}
        bundle = {"inventory": [{"id": "agents", "path": "AGENTS.md"}]}
        regressions, changes = run.compare_policy_inventory(bundle, baseline, set())
        self.assertEqual(["rules:policy-source-removed"], regressions)
        self.assertEqual(regressions, changes)

    def test_compare_policy_inventory_detects_path_change(self) -> None:
        baseline = {"policy_sources": [{"id": "rules", "path": ".codex/rules.md"}]}
        bundle = {"inventory": [{"id": "rules", "path": ".codex/moved.md"}]}
        regressions, changes = run.compare_policy_inventory(bundle, baseline, set())
        self.assertEqual(["rules:policy-source-path-changed"], regressions)
        self.assertEqual(regressions, changes)

    def test_compare_policy_inventory_detects_and_authorizes_content_change(self) -> None:
        baseline_source = {
            "id": "rules", "path": ".codex/rules.md", "kind": "rules", "semantic": True,
            "bytes": 10, "sha256": "a" * 64,
        }
        current_source = {**baseline_source, "bytes": 11, "sha256": "b" * 64}
        baseline = {"policy_sources": [baseline_source]}
        bundle = {"inventory": [current_source]}
        regressions, changes = run.compare_policy_inventory(bundle, baseline, set())
        self.assertEqual(["rules:policy-source-changed"], regressions)
        self.assertEqual(regressions, changes)
        regressions, changes = run.compare_policy_inventory(bundle, baseline, {"rules"})
        self.assertEqual([], regressions)
        self.assertEqual(["rules:policy-source-changed"], changes)

    def test_load_baseline_rejects_tampered_policy_inventory(self) -> None:
        contract = run.normalize_case_contracts([{
            "id": "case",
            "group": "test",
            "description": "Test.",
            "phase": "test",
            "ordinary_loop": False,
            "prompt": "Proceed.",
            "decision": "proceed",
            "required_actions": [],
            "allowed_actions": [],
            "forbidden_actions": [],
            "required_reasons": [],
            "critical": True,
        }])
        baseline = {
            "case_contracts": contract,
            "case_contract_sha256": run.digest_json(contract),
            "results": [{"case_id": "case", "passed": True}],
            "policy_sources": [{"id": "agents", "path": "AGENTS.md"}],
            "policy_inventory_sha256": "tampered",
        }
        with tempfile.TemporaryDirectory() as directory:
            summary_path = Path(directory) / "summary.json"
            summary_path.write_text(json.dumps(baseline), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "inventory digest"):
                run.load_baseline(summary_path)


if __name__ == "__main__":
    unittest.main()
