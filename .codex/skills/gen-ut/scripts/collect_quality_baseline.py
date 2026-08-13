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
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import scan_quality_rules as quality_rules


def parse_target_classes(raw: str) -> list[str]:
    result = [each.strip() for each in raw.split(",") if each.strip()]
    if result:
        return result
    raise ValueError("target-classes must not be empty")


def find_sourcefile_node(root: ET.Element, fqcn: str) -> ET.Element | None:
    package_name, _, simple_name = fqcn.rpartition(".")
    package_path = package_name.replace(".", "/")
    source_name = f"{simple_name}.java"
    for package in root.findall("package"):
        if package.get("name") != package_path:
            continue
        for sourcefile in package.findall("sourcefile"):
            if sourcefile.get("name") == source_name:
                return sourcefile
    return None


def summarize_target_coverage(root: ET.Element, fqcn: str) -> tuple[bool, dict[str, tuple[int, int, float] | None], list[int]]:
    class_name = fqcn.replace(".", "/")
    matched_nodes = [each for each in root.iter("class") if each.get("name") == class_name or each.get("name", "").startswith(class_name + "$")]
    counters: dict[str, tuple[int, int, float] | None] = {}
    for counter_type in ("CLASS", "LINE", "BRANCH"):
        covered = 0
        missed = 0
        found_counter = False
        for each in matched_nodes:
            counter = next((item for item in each.findall("counter") if item.get("type") == counter_type), None)
            if counter is None:
                continue
            found_counter = True
            covered += int(counter.get("covered"))
            missed += int(counter.get("missed"))
        total = covered + missed
        counters[counter_type] = (covered, missed, 100.0 if 0 == total else covered * 100.0 / total) if found_counter else None
    sourcefile = find_sourcefile_node(root, fqcn)
    missed_branch_lines = []
    if sourcefile is not None:
        missed_branch_lines = [int(each.get("nr")) for each in sourcefile.findall("line") if int(each.get("mb", "0")) > 0]
    return bool(matched_nodes), counters, missed_branch_lines


def meets_target(found: bool, counters: dict[str, tuple[int, int, float] | None], minimum_ratio: float) -> bool:
    return found and all(counters[each] is not None and counters[each][2] + 1e-9 >= minimum_ratio for each in ("CLASS", "LINE", "BRANCH"))


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
            mode = scan_result["rules"][rule]["mode"]
            if "automated" == mode:
                print(f"[{rule}] ok")
            elif "manual" == mode:
                print(f"[{rule}] semanticReviewRequired=true")
            else:
                print(f"[{rule}] mechanical=ok semanticReviewRequired=true")
            for each in scan_result.get("reviews", {}).get(rule, []):
                print(f"review: {each}")
    prechecks = scan_result.get("prechecks", {})
    for name in sorted(prechecks):
        violations = prechecks[name]["violations"]
        if not violations:
            print(f"[precheck:{name}] ok")
            continue
        print(f"[precheck:{name}] {prechecks[name]['message']}")
        for each in violations:
            print(each)


def print_coverage_baseline(jacoco_xml_path: Path, target_classes: list[str], minimum_ratio: float | None) -> bool:
    root = ET.parse(jacoco_xml_path).getroot()
    result = True
    for fqcn in target_classes:
        found, counters, missed_branch_lines = summarize_target_coverage(root, fqcn)
        if not found:
            print(f"[baseline] {fqcn} coverageStatus=missing")
            result = False
            continue
        for counter_type in ("CLASS", "LINE", "BRANCH"):
            counter = counters[counter_type]
            if counter is None:
                print(f"[baseline] {fqcn} (+inner) {counter_type} coverageStatus=missing")
                result = False
                continue
            covered, missed, ratio = counter
            print(f"[baseline] {fqcn} (+inner) {counter_type} covered={covered} missed={missed} ratio={ratio:.2f}%")
        if missed_branch_lines:
            line_text = ",".join(str(each) for each in missed_branch_lines)
            print(f"[baseline] {fqcn} branchMissLines={line_text}")
        else:
            print(f"[baseline] {fqcn} branchMissLines=none")
        if minimum_ratio is not None and not meets_target(found, counters, minimum_ratio):
            result = False
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description="Collect baseline coverage and quality-rule diagnostics for gen-ut.")
    parser.add_argument("--jacoco-xml-path", required=True, help="Path to the jacoco.xml generated by the coverage command.")
    parser.add_argument("--target-classes", required=True, help="Comma-separated target production classes.")
    parser.add_argument("--scope-baseline", help="Structured task scope baseline created by scan_quality_rules.py.")
    parser.add_argument("--minimum-ratio", type=float, help="Fail unless every target CLASS, LINE, and BRANCH ratio meets this percentage.")
    parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    args = parser.parse_args()

    try:
        target_classes = parse_target_classes(args.target_classes)
        if args.minimum_ratio is not None and not 0.0 <= args.minimum_ratio <= 100.0:
            raise ValueError("minimum-ratio must be between 0 and 100")
    except ValueError as ex:
        parser.error(str(ex))
    paths = [Path(each) for each in args.paths]
    baseline_path = Path(args.scope_baseline) if args.scope_baseline else None
    scan_result = quality_rules.collect_scan_result(paths, baseline_path)
    rules_ok = not quality_rules.failed_rule_names(scan_result)
    print_rule_baseline(scan_result)
    jacoco_xml_path = Path(args.jacoco_xml_path)
    if not jacoco_xml_path.exists():
        print(f"[baseline] missingJacocoXml={jacoco_xml_path}", file=sys.stderr)
        return 2
    try:
        coverage_ok = print_coverage_baseline(jacoco_xml_path, target_classes, args.minimum_ratio)
    except (OSError, ET.ParseError) as ex:
        print(f"[baseline] invalidJacocoXml={jacoco_xml_path}: {ex}", file=sys.stderr)
        return 2
    return 0 if args.minimum_ratio is None or coverage_ok and rules_ok else 1


if __name__ == "__main__":
    sys.exit(main())
