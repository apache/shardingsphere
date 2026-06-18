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
Collect a baseline quality summary for gen-ut before editing begins.
"""

import argparse
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import scan_quality_rules as quality_rules


@dataclass(frozen=True)
class CommandResult:
    command: str
    returncode: int
    stdout: str
    stderr: str
    duration_seconds: float


def run_command(command: str, workdir: Path) -> CommandResult:
    started = time.monotonic()
    completed = subprocess.run(command, shell=True, cwd=workdir, capture_output=True, text=True)
    return CommandResult(
        command=command,
        returncode=completed.returncode,
        stdout=completed.stdout,
        stderr=completed.stderr,
        duration_seconds=time.monotonic() - started,
    )


def parse_target_classes(raw: str) -> list[str]:
    result = [each.strip() for each in raw.split(",") if each.strip()]
    if result:
        return result
    raise ValueError("target-classes must not be empty")


def validate_workdir(path: Path) -> Path:
    workdir = path.resolve()
    if not workdir.exists():
        raise ValueError(f"working directory does not exist: {workdir}")
    if not workdir.is_dir():
        raise ValueError(f"working directory is not a directory: {workdir}")
    return workdir


def find_sourcefile_node(root: ET.Element, fqcn: str) -> ET.Element | None:
    package_name, simple_name = fqcn.rsplit(".", 1)
    package_path = package_name.replace(".", "/")
    source_name = f"{simple_name}.java"
    for package in root.findall("package"):
        if package.get("name") != package_path:
            continue
        for sourcefile in package.findall("sourcefile"):
            if sourcefile.get("name") == source_name:
                return sourcefile
    return None


def summarize_target_coverage(root: ET.Element, fqcn: str) -> tuple[dict[str, tuple[int, int, float]], list[int]]:
    class_name = fqcn.replace(".", "/")
    matched_nodes = [each for each in root.iter("class") if each.get("name") == class_name or each.get("name", "").startswith(class_name + "$")]
    counters = {}
    for counter_type in ("CLASS", "LINE", "BRANCH"):
        covered = 0
        missed = 0
        for each in matched_nodes:
            counter = next((item for item in each.findall("counter") if item.get("type") == counter_type), None)
            if counter is None:
                continue
            covered += int(counter.get("covered"))
            missed += int(counter.get("missed"))
        total = covered + missed
        counters[counter_type] = (covered, missed, 100.0 if 0 == total else covered * 100.0 / total)
    sourcefile = find_sourcefile_node(root, fqcn)
    missed_branch_lines = []
    if sourcefile is not None:
        missed_branch_lines = [int(each.get("nr")) for each in sourcefile.findall("line") if int(each.get("mb", "0")) > 0]
    return counters, missed_branch_lines


def print_rule_baseline(scan_result: dict) -> None:
    print(f"[baseline] javaFiles={scan_result['java_file_count']}")
    if scan_result["candidates"]:
        print("[R8-CANDIDATES]")
        for each in scan_result["candidates"]:
            print(quality_rules.describe_candidate(each))
    else:
        print("[R8-CANDIDATES] no candidates")
    for rule in quality_rules.RULE_ORDER:
        violations = scan_result["rules"][rule]["violations"]
        if violations:
            print(f"[{rule}] {scan_result['rules'][rule]['message']}")
            for each in violations:
                print(each)
        else:
            print(f"[{rule}] ok")
    prechecks = scan_result.get("prechecks", {})
    for name in sorted(prechecks):
        violations = prechecks[name]["violations"]
        if not violations:
            print(f"[precheck:{name}] ok")
            continue
        print(f"[precheck:{name}] {prechecks[name]['message']}")
        for each in violations:
            print(each)


def print_coverage_baseline(jacoco_xml_path: Path, target_classes: list[str]) -> None:
    root = ET.parse(jacoco_xml_path).getroot()
    for fqcn in target_classes:
        counters, missed_branch_lines = summarize_target_coverage(root, fqcn)
        for counter_type in ("CLASS", "LINE", "BRANCH"):
            covered, missed, ratio = counters[counter_type]
            print(f"[baseline] {fqcn} (+inner) {counter_type} covered={covered} missed={missed} ratio={ratio:.2f}%")
        if missed_branch_lines:
            line_text = ",".join(str(each) for each in missed_branch_lines)
            print(f"[baseline] {fqcn} branchMissLines={line_text}")
        else:
            print(f"[baseline] {fqcn} branchMissLines=none")


def main() -> int:
    parser = argparse.ArgumentParser(description="Collect baseline coverage and quality-rule diagnostics for gen-ut.")
    parser.add_argument("--workdir", default=".", help="Working directory for the coverage command.")
    parser.add_argument("--coverage-command", required=True, help="Shell command that generates the jacoco report for the target test scope.")
    parser.add_argument("--jacoco-xml-path", required=True, help="Path to the jacoco.xml generated by the coverage command.")
    parser.add_argument("--target-classes", required=True, help="Comma-separated target production classes.")
    parser.add_argument("--baseline-before", help="Path to the baseline git status captured at task start.")
    parser.add_argument("--allow-metadata-accessor-tests", action="store_true", help="Allow R15-B when the user explicitly requested metadata accessor tests.")
    parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    args = parser.parse_args()

    try:
        workdir = validate_workdir(Path(args.workdir))
        target_classes = parse_target_classes(args.target_classes)
    except ValueError as ex:
        parser.error(str(ex))
    java_paths = [Path(each) for each in args.paths if each.endswith(".java")]
    baseline_path = Path(args.baseline_before) if args.baseline_before else None
    scan_result = quality_rules.collect_scan_result(java_paths, baseline_path, args.allow_metadata_accessor_tests)
    print_rule_baseline(scan_result)
    coverage_result = run_command(args.coverage_command, workdir)
    print(f"[baseline] coverageCommandExit={coverage_result.returncode} duration={coverage_result.duration_seconds:.2f}s")
    print(f"[baseline] coverageCommand={coverage_result.command}")
    if 0 != coverage_result.returncode:
        if coverage_result.stdout.strip():
            print("[baseline] coverageStdout:")
            print(coverage_result.stdout.rstrip())
        if coverage_result.stderr.strip():
            print("[baseline] coverageStderr:")
            print(coverage_result.stderr.rstrip())
        return coverage_result.returncode
    jacoco_xml_path = Path(args.jacoco_xml_path)
    if not jacoco_xml_path.exists():
        print(f"[baseline] missingJacocoXml={jacoco_xml_path}", file=sys.stderr)
        return 2
    try:
        print_coverage_baseline(jacoco_xml_path, target_classes)
    except (OSError, ET.ParseError) as ex:
        print(f"[baseline] invalidJacocoXml={jacoco_xml_path}: {ex}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
