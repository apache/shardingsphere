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

"""Shared helpers for review-pr local scripts."""

from __future__ import annotations

import argparse
import base64
import binascii
import hashlib
import json
import os
import stat
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path, PurePosixPath
from typing import Any, Iterable


@dataclass(frozen=True)
class ChangedFile:
    status: str
    path: str
    old_path: str | None = None


def read_file_object(path: str | Path) -> tuple[dict[str, str | None], bytes | None]:
    source_path = Path(path)
    try:
        metadata = source_path.lstat()
    except (FileNotFoundError, NotADirectoryError):
        return {"mode": None, "sha256": None}, None
    if stat.S_ISLNK(metadata.st_mode):
        mode, content = "120000", os.fsencode(os.readlink(source_path))
    elif stat.S_ISREG(metadata.st_mode):
        mode = "100755" if metadata.st_mode & stat.S_IXUSR else "100644"
        content = source_path.read_bytes()
    else:
        raise RuntimeError(f"File snapshots require a regular file or symbolic link: {path}")
    return {"mode": mode, "sha256": hashlib.sha256(content).hexdigest()}, content


def validate_snapshot_path(value: Any) -> str:
    if not isinstance(value, str) or not Path(value).is_absolute() or ".." in Path(value).parts:
        raise RuntimeError("File snapshots require absolute literal file paths without parent traversal")
    return str(Path(value))


def capture_file_snapshots(paths: list[str], output: str | Path, extend: str | Path | None = None) -> None:
    normalized_paths = [validate_snapshot_path(each) for each in paths]
    if not normalized_paths or len(normalized_paths) != len(set(normalized_paths)):
        raise RuntimeError("File snapshot scope must be nonempty and contain no duplicate paths")
    parent = _load_snapshot_chain(extend, set()) if extend is not None else None
    if parent is not None and any(path in parent["objects"] for path in normalized_paths):
        raise RuntimeError("A snapshot extension cannot replace an existing target's original object")
    if os.path.abspath(output) in normalized_paths or parent is not None and os.path.abspath(output) in parent["objects"]:
        raise RuntimeError("The file snapshot manifest cannot be one of its delivery targets")
    files = []
    for path in normalized_paths:
        metadata, content = read_file_object(path)
        files.append({"path": path, "base": {**metadata, "bytes_base64": base64.b64encode(content).decode("ascii") if content is not None else None}})
    configuration = {"version": 1, "files": files} if parent is None else {"version": 2, "parent": parent["descriptor"], "files": files}
    with os.fdopen(os.open(output, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600), "w", encoding="utf-8") as output_file:
        json.dump(configuration, output_file, indent=2, sort_keys=True)
        output_file.write("\n")


def load_file_snapshots(snapshot_file: str | Path) -> dict[str, tuple[dict[str, str | None], bytes | None]]:
    return _load_snapshot_chain(snapshot_file, set())["objects"]


def load_snapshot_root(snapshot_file: str | Path) -> dict[str, tuple[dict[str, str | None], bytes | None]]:
    return _load_snapshot_chain(snapshot_file, set())["root"]


def file_snapshot_parent(snapshot_file: str | Path) -> dict[str, str] | None:
    return _load_snapshot_chain(snapshot_file, set())["parent"]


def _load_snapshot_chain(snapshot_file: str | Path, ancestors: set[str], expected_hash: str | None = None) -> dict[str, Any]:
    snapshot_path = str(Path(snapshot_file).absolute())
    if snapshot_path in ancestors:
        raise RuntimeError("File snapshot parent chain contains a cycle")
    raw = Path(snapshot_path).read_bytes()
    descriptor = {"path": snapshot_path, "sha256": hashlib.sha256(raw).hexdigest()}
    if expected_hash is not None and descriptor["sha256"] != expected_hash:
        raise RuntimeError("File snapshot parent changed from its recorded original bytes")
    configuration = json.loads(raw.decode("utf-8"))
    if not isinstance(configuration, dict) or type(configuration.get("version")) is not int or configuration["version"] not in {1, 2}:
        raise RuntimeError("Unsupported file snapshot manifest")
    if not isinstance(configuration.get("files"), list) or not configuration["files"]:
        raise RuntimeError("File snapshot scope must be nonempty")
    result = {}
    parent_descriptor = None
    root = result
    if configuration["version"] == 2:
        parent_descriptor = configuration.get("parent")
        if not isinstance(parent_descriptor, dict) or not isinstance(parent_descriptor.get("path"), str) or not Path(parent_descriptor["path"]).is_absolute():
            raise RuntimeError("A snapshot extension requires an absolute parent manifest path")
        if not isinstance(parent_descriptor.get("sha256"), str):
            raise RuntimeError("A snapshot extension requires its parent's original byte hash")
        parent = _load_snapshot_chain(parent_descriptor["path"], ancestors | {snapshot_path}, parent_descriptor["sha256"])
        result = dict(parent["objects"])
        root = parent["root"]
    for entry in configuration["files"]:
        if not isinstance(entry, dict) or "path" not in entry or not isinstance(entry.get("base"), dict):
            raise RuntimeError("File snapshots require a path and original base object")
        path = validate_snapshot_path(entry["path"])
        if path in result:
            raise RuntimeError(f"File snapshot scope contains a duplicate path: {path}")
        base = entry["base"]
        if any(key not in base for key in ("mode", "sha256", "bytes_base64")):
            raise RuntimeError(f"File snapshots require original bytes, mode, and hash: {path}")
        if base["mode"] is None and base["sha256"] is None and base["bytes_base64"] is None:
            result[path] = ({"mode": None, "sha256": None}, None)
            continue
        if not isinstance(base["mode"], str) or base["mode"] not in {"100644", "100755", "120000"} or not isinstance(base["bytes_base64"], str):
            raise RuntimeError(f"Invalid original file snapshot object: {path}")
        try:
            content = base64.b64decode(base["bytes_base64"], validate=True)
        except (ValueError, binascii.Error) as ex:
            raise RuntimeError(f"Invalid original file snapshot bytes: {path}") from ex
        if hashlib.sha256(content).hexdigest() != base["sha256"]:
            raise RuntimeError(f"Original file snapshot hash does not match its bytes: {path}")
        result[path] = ({"mode": base["mode"], "sha256": base["sha256"]}, content)
    return {"objects": result, "root": root, "parent": parent_descriptor, "descriptor": descriptor}


def resolve_snapshot_changes(snapshots: dict[str, tuple[dict[str, str | None], bytes | None]]) -> list[ChangedFile]:
    result = []
    for path, (base, _) in snapshots.items():
        candidate, _ = read_file_object(path)
        if base != candidate:
            status = "A" if base["mode"] is None else "D" if candidate["mode"] is None else "M"
            result.append(ChangedFile(status=status, path=path))
    return result


def run_git(args: list[str], repo_root: Path, allow_empty: bool = False) -> str:
    process = subprocess.run(["git", *args], cwd=repo_root, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
    if 0 == process.returncode or allow_empty and 1 == process.returncode:
        return process.stdout
    command = "git " + " ".join(args)
    raise RuntimeError(f"{command} failed with exit {process.returncode}: {process.stderr.strip()}")


def get_repo_root(path: Path | None = None) -> Path:
    process = subprocess.run(["git", "rev-parse", "--show-toplevel"], cwd=path, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
    if 0 != process.returncode:
        target = path if path else "current directory"
        raise RuntimeError(f"{target} is not inside a git repository")
    return Path(process.stdout.strip())


def parse_name_status(output: str) -> list[ChangedFile]:
    result: list[ChangedFile] = []
    for line in output.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        status = parts[0]
        if status.startswith(("R", "C")):
            result.append(ChangedFile(status=status, path=parts[-1], old_path=parts[1]))
        else:
            result.append(ChangedFile(status=status, path=parts[-1]))
    return result


def resolve_candidate_changes(repo_root: Path, baseline_ref: str, candidate_files_path: str) -> list[ChangedFile]:
    candidate_paths = [line.strip() for line in Path(candidate_files_path).read_text(encoding="utf-8").splitlines() if line.strip()]
    if not candidate_paths:
        raise RuntimeError("Candidate file list is empty")
    if len(candidate_paths) != len(set(candidate_paths)):
        raise RuntimeError("Candidate file list contains duplicate paths")
    invalid_paths = [each for each in candidate_paths
                     if PurePosixPath(each).is_absolute() or "." == each or ".." in PurePosixPath(each).parts]
    if invalid_paths:
        raise RuntimeError(f"Candidate file list contains invalid repository-relative paths: {', '.join(invalid_paths)}")
    literal_paths = [f":(literal){each}" for each in candidate_paths]
    tracked_changes = parse_name_status(run_git(["diff", "--name-status", baseline_ref, "--", *literal_paths], repo_root))
    untracked_paths = run_git(["ls-files", "--others", "--exclude-standard", "--", *literal_paths],
                              repo_root, allow_empty=True).splitlines()
    changed_files = [*tracked_changes, *(ChangedFile(status="A", path=each) for each in untracked_paths if each)]
    changed_by_path = {each.path: each for each in changed_files}
    if len(changed_by_path) != len(changed_files):
        raise RuntimeError("Candidate scope resolves the same final path more than once")
    unchanged_paths = [each for each in candidate_paths if each not in changed_by_path]
    if unchanged_paths:
        raise RuntimeError(f"Candidate file list contains paths that are not changed from the baseline: {', '.join(unchanged_paths)}")
    return [changed_by_path[each] for each in candidate_paths]


def categorize(path: str) -> str:
    if "RELEASE-NOTES.md" == path:
        return "release-notes"
    if "distribution" in path:
        return "distribution"
    if path.startswith("src/main/java/") or "/src/main/java/" in path:
        return "production-java"
    if path.startswith("src/test/") or "/src/test/" in path:
        return "tests"
    if path.startswith(".github/") or path.endswith((".xml", ".properties", ".yml", ".yaml", ".toml", ".gradle")):
        return "build-config"
    if path.startswith("docs/") or path.endswith((".md", ".adoc")):
        return "docs"
    if "target/" in path or "/generated/" in path:
        return "generated"
    return "other"


def final_paths(changed_files: Iterable[ChangedFile]) -> list[str]:
    return [each.path for each in changed_files]


def compare_github_files(local_paths: Iterable[str], github_files_path: str | None, limit: int | None = None) -> dict[str, Any]:
    if not github_files_path:
        return {"provided": False}
    github_paths = sorted(line.strip() for line in Path(github_files_path).read_text(encoding="utf-8").splitlines() if line.strip())
    local_sorted = sorted(local_paths)
    only_in_github = sorted(set(github_paths) - set(local_sorted))
    only_in_local = sorted(set(local_sorted) - set(github_paths))
    if limit is not None:
        only_in_github = only_in_github[:limit]
        only_in_local = only_in_local[:limit]
    return {
        "provided": True,
        "matched": github_paths == local_sorted,
        "github_count": len(github_paths),
        "local_count": len(local_sorted),
        "only_in_github": only_in_github,
        "only_in_local": only_in_local,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Capture original delivery files for review without Git.")
    commands = parser.add_subparsers(dest="command", required=True)
    capture = commands.add_parser("capture", help="Capture original bytes before changing delivery files")
    capture.add_argument("--files", required=True, help="Absolute delivery paths, one per line")
    capture.add_argument("--output", required=True, help="New private snapshot manifest path")
    capture.add_argument("--extend", help="Immutable parent manifest; new targets require separate task authorization")
    args = parser.parse_args()
    try:
        paths = [line for line in Path(args.files).read_text(encoding="utf-8").splitlines() if line]
        capture_file_snapshots(paths, args.output, extend=args.extend)
    except (OSError, RuntimeError, ValueError) as ex:
        print(f"error: {ex}", file=sys.stderr)
        return 1
    print(f"snapshot={os.path.abspath(args.output)} files={len(paths)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
