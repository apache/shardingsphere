#!/usr/bin/env python3
"""
Consolidated hard-gate scan for gen-ut.
"""

import argparse
import re
import subprocess
import sys
from collections import defaultdict
from pathlib import Path


RULE_ORDER = ("R8", "R14", "R15-A", "R15-B", "R15-C", "R15-D", "R15-E", "R15-F", "R15-G", "R15-H", "R15-I")
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
}
BOOLEAN_ASSERTION_BAN_PATTERN = re.compile(
    r"assertThat\s*\((?s:.*?)is\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)\s*\)"
    r"|assertEquals\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*,"
    r"|assertEquals\s*\((?s:.*?),\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)",
    re.S,
)
CONSUMER_TOKEN_PATTERN = re.compile(r"\bConsumer\s*(?:<|\b)")
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
UNTRACKED_STATUS_PREFIX = "?? "


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


def parse_method_sources(method_name: str, annotation_block: str) -> list[str]:
    resolved = []
    matches = list(METHOD_SOURCE_PATTERN.finditer(annotation_block))
    if not matches:
        return resolved
    for each in matches:
        raw = each.group(1)
        if raw is None or not raw.strip():
            resolved.append(method_name)
            continue
        normalized = re.sub(r"\bvalue\s*=\s*", "", raw.strip())
        for name in re.findall(r'"([^"]+)"', normalized):
            resolved.append(name.split("#", 1)[-1])
    return resolved


def parse_method_bodies(source: str) -> dict[str, str]:
    result = {}
    for match in METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(1)
        brace_index = opening_brace_index(match)
        if brace_index >= 0:
            result[method_name] = extract_block(source, brace_index)
    return result


def run_git_command(args: list[str]) -> str:
    return subprocess.run(args, check=True, capture_output=True, text=True).stdout


def get_git_diff_lines(path: Path, *, cached: bool = False) -> list[str]:
    command = ["git", "diff"]
    if cached:
        command.append("--cached")
    command.extend(["-U0", "--", str(path)])
    return run_git_command(command).splitlines()


def get_status_line_for_path(path: Path) -> str | None:
    output = run_git_command(["git", "status", "--porcelain", "--", str(path)])
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


def get_after_status_lines() -> set[str]:
    output = run_git_command(["git", "status", "--porcelain"])
    return set(each for each in output.splitlines() if each)


def normalize_status_path(line: str) -> str:
    path = line[3:].strip()
    if " -> " in path:
        path = path.split(" -> ", 1)[1].strip()
    return path


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


def check_r15_a(path: Path, source: str) -> list[str]:
    param_targets = set()
    plain_target_count = defaultdict(int)
    for match in TEST_METHOD_DECL_PATTERN.finditer(source):
        annotation_block = match.group(1)
        brace_index = opening_brace_index(match)
        body = extract_block(source, brace_index)
        methods = [each for each in R15_A_CALL_PATTERN.findall(body) if each not in R15_A_IGNORE]
        if not methods:
            continue
        target = methods[0]
        if "@ParameterizedTest" in annotation_block:
            param_targets.add(target)
        else:
            plain_target_count[target] += 1
    return [f"{path}: method={method_name} nonParameterizedCount={count}"
            for method_name, count in plain_target_count.items() if count >= 3 and method_name not in param_targets]


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


def scan_java_file(path: Path, allow_metadata_accessor_tests: bool) -> dict[str, list[str]]:
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
    return violations


def print_rule_summary(violations_by_rule: dict[str, list[str]]) -> int:
    failed = False
    for rule in RULE_ORDER:
        violations = violations_by_rule[rule]
        if violations:
            failed = True
            print(f"[{rule}] {RULE_MESSAGES[rule]}")
            for each in violations:
                print(each)
        else:
            print(f"[{rule}] ok")
    return 1 if failed else 0


def check_r15_c(baseline_before: Path | None) -> list[str]:
    if baseline_before is None:
        return []
    before = set(baseline_before.read_text(encoding="utf-8").splitlines()) if baseline_before.exists() else set()
    after = get_after_status_lines()
    violations = []
    for each in sorted(after - before):
        path = normalize_status_path(each)
        if "/src/main/" in path or path.startswith("src/main/"):
            violations.append(path)
    return violations


def main() -> int:
    parser = argparse.ArgumentParser(description="Consolidated hard-gate scan for gen-ut.")
    parser.add_argument("--baseline-before", help="Path to the baseline git status captured at task start.")
    parser.add_argument("--allow-metadata-accessor-tests", action="store_true", help="Allow R15-B when user explicitly requested metadata accessor tests.")
    parser.add_argument("paths", nargs="+", help="Resolved test file set.")
    args = parser.parse_args()

    java_paths = [Path(each) for each in args.paths if each.endswith(".java")]
    violations_by_rule = defaultdict(list)
    for path in java_paths:
        file_violations = scan_java_file(path, args.allow_metadata_accessor_tests)
        for rule, entries in file_violations.items():
            violations_by_rule[rule].extend(entries)
    baseline_path = Path(args.baseline_before) if args.baseline_before else None
    violations_by_rule["R15-C"].extend(check_r15_c(baseline_path))
    return print_rule_summary(violations_by_rule)


if __name__ == "__main__":
    sys.exit(main())
