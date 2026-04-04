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

"""Prepare MCP server.json for publication."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Prepare ShardingSphere MCP server.json for publication.")
    parser.add_argument("--path", default="mcp/server.json", help="Path to server.json.")
    parser.add_argument("--version", required=True, help="Published server version.")
    parser.add_argument("--identifier", required=True, help="OCI identifier to publish.")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    path = Path(args.path)
    server = json.loads(path.read_text())
    server["version"] = args.version
    server["packages"][0]["identifier"] = args.identifier
    server["packages"][0]["version"] = args.version
    path.write_text(json.dumps(server, indent=2) + "\n")


if __name__ == "__main__":
    main()
