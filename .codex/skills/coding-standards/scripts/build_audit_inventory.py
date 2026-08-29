#!/usr/bin/env python3
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

"""Build a deterministic, read-only scope inventory for the coding-standards skill."""

from __future__ import annotations

import argparse
import json
import os
import subprocess
import sys
import xml.etree.ElementTree as ET
from collections import Counter
from dataclasses import dataclass
from pathlib import Path
from typing import Sequence


IGNORED_POM_DIRECTORIES = {".agents", ".codex", ".git", "target"}


@dataclass(frozen=True)
class ModuleDeclaration:

    path: str
    source: str


@dataclass(frozen=True)
class PomData:

    artifact_id: str
    packaging: str
    declarations: tuple[ModuleDeclaration, ...]
    parse_error: str | None = None


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def direct_child(element: ET.Element, name: str) -> ET.Element | None:
    return next((each for each in element if local_name(each.tag) == name), None)


def direct_children(element: ET.Element | None, name: str) -> list[ET.Element]:
    return [] if element is None else [each for each in element if local_name(each.tag) == name]


def direct_text(element: ET.Element, name: str, default: str = "") -> str:
    child = direct_child(element, name)
    return default if child is None or child.text is None else child.text.strip()


def module_declarations(element: ET.Element, source: str) -> list[ModuleDeclaration]:
    modules = direct_child(element, "modules")
    result = []
    for each in direct_children(modules, "module"):
        if each.text and each.text.strip():
            result.append(ModuleDeclaration(each.text.strip(), source))
    return result


def parse_pom(pom_path: Path) -> PomData:
    try:
        root = ET.parse(pom_path).getroot()
    except (ET.ParseError, OSError) as ex:
        return PomData("", "", (), str(ex))
    declarations = module_declarations(root, "default")
    profiles = direct_child(root, "profiles")
    for profile in direct_children(profiles, "profile"):
        profile_id = direct_text(profile, "id", "<unnamed>")
        declarations.extend(module_declarations(profile, f"profile:{profile_id}"))
    return PomData(
        direct_text(root, "artifactId"),
        direct_text(root, "packaging", "jar"),
        tuple(declarations),
    )


def discover_poms(repo_root: Path) -> list[Path]:
    result = []
    for current_root, directory_names, file_names in os.walk(repo_root):
        directory_names[:] = sorted(each for each in directory_names if each not in IGNORED_POM_DIRECTORIES)
        if "pom.xml" in file_names:
            result.append(Path(current_root) / "pom.xml")
    return sorted(result, key=lambda each: relative_path(repo_root, each))


def discover_repository_files(repo_root: Path) -> list[Path]:
    try:
        completed = subprocess.run(
            ["git", "-C", str(repo_root), "ls-files", "-z", "--cached", "--others", "--exclude-standard"],
            check=False, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
        )
    except OSError as ex:
        raise ValueError("repository files cannot be listed by git") from ex
    if 0 != completed.returncode:
        raise ValueError("repository files cannot be listed by git")
    paths = [repo_root / Path(os.fsdecode(each)) for each in completed.stdout.split(b"\0") if each]
    return sorted((each for each in paths if each.is_file() or each.is_symlink()), key=lambda each: relative_path(repo_root, each))


def discover_scope_files(target: Path, repository_files: list[Path]) -> list[Path]:
    if target.is_file():
        return [target]
    return [each for each in repository_files if is_within(each, target)]


def relative_path(repo_root: Path, path: Path) -> str:
    relative = path.relative_to(repo_root)
    return "." if not relative.parts else relative.as_posix()


def module_path(repo_root: Path, pom_path: Path) -> str:
    return relative_path(repo_root, pom_path.parent)


def is_within(path: Path, parent: Path) -> bool:
    try:
        path.relative_to(parent)
        return True
    except ValueError:
        return False


def resolve_declared_pom(repo_root: Path, owner_pom: Path, declaration: ModuleDeclaration) -> tuple[Path | None, str | None]:
    if "${" in declaration.path:
        return None, "unresolved_module_path"
    candidate = (owner_pom.parent / declaration.path).resolve()
    if not is_within(candidate, repo_root):
        return None, "outside_repository_module"
    candidate_pom = candidate if "pom.xml" == candidate.name else candidate / "pom.xml"
    return (candidate_pom, None) if candidate_pom.is_file() else (None, "missing_module_pom")


def service_files(repo_root: Path, module_dir: Path, source_set: str) -> list[str]:
    service_dir = module_dir / "src" / source_set / "resources" / "META-INF" / "services"
    if not service_dir.is_dir():
        return []
    return sorted(relative_path(repo_root, each) for each in service_dir.rglob("*") if each.is_file())


def source_set(relative: str) -> str:
    parts = Path(relative).parts
    if 2 <= len(parts) and parts[:2] == (".github", "workflows"):
        return "workflow"
    for index in range(len(parts) - 1):
        if "src" == parts[index] and "main" == parts[index + 1]:
            return "production"
        if "src" == parts[index] and "test" == parts[index + 1]:
            return "test"
    return "repository"


def inspect_file_content(path: Path) -> tuple[int | None, str]:
    if path.is_symlink():
        return None, "symlink"
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        return None, "non_utf8_or_binary"
    except OSError:
        return None, "unreadable"
    return len(text.splitlines()), "utf8_text"


def file_record(repo_root: Path, path: Path) -> dict[str, object]:
    relative = relative_path(repo_root, path)
    line_count, content_kind = inspect_file_content(path)
    try:
        size_bytes = path.lstat().st_size
    except OSError:
        size_bytes = None
    return {
        "path": relative,
        "extension": path.suffix.lower() or None,
        "source_set": source_set(relative),
        "size_bytes": size_bytes,
        "content_kind": content_kind,
        "line_count": line_count,
    }


def build_graph(repo_root: Path, pom_paths: list[Path]) -> tuple[dict[str, PomData], dict[str, list[dict[str, str]]], dict[str, list[str]], list[dict[str, str]]]:
    pom_by_path = {module_path(repo_root, each): each for each in pom_paths}
    data_by_path = {path: parse_pom(pom) for path, pom in pom_by_path.items()}
    children_by_path = {path: [] for path in pom_by_path}
    parents_by_path = {path: [] for path in pom_by_path}
    issues = []
    for owner_path, data in data_by_path.items():
        if data.parse_error:
            issues.append({"type": "invalid_pom", "pom": relative_path(repo_root, pom_by_path[owner_path]), "detail": data.parse_error})
            continue
        for declaration in data.declarations:
            child_pom, issue_type = resolve_declared_pom(repo_root, pom_by_path[owner_path], declaration)
            if issue_type:
                issues.append({"type": issue_type, "pom": relative_path(repo_root, pom_by_path[owner_path]),
                               "module": declaration.path, "source": declaration.source})
                continue
            child_path = module_path(repo_root, child_pom)
            if child_path not in parents_by_path:
                issues.append({"type": "undiscovered_module_pom", "pom": relative_path(repo_root, pom_by_path[owner_path]),
                               "module": declaration.path, "source": declaration.source})
                continue
            children_by_path[owner_path].append({"path": child_path, "source": declaration.source})
            parents_by_path[child_path].append(owner_path)
    for children in children_by_path.values():
        children.sort(key=lambda each: (each["path"], each["source"]))
    for parents in parents_by_path.values():
        parents[:] = sorted(set(parents))
    issues.sort(key=lambda each: tuple(str(each.get(key, "")) for key in ("pom", "type", "module", "source", "detail")))
    return data_by_path, children_by_path, parents_by_path, issues


def find_owner_module(repo_root: Path, target: Path, module_paths: set[str]) -> str:
    current = target.parent if target.is_file() else target
    while is_within(current, repo_root):
        candidate = relative_path(repo_root, current)
        if candidate in module_paths:
            return candidate
        if current == repo_root:
            break
        current = current.parent
    raise ValueError(f"scope has no owning Maven module: {relative_path(repo_root, target)}")


def descendants(module: str, children_by_path: dict[str, list[dict[str, str]]]) -> set[str]:
    result = {module}
    pending = [module]
    while pending:
        current = pending.pop()
        for child in children_by_path.get(current, []):
            child_path = child["path"]
            if child_path not in result:
                result.add(child_path)
                pending.append(child_path)
    return result


def resolve_scope(repo_root: Path, raw_scope: str, module_paths: set[str], children_by_path: dict[str, list[dict[str, str]]]) -> tuple[dict[str, object], set[str]]:
    scope_path = Path(raw_scope)
    if scope_path.is_absolute() or ".." in scope_path.parts:
        raise ValueError(f"scope must be a repository-relative path: {raw_scope}")
    target = repo_root / scope_path
    if not is_within(target.resolve(), repo_root):
        raise ValueError(f"scope escapes repository: {raw_scope}")
    if not target.exists() and not target.is_symlink():
        raise ValueError(f"scope does not exist: {raw_scope}")
    owner = find_owner_module(repo_root, target, module_paths)
    owner_dir = repo_root if "." == owner else repo_root / owner
    is_repository = target == repo_root
    is_module = target == owner_dir
    selected = set(module_paths) if is_repository else descendants(owner, children_by_path) if is_module else {owner}
    kind = "repository" if is_repository else "module" if is_module else "file" if target.is_file() else "path"
    return {
        "path": relative_path(repo_root, target),
        "kind": kind,
        "owner_module": owner,
        "selected_modules": sorted(selected),
    }, selected


def module_record(repo_root: Path, path: str, data: PomData, children: list[dict[str, str]], parents: list[str]) -> dict[str, object]:
    module_dir = repo_root if "." == path else repo_root / path
    return {
        "path": path,
        "pom": relative_path(repo_root, module_dir / "pom.xml"),
        "artifact_id": data.artifact_id,
        "packaging": data.packaging,
        "parents": parents,
        "children": children,
        "is_leaf": not children,
        "has_main_java": (module_dir / "src" / "main" / "java").is_dir(),
        "has_test_java": (module_dir / "src" / "test" / "java").is_dir(),
        "main_service_files": service_files(repo_root, module_dir, "main"),
        "test_service_files": service_files(repo_root, module_dir, "test"),
        "parse_error": data.parse_error,
    }


def build_inventory(repo_root: Path, raw_scopes: Sequence[str]) -> dict[str, object]:
    resolved_root = repo_root.resolve()
    if not (resolved_root / "pom.xml").is_file():
        raise ValueError(f"repository root has no pom.xml: {repo_root}")
    if not raw_scopes:
        raise ValueError("at least one --scope is required")
    pom_paths = discover_poms(resolved_root)
    repository_files = discover_repository_files(resolved_root)
    data_by_path, children_by_path, parents_by_path, issues = build_graph(resolved_root, pom_paths)
    scope_records = []
    selected_modules = set()
    selected_files = set()
    for raw_scope in raw_scopes:
        scope_record, selected = resolve_scope(resolved_root, raw_scope, set(data_by_path), children_by_path)
        scope_target = resolved_root if "." == scope_record["path"] else resolved_root / str(scope_record["path"])
        scope_files = discover_scope_files(scope_target, repository_files)
        scope_record["file_count"] = len(scope_files)
        scope_records.append(scope_record)
        selected_modules.update(selected)
        selected_files.update(scope_files)
    selected_issues = [each for each in issues if module_path(resolved_root, resolved_root / each["pom"]) in selected_modules]
    modules = [module_record(resolved_root, each, data_by_path[each], children_by_path[each], parents_by_path[each])
               for each in sorted(selected_modules)]
    files = [file_record(resolved_root, each) for each in sorted(selected_files, key=lambda each: relative_path(resolved_root, each))]
    extension_counts = Counter(each["extension"] or "<none>" for each in files)
    unreferenced_poms = [each for each in sorted(selected_modules) if "." != each and not parents_by_path[each]]
    return {
        "version": 2,
        "scopes": scope_records,
        "discovered_module_count": len(data_by_path),
        "selected_module_count": len(modules),
        "leaf_module_count": sum(1 for each in modules if each["is_leaf"]),
        "file_count": len(files),
        "text_file_count": sum(1 for each in files if each["line_count"] is not None),
        "text_line_count": sum(int(each["line_count"]) for each in files if each["line_count"] is not None),
        "file_type_counts": dict(sorted(extension_counts.items())),
        "unreferenced_poms": unreferenced_poms,
        "issues": selected_issues,
        "modules": modules,
        "files": files,
    }


def parse_args(argv: Sequence[str] | None = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build a deterministic, read-only coding-standards scope inventory.")
    parser.add_argument("--repo-root", default=".", help="Repository root containing pom.xml")
    parser.add_argument("--scope", action="append", required=True, help="Repository-relative file, package directory, or Maven module path")
    return parser.parse_args(argv)


def main(argv: Sequence[str] | None = None) -> int:
    args = parse_args(argv)
    try:
        inventory = build_inventory(Path(args.repo_root), args.scope)
    except (OSError, ValueError) as ex:
        print(f"error: {ex}", file=sys.stderr)
        return 1
    print(json.dumps(inventory, ensure_ascii=False, indent=2, sort_keys=True))
    return 0


if __name__ == "__main__":
    sys.exit(main())
