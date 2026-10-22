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
from types import ModuleType
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

    def test_bare_invocation_defaults_to_validate_without_semantic_execution(self) -> None:
        with tempfile.TemporaryDirectory() as directory, patch(
                "sys.argv", ["run.py", "--output-dir", str(Path(directory) / "output")]
        ), patch.object(run, "run_cases") as run_cases, patch("builtins.print"):
            self.assertEqual(0, run.main())
        run_cases.assert_not_called()

    def test_main_summary_contract_for_each_mode(self) -> None:
        case = run.load_cases(["read_only_review"])[0]
        bundle = run.load_policy_manifest(Path(run.__file__).resolve().parents[3])
        policy_keys = [
            "policy_manifest", "policy_manifest_sha256", "policy_bundle_sha256", "policy_inventory_sha256",
            "policy_sources", "policy_source_changes", "authorized_policy_source_changes",
            "policy_source_baseline_available", "policy_binding_sha256", "policy_bindings", "policy_binding_changes",
            "authorized_policy_binding_changes", "policy_binding_baseline_available", "profiles", "profile_case_counts",
            "root_max_bytes", "default_project_doc_max_bytes", "semantic_evaluation", "routing_trace", "impact",
        ]
        actual = {"results": [self._create_case_result(case, "Review FooService without editing it.")]}
        evaluations = [self._create_evaluation([case["id"]], [])]
        trace_results = [{"passed": True}]
        for mode in ("validate", "trace", "semantic", "all"):
            with self.subTest(mode=mode), tempfile.TemporaryDirectory() as directory:
                output_dir = Path(directory) / "output"
                with patch("sys.argv", [
                        "run.py", "--mode", mode, "--case", case["id"], "--profile", "root",
                        "--output-dir", str(output_dir)
                ]), patch.object(run, "resolve_codex_home", return_value=Path(directory)), patch.object(
                        run, "run_cases", return_value=(0, 1.0, actual, evaluations)
                ) as run_cases, patch.object(
                        run, "semantic_surface_digest", return_value="semantic-surface"
                ), patch.object(
                        run, "run_local_read_traces", return_value=trace_results
                ), patch.object(
                        run, "compare_policy_inventory", return_value=(["inventory-regression"], [])
                ), patch.object(
                        run, "compare_with_baseline", return_value=(["semantic-regression"], [])
                ), patch.object(
                        run, "read_usage", return_value={
                            "input_tokens": 1, "cached_input_tokens": 0, "uncached_input_tokens": 1, "output_tokens": 1,
                        }
                ), patch("builtins.print") as print_summary:
                    self.assertEqual(1, run.main())
                summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
                self.assertEqual(summary, json.loads(print_summary.call_args.args[0]))
                deterministic = mode in {"validate", "trace"}
                policy_start = 2 if deterministic else 4
                self.assertEqual(policy_keys, list(summary)[policy_start:policy_start + len(policy_keys)])
                self.assertEqual(["label", "mode"], list(summary)[:2])
                self.assertEqual(mode, summary["mode"])
                self.assertEqual(bundle["manifest_file"], summary["policy_manifest"])
                self.assertEqual(bundle["manifest_sha256"], summary["policy_manifest_sha256"])
                self.assertEqual(bundle["bundle_sha256"], summary["policy_bundle_sha256"])
                self.assertEqual(bundle["inventory_sha256"], summary["policy_inventory_sha256"])
                self.assertEqual(bundle["inventory"], summary["policy_sources"])
                self.assertEqual([], summary["policy_source_changes"])
                self.assertEqual([], summary["authorized_policy_source_changes"])
                self.assertFalse(summary["policy_source_baseline_available"])
                self.assertFalse(summary["policy_binding_baseline_available"])
                self.assertEqual(bundle["root_max_bytes"], summary["root_max_bytes"])
                self.assertEqual(run.DEFAULT_PROJECT_DOC_MAX_BYTES, summary["default_project_doc_max_bytes"])
                self.assertIsNone(summary["impact"])
                expected_profiles = {"root": bundle["profiles"]["root"]} if deterministic else bundle["profiles"]
                self.assertEqual(expected_profiles, summary["profiles"])
                self.assertEqual(not deterministic, summary["semantic_evaluation"]["executed"])
                expected_traces = trace_results if mode in {"trace", "all"} else []
                self.assertEqual(expected_traces, summary["trace_results"])
                self.assertEqual(bool(expected_traces), summary["routing_trace"]["executed"])
                expected_regressions = ["inventory-regression"] if deterministic else ["inventory-regression", "semantic-regression"]
                self.assertEqual(expected_regressions, summary["critical_regressions"])
                if deterministic:
                    run_cases.assert_not_called()
                    self.assertEqual([
                        "trace_results", "critical_regressions", "output_dir", "policy_snapshot_dir"
                    ], list(summary)[policy_start + len(policy_keys):])
                else:
                    run_cases.assert_called_once()
                    self.assertEqual(["policy_file", "policy_sha256"], list(summary)[2:4])
                    self.assertEqual("AGENTS.md", summary["policy_file"])
                    self.assertEqual(hashlib.sha256(Path(run.__file__).read_bytes()).hexdigest(), summary["runner_sha256"])

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

    def _create_passing_baseline(self) -> dict[str, Any]:
        return {
            "evaluator": {"model": run.EVALUATOR_MODEL, "reasoning_effort": run.EVALUATOR_REASONING_EFFORT},
            "case_contracts": run.normalize_case_contracts([self.case]),
            "evaluations": [{"case_count": 1, "runner_exit_code": 0, "failed_case_ids": []}],
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }

    @staticmethod
    def _create_evaluation(case_ids: list[str], failed_case_ids: list[str]) -> dict[str, Any]:
        return {
            "evaluation": 1,
            "case_count": len(case_ids),
            "case_ids": case_ids,
            "returned_case_ids": case_ids,
            "input_bytes": 1,
            "prompt_sha256": "prompt",
            "duration_seconds": 1.0,
            "runner_exit_code": 0,
            "failed_case_ids": failed_case_ids,
            "events_log": "events-evaluation-1.jsonl",
            "stderr_log": "stderr-evaluation-1.log",
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
        self.assertTrue(actual)
        self.assertTrue(all(actual))
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
        submitted_prompts = {}

        def fake_run_codex(
                policy: bytes, codex_home: Path, output_dir: Path, schema_path: Path,
                prompt: str, timeout: int, evaluation_number: int) -> tuple[int, float, dict[str, Any]]:
            del policy, codex_home, output_dir, timeout
            submitted_prompts[evaluation_number] = prompt
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
        self.assertEqual(
            [hashlib.sha256(submitted_prompts[index].encode("utf-8")).hexdigest() for index in (1, 2)],
            [each["prompt_sha256"] for each in evaluations],
        )

    def test_serial_initial_evaluation_cli_flag(self) -> None:
        for arguments, expected in ((["run.py"], False), (["run.py", "--serial-initial-evaluation"], True)):
            with self.subTest(arguments=arguments), patch("sys.argv", arguments):
                self.assertEqual(expected, run.parse_args().serial_initial_evaluation)

    def test_claim_confirmation_recovery_only_once(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            summary = root / "summary.json"
            summary.write_text("{}", encoding="utf-8")
            recovery = {
                "summary": {"output_dir": str(root)},
                "provenance": {
                    "summary_path": str(summary.resolve()),
                    "preserved_artifacts_sha256": {str(summary.resolve()): hashlib.sha256(summary.read_bytes()).hexdigest()},
                },
            }
            run.claim_confirmation_recovery(recovery, root / "resumed")
            claim_path = root / "confirmation-recovery.json"
            original_claim = claim_path.read_bytes()
            claim = json.loads(original_claim)
            self.assertEqual(str((root / "resumed").resolve()), claim["output_dir"])
            self.assertEqual(recovery["provenance"]["preserved_artifacts_sha256"][str(summary.resolve())], claim["summary_sha256"])
            with self.assertRaises(FileExistsError):
                run.claim_confirmation_recovery(recovery, root / "duplicate")
            self.assertEqual(original_claim, claim_path.read_bytes())
            self.assertEqual("{}", summary.read_text(encoding="utf-8"))

    def test_claim_confirmation_recovery_rejects_changed_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            summary = root / "summary.json"
            summary.write_text("changed", encoding="utf-8")
            recovery = {
                "summary": {"output_dir": str(root)},
                "provenance": {
                    "summary_path": str(summary.resolve()),
                    "preserved_artifacts_sha256": {str(summary.resolve()): hashlib.sha256(b"original").hexdigest()},
                },
            }
            with self.assertRaisesRegex(ValueError, "evidence changed"):
                run.claim_confirmation_recovery(recovery, root / "resumed")
            self.assertFalse((root / "confirmation-recovery.json").exists())

    def test_run_cases_serial_initial_evaluation(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        cases = [self.case, other_case]
        lock = threading.Lock()
        active = 0
        maximum_active = 0

        def fake_run_codex(
                policy: bytes, codex_home: Path, output_dir: Path, schema_path: Path,
                prompt: str, timeout: int, evaluation_number: int) -> tuple[int, float, dict[str, Any]]:
            nonlocal active, maximum_active
            del policy, codex_home, output_dir, schema_path, prompt, timeout
            with lock:
                active += 1
                maximum_active = max(maximum_active, active)
            threading.Event().wait(0.02)
            with lock:
                active -= 1
            return 0, float(evaluation_number), {"results": [self._create_case_result(cases[evaluation_number - 1], "Proceed.")]}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "partition_cases", return_value=[[self.case], [other_case]]), patch.object(
                run, "run_codex", side_effect=fake_run_codex):
            exit_code, _, actual, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), cases, "policy_sha256", 1, serial=True
            )
        self.assertEqual(0, exit_code)
        self.assertEqual(1, maximum_active)
        self.assertEqual([each["id"] for each in cases], [each["case_id"] for each in actual["results"]])
        self.assertEqual([1, 1], [each["parallel_limit"] for each in evaluations])
        self.assertEqual([[self.case["id"]], [other_case["id"]]], [each["returned_case_ids"] for each in evaluations])

    def test_run_cases_serial_initial_evaluation_preserves_runner_failure(self) -> None:
        other_case = copy.deepcopy(self.case)
        other_case["id"] = "other_case"
        actual = {"results": [self._create_case_result(other_case, "Proceed.")]}
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "partition_cases", return_value=[[self.case], [other_case]]), patch.object(
                run, "run_codex", side_effect=[(7, 1.0, {}), (0, 2.0, actual)]) as run_codex:
            exit_code, _, result, evaluations = run.run_cases(
                b"policy", Path(output_directory), Path(output_directory), [self.case, other_case], "policy_sha256", 1, serial=True
            )
        self.assertEqual(7, exit_code)
        self.assertEqual({}, result)
        self.assertEqual(2, run_codex.call_count)
        self.assertEqual([7, 0], [each["runner_exit_code"] for each in evaluations])
        self.assertEqual([[], [other_case["id"]]], [each["returned_case_ids"] for each in evaluations])

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
        baseline = self._create_passing_baseline()
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, [])
        self.assertFalse(stability["passed"])
        self.assertEqual(["semantic-stability-safety-regression"], stability["blockers"])

    def test_semantic_stability_requires_confirmation_instead_of_aggregate_floor(self) -> None:
        actual_case = self._create_case_result(self.case, "Refuse.")
        actual_case["decision"] = "refuse"
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = self._create_passing_baseline()
        stability = run.summarize_semantic_stability([self.case], actual, results, baseline, [])
        self.assertFalse(stability["aggregate_non_regression"])
        self.assertTrue(stability["aggregate_non_regression_is_diagnostic"])
        self.assertEqual(["semantic-stability-confirmation-required"], stability["blockers"])

    def test_semantic_stability_accepts_confirmed_case_without_aggregate_offset(self) -> None:
        actual_case = self._create_case_result(self.case, "Refuse.")
        actual_case["decision"] = "refuse"
        actual = {"results": [actual_case]}
        results = run.grade([self.case], actual)
        baseline = self._create_passing_baseline()
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
        baseline = self._create_passing_baseline()
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
            "case_contracts": run.normalize_case_contracts([self.case]),
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }
        baseline_evaluator = ModuleType("baseline")
        baseline_evaluator.select_semantic_policy_assertions = Mock(return_value={"baseline": []})
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
                round_number: int, side_specs: dict[str, dict[str, Any]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del codex_home, confirmation_root, timeout
            self.assertIs(baseline_evaluator, side_specs["baseline"]["evaluator"])
            self.assertEqual(baseline["case_contracts"], side_specs["baseline"]["cases"])
            self.assertEqual({"baseline": []}, side_specs["baseline"]["semantic_policy_assertions"])
            calls.append({side: list(spec["case_ids"]) for side, spec in side_specs.items()})
            if round_number == 2:
                return {
                    "baseline": create_sample("baseline", 2, True),
                    "candidate": create_sample("candidate", 2, False),
                }
            return {"candidate": create_sample("candidate", 3, False)}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "load_baseline_evaluator", return_value=baseline_evaluator), patch.object(
                run, "select_semantic_policy_assertions", side_effect=AssertionError("Candidate selector used for V0")), patch.object(
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
        baseline_evaluator.select_semantic_policy_assertions.assert_called_once_with(baseline["case_contracts"], [])

    def test_semantic_confirmation_stops_after_candidate_recovers(self) -> None:
        initial_case = self._create_case_result(self.case, "Refuse.")
        initial_case["decision"] = "refuse"
        initial_actual = {"results": [initial_case]}
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "case_contracts": run.normalize_case_contracts([self.case]),
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
                run, "load_baseline_evaluator", return_value=run), patch.object(
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
            "case_contracts": run.normalize_case_contracts([self.case]),
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
                round_number: int, side_specs: dict[str, dict[str, Any]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del side_specs, codex_home, confirmation_root, timeout
            if round_number == 2:
                return {
                    "baseline": create_sample("baseline", 2, passing_actual),
                    "candidate": create_sample("candidate", 2, unsafe_actual),
                }
            return {"candidate": create_sample("candidate", 3, passing_actual)}

        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "load_baseline_evaluator", return_value=run), patch.object(
                run, "run_confirmation_round", side_effect=fake_round):
            actual = run.run_semantic_confirmation(
                [self.case], initial_actual, run.grade([self.case], initial_actual), baseline, [],
                b"candidate-policy", "candidate-policy", {}, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual([False, True, False], actual["candidate_safety_samples"][self.case["id"]])
        self.assertEqual([], actual["safety_regressions"])
        self.assertEqual([], actual["confirmed_case_regressions"])

    def _create_confirmation_recovery(self) -> tuple[list[dict[str, Any]], dict[str, Any], dict[str, Any], dict[str, Any]]:
        recovered_case = copy.deepcopy(self.case)
        recovered_case["id"] = "recovered_case"
        cases = [self.case, recovered_case]
        initial_actual = {"results": [self._create_case_result(each, "Refuse.") for each in cases]}
        for each in initial_actual["results"]:
            each["decision"] = "refuse"
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "case_contracts": run.normalize_case_contracts(cases),
            "results": [{"case_id": each["id"], "critical": True, "passed": True, "failures": []} for each in cases],
        }
        round_actual = {"results": [initial_actual["results"][0], self._create_case_result(recovered_case, "Proceed.")]}
        usage = {"input_tokens": 0, "cached_input_tokens": 0, "uncached_input_tokens": 0, "output_tokens": 0}
        recovery = {
            "round_results": {
                side: ({
                    "side": side,
                    "round": 2,
                    "case_ids": [each["id"] for each in cases],
                    "runner_exit_code": 0,
                    "transport_validation": {"passed": True},
                    "results": run.grade(cases, round_actual),
                    "usage": usage,
                }, round_actual)
                for side in ("baseline", "candidate")
            },
            "failed_samples": [{
                "side": side,
                "round": 3,
                "case_ids": [self.case["id"]],
                "runner_exit_code": 1,
                "transport_validation": {"passed": False},
                "results": [],
                "usage": usage,
            } for side in ("baseline", "candidate")],
        }
        return cases, initial_actual, baseline, recovery

    def test_semantic_confirmation_recovery_preserves_prior_observations_and_pending_cases(self) -> None:
        cases, initial_actual, baseline, recovery = self._create_confirmation_recovery()

        def fake_round(
                round_number: int, side_specs: dict[str, dict[str, Any]], codex_home: Path,
                confirmation_root: Path, timeout: int, *, serial: bool = False
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del codex_home, confirmation_root, timeout
            self.assertEqual(3, round_number)
            self.assertTrue(serial)
            self.assertEqual({"baseline": [self.case["id"]], "candidate": [self.case["id"]]}, {
                side: spec["case_ids"] for side, spec in side_specs.items()
            })
            result = {}
            for side in ("baseline", "candidate"):
                actual_case = self._create_case_result(self.case, "Proceed." if side == "baseline" else "Refuse.")
                if side == "candidate":
                    actual_case["decision"] = "refuse"
                actual = {"results": [actual_case]}
                sample = copy.deepcopy(recovery["failed_samples"][0])
                sample.update({
                    "side": side,
                    "runner_exit_code": 0,
                    "transport_validation": {"passed": True},
                    "results": run.grade([self.case], actual),
                })
                result[side] = sample, actual
            return result

        original_recovery = copy.deepcopy(recovery)
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "load_baseline_evaluator", return_value=run), patch.object(
                run, "run_confirmation_round", side_effect=fake_round) as run_round, patch.object(
                run, "claim_confirmation_recovery") as claim_recovery:
            actual = run.run_semantic_confirmation(
                cases, initial_actual, run.grade(cases, initial_actual), baseline, [], b"candidate-policy",
                "candidate-policy", {}, Path("unused"), Path(output_directory), 1, recovery=recovery
            )
        self.assertEqual(1, run_round.call_count)
        claim_recovery.assert_called_once_with(recovery, Path(output_directory))
        self.assertEqual([True, False, True], actual["baseline_samples"][self.case["id"]])
        self.assertEqual([False, False, False], actual["candidate_samples"][self.case["id"]])
        self.assertEqual([True, True], actual["baseline_samples"]["recovered_case"])
        self.assertEqual([False, True], actual["candidate_samples"]["recovered_case"])
        self.assertEqual([self.case["id"]], actual["confirmed_case_regressions"])
        self.assertEqual([], actual["blockers"])
        self.assertEqual([2, 2, 3, 3], [each["round"] for each in actual["samples"]])
        self.assertEqual(original_recovery, recovery)

    def test_semantic_confirmation_recovery_rejects_changed_pending_cases(self) -> None:
        cases, initial_actual, baseline, recovery = self._create_confirmation_recovery()
        recovery["failed_samples"][0]["case_ids"] = ["recovered_case"]
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "load_baseline_evaluator", return_value=run), patch.object(
                run, "run_confirmation_round") as run_round, patch.object(
                run, "claim_confirmation_recovery") as claim_recovery, self.assertRaisesRegex(ValueError, "reconstructed plan"):
            run.run_semantic_confirmation(
                cases, initial_actual, run.grade(cases, initial_actual), baseline, [], b"candidate-policy",
                "candidate-policy", {}, Path("unused"), Path(output_directory), 1, recovery=recovery
            )
        run_round.assert_not_called()
        claim_recovery.assert_not_called()

    def test_semantic_confirmation_recovery_keeps_transport_failure_blocking(self) -> None:
        cases, initial_actual, baseline, recovery = self._create_confirmation_recovery()
        failed_round = {each["side"]: (copy.deepcopy(each), {}) for each in recovery["failed_samples"]}
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "load_baseline_policy", return_value=b"baseline-policy"), patch.object(
                run, "load_baseline_evaluator", return_value=run), patch.object(
                run, "run_confirmation_round", return_value=failed_round) as run_round, patch.object(
                run, "claim_confirmation_recovery") as claim_recovery:
            actual = run.run_semantic_confirmation(
                cases, initial_actual, run.grade(cases, initial_actual), baseline, [], b"candidate-policy",
                "candidate-policy", {}, Path("unused"), Path(output_directory), 1, recovery=recovery
            )
        self.assertEqual(1, run_round.call_count)
        claim_recovery.assert_called_once_with(recovery, Path(output_directory))
        self.assertEqual([True, False], actual["baseline_samples"][self.case["id"]])
        self.assertEqual([False, False], actual["candidate_samples"][self.case["id"]])
        self.assertEqual(["semantic-stability-confirmation-runner-failed"], actual["blockers"])
        self.assertEqual([], actual["confirmed_case_regressions"])
        self.assertEqual([1, 1], [each["runner_exit_code"] for each in actual["samples"] if each["round"] == 3])

    def _create_preserved_confirmation_run(self, directory: Path) -> dict[str, Any]:
        directory = directory.resolve()
        cases = [copy.deepcopy(self.case), copy.deepcopy(self.case)]
        cases[0]["required_summary_term_groups"] = [["zzz", "aaa"]]
        cases[1]["id"] = "recovered_case"
        case_source = "\n".join(
            "[[cases]]\n" + "\n".join(f"{key} = {json.dumps(value)}" for key, value in each.items())
            for each in cases
        )
        bundle = run.load_policy_manifest(Path(run.__file__).resolve().parents[3])
        modules = {}
        for side in ("baseline", "candidate"):
            repo_root = directory / side
            for source in bundle["sources"]:
                path = repo_root / source["path"]
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes(source["data"])
            (repo_root / ".codex/harness/agents/cases.toml").write_text(case_source, encoding="utf-8")
            if side == "baseline":
                policy_path = repo_root / "AGENTS.md"
                policy_path.write_bytes(policy_path.read_bytes().rstrip(b"\n"))
            runner_path = repo_root / ".codex/harness/agents/run.py"
            spec = importlib.util.spec_from_file_location(f"preserved_{side}", runner_path)
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            modules[side] = module

        def fake_process(command: list[str], **kwargs: Any) -> Mock:
            result_path = Path(command[command.index("--output-last-message") + 1])
            output_dir = result_path.parent
            policy = (Path(command[command.index("--cd") + 1]) / "AGENTS.md").read_bytes()
            if output_dir.parent.name == "round-3":
                kwargs["stdout"].write(json.dumps({
                    "type": "turn.failed", "error": {"message": "429 Too Many Requests"},
                }) + "\n")
                return Mock(returncode=1)
            actual = {"results": []}
            for each in cases:
                passed = not policy.endswith(b"\n") if output_dir.parent.name != "round-2" else each["id"] == "recovered_case"
                actual_case = self._create_case_result(each, "Proceed. zzz" if passed else "Refuse.")
                if not passed:
                    actual_case["decision"] = "refuse"
                actual["results"].append(actual_case)
            result_path.write_text(json.dumps(actual), encoding="utf-8")
            kwargs["stdout"].write(json.dumps({
                "type": "item.completed", "item": {"type": "agent_message", "text": json.dumps(actual)},
            }) + "\n")
            kwargs["stdout"].write(json.dumps({"type": "turn.completed", "usage": {}}) + "\n")
            return Mock(returncode=0)

        baseline_output = directory / "baseline-output"
        candidate_output = directory / "candidate-output"
        with patch.object(run.subprocess, "run", side_effect=fake_process), patch.object(
                modules["baseline"], "resolve_codex_home", return_value=directory / "codex-home"), patch(
                "sys.argv", ["run.py", "--mode", "all", "--output-dir", str(baseline_output)]
        ), patch("builtins.print"):
            self.assertEqual(0, modules["baseline"].main())
        with patch.object(run.subprocess, "run", side_effect=fake_process), patch.object(
                modules["candidate"], "resolve_codex_home", return_value=directory / "codex-home"), patch("sys.argv", [
                    "run.py", "--mode", "all", "--semantic-stability-check", "--baseline", str(baseline_output),
                    "--authorized-policy-source-change", "agents", "--output-dir", str(candidate_output),
                ]), patch("builtins.print"):
            self.assertEqual(1, modules["candidate"].main())
        return {
            "module": modules["candidate"],
            "baseline_path": baseline_output,
            "candidate_path": candidate_output,
            "baseline": modules["candidate"].load_baseline(baseline_output),
            "cases": cases,
        }

    def test_confirmation_recovery_loads_raw_samples_and_main_runs_only_missing_observations(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            fixture = self._create_preserved_confirmation_run(Path(directory))
            candidate = fixture["module"]
            bundle = candidate.load_policy_manifest(Path(candidate.__file__).resolve().parents[3])
            recovery = candidate.load_confirmation_recovery(
                fixture["candidate_path"], fixture["baseline_path"], fixture["baseline"], bundle,
                fixture["cases"], candidate.normalize_case_policy_bindings(fixture["cases"], bundle)
            )
            self.assertEqual([False, False], [each["passed"] for each in recovery["summary"]["results"]])
            self.assertEqual([False, True], [each["passed"] for each in recovery["round_results"]["candidate"][0]["results"]])
            self.assertEqual([3, 3], [each["round"] for each in recovery["failed_samples"]])
            self.assertTrue(recovery["provenance"]["preserved_artifacts_sha256"])
            original_summary = (fixture["candidate_path"] / "summary.json").read_bytes()
            calls = []

            def fake_process(command: list[str], **kwargs: Any) -> Mock:
                result_path = Path(command[command.index("--output-last-message") + 1])
                output_dir = result_path.parent
                self.assertEqual("round-3", output_dir.parent.name)
                calls.append(output_dir.name)
                actual = {"results": [self._create_case_result(self.case, "Proceed. zzz")]}
                result_path.write_text(json.dumps(actual), encoding="utf-8")
                kwargs["stdout"].write(json.dumps({
                    "type": "item.completed", "item": {"type": "agent_message", "text": json.dumps(actual)},
                }) + "\n")
                kwargs["stdout"].write(json.dumps({"type": "turn.completed", "usage": {}}) + "\n")
                return Mock(returncode=0)

            output_dir = Path(directory) / "recovered-output"
            with patch.object(run.subprocess, "run", side_effect=fake_process), patch.object(
                    candidate, "resolve_codex_home", return_value=Path(directory) / "codex-home"), patch("sys.argv", [
                        "run.py", "--mode", "all", "--semantic-stability-check", "--baseline", str(fixture["baseline_path"]),
                        "--authorized-policy-source-change", "agents", "--resume-confirmation", str(fixture["candidate_path"]),
                        "--output-dir", str(output_dir),
                    ]), patch("builtins.print"):
                self.assertEqual(0, candidate.main())
            summary = json.loads((output_dir / "summary.json").read_text(encoding="utf-8"))
            confirmation = summary["semantic_stability"]["confirmation"]
            self.assertEqual(["baseline", "candidate"], calls)
            self.assertEqual([False, False, True], confirmation["candidate_samples"][self.case["id"]])
            self.assertEqual([False, True], confirmation["candidate_samples"]["recovered_case"])
            self.assertTrue(summary["semantic_stability"]["passed"])
            self.assertEqual(original_summary, (fixture["candidate_path"] / "summary.json").read_bytes())
            self.assertEqual(2, len(summary["confirmation_recovery"]["failed_attempts"]))
            self.assertTrue((fixture["candidate_path"] / "confirmation-recovery.json").is_file())

    def test_confirmation_recovery_rejects_changed_sources_and_raw_artifacts(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            fixture = self._create_preserved_confirmation_run(Path(directory))
            candidate = fixture["module"]
            repo_root = Path(candidate.__file__).resolve().parents[3]
            output_dir = fixture["candidate_path"]
            summary_path = output_dir / "summary.json"
            previous = json.loads(summary_path.read_text(encoding="utf-8"))
            cross_run = copy.deepcopy(previous)
            cross_run["evaluations"][0]["events_log"] = str(fixture["baseline_path"] / "events-evaluation-1.jsonl")
            mutations = [
                ("current-policy", repo_root / "AGENTS.md", (repo_root / "AGENTS.md").read_bytes().rstrip(b"\n")),
                ("current-cases", repo_root / ".codex/harness/agents/cases.toml",
                 (repo_root / ".codex/harness/agents/cases.toml").read_bytes().replace(b'prompt = "Proceed."', b'prompt = "Inspect."')),
                ("current-core", Path(candidate.__file__), Path(candidate.__file__).read_bytes().replace(b"MAX_EVALUATION_INPUT_BYTES = 110_000", b"MAX_EVALUATION_INPUT_BYTES = 109_999")),
                ("preserved-source", output_dir / "policy-snapshot/AGENTS.md", b"changed"),
                ("schema", output_dir / "decision-evaluation-1.schema.json", b"{}"),
                ("raw-result", output_dir / "result-evaluation-1.json", b'{"results": []}'),
                ("completed-event", output_dir / "events-evaluation-1.jsonl", b'{"type": "turn.completed"}\n'),
                ("cross-run-log", summary_path, json.dumps(cross_run).encode("utf-8")),
                ("missing-third-log", output_dir / "semantic-confirmation/round-3/candidate/events-evaluation-1.jsonl", None),
                ("partial-third-result", output_dir / "semantic-confirmation/round-3/candidate/result-evaluation-1.json", b'{"results": []}'),
            ]
            for name, path, replacement in mutations:
                with self.subTest(name=name):
                    original = path.read_bytes() if path.exists() else None
                    try:
                        if replacement is None:
                            path.unlink()
                        else:
                            path.write_bytes(replacement)
                        with self.assertRaises(ValueError):
                            bundle = candidate.load_policy_manifest(repo_root)
                            cases = candidate.load_cases(None)
                            candidate.load_confirmation_recovery(
                                output_dir, fixture["baseline_path"], fixture["baseline"], bundle, cases,
                                candidate.normalize_case_policy_bindings(cases, bundle)
                            )
                    finally:
                        if original is None:
                            path.unlink()
                        else:
                            path.write_bytes(original)

    def test_confirmation_recovery_main_rejects_filters_before_execution_or_claim(self) -> None:
        required = ["run.py", "--mode", "all", "--semantic-stability-check", "--baseline", "baseline", "--resume-confirmation", "previous"]
        for extra in (["--mode", "semantic"], ["--case", "test_case"], ["--profile", "root"], ["--impact-source", "agents"],
                      ["--allow-failures"], ["--transport-check"], ["--list-cases"]):
            with self.subTest(extra=extra), patch("sys.argv", required + extra), patch.object(
                    run, "run_cases") as run_cases, patch.object(run, "claim_confirmation_recovery") as claim_recovery, self.assertRaisesRegex(
                    ValueError, "requires unfiltered"):
                run.main()
            run_cases.assert_not_called()
            claim_recovery.assert_not_called()

    def test_confirmation_round_runs_baseline_and_candidate_concurrently(self) -> None:
        barrier = threading.Barrier(2)

        def fake_sample(
                side: str, round_number: int, cases: list[dict[str, Any]], policy: bytes,
                policy_sha256: str, semantic_policy_assertions: dict[str, list[dict[str, Any]]],
                codex_home: Path, confirmation_root: Path, timeout: int, evaluator: ModuleType | None
        ) -> tuple[dict[str, Any], dict[str, Any]]:
            del round_number, cases, policy, policy_sha256, semantic_policy_assertions
            del codex_home, confirmation_root, timeout, evaluator
            barrier.wait(timeout=2)
            return {"side": side}, {"results": []}

        side_specs = {
            side: {
                "case_ids": [self.case["id"]],
                "cases": [self.case],
                "evaluator": None,
                "policy": side.encode(),
                "policy_sha256": side,
                "semantic_policy_assertions": {},
            }
            for side in ("baseline", "candidate")
        }
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_confirmation_sample", side_effect=fake_sample):
            actual = run.run_confirmation_round(
                2, side_specs, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual({"baseline", "candidate"}, set(actual))

    def test_confirmation_round_serial_mode_preserves_side_order_and_limits_concurrency(self) -> None:
        lock = threading.Lock()
        active = 0
        max_active = 0
        started_sides = []

        def fake_sample(*args: Any) -> tuple[dict[str, Any], dict[str, Any]]:
            nonlocal active, max_active
            side = args[0]
            with lock:
                started_sides.append(side)
                active += 1
                max_active = max(max_active, active)
            threading.Event().wait(0.02)
            with lock:
                active -= 1
            return {"side": side}, {"results": []}

        side_specs = {
            side: {
                "case_ids": [self.case["id"]],
                "cases": [self.case],
                "evaluator": None,
                "policy": side.encode(),
                "policy_sha256": side,
                "semantic_policy_assertions": {},
            }
            for side in ("baseline", "candidate")
        }
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "run_confirmation_sample", side_effect=fake_sample):
            actual = run.run_confirmation_round(
                3, side_specs, Path("unused"), Path(output_directory), 1, serial=True
            )
        self.assertEqual(["baseline", "candidate"], started_sides)
        self.assertEqual(["baseline", "candidate"], list(actual))
        self.assertEqual(1, max_active)

    def test_confirmation_sample_uses_selected_evaluator(self) -> None:
        actual = {"results": [self._create_case_result(self.case, "Proceed.")]}
        evaluations = [{
            "evaluation": 1,
            "case_ids": [self.case["id"]],
            "returned_case_ids": [self.case["id"]],
            "input_bytes": 1,
            "runner_exit_code": 0,
        }]
        baseline_evaluator = ModuleType("baseline")
        baseline_evaluator.run_cases = Mock(return_value=(0, 1.0, actual, evaluations))
        baseline_evaluator.grade = Mock(return_value=[{"passed": True}])
        for side, evaluator, expected in (("baseline", baseline_evaluator, True), ("candidate", None, False)):
            with self.subTest(side=side), tempfile.TemporaryDirectory() as directory, patch.object(
                    run, "run_cases", return_value=(0, 1.0, actual, evaluations)
            ) as current_runner, patch.object(run, "grade", return_value=[{"passed": False}]) as current_grader, patch.object(
                    run, "read_usage", return_value={"input_tokens": 0, "cached_input_tokens": 0, "uncached_input_tokens": 0, "output_tokens": 0}
            ):
                sample, returned = run.run_confirmation_sample(
                    side, 2, [self.case], b"policy", "policy", {}, Path("unused"), Path(directory), 1, evaluator
                )
                self.assertEqual([{"passed": expected}], sample["results"])
                self.assertEqual(actual, returned)
                if evaluator is not None:
                    current_runner.assert_not_called()
                    current_grader.assert_not_called()
                else:
                    current_runner.assert_called_once()
                    current_grader.assert_called_once_with([self.case], actual)
        baseline_evaluator.run_cases.assert_called_once()
        baseline_evaluator.grade.assert_called_once_with([self.case], actual)

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
                "cases": [self.case],
                "evaluator": None,
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

        baseline_evaluator = ModuleType("baseline")
        baseline_evaluator.partition_cases = Mock(side_effect=fake_partition)
        side_specs["baseline"]["evaluator"] = baseline_evaluator
        with tempfile.TemporaryDirectory() as output_directory, patch.object(
                run, "partition_cases", side_effect=fake_partition) as current_partition, patch.object(
                run, "run_confirmation_sample", side_effect=fake_sample):
            run.run_confirmation_round(
                2, side_specs, Path("unused"), Path(output_directory), 1
            )
        self.assertEqual(1, max_active)
        baseline_evaluator.partition_cases.assert_called_once_with(b"baseline", [self.case], "baseline", {})
        current_partition.assert_called_once_with(b"candidate", [self.case], "candidate", {})

    def test_semantic_confirmation_checks_changed_contract_on_candidate_only(self) -> None:
        initial_case = self._create_case_result(self.case, "Refuse.")
        initial_case["decision"] = "refuse"
        initial_actual = {"results": [initial_case]}
        initial_results = run.grade([self.case], initial_actual)
        baseline = {
            "policy_sha256": "baseline-policy",
            "policy_bindings": [],
            "case_contracts": run.normalize_case_contracts([self.case]),
            "results": [{"case_id": self.case["id"], "critical": True, "passed": True, "failures": []}],
        }

        def fake_round(
                round_number: int, side_specs: dict[str, dict[str, Any]],
                codex_home: Path, confirmation_root: Path, timeout: int
        ) -> dict[str, tuple[dict[str, Any], dict[str, Any]]]:
            del round_number, codex_home, confirmation_root, timeout
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
                run, "load_baseline_evaluator", return_value=run), patch.object(
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

    def test_semantic_stability_accepts_confirmed_added_case_with_original_v0(self) -> None:
        added_case = copy.deepcopy(self.case)
        added_case["id"] = "added_case"
        cases = [self.case, added_case]
        actual = {"results": [self._create_case_result(each, "Proceed.") for each in cases]}
        results = run.grade(cases, actual)
        baseline = self._create_passing_baseline()
        confirmation = {
            "safety_regressions": [],
            "blockers": [],
            "confirmed_case_regressions": [],
            "changed_contract_failures": [],
        }
        stability = run.summarize_semantic_stability(
            cases, actual, results, baseline, [], ["added_case:case-added"], confirmation
        )
        self.assertTrue(stability["passed"])
        self.assertEqual(1, stability["baseline_case_count"])
        self.assertEqual(2, stability["candidate_case_count"])
        self.assertIsNone(stability["aggregate_non_regression"])
        self.assertEqual(["added_case"], stability["confirmation_plan"]["candidate_case_ids"])

    def test_semantic_stability_added_case_cannot_offset_original_failure(self) -> None:
        added_case = copy.deepcopy(self.case)
        added_case["id"] = "added_case"
        cases = [self.case, added_case]
        failed_original = self._create_case_result(self.case, "Refuse.")
        failed_original["decision"] = "refuse"
        actual = {"results": [failed_original, self._create_case_result(added_case, "Proceed.")]}
        results = run.grade(cases, actual)
        baseline = self._create_passing_baseline()
        confirmation = {
            "safety_regressions": [],
            "blockers": [],
            "confirmed_case_regressions": [self.case["id"]],
            "changed_contract_failures": [],
        }
        stability = run.summarize_semantic_stability(
            cases, actual, results, baseline, [], ["added_case:case-added"], confirmation
        )
        self.assertFalse(stability["passed"])
        self.assertEqual(["semantic-stability-confirmed-case-regression"], stability["blockers"])
        self.assertIsNone(stability["aggregate_non_regression"])

    def test_semantic_stability_rejects_unreported_addition_and_removed_original(self) -> None:
        added_case = copy.deepcopy(self.case)
        added_case["id"] = "added_case"
        baseline = self._create_passing_baseline()
        for cases, contract_changes in (([self.case, added_case], []), ([added_case], ["added_case:case-added"])):
            with self.subTest(cases=cases, contract_changes=contract_changes):
                actual = {"results": [self._create_case_result(each, "Proceed.") for each in cases]}
                stability = run.summarize_semantic_stability(
                    cases, actual, run.grade(cases, actual), baseline, [], contract_changes
                )
                self.assertIn("semantic-stability-case-set-changed", stability["blockers"])

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
            "semantic_prompt_sha256": "prompts",
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
            legacy_baseline = {key: value for key, value in recorded_baseline.items() if key != "semantic_prompt_sha256"}
            self.assertFalse(run.semantic_evaluation_unchanged(legacy_baseline, current))
        recorded_baseline["semantic_core_sha256"] = "tampered"
        with patch.object(run, "load_baseline_source", return_value=source), self.assertRaisesRegex(
                ValueError, "core digest does not match"):
            run.semantic_evaluation_unchanged(recorded_baseline, current)

    def test_semantic_prompt_fingerprint_tracks_selected_assertions(self) -> None:
        self.case["semantic_policy_assertions"] = True
        binding = {
            "case_id": self.case["id"],
            "assertions": [{"source_id": "rules", "source_path": "rules.md", "statements": ["Proceed read-only."]}],
        }
        assertions = run.select_semantic_policy_assertions([self.case], [binding])
        other_assertions = {**assertions, "other_case": binding["assertions"]}
        actual_result = {"results": [self._create_case_result(self.case, "Proceed.")]}
        prompt_digests = []
        for selected_assertions in (assertions, {}, other_assertions):
            with tempfile.TemporaryDirectory() as directory, patch.object(
                    run, "run_codex", return_value=(0, 1.0, actual_result)
            ):
                _, _, _, evaluations = run.run_cases(
                    b"policy", Path(directory), Path(directory), [self.case], "policy", 1, selected_assertions
                )
            prompt_digests.append(run.digest_json([
                {"case_ids": each["case_ids"], "prompt_sha256": each["prompt_sha256"]}
                for each in evaluations
            ]))
        source = Path(run.__file__).read_bytes()
        baseline = {
            "semantic_core_sha256": run.semantic_core_source_digest(source),
            "semantic_prompt_sha256": prompt_digests[0],
        }
        with patch.object(run, "load_baseline_source", return_value=source):
            for index, expected in ((0, True), (1, False), (2, True)):
                with self.subTest(assertion_selection=index):
                    current = {**baseline, "semantic_prompt_sha256": prompt_digests[index]}
                    self.assertEqual(expected, run.semantic_evaluation_unchanged(baseline, current))

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

    def test_load_baseline_evaluator_uses_verified_snapshot(self) -> None:
        source = (
            b"def select_semantic_policy_assertions(cases, bindings):\n    return {'baseline': []}\n"
            b"if __name__ == '__main__':\n    raise AssertionError('Baseline main must not run')\n"
        )
        with tempfile.TemporaryDirectory() as directory:
            source_path = Path(directory) / ".codex" / "harness" / "agents" / "run.py"
            source_path.parent.mkdir(parents=True)
            source_path.write_bytes(source)
            baseline = {
                "policy_snapshot_dir": directory,
                "policy_sources": [{
                    "id": "harness-runner", "path": ".codex/harness/agents/run.py",
                    "sha256": hashlib.sha256(source).hexdigest(),
                }],
            }
            evaluator = run.load_baseline_evaluator(baseline)
            self.assertIsInstance(evaluator, ModuleType)
            self.assertEqual({"baseline": []}, evaluator.select_semantic_policy_assertions([], []))
            source_path.write_bytes(b"raise AssertionError('Unverified baseline executed')")
            with self.assertRaisesRegex(ValueError, "digest does not match"):
                run.load_baseline_evaluator(baseline)

    def test_main_transport_check_reports_semantic_failure_without_failing_transport(self) -> None:
        case = run.load_cases(["read_only_review"])[0]
        passing_result = self._create_case_result(case, "Review FooService without editing it.")
        failing_result = self._create_case_result(case, "Refuse.")
        failing_result["decision"] = "refuse"
        passing_evaluations = [self._create_evaluation([case["id"]], [])]
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
                    "run.py", "--mode", "semantic", "--case", case["id"], "--output-dir", str(baseline_dir)
            ]), patch.object(
                    run, "run_cases", return_value=(
                        0, 1.0, {"results": [passing_result]}, passing_evaluations
                    )
            ), common_patches[0], common_patches[1]:
                self.assertEqual(0, run.main())
            with patch("sys.argv", [
                    "run.py", "--mode", "semantic", "--case", case["id"], "--transport-check",
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
        evaluations = [self._create_evaluation(case_ids, failed_case_ids)]
        with tempfile.TemporaryDirectory() as output_parent:
            baseline_dir = Path(output_parent) / "baseline"
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
            baseline_summary = json.loads((baseline_dir / "summary.json").read_text(encoding="utf-8"))
            for state, expected in (("same", True), ("changed", False), ("legacy", False)):
                with self.subTest(prompt_fingerprint=state):
                    selected_baseline = copy.deepcopy(baseline_summary)
                    if state == "legacy":
                        selected_baseline.pop("semantic_prompt_sha256")
                    elif state == "changed":
                        selected_baseline["semantic_prompt_sha256"] = "changed"
                    (baseline_dir / "summary.json").write_text(json.dumps(selected_baseline), encoding="utf-8")
                    output_dir = Path(output_parent) / state
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
                    self.assertEqual(expected, summary["semantic_stability"]["semantic_evaluation_unchanged"])
                    self.assertEqual(baseline_summary["semantic_prompt_sha256"], summary["semantic_prompt_sha256"])

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
            return [self._create_evaluation(case_ids, failed_case_ids)]

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
        for statement in (statement, "Retain unsupported defensive code before handoff."):
            with self.subTest(statement=statement):
                assertions[self.case["id"]][0]["statements"] = [statement]
                prompt = run.create_prompt([self.case], "sha256", assertions)
                self.assertIn("Apply each bound statement only to its named case", prompt)
                self.assertIn("Source `implementation-rules` at `.codex/rules.md`", prompt)
                self.assertIn(statement, prompt)
                self.assertNotIn("This statement must not be supplied.", prompt)

    def test_create_prompt_does_not_supply_scenario_answers(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        for answer in (
                "the only action is `assess_non_regression`; include `optional_skill_not_triggered`",
                "For a read-only plan applying canonical implementation rules, include `codex_design_style_required`",
                "include `assess_non_regression`, `meaningful_test_required`, and `functional_non_regression_required`",
                "include both `functional_non_regression_required` and `performance_non_regression_required`",
                "For a post-write code or test decision applying repository impact rules, include `code_policy_required`",
                "include `inspect_local`, `edit_code`, `run_local_checks`, and `run_pre_handoff_review`",
                "and the case asks to fix it now, include `fix_review_findings`",
                "does not authorize a Git write, include `propose_commit_message`",
                "When `original_baseline_must_persist` is included, state `original baseline` in the summary",
        ):
            with self.subTest(answer=answer):
                self.assertNotIn(answer, prompt)
        self.assertIn("Preserve exact technical terms", prompt)

    def test_create_prompt_excludes_expected_answer_fields(self) -> None:
        case = copy.deepcopy(self.case)
        case["required_summary_terms"] = ["hidden_required_answer"]
        case["forbidden_summary_terms"] = ["hidden_forbidden_answer"]
        case["required_summary_term_groups"] = [["hidden_group_answer"]]
        case["required_summary_prefix"] = "hidden_prefix_answer"
        prompt = run.create_prompt([case], "sha256")
        for answer in (
                "hidden_required_answer", "hidden_forbidden_answer", "hidden_group_answer", "hidden_prefix_answer"):
            with self.subTest(answer=answer):
                self.assertNotIn(answer, prompt)

    def test_discovery_java_fixture_lengths_include_indentation(self) -> None:
        case = run.load_cases(["discovery_07"])[0]
        for marker, expected_length in (("A", 199), ("B", 200), ("C", 201)):
            with self.subTest(marker=marker):
                source = case["prompt"].split(f"```java {marker}\n", 1)[1].split("\n```", 1)[0]
                lines = source.splitlines()
                self.assertEqual(2, len(lines))
                self.assertTrue(lines[0].startswith(" " * 8))
                reconstructed = lines[0].rstrip() + " " + lines[1].lstrip()
                self.assertEqual(expected_length, len(reconstructed))
                self.assertTrue(all(len(line) <= 200 for line in lines))

    def test_create_prompt_distinguishes_local_checks_from_smoke_actions(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("Use `run_local_checks` when the case explicitly asks to run or rerun a focused unit test", prompt)
        self.assertIn("Do not classify a focused unit test, build, or ordinary local verification as a smoke test", prompt)
        self.assertIn("Use `triage_failed_smoke` only when the case explicitly identifies an E2E", prompt)
        self.assertIn("Use `rerun_failed_smoke` only when such a smoke case explicitly asks to rerun it", prompt)

    def test_create_prompt_limits_bounded_review_to_optional_skill(self) -> None:
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("bounded adversarial review owned by an applicable optional Skill", prompt)

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

    def test_grade_checks_authorization_word_forms(self) -> None:
        case = run.load_cases(["frozen_ledger_survives_failure_and_review"])[0]
        for wording, expected in (("Please authorize editing", True), ("Please grant authorization to edit", True), ("Edit", False)):
            with self.subTest(wording=wording):
                summary = f"Preserve the original baseline. {wording} module-b/src/Loader.java to register FooService."
                actual = {"results": [self._create_case_result(case, summary)]}
                self.assertEqual(expected, run.grade([case], actual)[0]["passed"])

    def test_grade_checks_no_performance_risk_preflight(self) -> None:
        case = run.load_cases(["no_performance_risk_needs_no_benchmark"])[0]
        result = self._create_case_result(
            case, "Inspection finds no control-flow, allocation, I/O, concurrency, data-size, or frequency change; no credible cost increase requires benchmarking."
        )
        result["reasons"] = ["performance_non_regression_required"]
        for reasons in (result["reasons"], [*result["reasons"], "optional_skill_not_triggered"]):
            with self.subTest(reasons=reasons):
                actual = {"results": [{**result, "reasons": reasons}]}
                self.assertTrue(run.grade([case], actual)[0]["passed"])
        for variant, overrides in (
                ("missing performance reason", {"reasons": []}),
                ("optional reason alone", {"reasons": ["optional_skill_not_triggered"]}),
                ("missing assessment", {"actions": []}),
                ("performance Skill", {"actions": ["assess_non_regression", "invoke_performance_optimization"]}),
                ("benchmark measurement", {"actions": ["assess_non_regression", "measure_performance"]}),
                ("benchmark baseline", {"actions": ["assess_non_regression", "capture_performance_baseline"]}),
                ("wrong decision", {"decision": "confirm"}),
        ):
            with self.subTest(variant=variant):
                actual = {"results": [{**result, **overrides}]}
                self.assertFalse(run.grade([case], actual)[0]["passed"])

    def test_grade_checks_bounded_case_decisions(self) -> None:
        examples = {
            "standalone_coding_standards_audits_sneaky_throws_exception_types": {
                "result": ("Non-compliant", "Strictly compliant"),
                "read": ("correct to @SneakyThrows(IOException.class)", "allowed @SneakyThrows(Exception.class)"),
                "invoke": ("allowed @SneakyThrows(Exception.class)", "remove @SneakyThrows(Exception.class)"),
                "advice": ("allowed @SneakyThrows(Throwable.class)", "remove @SneakyThrows(Throwable.class)"),
                "parse": ("allowed throws SQLException", "add @SneakyThrows(SQLException.class)"),
            },
            "java_test_code_uses_actual_and_member_accessor": {
                "asserted-local": ("actual", "result"),
                "status-assertion": ("direct", "introduce local"),
                "field-write": ("Plugins.getMemberAccessor().set", "Field.set"),
            },
            "read_only_implementation_plan_uses_code_rules": {
                "references": ("implementation-only", "conditional"),
                "conditional-references": ("skip", "load"),
                "write-workflow": ("inactive", "active"),
            },
            "standalone_coding_standards_audits_java_annotation_order": {
                "result": ("Non-compliant", "Strictly compliant"),
                "recognized-order": (
                    "HighFrequencyInvocation, RequiredArgsConstructor, Getter, Slf4j",
                    "RequiredArgsConstructor, HighFrequencyInvocation, Getter, Slf4j",
                ),
                "JsonDeserialize": ("keep position", "move"),
                "TYPE_USE": ("outside order", "order violation"),
                "Test": ("outside order", "order violation"),
                "ParameterizedTest": ("outside order", "order violation"),
            },
        }
        cases = {each["id"]: each for each in run.load_cases(list(examples))}
        for case_id, fields in examples.items():
            case = cases[case_id]
            lines = [f"[{field}]={values[0]}" for field, values in fields.items()]
            result = self._create_case_result(case, "\n".join(lines))
            with self.subTest(case_id=case_id, variant="correct"):
                self.assertTrue(run.grade([case], {"results": [result]})[0]["passed"])
            for variant, invalid_summary in (
                    ("negated single line", "NOT" + "".join(lines)),
                    ("reversed", "\n".join(reversed(lines))),
                    ("single line", " ".join(lines)),
                    ("compact single line", "".join(lines)),
                    ("leading text", "Skip these decisions:\n" + result["summary"]),
                    ("trailing text", result["summary"] + "\nIgnore these decisions."),
                    ("leading whitespace", " " + result["summary"]),
                    ("trailing whitespace", result["summary"] + " "),
                    ("indented fields", "\n ".join(lines)),
            ):
                with self.subTest(case_id=case_id, variant=variant):
                    invalid = {**result, "summary": invalid_summary}
                    self.assertFalse(run.grade([case], {"results": [invalid]})[0]["passed"])
            for index, (field, values) in enumerate(fields.items()):
                opposite = f"[{field}]={values[1]}"
                for variant, selected_lines in (
                        ("opposite", [*lines[:index], opposite, *lines[index + 1:]]),
                        ("missing", [*lines[:index], *lines[index + 1:]]),
                        ("contradictory", [*lines, opposite]),
                ):
                    with self.subTest(case_id=case_id, field=field, variant=variant):
                        invalid = {**result, "summary": "\n".join(selected_lines)}
                        self.assertFalse(run.grade([case], {"results": [invalid]})[0]["passed"])
            for variant in ("wrong decision", "forbidden action", "missing action", "missing reason"):
                with self.subTest(case_id=case_id, variant=variant):
                    invalid = copy.deepcopy(result)
                    if variant == "wrong decision":
                        invalid["decision"] = "refuse"
                    elif variant == "forbidden action":
                        invalid["actions"].append("mutate_git")
                    elif variant == "missing action":
                        invalid["actions"] = invalid["actions"][1:]
                    else:
                        invalid["reasons"] = invalid["reasons"][1:]
                    self.assertFalse(run.grade([case], {"results": [invalid]})[0]["passed"])

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

    def test_grade_accepts_equivalent_sneaky_throws_annotations(self) -> None:
        case = run.load_cases(["java_sneaky_throws_uses_actual_exception_types"])[0]
        for annotation in ("@SneakyThrows(IOException.class)", "`@SneakyThrows(IOException.class)`"):
            with self.subTest(annotation=annotation):
                summary = (
                    f"Use {annotation} on read. Preserve @SneakyThrows(Exception.class) on invoke, "
                    "@SneakyThrows(Throwable.class) on advice, and throws SQLException without @SneakyThrows on parse. "
                    "Then rerun affected checks and Formal Review."
                )
                actual = {"results": [self._create_case_result(case, summary)]}
                result = run.grade([case], actual)[0]
                self.assertTrue(result["passed"], result["failures"])

    def test_grade_rejects_sneaky_throws_action_contract_violations(self) -> None:
        case = run.load_cases(["java_sneaky_throws_uses_actual_exception_types"])[0]
        summary = (
            "Use @SneakyThrows(IOException.class) on read. Preserve @SneakyThrows(Exception.class) on invoke, "
            "@SneakyThrows(Throwable.class) on advice, and throws SQLException without @SneakyThrows on parse."
        )
        for actions in (
                [action for action in case["required_actions"] if action != "run_pre_handoff_review"],
                [*case["required_actions"], "keep_manual_throw"]):
            with self.subTest(actions=actions):
                actual_result = self._create_case_result(case, summary)
                actual_result["actions"] = actions
                result = run.grade([case], {"results": [actual_result]})[0]
                self.assertFalse(result["passed"], result)
                self.assertTrue(any("actions=" in failure for failure in result["failures"]), result["failures"])

    def test_grade_accepts_unambiguous_defensive_guard_removal(self) -> None:
        case = run.load_cases(["speculative_defensive_guard_is_removed"])[0]
        actual = {"results": [self._create_case_result(case, "defense=remove;fallback_test=remove")]}
        result = run.grade([case], actual)[0]
        self.assertTrue(result["passed"], result["failures"])

    def test_grade_rejects_contradictory_defensive_guard_decisions(self) -> None:
        case = run.load_cases(["speculative_defensive_guard_is_removed"])[0]
        for summary in (
                "defense=remove; fallback_test=remove. Keep the null guard; remove the fallback-only test.",
                "defense=remove;fallback_test=remove. Keep the null guard.",
                "defense=remove;fallback_test=remove. Keep the fallback-only test.",
                "defense=remove;fallback_test=remove;defense=keep"):
            with self.subTest(summary=summary):
                actual = {"results": [self._create_case_result(case, summary)]}
                result = run.grade([case], actual)[0]
                self.assertFalse(result["passed"], result)

    def test_grade_rejects_malformed_defensive_guard_decisions_within_length_limit(self) -> None:
        case = run.load_cases(["speculative_defensive_guard_is_removed"])[0]
        for summary in ("defense=remove\nfallback_test=remove", "defense=removefallback_test=remove"):
            with self.subTest(summary=summary):
                actual = {"results": [self._create_case_result(case, summary)]}
                result = run.grade([case], actual)[0]
                self.assertFalse(result["passed"], result)

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

    def test_load_policy_manifest_rejects_unregistered_skill_policy_files(self) -> None:
        for relative_path in (
                ".codex/context/example.md",
                ".codex/harness/agents/example.md",
                ".codex/skills/example/SKILL.md",
                ".codex/skills/example/agents/openai.yaml",
                ".codex/skills/example/references/rules/example.md",
        ):
            with self.subTest(path=relative_path), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                manifest_path = self.create_bundle(root)
                policy_path = root / relative_path
                policy_path.parent.mkdir(parents=True, exist_ok=True)
                policy_path.write_text("Policy.\n", encoding="utf-8")
                with self.assertRaisesRegex(ValueError, "absent from the manifest"):
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
            "test-write": {"token-efficiency", "code-of-conduct", "code-implementation", "implementation-rules", "testing-rules", "contracts-and-removal"},
            "code-review": {"contracts-and-removal", "code-verification"},
            "code-design": {"implementation-rules", "testing-rules", "contracts-and-removal", "cross-cutting-skills"},
            "runtime-diagnosis": {"runtime-triage", "code-verification"},
            "command-execution": {"token-efficiency", "docker-resources", "code-verification"},
            "gen-ut": {"code-of-conduct", "code-implementation", "implementation-rules", "contracts-and-removal", "gen-ut"},
            "review-pr": {"code-of-conduct", "implementation-rules", "code-verification", "review-pr"},
            "policy-maintenance": {"policy-maintenance", "policy-source-manifest"},
        }
        for profile, required_sources in expected.items():
            with self.subTest(profile=profile):
                self.assertTrue(required_sources.issubset(bundle["profiles"][profile]))

    def test_case_policy_binding_requires_full_statement_in_source(self) -> None:
        for punctuation in ".!?;。！？；":
            with self.subTest(punctuation=punctuation), tempfile.TemporaryDirectory() as directory:
                statement = (
                    f"Preserve every supported behavior before handing off the completed task{punctuation}"
                    if punctuation in ".!?;"
                    else f"在交付本次任务之前，必须保留全部现有的受支持行为并完成必要的验证{punctuation}"
                )
                short_statement = f"Preserve behavior{punctuation}"
                root = Path(directory)
                manifest_path = self.create_bundle(root)
                (root / ".codex" / "rules.md").write_text(f"{statement}\n{short_statement}\n", encoding="utf-8")
                bundle = run.load_policy_manifest(root, manifest_path)
                binding = run.normalize_case_policy_bindings([self.create_bound_case(statement)], bundle)[0]
                self.assertEqual(".codex/rules.md", binding["assertions"][0]["source_path"])
                for invalid_statement in (statement[:-1], short_statement):
                    with self.assertRaisesRegex(ValueError, "complete normative sentences"):
                        run.normalize_case_policy_bindings([self.create_bound_case(invalid_statement)], bundle)
                for invalid_statement in (
                        statement.replace("supported", "unsupported").replace("受支持", "不受支持"),
                        statement[:-1] + (";" if punctuation == "." else "."),
                ):
                    with self.assertRaisesRegex(ValueError, "absent from its source"):
                        run.normalize_case_policy_bindings([self.create_bound_case(invalid_statement)], bundle)

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
