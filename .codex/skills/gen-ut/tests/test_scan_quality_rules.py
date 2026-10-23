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
import subprocess
import sys
import tempfile
import unittest
from dataclasses import asdict
from pathlib import Path
from unittest.mock import patch


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import scan_quality_rules as rules


class ScanQualityRulesTest(unittest.TestCase):

    def test_candidate_reports_facts_without_deciding_suitability(self):
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
        self.assertEqual("TargetTest.java: method=run plainTestCount=3 parameterizedPresent=False", rules.describe_candidate(asdict(candidates[0])))

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
        reviews = rules.find_argument_source_reviews(Path("TargetTest.java"), source)
        self.assertEqual(1, len(reviews))

    def test_nested_type_declarations_require_manual_review(self):
        path = Path("module/src/test/java/foo/TargetTest.java")
        fixtures = (
            (
                "nested-test-group",
                "final int value",
                "target.run(value)",
                "@Nested\n    final class FooGroup { @Test void assertClose() { target.close(); } }",
                "+    final class FooGroup {",
                "module/src/test/java/foo/TargetTest.java: final class FooGroup {",
            ),
            (
                "data-only-case",
                "final FooCase value",
                "target.run(value.input())",
                "private record FooCase(int input, int expected) {}",
                "+    private record FooCase(int input, int expected) {}",
                "module/src/test/java/foo/TargetTest.java: private record FooCase(int input, int expected) {}",
            ),
            (
                "assertion-callback-case",
                "final FooCase value",
                "int actual = target.run(value.input()); value.assertion().accept(actual)",
                "private record FooCase(int input, Consumer<Integer> assertion) {}",
                "+    private record FooCase(int input, Consumer<Integer> assertion) {}",
                "module/src/test/java/foo/TargetTest.java: private record FooCase(int input, Consumer<Integer> assertion) {}",
            ),
        )
        for name, parameter, invocation, declaration, added_line, expected_review in fixtures:
            with self.subTest(name=name):
                source = (
                    'class TargetTest {\n    @ParameterizedTest(name = "{0}")\n'
                    '    @MethodSource("FooCases#values")\n'
                    f'    void assertRun(final String name, {parameter}) {{ {invocation}; }}\n'
                    f'    {declaration}\n}}'
                )
                context = rules.FileScanContext(
                    path=path, source=source, method_bodies={}, method_blocks={}, candidates=[],
                    target_type_name="Target", target_public_methods={"run"}, added_lines=[added_line],
                )
                with patch.object(rules, "create_file_scan_context", return_value=context), patch.object(rules, "check_scope_baseline", return_value=[]):
                    actual = rules.collect_scan_result([path], Path("foo_scope_baseline.json"))
                self.assertEqual([expected_review], actual["reviews"]["R15-G"])
                self.assertEqual([], rules.failed_rule_names(actual))

    def test_nested_type_review_applicability(self):
        source = "class TargetTest { @ParameterizedTest void assertRun() {} private class FooGroup {} }"
        fixtures = (
            ("unchanged-declaration", source, []),
            ("no-parameterized-test", source.replace("@ParameterizedTest", "@Test"), ["+    private class FooGroup {}"]),
            ("top-level-declaration", source, ["+class TargetTest {", "+    class TargetTest {"]),
        )
        for name, candidate_source, added_lines in fixtures:
            with self.subTest(name=name):
                self.assertEqual([], rules.check_r15_g(Path("TargetTest.java"), candidate_source, added_lines))

    def test_scan_reports_only_automatic_rules_and_source_locations(self):
        path = Path("TargetTest.java")
        context = rules.FileScanContext(
            path=path, source="class TargetTest { @Test void assertRun() { assertTrue(target.run()); } }",
            method_bodies={}, method_blocks={}, candidates=[], target_type_name="Target", target_public_methods={"run"}, added_lines=[],
        )
        with patch.object(rules, "create_file_scan_context", return_value=context), patch.object(rules, "check_scope_baseline", return_value=[]):
            actual = rules.collect_scan_result([path], Path("scope.json"))
        self.assertEqual({"R15-C", "R15-J"}, set(actual["rules"]))
        self.assertEqual({"R15-D", "R15-F", "R15-G", "R15-H", "R15-I", "R15-J"}, set(actual["reviews"]))
        self.assertTrue(all(not locations for locations in actual["reviews"].values()))
        self.assertEqual([], rules.failed_rule_names(actual))

    def test_boolean_dispatch_rule_reports_switch_review_candidate(self):
        path = Path("TargetTest.java")
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
        context = rules.FileScanContext(path=path, source=source, method_bodies={}, method_blocks={}, candidates=[],
                                        target_type_name="Target", target_public_methods={"run"}, added_lines=[])
        with patch.object(rules, "create_file_scan_context", return_value=context), patch.object(rules, "check_scope_baseline", return_value=[]):
            actual = rules.collect_scan_result([path], Path("scope.json"))
        self.assertEqual(["TargetTest.java:3 method=assertRun"], actual["reviews"]["R15-H"])
        self.assertEqual([], rules.failed_rule_names(actual))

    def test_standard_argument_sources_report_locations(self):
        for annotation in ('@CsvSource({"foo,1", "bar,2", "baz,3"})', '@ValueSource(strings = {"foo", "bar", "baz"})', '@MethodSource("cases")'):
            with self.subTest(annotation=annotation):
                source = f'@ParameterizedTest(name = "{{0}}") {annotation} void assertRun(final String name) {{ target.run(name); }}'
                actual = rules.find_argument_source_reviews(Path("TargetTest.java"), source)
                detail = "providers=cases" if "@MethodSource" in annotation else "configured argument source"
                self.assertEqual([f"TargetTest.java:1 method=assertRun {detail}"], actual)

    def test_branch_and_business_callback_candidates_require_review_without_mechanical_rejection(self):
        path = Path("TargetTest.java")
        source = '''
            class TargetTest {
                @ParameterizedTest(name = "{0}")
                @MethodSource("cases")
                void assertRun(final String name, final Consumer<String> callback) {
                    switch (name) { case "foo" -> target.run(callback); default -> target.close(); }
                }
            }
        '''
        blocks = rules.parse_method_blocks(source)
        context = rules.FileScanContext(path=path, source=source, method_bodies={name: body for name, (_, body) in blocks.items()},
                                        method_blocks=blocks, candidates=[], target_type_name="Target", target_public_methods={"run", "close"}, added_lines=[])
        with patch.object(rules, "create_file_scan_context", return_value=context), patch.object(rules, "check_scope_baseline", return_value=[]):
            actual = rules.collect_scan_result([path], Path("scope.json"))
        self.assertEqual([], rules.failed_rule_names(actual))
        for rule in ("R15-F", "R15-I"):
            with self.subTest(rule=rule):
                self.assertTrue(actual["reviews"][rule])

    def test_reports_preserve_locations_and_mechanical_exit_codes(self):
        locations = {name: [f"TargetTest.java:{line} method=assertRun"] for line, name in enumerate(
            ("R15-D", "R15-F", "R15-G", "R15-H", "R15-I", "R15-J"), start=1,
        )}
        candidate = {"path": "TargetTest.java", "method": "run", "plain_test_count": 3, "parameterized_present": False}
        for failed_rule in (None, "R15-C", "R15-J"):
            result = {
                "rules": rules.build_rule_result({name: ["violation"] if name == failed_rule else [] for name in ("R15-C", "R15-J")}),
                "reviews": locations, "candidates": [candidate], "java_file_count": 1,
            }
            for printer in (rules.print_rule_summary, rules.print_summary_only):
                with self.subTest(failed_rule=failed_rule, printer=printer.__name__), patch.object(sys, "stdout", new_callable=io.StringIO) as stdout:
                    self.assertEqual(int(failed_rule is not None), printer(result))
                    output = stdout.getvalue()
                    self.assertNotIn("semanticReviewRequired", output)
                    self.assertNotIn("decision=", output)
                    for name, entries in locations.items():
                        if printer is rules.print_rule_summary:
                            self.assertIn(f"[{name}] review: {entries[0]}", output)
                        else:
                            self.assertIn(f"{name}=1", output)
                            self.assertNotIn(entries[0], output)
                    if printer is rules.print_summary_only:
                        self.assertIn("TargetTest.java: method=run plainTestCount=3 parameterizedPresent=False", output)

    def test_repeated_test_is_an_entry_but_helper_still_fails_ownership_gate(self):
        source = '''
            class TargetTest {
                @RepeatedTest(3)
                void assertRepeatedExecution() {
                    final Target target = new Target();
                    assertTrue(target.run());
                }
                private boolean runHelper() {
                    final Target target = new Target();
                    return target.run();
                }
            }
        '''
        with tempfile.TemporaryDirectory() as temp_dir:
            test_path = Path(temp_dir) / "module/src/test/java/sample/TargetTest.java"
            target_path = Path(temp_dir) / "module/src/main/java/sample/Target.java"
            test_path.parent.mkdir(parents=True)
            target_path.parent.mkdir(parents=True)
            test_path.write_text(source, encoding="utf-8")
            target_path.write_text("class Target {\n    public boolean run() { return true; }\n}\n", encoding="utf-8")
            with patch.object(rules, "check_scope_baseline", return_value=[]):
                actual = rules.collect_scan_result([test_path], Path(temp_dir) / "scope.json")
            violations = actual["rules"]["R15-J"]["violations"]
            self.assertEqual(1, len(violations))
            self.assertIn("method=runHelper reason=helperInvokesTargetPublicMethod", violations[0])

    def test_resolve_target_source_path_for_root_module(self):
        source = "class TargetTest {}"
        actual = rules.resolve_target_source_path(Path("src/test/java/sample/TargetTest.java"), source)
        self.assertEqual(Path("src/main/java/sample/Target.java"), actual)

    def test_json_precedes_summary(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            path = Path(temp_dir) / "TargetTest.java"
            path.write_text("class TargetTest {}", encoding="utf-8")
            with (
                patch.object(sys, "argv", ["scan", "--json", "--summary-only", str(path)]),
                patch.object(sys, "stdout", new_callable=io.StringIO) as stdout,
            ):
                actual = rules.main()
        result = json.loads(stdout.getvalue())
        self.assertEqual(1, actual)
        self.assertEqual(["candidates", "java_file_count", "reviews", "rules"], list(result))
        self.assertEqual(["R15-C", "R15-J"], list(result["rules"]))
        self.assertEqual([str(path)], result["reviews"]["R15-J"])
        self.assertNotIn("R15-A", result["reviews"])
        self.assertEqual(["missing scope baseline"], result["rules"]["R15-C"]["violations"])
        self.assertEqual(1, result["java_file_count"])
        self.assertEqual(json.dumps(result, indent=2, sort_keys=True) + "\n", stdout.getvalue())

    def test_capture_precedes_report_flags(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            baseline = repo_root / "scope.json"
            path = repo_root / "TargetTest.java"
            with (
                patch.object(sys, "argv", ["scan", "--capture-scope-baseline", str(baseline), "--json", "--summary-only", str(path)]),
                patch.object(sys, "stdout", new_callable=io.StringIO) as stdout,
                patch.object(rules, "get_repo_root", return_value=repo_root),
                patch.object(rules, "write_scope_baseline") as write_baseline,
                patch.object(rules, "collect_scan_result", side_effect=AssertionError("scan requested")),
            ):
                actual = rules.main()
            write_baseline.assert_called_once_with(baseline, repo_root, [path])
        self.assertEqual(0, actual)
        self.assertEqual(f"[scope-baseline] path={baseline.resolve()} allowedPathCount=1\n", stdout.getvalue())

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
            ignored = repo_root / ".task-cache/generated.bin"
            allowed.parent.mkdir(parents=True)
            ignored.parent.mkdir(parents=True)
            allowed.write_text("class TargetTest {}\n", encoding="utf-8")
            (repo_root / ".gitignore").write_text(".task-cache/\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-ignored-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                ignored.write_bytes(b"generated")
                self.assertEqual([".task-cache/generated.bin"], rules.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_scope_baseline_ignores_verification_outputs(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            allowed = repo_root / "module/src/test/java/TargetTest.java"
            allowed.parent.mkdir(parents=True)
            allowed.write_text("class TargetTest {}\n", encoding="utf-8")
            (repo_root / "module/pom.xml").write_text("<project />\n", encoding="utf-8")
            (repo_root / ".gitignore").write_text("target/\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-verification-output-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                maven_output = repo_root / "module/target/surefire-reports/result.xml"
                python_output = repo_root / ".codex/skills/gen-ut/scripts/__pycache__/scan.pyc"
                maven_output.parent.mkdir(parents=True)
                python_output.parent.mkdir(parents=True)
                maven_output.write_text("verified\n", encoding="utf-8")
                python_output.write_bytes(b"compiled")
                self.assertEqual([], rules.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_scope_baseline_detects_untracked_source_in_target_package(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            allowed = repo_root / "module/src/test/java/sample/AllowedTest.java"
            allowed.parent.mkdir(parents=True)
            allowed.write_text("class AllowedTest {}\n", encoding="utf-8")
            (repo_root / "module/pom.xml").write_text("<project />\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-target-package-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                outside = repo_root / "module/src/test/java/target/HiddenTest.java"
                outside.parent.mkdir(parents=True)
                outside.write_text("class HiddenTest {}\n", encoding="utf-8")
                self.assertEqual(["module/src/test/java/target/HiddenTest.java"], rules.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_scope_baseline_detects_untracked_resource_in_python_cache_directory(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            allowed = repo_root / "module/src/test/java/sample/AllowedTest.java"
            allowed.parent.mkdir(parents=True)
            allowed.write_text("class AllowedTest {}\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-python-cache-resource-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                outside = repo_root / "module/src/test/resources/__pycache__/fixture.bin"
                outside.parent.mkdir(parents=True)
                outside.write_bytes(b"fixture")
                self.assertEqual(["module/src/test/resources/__pycache__/fixture.bin"], rules.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_scope_baseline_detects_tracked_file_in_verification_output_directory(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            allowed = repo_root / "module/src/test/java/TargetTest.java"
            tracked = repo_root / "fixtures/target/reference.txt"
            allowed.parent.mkdir(parents=True)
            tracked.parent.mkdir(parents=True)
            allowed.write_text("class TargetTest {}\n", encoding="utf-8")
            tracked.write_text("before\n", encoding="utf-8")
            (repo_root / ".gitignore").write_text("target/\n", encoding="utf-8")
            self.run_git(repo_root, "add", ".")
            self.run_git(repo_root, "add", "-f", "fixtures/target/reference.txt")
            self.run_git(repo_root, "-c", "user.name=Test", "-c", "user.email=test@example.com", "commit", "-m", "initial")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-tracked-output-scope.json"
            try:
                rules.write_scope_baseline(baseline_path, repo_root, [allowed])
                tracked.write_text("after\n", encoding="utf-8")
                self.assertEqual(["fixtures/target/reference.txt"], rules.check_scope_baseline(baseline_path, [allowed]))
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
