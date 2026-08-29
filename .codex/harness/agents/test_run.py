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

    def test_summarize_runner_failure_prioritizes_network_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as output_directory:
            output_path = Path(output_directory)
            (output_path / "events.jsonl").write_text(json.dumps({
                "type": "error", "message": "Connection failed: failed to lookup address information",
            }), encoding="utf-8")
            (output_path / "stderr.log").write_text("Harness timeout after 600 seconds.\n", encoding="utf-8")
            actual = run.summarize_runner_failure(output_path, 124)
        self.assertEqual("network", actual["category"])
        self.assertEqual(["Connection failed: failed to lookup address information"], actual["evidence"])

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
                output_path, snapshot_path, "failed-candidate", 1, [{"evaluation": 1, "runner_exit_code": 1}]
            )
            persisted = json.loads((output_path / "summary.json").read_text(encoding="utf-8"))
        self.assertEqual(actual, persisted)
        self.assertEqual("sandbox", persisted["runner_failure"]["category"])
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

    def test_normalize_case_contracts_sorts_summary_terms(self) -> None:
        self.case["required_summary_terms"] = ["beta", "alpha"]
        self.case["forbidden_summary_terms"] = ["delta", "gamma"]
        actual = run.normalize_case_contracts([self.case])[0]
        self.assertEqual(["alpha", "beta"], actual["required_summary_terms"])
        self.assertEqual(["delta", "gamma"], actual["forbidden_summary_terms"])

    def test_normalize_case_contracts_preserves_explicit_profile(self) -> None:
        self.case["profile"] = "code-write"
        self.assertEqual("code-write", run.normalize_case_contracts([self.case])[0]["profile"])

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

    def test_create_prompt_labels_profile_as_routing_only(self) -> None:
        self.case["profile"] = "code-write"
        prompt = run.create_prompt([self.case], "sha256")
        self.assertIn("routing profile `code-write`", prompt)
        self.assertIn("root decision", prompt)
        self.assertIn("do not supply profile source contents", prompt)

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

    def test_semantic_metadata_disclaims_profile_content_evaluation(self) -> None:
        metadata = run.semantic_evaluation_metadata()
        self.assertEqual("root", metadata["decision_surface"])
        self.assertFalse(metadata["profile_source_contents_included"])
        self.assertIn("non-root", metadata["does_not_prove"])

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
