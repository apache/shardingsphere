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

import io
import json
import os
import subprocess
import sys
import tempfile
import unittest
from argparse import Namespace
from contextlib import redirect_stdout
from pathlib import Path
from unittest.mock import patch


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import build_review_inventory as inventory
import resolve_review_scope as common


class BuildReviewInventoryTest(unittest.TestCase):

    @patch.object(inventory, "run_git")
    @patch.object(inventory, "get_repo_root", return_value=Path("/repo"))
    def test_build_inventory_uses_triple_dot_scope(self, _, run_git):
        run_git.side_effect = [
            "base-sha\n",
            "head-sha\n",
            "merge-base\n",
            "M\tsrc/main/java/Foo.java\nA\tdocs/guide.md\n",
            " M unrelated.java\n",
        ]
        args = Namespace(base_ref="base", head_ref="head", previous_head=None, github_files=None, candidate_files=None, file_snapshots=None)
        actual = inventory.build_inventory(args)
        self.assertEqual("merge-base", actual["scope"]["merge_base"])
        self.assertEqual(2, actual["scope"]["changed_file_count"])
        self.assertEqual(1, actual["dirty_worktree"]["count"])
        self.assertEqual("src/main/java/Foo.java", actual["changed_files_by_category"]["production-java"][0]["path"])
        run_git.assert_any_call(["diff", "--name-status", "merge-base..head"], Path("/repo"))

    @patch.object(inventory, "resolve_candidate_changes")
    @patch.object(inventory, "run_git")
    @patch.object(inventory, "get_repo_root", return_value=Path("/repo"))
    def test_build_inventory_uses_exact_candidate_files(self, _, run_git, resolve_candidate_changes):
        run_git.side_effect = ["base-sha\n", "head-sha\n", "merge-base\n", " M unrelated.java\n"]
        resolve_candidate_changes.return_value = [common.ChangedFile(status="M", path="AGENTS.md")]
        args = Namespace(base_ref="base", head_ref="head", previous_head=None, github_files=None, candidate_files="candidate.txt", file_snapshots=None)
        actual = inventory.build_inventory(args)
        self.assertEqual(1, actual["scope"]["changed_file_count"])
        self.assertEqual({"provided": True}, actual["scope"]["candidate_files"])
        self.assertEqual("AGENTS.md", actual["changed_files_by_category"]["docs"][0]["path"])
        resolve_candidate_changes.assert_called_once_with(Path("/repo"), "merge-base", "candidate.txt")

    def test_snapshot_inventory_covers_mixed_delivery_paths_without_git(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source = root / "repo/src/main/java/Foo.java"
            global_rules, unchanged = root / "global/AGENTS.md", root / "unchanged.md"
            source.parent.mkdir(parents=True)
            global_rules.parent.mkdir()
            source.write_bytes(b"Original source.\n")
            global_rules.write_bytes(b"Original rules.\n")
            unchanged.write_bytes(b"Unchanged.\n")
            snapshot = root / "original.json"
            common.capture_file_snapshots([str(source), str(global_rules), str(unchanged)], snapshot)
            source.write_bytes(b"Candidate source.\n")
            global_rules.write_bytes(b"Candidate rules.\n")
            args = Namespace(file_snapshots=str(snapshot), base_ref=None, head_ref=None, previous_head=None, github_files=None, candidate_files=None)
            with patch.object(inventory, "get_repo_root", side_effect=AssertionError("Snapshot inventory must not resolve Git")), \
                    patch.object(inventory, "run_git", side_effect=AssertionError("Snapshot inventory must not call Git")):
                actual = inventory.build_inventory(args)
            self.assertEqual("file-snapshots", actual["scope"]["mode"])
            self.assertEqual(2, actual["scope"]["changed_file_count"])
            self.assertEqual([{"status": "M", "path": str(source), "old_path": None}], actual["changed_files_by_category"]["production-java"])
            self.assertEqual([{"status": "M", "path": str(global_rules), "old_path": None}], actual["changed_files_by_category"]["docs"])
            self.assertIsNone(actual["dirty_worktree"])
            self.assertNotIn("merge_base", actual["scope"])
            rendered = inventory.render_markdown(actual)
            self.assertIn(str(snapshot), rendered)
            self.assertNotIn("Merge base:", rendered)

    def test_snapshot_inventory_rejects_git_scope_options(self):
        for option in ("base_ref", "head_ref", "previous_head", "github_files", "candidate_files"):
            with self.subTest(option=option):
                arguments = {"file_snapshots": "original.json", "base_ref": None, "head_ref": None, "previous_head": None,
                             "github_files": None, "candidate_files": None, option: "git-input"}
                with self.assertRaisesRegex(RuntimeError, "cannot be combined"):
                    inventory.build_inventory(Namespace(**arguments))

    def test_snapshot_inventory_command_needs_no_git_refs(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source, snapshot = root / "delivery.md", root / "original.json"
            common.capture_file_snapshots([str(source)], snapshot)
            source.write_bytes(b"New delivery.\n")
            output = io.StringIO()
            with patch.object(sys, "argv", ["build_review_inventory.py", "--file-snapshots", str(snapshot), "--format", "json"]), redirect_stdout(output):
                actual = inventory.main()
            self.assertEqual(0, actual)
            document = json.loads(output.getvalue())
            self.assertEqual([{"status": "A", "path": str(source), "old_path": None}], document["changed_files_by_category"]["docs"])

    def test_default_inventory_command_leaves_no_repository_bytecode(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            repo = root / "repo"
            scripts = repo / ".codex/skills/review-pr/scripts"
            scripts.mkdir(parents=True)
            for name in ("build_review_inventory.py", "resolve_review_scope.py"):
                (scripts / name).write_bytes((SCRIPTS_DIR / name).read_bytes())
            source, snapshot = repo / "report.md", root / "original.json"
            source.write_bytes(b"Original report.\n")
            common.capture_file_snapshots([str(source)], snapshot)
            source.write_bytes(b"Reviewed report.\n")
            environment = {key: value for key, value in os.environ.items() if key not in {"PYTHONDONTWRITEBYTECODE", "PYTHONPYCACHEPREFIX"}}
            process = subprocess.run([sys.executable, str(scripts / "build_review_inventory.py"), "--file-snapshots", str(snapshot), "--format", "json"],
                                     cwd=repo, env=environment, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True, timeout=20)
            actual = json.loads(process.stdout)
            self.assertEqual([{"status": "M", "path": str(source), "old_path": None}], actual["changed_files_by_category"]["docs"])
            self.assertEqual([], list(repo.rglob("*.pyc")))


if __name__ == "__main__":
    unittest.main()
