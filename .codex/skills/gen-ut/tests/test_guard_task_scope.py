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
from pathlib import Path


SCRIPTS_DIR = Path(__file__).resolve().parents[1] / "scripts"
if str(SCRIPTS_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPTS_DIR))

import guard_task_scope as scope


class GuardTaskScopeTest(unittest.TestCase):

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
                scope.write_scope_baseline(baseline_path, repo_root, [allowed])
                self.assertEqual([], scope.check_scope_baseline(baseline_path, [allowed]))
                outside.write_text("changed during task\n", encoding="utf-8")
                violations = scope.check_scope_baseline(baseline_path, [allowed])
                self.assertEqual(["AGENTS.md"], violations)
                allowed.write_text("class TargetTest { }\n", encoding="utf-8")
                self.assertEqual(["AGENTS.md"], scope.check_scope_baseline(baseline_path, [allowed]))
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
                scope.write_scope_baseline(baseline_path, repo_root, [allowed])
                ignored.write_bytes(b"generated")
                self.assertEqual([".task-cache/generated.bin"], scope.check_scope_baseline(baseline_path, [allowed]))
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
                scope.write_scope_baseline(baseline_path, repo_root, [allowed])
                maven_output = repo_root / "module/target/surefire-reports/result.xml"
                python_output = repo_root / ".codex/skills/gen-ut/scripts/__pycache__/scan.pyc"
                maven_output.parent.mkdir(parents=True)
                python_output.parent.mkdir(parents=True)
                maven_output.write_text("verified\n", encoding="utf-8")
                python_output.write_bytes(b"compiled")
                self.assertEqual([], scope.check_scope_baseline(baseline_path, [allowed]))
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
                scope.write_scope_baseline(baseline_path, repo_root, [allowed])
                outside = repo_root / "module/src/test/java/target/HiddenTest.java"
                outside.parent.mkdir(parents=True)
                outside.write_text("class HiddenTest {}\n", encoding="utf-8")
                self.assertEqual(["module/src/test/java/target/HiddenTest.java"], scope.check_scope_baseline(baseline_path, [allowed]))
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
                scope.write_scope_baseline(baseline_path, repo_root, [allowed])
                outside = repo_root / "module/src/test/resources/__pycache__/fixture.bin"
                outside.parent.mkdir(parents=True)
                outside.write_bytes(b"fixture")
                self.assertEqual(["module/src/test/resources/__pycache__/fixture.bin"], scope.check_scope_baseline(baseline_path, [allowed]))
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
                scope.write_scope_baseline(baseline_path, repo_root, [allowed])
                tracked.write_text("after\n", encoding="utf-8")
                self.assertEqual(["fixtures/target/reference.txt"], scope.check_scope_baseline(baseline_path, [allowed]))
            finally:
                baseline_path.unlink(missing_ok=True)

    def test_missing_scope_baseline_is_failure(self):
        missing = Path(tempfile.gettempdir()) / "missing-gen-ut-scope-baseline.json"
        missing.unlink(missing_ok=True)
        self.assertEqual([f"missing scope baseline: {missing}"], scope.check_scope_baseline(missing, []))

    def test_scope_baseline_accepts_binary_test_resource(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            repo_root = Path(temp_dir)
            self.run_git(repo_root, "init")
            resource = repo_root / "module/src/test/resources/sample.bin"
            resource.parent.mkdir(parents=True)
            resource.write_bytes(b"\xff\x00")
            baseline_path = Path(temp_dir).parent / f"{repo_root.name}-binary-scope.json"
            try:
                scope.write_scope_baseline(baseline_path, repo_root, [resource])
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
                    scope.write_scope_baseline(baseline_path, repo_root, [production])
            finally:
                baseline_path.unlink(missing_ok=True)

    @staticmethod
    def run_git(repo_root: Path, *args: str) -> None:
        subprocess.run(["git", *args], cwd=repo_root, check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)


if __name__ == "__main__":
    unittest.main()
