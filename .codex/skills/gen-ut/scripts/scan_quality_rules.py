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
import difflib
import hashlib
import json
import os
import re
import stat
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
    "R15-A": "parameterization suitability requires semantic evidence",
    "R15-B": "metadata accessor candidates require request-scope review",
    "R15-C": "out-of-scope worktree mutation detected",
    "R15-D": "each @ParameterizedTest must have >= 3 Arguments rows from @MethodSource",
    "R15-E": "each @ParameterizedTest method must declare first parameter as `final String name`",
    "R15-F": "@ParameterizedTest method body must not contain switch",
    "R15-G": "parameterized tests must not introduce nested helper type declarations",
    "R15-H": "do not dispatch boolean assertions by control flow to choose assertTrue/assertFalse",
    "R15-I": "parameterized tests must not use Consumer in signatures or @MethodSource argument rows",
    "R15-J": "helper and provider target invocations require semantic ownership review",
}
RULE_MODES = {
    "R15-A": "manual",
    "R15-B": "manual",
    "R15-D": "hybrid",
    "R15-I": "hybrid",
    "R15-J": "hybrid",
}
RULE_SPECS = tuple(RuleSpec(each, RULE_MESSAGES[each], RULE_MODES.get(each, "automated")) for each in RULE_ORDER)
BOOLEAN_ASSERTION_BAN_PATTERN = re.compile(
    r"assertThat\s*\((?s:[^;]*?)is\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)\s*\)"
    r"|assertEquals\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*,"
    r"|assertEquals\s*\((?s:[^;]*?),\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)",
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
TYPE_DECL_LINE_PATTERN = re.compile(
    r"^\s*(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed)\s+)*(class|interface|enum|record)\s+(\w+)\b"
)
PUBLIC_METHOD_DECL_PATTERN = re.compile(
    r"^\s*public\s+(?:default\s+)?(?:static\s+)?(?:final\s+)?[\w$<>\[\], ?]+\s+(\w+)\s*\(",
    re.M,
)
CONSTRUCTOR_TEST_PREFIXES = ("New", "Construct", "Constructor")
TEST_SCOPE_MARKERS = ("src/test/java/", "src/test/resources/")


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


def parse_method_blocks(source: str) -> dict[str, tuple[int, str]]:
    result = {}
    for match in METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(1)
        line = line_number(source, match.start())
        brace_index = opening_brace_index(match)
        if brace_index >= 0:
            result[method_name] = (line, extract_block(source, brace_index))
    return result


def run_git_command(args: list[str], *, cwd: Path | None = None, allow_failure: bool = False) -> str:
    try:
        return subprocess.run(args, cwd=cwd, check=True, capture_output=True, text=True).stdout
    except subprocess.CalledProcessError:
        if allow_failure:
            return ""
        raise


def get_repo_root(path: Path | None = None) -> Path:
    output = run_git_command(["git", "rev-parse", "--show-toplevel"], cwd=path)
    return Path(output.strip()).resolve()


def get_head(repo_root: Path) -> str | None:
    result = run_git_command(["git", "rev-parse", "--verify", "HEAD"], cwd=repo_root, allow_failure=True).strip()
    return result or None


def normalize_repo_paths(repo_root: Path, paths: list[Path]) -> list[str]:
    result = []
    for each in paths:
        resolved = (repo_root / each).resolve() if not each.is_absolute() else each.resolve()
        try:
            relative = resolved.relative_to(repo_root)
        except ValueError as ex:
            raise ValueError(f"path is outside repository: {resolved}") from ex
        result.append(relative.as_posix())
    return sorted(set(result))


def validate_test_scope_paths(paths: list[str]) -> None:
    invalid = [each for each in paths if not any(marker in each for marker in TEST_SCOPE_MARKERS)]
    if invalid:
        raise ValueError(f"path is outside test scope: {invalid[0]}")


def fingerprint_path(path: Path) -> str:
    digest = hashlib.sha256()
    if path.is_symlink():
        digest.update(b"symlink\0")
        digest.update(str(path.readlink()).encode("utf-8", errors="surrogateescape"))
        return digest.hexdigest()
    if not path.exists():
        return "missing"
    if path.is_dir():
        return "directory"
    digest.update(b"file\0")
    digest.update(str(stat.S_IMODE(path.stat().st_mode)).encode("ascii"))
    digest.update(b"\0")
    with path.open("rb") as input_file:
        while chunk := input_file.read(64 * 1024):
            digest.update(chunk)
    return digest.hexdigest()


def parse_status_entries(raw: bytes) -> dict[str, str]:
    records = raw.split(b"\0")
    result = {}
    index = 0
    while index < len(records):
        record = records[index]
        index += 1
        if not record:
            continue
        if len(record) < 4 or b" " != record[2:3]:
            raise ValueError("unexpected git status --porcelain output")
        status = record[:2].decode("ascii")
        path = record[3:].decode("utf-8", errors="surrogateescape")
        result[path] = status
        if status[0] in "RC" or status[1] in "RC":
            if index >= len(records) or not records[index]:
                raise ValueError("incomplete rename entry in git status output")
            old_path = records[index].decode("utf-8", errors="surrogateescape")
            result[old_path] = "rename-source"
            index += 1
    return result


def collect_worktree_entries(repo_root: Path, allowed_paths: set[str]) -> dict[str, dict[str, str]]:
    completed = subprocess.run(
        ["git", "status", "--porcelain=v1", "-z", "--untracked-files=all", "--ignored=traditional"],
        cwd=repo_root, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
    )
    return {
        path: {"status": status, "fingerprint": fingerprint_path(repo_root / path)}
        for path, status in parse_status_entries(completed.stdout).items()
        if path not in allowed_paths
    }


def build_scope_snapshot(repo_root: Path, allowed_paths: list[Path]) -> dict:
    normalized_allowed = normalize_repo_paths(repo_root, allowed_paths)
    validate_test_scope_paths(normalized_allowed)
    return {
        "version": 1,
        "repo_root": str(repo_root),
        "head": get_head(repo_root),
        "allowed_paths": normalized_allowed,
        "allowed_contents": {
            each: (repo_root / each).read_text(encoding="utf-8") if each.endswith(".java") and (repo_root / each).is_file() else None
            for each in normalized_allowed
        },
        "outside_entries": collect_worktree_entries(repo_root, set(normalized_allowed)),
    }


def write_scope_baseline(output_path: Path, repo_root: Path, allowed_paths: list[Path]) -> None:
    repo_root = repo_root.resolve()
    output_path = output_path.resolve()
    try:
        output_path.relative_to(repo_root)
    except ValueError:
        pass
    else:
        raise ValueError("scope baseline must be stored outside the repository")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    payload = json.dumps(build_scope_snapshot(repo_root, allowed_paths), indent=2, sort_keys=True) + "\n"
    descriptor = os.open(output_path, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
    with os.fdopen(descriptor, "w", encoding="utf-8") as output_file:
        output_file.write(payload)


def load_scope_baseline(baseline_path: Path) -> dict:
    result = json.loads(baseline_path.read_text(encoding="utf-8"))
    if not isinstance(result, dict) or 1 != result.get("version"):
        raise ValueError(f"invalid scope baseline: {baseline_path}")
    expected_types = {
        "repo_root": str,
        "allowed_paths": list,
        "allowed_contents": dict,
        "outside_entries": dict,
    }
    if any(not isinstance(result.get(key), expected_type) for key, expected_type in expected_types.items()):
        raise ValueError(f"invalid scope baseline: {baseline_path}")
    if result.get("head") is not None and not isinstance(result.get("head"), str):
        raise ValueError(f"invalid scope baseline: {baseline_path}")
    return result


def check_scope_baseline(baseline_path: Path, allowed_paths: list[Path]) -> list[str]:
    if not baseline_path.exists():
        return [f"missing scope baseline: {baseline_path}"]
    baseline = load_scope_baseline(baseline_path)
    repo_root = Path(baseline["repo_root"])
    if get_head(repo_root) != baseline.get("head"):
        return ["repository HEAD differs from scope baseline"]
    normalized_allowed = normalize_repo_paths(repo_root, allowed_paths)
    if normalized_allowed != baseline.get("allowed_paths"):
        return ["resolved test file set differs from scope baseline"]
    current = collect_worktree_entries(repo_root, set(normalized_allowed))
    before = baseline.get("outside_entries", {})
    return sorted(path for path in set(before) | set(current) if before.get(path) != current.get(path))


def get_added_lines_for_path(path: Path, scope_baseline: dict | None = None) -> list[str]:
    if scope_baseline is None:
        return []
    repo_root = Path(scope_baseline["repo_root"])
    relative_path = normalize_repo_paths(repo_root, [path])[0]
    before = scope_baseline.get("allowed_contents", {}).get(relative_path) or ""
    after = path.read_text(encoding="utf-8") if path.exists() else ""
    return [f"+{each[2:]}" for each in difflib.ndiff(before.splitlines(), after.splitlines()) if each.startswith("+ ")]


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
    decision = "already parameterized" if candidate["parameterized_present"] else "manual-review-required"
    return f'{candidate["path"]}: method={candidate["method"]} plainTestCount={candidate["plain_test_count"]} parameterizedPresent={candidate["parameterized_present"]} decision={decision}'


def check_parameterized_name(path: Path, source: str) -> list[str]:
    source = mask_java_non_code(source, False)
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
        for provider in providers:
            body = method_bodies.get(provider)
            if body is None:
                continue
            total_rows += len(re.findall(r"\b(?:Arguments\.of|arguments)\s*\(", body))
        if any(provider not in method_bodies for provider in providers):
            continue
        if total_rows < 3:
            violations.append(f"{path}:{line} method={method_name} argumentsRows={total_rows}")
    return violations


def find_unresolved_method_sources(path: Path, source: str, method_bodies: dict[str, str]) -> list[str]:
    result = []
    for match in PARAM_METHOD_PATTERN.finditer(source):
        method_name = match.group(2)
        providers = parse_method_sources(method_name, match.group(1))
        unresolved = [each for each in providers if each not in method_bodies]
        if unresolved:
            result.append(f"{path}:{line_number(source, match.start())} method={method_name} unresolvedProviders={','.join(unresolved)}")
    return result


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
    source = mask_java_non_code(source, True)
    return [f"{path}:{line_number(source, match.start())}" for match in BOOLEAN_ASSERTION_BAN_PATTERN.finditer(source)]


def check_r15_h(path: Path, source: str) -> list[str]:
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


def find_metadata_accessor_test_candidates(path: Path, source: str) -> list[str]:
    source = mask_java_non_code(source, False)
    result = []
    for match in TEST_METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(2)
        if any(method_name.startswith(f"assert{each}") for each in ("GetType", "GetOrder", "GetTypeClass")):
            result.append(f"{path}:{line_number(source, match.start())} method={method_name}")
    return result


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


def file_rule_violations(context: FileScanContext) -> dict[str, list[str]]:
    violations = defaultdict(list)
    violations["R8"].extend(check_parameterized_name(context.path, context.source))
    violations["R14"].extend(check_r14(context.path, context.source))
    violations["R15-D"].extend(check_r15_d(context.path, context.source, context.method_bodies))
    violations["R15-E"].extend(check_r15_e(context.path, context.source))
    violations["R15-F"].extend(check_r15_f(context.path, context.source))
    violations["R15-G"].extend(check_r15_g(context.path, context.source, context.added_lines))
    violations["R15-H"].extend(check_r15_h(context.path, context.source))
    violations["R15-I"].extend(check_r15_i(context.path, context.source, context.method_bodies))
    violations["R15-J"].extend(check_r15_j(context))
    return violations


def build_rule_result(violations_by_rule: dict[str, list[str]]) -> dict[str, dict[str, object]]:
    return {
        each.name: {
            "message": each.message,
            "mode": each.mode,
            "violations": violations_by_rule[each.name],
        }
        for each in RULE_SPECS
    }


def collect_scan_result(paths: list[Path], scope_baseline_path: Path | None) -> dict:
    violations_by_rule = defaultdict(list)
    precheck_violations = defaultdict(list)
    java_paths = [each for each in paths if each.suffix == ".java"]
    scope_baseline = load_scope_baseline(scope_baseline_path) if scope_baseline_path and scope_baseline_path.exists() else None
    contexts = [create_file_scan_context(each, scope_baseline) for each in java_paths]
    for context in contexts:
        for rule, entries in file_rule_violations(context).items():
            violations_by_rule[rule].extend(entries)
        for name, entries in collect_precheck_violations(context).items():
            precheck_violations[name].extend(entries)
    violations_by_rule["R15-C"].extend(
        check_scope_baseline(scope_baseline_path, paths) if scope_baseline_path else ["missing scope baseline"]
    )
    unresolved_sources = [
        each
        for context in contexts
        for each in find_unresolved_method_sources(context.path, context.source, context.method_bodies)
    ]
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
        "reviews": {
            "R15-A": [str(context.path) for context in contexts if not context.target_public_methods],
            "R15-B": [each for context in contexts for each in find_metadata_accessor_test_candidates(context.path, context.source)],
            "R15-D": unresolved_sources,
            "R15-I": unresolved_sources,
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
        elif "manual" == each.mode:
            print(f"[{each.name}] semanticReviewRequired=true")
        else:
            print(f"[{each.name}] mechanical=ok semanticReviewRequired=true")
        for review in result.get("reviews", {}).get(each.name, []):
            print(f"review: {review}")
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
    manual_rules = ",".join(each.name for each in RULE_SPECS if "automated" != each.mode)
    print(f"[summary] semanticReviewRequired={manual_rules}")
    if failed_rules:
        print(f"[summary] violations={' '.join(failed_rules)}")
        return 1
    print("[summary] all mechanical rules ok")
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
    parser = argparse.ArgumentParser(description="Mechanical quality-rule scan and task-scope guard for gen-ut.")
    parser.add_argument("--scope-baseline", help="Structured task scope baseline created before editing.")
    parser.add_argument("--capture-scope-baseline", help="Write a structured task scope baseline to this path, then exit.")
    parser.add_argument("--json", action="store_true", help="Emit JSON output instead of the default text report.")
    parser.add_argument("--summary-only", action="store_true", help="Emit a compact text summary with candidate information.")
    parser.add_argument("--precheck-only", action="store_true", help="Emit only lightweight deterministic prechecks for early edit loops.")
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
        if args.precheck_only:
            return print_precheck_summary(result)
        if args.summary_only:
            return print_summary_only(result)
        return print_rule_summary(result)
    except (OSError, ValueError, json.JSONDecodeError, subprocess.CalledProcessError) as ex:
        print(ex, file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
