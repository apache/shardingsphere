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
"""Mechanical quality-rule scan and task-scope guard for gen-ut."""

import argparse
import json
import re
import subprocess
import sys
from collections import defaultdict
from dataclasses import asdict
from dataclasses import dataclass
from pathlib import Path

from guard_task_scope import check_scope_baseline, get_added_lines_for_path, get_repo_root, load_scope_baseline, write_scope_baseline


@dataclass(frozen=True)
class CandidateSummary:
    path: str
    method: str
    plain_test_count: int
    parameterized_present: bool


@dataclass(frozen=True)
class RuleSpec:
    name: str
    message: str
    mode: str


@dataclass(frozen=True)
class FileScanContext:
    path: Path
    source: str
    method_bodies: dict[str, str]
    method_blocks: dict[str, tuple[int, str]]
    candidates: list[CandidateSummary]
    target_type_name: str | None
    target_public_methods: set[str]
    added_lines: list[str]


RULE_SPECS = (
    RuleSpec("R15-C", "out-of-scope worktree mutation detected", "automated"),
    RuleSpec("R15-J", "helper or provider invokes a target public method", "hybrid"),
)
CONSUMER_TOKEN_PATTERN = re.compile(r"\bConsumer\s*(?:<|\b)")
CONSTRUCTOR_CALL_PATTERN = re.compile(r"\bnew\s+(\w+)\s*\(")
METHOD_DECL_PATTERN = re.compile(r"(?:private|protected|public)?\s*(?:static\s+)?[\w$<>\[\], ?]+\s+(\w+)\s*\([^)]*\)\s*(?:throws [^{]+)?\{", re.S)
METHOD_SOURCE_PATTERN = re.compile(r"@MethodSource(?:\s*\(([^)]*)\))?")
PARAM_METHOD_BODY_PATTERN = re.compile(
    r"@ParameterizedTest(?:\s*\([^)]*\))?\s*(?:@\w+(?:\s*\([^)]*\))?\s*)*void\s+(assert\w+)\s*\([^)]*\)\s*(?:throws [^{]+)?\{",
    re.S,
)
PARAM_METHOD_PATTERN = re.compile(
    r"@ParameterizedTest(?:\s*\([^)]*\))?\s*((?:@\w+(?:\s*\([^)]*\))?\s*)*)void\s+(assert\w+)\s*\(([^)]*)\)\s*(?:throws [^{]+)?",
    re.S,
)
TEST_METHOD_DECL_PATTERN = re.compile(
    r"((?:@Test(?:\s*\([^)]*\))?|@ParameterizedTest(?:\s*\([^)]*\))?|@RepeatedTest(?:\s*\([^)]*\))?)\s*(?:@\w+(?:\s*\([^)]*\))?\s*)*)"
    r"void\s+(assert\w+)\s*\([^)]*\)\s*(?:throws [^{]+)?\{",
    re.S,
)
R15_A_CALL_PATTERN = re.compile(r"\b\w+\.(\w+)\s*\(")
R15_A_IGNORE = {"assertThat", "assertTrue", "assertFalse", "mock", "when", "verify", "is", "not"}
R15_G_TYPE_DECL_PATTERN = re.compile(
    r"^\+\s+(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed)\s+)*(class|interface|enum|record)\b"
)
R15_H_IF_ELSE_PATTERN = re.compile(
    r"if\s*\([^)]*\)\s*\{[\s\S]*?assertTrue\s*\([^;]+\)\s*;[\s\S]*?\}\s*else\s*\{[\s\S]*?assertFalse\s*\([^;]+\)\s*;[\s\S]*?\}"
    r"|if\s*\([^)]*\)\s*\{[\s\S]*?assertFalse\s*\([^;]+\)\s*;[\s\S]*?\}\s*else\s*\{[\s\S]*?assertTrue\s*\([^;]+\)\s*;[\s\S]*?\}",
    re.S,
)
R15_H_IF_RETURN_PATTERN = re.compile(
    r"if\s*\([^)]*\)\s*\{[\s\S]*?assertTrue\s*\([^;]+\)\s*;[\s\S]*?return\s*;[\s\S]*?\}\s*assertFalse\s*\([^;]+\)\s*;"
    r"|if\s*\([^)]*\)\s*\{[\s\S]*?assertFalse\s*\([^;]+\)\s*;[\s\S]*?return\s*;[\s\S]*?\}\s*assertTrue\s*\([^;]+\)\s*;",
    re.S,
)
R15_SWITCH_PATTERN = re.compile(r"\bswitch\s*\(")
TYPE_DECL_LINE_PATTERN = re.compile(
    r"^\s*(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed)\s+)*(class|interface|enum|record)\s+(\w+)\b"
)
PUBLIC_METHOD_DECL_PATTERN = re.compile(
    r"^\s*public\s+(?:default\s+)?(?:static\s+)?(?:final\s+)?[\w$<>\[\], ?]+\s+(\w+)\s*\(",
    re.M,
)
CONSTRUCTOR_TEST_PREFIXES = ("New", "Construct", "Constructor")


def line_number(source: str, index: int) -> int:
    return source.count("\n", 0, index) + 1


def mask_java_non_code(source: str, mask_literals: bool) -> str:
    result = list(source)
    index = 0
    state = "code"
    while index < len(source):
        if "line-comment" == state:
            if "\n" == source[index]:
                state = "code"
            else:
                result[index] = " "
            index += 1
            continue
        if "block-comment" == state:
            if source.startswith("*/", index):
                result[index:index + 2] = [" ", " "]
                index += 2
                state = "code"
            else:
                if "\n" != source[index]:
                    result[index] = " "
                index += 1
            continue
        if state in ("string", "character"):
            delimiter = '"' if "string" == state else "'"
            if "\\" == source[index] and index + 1 < len(source):
                if mask_literals:
                    result[index:index + 2] = [" ", " "]
                index += 2
                continue
            if mask_literals and "\n" != source[index]:
                result[index] = " "
            if delimiter == source[index]:
                state = "code"
            index += 1
            continue
        if "text-block" == state:
            if source.startswith('"""', index):
                if mask_literals:
                    result[index:index + 3] = [" ", " ", " "]
                index += 3
                state = "code"
            else:
                escaped = "\\" == source[index] and index + 1 < len(source)
                if mask_literals:
                    result[index] = " " if "\n" != source[index] else "\n"
                    if escaped and "\n" != source[index + 1]:
                        result[index + 1] = " "
                index += 2 if escaped else 1
            continue
        if source.startswith("//", index):
            result[index:index + 2] = [" ", " "]
            index += 2
            state = "line-comment"
        elif source.startswith("/*", index):
            result[index:index + 2] = [" ", " "]
            index += 2
            state = "block-comment"
        elif source.startswith('"""', index):
            if mask_literals:
                result[index:index + 3] = [" ", " ", " "]
            index += 3
            state = "text-block"
        elif source[index] in ('"', "'"):
            if mask_literals:
                result[index] = " "
            state = "string" if '"' == source[index] else "character"
            index += 1
        else:
            index += 1
    return "".join(result)


def opening_brace_index(match: re.Match[str]) -> int:
    return match.end() - 1


def extract_block(text: str, brace_index: int) -> str:
    depth = 0
    index = brace_index
    while index < len(text):
        if "{" == text[index]:
            depth += 1
        elif "}" == text[index]:
            depth -= 1
            if 0 == depth:
                return text[brace_index + 1:index]
        index += 1
    return ""


def parse_method_sources(method_name: str, annotation_block: str) -> list[str]:
    result = []
    matches = list(METHOD_SOURCE_PATTERN.finditer(annotation_block))
    if not matches:
        return result
    for each in matches:
        raw = each.group(1)
        if raw is None or not raw.strip():
            result.append(method_name)
            continue
        normalized = re.sub(r"\bvalue\s*=\s*", "", raw.strip())
        for name in re.findall(r'"([^"]+)"', normalized):
            result.append(name.split("#", 1)[-1])
    return result


def parse_method_blocks(source: str) -> dict[str, tuple[int, str]]:
    result = {}
    for match in METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(1)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        if brace_index >= 0:
            result[method_name] = (line, extract_block(source, brace_index))
    return result


def get_top_level_class_name(source: str) -> str | None:
    for line in source.splitlines():
        match = TYPE_DECL_LINE_PATTERN.match(line)
        if match:
            return match.group(2)
    return None


def get_target_type_name(source: str) -> str | None:
    top_level_class_name = get_top_level_class_name(source)
    if top_level_class_name and top_level_class_name.endswith("Test"):
        return top_level_class_name[:-4]
    return None


def get_test_method_names(source: str) -> set[str]:
    return {match.group(2) for match in TEST_METHOD_DECL_PATTERN.finditer(source)}


def resolve_target_source_path(path: Path, source: str) -> Path | None:
    target_type_name = get_target_type_name(source)
    if not target_type_name:
        return None
    path_text = path.as_posix()
    test_marker = "src/test/java/"
    marker_index = path_text.find(test_marker)
    if marker_index < 0:
        return None
    target_path = path_text[:marker_index] + "src/main/java/" + path_text[marker_index + len(test_marker):]
    return Path(target_path).with_name(f"{target_type_name}.java")


def load_target_public_methods(path: Path, source: str) -> set[str]:
    target_source_path = resolve_target_source_path(path, source)
    if target_source_path is None or not target_source_path.exists():
        return set()
    target_source = mask_java_non_code(target_source_path.read_text(encoding="utf-8"), False)
    return {match.group(1) for match in PUBLIC_METHOD_DECL_PATTERN.finditer(target_source)}


def list_distinct(values: list[str]) -> list[str]:
    return list(dict.fromkeys(values))


def extract_invoked_methods(body: str) -> list[str]:
    return list_distinct([each for each in R15_A_CALL_PATTERN.findall(body) if each not in R15_A_IGNORE])


def extract_constructed_types(body: str) -> list[str]:
    return list_distinct(CONSTRUCTOR_CALL_PATTERN.findall(body))


def method_name_prefix(method_name: str) -> str:
    return method_name[0].upper() + method_name[1:] if method_name else method_name


def infer_candidate_target(test_method_name: str, invoked_methods: list[str], constructed_types: list[str], target_type_name: str | None) -> str | None:
    raw_name = test_method_name[6:] if test_method_name.startswith("assert") else test_method_name
    if target_type_name and target_type_name in constructed_types and (raw_name.startswith(f"New{target_type_name}") or raw_name.startswith(CONSTRUCTOR_TEST_PREFIXES)):
        return f"constructor:{target_type_name}"
    for candidate_name in (raw_name, raw_name[3:] if raw_name.startswith("Not") else raw_name):
        matching_methods = [each for each in invoked_methods if candidate_name.startswith(method_name_prefix(each))]
        if matching_methods:
            return max(matching_methods, key=len)
    if 1 == len(invoked_methods):
        return invoked_methods[0]
    return None


def analyze_parameterization_candidates(path: Path, source: str, target_public_methods: set[str]) -> list[CandidateSummary]:
    source = mask_java_non_code(source, False)
    target_type_name = get_target_type_name(source)
    statistics = defaultdict(lambda: {"plain": 0, "parameterized": False})
    for match in TEST_METHOD_DECL_PATTERN.finditer(source):
        annotation_block = match.group(1)
        test_method_name = match.group(2)
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        invoked_methods = [each for each in extract_invoked_methods(body) if each in target_public_methods]
        constructed_types = extract_constructed_types(body)
        target = infer_candidate_target(test_method_name, invoked_methods, constructed_types, target_type_name)
        if target is None:
            continue
        if "@ParameterizedTest" in annotation_block:
            statistics[target]["parameterized"] = True
        else:
            statistics[target]["plain"] += 1
    result = []
    for method_name in sorted(statistics):
        plain_test_count = statistics[method_name]["plain"]
        parameterized_present = statistics[method_name]["parameterized"]
        if plain_test_count >= 3 or parameterized_present:
            result.append(CandidateSummary(
                path=str(path),
                method=method_name,
                plain_test_count=plain_test_count,
                parameterized_present=parameterized_present,
            ))
    return result


def describe_candidate(candidate: dict) -> str:
    return f'{candidate["path"]}: method={candidate["method"]} plainTestCount={candidate["plain_test_count"]} parameterizedPresent={candidate["parameterized_present"]}'


def find_switch_dispatch_candidates(path: Path, source: str) -> list[str]:
    violations = []
    for match in PARAM_METHOD_BODY_PATTERN.finditer(source):
        method_name = match.group(1)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        if R15_SWITCH_PATTERN.search(body):
            violations.append(f"{path}:{line} method={method_name}")
    return violations


def check_r15_g(path: Path, source: str, added_lines: list[str]) -> list[str]:
    if "@ParameterizedTest" not in source:
        return []
    top_level_class_name = get_top_level_class_name(source)
    violations = []
    for line in added_lines:
        if line.startswith("+++") or line.startswith("@@"):
            continue
        if not line.startswith("+"):
            continue
        if not R15_G_TYPE_DECL_PATTERN.search(line):
            continue
        stripped = line[1:].strip()
        match = TYPE_DECL_LINE_PATTERN.match(stripped)
        if match and match.group(2) == top_level_class_name:
            continue
        violations.append(f"{path}: {stripped}")
    return violations


def find_consumer_transport_candidates(path: Path, source: str, method_bodies: dict[str, str]) -> list[str]:
    violations = []
    for match in PARAM_METHOD_PATTERN.finditer(source):
        annotation_block = match.group(1)
        method_name = match.group(2)
        params = match.group(3)
        line = line_number(source, match.start())
        if CONSUMER_TOKEN_PATTERN.search(params):
            violations.append(f"{path}:{line} method={method_name} reason=consumerInParameterizedMethodSignature")
        for provider in parse_method_sources(method_name, annotation_block):
            body = method_bodies.get(provider)
            if body and CONSUMER_TOKEN_PATTERN.search(body):
                violations.append(f"{path}:{line} method={method_name} provider={provider} reason=consumerInMethodSourceArguments")
    return violations


def check_r15_j(context: FileScanContext) -> list[str]:
    if not context.target_type_name or not context.target_public_methods:
        return []
    test_method_names = get_test_method_names(context.source)
    local_var_pattern = re.compile(rf"\b{re.escape(context.target_type_name)}\s+(\w+)\s*=")
    method_pattern = "|".join(sorted((re.escape(each) for each in context.target_public_methods), key=len, reverse=True))
    if not method_pattern:
        return []
    violations = []
    for method_name, (line, body) in context.method_blocks.items():
        if method_name in test_method_names:
            continue
        local_vars = list_distinct(local_var_pattern.findall(body))
        if local_vars:
            variable_pattern = "|".join(sorted((re.escape(each) for each in local_vars), key=len, reverse=True))
            if re.search(rf"\b(?:{variable_pattern})\s*\.\s*(?:{method_pattern})\s*\(", body):
                violations.append(f"{context.path}:{line} method={method_name} reason=helperInvokesTargetPublicMethod")
                continue
        if re.search(rf"new\s+{re.escape(context.target_type_name)}\s*\([^;{{}}]*\)\s*\.\s*(?:{method_pattern})\s*\(", body, re.S):
            violations.append(f"{context.path}:{line} method={method_name} reason=chainedTargetInvocationOutsideTestBody")
    return violations


def find_boolean_dispatch_candidates(path: Path, source: str) -> list[str]:
    source = mask_java_non_code(source, True)
    violations = []
    for match in TEST_METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(2)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        switch_dispatch = R15_SWITCH_PATTERN.search(body) and "assertTrue" in body and "assertFalse" in body
        if R15_H_IF_ELSE_PATTERN.search(body) or R15_H_IF_RETURN_PATTERN.search(body) or switch_dispatch:
            violations.append(f"{path}:{line} method={method_name}")
    return violations


def create_file_scan_context(path: Path, scope_baseline: dict | None = None) -> FileScanContext:
    source = path.read_text(encoding="utf-8")
    method_blocks = parse_method_blocks(source)
    target_public_methods = load_target_public_methods(path, source)
    return FileScanContext(
        path=path,
        source=source,
        method_bodies={name: body for name, (_, body) in method_blocks.items()},
        method_blocks=method_blocks,
        candidates=analyze_parameterization_candidates(path, source, target_public_methods),
        target_type_name=get_target_type_name(source),
        target_public_methods=target_public_methods,
        added_lines=get_added_lines_for_path(path, scope_baseline),
    )


def build_rule_result(violations_by_rule: dict[str, list[str]]) -> dict[str, dict[str, object]]:
    return {
        each.name: {
            "message": each.message,
            "mode": each.mode,
            "violations": violations_by_rule[each.name],
        }
        for each in RULE_SPECS
    }


def find_argument_source_reviews(path: Path, source: str) -> list[str]:
    result = []
    for match in PARAM_METHOD_PATTERN.finditer(source):
        method_name = match.group(2)
        providers = parse_method_sources(method_name, match.group(1))
        detail = f"providers={','.join(providers)}" if providers else "configured argument source"
        result.append(f"{path}:{line_number(source, match.start())} method={method_name} {detail}")
    return result


def collect_scan_result(paths: list[Path], scope_baseline_path: Path | None) -> dict:
    violations_by_rule = defaultdict(list)
    java_paths = [each for each in paths if each.suffix == ".java"]
    scope_baseline = load_scope_baseline(scope_baseline_path) if scope_baseline_path and scope_baseline_path.exists() else None
    contexts = [create_file_scan_context(each, scope_baseline) for each in java_paths]
    for context in contexts:
        violations_by_rule["R15-J"].extend(check_r15_j(context))
    violations_by_rule["R15-C"].extend(
        check_scope_baseline(scope_baseline_path, paths) if scope_baseline_path else ["missing scope baseline"]
    )
    return {
        "rules": build_rule_result(violations_by_rule),
        "candidates": [asdict(each) for context in contexts for each in context.candidates],
        "reviews": {
            "R15-D": [each for context in contexts for each in find_argument_source_reviews(context.path, context.source)],
            "R15-F": [each for context in contexts for each in find_switch_dispatch_candidates(context.path, context.source)],
            "R15-G": [each for context in contexts for each in check_r15_g(context.path, context.source, context.added_lines)],
            "R15-H": [each for context in contexts for each in find_boolean_dispatch_candidates(context.path, context.source)],
            "R15-I": [each for context in contexts for each in find_consumer_transport_candidates(context.path, context.source, context.method_bodies)],
            "R15-J": [str(context.path) for context in contexts if not context.target_public_methods],
        },
        "java_file_count": len(contexts),
    }


def failed_rule_names(result: dict) -> list[str]:
    return [each.name for each in RULE_SPECS if result["rules"][each.name]["violations"]]


def print_rule_summary(result: dict) -> int:
    failed_rules = set(failed_rule_names(result))
    for each in RULE_SPECS:
        violations = result["rules"][each.name]["violations"]
        if each.name in failed_rules:
            print(f"[{each.name}] {each.message}")
            for violation in violations:
                print(violation)
            continue
        if "automated" == each.mode:
            print(f"[{each.name}] ok")
        else:
            print(f"[{each.name}] mechanical=ok")
    for name, reviews in result.get("reviews", {}).items():
        for review in reviews:
            print(f"[{name}] review: {review}")
    return 1 if failed_rules else 0


def print_summary_only(result: dict) -> int:
    candidates = result["candidates"]
    if candidates:
        print("[R8-CANDIDATES]")
        for each in candidates:
            print(describe_candidate(each))
    else:
        print("[R8-CANDIDATES] no candidates")
    failed_rules = [f"{each.name}={len(result['rules'][each.name]['violations'])}" for each in RULE_SPECS if result["rules"][each.name]["violations"]]
    print(f"[summary] javaFiles={result['java_file_count']}")
    reviews = [f"{name}={len(locations)}" for name, locations in result.get("reviews", {}).items() if locations]
    if reviews:
        print(f"[summary] reviews={' '.join(reviews)}")
    if failed_rules:
        print(f"[summary] violations={' '.join(failed_rules)}")
        return 1
    print("[summary] all mechanical rules ok")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Mechanical quality-rule scan and task-scope guard for gen-ut.")
    parser.add_argument("--scope-baseline", help="Structured task scope baseline created before editing.")
    parser.add_argument("--capture-scope-baseline", help="Write a structured task scope baseline to this path, then exit.")
    parser.add_argument("--json", action="store_true", help="Emit JSON output instead of the default text report.")
    parser.add_argument("--summary-only", action="store_true", help="Emit a compact text summary with candidate information.")
    parser.add_argument("paths", nargs="*", help="Resolved test file set.")
    args = parser.parse_args()

    try:
        if not args.paths:
            parser.error("at least one resolved test file is required")
        paths = [Path(each) for each in args.paths]
        if args.capture_scope_baseline:
            output_path = Path(args.capture_scope_baseline)
            write_scope_baseline(output_path, get_repo_root(), paths)
            print(f"[scope-baseline] path={output_path.resolve()} allowedPathCount={len(paths)}")
            return 0
        baseline_path = Path(args.scope_baseline) if args.scope_baseline else None
        result = collect_scan_result(paths, baseline_path)
        if args.json:
            print(json.dumps(result, indent=2, sort_keys=True))
            return 1 if failed_rule_names(result) else 0
        if args.summary_only:
            return print_summary_only(result)
        return print_rule_summary(result)
    except (OSError, ValueError, json.JSONDecodeError, subprocess.CalledProcessError) as ex:
        print(ex, file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
