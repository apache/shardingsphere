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

"""Prepare and validate MCP server.json for publication."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from urllib.parse import urlparse


SCHEMA_URL = "https://static.modelcontextprotocol.io/schemas/2025-12-11/server.schema.json"
SERVER_NAME = "io.github.apache/shardingsphere-mcp"
OCI_IDENTIFIER_PATTERN = re.compile(r"^ghcr\.io/apache/shardingsphere-mcp:[^:\s]+$")
VERSION_RANGE_PATTERN = re.compile(r"^(?:[\^~]|[<>]=?|.*(?:\*|\.x|\s-\s|\|\|).*)")
SUPPORTED_TRANSPORTS = {"stdio", "streamable-http"}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Prepare ShardingSphere MCP server.json for publication.")
    parser.add_argument("--path", default="mcp/server.json", help="Path to server.json.")
    parser.add_argument("--version", help="Published server version.")
    parser.add_argument("--identifier", help="OCI identifier to publish.")
    parser.add_argument("--validate-only", action="store_true", help="Validate server.json without rewriting it.")
    parser.add_argument("--allow-snapshot", action="store_true", help="Allow SNAPSHOT values for development metadata validation.")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    path = Path(args.path)
    server = json.loads(path.read_text())
    if not args.validate_only:
        if not args.version or not args.identifier:
            raise ValueError("--version and --identifier are required unless --validate-only is set.")
        prepare_server_json(server, args.version, args.identifier)
    validate_server_json(server, args.allow_snapshot)
    if not args.validate_only:
        path.write_text(json.dumps(server, indent=2) + "\n")


def prepare_server_json(server: dict, version: str, identifier: str) -> None:
    server["version"] = version
    for package in server["packages"]:
        package["identifier"] = identifier
        package["version"] = version


def validate_server_json(server: dict, allow_snapshot: bool) -> None:
    require(SCHEMA_URL == server.get("$schema"), "server.json must use the official MCP Registry schema.")
    require(SERVER_NAME == server.get("name"), "server.json name must match the published ShardingSphere MCP server name.")
    require_string(server, "description", 100)
    require_string(server, "version", 255)
    validate_version("server version", server["version"], allow_snapshot)
    packages = server.get("packages")
    require(isinstance(packages, list) and packages, "server.json packages must be a non-empty array.")
    transport_types = {validate_package(each, server["version"], allow_snapshot) for each in packages}
    require(SUPPORTED_TRANSPORTS == transport_types, "server.json packages must expose stdio and streamable-http transports.")


def validate_package(package: dict, server_version: str, allow_snapshot: bool) -> str:
    require(isinstance(package, dict), "MCP Registry package must be an object.")
    require("oci" == package.get("registryType"), "MCP Registry package registryType must be oci.")
    identifier = package.get("identifier")
    require(isinstance(identifier, str) and OCI_IDENTIFIER_PATTERN.match(identifier), "OCI identifier must target ghcr.io/apache/shardingsphere-mcp:<tag>.")
    validate_version("package identifier", identifier, allow_snapshot)
    require(identifier.endswith(f":{server_version}"), "OCI identifier tag must match the server version.")
    if "version" in package:
        validate_version("package version", package["version"], allow_snapshot)
        require(server_version == package["version"], "MCP Registry package version must match the server version.")
    transport = package.get("transport")
    require(isinstance(transport, dict), "MCP Registry package transport must be an object.")
    transport_type = transport.get("type")
    require(transport_type in SUPPORTED_TRANSPORTS, "MCP Registry package transport type must be stdio or streamable-http.")
    if "streamable-http" == transport_type:
        require_http_url(transport.get("url"))
    require_environment_variable(package, "SHARDINGSPHERE_MCP_TRANSPORT")
    require_environment_variable(package, "SHARDINGSPHERE_MCP_CONFIG")
    return transport_type


def require_string(target: dict, key: str, max_length: int) -> None:
    value = target.get(key)
    require(isinstance(value, str) and value, f"server.json field {key} must be a non-empty string.")
    require(len(value) <= max_length, f"server.json field {key} must be at most {max_length} characters.")


def validate_version(label: str, value: str, allow_snapshot: bool) -> None:
    require(isinstance(value, str) and value, f"{label} must be a non-empty string.")
    require("latest" != value, f"{label} must not use latest.")
    require(not VERSION_RANGE_PATTERN.match(value), f"{label} must be a specific version, not a range.")
    if not allow_snapshot:
        require("SNAPSHOT" not in value, f"{label} must not contain SNAPSHOT for publication.")


def require_http_url(value: object) -> None:
    require(isinstance(value, str) and value, "streamable-http transport must define a URL.")
    parsed = urlparse(value)
    require(parsed.scheme in ("http", "https") and bool(parsed.netloc), "streamable-http transport URL must be an HTTP URL.")


def require_environment_variable(package: dict, name: str) -> None:
    env_vars = package.get("environmentVariables")
    require(isinstance(env_vars, list), f"MCP Registry package must define {name}.")
    require(any(name == each.get("name") for each in env_vars if isinstance(each, dict)), f"MCP Registry package must define {name}.")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise ValueError(message)


if __name__ == "__main__":
    try:
        main()
    except ValueError as ex:
        raise SystemExit(str(ex)) from ex
