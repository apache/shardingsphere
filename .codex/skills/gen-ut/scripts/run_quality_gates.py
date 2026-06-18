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
Run independent quality gates in parallel with optional gate-level reuse.
"""

import argparse
import json
import sys
import subprocess
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import verification_gate_state as snapshot_state


@dataclass(frozen=True)
class GateSpec:
    name: str
    command: str


@dataclass(frozen=True)
class GateResult:
    spec: GateSpec
    status: str
    returncode: int
    stdout: str
    stderr: str
    duration_seconds: float
    state_gate: str | None = None
    digest: str | None = None


def parse_gate(raw: str) -> GateSpec:
    if "=" not in raw:
        raise ValueError(f"invalid gate definition: {raw!r}")
    name, command = raw.split("=", 1)
    name = name.strip()
    command = command.strip()
    if not name or not command:
        raise ValueError(f"invalid gate definition: {raw!r}")
    return GateSpec(name=name, command=command)


def parse_gates(raw_gates: list[str]) -> list[GateSpec]:
    result = []
    seen_names = set()
    for each in raw_gates:
        spec = parse_gate(each)
        if spec.name in seen_names:
            raise ValueError(f"duplicate gate name: {spec.name}")
        seen_names.add(spec.name)
        result.append(spec)
    return result


def parse_gate_mapping(raw: str) -> tuple[str, str]:
    if "=" not in raw:
        normalized = raw.strip()
        if not normalized:
            raise ValueError(f"invalid gate mapping: {raw!r}")
        return normalized, normalized
    gate_name, gate_key = raw.split("=", 1)
    gate_name = gate_name.strip()
    gate_key = gate_key.strip()
    if not gate_name or not gate_key:
        raise ValueError(f"invalid gate mapping: {raw!r}")
    return gate_name, gate_key


def parse_gate_mappings(raw_values: list[str]) -> dict[str, str]:
    result = {}
    for each in raw_values:
        gate_name, gate_key = parse_gate_mapping(each)
        if gate_name in result and result[gate_name] != gate_key:
            raise ValueError(f"duplicate gate mapping for {gate_name}: {result[gate_name]} vs {gate_key}")
        result[gate_name] = gate_key
    return result


def validate_gate_mappings(specs: list[GateSpec], mappings: dict[str, str], option_name: str) -> None:
    valid_names = {each.name for each in specs}
    unknown = sorted(each for each in mappings if each not in valid_names)
    if unknown:
        raise ValueError(f"{option_name} references unknown gate(s): {', '.join(unknown)}")


def flatten_tracked_paths(values: list[list[str]]) -> list[str]:
    result = []
    for group in values:
        result.extend(group)
    return result


def validate_workdir(path: Path) -> Path:
    workdir = path.resolve()
    if not workdir.exists():
        raise ValueError(f"working directory does not exist: {workdir}")
    if not workdir.is_dir():
        raise ValueError(f"working directory is not a directory: {workdir}")
    return workdir


def validate_state_inputs(tracked_paths: list[str], reuse_mapping: dict[str, str], record_mapping: dict[str, str], state_file: str | None) -> None:
    if not state_file and (reuse_mapping or record_mapping):
        raise ValueError("state-file is required when reuse-gate or record-gate is configured")
    if state_file and not tracked_paths:
        raise ValueError("tracked-path is required when state-file is configured")


def current_digest(tracked_paths: list[str]) -> tuple[str, int]:
    normalized_paths = snapshot_state.tracked_paths(tracked_paths)
    return snapshot_state.calculate_digest(normalized_paths), len(normalized_paths)


def gate_digest(base_digest: str, spec: GateSpec, workdir: Path) -> str:
    return snapshot_state.extend_digest(base_digest, [str(workdir), spec.name, spec.command])


def run_gate(spec: GateSpec, workdir: Path) -> GateResult:
    started = time.monotonic()
    completed = subprocess.run(spec.command, shell=True, cwd=workdir, capture_output=True, text=True)
    return GateResult(
        spec=spec,
        status="executed",
        returncode=completed.returncode,
        stdout=completed.stdout,
        stderr=completed.stderr,
        duration_seconds=time.monotonic() - started,
    )


def run_serial(specs: list[GateSpec], workdir: Path) -> list[GateResult]:
    return [run_gate(spec, workdir) for spec in specs]


def run_parallel(specs: list[GateSpec], workdir: Path, max_parallel: int) -> list[GateResult]:
    ordered_results: list[GateResult | None] = [None] * len(specs)
    with ThreadPoolExecutor(max_workers=max_parallel) as executor:
        future_to_index = {executor.submit(run_gate, spec, workdir): index for index, spec in enumerate(specs)}
        for future in as_completed(future_to_index):
            ordered_results[future_to_index[future]] = future.result()
    return [each for each in ordered_results if each is not None]


def reusable_result(spec: GateSpec, gate_key: str, digest: str) -> GateResult:
    return GateResult(spec=spec, status="reused", returncode=0, stdout="", stderr="", duration_seconds=0.0, state_gate=gate_key, digest=digest)


def print_result(result: GateResult) -> None:
    if "reused" == result.status:
        print(f"[gate:{result.spec.name}] reused gate={result.state_gate} digest={result.digest}")
        return
    print(f"[gate:{result.spec.name}] exit={result.returncode} duration={result.duration_seconds:.2f}s")
    print(f"[gate:{result.spec.name}] cmd={result.spec.command}")
    if 0 != result.returncode:
        if result.stdout.strip():
            print(f"[gate:{result.spec.name}] stdout:")
            print(result.stdout.rstrip())
        if result.stderr.strip():
            print(f"[gate:{result.spec.name}] stderr:")
            print(result.stderr.rstrip())


def load_state(state_file: str | None) -> dict:
    return snapshot_state.read_state(Path(state_file)) if state_file else {}


def gate_digests(specs: list[GateSpec], base_digest: str | None, workdir: Path) -> dict[str, str]:
    if base_digest is None:
        return {}
    return {each.name: gate_digest(base_digest, each, workdir) for each in specs}


def partition_specs_for_execution(
        specs: list[GateSpec], reuse_mapping: dict[str, str], digests: dict[str, str], state: dict,
) -> tuple[list[GateResult | None], list[GateSpec], list[int]]:
    indexed_results: list[GateResult | None] = [None] * len(specs)
    executable_specs = []
    executable_indices = []
    for index, spec in enumerate(specs):
        gate_key = reuse_mapping.get(spec.name)
        gate_result_digest = digests.get(spec.name)
        if gate_key and gate_result_digest and snapshot_state.matches_gate_digest(state, gate_key, gate_result_digest):
            indexed_results[index] = reusable_result(spec, gate_key, gate_result_digest)
            continue
        executable_specs.append(spec)
        executable_indices.append(index)
    return indexed_results, executable_specs, executable_indices


def execute_specs(specs: list[GateSpec], workdir: Path, max_parallel: int) -> list[GateResult]:
    if 1 == max_parallel:
        return run_serial(specs, workdir)
    return run_parallel(specs, workdir, max_parallel)


def mark_green_gates(
        state: dict, record_mapping: dict[str, str], results: list[GateResult], gate_digests: dict[str, str], tracked_file_count: int | None,
) -> bool:
    if not gate_digests or tracked_file_count is None:
        return False
    updated = False
    for each in results:
        if "executed" != each.status or 0 != each.returncode or each.spec.name not in record_mapping:
            continue
        snapshot_state.set_gate_digest(state, record_mapping[each.spec.name], gate_digests[each.spec.name], tracked_file_count)
        updated = True
    return updated


def main() -> int:
    parser = argparse.ArgumentParser(description="Run independent quality gates in parallel.")
    parser.add_argument("--workdir", default=".", help="Working directory for every gate command.")
    parser.add_argument("--serial", action="store_true", help="Run gates serially instead of in parallel.")
    parser.add_argument("--max-parallel", type=int, default=4, help="Maximum parallel gate count.")
    parser.add_argument("--state-file", help="Optional state file used for gate-level reuse.")
    parser.add_argument("--tracked-path", nargs="+", action="append", default=[], help="Tracked file paths used to compute the verification digest.")
    parser.add_argument("--reuse-gate", action="append", default=[], help="Reuse mapping in the form gate-name[=state-gate].")
    parser.add_argument("--record-gate", action="append", default=[], help="Record mapping in the form gate-name[=state-gate].")
    parser.add_argument("--gate", action="append", required=True, help="Gate definition in the form name=command.")
    args = parser.parse_args()

    try:
        workdir = validate_workdir(Path(args.workdir))
        specs = parse_gates(args.gate)
        reuse_mapping = parse_gate_mappings(args.reuse_gate)
        record_mapping = parse_gate_mappings(args.record_gate)
        validate_gate_mappings(specs, reuse_mapping, "--reuse-gate")
        validate_gate_mappings(specs, record_mapping, "--record-gate")
        tracked_paths = flatten_tracked_paths(args.tracked_path)
        validate_state_inputs(tracked_paths, reuse_mapping, record_mapping, args.state_file)
    except ValueError as ex:
        parser.error(str(ex))
    try:
        base_digest = None
        tracked_file_count = None
        state = load_state(args.state_file)
        if tracked_paths:
            base_digest, tracked_file_count = current_digest(tracked_paths)
        gate_digest_map = gate_digests(specs, base_digest, workdir)
        max_parallel = 1 if args.serial else max(1, min(args.max_parallel, len(specs)))
        print(f"[quality-gates] mode={'serial' if 1 == max_parallel else 'parallel'} gateCount={len(specs)} workdir={workdir}")
        indexed_results, executable_specs, executable_indices = partition_specs_for_execution(specs, reuse_mapping, gate_digest_map, state)
        executed_results = execute_specs(executable_specs, workdir, max_parallel)
        for index, result in zip(executable_indices, executed_results):
            indexed_results[index] = result
        results = [each for each in indexed_results if each is not None]
        failed = False
        for each in results:
            print_result(each)
            failed = failed or 0 != each.returncode
        if args.state_file and mark_green_gates(state, record_mapping, results, gate_digest_map, tracked_file_count):
            snapshot_state.write_state(Path(args.state_file), state)
        return 1 if failed else 0
    except (ValueError, OSError, json.JSONDecodeError) as ex:
        print(ex, file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
