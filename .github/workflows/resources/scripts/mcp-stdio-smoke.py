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

"""Smoke test the published MCP container in stdio mode."""

from __future__ import annotations

import argparse
import json
import os
import select
import subprocess
import sys
from typing import Any

REQUEST_TIMEOUT_SECONDS = 15
PROCESS_TIMEOUT_SECONDS = 5
PROTOCOL_VERSION = "2025-11-25"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run a stdio smoke test against an MCP Docker image.")
    parser.add_argument(
        "--mode",
        choices=("docker", "command"),
        default="docker",
        help="Launch mode. Use docker for a container image or command for a local process.",
    )
    parser.add_argument("launcher", nargs="+", help="Docker image name or command to run.")
    return parser.parse_args()


def write_request(process: subprocess.Popen[str], payload: dict[str, Any]) -> None:
    assert process.stdin is not None
    process.stdin.write(json.dumps(payload) + "\n")
    process.stdin.flush()


def read_response(process: subprocess.Popen[str], request_id: str) -> dict[str, Any]:
    assert process.stdout is not None
    while True:
        ready, _, _ = select.select([process.stdout], [], [], REQUEST_TIMEOUT_SECONDS)
        if not ready:
            raise AssertionError(f"STDIO smoke failed. Timed out waiting for request {request_id}.")
        line = process.stdout.readline()
        if not line:
            raise AssertionError(f"STDIO smoke failed. No response for request {request_id}.")
        if not line.strip():
            continue
        response = json.loads(line)
        if str(response.get("id")) != request_id:
            continue
        error = response.get("error")
        if error is not None:
            raise AssertionError(f"STDIO smoke failed. Response error: {error}.")
        return response["result"]


def read_stderr(process: subprocess.Popen[str]) -> str:
    assert process.stderr is not None
    try:
        return process.stderr.read().strip()
    except Exception:
        return ""


def main() -> None:
    args = parse_args()
    env = os.environ.copy()
    env["SHARDINGSPHERE_MCP_TRANSPORT"] = "stdio"
    command = (
        ["docker", "run", "--rm", "-i", "-e", "SHARDINGSPHERE_MCP_TRANSPORT=stdio", args.launcher[0]]
        if "docker" == args.mode
        else args.launcher
    )
    process = subprocess.Popen(
        command,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        env=env,
    )
    try:
        write_request(
            process,
            {
                "jsonrpc": "2.0",
                "id": "init-1",
                "method": "initialize",
                "params": {
                    "protocolVersion": PROTOCOL_VERSION,
                    "capabilities": {},
                    "clientInfo": {"name": "ci-stdio-smoke", "version": "1.0.0"},
                },
            },
        )
        initialize_result = read_response(process, "init-1")
        if initialize_result.get("protocolVersion") != PROTOCOL_VERSION:
            raise AssertionError(f"Unexpected protocol version: {initialize_result}")
        write_request(process, {"jsonrpc": "2.0", "method": "notifications/initialized", "params": {}})
        write_request(
            process,
            {"jsonrpc": "2.0", "id": "tool-list-1", "method": "tools/list", "params": {}},
        )
        tools_result = read_response(process, "tool-list-1")
        tool_names = {each["name"] for each in tools_result["tools"]}
        expected_tool_names = {"search_metadata", "execute_query"}
        if not expected_tool_names.issubset(tool_names):
            raise AssertionError(f"Expected tools/list to expose {sorted(expected_tool_names)}. Actual tools: {sorted(tool_names)}")
        write_request(
            process,
            {"jsonrpc": "2.0", "id": "resource-read-1", "method": "resources/read", "params": {"uri": "shardingsphere://capabilities"}},
        )
        capabilities_result = read_response(process, "resource-read-1")
        resource_contents = capabilities_result.get("contents", [])
        if not any("supportedTools" in each.get("text", "") for each in resource_contents):
            raise AssertionError(f"Expected capabilities resource to expose supportedTools. Actual contents: {resource_contents}")
        write_request(
            process,
            {
                "jsonrpc": "2.0",
                "id": "tool-call-1",
                "method": "tools/call",
                "params": {"name": "search_metadata", "arguments": {"database": "orders", "query": "order", "object_types": ["TABLE"]}},
            },
        )
        search_result = read_response(process, "tool-call-1")
        search_items = search_result.get("structuredContent", {}).get("items", [])
        search_item_names = {each.get("name") for each in search_items if isinstance(each, dict)}
        if "orders" not in search_item_names:
            raise AssertionError(f"Expected search_metadata to expose the orders table. Actual search items: {search_items}")
        assert process.stdin is not None
        process.stdin.close()
        exit_code = process.wait(timeout=PROCESS_TIMEOUT_SECONDS)
        if exit_code != 0:
            raise AssertionError(f"STDIO smoke failed. Container exited with code {exit_code}. stderr: {read_stderr(process)}")
    except Exception as ex:
        if process.poll() is None:
            process.kill()
            process.wait(timeout=PROCESS_TIMEOUT_SECONDS)
        print(f"{ex} stderr: {read_stderr(process)}", file=sys.stderr)
        raise


if __name__ == "__main__":
    main()
