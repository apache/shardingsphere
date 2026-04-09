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
Consolidated quality-rule scan for gen-ut.
"""

import argparse
import json
import re
import subprocess
import sys
from collections import defaultdict
from dataclasses import asdict
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class CandidateSummary:
    path: str
    method: str
    plain_test_count: int
    parameterized_present: bool
    high_fit: bool


@dataclass(frozen=True)
class RuleSpec:
    name: str
    message: str


@dataclass(frozen=True)
class FileScanContext:
    path: Path
    source: str
    method_bodies: dict[str, str]
    method_blocks: dict[str, tuple[int, str]]
    candidates: list[CandidateSummary]
    target_type_name: str | None
    target_public_methods: set[str]


RULE_ORDER = ("R8", "R14", "R15-A", "R15-B", "R15-C", "R15-D", "R15-E", "R15-F", "R15-G", "R15-H", "R15-I", "R15-J")
PRECHECK_ORDER = (
    "checkstyle-final-parameters",
    "parameterized-methodsource",
    "parameterized-name-parameter",
)
PRECHECK_MESSAGES = {
    "checkstyle-final-parameters": "test method parameters should be final to avoid Checkstyle FinalParameters failures",
    "parameterized-methodsource": "parameterized tests should use @MethodSource providers with at least 3 Arguments rows",
    "parameterized-name-parameter": "parameterized tests should declare the first parameter exactly as `final String name`",
}
RULE_MESSAGES = {
    "R8": "@ParameterizedTest must use name = \"{0}\"",
    "R14": "forbidden boolean assertion found",
    "R15-A": "high-fit candidate likely exists but no parameterized test found",
    "R15-B": "metadata accessor test detected without explicit user request",
    "R15-C": "out-of-scope production path modified",
    "R15-D": "each @ParameterizedTest must have >= 3 Arguments rows from @MethodSource",
    "R15-E": "each @ParameterizedTest method must declare first parameter as `final String name`",
    "R15-F": "@ParameterizedTest method body must not contain switch",
    "R15-G": "parameterized tests must not introduce nested helper type declarations",
    "R15-H": "do not dispatch boolean assertions by control flow to choose assertTrue/assertFalse",
    "R15-I": "parameterized tests must not use Consumer in signatures or @MethodSource argument rows",
    "R15-J": "non-test helpers and @MethodSource providers must not invoke target public methods without assertions in test bodies",
}
RULE_SPECS = tuple(RuleSpec(each, RULE_MESSAGES[each]) for each in RULE_ORDER)
BOOLEAN_ASSERTION_BAN_PATTERN = re.compile(
    r"assertThat\s*\((?s:.*?)is\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)\s*\)"
    r"|assertEquals\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*,"
    r"|assertEquals\s*\((?s:.*?),\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)",
    re.S,
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
    r"((?:@Test(?:\s*\([^)]*\))?|@ParameterizedTest(?:\s*\([^)]*\))?)\s*(?:@\w+(?:\s*\([^)]*\))?\s*)*)"
    r"void\s+(assert\w+)\s*\([^)]*\)\s*(?:throws [^{]+)?\{",
    re.S,
)
TEST_METHOD_SIGNATURE_PATTERN = re.compile(
    r"((?:@Test(?:\s*\([^)]*\))?|@ParameterizedTest(?:\s*\([^)]*\))?)\s*(?:@\w+(?:\s*\([^)]*\))?\s*)*)"
    r"void\s+(assert\w+)\s*\(([^)]*)\)\s*(?:throws [^{]+)?",
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
R15_NAME_PATTERN = re.compile(r'name\s*=\s*"\{0\}"')
R15_SWITCH_PATTERN = re.compile(r"\bswitch\s*\(")
R15_B_PATTERN = re.compile(
    r"@Test(?s:.*?)void\s+assert\w*(GetType|GetOrder|GetTypeClass)\b"
    r"|assertThat\((?s:.*?)\.getType\(\)"
    r"|assertThat\((?s:.*?)\.getOrder\(\)"
    r"|assertThat\((?s:.*?)\.getTypeClass\(\)",
    re.S,
)
TYPE_DECL_LINE_PATTERN = re.compile(
    r"^\s*(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed)\s+)*(class|interface|enum|record)\s+(\w+)\b"
)
PUBLIC_METHOD_DECL_PATTERN = re.compile(
    r"^\s*public\s+(?:default\s+)?(?:static\s+)?(?:final\s+)?[\w$<>\[\], ?]+\s+(\w+)\s*\(",
    re.M,
)
UNTRACKED_STATUS_PREFIX = "?? "
CONSTRUCTOR_TEST_PREFIXES = ("New", "Construct", "Constructor")


def line_number(source: str, index: int) -> int:
    return source.count("\n", 0, index) + 1


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


def split_parameters(params: str) -> list[str]:
    result = []
    current = []
    angle_depth = 0
    paren_depth = 0
    bracket_depth = 0
    for char in params:
        if "," == char and 0 == angle_depth and 0 == paren_depth and 0 == bracket_depth:
            part = "".join(current).strip()
            if part:
                result.append(part)
            current = []
            continue
        current.append(char)
        if "<" == char:
            angle_depth += 1
        elif ">" == char and angle_depth > 0:
            angle_depth -= 1
        elif "(" == char:
            paren_depth += 1
        elif ")" == char and paren_depth > 0:
            paren_depth -= 1
        elif "[" == char:
            bracket_depth += 1
        elif "]" == char and bracket_depth > 0:
            bracket_depth -= 1
    tail = "".join(current).strip()
    if tail:
        result.append(tail)
    return result


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


def parse_method_bodies(source: str) -> dict[str, str]:
    return {name: body for name, (_, body) in parse_method_blocks(source).items()}


def parse_method_blocks(source: str) -> dict[str, tuple[int, str]]:
    result = {}
    for match in METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(1)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        if brace_index >= 0:
            result[method_name] = (line, extract_block(source, brace_index))
    return result


def run_git_command(args: list[str], *, allow_failure: bool = False) -> str:
    try:
        return subprocess.run(args, check=True, capture_output=True, text=True).stdout
    except subprocess.CalledProcessError:
        if allow_failure:
            return ""
        raise


def get_git_diff_lines(path: Path, *, cached: bool = False) -> list[str]:
    command = ["git", "diff"]
    if cached:
        command.append("--cached")
    command.extend(["-U0", "--", str(path)])
    return run_git_command(command, allow_failure=True).splitlines()


def get_status_line_for_path(path: Path) -> str | None:
    output = run_git_command(["git", "status", "--porcelain", "--", str(path)], allow_failure=True)
    lines = [each for each in output.splitlines() if each]
    return lines[0] if lines else None


def get_added_lines_for_path(path: Path) -> list[str]:
    result = []
    for cached in (False, True):
        result.extend(get_git_diff_lines(path, cached=cached))
    if result:
        return list(dict.fromkeys(result))
    status_line = get_status_line_for_path(path)
    if status_line and status_line.startswith(UNTRACKED_STATUS_PREFIX):
        return [f"+{each}" for each in path.read_text(encoding="utf-8").splitlines()]
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
    path_text = str(path)
    test_marker = "/src/test/java/"
    if test_marker not in path_text:
        return None
    return Path(path_text.replace(test_marker, "/src/main/java/")).with_name(f"{target_type_name}.java")


def load_target_public_methods(path: Path, source: str) -> set[str]:
    target_source_path = resolve_target_source_path(path, source)
    if target_source_path is None or not target_source_path.exists():
        return set()
    target_source = target_source_path.read_text(encoding="utf-8")
    return {match.group(1) for match in PUBLIC_METHOD_DECL_PATTERN.finditer(target_source)}


def get_after_status_lines() -> set[str]:
    output = run_git_command(["git", "status", "--porcelain"])
    return set(each for each in output.splitlines() if each)


def is_src_main_path(path: str) -> bool:
    return "/src/main/" in path or path.startswith("src/main/")


def normalize_status_path(line: str) -> str:
    path = line[3:].strip()
    if " -> " in path:
        path = path.split(" -> ", 1)[1].strip()
    return path


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


def analyze_parameterization_candidates(path: Path, source: str) -> list[CandidateSummary]:
    target_type_name = get_target_type_name(source)
    statistics = defaultdict(lambda: {"plain": 0, "parameterized": False})
    for match in TEST_METHOD_DECL_PATTERN.finditer(source):
        annotation_block = match.group(1)
        test_method_name = match.group(2)
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        invoked_methods = extract_invoked_methods(body)
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
                high_fit=plain_test_count >= 3,
            ))
    return result


def describe_candidate(candidate: dict) -> str:
    decision = "recommend refactor" if candidate["high_fit"] and not candidate["parameterized_present"] else "already parameterized" if candidate["parameterized_present"] else "observe"
    return f'{candidate["path"]}: method={candidate["method"]} plainTestCount={candidate["plain_test_count"]} parameterizedPresent={candidate["parameterized_present"]} decision={decision}'


def check_parameterized_name(path: Path, source: str) -> list[str]:
    violations = []
    token = "@ParameterizedTest"
    pos = 0
    while True:
        token_pos = source.find(token, pos)
        if token_pos < 0:
            break
        line = line_number(source, token_pos)
        cursor = token_pos + len(token)
        while cursor < len(source) and source[cursor].isspace():
            cursor += 1
        if cursor >= len(source) or "(" != source[cursor]:
            violations.append(f"{path}:{line}")
            pos = token_pos + len(token)
            continue
        depth = 1
        end = cursor + 1
        while end < len(source) and depth:
            if "(" == source[end]:
                depth += 1
            elif ")" == source[end]:
                depth -= 1
            end += 1
        if depth or not R15_NAME_PATTERN.search(source[cursor + 1:end - 1]):
            violations.append(f"{path}:{line}")
        pos = end
    return violations


def check_r15_a(candidates: list[CandidateSummary]) -> list[str]:
    result = []
    for each in candidates:
        if each.high_fit and not each.parameterized_present:
            result.append(f"{each.path}: method={each.method} nonParameterizedCount={each.plain_test_count}")
    return result


def check_r15_d(path: Path, source: str, method_bodies: dict[str, str]) -> list[str]:
    violations = []
    for match in PARAM_METHOD_PATTERN.finditer(source):
        annotation_block = match.group(1)
        method_name = match.group(2)
        line = line_number(source, match.start())
        providers = parse_method_sources(method_name, annotation_block)
        if not providers:
            violations.append(f"{path}:{line} method={method_name} missing @MethodSource")
            continue
        total_rows = 0
        unresolved = []
        for provider in providers:
            body = method_bodies.get(provider)
            if body is None:
                unresolved.append(provider)
                continue
            total_rows += len(re.findall(r"\b(?:Arguments\.of|arguments)\s*\(", body))
        if unresolved:
            violations.append(f"{path}:{line} method={method_name} unresolvedProviders={','.join(unresolved)}")
            continue
        if total_rows < 3:
            violations.append(f"{path}:{line} method={method_name} argumentsRows={total_rows}")
    return violations


def check_r15_e(path: Path, source: str) -> list[str]:
    violations = []
    for match in PARAM_METHOD_PATTERN.finditer(source):
        method_name = match.group(2)
        params = match.group(3).strip()
        line = line_number(source, match.start())
        if not params:
            violations.append(f"{path}:{line} method={method_name} missingParameters")
            continue
        first_param = params.split(",", 1)[0].strip()
        normalized = re.sub(r"\s+", " ", first_param)
        if "final String name" != normalized:
            violations.append(f"{path}:{line} method={method_name} firstParam={first_param}")
    return violations


def check_r15_f(path: Path, source: str) -> list[str]:
    violations = []
    for match in PARAM_METHOD_BODY_PATTERN.finditer(source):
        method_name = match.group(1)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        if R15_SWITCH_PATTERN.search(body):
            violations.append(f"{path}:{line} method={method_name}")
    return violations


def check_r15_g(path: Path, source: str) -> list[str]:
    if "@ParameterizedTest" not in source:
        return []
    top_level_class_name = get_top_level_class_name(source)
    violations = []
    for line in get_added_lines_for_path(path):
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


def check_r15_i(path: Path, source: str, method_bodies: dict[str, str]) -> list[str]:
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


def check_r14(path: Path, source: str) -> list[str]:
    return [f"{path}:{line_number(source, match.start())}" for match in BOOLEAN_ASSERTION_BAN_PATTERN.finditer(source)]


def check_r15_h(path: Path, source: str) -> list[str]:
    violations = []
    for match in TEST_METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(2)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        if R15_H_IF_ELSE_PATTERN.search(body) or R15_H_IF_RETURN_PATTERN.search(body):
            violations.append(f"{path}:{line} method={method_name}")
    return violations


def check_r15_b(path: Path, source: str) -> list[str]:
    return [f"{path}:{line_number(source, match.start())}" for match in R15_B_PATTERN.finditer(source)]


def checkstyle_preview_final_parameters(path: Path, source: str) -> list[str]:
    violations = []
    for match in TEST_METHOD_SIGNATURE_PATTERN.finditer(source):
        method_name = match.group(2)
        params = match.group(3).strip()
        if not params:
            continue
        line = line_number(source, match.start())
        for each in split_parameters(params):
            normalized = re.sub(r"\s+", " ", each.strip())
            if not normalized.startswith("final "):
                violations.append(f"{path}:{line} method={method_name} param={each.strip()}")
    return violations


def collect_precheck_violations(context: FileScanContext) -> dict[str, list[str]]:
    return {
        "checkstyle-final-parameters": checkstyle_preview_final_parameters(context.path, context.source),
        "parameterized-methodsource": check_r15_d(context.path, context.source, context.method_bodies),
        "parameterized-name-parameter": check_r15_e(context.path, context.source),
    }


def scan_java_file(path: Path, allow_metadata_accessor_tests: bool) -> tuple[dict[str, list[str]], list[CandidateSummary]]:
    source = path.read_text(encoding="utf-8")
    method_bodies = parse_method_bodies(source)
    violations = defaultdict(list)
    violations["R8"].extend(check_parameterized_name(path, source))
    violations["R15-A"].extend(check_r15_a(path, source))
    violations["R15-D"].extend(check_r15_d(path, source, method_bodies))
    violations["R15-E"].extend(check_r15_e(path, source))
    violations["R15-F"].extend(check_r15_f(path, source))
    violations["R15-G"].extend(check_r15_g(path, source))
    violations["R15-I"].extend(check_r15_i(path, source, method_bodies))
    violations["R14"].extend(check_r14(path, source))
    violations["R15-H"].extend(check_r15_h(path, source))
    if not allow_metadata_accessor_tests:
        violations["R15-B"].extend(check_r15_b(path, source))
    return violations, analyze_parameterization_candidates(path, source)


def check_r15_c(baseline_before: Path | None) -> list[str]:
    if baseline_before is None:
        return []
    before_lines = baseline_before.read_text(encoding="utf-8").splitlines() if baseline_before.exists() else []
    before_paths = {
        normalize_status_path(each) for each in before_lines
        if each and is_src_main_path(normalize_status_path(each))
    }
    after_paths = {
        normalize_status_path(each) for each in get_after_status_lines()
        if is_src_main_path(normalize_status_path(each))
    }
    return sorted(after_paths - before_paths)


def create_file_scan_context(path: Path) -> FileScanContext:
    source = path.read_text(encoding="utf-8")
    method_blocks = parse_method_blocks(source)
    return FileScanContext(
        path=path,
        source=source,
        method_bodies={name: body for name, (_, body) in method_blocks.items()},
        method_blocks=method_blocks,
        candidates=analyze_parameterization_candidates(path, source),
        target_type_name=get_target_type_name(source),
        target_public_methods=load_target_public_methods(path, source),
    )


def file_rule_violations(context: FileScanContext, allow_metadata_accessor_tests: bool) -> dict[str, list[str]]:
    violations = defaultdict(list)
    violations["R8"].extend(check_parameterized_name(context.path, context.source))
    violations["R14"].extend(check_r14(context.path, context.source))
    violations["R15-A"].extend(check_r15_a(context.candidates))
    violations["R15-D"].extend(check_r15_d(context.path, context.source, context.method_bodies))
    violations["R15-E"].extend(check_r15_e(context.path, context.source))
    violations["R15-F"].extend(check_r15_f(context.path, context.source))
    violations["R15-G"].extend(check_r15_g(context.path, context.source))
    violations["R15-H"].extend(check_r15_h(context.path, context.source))
    violations["R15-I"].extend(check_r15_i(context.path, context.source, context.method_bodies))
    violations["R15-J"].extend(check_r15_j(context))
    if not allow_metadata_accessor_tests:
        violations["R15-B"].extend(check_r15_b(context.path, context.source))
    return violations


def build_rule_result(violations_by_rule: dict[str, list[str]]) -> dict[str, dict[str, object]]:
    return {
        each.name: {
            "message": each.message,
            "violations": violations_by_rule[each.name],
        }
        for each in RULE_SPECS
    }


def collect_scan_result(java_paths: list[Path], baseline_before: Path | None, allow_metadata_accessor_tests: bool) -> dict:
    violations_by_rule = defaultdict(list)
    precheck_violations = defaultdict(list)
    contexts = [create_file_scan_context(each) for each in java_paths]
    for context in contexts:
        for rule, entries in file_rule_violations(context, allow_metadata_accessor_tests).items():
            violations_by_rule[rule].extend(entries)
        for name, entries in collect_precheck_violations(context).items():
            precheck_violations[name].extend(entries)
    violations_by_rule["R15-C"].extend(check_r15_c(baseline_before))
    return {
        "rules": build_rule_result(violations_by_rule),
        "prechecks": {
            name: {
                "message": PRECHECK_MESSAGES[name],
                "violations": precheck_violations[name],
            }
            for name in PRECHECK_ORDER
        },
        "candidates": [asdict(each) for context in contexts for each in context.candidates],
        "java_file_count": len(contexts),
    }


def failed_rule_names(result: dict) -> list[str]:
    return [each.name for each in RULE_SPECS if result["rules"][each.name]["violations"]]


def failed_precheck_names(result: dict) -> list[str]:
    prechecks = result.get("prechecks", {})
    return [each for each in PRECHECK_ORDER if prechecks.get(each, {}).get("violations")]


def print_rule_summary(result: dict) -> int:
    failed_rules = set(failed_rule_names(result))
    for each in RULE_SPECS:
        violations = result["rules"][each.name]["violations"]
        if each.name in failed_rules:
            print(f"[{each.name}] {each.message}")
            for violation in violations:
                print(violation)
            continue
        print(f"[{each.name}] ok")
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
    if failed_rules:
        print(f"[summary] violations={' '.join(failed_rules)}")
        return 1
    print("[summary] all rules ok")
    return 0


def print_precheck_summary(result: dict) -> int:
    prechecks = result.get("prechecks", {})
    failed = False
    for name in PRECHECK_ORDER:
        details = prechecks.get(name)
        if not details:
            continue
        violations = details["violations"]
        if not violations:
            print(f"[precheck:{name}] ok")
            continue
        failed = True
        print(f"[precheck:{name}] {details['message']}")
        for each in violations:
            print(each)
    return 1 if failed else 0


def main() -> int:
    parser = argparse.ArgumentParser(description="Consolidated quality-rule scan for gen-ut.")
    parser.add_argument("--baseline-before", help="Path to the baseline git status captured at task start.")
    parser.add_argument("--allow-metadata-accessor-tests", action="store_true", help="Allow R15-B when user explicitly requested metadata accessor tests.")
    parser.add_argument("--json", action="store_true", help="Emit JSON output instead of the default text report.")
    parser.add_argument("--summary-only", action="store_true", help="Emit a compact text summary with candidate information.")
    parser.add_argument("--precheck-only", action="store_true", help="Emit only lightweight deterministic prechecks for early edit loops.")
    parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    args = parser.parse_args()

    java_paths = [Path(each) for each in args.paths if each.endswith(".java")]
    baseline_path = Path(args.baseline_before) if args.baseline_before else None
    result = collect_scan_result(java_paths, baseline_path, args.allow_metadata_accessor_tests)
    if args.json:
        print(json.dumps(result, indent=2, sort_keys=True))
        return 1 if failed_rule_names(result) else 0
    if args.precheck_only:
        return print_precheck_summary(result)
    if args.summary_only:
        return print_summary_only(result)
    return print_rule_summary(result)


if __name__ == "__main__":
    sys.exit(main())
