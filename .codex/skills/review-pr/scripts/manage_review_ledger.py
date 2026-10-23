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

"""Manage private temporary coverage ledgers for the review-pr skill."""

from __future__ import annotations

import argparse
import difflib
import hashlib
import json
import os
import re
import stat
import subprocess
import sys
import tempfile
import time
from collections import Counter
from pathlib import Path, PurePosixPath
from typing import Any, Iterable

from resolve_review_scope import (
    categorize, compare_github_files, get_repo_root, parse_name_status, resolve_candidate_changes, run_git,
)


LEDGER_KIND = "review-pr-coverage-ledger"
LEDGER_VERSION = 4
LEDGER_FILE_NAME = "ledger.json"
FILE_STATUSES = frozenset({"pending", "reviewed", "churn-only", "test-only-reviewed", "not-applicable", "blocked"})
FINAL_FILE_STATUSES = FILE_STATUSES - {"pending"}
SUBSTANTIVE_FILE_STATUSES = frozenset({"reviewed", "test-only-reviewed"})
EXPLAINED_FILE_STATUSES = frozenset({"churn-only", "not-applicable"})
FINDING_STATUSES = frozenset({"candidate", "confirmed", "withdrawn", "review-incomplete-gap", "non-blocking", "out-of-scope"})
PASS_FOCUSES = ("root-cause", "blast-radius", "tests-runtime", "convergence")
REVIEW_FOCUSES = ("Code Correctness Review", "Mergeability Review", "CI Review")
SOURCE_ROLES = frozenset({"rule", "requirement", "evidence"})


def ledger_root() -> Path:
    result = Path(tempfile.gettempdir()) / "codex-review-pr"
    if result.exists():
        if result.is_symlink() or not result.is_dir():
            raise RuntimeError(f"Invalid review ledger root: {result}")
        if result.stat().st_uid != os.getuid():
            raise RuntimeError(f"Review ledger root has an unexpected owner: {result}")
    else:
        result.mkdir(mode=0o700)
    result.chmod(0o700)
    return result.resolve()


def sanitize(value: str) -> str:
    return re.sub(r"[^A-Za-z0-9_.-]+", "-", value).strip("-") or "unknown"


def create_ledger_dir(repo_root: Path, pr: str, head_sha: str) -> Path:
    prefix = f"{sanitize(repo_root.name)}-pr-{sanitize(pr)}-{head_sha[:12]}-"
    return Path(tempfile.mkdtemp(prefix=prefix, dir=ledger_root()))


def resolve_ledger_file(value: str | Path) -> Path:
    result = Path(value)
    return result / LEDGER_FILE_NAME if result.is_dir() else result


def ensure_safe_ledger_file(ledger_file: Path) -> Path:
    root = ledger_root().resolve()
    target = ledger_file.resolve()
    if LEDGER_FILE_NAME != target.name or root != target.parent.parent:
        raise RuntimeError(f"Ledger is outside the private review ledger root: {target}")
    return target


def validate_schema(ledger: object) -> None:
    if not isinstance(ledger, dict):
        raise RuntimeError("Malformed review coverage ledger")
    if LEDGER_KIND != ledger.get("kind") or LEDGER_VERSION != ledger.get("version"):
        raise RuntimeError("Unsupported or invalid review coverage ledger")
    required_types = {
        "scope": dict,
        "files": list,
        "findings": list,
        "passes": list,
        "review_revision": int,
        "basis": dict,
    }
    if any(not isinstance(ledger.get(key), expected_type) for key, expected_type in required_types.items()):
        raise RuntimeError("Malformed review coverage ledger")
    if not isinstance(ledger["scope"].get("github_files", {}), dict):
        raise RuntimeError("Malformed review coverage ledger scope")
    scope_types = {"repo_root": str, "base_sha": str, "head_sha": str, "merge_base": str, "head_ref": str, "changed_file_count": int,
                   "candidate_files": dict, "review_basis_file": str}
    if any(not isinstance(ledger["scope"].get(key), value) for key, value in scope_types.items()):
        raise RuntimeError("Malformed review coverage ledger scope")
    basis_types = {"focus": str, "baseline": dict, "delta": list, "sources": list}
    if any(not isinstance(ledger["basis"].get(key), value) for key, value in basis_types.items()):
        raise RuntimeError("Malformed review coverage ledger basis")
    candidate_files = ledger["scope"]["candidate_files"]
    if not isinstance(candidate_files.get("provided"), bool) or not isinstance(candidate_files.get("path"), str):
        raise RuntimeError("Malformed review coverage ledger candidate scope")
    if any(not has_text(ledger["basis"]["baseline"].get(key)) for key in ("commit", "tree")):
        raise RuntimeError("Malformed review coverage ledger original baseline")
    for entry in ledger["basis"]["delta"]:
        if not isinstance(entry, dict) or not has_text(entry.get("path")) or any(not isinstance(entry.get(key), dict) for key in ("base", "candidate")):
            raise RuntimeError("Malformed review coverage ledger delta")
    entry_schemas = (
        ("file", ledger["files"], {
            "path": str, "status": str, "clusters": list, "risk_axes": list, "findings": list,
            "base": dict, "candidate": dict, "blocks": list, "coverage": list,
        }),
        ("finding", ledger["findings"], {
            "id": str, "status": str, "origin": str, "fix_boundary": str, "evidence": list, "full_path": list,
            "counter_evidence": list, "necessity": str, "scope_proof": str, "files": list, "reason": str, "anchors": list,
        }),
        ("pass", ledger["passes"], {
            "focus": str, "new_findings": int, "review_revision": int, "basis_fingerprint": str,
        }),
    )
    for entry_name, entries, schema in entry_schemas:
        for entry in entries:
            if not isinstance(entry, dict) or any(not isinstance(entry.get(key), expected_type) for key, expected_type in schema.items()):
                raise RuntimeError(f"Malformed review coverage ledger {entry_name} entry")
    list_fields = ((ledger["files"], ("clusters", "risk_axes", "findings")),
                   (ledger["findings"], ("evidence", "full_path", "counter_evidence", "files")))
    for entries, fields in list_fields:
        for entry in entries:
            if any(not all(isinstance(value, str) for value in entry[field]) for field in fields):
                raise RuntimeError("Malformed review coverage ledger list entry")
    for entry in ledger["files"]:
        if "old_path" not in entry or entry["old_path"] is not None and not isinstance(entry["old_path"], str):
            raise RuntimeError("Malformed review coverage ledger file entry")
        for block in entry["blocks"]:
            fields = {"id": str, "kind": str, "base_path": str, "candidate_path": str,
                      "base_start": int, "base_count": int, "candidate_start": int, "candidate_count": int}
            if not isinstance(block, dict) or any(not isinstance(block.get(key), value) for key, value in fields.items()):
                raise RuntimeError("Malformed review coverage ledger block")
        for coverage in entry["coverage"]:
            validate_coverage_shape(coverage, captured=True)
    for finding in ledger["findings"]:
        for anchor in finding["anchors"]:
            validate_anchor_shape(anchor, captured=True)
    for source in ledger["basis"]["sources"]:
        validate_source_shape(source)
        if any(not has_text(source.get(key)) for key in ("mode", "sha256")):
            raise RuntimeError("Malformed review coverage ledger source snapshot")


def read_ledger(value: str | Path) -> dict[str, Any]:
    ledger_file = ensure_safe_ledger_file(resolve_ledger_file(value))
    ledger = json.loads(ledger_file.read_text(encoding="utf-8"))
    validate_schema(ledger)
    return ledger


def write_ledger(ledger_file: Path, ledger: dict[str, Any]) -> None:
    ledger_file = ensure_safe_ledger_file(ledger_file)
    ledger["updated_at"] = int(time.time())
    temporary_file = ledger_file.with_suffix(".tmp")
    temporary_file.write_text(json.dumps(ledger, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    temporary_file.chmod(0o600)
    temporary_file.replace(ledger_file)
    ledger_file.chmod(0o600)


def find_file_entry(ledger: dict[str, Any], file_path: str) -> dict[str, Any]:
    for each in ledger["files"]:
        if file_path == each["path"]:
            return each
    raise RuntimeError(f"File is not in ledger scope: {file_path}")


def unique_extend(values: list[str], additions: Iterable[str]) -> None:
    for each in additions:
        if each not in values:
            values.append(each)


def has_text(value: object) -> bool:
    return isinstance(value, str) and bool(value.strip())


def has_text_entries(values: Iterable[object]) -> bool:
    entries = list(values)
    return bool(entries) and all(has_text(each) for each in entries)


def digest(value: Any) -> str:
    return hashlib.sha256(json.dumps(value, sort_keys=True, separators=(",", ":")).encode()).hexdigest()


def validate_source_shape(source: Any) -> None:
    if not isinstance(source, dict) or any(not has_text(source.get(key)) for key in ("role", "origin", "path")) or source["role"] not in SOURCE_ROLES:
        raise RuntimeError("Review basis sources require role, origin, and a readable material path")


def validate_anchor_shape(anchor: Any, captured: bool = False) -> None:
    if not isinstance(anchor, dict) or anchor.get("side") not in {"base", "candidate", "source"} or not has_text(anchor.get("path")):
        raise RuntimeError("Source anchors require side and path")
    if "source" == anchor["side"] and not has_text(anchor.get("origin")):
        raise RuntimeError("Source anchors require an explicit declared origin")
    if ("start_line" in anchor) != ("end_line" in anchor):
        raise RuntimeError("Source anchors require both line bounds or neither")
    for key in ("start_line", "end_line"):
        if key in anchor and type(anchor[key]) is not int:
            raise RuntimeError("Source anchor line bounds must be integers")
    if captured and any(not has_text(anchor.get(key)) for key in ("sha256", "mode", "range_sha256")):
        raise RuntimeError("Captured source anchors require content fingerprints")
    if captured and any(key not in anchor for key in ("start_line", "end_line")):
        raise RuntimeError("Captured source anchors require normalized line bounds")


def validate_coverage_shape(coverage: Any, captured: bool = False) -> None:
    if not isinstance(coverage, dict) or any(not has_text(coverage.get(key)) for key in ("block", "behavior", "counter_evidence")):
        raise RuntimeError("Block coverage requires block, behavior, and counter-evidence")
    if any(not isinstance(coverage.get(key), list) or not has_text_entries(coverage[key]) for key in ("risk_axes", "requirements")):
        raise RuntimeError("Block coverage requires risk axes and applicable requirement origins")
    if not isinstance(coverage.get("anchors"), list) or not coverage["anchors"]:
        raise RuntimeError("Block coverage requires real source anchors")
    for anchor in coverage["anchors"]:
        validate_anchor_shape(anchor, captured=captured)


def read_object(repo_root: Path, scope: dict[str, Any], side: str, path: str) -> tuple[dict[str, Any], bytes | None]:
    if "source" != side and (PurePosixPath(path).is_absolute() or ".." in PurePosixPath(path).parts or "." == path):
        raise RuntimeError(f"Invalid repository-relative source anchor path: {path}")
    if "base" == side or "candidate" == side and not scope["candidate_files"]["provided"]:
        ref = scope["merge_base"] if "base" == side else scope["head_ref"]
        process = subprocess.run(["git", "ls-tree", "-z", ref, "--", path], cwd=repo_root, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                 env={**os.environ, "GIT_LITERAL_PATHSPECS": "1"}, check=False)
        if process.returncode:
            raise RuntimeError(f"Cannot read {side} object: {path}")
        objects = [each.split(b"\t", 1) for each in process.stdout.split(b"\0") if each]
        entry = next((value for metadata, value in objects if value.decode() == path), None)
        if entry is None:
            return {"mode": None, "sha256": None}, None
        metadata = next(metadata for metadata, value in objects if value == entry).decode().split()
        mode, object_type, object_id = metadata
        if "tree" == object_type:
            return {"mode": None, "sha256": None}, None
        if "commit" == object_type:
            content = object_id.encode()
        else:
            blob = subprocess.run(["git", "cat-file", "blob", object_id], cwd=repo_root, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
            if blob.returncode:
                raise RuntimeError(f"Cannot read {side} object bytes: {path}")
            content = blob.stdout
    else:
        source_path = Path(path) if Path(path).is_absolute() else repo_root / path
        try:
            metadata = source_path.stat() if "source" == side else source_path.lstat()
        except (FileNotFoundError, NotADirectoryError):
            return {"mode": None, "sha256": None}, None
        if stat.S_ISLNK(metadata.st_mode):
            mode, content = "120000", os.fsencode(os.readlink(source_path))
        elif stat.S_ISDIR(metadata.st_mode) and "candidate" == side:
            index = subprocess.run(["git", "ls-files", "--stage", "-z", "--", path], cwd=repo_root, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                   env={**os.environ, "GIT_LITERAL_PATHSPECS": "1"}, check=False)
            if index.returncode:
                raise RuntimeError(f"Cannot determine candidate Git type: {path}")
            entries = [each.split(b"\t", 1) for each in index.stdout.split(b"\0") if each]
            if not any(value.decode() == path and metadata.split()[0] == b"160000" for metadata, value in entries):
                return {"mode": None, "sha256": None}, None
            if get_repo_root(source_path).resolve() != source_path.resolve():
                raise RuntimeError(f"Gitlink checkout must have its own repository: {path}")
            if run_git(["status", "--porcelain", "--untracked-files=no"], source_path).strip():
                raise RuntimeError(f"Gitlink checkout has staged or unstaged tracked changes: {path}")
            mode, content = "160000", run_git(["rev-parse", "HEAD"], source_path).strip().encode()
        else:
            mode = "100755" if metadata.st_mode & stat.S_IXUSR else "100644"
            content = source_path.read_bytes()
    return {"mode": mode, "sha256": hashlib.sha256(content).hexdigest()}, content


def changed_blocks(path: str, base_path: str, base: dict[str, Any], candidate: dict[str, Any], old: bytes | None, new: bytes | None) -> list[dict[str, Any]]:
    result = []
    try:
        old_lines, new_lines = (old or b"").decode("utf-8").splitlines(keepends=True), (new or b"").decode("utf-8").splitlines(keepends=True)
        opaque = b"\0" in (old or b"") or b"\0" in (new or b"") or base["mode"] in {"120000", "160000"} or candidate["mode"] in {"120000", "160000"}
    except UnicodeDecodeError:
        old_lines, new_lines, opaque = [], [], True
    if old != new and opaque:
        result.append({"kind": "binary", "base_start": 0, "base_count": 0, "candidate_start": 0, "candidate_count": 0})
    elif old != new:
        for tag, old_start, old_end, new_start, new_end in difflib.SequenceMatcher(None, old_lines, new_lines).get_opcodes():
            if "equal" != tag:
                result.append({"kind": "text", "base_start": old_start + 1, "base_count": old_end - old_start,
                               "candidate_start": new_start + 1, "candidate_count": new_end - new_start})
    if base_path != path:
        result.append({"kind": "rename", "base_start": 0, "base_count": 0, "candidate_start": 0, "candidate_count": 0})
    if base["mode"] and candidate["mode"] and base["mode"] != candidate["mode"]:
        result.append({"kind": "mode", "base_start": 0, "base_count": 0, "candidate_start": 0, "candidate_count": 0})
    if not result and base != candidate:
        result.append({"kind": "object", "base_start": 0, "base_count": 0, "candidate_start": 0, "candidate_count": 0})
    for block in result:
        block.update({"base_path": base_path, "candidate_path": path})
        block["id"] = digest({**block, "base": base, "candidate": candidate})
    return result


def capture_basis(scope: dict[str, Any]) -> tuple[dict[str, Any], list[dict[str, Any]]]:
    repo_root = Path(scope["repo_root"])
    configuration = json.loads(Path(scope["review_basis_file"]).read_text(encoding="utf-8"))
    if not isinstance(configuration, dict) or configuration.get("focus") not in REVIEW_FOCUSES or not isinstance(configuration.get("sources"), list):
        raise RuntimeError("Review basis requires a supported focus and declared sources")
    sources = []
    for source in configuration["sources"]:
        validate_source_shape(source)
        snapshot, content = read_object(repo_root, scope, "source", source["path"])
        if content is None:
            raise RuntimeError(f"Review basis source is unavailable: {source['origin']}")
        sources.append({**source, **snapshot})
    if not any(each["role"] in {"rule", "requirement"} for each in sources) or len(sources) != len({each["origin"] for each in sources}):
        raise RuntimeError("Review basis requires unambiguous applicable rule or requirement sources")
    candidate_files = scope["candidate_files"]
    changes = resolve_candidate_changes(repo_root, scope["merge_base"], candidate_files["path"]) if candidate_files["provided"] else parse_name_status(
        run_git(["diff", "--name-status", f"{scope['merge_base']}..{scope['head_ref']}"], repo_root))
    files = []
    paths = sorted({path for change in changes for path in (change.old_path or change.path, change.path)})
    objects = {(side, path): read_object(repo_root, scope, side, path) for path in paths for side in ("base", "candidate")}
    for change in changes:
        base_path = change.old_path or change.path
        base, old = objects[("base", base_path)]
        candidate, new = objects[("candidate", change.path)]
        files.append({"path": change.path, "old_path": change.old_path, "git_status": change.status, "base": base, "candidate": candidate,
                      "blocks": changed_blocks(change.path, base_path, base, candidate, old, new)})
    delta = [{"path": path, "base": objects[("base", path)][0], "candidate": objects[("candidate", path)][0]} for path in paths]
    return {"focus": configuration["focus"], "baseline": {"commit": scope["merge_base"], "tree": run_git(["rev-parse", f"{scope['merge_base']}^{{tree}}"], repo_root).strip()},
            "sources": sources, "delta": delta}, files


def capture_anchor(anchor: dict[str, Any], ledger: dict[str, Any]) -> dict[str, Any]:
    if "source" == anchor["side"]:
        source = next((each for each in ledger["basis"]["sources"] if each["origin"] == anchor["origin"] and each["path"] == anchor["path"]), None)
        if source is None:
            raise RuntimeError("Source anchor does not match a declared material and origin")
    snapshot, content = read_object(Path(ledger["scope"]["repo_root"]), ledger["scope"], anchor["side"], anchor["path"])
    if content is None:
        raise RuntimeError(f"Source anchor is unavailable: {anchor['side']}:{anchor['path']}")
    start, end = anchor.get("start_line", 0), anchor.get("end_line", 0)
    if start or end:
        try:
            lines = content.decode("utf-8").splitlines(keepends=True)
        except UnicodeDecodeError as ex:
            raise RuntimeError("Line anchors require UTF-8 text; use a whole-object anchor for binary evidence") from ex
        if start < 1 or end < start or end > len(lines):
            raise RuntimeError("Source anchor line range is outside the actual source")
        selected = "".join(lines[start - 1:end]).encode("utf-8")
    else:
        selected = content
    return {**anchor, **snapshot, "start_line": start, "end_line": end, "range_sha256": hashlib.sha256(selected).hexdigest()}


def anchor_covers_block(anchor: dict[str, Any], block: dict[str, Any]) -> bool:
    side = anchor["side"]
    if side not in {"base", "candidate"} or anchor["path"] != block[f"{side}_path"]:
        return False
    if "text" != block["kind"]:
        return not anchor["start_line"] and not anchor["end_line"]
    count, start = block[f"{side}_count"], block[f"{side}_start"]
    return count > 0 and anchor["start_line"] > 0 and anchor["start_line"] <= start + count - 1 and anchor["end_line"] >= start


def coverage_errors(entry: dict[str, Any], requirement_origins: set[str]) -> list[str]:
    errors = []
    block_ids = {each["id"] for each in entry["blocks"]}
    covered_blocks = {each["block"] for each in entry["coverage"]}
    if covered_blocks - block_ids:
        errors.append(f"{entry['path']} has unknown changed-block coverage")
    if entry["status"] in SUBSTANTIVE_FILE_STATUSES and block_ids - covered_blocks:
        errors.append(f"{entry['path']} has uncovered changed blocks: {len(block_ids - covered_blocks)}")
    for coverage in entry["coverage"]:
        if set(coverage["requirements"]) - requirement_origins:
            errors.append(f"{entry['path']} block coverage references unknown applicable requirement origins")
        block = next((each for each in entry["blocks"] if each["id"] == coverage["block"]), None)
        if block is not None and not any(anchor_covers_block(each, block) for each in coverage["anchors"]):
            errors.append(f"{entry['path']} block coverage has no anchor at the actual change")
    return errors


def all_anchors(ledger: dict[str, Any]) -> Iterable[dict[str, Any]]:
    for entry in ledger["files"]:
        for coverage in entry["coverage"]:
            yield from coverage["anchors"]
    for finding in ledger["findings"]:
        yield from finding["anchors"]


def basis_fingerprint(ledger: dict[str, Any]) -> str:
    basis = ledger["basis"]
    scope_paths = {each["path"] for each in basis["delta"]}
    evidence = {(each["side"], each["path"], each["mode"], each["sha256"]) for each in all_anchors(ledger)
                if "source" != each["side"] and each["path"] not in scope_paths}
    return digest({"focus": basis["focus"], "baseline": basis["baseline"]["tree"], "delta": basis["delta"],
                   "sources": sorted((each["role"], each["origin"], each["mode"], each["sha256"]) for each in basis["sources"]),
                   "evidence": sorted(evidence)})


def validate_current_basis(ledger: dict[str, Any]) -> list[str]:
    current, files = capture_basis(ledger["scope"])
    errors = []
    if current != ledger["basis"]:
        errors.append("Effective candidate, original baseline, focus, or declared review sources changed; initialize a new ledger")
    snapshots = [{key: each[key] for key in ("path", "old_path", "base", "candidate", "blocks")} for each in ledger["files"]]
    actual = [{key: each[key] for key in ("path", "old_path", "base", "candidate", "blocks")} for each in files]
    if snapshots != actual or ledger["scope"]["changed_file_count"] != len(files):
        errors.append("Ledger files, changed-file count, or changed blocks do not match the effective candidate")
    if ledger["scope"].get("github_files_file"):
        comparison = compare_github_files((each["path"] for each in files), ledger["scope"]["github_files_file"])
        if comparison != ledger["scope"]["github_files"]:
            errors.append("Authoritative GitHub file material changed or no longer matches the ledger")
    if errors:
        return errors
    for anchor in all_anchors(ledger):
        if capture_anchor(anchor, ledger) != anchor:
            errors.append(f"Source anchor is stale: {anchor['side']}:{anchor['path']}")
    return errors


def sync_file_findings(ledger: dict[str, Any]) -> None:
    for each in ledger["files"]:
        each["findings"] = []
    for finding in ledger["findings"]:
        for file_path in finding["files"]:
            unique_extend(find_file_entry(ledger, file_path)["findings"], [finding["id"]])


def cmd_init(args: argparse.Namespace) -> int:
    repo_root = get_repo_root(Path(args.repo_root))
    base_sha = run_git(["rev-parse", args.base_ref], repo_root).strip()
    head_sha = run_git(["rev-parse", args.head_ref], repo_root).strip()
    merge_base = run_git(["merge-base", args.base_ref, args.head_ref], repo_root).strip()
    candidate_files = getattr(args, "candidate_files", None)
    scope = {
        "repo": repo_root.name,
        "repo_root": str(repo_root),
        "pr": args.pr,
        "base_ref": args.base_ref,
        "base_sha": base_sha,
        "head_ref": args.head_ref,
        "head_sha": head_sha,
        "merge_base": merge_base,
        "candidate_files": {"provided": bool(candidate_files), "path": str(Path(candidate_files).resolve()) if candidate_files else ""},
        "review_basis_file": str(Path(args.review_basis).resolve()),
        "github_files_file": str(Path(args.github_files).resolve()) if args.github_files else "",
    }
    basis, changed_files = capture_basis(scope)
    scope.update({"changed_file_count": len(changed_files), "github_files": compare_github_files((each["path"] for each in changed_files), args.github_files)})
    ledger_dir = create_ledger_dir(repo_root, args.pr, head_sha)
    ledger_file = ledger_dir / LEDGER_FILE_NAME
    now = int(time.time())
    ledger = {
        "kind": LEDGER_KIND,
        "version": LEDGER_VERSION,
        "created_at": now,
        "updated_at": now,
        "review_revision": 0,
        "scope": scope,
        "basis": basis,
        "files": [{
            **each,
            "category": categorize(each["path"]),
            "status": "pending",
            "clusters": [],
            "risk_axes": [],
            "findings": [],
            "coverage": [],
            "reason": "",
        } for each in changed_files],
        "findings": [],
        "passes": [],
    }
    try:
        write_ledger(ledger_file, ledger)
    except (OSError, RuntimeError):
        try:
            remove_ledger_dir(ledger_file)
        except (OSError, RuntimeError) as ex:
            print(f"error: Initialization cleanup failed; current-review resources may remain at {ledger_dir}: {ex}", file=sys.stderr)
        raise
    print(f"ledger={ledger_file}")
    print(f"files={len(changed_files)}")
    return 0


def cmd_mark_file(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    errors = validate_current_basis(ledger)
    if errors:
        raise RuntimeError("; ".join(errors))
    apply_mark_file(ledger, args)
    write_ledger(ledger_file, ledger)
    print(f"marked={args.path} status={args.status}")
    return 0


def apply_mark_file(ledger: dict[str, Any], args: argparse.Namespace) -> None:
    entry = find_file_entry(ledger, args.path)
    if args.coverage:
        coverage = json.loads(Path(args.coverage).read_text(encoding="utf-8"))
        if not isinstance(coverage, list):
            raise RuntimeError("File coverage material must be a list of reviewed blocks")
        for block_review in coverage:
            validate_coverage_shape(block_review)
            block_review["anchors"] = [capture_anchor(each, ledger) for each in block_review["anchors"]]
        entry["coverage"] = coverage
    entry["status"] = args.status
    unique_extend(entry["clusters"], args.cluster or [])
    unique_extend(entry["risk_axes"], args.risk_axis or [])
    entry["reason"] = args.reason or ""
    errors = coverage_errors(entry, {each["origin"] for each in ledger["basis"]["sources"] if each["role"] in {"rule", "requirement"}})
    if errors:
        raise RuntimeError("; ".join(errors))
    ledger["review_revision"] += 1


def cmd_add_finding(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    errors = validate_current_basis(ledger)
    if errors:
        raise RuntimeError("; ".join(errors))
    apply_add_finding(ledger, args)
    write_ledger(ledger_file, ledger)
    print(f"finding={args.id} status={args.status}")
    return 0


def apply_add_finding(ledger: dict[str, Any], args: argparse.Namespace) -> None:
    anchors = json.loads(Path(args.anchors).read_text(encoding="utf-8")) if args.anchors else []
    if not isinstance(anchors, list):
        raise RuntimeError("Finding anchors material must be a list")
    for anchor in anchors:
        validate_anchor_shape(anchor)
    finding = {
        "id": args.id,
        "status": args.status,
        "origin": args.origin or "",
        "fix_boundary": args.fix_boundary or "",
        "evidence": args.evidence or [],
        "full_path": args.full_path or [],
        "counter_evidence": args.counter_evidence or [],
        "necessity": args.necessity or "",
        "scope_proof": args.scope_proof or "",
        "files": args.file or [],
        "reason": args.reason or "",
        "anchors": [capture_anchor(each, ledger) for each in anchors],
    }
    existing = next((each for each in ledger["findings"] if args.id == each["id"]), None)
    if existing:
        existing.update(finding)
    else:
        ledger["findings"].append(finding)
    sync_file_findings(ledger)
    ledger["review_revision"] += 1


def cmd_add_pass(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    errors = validate_current_basis(ledger)
    if errors:
        raise RuntimeError("; ".join(errors))
    apply_add_pass(ledger, args)
    write_ledger(ledger_file, ledger)
    print(f"pass={args.focus} new_findings={args.new_findings}")
    return 0


def apply_add_pass(ledger: dict[str, Any], args: argparse.Namespace) -> None:
    if 0 > args.new_findings:
        raise RuntimeError("New finding count must not be negative")
    ledger["passes"].append({
        "focus": args.focus,
        "new_findings": args.new_findings,
        "review_revision": ledger["review_revision"],
        "finding_count": len(ledger["findings"]),
        "created_at": int(time.time()),
        "basis_fingerprint": basis_fingerprint(ledger),
    })


def cmd_batch(args: argparse.Namespace) -> int:
    operations = json.loads(Path(args.operations).read_text(encoding="utf-8"))
    if not isinstance(operations, list) or not operations:
        raise RuntimeError("Batch operations must be a nonempty list of argument lists")
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    errors = validate_current_basis(ledger)
    if errors:
        raise RuntimeError("; ".join(errors))
    parser = build_parser()
    updates = {"mark-file": apply_mark_file, "add-finding": apply_add_finding, "add-pass": apply_add_pass}
    for operation in operations:
        if not isinstance(operation, list) or not operation or not all(isinstance(each, str) for each in operation):
            raise RuntimeError("Each batch operation must be a nonempty list of argument strings")
        if operation[0] not in updates:
            raise RuntimeError("Batch operations support only mark-file, add-finding and add-pass")
        try:
            update = parser.parse_args([operation[0], "--ledger", str(ledger_file), *operation[1:]])
        except SystemExit as ex:
            raise RuntimeError("Invalid batch operation arguments") from ex
        if update.ledger != str(ledger_file):
            raise RuntimeError("Every batch operation must use the outer ledger")
        updates[update.command](ledger, update)
    errors = validate_current_basis(ledger)
    if errors:
        raise RuntimeError("; ".join(errors))
    write_ledger(ledger_file, ledger)
    print(f"batched={len(operations)}")
    return 0


def validate_ledger(ledger: dict[str, Any], incomplete_result: bool = False) -> list[str]:
    validate_schema(ledger)
    return _validate_ledger(ledger, incomplete_result)


def _validate_ledger(ledger: dict[str, Any], incomplete_result: bool = False, current_basis: bool = True) -> list[str]:
    result = validate_current_basis(ledger) if current_basis else []
    requirement_origins = {each["origin"] for each in ledger["basis"]["sources"] if each["role"] in {"rule", "requirement"}}
    for entry in ledger["files"]:
        result.extend(coverage_errors(entry, requirement_origins))
    github_files = ledger["scope"].get("github_files", {})
    if github_files.get("provided") and not github_files.get("matched"):
        result.append("GitHub file list does not match local triple-dot scope")
    file_counts = Counter(each["path"] for each in ledger["files"])
    duplicate_files = [file_path for file_path, count in file_counts.items() if 1 < count]
    if duplicate_files:
        result.append(f"Duplicate scope files remain: {len(duplicate_files)}")
    pending_files = [each["path"] for each in ledger["files"] if "pending" == each["status"]]
    if pending_files:
        result.append(f"Pending files remain: {len(pending_files)}")
    invalid_files = [each["path"] for each in ledger["files"] if each["status"] not in FINAL_FILE_STATUSES]
    if invalid_files:
        result.append(f"Invalid final file statuses remain: {len(invalid_files)}")
    incomplete_gaps = [each for each in ledger["findings"] if "review-incomplete-gap" == each["status"]]
    if incomplete_result and not incomplete_gaps:
        result.append("Review Incomplete validation requires at least one incomplete gap")
    blocked_files = [each for each in ledger["files"] if "blocked" == each["status"]]
    if incomplete_result:
        incomplete_gap_ids = {each["id"] for each in incomplete_gaps}
        unexplained_blocked_files = [each["path"] for each in blocked_files if not has_text(each.get("reason"))]
        if unexplained_blocked_files:
            result.append(f"Blocked files missing incomplete reasons: {len(unexplained_blocked_files)}")
        unlinked_blocked_files = [each["path"] for each in blocked_files
                                  if incomplete_gap_ids.isdisjoint(each["findings"])]
        if unlinked_blocked_files:
            result.append(f"Blocked files not linked to an incomplete gap: {len(unlinked_blocked_files)}")
    elif blocked_files:
        result.append(f"Blocked files require more evidence or an incomplete result: {len(blocked_files)}")
    missing_clusters = [each["path"] for each in ledger["files"]
                        if each["status"] in SUBSTANTIVE_FILE_STATUSES
                        and not has_text_entries(each["clusters"])]
    if missing_clusters:
        result.append(f"Substantive files missing behavior clusters: {len(missing_clusters)}")
    missing_risk_axes = [each["path"] for each in ledger["files"]
                         if each["status"] in SUBSTANTIVE_FILE_STATUSES
                         and not has_text_entries(each["risk_axes"])]
    if missing_risk_axes:
        result.append(f"Substantive files missing reviewed risk axes: {len(missing_risk_axes)}")
    unexplained_files = [each["path"] for each in ledger["files"]
                         if each["status"] in EXPLAINED_FILE_STATUSES and not has_text(each.get("reason"))]
    if unexplained_files:
        result.append(f"Non-substantive file classifications missing reasons: {len(unexplained_files)}")
    finding_counts = Counter(each["id"] for each in ledger["findings"])
    duplicate_findings = [finding_id for finding_id, count in finding_counts.items() if 1 < count]
    if duplicate_findings:
        result.append(f"Duplicate finding ids remain: {len(duplicate_findings)}")
    fix_boundary_counts = Counter(each["fix_boundary"] for each in ledger["findings"]
                                  if "confirmed" == each["status"] and each["fix_boundary"])
    duplicate_fix_boundaries = [fix_boundary for fix_boundary, count in fix_boundary_counts.items() if 1 < count]
    if duplicate_fix_boundaries:
        result.append(f"Confirmed findings share fix boundaries: {len(duplicate_fix_boundaries)}")
    finding_ids = set(finding_counts)
    scope_files = set(file_counts)
    for each in ledger["files"]:
        unknown_findings = [finding_id for finding_id in each["findings"] if finding_id not in finding_ids]
        if unknown_findings:
            result.append(f"{each['path']} references unknown findings: {', '.join(unknown_findings)}")
    for each in ledger["findings"]:
        if each["status"] not in FINDING_STATUSES:
            result.append(f"{each['id']} has invalid status: {each['status']}")
        if "candidate" == each["status"]:
            result.append(f"{each['id']} is still a candidate")
        unknown_files = [file_path for file_path in each["files"] if file_path not in scope_files]
        if unknown_files:
            result.append(f"{each['id']} references files outside scope: {', '.join(unknown_files)}")
        if "confirmed" == each["status"]:
            if not each["anchors"]:
                result.append(f"{each['id']} confirmed finding is missing real source anchors")
            if not has_text(each["origin"]):
                result.append(f"{each['id']} confirmed finding is missing origin")
            if not has_text(each["fix_boundary"]):
                result.append(f"{each['id']} confirmed finding is missing fix boundary")
            if not has_text_entries(each["evidence"]):
                result.append(f"{each['id']} confirmed finding is missing evidence")
            if not has_text_entries(each["full_path"]):
                result.append(f"{each['id']} confirmed finding is missing full-path review")
            if not has_text_entries(each["counter_evidence"]):
                result.append(f"{each['id']} confirmed finding is missing counter-evidence review")
            if not has_text(each["necessity"]):
                result.append(f"{each['id']} confirmed finding is missing necessity")
            if not has_text(each["scope_proof"]):
                result.append(f"{each['id']} confirmed finding is missing scope proof")
            if not has_text_entries(each["files"]):
                result.append(f"{each['id']} confirmed finding is missing scope files")
        if "review-incomplete-gap" == each["status"]:
            if not has_text(each["reason"]):
                missing_fact = "the unavailable fact" if incomplete_result else "a reason"
                result.append(f"{each['id']} incomplete gap is missing {missing_fact}")
            if incomplete_result:
                if not each["anchors"]:
                    result.append(f"{each['id']} incomplete gap is missing real unavailability-proof anchors")
                if not has_text(each["origin"]):
                    result.append(f"{each['id']} incomplete gap is missing the fact source")
                if not has_text_entries(each["evidence"]):
                    result.append(f"{each['id']} incomplete gap is missing unavailability proof")
                if not has_text_entries(each["full_path"]):
                    result.append(f"{each['id']} incomplete gap is missing the affected full path")
                if not has_text_entries(each["counter_evidence"]):
                    result.append(f"{each['id']} incomplete gap is missing alternative evidence checks")
                if not has_text(each["necessity"]):
                    result.append(f"{each['id']} incomplete gap is missing outcome impact")
                if not has_text(each["scope_proof"]):
                    result.append(f"{each['id']} incomplete gap is missing scope proof")
                if not has_text_entries(each["files"]):
                    result.append(f"{each['id']} incomplete gap is missing affected scope files")
            else:
                result.append(f"{each['id']} requires a Review Incomplete result")
    current_fingerprint = basis_fingerprint(ledger)
    pass_foci = {each.get("focus") for each in ledger["passes"] if each["basis_fingerprint"] == current_fingerprint}
    missing_passes = [each for each in PASS_FOCUSES if each not in pass_foci]
    if missing_passes:
        result.append(f"Required review passes missing: {', '.join(missing_passes)}")
    if ledger["passes"]:
        final_pass = ledger["passes"][-1]
        if "convergence" != final_pass.get("focus"):
            result.append("Latest review pass is not the convergence pass")
        if 0 != final_pass.get("new_findings"):
            result.append("Latest convergence pass did not finish with zero new findings")
        if ledger["review_revision"] != final_pass.get("review_revision"):
            result.append("Latest convergence pass predates a file or finding change")
        if current_fingerprint != final_pass["basis_fingerprint"]:
            result.append("Latest convergence pass uses a different review basis")
    return result


def cmd_validate(args: argparse.Namespace) -> int:
    ledger = read_ledger(args.ledger)
    errors = _validate_ledger(ledger)
    if errors:
        for each in errors:
            print(f"ERROR: {each}")
        return 1
    print("OK: mechanical coverage and evidence are current; correctness requires Formal Review")
    return 0


def cmd_validate_incomplete(args: argparse.Namespace) -> int:
    ledger = read_ledger(args.ledger)
    errors = _validate_ledger(ledger, incomplete_result=True)
    if errors:
        for each in errors:
            print(f"ERROR: {each}")
        return 1
    print("OK: mechanical incomplete-evidence record is current; classification requires Formal Review")
    return 0


def read_review_record(reference: Any) -> dict[str, Any]:
    if not isinstance(reference, dict) or any(not has_text(reference.get(key)) for key in ("path", "sha256")):
        raise RuntimeError("Review references require a path and original content hash")
    content = Path(reference["path"]).read_bytes()
    if hashlib.sha256(content).hexdigest() != reference["sha256"]:
        raise RuntimeError("An original review result changed after collection")
    review = json.loads(content)
    if not isinstance(review, dict) or any(not has_text(review.get(key)) for key in ("reviewer", "ledger", "ledger_sha256")):
        raise RuntimeError("Review results require an isolated reviewer identity and original ledger reference")
    validate_review_decision(review)
    ledger_file = ensure_safe_ledger_file(resolve_ledger_file(review["ledger"]))
    if hashlib.sha256(ledger_file.read_bytes()).hexdigest() != review["ledger_sha256"]:
        raise RuntimeError("An original review ledger changed after collection")
    return {**review, "path": str(Path(reference["path"]).resolve()), "ledger_file": str(ledger_file), "ledger": read_ledger(ledger_file)}


def validate_review_decision(assessment: dict[str, Any]) -> None:
    result, feedback = assessment.get("result"), assessment.get("feedback_mode")
    if not isinstance(result, str) or not isinstance(feedback, str):
        raise RuntimeError("Review result and feedback mode must be strings")
    if result not in {"Mergeable", "Not Mergeable", "Review Incomplete"} or feedback not in {"", "Change Request", "Needs Discussion"}:
        raise RuntimeError("Review decisions require a supported result and feedback mode")
    if ("Not Mergeable" == result) != bool(feedback):
        raise RuntimeError("Only Not Mergeable decisions require a feedback mode")


def review_input_identity(ledger: dict[str, Any]) -> str:
    basis = ledger["basis"]
    return digest({"focus": basis["focus"], "baseline": basis["baseline"]["tree"], "delta": basis["delta"],
                   "requirements": sorted((each["role"], each["origin"], each["mode"], each["sha256"]) for each in basis["sources"]
                                          if each["role"] in {"rule", "requirement"})})


def review_evidence_versions(ledger: dict[str, Any]) -> dict[tuple[str, str, str], tuple[str, str]]:
    result = {("source", "", each["origin"]): (each["mode"], each["sha256"]) for each in ledger["basis"]["sources"]}
    result.update(((each["side"], "" if "source" == each["side"] else each["path"], each.get("origin", "")),
                   (each["mode"], each["sha256"])) for each in all_anchors(ledger))
    return result


def result_review_errors(proof: Any, reviews: dict[str, dict[str, Any]], basis_ledger: dict[str, Any],
                         source_ledgers: dict[str, dict[str, Any]], own_reviewer: str = "", current_basis: bool = True) -> list[str]:
    if not isinstance(proof, dict) or any(not has_text(proof.get(key)) for key in ("reason", "counter_evidence")):
        raise RuntimeError("Result review requires an evidence assessment and strongest counter-evidence")
    if not isinstance(proof.get("evidence"), list) or not proof["evidence"]:
        raise RuntimeError("Result review requires current source evidence")
    errors = []
    for entry in proof["evidence"]:
        if not isinstance(entry, dict) or not has_text(entry.get("reviewer")) or not isinstance(entry.get("anchor"), dict):
            raise RuntimeError("Result-review evidence requires a reviewer and captured source anchor")
        reviewer = entry["reviewer"]
        if reviewer not in reviews or own_reviewer and reviewer != own_reviewer:
            errors.append("Result-review evidence does not belong to the reviewed original result")
            continue
        anchor = entry["anchor"]
        validate_anchor_shape(anchor, captured=True)
        ledger = reviews[reviewer]["ledger"]
        if not current_basis and anchor not in all_anchors(ledger):
            errors.append("Historical result-review evidence is not recorded in its original ledger")
        elif current_basis:
            evidence_ledger = basis_ledger
            current_anchor = anchor
            if "source" == anchor["side"]:
                if not any(each["origin"] == anchor["origin"] and each["path"] == anchor["path"] for each in ledger["basis"]["sources"]):
                    raise RuntimeError("Source anchor does not match a declared material and origin")
                evidence_ledger = source_ledgers.get(anchor["origin"], ledger)
                material_path = next(each["path"] for each in evidence_ledger["basis"]["sources"] if each["origin"] == anchor["origin"])
                current_anchor = {**anchor, "path": material_path}
            if capture_anchor(current_anchor, evidence_ledger) != current_anchor:
                errors.append("Result-review evidence is stale")
    return errors


def validate_session(session: Any) -> list[str]:
    if not isinstance(session, dict) or not isinstance(session.get("reviews"), list) or len(session["reviews"]) < 2:
        raise RuntimeError("A review session requires at least two original results")
    if not isinstance(session.get("rounds"), list) or not session["rounds"]:
        raise RuntimeError("A review session requires substantive result-review stages")
    records = [read_review_record(each) for each in session["reviews"]]
    for key in ("reviewer", "path", "ledger_file"):
        if len(records) != len({each[key] for each in records}):
            raise RuntimeError("Independent reviews must have distinct identities, results and ledgers")
    errors = []
    if len({each["ledger"]["basis"]["baseline"]["tree"] for each in records}) != 1:
        errors.append("The original task baseline changed across review rounds")
    current_start = 0
    current_review_start = 0
    review_offset = 0
    basis_assessments = 0
    previous_identity = None
    source_versions = {}
    stage_records = []
    for index, review_round in enumerate(session["rounds"]):
        if not isinstance(review_round, dict) or not isinstance(review_round.get("report_reviews"), list) or not isinstance(review_round.get("findings"), list):
            raise RuntimeError("Every stage requires report reviews and explicit finding dispositions")
        report_count = len(review_round["report_reviews"])
        if report_count not in {1, 2}:
            raise RuntimeError("Every review stage requires one or two original reports")
        stage = records[review_offset:review_offset + report_count]
        if len(stage) != report_count:
            raise RuntimeError("Review stages must account for every original result exactly once")
        stage_records.append(stage)
        identity = review_input_identity(stage[0]["ledger"])
        if any(identity != review_input_identity(each["ledger"]) for each in stage[1:]):
            errors.append("Review candidates, original baselines, requirements or focus differ within a stage")
        stage_versions = {}
        for record in stage:
            versions = review_evidence_versions(record["ledger"])
            if any(key in stage_versions and stage_versions[key] != value for key, value in versions.items()):
                errors.append("Independent reviews disagree on a shared source version")
            stage_versions.update(versions)
        if identity != previous_identity or any(key in source_versions and source_versions[key] != value for key, value in stage_versions.items()):
            if report_count != 2:
                errors.append("Each review basis requires an initial pair of independent original assessments")
            current_start = index
            current_review_start = review_offset
            basis_assessments = 0
            source_versions = {}
        source_versions.update(stage_versions)
        basis_assessments += report_count
        if basis_assessments > 6:
            errors.append("The current review basis exceeds six independent original assessments")
        previous_identity = identity
        review_offset += report_count
    if review_offset != len(records):
        raise RuntimeError("Review stages must account for every original result exactly once")
    for index, record in enumerate(records):
        ledger = record["ledger"]
        errors.extend(_validate_ledger(ledger, incomplete_result="Review Incomplete" == record["result"], current_basis=index >= current_review_start))
        confirmed = any(each["status"] == "confirmed" for each in ledger["findings"])
        if "Review Incomplete" != record["result"] and confirmed != ("Not Mergeable" == record["result"]):
            errors.append("An original decision contradicts its confirmed finding set")
    basis_ledger = records[current_review_start]["ledger"]
    source_ledgers = {source["origin"]: record["ledger"] for record in records[current_review_start:]
                      for source in record["ledger"]["basis"]["sources"]}
    known_reviews = {}
    original_findings = set()
    prior_ids = set()
    final_groups = []
    for index, review_round in enumerate(session["rounds"]):
        current_basis = index >= current_start
        stage = stage_records[index]
        if index > current_start and len(stage) != 1:
            errors.append("Later stages on an unchanged review basis require one independent original assessment")
        known_reviews.update((each["reviewer"], each) for each in stage)
        for record in stage:
            original_findings.update((record["reviewer"], each["id"]) for each in record["ledger"]["findings"])
        audited_reviewers = []
        for report_review in review_round["report_reviews"]:
            if not isinstance(report_review, dict) or not has_text(report_review.get("reviewer")):
                raise RuntimeError("Every report review requires its original reviewer identity")
            reviewer = report_review["reviewer"]
            audited_reviewers.append(reviewer)
            errors.extend(result_review_errors(report_review, known_reviews, basis_ledger, source_ledgers,
                                                own_reviewer=reviewer, current_basis=current_basis))
        if Counter(audited_reviewers) != Counter(each["reviewer"] for each in stage):
            errors.append("Every original report requires substantive result review in its stage")
        groups, members = {}, {}
        for finding in review_round["findings"]:
            if (not isinstance(finding, dict) or not has_text(finding.get("id")) or not has_text(finding.get("classification"))
                    or not isinstance(finding.get("status"), str) or finding["status"] not in FINDING_STATUSES):
                raise RuntimeError("Finding dispositions require a stable identity, classification and supported status")
            if not isinstance(finding.get("members"), list):
                raise RuntimeError("Finding dispositions require explicit original finding mappings")
            if finding["id"] in groups:
                errors.append("Canonical finding identities are duplicated")
            groups[finding["id"]] = finding
            errors.extend(result_review_errors(finding, known_reviews, basis_ledger, source_ledgers, current_basis=current_basis))
            for member in finding["members"]:
                if not isinstance(member, dict) or any(not has_text(member.get(key)) for key in ("reviewer", "finding")):
                    raise RuntimeError("Finding mappings require an original reviewer and finding identity")
                key = (member["reviewer"], member["finding"])
                if key in members:
                    errors.append("An original finding has multiple dispositions")
                members[key] = finding["id"]
        if set(members) != original_findings:
            errors.append("Every historical candidate requires exactly one explicit disposition")
        if prior_ids - groups.keys():
            errors.append("A previous result-review finding disappeared without a disposition")
        prior_ids = set(groups)
        final_groups = list(groups.values())
        if any(each["status"] == "candidate" for each in final_groups):
            if (review_round.get("result"), review_round.get("feedback_mode")) != ("", ""):
                errors.append("Unresolved result-review candidates require an empty result and feedback mode")
        else:
            validate_review_decision(review_round)
            confirmed = any(each["status"] == "confirmed" for each in final_groups)
            gaps = any(each["status"] == "review-incomplete-gap" for each in final_groups)
            expected_result = "Review Incomplete" if gaps else "Not Mergeable" if confirmed else "Mergeable"
            if review_round["result"] != expected_result:
                errors.append("A round decision contradicts its complete historical dispositions")
    if any(each["status"] == "candidate" for each in final_groups):
        errors.append("Result-review candidates remain unresolved")
    else:
        validate_review_decision(session)
        if (session["result"], session["feedback_mode"]) != (session["rounds"][-1]["result"], session["rounds"][-1]["feedback_mode"]):
            errors.append("The final decision contradicts the latest complete adjudication")
    return errors


def cmd_validate_session(args: argparse.Namespace) -> int:
    errors = validate_session(json.loads(Path(args.session).read_text(encoding="utf-8")))
    if errors:
        for error in errors:
            print(f"ERROR: {error}")
        return 1
    print("OK: review stages and substantive result-review records are complete; semantic judgment remains the reviewer's responsibility")
    return 0


def cmd_status(args: argparse.Namespace) -> int:
    ledger = read_ledger(args.ledger)
    file_counts = Counter(each["status"] for each in ledger["files"])
    finding_counts = Counter(each["status"] for each in ledger["findings"])
    pass_counts = Counter(each["focus"] for each in ledger["passes"])
    print(f"files={dict(sorted(file_counts.items()))}")
    print(f"findings={dict(sorted(finding_counts.items()))}")
    print(f"passes={dict(sorted(pass_counts.items()))}")
    print(f"review_revision={ledger['review_revision']}")
    print(f"basis_fingerprint={basis_fingerprint(ledger)}")
    for entry in ledger["files"]:
        print(f"blocks={entry['path']} " + ",".join(each["id"] for each in entry["blocks"]))
    return 0


def remove_ledger_dir(ledger_file: Path) -> None:
    ledger_file = ensure_safe_ledger_file(ledger_file)
    ledger_dir = ledger_file.parent
    entries = list(ledger_dir.iterdir())
    owned_names = {LEDGER_FILE_NAME, ledger_file.with_suffix(".tmp").name}
    if any(each.name not in owned_names or each.is_symlink() or not each.is_file() for each in entries):
        raise RuntimeError("Ledger directory contains unowned resources; cleanup refused")
    for entry in entries:
        entry.unlink()
    ledger_dir.rmdir()


def cmd_cleanup(args: argparse.Namespace) -> int:
    ledger_file = ensure_safe_ledger_file(resolve_ledger_file(args.ledger))
    read_ledger(ledger_file)
    remove_ledger_dir(ledger_file)
    print("removed=1")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Manage temporary review coverage ledgers.")
    subparsers = parser.add_subparsers(dest="command", required=True)
    init = subparsers.add_parser("init", help="Initialize a private temporary coverage ledger")
    init.add_argument("--repo-root", default=".", help="Repository root or any path inside it")
    init.add_argument("--pr", required=True, help="PR number or stable review identifier")
    init.add_argument("--base-ref", required=True, help="Base ref used to compute merge-base")
    init.add_argument("--head-ref", required=True, help="PR head ref")
    init.add_argument("--github-files", help="File containing GitHub changed paths")
    init.add_argument("--candidate-files", help="Exact repository-relative local candidate paths, one per line")
    init.add_argument("--review-basis", required=True, help="JSON focus and rule/requirement/evidence sources with role, origin, and material path")
    init.set_defaults(func=cmd_init)
    mark_file = subparsers.add_parser("mark-file", help="Set one authoritative file's coverage state")
    mark_file.add_argument("--ledger", required=True, help="Ledger file or directory")
    mark_file.add_argument("--path", required=True, help="Repository-relative changed file path")
    mark_file.add_argument("--status", required=True, choices=sorted(FILE_STATUSES), help="Coverage state")
    mark_file.add_argument("--cluster", action="append", help="Behavior cluster containing this file")
    mark_file.add_argument("--risk-axis", action="append", help="Risk axis covered for this file")
    mark_file.add_argument("--reason", help="Reason for a non-substantive file classification")
    mark_file.add_argument("--coverage", help="JSON changed-block coverage with behavior, risk axes, requirements, counter-evidence, and source anchors")
    mark_file.set_defaults(func=cmd_mark_file)
    finding = subparsers.add_parser("add-finding", help="Add or update one finding classification")
    finding.add_argument("--ledger", required=True, help="Ledger file or directory")
    finding.add_argument("--id", required=True, help="Stable finding id")
    finding.add_argument("--status", required=True, choices=sorted(FINDING_STATUSES), help="Finding status")
    finding.add_argument("--origin", help="Finding origin classification")
    finding.add_argument("--fix-boundary", help="Minimum independent fix boundary")
    finding.add_argument("--evidence", action="append", help="Public or sanitized evidence anchor")
    finding.add_argument("--full-path", action="append", help="Production or test path traced end to end")
    finding.add_argument("--counter-evidence", action="append", help="Strongest counter-evidence checked")
    finding.add_argument("--necessity", help="Why the change is required for safety or correctness")
    finding.add_argument("--scope-proof", help="Why the PR owns the finding")
    finding.add_argument("--file", action="append", help="Repository-relative scope file")
    finding.add_argument("--reason", help="Reason for an incomplete or non-blocking classification")
    finding.add_argument("--anchors", help="JSON real source anchors supporting this finding or unavailable-evidence proof")
    finding.set_defaults(func=cmd_add_finding)
    review_pass = subparsers.add_parser("add-pass", help="Record a required review pass")
    review_pass.add_argument("--ledger", required=True, help="Ledger file or directory")
    review_pass.add_argument("--focus", required=True, choices=PASS_FOCUSES, help="Pass focus")
    review_pass.add_argument("--new-findings", required=True, type=int, help="New independent findings")
    review_pass.set_defaults(func=cmd_add_pass)
    batch = subparsers.add_parser("batch", help="Apply ordered updates to one ledger with one atomic write")
    batch.add_argument("--ledger", required=True, help="Ledger file or directory")
    batch.add_argument("--operations", required=True, help="JSON nonempty ordered list of existing mutation argument lists without --ledger")
    batch.set_defaults(func=cmd_batch)
    validate = subparsers.add_parser("validate", help="Validate mechanical coverage completion")
    validate.add_argument("--ledger", required=True, help="Ledger file or directory")
    validate.set_defaults(func=cmd_validate)
    validate_incomplete = subparsers.add_parser("validate-incomplete", help="Validate a proven Review Incomplete result")
    validate_incomplete.add_argument("--ledger", required=True, help="Ledger file or directory")
    validate_incomplete.set_defaults(func=cmd_validate_incomplete)
    validate_session_parser = subparsers.add_parser("validate-session", help="Validate cross-round blind reviews and substantive result review")
    validate_session_parser.add_argument("--session", required=True, help="JSON original result references and result-review dispositions")
    validate_session_parser.set_defaults(func=cmd_validate_session)
    status = subparsers.add_parser("status", help="Print compact coverage status")
    status.add_argument("--ledger", required=True, help="Ledger file or directory")
    status.set_defaults(func=cmd_status)
    cleanup = subparsers.add_parser("cleanup", help="Remove this exact temporary ledger")
    cleanup.add_argument("--ledger", required=True, help="Ledger file or directory")
    cleanup.set_defaults(func=cmd_cleanup)
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    try:
        return args.func(args)
    except (OSError, RuntimeError, json.JSONDecodeError) as ex:
        print(f"error: {ex}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
