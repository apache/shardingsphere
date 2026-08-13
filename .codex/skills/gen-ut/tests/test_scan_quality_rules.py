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

import json
import subprocess
import sys
import tempfile
import unittest
from dataclasses import asdict
from pathlib import Path


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import scan_quality_rules as rules


class ScanQualityRulesTest(unittest.TestCase):

    def test_candidate_count_requires_manual_decision(self):
        source = """
            class TargetTest {
                @Test void assertTargetFirst() { assertTrue(target.run(1)); }
                @Test void assertTargetSecond() { assertFalse(target.run(2)); }
                @Test void assertTargetThird() { assertThat(target.run(3), is(expected)); }
            }
        """
        candidates = rules.analyze_parameterization_candidates(Path("TargetTest.java"), source, {"run"})
        self.assertEqual(1, len(candidates))
        self.assertNotIn("high_fit", asdict(candidates[0]))
        self.assertIn("decision=manual-review-required", rules.describe_candidate(asdict(candidates[0])))

    def test_non_target_invocation_is_not_candidate(self):
        source = """
            class TargetTest {
                @Test void assertFirst() { assertTrue(Collections.singleton("a").isEmpty()); }
                @Test void assertSecond() { assertTrue(Collections.singleton("b").isEmpty()); }
                @Test void assertThird() { assertTrue(Collections.singleton("c").isEmpty()); }
            }
        """
        self.assertEqual([], rules.analyze_parameterization_candidates(Path("TargetTest.java"), source, {"run"}))

    def test_external_method_source_requires_review_instead_of_failure(self):
        source = """
            class TargetTest {
                @ParameterizedTest(name = "{0}")
                @MethodSource("Samples#cases")
                void assertRun(final String name, final int value) {
                    assertThat(target.run(value), is(value));
                }
            }
        """
        method_bodies = {name: body for name, (_, body) in rules.parse_method_blocks(source).items()}
        self.assertEqual([], rules.check_r15_d(Path("TargetTest.java"), source, method_bodies))
        reviews = rules.find_unresolved_method_sources(Path("TargetTest.java"), source, method_bodies)
        self.assertEqual(1, len(reviews))

    def test_ordinary_get_type_is_not_metadata_accessor_violation(self):
        source = """
            class TargetTest {
                @Test void assertRecord() {
                    assertThat(record.getType(), is(OperationType.INSERT));
                }
            }
        """
        self.assertEqual([], rules.find_metadata_accessor_test_candidates(Path("TargetTest.java"), source))

    def test_boolean_rule_ignores_comments_and_literals(self):
        source = '''
            class TargetTest {
                // assertEquals(true, target.run());
                String example = "assertThat(value, is(false))";
            }
        '''
        self.assertEqual([], rules.check_r14(Path("TargetTest.java"), source))

    def test_boolean_rule_detects_code(self):
        source = "class TargetTest { @Test void assertRun() { assertEquals(true, target.run()); } }"
        self.assertEqual(["TargetTest.java:1"], rules.check_r14(Path("TargetTest.java"), source))

    def test_parameterized_name_rule_ignores_comment(self):
        source = "// @ParameterizedTest\nclass TargetTest {}"
        self.assertEqual([], rules.check_parameterized_name(Path("TargetTest.java"), source))

    def test_boolean_dispatch_rule_detects_switch(self):
        source = '''
            class TargetTest {
                @Test void assertRun() {
                    switch (value) {
                        case 1 -> assertTrue(target.run());
                        default -> assertFalse(target.run());
                    }
                }
            }
        '''
        self.assertEqual(["TargetTest.java:3 method=assertRun"], rules.check_r15_h(Path("TargetTest.java"), source))

    def test_resolve_target_source_path_for_root_module(self):
        source = "class TargetTest {}"
        actual = rules.resolve_target_source_path(Path("src/test/java/sample/TargetTest.java"), source)
        self.assertEqual(Path("src/main/java/sample/Target.java"), actual)

    def test_scope_baseline_detects_change_to_preexisting_dirty_file(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            allowed = repo_root / "module/src/test/java/TargetTest.java"
            outside = repo_root / "AGENTS.md"
            allowed.parent.mkdir(parents=True)
            allowed.write_text("class TargetTest {}\n", encoding="utf-8")
            outside.write_text("initial\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            outside.write_text("dirty before\n", encoding="utf-8")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                self.assertEqual([], rules.check_scope_baseline(baseline_path, [allowed]))
                outside.write_text("changed during task\n", encoding="utf-8")
                violations = rules.check_scope_baseline(baseline_path, [allowed])
                self.assertEqual(["AGENTS.md"], violations)
                allowed.write_text("class TargetTest { }\n", encoding="utf-8")
                self.assertEqual(["AGENTS.md"], rules.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_scope_baseline_detects_new_ignored_file(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            allowed = repo_root / "module/src/test/java/TargetTest.java"
            ignored = repo_root / "target/generated.bin"
            allowed.parent.mkdir(parents=True)
            ignored.parent.mkdir(parents=True)
            allowed.write_text("class TargetTest {}\n", encoding="utf-8")
            (repo_root / ".gitignore").write_text("target/\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-ignored-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                ignored.write_bytes(b"generated")
                self.assertEqual(["target/generated.bin"], rules.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_missing_scope_baseline_is_failure(self):
        missing = Path(tempfile.gettempdir()) / "missing-gen-ut-scope-baseline.json"
        missing.unlink(missing_ok=True)
        self.assertEqual([f"missing scope baseline: {missing}"], rules.check_scope_baseline(missing, []))

    def test_scope_baseline_accepts_binary_test_resource(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            resource = repo_root / "module/src/test/resources/sample.bin"
            resource.parent.mkdir(parents=True)
            resource.write_bytes(b"\xff\x00")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-binary-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [resource])
                baseline = json.loads(baseline_path.read_text(encoding="utf-8"))
                self.assertIsNone(baseline["allowed_contents"]["module/src/test/resources/sample.bin"])
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_scope_baseline_rejects_non_test_path(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            production = repo_root / "src/main/java/Target.java"
            production.parent.mkdir(parents=True)
            production.write_text("class Target {}\n", encoding="utf-8")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-invalid-scope.json"
            try:
                with self.assertRaisesRegex(ValueError, "outside test scope"):
                    rules.write_scope_baseline(baseline_path, repo_root, [production])
            finally:
                baseline_path.unlink(missing_ok=True)

    @staticmethod
    def run_git(repo_root: Path, *args: str) -> None:
        subprocess.run(["git", *args], cwd=repo_root, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


if __name__ == "__main__":
    unittest.main()
