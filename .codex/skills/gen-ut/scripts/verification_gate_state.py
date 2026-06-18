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

#!/usr/bin/env python3
"""
Verification snapshot digest and reusable gate state for gen-ut.
"""

import argparse
import hashlib
import json
import sys
import time
from pathlib import Path


TRACKED_SUFFIXES = {".java", ".xml", ".yaml", ".yml", ".properties"}
GATES_KEY = "gates"
TARGET_TEST_GATE = "target-test"
LEGACY_TARGET_DIGEST_KEY = "latest_green_target_test_digest"
LEGACY_TARGET_UPDATED_AT_KEY = "latest_green_target_test_updated_at"
LEGACY_TRACKED_FILE_COUNT_KEY = "tracked_file_count"


def normalize_paths(paths: list[str]) -> list[Path]:
    result = []
    seen = set()
    for each in paths:
        path = Path(each)
        if path.suffix.lower() not in TRACKED_SUFFIXES:
            continue
        resolved = path.resolve()
        if not resolved.exists():
            raise ValueError(f"tracked path does not exist: {resolved}")
        if not resolved.is_file():
            raise ValueError(f"tracked path is not a file: {resolved}")
        if resolved in seen:
            continue
        seen.add(resolved)
        result.append(resolved)
    return sorted(result, key=str)


def tracked_paths(paths: list[str]) -> list[Path]:
    result = normalize_paths(paths)
    if result:
        return result
    raise ValueError("no trackable files found in verification snapshot")


def calculate_digest(paths: list[Path]) -> str:
    digest = hashlib.sha256()
    for path in paths:
        digest.update(str(path).encode("utf-8"))
        digest.update(b"\0")
        digest.update(path.read_bytes())
        digest.update(b"\0")
    return digest.hexdigest()


def extend_digest(base_digest: str, tokens: list[str]) -> str:
    digest = hashlib.sha256()
    digest.update(base_digest.encode("utf-8"))
    digest.update(b"\0")
    for each in tokens:
        digest.update(each.encode("utf-8"))
        digest.update(b"\0")
    return digest.hexdigest()


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


def ensure_gate_map(state: dict) -> dict[str, dict]:
    gates = state.get(GATES_KEY)
    if gates is None:
        gates = {}
        state[GATES_KEY] = gates
    if not isinstance(gates, dict):
        raise ValueError("invalid gate state payload: expected object for `gates`")
    return gates


def legacy_target_entry(state: dict) -> dict | None:
    digest = state.get(LEGACY_TARGET_DIGEST_KEY)
    if not digest:
        return None
    return {
        "digest": digest,
        "updated_at": state.get(LEGACY_TARGET_UPDATED_AT_KEY),
        "tracked_file_count": state.get(LEGACY_TRACKED_FILE_COUNT_KEY),
    }


def get_gate_entry(state: dict, gate_name: str) -> dict | None:
    gates = ensure_gate_map(state)
    result = gates.get(gate_name)
    if result is not None and not isinstance(result, dict):
        raise ValueError(f"invalid gate entry for {gate_name}: expected JSON object")
    if result is None and TARGET_TEST_GATE == gate_name:
        return legacy_target_entry(state)
    return result


def get_gate_digest(state: dict, gate_name: str) -> str | None:
    entry = get_gate_entry(state, gate_name)
    return entry.get("digest") if entry else None


def set_gate_digest(state: dict, gate_name: str, digest: str, tracked_file_count: int) -> None:
    gates = ensure_gate_map(state)
    updated_at = int(time.time())
    gates[gate_name] = {
        "digest": digest,
        "tracked_file_count": tracked_file_count,
        "updated_at": updated_at,
    }
    if TARGET_TEST_GATE == gate_name:
        state[LEGACY_TARGET_DIGEST_KEY] = digest
        state[LEGACY_TARGET_UPDATED_AT_KEY] = updated_at
        state[LEGACY_TRACKED_FILE_COUNT_KEY] = tracked_file_count


def matches_gate_digest(state: dict, gate_name: str, digest: str) -> bool:
    return get_gate_digest(state, gate_name) == digest


def current_digest(paths: list[str]) -> tuple[str, int]:
    normalized_paths = tracked_paths(paths)
    return calculate_digest(normalized_paths), len(normalized_paths)


def command_digest(paths: list[str]) -> int:
    digest, _ = current_digest(paths)
    print(digest)
    return 0


def command_mark_gate_green(state_file: Path, gate_name: str, paths: list[str]) -> int:
    digest, tracked_file_count = current_digest(paths)
    state = read_state(state_file)
    set_gate_digest(state, gate_name, digest, tracked_file_count)
    write_state(state_file, state)
    print(digest)
    return 0


def command_match_gate_green(state_file: Path, gate_name: str, paths: list[str]) -> int:
    digest, _ = current_digest(paths)
    state = read_state(state_file)
    latest_green = get_gate_digest(state, gate_name)
    if latest_green == digest:
        print(f"MATCH {digest}")
        return 0
    if latest_green:
        print(f"MISMATCH current={digest} recorded={latest_green}")
    else:
        print(f"MISSING current={digest}")
    return 1


def execute_command(args: argparse.Namespace) -> int:
    if "digest" == args.command:
        return command_digest(args.paths)
    if "mark-green" == args.command:
        return command_mark_gate_green(Path(args.state_file), TARGET_TEST_GATE, args.paths)
    if "match-green" == args.command:
        return command_match_gate_green(Path(args.state_file), TARGET_TEST_GATE, args.paths)
    if "mark-gate-green" == args.command:
        return command_mark_gate_green(Path(args.state_file), args.gate, args.paths)
    return command_match_gate_green(Path(args.state_file), args.gate, args.paths)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Verification snapshot digest and gate reuse state.")
    subparsers = parser.add_subparsers(dest="command", required=True)
    digest_parser = subparsers.add_parser("digest", help="Print the current verification snapshot digest.")
    digest_parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    mark_green_parser = subparsers.add_parser("mark-green", help="Record the current digest as the latest green target-test digest.")
    mark_green_parser.add_argument("--state-file", required=True, help="State file path.")
    mark_green_parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    match_green_parser = subparsers.add_parser("match-green", help="Check whether the current digest matches the latest green target-test digest.")
    match_green_parser.add_argument("--state-file", required=True, help="State file path.")
    match_green_parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    mark_gate_green_parser = subparsers.add_parser("mark-gate-green", help="Record the current digest as the latest green digest for a named gate.")
    mark_gate_green_parser.add_argument("--state-file", required=True, help="State file path.")
    mark_gate_green_parser.add_argument("--gate", required=True, help="Logical gate name, for example target-test or coverage.")
    mark_gate_green_parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    match_gate_green_parser = subparsers.add_parser("match-gate-green", help="Check whether the current digest matches the latest green digest for a named gate.")
    match_gate_green_parser.add_argument("--state-file", required=True, help="State file path.")
    match_gate_green_parser.add_argument("--gate", required=True, help="Logical gate name, for example target-test or coverage.")
    match_gate_green_parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    try:
        return execute_command(args)
    except (ValueError, OSError, json.JSONDecodeError) as ex:
        print(ex, file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
