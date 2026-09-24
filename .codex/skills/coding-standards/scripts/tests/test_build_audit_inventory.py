#!/usr/bin/env python3
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
from unittest.mock import Mock, patch


SCRIPT_DIR = Path(__file__).resolve().parents[1]
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import build_audit_inventory as inventory_builder  # noqa: E402


class BuildAuditInventoryTest(unittest.TestCase):

    def test_repository_file_discovery_uses_git_inventory_when_available(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.write_text(repo / "tracked.java", "class Tracked {}\n")
            self.write_text(repo / "ignored.java", "class Ignored {}\n")
            completed = Mock(returncode=0, stdout=b"tracked.java\0")

            with patch.object(inventory_builder.subprocess, "run", return_value=completed):
                actual = inventory_builder.discover_repository_files(repo)

            self.assertEqual([repo / "tracked.java"], actual)

    def test_repository_file_discovery_fails_when_git_command_fails(self) -> None:
        completed = Mock(returncode=128, stdout=b"")

        with patch.object(inventory_builder.subprocess, "run", return_value=completed):
            with self.assertRaisesRegex(ValueError, "cannot be listed by git"):
                inventory_builder.discover_repository_files(Path("repo"))

    def test_repository_file_discovery_fails_when_git_is_unavailable(self) -> None:
        with patch.object(inventory_builder.subprocess, "run", side_effect=OSError):
            with self.assertRaisesRegex(ValueError, "cannot be listed by git"):
                inventory_builder.discover_repository_files(Path("repo"))

    def test_build_repository_inventory(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a",), profile_modules={"extra": ("module-b",)})
            self.write_pom(repo / "module-a", "module-a")
            self.write_pom(repo / "module-b", "module-b")
            self.write_pom(repo / "orphan", "orphan")
            service_file = repo / "module-a" / "src" / "main" / "resources" / "META-INF" / "services" / "example.Service"
            service_file.parent.mkdir(parents=True)
            service_file.write_text("example.Implementation\n", encoding="utf-8")
            (repo / "module-b" / "src" / "test" / "java").mkdir(parents=True)

            actual = inventory_builder.build_inventory(repo, (".",))

            self.assertEqual(4, actual["discovered_module_count"])
            self.assertEqual(4, actual["selected_module_count"])
            self.assertEqual(3, actual["leaf_module_count"])
            self.assertEqual(["orphan"], actual["unreferenced_poms"])
            modules = {each["path"]: each for each in actual["modules"]}
            self.assertEqual(
                [{"path": "module-a", "source": "default"}, {"path": "module-b", "source": "profile:extra"}],
                modules["."]["children"],
            )
            self.assertEqual(
                ["module-a/src/main/resources/META-INF/services/example.Service"],
                modules["module-a"]["main_service_files"],
            )
            self.assertTrue(modules["module-b"]["has_test_java"])

    def test_list_all_file_types_and_source_sets(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a",))
            self.write_pom(repo / "module-a", "module-a")
            self.write_text(repo / "module-a" / "src" / "main" / "java" / "Foo.java", "class Foo {\n}\n")
            self.write_text(repo / "module-a" / "src" / "test" / "java" / "FooTest.java", "class FooTest {}\n")
            self.write_text(repo / "module-a" / "src" / "main" / "antlr4" / "Foo.g4", "grammar Foo;\n")
            self.write_text(repo / ".github" / "workflows" / "check.yaml", "name: Check\n")
            self.write_text(repo / "config" / "example_config.xml", "<config/>\n")
            binary_file = repo / "module-a" / "src" / "main" / "resources" / "image.bin"
            binary_file.parent.mkdir(parents=True)
            binary_file.write_bytes(b"\xff\x00")
            symlink = repo / "module-a" / "src" / "main" / "resources" / "image-link.bin"
            symlink.symlink_to(binary_file)
            self.write_text(repo / ".gitignore", "target/\n")
            self.write_text(repo / "module-a" / "target" / "Generated.java", "class Generated {}\n")

            actual = inventory_builder.build_inventory(repo, (".",))

            files = {each["path"]: each for each in actual["files"]}
            self.assertEqual(2, actual["version"])
            self.assertEqual("production", files["module-a/src/main/java/Foo.java"]["source_set"])
            self.assertEqual(2, files["module-a/src/main/java/Foo.java"]["line_count"])
            self.assertEqual("test", files["module-a/src/test/java/FooTest.java"]["source_set"])
            self.assertEqual("production", files["module-a/src/main/antlr4/Foo.g4"]["source_set"])
            self.assertEqual("workflow", files[".github/workflows/check.yaml"]["source_set"])
            self.assertEqual(".yaml", files[".github/workflows/check.yaml"]["extension"])
            self.assertEqual("repository", files["config/example_config.xml"]["source_set"])
            self.assertIsNone(files["module-a/src/main/resources/image.bin"]["line_count"])
            self.assertEqual("non_utf8_or_binary", files["module-a/src/main/resources/image.bin"]["content_kind"])
            self.assertIsNone(files["module-a/src/main/resources/image-link.bin"]["line_count"])
            self.assertEqual("symlink", files["module-a/src/main/resources/image-link.bin"]["content_kind"])
            self.assertNotIn("module-a/target/Generated.java", files)
            self.assertEqual(len(files), actual["file_count"])
            self.assertEqual(len(files) - 2, actual["text_file_count"])

    def test_file_scope_keeps_exact_owner_module(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a",))
            self.write_pom(repo / "module-a", "module-a", modules=("nested",))
            self.write_pom(repo / "module-a" / "nested", "nested")
            source_file = repo / "module-a" / "src" / "main" / "java" / "example" / "Foo.java"
            source_file.parent.mkdir(parents=True)
            source_file.write_text("class Foo {}\n", encoding="utf-8")

            actual = inventory_builder.build_inventory(repo, ("module-a/src/main/java/example/Foo.java",))

            self.assertEqual(1, actual["selected_module_count"])
            self.assertEqual(["module-a"], [each["path"] for each in actual["modules"]])
            self.assertEqual("file", actual["scopes"][0]["kind"])
            self.assertEqual("module-a", actual["scopes"][0]["owner_module"])
            self.assertEqual(["module-a/src/main/java/example/Foo.java"], [each["path"] for each in actual["files"]])

    def test_symlink_file_scope_keeps_exact_path_and_owner_module(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a", "module-b"))
            self.write_pom(repo / "module-a", "module-a")
            self.write_pom(repo / "module-b", "module-b")
            target = repo / "module-b" / "actual.txt"
            self.write_text(target, "actual\n")
            symlink = repo / "module-a" / "alias.txt"
            symlink.symlink_to(target)

            actual = inventory_builder.build_inventory(repo, ("module-a/alias.txt",))

            self.assertEqual("module-a/alias.txt", actual["scopes"][0]["path"])
            self.assertEqual("module-a", actual["scopes"][0]["owner_module"])
            self.assertEqual(["module-a"], [each["path"] for each in actual["modules"]])
            self.assertEqual("module-a/alias.txt", actual["files"][0]["path"])
            self.assertEqual("symlink", actual["files"][0]["content_kind"])
            self.assertIsNone(actual["files"][0]["line_count"])

    def test_reject_symlink_scope_outside_repository(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            repo = root / "repo"
            self.initialize_repository(repo)
            self.write_pom(repo, "root")
            target = root / "outside.txt"
            self.write_text(target, "outside\n")
            (repo / "alias.txt").symlink_to(target)

            with self.assertRaisesRegex(ValueError, "scope escapes repository"):
                inventory_builder.build_inventory(repo, ("alias.txt",))

    def test_path_scope_lists_only_files_below_exact_target(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a",))
            self.write_pom(repo / "module-a", "module-a")
            self.write_text(repo / "module-a" / "src" / "main" / "java" / "selected" / "Foo.java", "class Foo {}\n")
            self.write_text(repo / "module-a" / "src" / "main" / "java" / "sibling" / "Bar.java", "class Bar {}\n")

            actual = inventory_builder.build_inventory(repo, ("module-a/src/main/java/selected",))

            self.assertEqual("path", actual["scopes"][0]["kind"])
            self.assertEqual(["module-a/src/main/java/selected/Foo.java"], [each["path"] for each in actual["files"]])

    def test_pom_file_scope_keeps_exact_owner_module(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a",))
            self.write_pom(repo / "module-a", "module-a", modules=("nested",))
            self.write_pom(repo / "module-a" / "nested", "nested")

            actual = inventory_builder.build_inventory(repo, ("module-a/pom.xml",))

            self.assertEqual(1, actual["selected_module_count"])
            self.assertEqual(["module-a"], [each["path"] for each in actual["modules"]])
            self.assertEqual("file", actual["scopes"][0]["kind"])
            self.assertEqual("module-a", actual["scopes"][0]["owner_module"])

    def test_module_scope_includes_declared_descendants(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a",))
            self.write_pom(repo / "module-a", "module-a", modules=("nested",))
            self.write_pom(repo / "module-a" / "nested", "nested")

            actual = inventory_builder.build_inventory(repo, ("module-a",))

            self.assertEqual(["module-a", "module-a/nested"], [each["path"] for each in actual["modules"]])
            self.assertEqual("module", actual["scopes"][0]["kind"])

    def test_multiple_scopes_select_module_union(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("module-a", "module-b"))
            self.write_pom(repo / "module-a", "module-a")
            self.write_pom(repo / "module-b", "module-b")

            actual = inventory_builder.build_inventory(repo, ("module-b", "module-a"))

            self.assertEqual(["module-a", "module-b"], [each["path"] for each in actual["modules"]])
            self.assertEqual(2, len(actual["scopes"]))

    def test_report_invalid_module_declarations(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("${module.name}", "missing", "../outside"))

            actual = inventory_builder.build_inventory(repo, (".",))

            self.assertEqual(
                ["missing_module_pom", "outside_repository_module", "unresolved_module_path"],
                sorted(each["type"] for each in actual["issues"]),
            )

    def test_report_invalid_pom(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("broken",))
            broken_pom = repo / "broken" / "pom.xml"
            broken_pom.parent.mkdir(parents=True)
            broken_pom.write_text("<project>", encoding="utf-8")

            actual = inventory_builder.build_inventory(repo, (".",))

            self.assertEqual("invalid_pom", actual["issues"][0]["type"])
            modules = {each["path"]: each for each in actual["modules"]}
            self.assertIsNotNone(modules["broken"]["parse_error"])

    def test_reject_invalid_scope(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root")

            with self.assertRaisesRegex(ValueError, "repository-relative"):
                inventory_builder.build_inventory(repo, (str(repo),))
            with self.assertRaisesRegex(ValueError, "repository-relative"):
                inventory_builder.build_inventory(repo, ("../outside",))
            with self.assertRaisesRegex(ValueError, "scope does not exist"):
                inventory_builder.build_inventory(repo, ("missing",))

    def test_inventory_is_deterministic(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root", modules=("b", "a"))
            self.write_pom(repo / "a", "a")
            self.write_pom(repo / "b", "b")

            first = inventory_builder.build_inventory(repo, (".",))
            second = inventory_builder.build_inventory(repo, (".",))

            self.assertEqual(json.dumps(first, sort_keys=True), json.dumps(second, sort_keys=True))
            self.assertEqual([".", "a", "b"], [each["path"] for each in first["modules"]])

    def test_ignore_agent_codex_and_target_poms_but_list_their_files(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            repo = Path(temp_dir)
            self.initialize_repository(repo)
            self.write_pom(repo, "root")
            self.write_pom(repo / ".agents" / "helper", "helper")
            self.write_pom(repo / ".codex" / "helper", "helper")
            self.write_text(repo / ".gitignore", "target/\n")
            self.write_pom(repo / "module" / "target" / "fixture", "fixture")

            actual = inventory_builder.build_inventory(repo, (".",))

            self.assertEqual(["."], [each["path"] for each in actual["modules"]])
            self.assertEqual(
                [".agents/helper/pom.xml", ".codex/helper/pom.xml", ".gitignore", "pom.xml"],
                [each["path"] for each in actual["files"]],
            )

    @staticmethod
    def initialize_repository(directory: Path) -> None:
        subprocess.run(["git", "init", "--quiet", str(directory)], check=True)

    @staticmethod
    def write_text(path: Path, content: str) -> None:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8")

    @staticmethod
    def write_pom(directory: Path, artifact_id: str, modules: tuple[str, ...] = (),
                  profile_modules: dict[str, tuple[str, ...]] | None = None) -> None:
        directory.mkdir(parents=True, exist_ok=True)
        module_xml = "".join(f"<module>{each}</module>" for each in modules)
        default_modules = f"<modules>{module_xml}</modules>" if modules else ""
        profile_xml = ""
        for profile_id, paths in (profile_modules or {}).items():
            declared_modules = "".join(f"<module>{each}</module>" for each in paths)
            profile_xml += f"<profile><id>{profile_id}</id><modules>{declared_modules}</modules></profile>"
        profiles = f"<profiles>{profile_xml}</profiles>" if profile_xml else ""
        (directory / "pom.xml").write_text(
            f"<project><modelVersion>4.0.0</modelVersion><artifactId>{artifact_id}</artifactId>{default_modules}{profiles}</project>",
            encoding="utf-8",
        )


if __name__ == "__main__":
    unittest.main()
