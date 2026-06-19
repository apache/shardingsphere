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

"""Build a bounded local review-inventory draft for the review-pr skill."""

from __future__ import annotations

import argparse
import json
import re
import subprocess
import sys
from collections import defaultdict
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


MAX_ITEMS = 30
MAX_TEST_REFERENCES = 3


@dataclass(frozen=True)
class ChangedFile:
    status: str
    path: str
    old_path: str | None = None


def run_git(args: list[str], repo_root: Path, allow_empty: bool = False) -> str:
    process = subprocess.run(["git", *args], cwd=repo_root, text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
    if 0 == process.returncode or allow_empty and 1 == process.returncode:
        return process.stdout
    command = "git " + " ".join(args)
    raise RuntimeError(f"{command} failed with exit {process.returncode}: {process.stderr.strip()}")


def get_repo_root() -> Path:
    process = subprocess.run(["git", "rev-parse", "--show-toplevel"], text=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=False)
    if 0 != process.returncode:
        raise RuntimeError("Current directory is not inside a git repository")
    return Path(process.stdout.strip())


def parse_name_status(output: str) -> list[ChangedFile]:
    result: list[ChangedFile] = []
    for line in output.splitlines():
        if not line.strip():
            continue
        parts = line.split("\t")
        status = parts[0]
        if status.startswith("R") or status.startswith("C"):
            result.append(ChangedFile(status=status, path=parts[-1], old_path=parts[1]))
        else:
            result.append(ChangedFile(status=status, path=parts[-1]))
    return result


def final_paths(changed_files: Iterable[ChangedFile]) -> list[str]:
    return [each.path for each in changed_files]


def categorize(path: str) -> str:
    if path == "RELEASE-NOTES.md":
        return "release-notes"
    if "/src/main/java/" in path:
        return "production-java"
    if "/src/test/" in path:
        return "tests"
    if path.startswith("docs/") or path.endswith((".md", ".adoc")):
        return "docs"
    if path.startswith(".github/") or path.endswith((".xml", ".properties", ".yml", ".yaml", ".toml", ".gradle")):
        return "build-config"
    if "distribution" in path:
        return "distribution"
    if "target/" in path or "/generated/" in path:
        return "generated"
    return "other"


def read_file_at_ref(repo_root: Path, ref: str, path: str) -> str:
    return run_git(["show", f"{ref}:{path}"], repo_root, allow_empty=True)


def find_public_types(content: str) -> list[str]:
    pattern = re.compile(r"^\s*public\s+(?:final\s+|abstract\s+)?(?:class|interface|enum|record)\s+([A-Za-z_][A-Za-z0-9_]*)", re.MULTILINE)
    return pattern.findall(content)


def git_grep(repo_root: Path, ref: str, pattern: str, pathspecs: list[str]) -> list[str]:
    output = run_git(["grep", "-n", pattern, ref, "--", *pathspecs], repo_root, allow_empty=True)
    return output.splitlines()


def added_public_types(repo_root: Path, head_ref: str, changed_files: list[ChangedFile]) -> list[dict[str, object]]:
    result: list[dict[str, object]] = []
    for changed_file in changed_files:
        if "A" != changed_file.status or "/src/main/java/" not in changed_file.path or not changed_file.path.endswith(".java"):
            continue
        content = read_file_at_ref(repo_root, head_ref, changed_file.path)
        for type_name in find_public_types(content):
            test_refs = git_grep(repo_root, head_ref, type_name, ["*src/test*"])
            result.append({
                "type": type_name,
                "path": changed_file.path,
                "test_reference_count": len(test_refs),
                "sample_test_references": test_refs[:MAX_TEST_REFERENCES],
            })
    return result


def limited(items: Iterable[str], limit: int = MAX_ITEMS) -> tuple[list[str], int]:
    values = list(items)
    return values[:limit], max(len(values) - limit, 0)


def scan_diff_clues(repo_root: Path, base_ref: str, head_ref: str, paths: list[str]) -> dict[str, list[str]]:
    if not paths:
        return {}
    output = run_git(["diff", "--unified=0", f"{base_ref}..{head_ref}", "--", *paths], repo_root, allow_empty=True)
    clue_patterns = {
        "public-api": re.compile(r"^\+.*\bpublic\b.*(?:class|interface|enum|record|void|[A-Za-z0-9_<>\[\]]+\s+[A-Za-z0-9_]+\s*\()"),
        "state-sentinel": re.compile(r"^\+.*\b(null|Collections\.empty|emptyList|emptyMap|UNKNOWN|NONE|DEFAULT)\b"),
        "lifecycle": re.compile(r"^\+.*\b(register|unregister|release|free|close|cleanup|cache|registry|session|handle)\b", re.IGNORECASE),
        "protocol-command": re.compile(r"^\+.*\b(CommandPacketType|CommandPacketFactory|Executor|BATCH_|Packet|opcode|protocol)\b"),
        "todo": re.compile(r"^\+.*\b(TODO|FIXME)\b"),
        "compute-if-absent": re.compile(r"^\+.*computeIfAbsent"),
    }
    result: dict[str, list[str]] = {key: [] for key in clue_patterns}
    current_file = ""
    for line in output.splitlines():
        if line.startswith("+++ b/"):
            current_file = line.removeprefix("+++ b/")
            continue
        if not line.startswith("+") or line.startswith("+++"):
            continue
        for key, pattern in clue_patterns.items():
            if pattern.search(line):
                result[key].append(f"{current_file}: {line[:220]}")
    return {key: limited(values)[0] for key, values in result.items() if values}


def compare_github_files(local_paths: list[str], github_files_path: str | None) -> dict[str, object]:
    if not github_files_path:
        return {"provided": False}
    github_paths = sorted(line.strip() for line in Path(github_files_path).read_text(encoding="utf-8").splitlines() if line.strip())
    local_sorted = sorted(local_paths)
    return {
        "provided": True,
        "matched": github_paths == local_sorted,
        "github_count": len(github_paths),
        "local_count": len(local_sorted),
        "only_in_github": sorted(set(github_paths) - set(local_sorted))[:MAX_ITEMS],
        "only_in_local": sorted(set(local_sorted) - set(github_paths))[:MAX_ITEMS],
    }


def group_by_category(changed_files: list[ChangedFile]) -> dict[str, list[str]]:
    result: dict[str, list[str]] = defaultdict(list)
    for changed_file in changed_files:
        result[categorize(changed_file.path)].append(f"{changed_file.status}\t{changed_file.path}")
    return dict(sorted(result.items()))


def build_inventory(args: argparse.Namespace) -> dict[str, object]:
    repo_root = get_repo_root()
    base_sha = run_git(["rev-parse", args.base_ref], repo_root).strip()
    head_sha = run_git(["rev-parse", args.head_ref], repo_root).strip()
    merge_base = run_git(["merge-base", args.base_ref, args.head_ref], repo_root).strip()
    changed_files = parse_name_status(run_git(["diff", "--name-status", f"{merge_base}..{args.head_ref}"], repo_root))
    paths = final_paths(changed_files)
    production_paths = [path for path in paths if "/src/main/java/" in path and path.endswith(".java")]
    previous_delta: list[ChangedFile] = []
    if args.previous_head:
        previous_delta = parse_name_status(run_git(["diff", "--name-status", f"{args.previous_head}..{args.head_ref}"], repo_root, allow_empty=True))
    return {
        "scope": {
            "base_ref": args.base_ref,
            "base_sha": base_sha,
            "head_ref": args.head_ref,
            "head_sha": head_sha,
            "merge_base": merge_base,
            "changed_file_count": len(changed_files),
            "github_files": compare_github_files(paths, args.github_files),
        },
        "dirty_worktree": run_git(["status", "--short"], repo_root, allow_empty=True).splitlines(),
        "changed_files_by_category": group_by_category(changed_files),
        "added_public_types": added_public_types(repo_root, args.head_ref, changed_files),
        "diff_clues": scan_diff_clues(repo_root, merge_base, args.head_ref, production_paths),
        "latest_delta": {
            "previous_head": args.previous_head,
            "changed_file_count": len(previous_delta),
            "files": [f"{each.status}\t{each.path}" for each in previous_delta[:MAX_ITEMS]],
            "truncated_count": max(len(previous_delta) - MAX_ITEMS, 0),
        } if args.previous_head else None,
        "manual_checklist": [
            "Confirm GitHub file list matches local triple-dot scope before reporting scope findings.",
            "Classify each blocker origin: PR-caused, base-existing, exposed-by-PR, latest-introduced, older-PR-revision, or out-of-scope.",
            "Deduplicate candidate issues by independent fix boundary before output.",
            "Review lifecycle paths for every new registry/cache/session/handle state.",
            "Review supported-vs-rejected feature matrix and flag unsupported-but-accepted inputs.",
            "Map every new public production type to direct focused tests or record missing evidence.",
            "Run one final adversarial pass after findings are frozen; output only after no new independent fix boundary appears.",
        ],
    }


def markdown_list(items: Iterable[str], indent: str = "") -> list[str]:
    values = list(items)
    if not values:
        return [f"{indent}- None"]
    return [f"{indent}- `{value}`" for value in values]


def render_markdown(inventory: dict[str, object]) -> str:
    scope = inventory["scope"]
    github_files = scope["github_files"]
    lines = [
        "# Review Inventory Draft",
        "",
        "> Heuristic local inventory only. Confirm every candidate through public code, tests, docs, or specs before reporting it.",
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
            lines.extend(markdown_list([f"only in GitHub: {path}" for path in github_files["only_in_github"]], "  "))
            lines.extend(markdown_list([f"only in local: {path}" for path in github_files["only_in_local"]], "  "))
    else:
        lines.append("- GitHub file list matched local scope: `not checked`")
    lines.extend(["", "## Dirty Worktree", ""])
    lines.extend(markdown_list(inventory["dirty_worktree"]))
    lines.extend(["", "## Changed Files By Category", ""])
    for category, files in inventory["changed_files_by_category"].items():
        sample, truncated = limited(files, 20)
        lines.append(f"### {category}")
        lines.extend(markdown_list(sample))
        if truncated:
            lines.append(f"- ... {truncated} more")
        lines.append("")
    lines.extend(["## Added Public Production Types", ""])
    added_types = inventory["added_public_types"]
    if not added_types:
        lines.append("- None")
    for item in added_types:
        lines.append(f"- `{item['type']}` in `{item['path']}`; test references: `{item['test_reference_count']}`")
        for reference in item["sample_test_references"]:
            lines.append(f"  - `{reference}`")
    lines.extend(["", "## Diff Clues", ""])
    diff_clues = inventory["diff_clues"]
    if not diff_clues:
        lines.append("- None")
    for clue_type, clues in diff_clues.items():
        lines.append(f"### {clue_type}")
        lines.extend(markdown_list(clues))
        lines.append("")
    latest_delta = inventory["latest_delta"]
    if latest_delta:
        lines.extend(["## Latest Delta", ""])
        lines.append(f"- Previous head: `{latest_delta['previous_head']}`")
        lines.append(f"- Changed files since previous head: `{latest_delta['changed_file_count']}`")
        lines.extend(markdown_list(latest_delta["files"]))
        if latest_delta["truncated_count"]:
            lines.append(f"- ... {latest_delta['truncated_count']} more")
        lines.append("")
    lines.extend(["## Manual Checklist", ""])
    lines.extend(markdown_list(inventory["manual_checklist"]))
    return "\n".join(lines) + "\n"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build a bounded local review-inventory draft for review-pr.")
    parser.add_argument("--base-ref", required=True, help="Base ref or SHA used to compute merge-base")
    parser.add_argument("--head-ref", required=True, help="PR head ref or SHA")
    parser.add_argument("--previous-head", help="Previous reviewed PR head ref or SHA for latest-delta classification")
    parser.add_argument("--github-files", help="Optional file containing GitHub /pulls/{number}/files filenames, one per line")
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
