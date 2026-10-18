#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import hashlib
import json
import os
import subprocess
import sys
import tempfile
import unittest
from argparse import Namespace
from contextlib import redirect_stderr, redirect_stdout
from io import StringIO
from pathlib import Path
from unittest.mock import patch


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import review_ledger as ledger


class ReviewLedgerTest(unittest.TestCase):

    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.private_root = Path(temporary.name)
        self.repo = self.private_root / "repo"
        self.repo.mkdir()
        temporary_root = patch.object(ledger.tempfile, "gettempdir", return_value=str(self.private_root))
        temporary_root.start()
        self.addCleanup(temporary_root.stop)
        self.source_path = "src/main/java/Foo.java"
        self.owner_path = "src/main/java/Owner.java"
        (self.repo / self.source_path).parent.mkdir(parents=True)
        (self.repo / self.source_path).write_text("first\nold_first\nthird\nfourth\nfifth\nold_second\nlast\n", encoding="utf-8")
        (self.repo / self.owner_path).write_text("Supported owner contract.\n", encoding="utf-8")
        (self.repo / "Rules.md").write_text("Use the owning public boundary.\n", encoding="utf-8")
        (self.repo / "Requirements.md").write_text("Review this candidate under the task scope and accepted exceptions.\n", encoding="utf-8")
        self.git("init", "-q")
        self.git("add", ".")
        self.git("commit", "-qm", "Synthetic baseline")
        self.base_sha = self.git("rev-parse", "HEAD").strip()
        (self.repo / self.source_path).write_text("first\nnew_first\nthird\nfourth\nfifth\nnew_second\nlast\n", encoding="utf-8")
        self.candidate_files = self.private_root / "candidate-files.txt"
        self.candidate_files.write_text(self.source_path + "\n", encoding="utf-8")
        self.attempt_log = self.private_root / "attempt.log"
        self.attempt_log.write_text("Synthetic evidence route: unavailable target version; repository alternatives checked.\n", encoding="utf-8")
        self.basis_file = self.private_root / "basis.json"
        self.basis_file.write_text(json.dumps({"focus": "Code Correctness Review", "sources": [
            {"role": "rule", "origin": "repository:Rules.md", "path": "Rules.md"},
            {"role": "requirement", "origin": "user:synthetic-task", "path": "Requirements.md"},
            {"role": "evidence", "origin": "attempt:target-version", "path": str(self.attempt_log)},
        ]}), encoding="utf-8")

    def git(self, *arguments):
        environment = {**os.environ, "GIT_CONFIG_NOSYSTEM": "1", "GIT_CONFIG_GLOBAL": os.devnull,
                       "GIT_AUTHOR_NAME": "Synthetic Review", "GIT_COMMITTER_NAME": "Synthetic Review",
                       "GIT_AUTHOR_EMAIL": "review@example.invalid", "GIT_COMMITTER_EMAIL": "review@example.invalid"}
        return subprocess.run(["git", "-c", "core.hooksPath=" + os.devnull, *arguments], cwd=self.repo, env=environment,
                              check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True).stdout

    def test_complete_ledger_is_valid(self):
        self.assertEqual([], ledger.validate_ledger(self.create_valid_ledger()))

    def test_confirmed_finding_requires_non_blank_proof(self):
        actual_ledger = self.create_valid_ledger()
        finding = self.create_confirmed_finding("F1")
        finding.update({
            "origin": " ", "fix_boundary": "boundary", "evidence": [""], "full_path": [" "], "counter_evidence": [],
            "necessity": " ", "scope_proof": " ", "files": [""], "anchors": [],
        })
        actual_ledger["findings"] = [finding]
        errors = ledger.validate_ledger(actual_ledger)
        self.assertIn("F1 confirmed finding is missing origin", errors)
        self.assertIn("F1 confirmed finding is missing evidence", errors)
        self.assertIn("F1 confirmed finding is missing full-path review", errors)
        self.assertIn("F1 confirmed finding is missing counter-evidence review", errors)
        self.assertIn("F1 confirmed finding is missing necessity", errors)
        self.assertIn("F1 confirmed finding is missing scope proof", errors)
        self.assertIn("F1 confirmed finding is missing scope files", errors)

    def test_malformed_ledger_is_rejected(self):
        actual_ledger = self.create_valid_ledger()
        del actual_ledger["files"]
        with self.assertRaisesRegex(RuntimeError, "Malformed review coverage ledger"):
            ledger.validate_ledger(actual_ledger)

    def test_non_mapping_ledger_is_rejected(self):
        with self.assertRaisesRegex(RuntimeError, "Malformed review coverage ledger"):
            ledger.validate_schema([])

    def test_malformed_nested_ledger_value_is_rejected(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["files"][0]["clusters"] = [{}]
        with self.assertRaisesRegex(RuntimeError, "Malformed review coverage ledger list entry"):
            ledger.validate_ledger(actual_ledger)

    def test_convergence_pass_must_follow_latest_review_change(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["review_revision"] = 2
        errors = ledger.validate_ledger(actual_ledger)
        self.assertIn("Latest convergence pass predates a file or finding change", errors)

    def test_github_file_list_must_match_local_scope(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["scope"]["github_files"] = {"provided": True, "matched": False}
        errors = ledger.validate_ledger(actual_ledger)
        self.assertIn("GitHub file list does not match local triple-dot scope", errors)

    def test_init_uses_exact_candidate_files(self):
        actual = self.initialize_ledger()
        self.assertEqual({"provided": True, "path": str(self.candidate_files.resolve())}, actual["scope"]["candidate_files"])
        self.assertEqual([self.source_path], [each["path"] for each in actual["files"]])
        self.assertEqual(2, len(actual["files"][0]["blocks"]))
        self.assertEqual(self.base_sha, actual["basis"]["baseline"]["commit"])

    def test_confirmed_findings_must_have_distinct_fix_boundaries(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["findings"] = [self.create_confirmed_finding("F1"), self.create_confirmed_finding("F2")]
        errors = ledger.validate_ledger(actual_ledger)
        self.assertIn("Confirmed findings share fix boundaries: 1", errors)

    def test_incomplete_validation_requires_gap(self):
        errors = ledger.validate_ledger(self.create_valid_ledger(), incomplete_result=True)
        self.assertIn("Review Incomplete validation requires at least one incomplete gap", errors)

    def test_complete_incomplete_ledger_is_valid(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["findings"] = [self.create_incomplete_gap()]
        actual_ledger["files"][0].update({
            "status": "blocked",
            "findings": ["G1"],
            "reason": "Decisive version evidence is unavailable.",
        })
        self.assertEqual([], ledger.validate_ledger(actual_ledger, incomplete_result=True))

    def test_incomplete_gap_requires_complete_proof(self):
        actual_ledger = self.create_valid_ledger()
        finding = self.create_incomplete_gap()
        finding.update({
            "origin": " ", "evidence": [""], "full_path": [" "], "counter_evidence": [],
            "necessity": " ", "scope_proof": " ", "files": [""], "reason": " ", "anchors": [],
        })
        actual_ledger["findings"] = [finding]
        errors = ledger.validate_ledger(actual_ledger, incomplete_result=True)
        expected_errors = [
            "G1 incomplete gap is missing the unavailable fact",
            "G1 incomplete gap is missing the fact source",
            "G1 incomplete gap is missing unavailability proof",
            "G1 incomplete gap is missing the affected full path",
            "G1 incomplete gap is missing alternative evidence checks",
            "G1 incomplete gap is missing outcome impact",
            "G1 incomplete gap is missing scope proof",
            "G1 incomplete gap is missing affected scope files",
        ]
        for each in expected_errors:
            with self.subTest(error=each):
                self.assertIn(each, errors)

    def test_incomplete_blocked_file_requires_reason_and_gap_link(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["findings"] = [self.create_incomplete_gap()]
        actual_ledger["files"][0].update({"status": "blocked", "reason": "", "findings": []})
        errors = ledger.validate_ledger(actual_ledger, incomplete_result=True)
        self.assertIn("Blocked files missing incomplete reasons: 1", errors)
        self.assertIn("Blocked files not linked to an incomplete gap: 1", errors)

    def test_incomplete_validation_rejects_unresolved_candidate(self):
        actual_ledger = self.create_valid_ledger()
        candidate = self.create_confirmed_finding("F1")
        candidate["status"] = "candidate"
        actual_ledger["findings"] = [self.create_incomplete_gap(), candidate]
        errors = ledger.validate_ledger(actual_ledger, incomplete_result=True)
        self.assertIn("F1 is still a candidate", errors)

    def test_standard_validation_still_rejects_incomplete_gap(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["findings"] = [self.create_incomplete_gap()]
        errors = ledger.validate_ledger(actual_ledger)
        self.assertIn("G1 requires a Review Incomplete result", errors)

    def test_validate_incomplete_subcommand_uses_incomplete_validator(self):
        args = ledger.build_parser().parse_args(["validate-incomplete", "--ledger", "candidate"])
        self.assertIs(ledger.cmd_validate_incomplete, args.func)

    def test_cleanup_removes_only_validated_ledger_directory(self):
        with tempfile.TemporaryDirectory() as temp_dir, patch.object(ledger.tempfile, "gettempdir", return_value=temp_dir):
            ledger_dir = ledger.ledger_root() / "candidate"
            ledger_dir.mkdir()
            ledger_file = ledger_dir / ledger.LEDGER_FILE_NAME
            ledger.write_ledger(ledger_file, self.create_valid_ledger())
            with redirect_stdout(StringIO()):
                self.assertEqual(0, ledger.cmd_cleanup(Namespace(ledger=str(ledger_file))))
            self.assertFalse(ledger_dir.exists())
            outside_file = Path(temp_dir) / ledger.LEDGER_FILE_NAME
            outside_file.write_text("{}", encoding="utf-8")
            with self.assertRaisesRegex(RuntimeError, "outside the private review ledger root"):
                ledger.ensure_safe_ledger_file(outside_file)

    def test_file_alias_read_and_write_use_canonical_ledger(self):
        actual = self.create_valid_ledger()
        outside = self.private_root / "outside"
        outside.mkdir()
        alias = outside / "alias.json"
        alias.symlink_to(self.ledger_file)
        actual["review_revision"] += 1
        ledger.write_ledger(alias, actual)
        self.assertTrue(alias.is_symlink())
        self.assertFalse(alias.with_suffix(".tmp").exists())
        self.assertEqual(actual, ledger.read_ledger(alias))
        self.assertEqual(actual, ledger.read_ledger(self.ledger_file))

    def test_cleanup_file_alias_preserves_external_and_sibling_directories(self):
        actual = self.create_valid_ledger()
        sibling = ledger.create_ledger_dir(self.repo, "sibling", self.base_sha) / ledger.LEDGER_FILE_NAME
        ledger.write_ledger(sibling, actual)
        sibling_content = sibling.read_bytes()
        outside = self.private_root / "outside"
        outside.mkdir()
        marker = outside / "keep.txt"
        marker.write_text("Unrelated external resource.\n", encoding="utf-8")
        alias = outside / "alias.json"
        alias.symlink_to(self.ledger_file)
        with redirect_stdout(StringIO()):
            self.assertEqual(0, ledger.cmd_cleanup(Namespace(ledger=str(alias))))
        self.assertFalse(self.ledger_file.parent.exists())
        self.assertTrue(alias.is_symlink())
        self.assertEqual("Unrelated external resource.\n", marker.read_text(encoding="utf-8"))
        self.assertEqual(sibling_content, sibling.read_bytes())

    def test_cleanup_rejects_ledger_outside_private_root(self):
        actual = self.create_valid_ledger()
        outside = self.private_root / "outside"
        outside.mkdir()
        outside_file = outside / ledger.LEDGER_FILE_NAME
        outside_file.write_text(json.dumps(actual), encoding="utf-8")
        content = outside_file.read_bytes()
        with self.assertRaisesRegex(RuntimeError, "outside the private review ledger root"):
            ledger.cmd_cleanup(Namespace(ledger=str(outside_file)))
        self.assertEqual(content, outside_file.read_bytes())
        self.assertTrue(self.ledger_file.exists())

    def test_cleanup_rejects_unowned_directory_contents(self):
        self.create_valid_ledger()
        unknown = self.ledger_file.parent / "coverage.json"
        unknown.write_text("Unattributed temporary input.\n", encoding="utf-8")
        content = self.ledger_file.read_bytes()
        with self.assertRaisesRegex(RuntimeError, "unowned resources; cleanup refused"):
            ledger.cmd_cleanup(Namespace(ledger=str(self.ledger_file)))
        self.assertEqual(content, self.ledger_file.read_bytes())
        self.assertEqual("Unattributed temporary input.\n", unknown.read_text(encoding="utf-8"))

    def test_cleanup_preserves_invalid_ledger(self):
        self.create_valid_ledger()
        self.ledger_file.write_text("{}", encoding="utf-8")
        with self.assertRaisesRegex(RuntimeError, "Unsupported or invalid review coverage ledger"):
            ledger.cmd_cleanup(Namespace(ledger=str(self.ledger_file)))
        self.assertEqual("{}", self.ledger_file.read_text(encoding="utf-8"))

    def test_cleanup_failure_is_reported(self):
        self.create_valid_ledger()
        content = self.ledger_file.read_bytes()
        output, error = StringIO(), StringIO()
        arguments = ["review_ledger.py", "cleanup", "--ledger", str(self.ledger_file)]
        with patch.object(sys, "argv", arguments), patch.object(Path, "unlink", side_effect=PermissionError("cleanup denied")), \
                redirect_stdout(output), redirect_stderr(error):
            self.assertEqual(1, ledger.main())
        self.assertEqual("", output.getvalue())
        self.assertEqual("error: cleanup denied\n", error.getvalue())
        self.assertEqual(content, self.ledger_file.read_bytes())

    def test_init_write_failure_removes_only_new_directory(self):
        self.create_valid_ledger()
        sibling = self.ledger_file
        content = sibling.read_bytes()
        failure = OSError("initial write failed")

        def fail_write(ledger_file, _):
            ledger_file.with_suffix(".tmp").write_text("Partial initial write.\n", encoding="utf-8")
            raise failure

        with patch.object(ledger, "write_ledger", side_effect=fail_write), self.assertRaises(OSError) as caught:
            self.initialize_ledger()
        self.assertIs(failure, caught.exception)
        self.assertEqual([sibling.parent], list(ledger.ledger_root().iterdir()))
        self.assertEqual(content, sibling.read_bytes())

    def test_init_cleanup_failure_preserves_original_error_and_reports_residuals(self):
        failure = OSError("initial write failed")

        def fail_write(ledger_file, _):
            ledger_file.with_suffix(".tmp").write_text("Partial initial write.\n", encoding="utf-8")
            raise failure

        error = StringIO()
        with patch.object(ledger, "write_ledger", side_effect=fail_write), patch.object(Path, "unlink", side_effect=PermissionError("cleanup denied")), \
                redirect_stderr(error), self.assertRaises(OSError) as caught:
            self.initialize_ledger()
        self.assertIs(failure, caught.exception)
        directories = list(ledger.ledger_root().iterdir())
        self.assertEqual(1, len(directories))
        self.assertEqual("Partial initial write.\n", (directories[0] / "ledger.tmp").read_text(encoding="utf-8"))
        self.assertEqual(f"error: Initialization cleanup failed; current-review resources may remain at {directories[0]}: cleanup denied\n", error.getvalue())

    def test_init_write_failure_before_output_removes_new_directory(self):
        failure = PermissionError("initial write denied")
        with patch.object(ledger, "write_ledger", side_effect=failure), self.assertRaises(PermissionError) as caught:
            self.initialize_ledger()
        self.assertIs(failure, caught.exception)
        self.assertEqual([], list(ledger.ledger_root().iterdir()))

    def test_old_ledger_version_requires_new_review(self):
        actual = self.create_valid_ledger()
        actual["version"] = 3
        with self.assertRaisesRegex(RuntimeError, "Unsupported or invalid"):
            ledger.validate_ledger(actual)

    def test_scope_count_must_match_actual_candidate(self):
        actual = self.create_valid_ledger()
        actual["scope"]["changed_file_count"] = 2
        self.assertIn("Ledger files, changed-file count, or changed blocks do not match the effective candidate", ledger.validate_ledger(actual))

    def test_persisted_nested_shapes_have_cli_diagnostics(self):
        original = self.create_valid_ledger()
        cases = [
            ("scope", "candidate_files", {}, "candidate scope"),
            ("basis", "baseline", {}, "original baseline"),
            ("basis", "delta", [{}], "delta"),
            ("scope", "base_sha", None, "scope"),
            ("scope", "head_sha", None, "scope"),
        ]
        for section, key, value, diagnostic in cases:
            with self.subTest(key=key):
                actual = json.loads(json.dumps(original))
                actual[section][key] = value
                self.ledger_file.write_text(json.dumps(actual), encoding="utf-8")
                output = StringIO()
                with patch.object(sys, "argv", ["review_ledger.py", "validate", "--ledger", str(self.ledger_file)]), redirect_stderr(output):
                    self.assertEqual(1, ledger.main())
                self.assertEqual(f"error: Malformed review coverage ledger {diagnostic}\n", output.getvalue())

    def test_every_changed_block_requires_coverage(self):
        actual = self.create_valid_ledger()
        actual["files"][0]["coverage"].pop()
        self.assertIn(f"{self.source_path} has uncovered changed blocks: 1", ledger.validate_ledger(actual))

    def test_explained_file_status_preserves_non_substantive_boundary(self):
        original = self.create_valid_ledger()
        for status in ("churn-only", "not-applicable"):
            with self.subTest(status=status):
                actual = json.loads(json.dumps(original))
                actual["files"][0].update({"status": status, "reason": "Current evidence supports this non-substantive classification.", "coverage": []})
                for review_pass in actual["passes"]:
                    review_pass["basis_fingerprint"] = ledger.basis_fingerprint(actual)
                self.assertEqual([], ledger.validate_ledger(actual))

    def test_missing_changed_block_cannot_hide_unreviewed_code(self):
        actual = self.create_valid_ledger()
        actual["files"][0]["blocks"].pop()
        self.assertIn("Ledger files, changed-file count, or changed blocks do not match the effective candidate", ledger.validate_ledger(actual))

    def test_unknown_changed_block_coverage_is_rejected(self):
        actual = self.create_valid_ledger()
        actual["files"][0]["coverage"][0]["block"] = "unknown-block"
        self.assertIn(f"{self.source_path} has unknown changed-block coverage", ledger.validate_ledger(actual))

    def test_reviewed_block_requires_known_requirement_origin(self):
        actual = self.create_valid_ledger()
        actual["files"][0]["coverage"][0]["requirements"] = ["unrelated-task"]
        self.assertIn(f"{self.source_path} block coverage references unknown applicable requirement origins", ledger.validate_ledger(actual))

    def test_block_anchor_must_cover_changed_lines(self):
        actual = self.create_valid_ledger()
        coverage = actual["files"][0]["coverage"]
        coverage[0]["anchors"] = [{"side": "candidate", "path": self.source_path, "start_line": 3, "end_line": 3}]
        coverage_file = self.private_root / "incorrect-coverage.json"
        coverage_file.write_text(json.dumps(coverage), encoding="utf-8")
        args = ledger.build_parser().parse_args(["mark-file", "--ledger", str(self.ledger_file), "--path", self.source_path, "--status", "reviewed",
                                                 "--coverage", str(coverage_file)])
        with self.assertRaisesRegex(RuntimeError, "no anchor at the actual change"):
            ledger.cmd_mark_file(args)
        self.assertEqual(actual["files"][0]["coverage"][1], ledger.read_ledger(self.ledger_file)["files"][0]["coverage"][1])

    def test_source_anchor_rejects_wrong_origin_and_range(self):
        self.create_valid_ledger()
        invalid = [
            {"side": "source", "path": str(self.attempt_log), "origin": "unknown-origin"},
            {"side": "source", "path": "Requirements.md", "origin": "attempt:target-version"},
            {"side": "candidate", "path": self.source_path, "start_line": 1, "end_line": 80},
            {"side": "candidate", "path": self.source_path, "start_line": True, "end_line": 1},
        ]
        for anchor in invalid:
            with self.subTest(anchor=anchor), self.assertRaises(RuntimeError):
                anchors_file = self.private_root / "invalid-anchors.json"
                anchors_file.write_text(json.dumps([anchor]), encoding="utf-8")
                args = ledger.build_parser().parse_args(["add-finding", "--ledger", str(self.ledger_file), "--id", "F1", "--status", "non-blocking",
                                                         "--anchors", str(anchors_file)])
                ledger.cmd_add_finding(args)

    def test_changed_candidate_and_scope_invalidate_review(self):
        actual = self.create_valid_ledger()
        (self.repo / self.source_path).write_text("A new effective candidate.\n", encoding="utf-8")
        self.assertIn("Effective candidate, original baseline, focus, or declared review sources changed; initialize a new ledger", ledger.validate_ledger(actual))
        (self.repo / "new.txt").write_text("Additional task change.\n", encoding="utf-8")
        self.candidate_files.write_text(self.source_path + "\nnew.txt\n", encoding="utf-8")
        self.assertIn("Ledger files, changed-file count, or changed blocks do not match the effective candidate", ledger.validate_ledger(actual))

    def test_changed_rules_requirements_and_focus_invalidate_review(self):
        actual = self.create_valid_ledger()
        original_basis = self.basis_file.read_text(encoding="utf-8")
        for changed in ("Rules.md", "Requirements.md", "focus"):
            with self.subTest(changed=changed):
                if "focus" == changed:
                    configuration = json.loads(original_basis)
                    configuration["focus"] = "CI Review"
                    self.basis_file.write_text(json.dumps(configuration), encoding="utf-8")
                else:
                    path = self.repo / changed
                    original = path.read_text(encoding="utf-8")
                    path.write_text("Changed actual source commitment.\n", encoding="utf-8")
                self.assertIn("Effective candidate, original baseline, focus, or declared review sources changed; initialize a new ledger", ledger.validate_ledger(actual))
                if "focus" == changed:
                    self.basis_file.write_text(original_basis, encoding="utf-8")
                else:
                    path.write_text(original, encoding="utf-8")

    def test_source_and_range_fingerprints_are_recomputed(self):
        actual = self.create_valid_ledger()
        actual["files"][0]["coverage"][0]["anchors"][0]["range_sha256"] = "f" * 64
        self.assertIn(f"Source anchor is stale: candidate:{self.source_path}", ledger.validate_ledger(actual))

    def test_changed_unchanged_owner_evidence_invalidates_review(self):
        actual = self.create_valid_ledger()
        (self.repo / self.owner_path).write_text("Changed owner commitment.\n", encoding="utf-8")
        self.assertIn(f"Source anchor is stale: candidate:{self.owner_path}", ledger.validate_ledger(actual))

    def test_additional_source_evidence_invalidates_prior_passes(self):
        actual = self.create_valid_ledger()
        (self.repo / "new-owner.txt").write_text("Additional relevant owner evidence.\n", encoding="utf-8")
        actual["files"][0]["coverage"][0]["anchors"].append(ledger.capture_anchor({"side": "candidate", "path": "new-owner.txt"}, actual))
        errors = ledger.validate_ledger(actual)
        self.assertIn("Required review passes missing: root-cause, blast-radius, tests-runtime, convergence", errors)
        self.assertIn("Latest convergence pass uses a different review basis", errors)

    def test_metadata_only_change_preserves_discovery_pass_basis(self):
        actual = self.create_valid_ledger()
        actual["files"][0]["reason"] = "More precise wording with unchanged evidence."
        actual["review_revision"] += 1
        actual["passes"][-1]["review_revision"] = actual["review_revision"]
        self.assertEqual([], ledger.validate_ledger(actual))

    def test_add_pass_binds_basis_and_rejects_changed_candidate(self):
        actual = self.create_valid_ledger()
        args = Namespace(ledger=str(self.ledger_file), focus="convergence", new_findings=0)
        with redirect_stdout(StringIO()):
            self.assertEqual(0, ledger.cmd_add_pass(args))
        self.assertEqual(actual["passes"][-1]["basis_fingerprint"], ledger.read_ledger(self.ledger_file)["passes"][-1]["basis_fingerprint"])
        (self.repo / self.source_path).write_text("A changed candidate must not inherit passes.\n", encoding="utf-8")
        with self.assertRaisesRegex(RuntimeError, "initialize a new ledger"):
            ledger.cmd_add_pass(args)

    def test_same_effective_contents_match_local_and_committed_review_basis(self):
        local = self.create_valid_ledger()
        self.git("add", self.source_path)
        self.git("commit", "-qm", "Synthetic candidate")
        public = self.create_valid_ledger(committed=True)
        self.assertNotEqual(local["scope"]["head_sha"], public["scope"]["head_sha"])
        self.assertEqual(ledger.basis_fingerprint(local), ledger.basis_fingerprint(public))
        self.assertEqual([], ledger.validate_ledger(local))
        self.assertEqual([], ledger.validate_ledger(public))

    def test_rename_identity_does_not_depend_on_git_status_heuristics(self):
        renamed_path = "src/main/java/Renamed.java"
        (self.repo / self.source_path).rename(self.repo / renamed_path)
        self.candidate_files.write_text(self.source_path + "\n" + renamed_path + "\n", encoding="utf-8")
        local = self.create_valid_ledger()
        self.git("add", self.source_path, renamed_path)
        self.git("commit", "-qm", "Synthetic rename")
        public = self.create_valid_ledger(committed=True)
        self.assertEqual(ledger.basis_fingerprint(local), ledger.basis_fingerprint(public))
        self.assertEqual([], ledger.validate_ledger(public))

    def test_deleted_added_empty_binary_and_mode_changes_have_real_coverage(self):
        cases = ("deleted", "empty", "binary", "mode")
        for scenario in cases:
            with self.subTest(scenario=scenario):
                path = self.repo / self.source_path
                if not path.exists():
                    path.write_text("first\nold_first\nthird\nfourth\nfifth\nold_second\nlast\n", encoding="utf-8")
                path.chmod(0o644)
                if "deleted" == scenario:
                    path.unlink()
                elif "empty" == scenario:
                    path = self.repo / "empty.bin"
                    path.write_bytes(b"")
                elif "binary" == scenario:
                    path.write_bytes(b"\x00\xffchanged binary")
                else:
                    path.write_text("first\nold_first\nthird\nfourth\nfifth\nold_second\nlast\n", encoding="utf-8")
                    path.chmod(0o755)
                self.candidate_files.write_text(path.relative_to(self.repo).as_posix() + "\n", encoding="utf-8")
                actual = self.create_valid_ledger()
                self.assertEqual([], ledger.validate_ledger(actual))
                expected_kind = {"deleted": "text", "empty": "object", "binary": "binary", "mode": "mode"}[scenario]
                self.assertIn(expected_kind, {each["kind"] for each in actual["files"][0]["blocks"]})

    def test_confirmed_prose_requires_real_source_anchors(self):
        actual = self.create_valid_ledger()
        finding = self.create_confirmed_finding("F1")
        finding.update({"evidence": ["x"], "full_path": ["x"], "counter_evidence": ["x"], "necessity": "x", "scope_proof": "x", "anchors": []})
        actual["findings"] = [finding]
        self.assertIn("F1 confirmed finding is missing real source anchors", ledger.validate_ledger(actual))

    def test_deleted_file_replaced_by_directory_is_not_a_gitlink(self):
        path = self.repo / self.source_path
        path.unlink()
        path.mkdir()
        actual = self.create_valid_ledger()
        self.assertEqual({"mode": None, "sha256": None}, actual["files"][0]["candidate"])
        self.assertEqual("D", actual["files"][0]["git_status"])
        self.assertEqual([], ledger.validate_ledger(actual))
        child = self.repo / "container/child.txt"
        child.parent.mkdir()
        child.write_text("Tracked child.\n", encoding="utf-8")
        self.git("add", "container/child.txt")
        self.git("commit", "-qm", "Synthetic child baseline")
        self.base_sha = self.git("rev-parse", "HEAD").strip()
        child.unlink()
        child.parent.rmdir()
        child.parent.write_text("A parent file replaces the directory.\n", encoding="utf-8")
        self.candidate_files.write_text("container/child.txt\ncontainer\n", encoding="utf-8")
        actual = self.create_valid_ledger()
        self.assertEqual({"mode": None, "sha256": None}, actual["files"][0]["candidate"])
        self.assertEqual("D", actual["files"][0]["git_status"])
        self.assertEqual({"mode": None, "sha256": None}, actual["files"][1]["base"])
        self.assertEqual("100644", actual["files"][1]["candidate"]["mode"])
        self.assertEqual([], ledger.validate_ledger(actual))

    def test_symlink_evidence_uses_link_payload(self):
        path = self.repo / self.source_path
        path.unlink()
        path.symlink_to("../../missing-target")
        actual = self.create_valid_ledger()
        self.assertEqual("120000", actual["files"][0]["candidate"]["mode"])
        self.assertEqual([], ledger.validate_ledger(actual))

    def test_gitlink_head_change_invalidates_review_basis(self):
        checkout = self.create_gitlink_candidate()
        actual = self.create_valid_ledger()
        (checkout / "contract.txt").write_text("Third gitlink commit.\n", encoding="utf-8")
        self.git("-C", str(checkout), "add", "contract.txt")
        self.git("-C", str(checkout), "commit", "-qm", "Synthetic gitlink C")
        self.assertIn("Effective candidate, original baseline, focus, or declared review sources changed; initialize a new ledger", ledger.validate_ledger(actual))

    def test_same_gitlink_pointer_matches_local_and_committed_basis(self):
        checkout = self.create_gitlink_candidate()
        local = self.create_valid_ledger()
        pointer = self.git("-C", str(checkout), "rev-parse", "HEAD").strip()
        self.git("update-index", "--cacheinfo", "160000," + pointer + ",dependency")
        self.git("commit", "-qm", "Synthetic gitlink candidate")
        public = self.create_valid_ledger(committed=True)
        self.assertEqual("160000", local["files"][0]["candidate"]["mode"])
        self.assertEqual(local["files"][0]["candidate"], public["files"][0]["candidate"])
        self.assertEqual(ledger.basis_fingerprint(local), ledger.basis_fingerprint(public))
        self.assertEqual([], ledger.validate_ledger(local))
        self.assertEqual([], ledger.validate_ledger(public))

    def test_gitlink_cannot_read_parent_repository_head(self):
        checkout = self.create_gitlink_candidate()
        pointer = self.git("-C", str(checkout), "rev-parse", "HEAD").strip()
        self.git("update-index", "--cacheinfo", "160000," + pointer + ",dependency")
        (checkout / ".git").rename(self.private_root / "detached-child-git")
        with self.assertRaisesRegex(RuntimeError, "Gitlink checkout must have its own repository"):
            self.initialize_ledger()

    def test_dirty_gitlink_cannot_reuse_pointer_basis(self):
        checkout = self.create_gitlink_candidate()
        actual = self.create_valid_ledger()
        (checkout / "contract.txt").write_text("Uncommitted tracked changes.\n", encoding="utf-8")
        for staged in (False, True):
            with self.subTest(staged=staged):
                if staged:
                    self.git("-C", str(checkout), "add", "contract.txt")
                with self.assertRaisesRegex(RuntimeError, "Gitlink checkout has staged or unstaged tracked changes"):
                    ledger.validate_ledger(actual)

    def create_gitlink_candidate(self):
        checkout = self.repo / "dependency"
        checkout.mkdir()
        self.git("-C", str(checkout), "init", "-q")
        (checkout / "contract.txt").write_text("First gitlink commit.\n", encoding="utf-8")
        self.git("-C", str(checkout), "add", "contract.txt")
        self.git("-C", str(checkout), "commit", "-qm", "Synthetic gitlink A")
        pointer = self.git("-C", str(checkout), "rev-parse", "HEAD").strip()
        self.git("update-index", "--add", "--cacheinfo", "160000," + pointer + ",dependency")
        self.git("commit", "-qm", "Synthetic gitlink baseline")
        self.base_sha = self.git("rev-parse", "HEAD").strip()
        (checkout / "contract.txt").write_text("Second gitlink commit.\n", encoding="utf-8")
        self.git("-C", str(checkout), "add", "contract.txt")
        self.git("-C", str(checkout), "commit", "-qm", "Synthetic gitlink B")
        self.candidate_files.write_text("dependency\n", encoding="utf-8")
        return checkout

    def test_incomplete_gap_requires_real_attempt_proof(self):
        actual = self.create_valid_ledger()
        finding = self.create_incomplete_gap()
        finding["anchors"] = []
        actual["findings"] = [finding]
        self.assertIn("G1 incomplete gap is missing real unavailability-proof anchors", ledger.validate_ledger(actual, incomplete_result=True))

    def test_session_requires_each_report_evidence_review(self):
        original = self.create_session([[], []])
        self.assertEqual([], ledger.validate_session(original))
        for missing in ("report", "reason", "counter_evidence", "evidence", "current_anchor", "own_evidence", "valid_range"):
            with self.subTest(missing=missing):
                actual = json.loads(json.dumps(original))
                report_reviews = actual["rounds"][0]["report_reviews"]
                if "report" == missing:
                    report_reviews.pop()
                elif "current_anchor" == missing:
                    report_reviews[0]["evidence"][0]["anchor"]["range_sha256"] = "0" * 64
                elif "own_evidence" == missing:
                    report_reviews[0]["evidence"][0]["reviewer"] = "review-2"
                elif "valid_range" == missing:
                    report_reviews[0]["evidence"][0]["anchor"]["end_line"] = 99
                else:
                    report_reviews[0][missing] = [] if "evidence" == missing else " "
                if missing in {"reason", "counter_evidence", "evidence", "valid_range"}:
                    self.assert_session_cli_rejected(actual)
                else:
                    self.assertTrue(ledger.validate_session(actual))

    def test_session_normalizes_matching_root_causes(self):
        actual = self.create_session([[("F1", "confirmed")], [("F2", "confirmed")]])
        for review_round in actual["rounds"]:
            findings = review_round["findings"]
            findings[0]["members"].extend(findings.pop()["members"])
        self.assertEqual([], ledger.validate_session(actual))

    def test_session_accepts_verified_maintainer_decision(self):
        (self.repo / "Rules.md").write_text("The candidate's project commitment requires an explicit maintainer decision before merge.\n", encoding="utf-8")
        actual = self.create_session([[("F1", "confirmed")], [("F2", "confirmed")]])
        for index, reference in enumerate(actual["reviews"]):
            report = json.loads(Path(reference["path"]).read_text(encoding="utf-8"))
            actual_ledger = ledger.read_ledger(report["ledger"])
            rule_anchor = ledger.capture_anchor({"side": "source", "origin": "repository:Rules.md", "path": "Rules.md", "start_line": 1, "end_line": 1}, actual_ledger)
            actual_ledger["findings"][0].update({
                "origin": "repository:Rules.md", "fix_boundary": "maintainer-decision:project-commitment",
                "reason": "The evidenced project commitment needs a maintainer decision.",
                "necessity": "The project rule reserves acceptance of this commitment to maintainers.",
                "evidence": ["Rules.md explicitly requires a maintainer decision before merge."],
                "full_path": ["Reviewed candidate to project commitment to required maintainer decision."],
                "counter_evidence": ["No existing project rule delegates this decision to the reviewer."],
                "scope_proof": "The reviewed candidate is governed by the project commitment rule.", "anchors": [rule_anchor],
            })
            ledger_file = Path(report["ledger"])
            ledger.write_ledger(ledger_file, actual_ledger)
            report.update({"ledger_sha256": hashlib.sha256(ledger_file.read_bytes()).hexdigest(), "feedback_mode": "Needs Discussion"})
            self.save_session_report(actual, index, report)
        for review_round in actual["rounds"]:
            findings = review_round["findings"]
            findings[0]["members"].extend(findings.pop()["members"])
            findings[0].update({"id": "project-commitment-decision", "classification": "project-commitment",
                                "reason": "The confirmed project rule requires a maintainer decision.",
                                "counter_evidence": "Reviewer agreement cannot replace the required maintainer decision.",
                                "evidence": [{"reviewer": "review-2", "anchor": rule_anchor}]})
            review_round["feedback_mode"] = "Needs Discussion"
        actual["feedback_mode"] = "Needs Discussion"
        self.assertEqual([], ledger.validate_session(actual))

    def test_session_preserves_all_candidate_dispositions(self):
        original = self.create_session([[("F1", "withdrawn"), ("F2", "non-blocking"), ("F3", "out-of-scope")],
                                        [], [], [], [], []])
        self.assertEqual([], ledger.validate_session(original))
        for missing in ("member", "finding", "disposition"):
            with self.subTest(missing=missing):
                actual = json.loads(json.dumps(original))
                findings = actual["rounds"][0]["findings"]
                if "member" == missing:
                    findings[0]["members"] = []
                elif "finding" == missing:
                    findings.pop()
                else:
                    actual["rounds"][-1]["findings"][0]["status"] = "candidate"
                self.assertTrue(ledger.validate_session(actual))

    def test_session_rejects_same_count_with_different_findings_or_feedback(self):
        actual = self.create_session([[("F1", "confirmed")], [("F2", "confirmed")]])
        self.assertTrue(ledger.validate_session(actual))
        for review_round in actual["rounds"]:
            review_round["findings"][0]["members"].extend(review_round["findings"].pop()["members"])
        report = json.loads(Path(actual["reviews"][-1]["path"]).read_text(encoding="utf-8"))
        report["feedback_mode"] = "Needs Discussion"
        self.save_session_report(actual, len(actual["reviews"]) - 1, report)
        self.assertTrue(ledger.validate_session(actual))

    def test_session_allows_second_pair_after_disagreement(self):
        actual = self.create_session([[("F1", "confirmed")], [], [("F1", "confirmed")], [("F1", "confirmed")]])
        self.assertEqual([], ledger.validate_session(actual))

    def test_session_preserves_finding_discovered_during_result_review(self):
        original = self.create_session([[], [], [("F1", "confirmed")], [("F1", "confirmed")]])
        original["rounds"][0]["findings"].append({
            "id": "normalized-F1", "members": [], "status": "confirmed",
            "classification": "correctness",
            "reason": "The report evidence review found an unsupported triggering path.",
            "counter_evidence": "The supported alternative does not protect this path.",
            "evidence": original["rounds"][0]["report_reviews"][0]["evidence"],
        })
        original["rounds"][0].update({"result": "Not Mergeable", "feedback_mode": "Change Request"})
        first_pair = json.loads(json.dumps(original))
        first_pair.update({"reviews": first_pair["reviews"][:2], "rounds": first_pair["rounds"][:1], "result": "Mergeable", "feedback_mode": ""})
        self.assertTrue(ledger.validate_session(first_pair))
        missing_identity = json.loads(json.dumps(original))
        missing_identity["rounds"][1]["findings"][0]["id"] = "replacement-F1"
        self.assertTrue(ledger.validate_session(missing_identity))
        self.assertEqual([], ledger.validate_session(original))

    def test_session_requires_historical_findings_in_final_pair(self):
        original = self.create_session([[("F1", "confirmed")], [("F1", "confirmed"), ("F2", "confirmed")],
                                        [("F1", "confirmed")], [("F1", "confirmed")],
                                        [("F1", "confirmed")], [("F1", "confirmed")]])
        self.assertTrue(ledger.validate_session(original))
        actual = json.loads(json.dumps(original))
        actual["rounds"][-1]["findings"].pop()
        self.assertTrue(ledger.validate_session(actual))
        actual = json.loads(json.dumps(original))
        for review_round in actual["rounds"][-2:]:
            review_round["findings"][-1]["status"] = "withdrawn"
        self.assertEqual([], ledger.validate_session(actual))

    def test_session_allows_more_than_two_complete_pairs(self):
        actual = self.create_session([[("F1", "confirmed")], [], [("F1", "confirmed")], [("F1", "confirmed")],
                                      [("F1", "confirmed")], [("F1", "confirmed")]])
        actual["rounds"][-1]["findings"][0].update({"reason": "The fresh source evidence confirms the same triggering path.",
                                                   "evidence": actual["rounds"][-1]["report_reviews"][0]["evidence"]})
        self.assertEqual([], ledger.validate_session(actual))

    def test_session_requires_confirmation_after_first_round(self):
        actual = self.create_session([[], []])
        actual.update({"reviews": actual["reviews"][:2], "rounds": actual["rounds"][:1]})
        self.assertTrue(ledger.validate_session(actual))

    def test_session_requires_stable_complete_dispositions(self):
        original = self.create_session([[("F1", "withdrawn")]] * 6)
        for field, value in (("classification", "performance"), ("status", "non-blocking")):
            with self.subTest(field=field):
                actual = json.loads(json.dumps(original))
                actual["rounds"][-1]["findings"][0][field] = value
                self.assertTrue(ledger.validate_session(actual))

    def test_session_requires_stable_feedback_mode(self):
        actual = self.create_session([[("F1", "confirmed")], [("F1", "confirmed")]])
        actual["rounds"][-2]["feedback_mode"] = "Needs Discussion"
        self.assertTrue(ledger.validate_session(actual))

    def test_session_requires_final_closed_dispositions(self):
        for status in ("withdrawn", "non-blocking", "out-of-scope"):
            with self.subTest(status=status):
                actual = self.create_session([[("F1", "withdrawn")], [("F1", status)]])
                for reference in actual["reviews"]:
                    report = json.loads(Path(reference["path"]).read_text(encoding="utf-8"))
                    self.assertEqual([], ledger.validate_ledger(ledger.read_ledger(report["ledger"])))
                errors = ledger.validate_session(actual)
                if "withdrawn" == status:
                    self.assertEqual([], errors)
                else:
                    self.assertIn("The latest pair does not agree with the result review and all historical findings", errors)

    def test_session_rejects_repeated_unconfirmed_adjudication(self):
        actual = self.create_session([[("F1", "confirmed")], []])
        self.assertTrue(ledger.validate_session(actual))

    def test_session_requires_complete_review_pairs(self):
        original = self.create_session([[], []])
        for count in (0, 1, 3):
            with self.subTest(count=count):
                actual = json.loads(json.dumps(original))
                actual["reviews"] = (actual["reviews"] * 2)[:count]
                with self.assertRaises(RuntimeError):
                    ledger.validate_session(actual)

    def test_session_rejects_reused_or_modified_original_records(self):
        original = self.create_session([[], []])
        original_report = Path(original["reviews"][1]["path"]).read_text(encoding="utf-8")
        for invalid in ("report_path", "reviewer", "ledger_path", "report_hash", "ledger_hash"):
            with self.subTest(invalid=invalid):
                actual = json.loads(json.dumps(original))
                report = json.loads(original_report)
                if "report_path" == invalid:
                    actual["reviews"][1] = actual["reviews"][0]
                elif "report_hash" == invalid:
                    actual["reviews"][1]["sha256"] = "0" * 64
                else:
                    if "reviewer" == invalid:
                        report["reviewer"] = "review-1"
                        actual["rounds"][0]["report_reviews"][1]["reviewer"] = "review-1"
                        actual["rounds"][0]["report_reviews"][1]["evidence"][0]["reviewer"] = "review-1"
                    elif "ledger_path" == invalid:
                        report["ledger"] = json.loads(Path(actual["reviews"][0]["path"]).read_text(encoding="utf-8"))["ledger"]
                        report["ledger_sha256"] = hashlib.sha256(Path(report["ledger"]).read_bytes()).hexdigest()
                    else:
                        report["ledger_sha256"] = "0" * 64
                    self.save_session_report(actual, 1, report)
                self.assert_session_cli_rejected(actual)
                Path(original["reviews"][1]["path"]).write_text(original_report, encoding="utf-8")

    def test_session_requires_same_current_review_basis(self):
        actual = self.create_session([[], []])
        report = json.loads(Path(actual["reviews"][1]["path"]).read_text(encoding="utf-8"))
        original = ledger.read_ledger(report["ledger"])
        for changed in ("focus", "rule", "requirement", "evidence_origin"):
            with self.subTest(changed=changed):
                actual_ledger = json.loads(json.dumps(original))
                basis = json.loads(self.basis_file.read_text(encoding="utf-8"))
                if "focus" == changed:
                    basis["focus"] = "CI Review"
                else:
                    role = "evidence" if "evidence_origin" == changed else changed
                    source = next(each for each in basis["sources"] if role == each["role"])
                    alternate = self.private_root / (changed + ".txt")
                    alternate.write_text("A different reviewed source contract.\n", encoding="utf-8")
                    source["path"] = str(alternate)
                self.update_session_basis(actual, 1, actual_ledger, basis)
                self.assertEqual([], ledger.validate_ledger(actual_ledger))
                self.assertTrue(ledger.validate_session(actual))
        self.update_session_basis(actual, 1, original, json.loads(self.basis_file.read_text(encoding="utf-8")))
        (self.repo / self.source_path).write_text("first\nchanged_after_review\nthird\nfourth\nfifth\nnew_second\nlast\n", encoding="utf-8")
        self.assertTrue(ledger.validate_session(actual))

    def test_session_accepts_different_discovered_evidence(self):
        actual = self.create_session([[], []])
        report = json.loads(Path(actual["reviews"][1]["path"]).read_text(encoding="utf-8"))
        actual_ledger = ledger.read_ledger(report["ledger"])
        basis = json.loads(self.basis_file.read_text(encoding="utf-8"))
        evidence = self.private_root / "additional-evidence.txt"
        evidence.write_text("Additional independent discovery evidence.\n", encoding="utf-8")
        basis["sources"].append({"role": "evidence", "origin": "discovery:second-review", "path": str(evidence)})
        self.update_session_basis(actual, 1, actual_ledger, basis)
        self.assertEqual([], ledger.validate_session(actual))

    def test_session_preserves_original_baseline_across_basis_changes(self):
        historical = self.create_session([[], []])
        self.assertEqual([], ledger.validate_session(historical))
        self.git("add", self.source_path)
        self.git("commit", "-qm", "Synthetic candidate baseline")
        self.base_sha = self.git("rev-parse", "HEAD").strip()
        (self.repo / self.source_path).write_text("first\nnext_first\nthird\nfourth\nfifth\nnew_second\nlast\n", encoding="utf-8")
        current = self.create_session([[], []], review_start=len(historical["reviews"]))
        self.assertEqual([], ledger.validate_session(current))
        actual = {**current, "reviews": historical["reviews"] + current["reviews"],
                  "rounds": historical["rounds"] + current["rounds"]}
        self.assertIn("The original task baseline changed across review rounds", ledger.validate_session(actual))

    def test_session_reconfirms_changed_basis_and_retains_history(self):
        for changed in ("candidate", "evidence"):
            with self.subTest(changed=changed):
                historical = self.create_session([[("F1", "confirmed")], [("F1", "confirmed")]])
                if "candidate" == changed:
                    (self.repo / self.source_path).write_text("first\nfixed_first\nthird\nfourth\nfifth\nnew_second\nlast\n", encoding="utf-8")
                else:
                    self.attempt_log.write_text("Synthetic evidence route: target version is now available and the original assumption is disproved.\n", encoding="utf-8")
                current = self.create_session([[], []], review_start=len(historical["reviews"]))
                for review_round in current["rounds"]:
                    review_round["findings"] = json.loads(json.dumps(historical["rounds"][-1]["findings"]))
                    for finding in review_round["findings"]:
                        finding.update({"status": "withdrawn", "reason": "Current evidence disproves the historical triggering path.",
                                        "evidence": review_round["report_reviews"][0]["evidence"]})
                actual = {**current, "reviews": historical["reviews"] + current["reviews"],
                          "rounds": historical["rounds"] + current["rounds"]}
                pending = {**actual, "reviews": actual["reviews"][:-2], "rounds": actual["rounds"][:-1]}
                self.assertTrue(ledger.validate_session(pending))
                self.assertEqual([], ledger.validate_session(actual))
                original_report = Path(historical["reviews"][0]["path"])
                original_report.write_bytes(original_report.read_bytes() + b"\n")
                self.assert_session_cli_rejected(actual)

    def test_session_preserves_real_incomplete_evidence(self):
        actual = self.create_session([[("G1", "review-incomplete-gap")], [("G1", "review-incomplete-gap")]])
        self.assertEqual([], ledger.validate_session(actual))
        actual = self.create_session([[], []])
        actual["result"] = "Review Incomplete"
        self.assertTrue(ledger.validate_session(actual))

    def test_session_cli_validates_without_writing_and_preserves_legacy_validation(self):
        actual = self.create_session([[], []])
        session_file = self.private_root / "session.json"
        session_file.write_text(json.dumps(actual), encoding="utf-8")
        original_files = {path: path.read_bytes() for path in self.private_root.rglob("*.json")}
        for command in (["validate-session", "--session", str(session_file)],
                        ["validate", "--ledger", json.loads(Path(actual["reviews"][0]["path"]).read_text(encoding="utf-8"))["ledger"]]):
            with self.subTest(command=command[0]), patch.object(sys, "argv", ["review_ledger.py", *command]), redirect_stdout(StringIO()):
                self.assertEqual(0, ledger.main())
        self.assertEqual(original_files, {path: path.read_bytes() for path in original_files})
        original = json.loads(json.dumps(actual))
        for invalid in ("unfinished", "shape", "result", "feedback_mode", "finding_status", "classification", "round_result", "round_feedback"):
            with self.subTest(invalid=invalid):
                actual = json.loads(json.dumps(original))
                if "unfinished" == invalid:
                    actual["rounds"][0]["report_reviews"] = []
                elif "shape" == invalid:
                    actual = {"reviews": "invalid"}
                elif "finding_status" == invalid:
                    actual["rounds"][0]["findings"] = [{"id": "invalid", "members": [], "status": []}]
                elif "classification" == invalid:
                    for review_round in actual["rounds"]:
                        review_round["findings"] = [{"id": "coordinator-candidate", "members": [], "status": "withdrawn", "classification": " ",
                                                     "reason": "The current owner evidence disproves this candidate.",
                                                     "counter_evidence": "The alternate triggering path is unsupported.",
                                                     "evidence": review_round["report_reviews"][0]["evidence"]}]
                elif invalid in {"round_result", "round_feedback"}:
                    actual["rounds"][0]["result" if "round_result" == invalid else "feedback_mode"] = []
                else:
                    actual[invalid] = []
                session_file.write_text(json.dumps(actual), encoding="utf-8")
                with patch.object(sys, "argv", ["review_ledger.py", "validate-session", "--session", str(session_file)]), \
                        redirect_stdout(StringIO()), redirect_stderr(StringIO()):
                    self.assertEqual(1, ledger.main())

    def create_session(self, review_findings, review_start=0):
        if 2 == len(review_findings):
            review_findings = review_findings * 2
        original = self.create_valid_ledger()
        actual = {"reviews": [], "rounds": [], "result": "Mergeable", "feedback_mode": ""}
        findings = {}
        report_reviews = []
        for index, each in enumerate(review_findings):
            reviewer = "review-" + str(review_start + index + 1)
            actual_ledger = json.loads(json.dumps(original))
            for finding_id, status in each:
                finding = self.create_incomplete_gap() if "review-incomplete-gap" == status else self.create_confirmed_finding(finding_id)
                finding.update({"id": finding_id, "status": status, "fix_boundary": finding_id})
                actual_ledger["findings"].append(finding)
            ledger_file = ledger.ledger_root() / reviewer / ledger.LEDGER_FILE_NAME
            ledger_file.parent.mkdir(exist_ok=True)
            ledger.write_ledger(ledger_file, actual_ledger)
            statuses = {finding["status"] for finding in actual_ledger["findings"]}
            result = "Review Incomplete" if "review-incomplete-gap" in statuses else "Not Mergeable" if "confirmed" in statuses else "Mergeable"
            report = {"reviewer": reviewer, "ledger": str(ledger_file), "ledger_sha256": hashlib.sha256(ledger_file.read_bytes()).hexdigest(),
                      "result": result, "feedback_mode": "Change Request" if "Not Mergeable" == result else ""}
            actual["reviews"].append({"path": str(self.private_root / (reviewer + ".json")), "sha256": ""})
            self.save_session_report(actual, index, report)
            evidence = [{"reviewer": reviewer, "anchor": actual_ledger["files"][0]["coverage"][0]["anchors"][0]}]
            report_reviews.append({"reviewer": reviewer, "reason": "Checked the report against its owning source contract.",
                                   "counter_evidence": "Checked the supported alternative behavior.", "evidence": evidence})
            for finding in actual_ledger["findings"]:
                disposition = findings.setdefault(finding["id"], {"id": "normalized-" + finding["id"], "members": [], "status": finding["status"],
                                                                  "classification": "correctness",
                                                                  "reason": "Checked the triggering path and independent fix boundary.",
                                                                  "counter_evidence": "Checked the strongest supported counterexample.", "evidence": evidence})
                disposition["members"].append({"reviewer": reviewer, "finding": finding["id"]})
            if index % 2:
                round_statuses = {finding["status"] for finding in findings.values()}
                result = "Review Incomplete" if "review-incomplete-gap" in round_statuses else "Not Mergeable" if "confirmed" in round_statuses else "Mergeable"
                actual["rounds"].append({"report_reviews": report_reviews, "findings": json.loads(json.dumps(list(findings.values()))),
                                         "result": result, "feedback_mode": "Change Request" if "Not Mergeable" == result else ""})
                report_reviews = []
                actual.update({"result": result, "feedback_mode": actual["rounds"][-1]["feedback_mode"]})
        return actual

    def save_session_report(self, session, index, report):
        path = Path(session["reviews"][index]["path"])
        path.write_text(json.dumps(report), encoding="utf-8")
        session["reviews"][index]["sha256"] = hashlib.sha256(path.read_bytes()).hexdigest()

    def assert_session_cli_rejected(self, session):
        session_file = self.private_root / "rejected-session.json"
        session_file.write_text(json.dumps(session), encoding="utf-8")
        output = StringIO()
        with patch.object(sys, "argv", ["review_ledger.py", "validate-session", "--session", str(session_file)]), \
                redirect_stdout(output), redirect_stderr(StringIO()):
            self.assertEqual(1, ledger.main())
        self.assertNotIn("OK:", output.getvalue())

    def update_session_basis(self, session, index, actual_ledger, basis):
        basis_file = self.private_root / ("independent-basis-" + str(index) + ".json")
        basis_file.write_text(json.dumps(basis), encoding="utf-8")
        actual_ledger["scope"]["review_basis_file"] = str(basis_file)
        actual_ledger["basis"], _ = ledger.capture_basis(actual_ledger["scope"])
        for review_pass in actual_ledger["passes"]:
            review_pass["basis_fingerprint"] = ledger.basis_fingerprint(actual_ledger)
        report = json.loads(Path(session["reviews"][index]["path"]).read_text(encoding="utf-8"))
        ledger_file = Path(report["ledger"])
        ledger.write_ledger(ledger_file, actual_ledger)
        report["ledger_sha256"] = hashlib.sha256(ledger_file.read_bytes()).hexdigest()
        self.save_session_report(session, index, report)

    def initialize_ledger(self, committed=False):
        arguments = ["init", "--repo-root", str(self.repo), "--pr", "synthetic-review", "--base-ref", self.base_sha,
                     "--head-ref", "HEAD", "--review-basis", str(self.basis_file)]
        if not committed:
            arguments.extend(["--candidate-files", str(self.candidate_files)])
        output = StringIO()
        with redirect_stdout(output):
            ledger.cmd_init(ledger.build_parser().parse_args(arguments))
        self.ledger_file = Path(output.getvalue().splitlines()[0].removeprefix("ledger="))
        return ledger.read_ledger(self.ledger_file)

    def create_valid_ledger(self, committed=False):
        actual = self.initialize_ledger(committed)
        for entry in actual["files"]:
            coverage = []
            for block in entry["blocks"]:
                side = "candidate" if entry["candidate"]["mode"] else "base"
                if "text" == block["kind"] and not block[f"{side}_count"]:
                    side = "base"
                anchor = {"side": side, "path": block[f"{side}_path"]}
                if "text" == block["kind"]:
                    anchor.update({"start_line": block[f"{side}_start"], "end_line": block[f"{side}_start"] + block[f"{side}_count"] - 1})
                coverage.append({"block": block["id"], "behavior": "Review the changed owning behavior.", "risk_axes": ["correctness"],
                                 "requirements": ["repository:Rules.md", "user:synthetic-task"], "counter_evidence": "Check the stable owner contract.",
                                 "anchors": [anchor, {"side": "candidate", "path": self.owner_path, "start_line": 1, "end_line": 1}]})
            coverage_file = self.private_root / "coverage.json"
            coverage_file.write_text(json.dumps(coverage), encoding="utf-8")
            args = ledger.build_parser().parse_args(["mark-file", "--ledger", str(self.ledger_file), "--path", entry["path"], "--status", "reviewed",
                                                     "--cluster", "core", "--risk-axis", "correctness", "--coverage", str(coverage_file)])
            with redirect_stdout(StringIO()):
                ledger.cmd_mark_file(args)
        actual = ledger.read_ledger(self.ledger_file)
        actual["passes"] = [{"focus": focus, "new_findings": 0, "review_revision": actual["review_revision"],
                             "basis_fingerprint": ledger.basis_fingerprint(actual)} for focus in ledger.PASS_FOCUSES]
        ledger.write_ledger(self.ledger_file, actual)
        return actual

    def create_confirmed_finding(self, finding_id):
        return {
            "id": finding_id,
            "status": "confirmed",
            "origin": "current head",
            "fix_boundary": "shared boundary",
            "evidence": ["public evidence"],
            "full_path": ["entry to result"],
            "counter_evidence": ["strongest counterexample"],
            "necessity": "required for correctness",
            "scope_proof": "introduced by reviewed change",
            "files": ["src/main/java/Foo.java"],
            "reason": "",
            "anchors": ledger.read_ledger(self.ledger_file)["files"][0]["coverage"][0]["anchors"],
        }

    def create_incomplete_gap(self):
        return {
            "id": "G1",
            "status": "review-incomplete-gap",
            "origin": "target tool version owner",
            "fix_boundary": "",
            "evidence": ["Authenticated source and local metadata were both unavailable."],
            "full_path": ["configuration input to version-specific behavior"],
            "counter_evidence": ["Repository metadata and public release records checked."],
            "necessity": "The missing fact can change whether the behavior is correct.",
            "scope_proof": "The reviewed file invokes the version-specific behavior.",
            "files": ["src/main/java/Foo.java"],
            "reason": "The target tool version is unavailable.",
            "anchors": [ledger.capture_anchor({"side": "source", "path": str(self.attempt_log), "origin": "attempt:target-version", "start_line": 1, "end_line": 1},
                                               ledger.read_ledger(self.ledger_file))],
        }


if __name__ == "__main__":
    unittest.main()
