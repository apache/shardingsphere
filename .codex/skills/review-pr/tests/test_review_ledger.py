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

import sys
import tempfile
import unittest
from argparse import Namespace
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path
from unittest.mock import patch


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import review_ledger as ledger


class ReviewLedgerTest(unittest.TestCase):

    def test_complete_ledger_is_valid(self):
        self.assertEqual([], ledger.validate_ledger(self.create_valid_ledger()))

    def test_confirmed_finding_requires_non_blank_proof(self):
        actual_ledger = self.create_valid_ledger()
        actual_ledger["findings"] = [{
            "id": "F1",
            "status": "confirmed",
            "origin": " ",
            "fix_boundary": "boundary",
            "evidence": [""],
            "full_path": [" "],
            "counter_evidence": [],
            "necessity": " ",
            "scope_proof": " ",
            "files": [""],
            "reason": "",
        }]
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
        actual_ledger["findings"] = [{
            "id": "G1",
            "status": "review-incomplete-gap",
            "origin": " ",
            "fix_boundary": "",
            "evidence": [""],
            "full_path": [" "],
            "counter_evidence": [],
            "necessity": " ",
            "scope_proof": " ",
            "files": [""],
            "reason": " ",
        }]
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

    @staticmethod
    def create_valid_ledger():
        review_passes = [{
            "focus": focus,
            "new_findings": 0,
            "review_revision": 1,
        } for focus in ledger.PASS_FOCUSES]
        return {
            "kind": ledger.LEDGER_KIND,
            "version": ledger.LEDGER_VERSION,
            "scope": {"github_files": {"provided": False}},
            "files": [{
                "path": "src/main/java/Foo.java",
                "status": "reviewed",
                "clusters": ["core"],
                "risk_axes": ["correctness"],
                "findings": [],
                "reason": "",
            }],
            "findings": [],
            "passes": review_passes,
            "review_revision": 1,
        }

    @staticmethod
    def create_confirmed_finding(finding_id):
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
        }

    @staticmethod
    def create_incomplete_gap():
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
        }


if __name__ == "__main__":
    unittest.main()
