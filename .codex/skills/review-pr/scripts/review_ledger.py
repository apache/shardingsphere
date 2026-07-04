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

"""Manage temporary full-coverage review ledgers for the review-pr skill."""

from __future__ import annotations

import argparse
import json
import re
import shutil
import sys
import tempfile
import time
from collections import Counter
from pathlib import Path
from typing import Any, Iterable

from review_common import categorize, compare_github_files, final_paths, get_repo_root, parse_name_status, run_git


FILE_STATUSES = frozenset({"pending", "reviewed", "churn-only", "test-only-reviewed", "not-applicable", "blocked"})
FINAL_FILE_STATUSES = FILE_STATUSES - {"pending"}
FINDING_STATUSES = frozenset({"candidate", "confirmed", "withdrawn", "review-incomplete-gap", "non-blocking", "out-of-scope"})
SEVERITIES = frozenset({"P0", "P1", "P2"})
PROOF_GATE_RESULTS = frozenset({"pending", "passed", "failed", "not-applicable"})
LEDGER_FILE_NAME = "ledger.json"


def ledger_root() -> Path:
    return Path(tempfile.gettempdir()) / "codex-review-pr"


def sanitize(value: str) -> str:
    return re.sub(r"[^A-Za-z0-9_.-]+", "-", value).strip("-") or "unknown"


def build_ledger_dir(repo_root: Path, pr: str, head_sha: str) -> Path:
    return ledger_root() / f"{sanitize(repo_root.name)}-pr-{sanitize(pr)}-{head_sha[:12]}"


def resolve_ledger_file(path: str | Path) -> Path:
    value = Path(path)
    return value / LEDGER_FILE_NAME if value.is_dir() else value


def ensure_safe_ledger_dir(path: Path) -> None:
    root = ledger_root().resolve()
    target = path.resolve()
    if target == root or root not in target.parents:
        raise RuntimeError(f"Refusing to remove path outside review ledger root: {target}")


def remove_ledger_dir(path: Path) -> bool:
    if not path.exists():
        return False
    ensure_safe_ledger_dir(path)
    shutil.rmtree(path)
    return True


def cleanup_matching_ledgers(repo_root: Path, pr: str, keep: Path | None = None) -> int:
    parent = ledger_root()
    if not parent.exists():
        return 0
    pattern = f"{sanitize(repo_root.name)}-pr-{sanitize(pr)}-*"
    result = 0
    for each in parent.glob(pattern):
        if keep and each.resolve() == keep.resolve():
            continue
        if each.is_dir() and remove_ledger_dir(each):
            result += 1
    return result


def read_ledger(ledger: str | Path) -> dict[str, Any]:
    ledger_file = resolve_ledger_file(ledger)
    return json.loads(ledger_file.read_text(encoding="utf-8"))


def write_ledger(ledger_file: Path, ledger: dict[str, Any]) -> None:
    ledger["updated_at"] = int(time.time())
    temp_file = ledger_file.with_suffix(".tmp")
    temp_file.write_text(json.dumps(ledger, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    temp_file.replace(ledger_file)


def find_file_entry(ledger: dict[str, Any], path: str) -> dict[str, Any]:
    for each in ledger["files"]:
        if each["path"] == path:
            return each
    raise RuntimeError(f"File is not in ledger scope: {path}")


def unique_extend(values: list[str], additions: Iterable[str]) -> None:
    for each in additions:
        if each not in values:
            values.append(each)


def cmd_init(args: argparse.Namespace) -> int:
    repo_root = get_repo_root(Path(args.repo_root))
    base_sha = run_git(["rev-parse", args.base_ref], repo_root).strip()
    head_sha = run_git(["rev-parse", args.head_ref], repo_root).strip()
    merge_base = run_git(["merge-base", args.base_ref, args.head_ref], repo_root).strip()
    changed_files = parse_name_status(run_git(["diff", "--name-status", f"{merge_base}..{args.head_ref}"], repo_root))
    paths = final_paths(changed_files)
    current_dir = build_ledger_dir(repo_root, args.pr, head_sha)
    cleanup_matching_ledgers(repo_root, args.pr, keep=current_dir)
    remove_ledger_dir(current_dir)
    current_dir.mkdir(parents=True, exist_ok=False)
    ledger_file = current_dir / LEDGER_FILE_NAME
    now = int(time.time())
    ledger = {
        "version": 1,
        "mode": args.mode,
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
            "github_files": compare_github_files(paths, args.github_files),
        },
        "files": [{
            "path": each.path,
            "git_status": each.status,
            "old_path": each.old_path,
            "category": categorize(each.path),
            "status": "pending",
            "diff_read": False,
            "entry_points_checked": False,
            "tests_checked": False,
            "adjacent_paths_checked": False,
            "risk_axes": [],
            "findings": [],
            "notes": "",
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
    entry["diff_read"] = entry["diff_read"] or args.diff_read
    entry["entry_points_checked"] = entry["entry_points_checked"] or args.entry_points_checked
    entry["tests_checked"] = entry["tests_checked"] or args.tests_checked
    entry["adjacent_paths_checked"] = entry["adjacent_paths_checked"] or args.adjacent_paths_checked
    unique_extend(entry["risk_axes"], args.risk_axis or [])
    unique_extend(entry["findings"], args.finding or [])
    if args.notes:
        entry["notes"] = args.notes
    write_ledger(ledger_file, ledger)
    print(f"marked={args.path} status={args.status}")
    return 0


def cmd_add_finding(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    finding = {
        "id": args.id,
        "status": args.status,
        "severity": args.severity,
        "origin": args.origin,
        "fix_boundary": args.fix_boundary,
        "evidence": args.evidence or [],
        "proof_gate": args.proof_gate,
        "counter_evidence": args.counter_evidence or [],
        "necessity": args.necessity or "",
        "scope_proof": args.scope_proof or "",
        "full_path_checked": args.full_path_checked,
        "files": args.file or [],
        "notes": args.notes or "",
    }
    existing = next((each for each in ledger["findings"] if each["id"] == args.id), None)
    if existing:
        existing.update(finding)
    else:
        ledger["findings"].append(finding)
    for each in finding["files"]:
        unique_extend(find_file_entry(ledger, each)["findings"], [args.id])
    write_ledger(ledger_file, ledger)
    print(f"finding={args.id} status={args.status}")
    return 0


def cmd_add_pass(args: argparse.Namespace) -> int:
    ledger_file = resolve_ledger_file(args.ledger)
    ledger = read_ledger(ledger_file)
    ledger["passes"].append({
        "focus": args.focus,
        "new_findings": args.new_findings,
        "notes": args.notes or "",
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
    pending = [each["path"] for each in ledger["files"] if "pending" == each["status"]]
    if pending:
        result.append(f"Pending files remain: {len(pending)}")
    invalid_files = [each["path"] for each in ledger["files"] if each["status"] not in FINAL_FILE_STATUSES]
    if invalid_files:
        result.append(f"Invalid final file statuses remain: {len(invalid_files)}")
    blocked = [each["path"] for each in ledger["files"] if "blocked" == each["status"]]
    if blocked:
        result.append(f"Blocked files require Review Incomplete or more evidence: {len(blocked)}")
    finding_ids = {each["id"] for each in ledger["findings"]}
    for each in ledger["files"]:
        missing = [finding for finding in each["findings"] if finding not in finding_ids]
        if missing:
            result.append(f"{each['path']} references unknown findings: {', '.join(missing)}")
    for each in ledger["findings"]:
        if each["status"] not in FINDING_STATUSES:
            result.append(f"{each['id']} has invalid status: {each['status']}")
        if "candidate" == each["status"]:
            result.append(f"{each['id']} is still candidate")
        if "confirmed" == each["status"]:
            if each["severity"] not in SEVERITIES:
                result.append(f"{each['id']} confirmed finding is missing P0/P1/P2 severity")
            if not each["evidence"]:
                result.append(f"{each['id']} confirmed finding is missing evidence")
            if not each["fix_boundary"]:
                result.append(f"{each['id']} confirmed finding is missing fix boundary")
            if "passed" != each.get("proof_gate"):
                result.append(f"{each['id']} confirmed finding did not pass Blocker Proof Gate")
            if not each.get("counter_evidence"):
                result.append(f"{each['id']} confirmed finding is missing counter-evidence review")
            if not each.get("necessity"):
                result.append(f"{each['id']} confirmed finding is missing merge-safety necessity")
            if not each.get("scope_proof"):
                result.append(f"{each['id']} confirmed finding is missing PR scope proof")
    if not any(0 == each.get("new_findings") for each in ledger["passes"]):
        result.append("No final adversarial pass with new_findings=0")
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
    ledger_dir = ledger_file.parent
    removed = remove_ledger_dir(ledger_dir)
    print(f"removed={1 if removed else 0}")
    return 0


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Manage temporary full-coverage review ledgers.")
    subparsers = parser.add_subparsers(dest="command", required=True)
    init = subparsers.add_parser("init", help="Initialize a temporary review ledger")
    init.add_argument("--repo-root", default=".", help="Repository root or any path inside it")
    init.add_argument("--pr", required=True, help="PR number or stable review identifier")
    init.add_argument("--base-ref", required=True, help="Base ref used to compute merge-base")
    init.add_argument("--head-ref", required=True, help="PR head ref")
    init.add_argument("--github-files", help="Optional file containing GitHub /pulls/{number}/files filenames")
    init.add_argument("--mode", choices=("full", "fast"), default="full", help="Ledger review mode")
    init.set_defaults(func=cmd_init)
    mark_file = subparsers.add_parser("mark-file", help="Mark a changed file review state")
    mark_file.add_argument("--ledger", required=True, help="Ledger file or directory")
    mark_file.add_argument("--path", required=True, help="Repo-relative changed file path")
    mark_file.add_argument("--status", required=True, choices=sorted(FILE_STATUSES), help="File review status")
    mark_file.add_argument("--diff-read", action="store_true", help="Mark the file diff as read")
    mark_file.add_argument("--entry-points-checked", action="store_true", help="Mark entry points as checked")
    mark_file.add_argument("--tests-checked", action="store_true", help="Mark related tests as checked")
    mark_file.add_argument("--adjacent-paths-checked", action="store_true", help="Mark adjacent paths as checked")
    mark_file.add_argument("--risk-axis", action="append", help="Risk axis covered for this file")
    mark_file.add_argument("--finding", action="append", help="Finding id associated with this file")
    mark_file.add_argument("--notes", help="Short internal note")
    mark_file.set_defaults(func=cmd_mark_file)
    finding = subparsers.add_parser("add-finding", help="Add or update a candidate finding")
    finding.add_argument("--ledger", required=True, help="Ledger file or directory")
    finding.add_argument("--id", required=True, help="Stable finding id")
    finding.add_argument("--status", required=True, choices=sorted(FINDING_STATUSES), help="Finding status")
    finding.add_argument("--severity", choices=sorted(SEVERITIES), help="Severity for confirmed findings")
    finding.add_argument("--origin", default="", help="Finding origin classification")
    finding.add_argument("--fix-boundary", default="", help="Minimum independent fix boundary")
    finding.add_argument("--evidence", action="append", help="Public evidence anchor")
    finding.add_argument("--proof-gate", choices=sorted(PROOF_GATE_RESULTS), default="pending", help="Blocker Proof Gate result")
    finding.add_argument("--counter-evidence", action="append", help="Public counter-evidence checked, or reason none exists")
    finding.add_argument("--necessity", default="", help="Why the finding is required for merge safety")
    finding.add_argument("--scope-proof", default="", help="Why this PR owns the finding")
    finding.add_argument("--full-path-checked", action="store_true", help="Full production or test path checked where applicable")
    finding.add_argument("--file", action="append", help="Repo-relative file path associated with this finding")
    finding.add_argument("--notes", help="Short internal note")
    finding.set_defaults(func=cmd_add_finding)
    review_pass = subparsers.add_parser("add-pass", help="Record an adversarial review pass")
    review_pass.add_argument("--ledger", required=True, help="Ledger file or directory")
    review_pass.add_argument("--focus", required=True, help="Pass focus")
    review_pass.add_argument("--new-findings", required=True, type=int, help="New independent findings discovered by this pass")
    review_pass.add_argument("--notes", help="Short internal note")
    review_pass.set_defaults(func=cmd_add_pass)
    validate = subparsers.add_parser("validate", help="Validate ledger completion gates")
    validate.add_argument("--ledger", required=True, help="Ledger file or directory")
    validate.set_defaults(func=cmd_validate)
    status = subparsers.add_parser("status", help="Print compact ledger status")
    status.add_argument("--ledger", required=True, help="Ledger file or directory")
    status.set_defaults(func=cmd_status)
    cleanup = subparsers.add_parser("cleanup", help="Remove a temporary ledger directory")
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
