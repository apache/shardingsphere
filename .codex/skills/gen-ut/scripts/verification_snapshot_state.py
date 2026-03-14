#!/usr/bin/env python3
"""
Verification snapshot digest and reusable target-test state for gen-ut.
"""

import argparse
import hashlib
import json
import sys
import time
from pathlib import Path


TRACKED_SUFFIXES = {".java", ".xml", ".yaml", ".yml", ".properties"}


def normalize_paths(paths: list[str]) -> list[Path]:
    result = []
    seen = set()
    for each in paths:
        path = Path(each)
        if path.suffix.lower() not in TRACKED_SUFFIXES:
            continue
        resolved = path.resolve()
        if resolved in seen:
            continue
        seen.add(resolved)
        result.append(resolved)
    return sorted(result, key=str)


def calculate_digest(paths: list[Path]) -> str:
    digest = hashlib.sha256()
    for path in paths:
        digest.update(str(path).encode("utf-8"))
        digest.update(b"\0")
        digest.update(path.read_bytes())
        digest.update(b"\0")
    return digest.hexdigest()


def tracked_paths(paths: list[str]) -> list[Path]:
    result = normalize_paths(paths)
    if result:
        return result
    raise ValueError("no trackable files found in verification snapshot")


def read_state(state_file: Path) -> dict:
    if not state_file.exists():
        return {}
    result = json.loads(state_file.read_text(encoding="utf-8"))
    if not isinstance(result, dict):
        raise ValueError(f"invalid state payload in {state_file}: expected JSON object")
    return result


def write_state(state_file: Path, state: dict) -> None:
    state_file.parent.mkdir(parents=True, exist_ok=True)
    state_file.write_text(json.dumps(state, indent=2, sort_keys=True) + "\n", encoding="utf-8")


def command_digest(paths: list[str]) -> int:
    print(calculate_digest(tracked_paths(paths)))
    return 0


def command_mark_green(state_file: Path, paths: list[str]) -> int:
    filtered_paths = tracked_paths(paths)
    digest = calculate_digest(filtered_paths)
    state = read_state(state_file)
    state["latest_green_target_test_digest"] = digest
    state["latest_green_target_test_updated_at"] = int(time.time())
    state["tracked_file_count"] = len(filtered_paths)
    write_state(state_file, state)
    print(digest)
    return 0


def command_match_green(state_file: Path, paths: list[str]) -> int:
    digest = calculate_digest(tracked_paths(paths))
    state = read_state(state_file)
    latest_green = state.get("latest_green_target_test_digest")
    if latest_green == digest:
        print(f"MATCH {digest}")
        return 0
    if latest_green:
        print(f"MISMATCH current={digest} recorded={latest_green}")
    else:
        print(f"MISSING current={digest}")
    return 1


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Verification snapshot digest and target-test reuse state.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    digest_parser = subparsers.add_parser("digest", help="Print the current verification snapshot digest.")
    digest_parser.add_argument("paths", nargs="+", help="Resolved test file set.")

    mark_green_parser = subparsers.add_parser("mark-green", help="Record the current digest as the latest green target-test digest.")
    mark_green_parser.add_argument("--state-file", required=True, help="State file path.")
    mark_green_parser.add_argument("paths", nargs="+", help="Resolved test file set.")

    match_green_parser = subparsers.add_parser("match-green", help="Check whether the current digest matches the latest green target-test digest.")
    match_green_parser.add_argument("--state-file", required=True, help="State file path.")
    match_green_parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    try:
        if "digest" == args.command:
            return command_digest(args.paths)
        if "mark-green" == args.command:
            return command_mark_green(Path(args.state_file), args.paths)
        return command_match_green(Path(args.state_file), args.paths)
    except (ValueError, OSError, json.JSONDecodeError) as ex:
        print(ex, file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
