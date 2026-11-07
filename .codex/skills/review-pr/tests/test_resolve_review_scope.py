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

import base64
import hashlib
import io
import json
import sys
import tempfile
import unittest
from contextlib import redirect_stdout
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

    def test_capture_file_snapshots_preserves_bytes_and_absence(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source, empty, absent = (root / each for each in ("source.bin", "empty.md", "new.md"))
            content = b"\xff\x00first\r\nlast"
            source.write_bytes(content)
            empty.touch()
            snapshot = root / "original.json"
            common.capture_file_snapshots([str(source), str(empty), str(absent)], snapshot)
            actual = json.loads(snapshot.read_text(encoding="utf-8"))
            self.assertEqual({"version": 1, "files": [
                {"path": str(source), "base": {"mode": "100644", "sha256": hashlib.sha256(content).hexdigest(),
                                               "bytes_base64": base64.b64encode(content).decode("ascii")}},
                {"path": str(empty), "base": {"mode": "100644", "sha256": hashlib.sha256(b"").hexdigest(), "bytes_base64": ""}},
                {"path": str(absent), "base": {"mode": None, "sha256": None, "bytes_base64": None}},
            ]}, actual)
            self.assertEqual(0o600, snapshot.stat().st_mode & 0o777)

    def test_capture_file_snapshots_does_not_replace_a_baseline(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            snapshot = root / "original.json"
            snapshot.write_bytes(b"Original task baseline.\n")
            with self.assertRaises(FileExistsError):
                common.capture_file_snapshots([str(root / "delivery.md")], snapshot)
            self.assertEqual(b"Original task baseline.\n", snapshot.read_bytes())

    def test_capture_file_snapshots_rejects_invalid_scope(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            snapshot, source = root / "original.json", root / "delivery.md"
            for paths in ([], ["relative.md"], [str(source), str(source)], [str(snapshot)]):
                with self.subTest(paths=paths), self.assertRaises(RuntimeError):
                    common.capture_file_snapshots(paths, snapshot)
            self.assertFalse(snapshot.exists())

    def test_read_file_object_preserves_link_identity(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            link = Path(temp_dir) / "link"
            link.symlink_to("missing-target")
            payload = b"missing-target"
            self.assertEqual(({"mode": "120000", "sha256": hashlib.sha256(payload).hexdigest()}, payload), common.read_file_object(link))

    def test_read_file_object_rejects_directory_targets(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            with self.assertRaises(RuntimeError):
                common.read_file_object(temp_dir)

    def test_load_file_snapshots_uses_original_bytes(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source, absent, empty = root / "source.md", root / "new.md", root / "empty.md"
            source.write_bytes(b"Current candidate.\n")
            original = b"Original\r\nbytes\xff"
            snapshot = root / "original.json"
            source_base = {"mode": "100644", "sha256": hashlib.sha256(original).hexdigest(), "bytes_base64": base64.b64encode(original).decode("ascii")}
            empty_base = {"mode": "100644", "sha256": hashlib.sha256(b"").hexdigest(), "bytes_base64": ""}
            snapshot.write_text(json.dumps({"version": 1, "files": [
                {"path": str(source), "base": source_base},
                {"path": str(absent), "base": {"mode": None, "sha256": None, "bytes_base64": None}},
                {"path": str(empty), "base": empty_base},
            ]}), encoding="utf-8")
            actual = common.load_file_snapshots(snapshot)
            self.assertEqual({
                str(source): ({"mode": "100644", "sha256": source_base["sha256"]}, original),
                str(absent): ({"mode": None, "sha256": None}, None),
                str(empty): ({"mode": "100644", "sha256": empty_base["sha256"]}, b""),
            }, actual)

    def test_load_file_snapshots_rejects_invalid_baselines(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source, snapshot = root / "source.md", root / "original.json"
            base = {"mode": "100644", "sha256": hashlib.sha256(b"original").hexdigest(), "bytes_base64": "b3JpZ2luYWw="}
            entry = {"path": str(source), "base": base}
            invalid = [
                {"version": 3, "files": [entry]},
                {"version": 1, "files": []},
                {"version": 1, "files": [{"path": str(source)}]},
                {"version": 1, "files": [{"path": "relative.md", "base": base}]},
                {"version": 1, "files": [{"path": str(root) + "/../source.md", "base": base}]},
                {"version": 1, "files": [entry, entry]},
                {"version": 1, "files": [{"path": str(source), "base": {"mode": "100644", "sha256": base["sha256"]}}]},
                {"version": 1, "files": [{"path": str(source), "base": {**base, "mode": {}}}]},
                {"version": 1, "files": [{"path": str(source), "base": {**base, "sha256": "0" * 64}}]},
                {"version": 1, "files": [{"path": str(source), "base": {**base, "bytes_base64": "!not-base64"}}]},
                {"version": 1, "files": [{"path": str(source), "base": {**base, "bytes_base64": None}}]},
            ]
            for configuration in invalid:
                with self.subTest(configuration=configuration):
                    snapshot.write_text(json.dumps(configuration), encoding="utf-8")
                    with self.assertRaises(RuntimeError):
                        common.load_file_snapshots(snapshot)

    def test_resolve_snapshot_changes_covers_file_states(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            changed, added, deleted, executable, unchanged = (root / each for each in ("changed", "new", "deleted", "script", "unchanged"))
            changed.write_bytes(b"new")
            added.write_bytes(b"")
            executable.write_bytes(b"old")
            executable.chmod(0o755)
            unchanged.write_bytes(b"old")
            original = ({"mode": "100644", "sha256": hashlib.sha256(b"old").hexdigest()}, b"old")
            snapshots = {str(changed): original, str(added): ({"mode": None, "sha256": None}, None),
                         str(deleted): original, str(executable): original, str(unchanged): original}
            with patch.object(common, "run_git", side_effect=AssertionError("Snapshot scope must not use Git")):
                actual = common.resolve_snapshot_changes(snapshots)
            self.assertEqual([ChangedFile("M", str(changed)), ChangedFile("A", str(added)), ChangedFile("D", str(deleted)),
                              ChangedFile("M", str(executable))], actual)

    def test_capture_command_preserves_literal_paths(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            source, paths, snapshot = root / "delivery report.md ", root / "targets.txt", root / "original.json"
            source.write_bytes(b"Original delivery.\n")
            paths.write_text(str(source) + "\n", encoding="utf-8")
            with patch.object(sys, "argv", ["resolve_review_scope.py", "capture", "--files", str(paths), "--output", str(snapshot)]), redirect_stdout(io.StringIO()):
                actual = common.main()
            self.assertEqual(0, actual)
            self.assertEqual(str(source), json.loads(snapshot.read_text(encoding="utf-8"))["files"][0]["path"])

    def test_capture_extension_keeps_originals_and_only_adds_new_targets(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            original, added = root / "original.md", root / "added.bin"
            baseline, extension = root / "baseline.json", root / "extension.json"
            original.write_bytes(b"First task bytes.\r\n")
            common.capture_file_snapshots([str(original)], baseline)
            baseline_bytes = baseline.read_bytes()
            original.write_bytes(b"Later candidate.\n")
            added_bytes = b"\xff\x00Added before its first write."
            added.write_bytes(added_bytes)
            common.capture_file_snapshots([str(added)], extension, extend=baseline)
            actual = json.loads(extension.read_text(encoding="utf-8"))
            self.assertEqual({"version": 2, "parent": {"path": str(baseline), "sha256": hashlib.sha256(baseline_bytes).hexdigest()}, "files": [
                {"path": str(added), "base": {"mode": "100644", "sha256": hashlib.sha256(added_bytes).hexdigest(),
                                              "bytes_base64": base64.b64encode(added_bytes).decode("ascii")}},
            ]}, actual)
            self.assertEqual(baseline_bytes, baseline.read_bytes())
            loaded = common.load_file_snapshots(extension)
            self.assertEqual(b"First task bytes.\r\n", loaded[str(original)][1])
            self.assertEqual(added_bytes, loaded[str(added)][1])

    def test_extensions_preserve_the_original_root_scope(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            original, second, third = (root / each for each in ("original", "second", "third"))
            baseline, middle, extension = (root / each for each in ("baseline.json", "middle.json", "extension.json"))
            original.write_bytes(b"Original.\n")
            common.capture_file_snapshots([str(original)], baseline)
            common.capture_file_snapshots([str(second)], middle, extend=baseline)
            third.write_bytes(b"")
            common.capture_file_snapshots([str(third)], extension, extend=middle)
            actual = common.load_snapshot_root(extension)
            self.assertEqual({str(original): ({"mode": "100644", "sha256": hashlib.sha256(b"Original.\n").hexdigest()}, b"Original.\n")}, actual)
            inherited = common.load_file_snapshots(extension)
            self.assertEqual(({"mode": None, "sha256": None}, None), inherited[str(second)])
            self.assertEqual(({"mode": "100644", "sha256": hashlib.sha256(b"").hexdigest()}, b""), inherited[str(third)])
            self.assertEqual({str(original), str(second), str(third)}, set(inherited))

    def test_file_snapshot_parent_reports_the_direct_link(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline, middle, extension = (root / each for each in ("baseline.json", "middle.json", "extension.json"))
            common.capture_file_snapshots([str(root / "first")], baseline)
            common.capture_file_snapshots([str(root / "second")], middle, extend=baseline)
            common.capture_file_snapshots([str(root / "third")], extension, extend=middle)
            for manifest, parent in ((baseline, None), (middle, baseline), (extension, middle)):
                with self.subTest(manifest=manifest):
                    expected = None if parent is None else {"path": str(parent), "sha256": hashlib.sha256(parent.read_bytes()).hexdigest()}
                    self.assertEqual(expected, common.file_snapshot_parent(manifest))

    def test_capture_extension_rejects_replacing_a_prior_original(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            original, baseline, extension = root / "original.md", root / "baseline.json", root / "extension.json"
            original.write_bytes(b"Original.\n")
            common.capture_file_snapshots([str(original)], baseline)
            baseline_bytes = baseline.read_bytes()
            original.write_bytes(b"Candidate.\n")
            with self.assertRaises(RuntimeError):
                common.capture_file_snapshots([str(original)], extension, extend=baseline)
            self.assertFalse(extension.exists())
            self.assertEqual(baseline_bytes, baseline.read_bytes())

    def test_load_extension_rejects_replacing_a_prior_original(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            original, baseline, extension = root / "original.md", root / "baseline.json", root / "extension.json"
            common.capture_file_snapshots([str(original)], baseline)
            parent_bytes = baseline.read_bytes()
            extension.write_text(json.dumps({"version": 2, "parent": {"path": str(baseline), "sha256": hashlib.sha256(parent_bytes).hexdigest()},
                                             "files": json.loads(parent_bytes)["files"]}), encoding="utf-8")
            with self.assertRaises(RuntimeError):
                common.load_file_snapshots(extension)

    def test_load_extension_rejects_a_changed_or_missing_parent(self):
        for condition in ("changed", "missing"):
            with self.subTest(condition=condition), tempfile.TemporaryDirectory() as temp_dir:
                root = Path(temp_dir)
                baseline, extension = root / "baseline.json", root / "extension.json"
                common.capture_file_snapshots([str(root / "original")], baseline)
                common.capture_file_snapshots([str(root / "added")], extension, extend=baseline)
                if condition == "changed":
                    baseline.write_bytes(baseline.read_bytes() + b"\n")
                else:
                    baseline.unlink()
                with self.assertRaises((RuntimeError, FileNotFoundError)):
                    common.load_file_snapshots(extension)

    def test_load_extension_rejects_invalid_parent_links_and_cycles(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline, extension = root / "baseline.json", root / "extension.json"
            common.capture_file_snapshots([str(root / "original")], baseline)
            entry = {"path": str(root / "added"), "base": {"mode": None, "sha256": None, "bytes_base64": None}}
            for parent in (None, {}, {"path": "relative.json", "sha256": "0" * 64}, {"path": str(baseline)},
                           {"path": str(baseline), "sha256": "0" * 64}, {"path": str(extension), "sha256": "0" * 64}):
                with self.subTest(parent=parent):
                    extension.write_text(json.dumps({"version": 2, "parent": parent, "files": [entry]}), encoding="utf-8")
                    with self.assertRaises(RuntimeError):
                        common.load_file_snapshots(extension)

    def test_capture_command_supports_an_immutable_extension(self):
        with tempfile.TemporaryDirectory() as temp_dir:
            root = Path(temp_dir)
            baseline, extension, paths = root / "baseline.json", root / "extension.json", root / "targets.txt"
            common.capture_file_snapshots([str(root / "original")], baseline)
            paths.write_text(str(root / "added") + "\n", encoding="utf-8")
            with patch.object(sys, "argv", ["resolve_review_scope.py", "capture", "--files", str(paths), "--output", str(extension), "--extend", str(baseline)]), \
                    redirect_stdout(io.StringIO()):
                actual = common.main()
            self.assertEqual(0, actual)
            self.assertEqual({"path": str(baseline), "sha256": hashlib.sha256(baseline.read_bytes()).hexdigest()}, common.file_snapshot_parent(extension))

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
