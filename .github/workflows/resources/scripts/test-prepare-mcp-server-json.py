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

"""Unit tests for prepare-mcp-server-json.py."""

from __future__ import annotations

import importlib.util
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path


sys.dont_write_bytecode = True
SCRIPT_PATH = Path(__file__).with_name("prepare-mcp-server-json.py")
MODULE_SPEC = importlib.util.spec_from_file_location("prepare_mcp_server_json", SCRIPT_PATH)
PREPARE_MODULE = importlib.util.module_from_spec(MODULE_SPEC)
MODULE_SPEC.loader.exec_module(PREPARE_MODULE)


class PrepareMCPServerJsonTest(unittest.TestCase):

    def test_release_rewrite(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            server_path = Path(temp_dir, "server.json")
            server_path.write_text(json.dumps(create_server_metadata()))
            run_script("--path", str(server_path), "--version", "5.5.4", "--identifier", "ghcr.io/apache/shardingsphere-mcp:5.5.4")
            actual = json.loads(server_path.read_text())
            self.assertEqual("5.5.4", actual["version"])
            self.assertEqual({"ghcr.io/apache/shardingsphere-mcp:5.5.4"}, {each["identifier"] for each in actual["packages"]})
            self.assertEqual({"5.5.4"}, {each["version"] for each in actual["packages"]})

    def test_snapshot_rejection(self) -> None:
        with tempfile.TemporaryDirectory() as temp_dir:
            server_path = Path(temp_dir, "server.json")
            server_path.write_text(json.dumps(create_server_metadata()))
            actual = run_script("--path", str(server_path), "--version", "5.5.4-SNAPSHOT", "--identifier", "ghcr.io/apache/shardingsphere-mcp:5.5.4-SNAPSHOT", check=False)
            self.assertNotEqual(0, actual.returncode)
            self.assertIn("SNAPSHOT", actual.stderr)

    def test_development_snapshot_validation(self) -> None:
        PREPARE_MODULE.validate_server_json(create_server_metadata(), True)

    def test_missing_http_url_rejection(self) -> None:
        server = create_server_metadata()
        del server["packages"][1]["transport"]["url"]
        with self.assertRaisesRegex(ValueError, "streamable-http transport must define a URL"):
            PREPARE_MODULE.validate_server_json(server, True)

    def test_mismatched_package_version_rejection(self) -> None:
        server = create_server_metadata()
        server["packages"][0]["version"] = "5.5.3"
        with self.assertRaisesRegex(ValueError, "package version must match"):
            PREPARE_MODULE.validate_server_json(server, True)


def create_server_metadata() -> dict:
    return {
        "$schema": PREPARE_MODULE.SCHEMA_URL,
        "name": PREPARE_MODULE.SERVER_NAME,
        "title": "Apache ShardingSphere MCP",
        "description": "MCP runtime for Apache ShardingSphere metadata discovery, SQL preview, and rule workflows",
        "websiteUrl": "https://github.com/apache/shardingsphere/tree/master/mcp",
        "repository": {
            "url": "https://github.com/apache/shardingsphere",
            "source": "github",
            "subfolder": "mcp",
        },
        "version": "5.5.4-SNAPSHOT",
        "packages": [
            create_package("stdio"),
            create_package("streamable-http", "http://127.0.0.1:18088/mcp"),
        ],
    }


def create_package(transport_type: str, url: str | None = None) -> dict:
    result = {
        "registryType": "oci",
        "identifier": "ghcr.io/apache/shardingsphere-mcp:5.5.4-SNAPSHOT",
        "version": "5.5.4-SNAPSHOT",
        "transport": {"type": transport_type},
        "environmentVariables": [
            {
                "name": "SHARDINGSPHERE_MCP_TRANSPORT",
                "value": "http" if "streamable-http" == transport_type else "stdio",
                "description": "Launch the container in the selected transport mode.",
                "isRequired": False,
                "format": "string",
                "isSecret": False,
            },
            {
                "name": "SHARDINGSPHERE_MCP_CONFIG",
                "description": "Optional absolute config path inside the OCI container.",
                "isRequired": False,
                "format": "string",
                "isSecret": False,
            },
        ],
    }
    if url:
        result["transport"]["url"] = url
    return result


def run_script(*args: str, check: bool = True) -> subprocess.CompletedProcess:
    return subprocess.run([sys.executable, str(SCRIPT_PATH), *args], capture_output=True, text=True, check=check)


if __name__ == "__main__":
    unittest.main()
