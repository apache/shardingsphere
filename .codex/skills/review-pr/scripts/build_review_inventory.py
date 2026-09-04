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

"""Build a deterministic local scope inventory for the review-pr skill."""

from __future__ import annotations

import argparse
import json
import sys
from collections import defaultdict
from typing import Iterable

from review_common import (
    ChangedFile, categorize, compare_github_files, final_paths, get_repo_root, parse_name_status, resolve_candidate_changes, run_git,
)


MAX_ITEMS = 30


def limited(items: Iterable[object], limit: int = MAX_ITEMS) -> tuple[list[object], int]:
    values = list(items)
    return values[:limit], max(len(values) - limit, 0)


def serialize_changed_file(changed_file: ChangedFile) -> dict[str, str | None]:
    return {
        "status": changed_file.status,
        "path": changed_file.path,
        "old_path": changed_file.old_path,
    }


def group_by_category(changed_files: list[ChangedFile]) -> dict[str, list[dict[str, str | None]]]:
    result: dict[str, list[dict[str, str | None]]] = defaultdict(list)
    for changed_file in changed_files:
        result[categorize(changed_file.path)].append(serialize_changed_file(changed_file))
    return dict(sorted(result.items()))


def build_inventory(args: argparse.Namespace) -> dict[str, object]:
    repo_root = get_repo_root()
    base_sha = run_git(["rev-parse", args.base_ref], repo_root).strip()
    head_sha = run_git(["rev-parse", args.head_ref], repo_root).strip()
    merge_base = run_git(["merge-base", args.base_ref, args.head_ref], repo_root).strip()
    candidate_files = getattr(args, "candidate_files", None)
    changed_files = resolve_candidate_changes(repo_root, merge_base, candidate_files) if candidate_files else parse_name_status(
        run_git(["diff", "--name-status", f"{merge_base}..{args.head_ref}"], repo_root))
    dirty_worktree = run_git(["status", "--short"], repo_root, allow_empty=True).splitlines()
    dirty_sample, dirty_truncated = limited(dirty_worktree)
    previous_delta: list[ChangedFile] = []
    if args.previous_head:
        previous_delta = parse_name_status(run_git(["diff", "--name-status", f"{args.previous_head}..{args.head_ref}"], repo_root, allow_empty=True))
    previous_sample, previous_truncated = limited(serialize_changed_file(each) for each in previous_delta)
    return {
        "version": 2,
        "scope": {
            "base_ref": args.base_ref,
            "base_sha": base_sha,
            "head_ref": args.head_ref,
            "head_sha": head_sha,
            "merge_base": merge_base,
            "changed_file_count": len(changed_files),
            "candidate_files": {"provided": bool(candidate_files)},
            "github_files": compare_github_files(final_paths(changed_files), args.github_files, MAX_ITEMS),
        },
        "dirty_worktree": {
            "count": len(dirty_worktree),
            "sample": dirty_sample,
            "truncated_count": dirty_truncated,
        },
        "changed_files_by_category": group_by_category(changed_files),
        "latest_delta": {
            "previous_head": args.previous_head,
            "changed_file_count": len(previous_delta),
            "files": previous_sample,
            "truncated_count": previous_truncated,
        } if args.previous_head else None,
    }


def markdown_list(items: Iterable[str], indent: str = "") -> list[str]:
    values = list(items)
    if not values:
        return [f"{indent}- None"]
    return [f"{indent}- `{value}`" for value in values]


def format_changed_file(changed_file: dict[str, str | None]) -> str:
    old_path = changed_file["old_path"]
    suffix = f" from {old_path}" if old_path else ""
    return f"{changed_file['status']}\t{changed_file['path']}{suffix}"


def render_markdown(inventory: dict[str, object]) -> str:
    scope = inventory["scope"]
    github_files = scope["github_files"]
    lines = [
        "# Review Scope Inventory",
        "",
        "> Deterministic scope data only; this inventory does not prove semantic review completeness.",
        "",
        "## Scope",
        "",
        f"- Base: `{scope['base_ref']}` -> `{scope['base_sha']}`",
        f"- Head: `{scope['head_ref']}` -> `{scope['head_sha']}`",
        f"- Merge base: `{scope['merge_base']}`",
        f"- Changed files: `{scope['changed_file_count']}`",
    ]
    if github_files.get("provided"):
        lines.append(f"- GitHub file list matched local scope: `{github_files['matched']}`")
        if not github_files["matched"]:
            lines.extend(markdown_list([f"only in GitHub: {each}" for each in github_files["only_in_github"]], "  "))
            lines.extend(markdown_list([f"only in local: {each}" for each in github_files["only_in_local"]], "  "))
    else:
        lines.append("- GitHub file list matched local scope: `not checked`")
    dirty_worktree = inventory["dirty_worktree"]
    lines.extend(["", "## Dirty Worktree", "", f"- Changed entries: `{dirty_worktree['count']}`"])
    lines.extend(markdown_list(dirty_worktree["sample"]))
    if dirty_worktree["truncated_count"]:
        lines.append(f"- ... {dirty_worktree['truncated_count']} more")
    lines.extend(["", "## Changed Files By Category", ""])
    for category, changed_files in inventory["changed_files_by_category"].items():
        sample, truncated = limited(changed_files, 20)
        lines.append(f"### {category}")
        lines.extend(markdown_list(format_changed_file(each) for each in sample))
        if truncated:
            lines.append(f"- ... {truncated} more")
        lines.append("")
    latest_delta = inventory["latest_delta"]
    if latest_delta:
        lines.extend(["## Latest Delta", "", f"- Previous head: `{latest_delta['previous_head']}`",
                      f"- Changed files: `{latest_delta['changed_file_count']}`"])
        lines.extend(markdown_list(format_changed_file(each) for each in latest_delta["files"]))
        if latest_delta["truncated_count"]:
            lines.append(f"- ... {latest_delta['truncated_count']} more")
        lines.append("")
    return "\n".join(lines) + "\n"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build deterministic scope inventory for review-pr.")
    parser.add_argument("--base-ref", required=True, help="Base ref or SHA used to compute merge-base")
    parser.add_argument("--head-ref", required=True, help="PR head ref or SHA")
    parser.add_argument("--previous-head", help="Previous reviewed PR head ref or SHA")
    parser.add_argument("--github-files", help="File containing GitHub changed paths, one per line")
    parser.add_argument("--candidate-files", help="Exact repository-relative local candidate paths, one per line")
    parser.add_argument("--format", choices=("markdown", "json"), default="markdown", help="Output format")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        inventory = build_inventory(args)
    except (OSError, RuntimeError) as ex:
        print(f"error: {ex}", file=sys.stderr)
        return 1
    if "json" == args.format:
        print(json.dumps(inventory, indent=2, sort_keys=True))
    else:
        print(render_markdown(inventory), end="")
    return 0


if __name__ == "__main__":
    sys.exit(main())
