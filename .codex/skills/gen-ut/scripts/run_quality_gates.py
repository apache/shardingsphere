#!/usr/bin/env python3
"""
Run independent final gates in parallel and print deterministic summaries.
"""

import argparse
import subprocess
import sys
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class GateSpec:
    name: str
    command: str


@dataclass(frozen=True)
class GateResult:
    spec: GateSpec
    returncode: int
    stdout: str
    stderr: str
    duration_seconds: float


def parse_gate(raw: str) -> GateSpec:
    if "=" not in raw:
        raise ValueError(f"invalid gate definition: {raw!r}")
    name, command = raw.split("=", 1)
    name = name.strip()
    command = command.strip()
    if not name or not command:
        raise ValueError(f"invalid gate definition: {raw!r}")
    return GateSpec(name=name, command=command)


def validate_workdir(path: Path) -> Path:
    workdir = path.resolve()
    if not workdir.exists():
        raise ValueError(f"working directory does not exist: {workdir}")
    if not workdir.is_dir():
        raise ValueError(f"working directory is not a directory: {workdir}")
    return workdir


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


def run_gate(spec: GateSpec, workdir: Path) -> GateResult:
    started = time.monotonic()
    completed = subprocess.run(spec.command, shell=True, cwd=workdir, capture_output=True, text=True)
    return GateResult(
        spec=spec,
        returncode=completed.returncode,
        stdout=completed.stdout,
        stderr=completed.stderr,
        duration_seconds=time.monotonic() - started,
    )


def print_result(result: GateResult) -> None:
    print(f"[gate:{result.spec.name}] exit={result.returncode} duration={result.duration_seconds:.2f}s")
    print(f"[gate:{result.spec.name}] cmd={result.spec.command}")
    if 0 != result.returncode:
        if result.stdout.strip():
            print(f"[gate:{result.spec.name}] stdout:")
            print(result.stdout.rstrip())
        if result.stderr.strip():
            print(f"[gate:{result.spec.name}] stderr:")
            print(result.stderr.rstrip())


def run_serial(specs: list[GateSpec], workdir: Path) -> list[GateResult]:
    return [run_gate(spec, workdir) for spec in specs]


def run_parallel(specs: list[GateSpec], workdir: Path, max_parallel: int) -> list[GateResult]:
    ordered_results: list[GateResult | None] = [None] * len(specs)
    with ThreadPoolExecutor(max_workers=max_parallel) as executor:
        future_to_index = {executor.submit(run_gate, spec, workdir): index for index, spec in enumerate(specs)}
        for future in as_completed(future_to_index):
            ordered_results[future_to_index[future]] = future.result()
    return [each for each in ordered_results if each is not None]


def main() -> int:
    parser = argparse.ArgumentParser(description="Run independent final gates in parallel.")
    parser.add_argument("--workdir", default=".", help="Working directory for every gate command.")
    parser.add_argument("--serial", action="store_true", help="Run gates serially instead of in parallel.")
    parser.add_argument("--max-parallel", type=int, default=4, help="Maximum parallel gate count.")
    parser.add_argument("--gate", action="append", required=True, help="Gate definition in the form name=command.")
    args = parser.parse_args()

    try:
        workdir = validate_workdir(Path(args.workdir))
        specs = parse_gates(args.gate)
    except ValueError as ex:
        parser.error(str(ex))
    max_parallel = 1 if args.serial else max(1, min(args.max_parallel, len(specs)))
    print(f"[final-gates] mode={'serial' if 1 == max_parallel else 'parallel'} gateCount={len(specs)} workdir={workdir}")
    results = run_serial(specs, workdir) if 1 == max_parallel else run_parallel(specs, workdir, max_parallel)
    failed = False
    for result in results:
        print_result(result)
        failed = failed or 0 != result.returncode
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
