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

"""Capture and validate task scope baselines for gen-ut."""

import difflib
import hashlib
import json
import os
import stat
import subprocess
from pathlib import Path


TEST_SCOPE_MARKERS = ("src/test/java/", "src/test/resources/")
MAVEN_OUTPUT_DIRECTORY = "target"
PYTHON_CACHE_DIRECTORY = "__pycache__"
PYTHON_CACHE_SUFFIXES = frozenset((".pyc", ".pyo"))


def run_git_command(args: list[str], *, cwd: Path | None = None, allow_failure: bool = False) -> str:
    try:
        return subprocess.run(args, cwd=cwd, check=True, capture_output=True, text=True).stdout
    except subprocess.CalledProcessError:
        if allow_failure:
            return ""
        raise


def get_repo_root(path: Path | None = None) -> Path:
    output = run_git_command(["git", "rev-parse", "--show-toplevel"], cwd=path)
    return Path(output.strip()).resolve()


def get_head(repo_root: Path) -> str | None:
    result = run_git_command(["git", "rev-parse", "--verify", "HEAD"], cwd=repo_root, allow_failure=True).strip()
    return result or None


def normalize_repo_paths(repo_root: Path, paths: list[Path]) -> list[str]:
    result = []
    for each in paths:
        resolved = (repo_root / each).resolve() if not each.is_absolute() else each.resolve()
        try:
            relative = resolved.relative_to(repo_root)
        except ValueError as ex:
            raise ValueError(f"path is outside repository: {resolved}") from ex
        result.append(relative.as_posix())
    return sorted(set(result))


def validate_test_scope_paths(paths: list[str]) -> None:
    invalid = [each for each in paths if not any(marker in each for marker in TEST_SCOPE_MARKERS)]
    if invalid:
        raise ValueError(f"path is outside test scope: {invalid[0]}")


def fingerprint_path(path: Path) -> str:
    digest = hashlib.sha256()
    if path.is_symlink():
        digest.update(b"symlink\0")
        digest.update(str(path.readlink()).encode("utf-8", errors="surrogateescape"))
        return digest.hexdigest()
    if not path.exists():
        return "missing"
    if path.is_dir():
        return "directory"
    digest.update(b"file\0")
    digest.update(str(stat.S_IMODE(path.stat().st_mode)).encode("ascii"))
    digest.update(b"\0")
    with path.open("rb") as input_file:
        while chunk := input_file.read(64 * 1024):
            digest.update(chunk)
    return digest.hexdigest()


def parse_status_entries(raw: bytes) -> dict[str, str]:
    records = raw.split(b"\0")
    result = {}
    index = 0
    while index < len(records):
        record = records[index]
        index += 1
        if not record:
            continue
        if len(record) < 4 or b" " != record[2:3]:
            raise ValueError("unexpected git status --porcelain output")
        status = record[:2].decode("ascii")
        path = record[3:].decode("utf-8", errors="surrogateescape")
        result[path] = status
        if status[0] in "RC" or status[1] in "RC":
            if index >= len(records) or not records[index]:
                raise ValueError("incomplete rename entry in git status output")
            old_path = records[index].decode("utf-8", errors="surrogateescape")
            result[old_path] = "rename-source"
            index += 1
    return result


def is_maven_target_output(repo_root: Path, path: Path, module_cache: dict[Path, bool]) -> bool:
    for index, each in enumerate(path.parts):
        if MAVEN_OUTPUT_DIRECTORY != each:
            continue
        module_path = Path(*path.parts[:index])
        if module_path not in module_cache:
            module_cache[module_path] = (repo_root / module_path / "pom.xml").is_file()
        if module_cache[module_path]:
            return True
    return False


def is_reproducible_verification_output(repo_root: Path, path: str, status: str, module_cache: dict[Path, bool]) -> bool:
    if status not in ("??", "!!"):
        return False
    relative_path = Path(path)
    python_cache = PYTHON_CACHE_DIRECTORY in relative_path.parts and relative_path.suffix in PYTHON_CACHE_SUFFIXES
    return python_cache or is_maven_target_output(repo_root, relative_path, module_cache)


def collect_worktree_entries(repo_root: Path, allowed_paths: set[str]) -> dict[str, dict[str, str]]:
    completed = subprocess.run(
        ["git", "status", "--porcelain=v1", "-z", "--untracked-files=all", "--ignored=traditional"],
        cwd=repo_root, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    )
    module_cache = {}
    return {
        path: {"status": status, "fingerprint": fingerprint_path(repo_root / path)}
        for path, status in parse_status_entries(completed.stdout).items()
        if path not in allowed_paths and not is_reproducible_verification_output(repo_root, path, status, module_cache)
    }


def build_scope_snapshot(repo_root: Path, allowed_paths: list[Path]) -> dict:
    normalized_allowed = normalize_repo_paths(repo_root, allowed_paths)
    validate_test_scope_paths(normalized_allowed)
    return {
        "version": 1,
        "repo_root": str(repo_root),
        "head": get_head(repo_root),
        "allowed_paths": normalized_allowed,
        "allowed_contents": {
            each: (repo_root / each).read_text(encoding="utf-8") if each.endswith(".java") and (repo_root / each).is_file() else None
            for each in normalized_allowed
        },
        "outside_entries": collect_worktree_entries(repo_root, set(normalized_allowed)),
    }


def write_scope_baseline(output_path: Path, repo_root: Path, allowed_paths: list[Path]) -> None:
    repo_root = repo_root.resolve()
    output_path = output_path.resolve()
    try:
        output_path.relative_to(repo_root)
    except ValueError:
        pass
    else:
        raise ValueError("scope baseline must be stored outside the repository")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(build_scope_snapshot(repo_root, allowed_paths), indent=2, sort_keys=True) + "\n"
    descriptor = os.open(output_path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(descriptor, "w", encoding="utf-8") as output_file:
        output_file.write(payload)


def load_scope_baseline(baseline_path: Path) -> dict:
    result = json.loads(baseline_path.read_text(encoding="utf-8"))
    if not isinstance(result, dict) or 1 != result.get("version"):
        raise ValueError(f"invalid scope baseline: {baseline_path}")
    expected_types = {
        "repo_root": str,
        "allowed_paths": list,
        "allowed_contents": dict,
        "outside_entries": dict,
    }
    if any(not isinstance(result.get(key), expected_type) for key, expected_type in expected_types.items()):
        raise ValueError(f"invalid scope baseline: {baseline_path}")
    if result.get("head") is not None and not isinstance(result.get("head"), str):
        raise ValueError(f"invalid scope baseline: {baseline_path}")
    return result


def check_scope_baseline(baseline_path: Path, allowed_paths: list[Path]) -> list[str]:
    if not baseline_path.exists():
        return [f"missing scope baseline: {baseline_path}"]
    baseline = load_scope_baseline(baseline_path)
    repo_root = Path(baseline["repo_root"])
    if get_head(repo_root) != baseline.get("head"):
        return ["repository HEAD differs from scope baseline"]
    normalized_allowed = normalize_repo_paths(repo_root, allowed_paths)
    if normalized_allowed != baseline.get("allowed_paths"):
        return ["resolved test file set differs from scope baseline"]
    current = collect_worktree_entries(repo_root, set(normalized_allowed))
    before = baseline.get("outside_entries", {})
    return sorted(path for path in set(before) | set(current) if before.get(path) != current.get(path))


def get_added_lines_for_path(path: Path, scope_baseline: dict | None = None) -> list[str]:
    if scope_baseline is None:
        return []
    repo_root = Path(scope_baseline["repo_root"])
    relative_path = normalize_repo_paths(repo_root, [path])[0]
    before = scope_baseline.get("allowed_contents", {}).get(relative_path) or ""
    after = path.read_text(encoding="utf-8") if path.exists() else ""
    return [f"+{each[2:]}" for each in difflib.ndiff(before.splitlines(), after.splitlines()) if each.startswith("+ ")]
