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
from pathlib import Path
from unittest.mock import patch


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import resolve_review_scope as common
from resolve_review_scope import ChangedFile, categorize, compare_github_files, parse_name_status


class ResolveReviewScopeTest(unittest.TestCase):

    def test_parse_name_status(self):
        actual = parse_name_status("M\tmodule/Foo.java\nR100\told.md\tnew.md\nC75\tsource.yml\tcopy.yml\n")
        self.assertEqual([
            ChangedFile(status="M", path="module/Foo.java"),
            ChangedFile(status="R100", path="new.md", old_path="old.md"),
            ChangedFile(status="C75", path="copy.yml", old_path="source.yml"),
        ], actual)

    def test_categorize_root_and_module_paths(self):
        self.assertEqual("production-java", categorize("src/main/java/Foo.java"))
        self.assertEqual("production-java", categorize("module/src/main/java/Foo.java"))
        self.assertEqual("tests", categorize("src/test/java/FooTest.java"))
        self.assertEqual("tests", categorize("module/src/test/resources/case.yaml"))
        self.assertEqual("build-config", categorize(".github/README.md"))
        self.assertEqual("docs", categorize("docs/community/index.md"))

    def test_resolve_candidate_changes_includes_untracked_files(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            candidate_file = Path(temp_dir) / "candidate.txt"
            candidate_file.write_text("AGENTS.md\n.codex/new.toml\n", encoding="utf-8")
            with patch.object(common, "run_git", side_effect=["M\tAGENTS.md\n", ".codex/new.toml\n"]) as run_git:
                actual = common.resolve_candidate_changes(Path("/repo"), "baseline", str(candidate_file))
        self.assertEqual([common.ChangedFile(status="M", path="AGENTS.md"),
                          common.ChangedFile(status="A", path=".codex/new.toml")], actual)
        literal_paths = [":(literal)AGENTS.md", ":(literal).codex/new.toml"]
        run_git.assert_any_call(["diff", "--name-status", "baseline", "--", *literal_paths], Path("/repo"))
        run_git.assert_any_call(["ls-files", "--others", "--exclude-standard", "--", *literal_paths], Path("/repo"), allow_empty=True)

    def test_resolve_candidate_changes_rejects_unchanged_paths(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            candidate_file = Path(temp_dir) / "candidate.txt"
            candidate_file.write_text("AGENTS.md\n", encoding="utf-8")
            with patch.object(common, "run_git", side_effect=["", ""]), self.assertRaisesRegex(RuntimeError, "not changed"):
                common.resolve_candidate_changes(Path("/repo"), "baseline", str(candidate_file))

    def test_compare_github_files_reports_bounded_differences(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            github_files = Path(temp_dir) / "github-files.txt"
            github_files.write_text("a.md\nb.md\nc.md\n", encoding="utf-8")
            actual = compare_github_files(["a.md", "d.md", "e.md"], str(github_files), limit=1)
        self.assertFalse(actual["matched"])
        self.assertEqual(3, actual["github_count"])
        self.assertEqual(3, actual["local_count"])
        self.assertEqual(["b.md"], actual["only_in_github"])
        self.assertEqual(["d.md"], actual["only_in_local"])


if __name__ == "__main__":
    unittest.main()
