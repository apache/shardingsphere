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
import json
import os
import re
import shutil
import sys
import tempfile
import time
from collections import Counter
from pathlib import Path
from typing import Any, Iterable

from review_common import categorize, compare_github_files, final_paths, get_repo_root, parse_name_status, run_git


LEDGER_KIND = "review-pr-coverage-ledger"
LEDGER_VERSION = 2
LEDGER_FILE_NAME = "ledger.json"
FILE_STATUSES = frozenset({"pending", "reviewed", "churn-only", "test-only-reviewed", "not-applicable", "blocked"})
FINAL_FILE_STATUSES = FILE_STATUSES - {"pending"}
FINDING_STATUSES = frozenset({"candidate", "confirmed", "withdrawn", "review-incomplete-gap", "non-blocking", "out-of-scope"})


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


def ensure_safe_ledger_file(ledger_file: Path) -> None:
    root = ledger_root().resolve()
    target = ledger_file.resolve()
    if LEDGER_FILE_NAME != target.name or root != target.parent.parent:
        raise RuntimeError(f"Ledger is outside the private review ledger root: {target}")


def validate_schema(ledger: dict[str, Any]) -> None:
    if LEDGER_KIND != ledger.get("kind") or LEDGER_VERSION != ledger.get("version"):
        raise RuntimeError("Unsupported or invalid review coverage ledger")


def read_ledger(value: str | Path) -> dict[str, Any]:
    ledger_file = resolve_ledger_file(value)
    ensure_safe_ledger_file(ledger_file)
    ledger = json.loads(ledger_file.read_text(encoding="utf-8"))
    validate_schema(ledger)
    return ledger


def write_ledger(ledger_file: Path, ledger: dict[str, Any]) -> None:
    ensure_safe_ledger_file(ledger_file)
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
    changed_files = parse_name_status(run_git(["diff", "--name-status", f"{merge_base}..{args.head_ref}"], repo_root))
    ledger_dir = create_ledger_dir(repo_root, args.pr, head_sha)
    ledger_file = ledger_dir / LEDGER_FILE_NAME
    now = int(time.time())
    ledger = {
        "kind": LEDGER_KIND,
        "version": LEDGER_VERSION,
        "created_at": now,
        "updated_at": now,
        "scope": {
            "repo": repo_root.name,
            "pr": args.pr,
            "base_ref": args.base_ref,
            "base_sha": base_sha,
            "head_ref": args.head_ref,
            "head_sha": head_sha,
            "merge_base": merge_base,
            "changed_file_count": len(changed_files),
            "github_files": compare_github_files(final_paths(changed_files), args.github_files),
        },
        "files": [{
            "path": each.path,
            "git_status": each.status,
            "old_path": each.old_path,
            "category": categorize(each.path),
            "status": "pending",
            "risk_axes": [],
            "findings": [],
        } for each in changed_files],
        "findings": [],
        "passes": [],
    }
    write_ledger(ledger_file, ledger)
    print(f"ledger={ledger_file}")
    print(f"files={len(changed_files)}")
    return 0


def cmd_mark_file(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    entry = find_file_entry(ledger, args.path)
    entry["status"] = args.status
    unique_extend(entry["risk_axes"], args.risk_axis or [])
    write_ledger(ledger_file, ledger)
    print(f"marked={args.path} status={args.status}")
    return 0


def cmd_add_finding(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
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
    }
    existing = next((each for each in ledger["findings"] if args.id == each["id"]), None)
    if existing:
        existing.update(finding)
    else:
        ledger["findings"].append(finding)
    sync_file_findings(ledger)
    write_ledger(ledger_file, ledger)
    print(f"finding={args.id} status={args.status}")
    return 0


def cmd_add_pass(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    ledger["passes"].append({
        "focus": args.focus,
        "new_findings": args.new_findings,
        "created_at": int(time.time()),
    })
    write_ledger(ledger_file, ledger)
    print(f"pass={args.focus} new_findings={args.new_findings}")
    return 0


def validate_ledger(ledger: dict[str, Any]) -> list[str]:
    result: list[str] = []
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
    blocked_files = [each["path"] for each in ledger["files"] if "blocked" == each["status"]]
    if blocked_files:
        result.append(f"Blocked files require more evidence or an incomplete result: {len(blocked_files)}")
    finding_counts = Counter(each["id"] for each in ledger["findings"])
    duplicate_findings = [finding_id for finding_id, count in finding_counts.items() if 1 < count]
    if duplicate_findings:
        result.append(f"Duplicate finding ids remain: {len(duplicate_findings)}")
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
            if not each["origin"]:
                result.append(f"{each['id']} confirmed finding is missing origin")
            if not each["fix_boundary"]:
                result.append(f"{each['id']} confirmed finding is missing fix boundary")
            if not each["evidence"]:
                result.append(f"{each['id']} confirmed finding is missing evidence")
            if not each.get("full_path"):
                result.append(f"{each['id']} confirmed finding is missing full-path review")
            if not each.get("counter_evidence"):
                result.append(f"{each['id']} confirmed finding is missing counter-evidence review")
            if not each.get("necessity"):
                result.append(f"{each['id']} confirmed finding is missing necessity")
            if not each.get("scope_proof"):
                result.append(f"{each['id']} confirmed finding is missing scope proof")
            if not each["files"]:
                result.append(f"{each['id']} confirmed finding is missing scope files")
        if "review-incomplete-gap" == each["status"] and not each["reason"]:
            result.append(f"{each['id']} incomplete gap is missing a reason")
    if not ledger["passes"]:
        result.append("No final adversarial pass was recorded")
    elif 0 != ledger["passes"][-1].get("new_findings"):
        result.append("Latest adversarial pass did not finish with zero new findings")
    return result


def cmd_validate(args: argparse.Namespace) -> int:
    ledger = read_ledger(args.ledger)
    errors = validate_ledger(ledger)
    if errors:
        for each in errors:
            print(f"ERROR: {each}")
        return 1
    print("OK")
    return 0


def cmd_status(args: argparse.Namespace) -> int:
    ledger = read_ledger(args.ledger)
    file_counts = Counter(each["status"] for each in ledger["files"])
    finding_counts = Counter(each["status"] for each in ledger["findings"])
    print(f"files={dict(sorted(file_counts.items()))}")
    print(f"findings={dict(sorted(finding_counts.items()))}")
    print(f"passes={len(ledger['passes'])}")
    return 0


def cmd_cleanup(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    read_ledger(ledger_file)
    ledger_dir = ledger_file.parent
    ensure_safe_ledger_file(ledger_file)
    shutil.rmtree(ledger_dir)
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
    init.set_defaults(func=cmd_init)
    mark_file = subparsers.add_parser("mark-file", help="Set one authoritative file's coverage state")
    mark_file.add_argument("--ledger", required=True, help="Ledger file or directory")
    mark_file.add_argument("--path", required=True, help="Repository-relative changed file path")
    mark_file.add_argument("--status", required=True, choices=sorted(FILE_STATUSES), help="Coverage state")
    mark_file.add_argument("--risk-axis", action="append", help="Risk axis covered for this file")
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
    finding.set_defaults(func=cmd_add_finding)
    review_pass = subparsers.add_parser("add-pass", help="Record an adversarial review pass")
    review_pass.add_argument("--ledger", required=True, help="Ledger file or directory")
    review_pass.add_argument("--focus", required=True, help="Pass focus")
    review_pass.add_argument("--new-findings", required=True, type=int, help="New independent findings")
    review_pass.set_defaults(func=cmd_add_pass)
    validate = subparsers.add_parser("validate", help="Validate mechanical coverage completion")
    validate.add_argument("--ledger", required=True, help="Ledger file or directory")
    validate.set_defaults(func=cmd_validate)
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
