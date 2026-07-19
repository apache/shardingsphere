<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# MCP conformance scope

The packaged server is checked with `modelcontextprotocol/conformance` v0.1.16 at commit
`21a9a2febd7100d7c17ac1021ee7f2ed9f66a1e0`, using protocol version `2025-11-25`.
`server-scenarios.txt` is the executable applicability list.

The list contains generic protocol scenarios that do not require a server to expose conformance-only capabilities.
Tool calls, resource reads, resource-template reads, prompt reads, completion, error payloads, and workflow behavior are instead exercised against ShardingSphere's real names and data by the deterministic MCP E2E suite.
The CI lane starts the packaged server with its loopback HTTP configuration so the official DNS rebinding scenario evaluates the server's loopback Origin policy rather than its separate Docker remote-binding policy.

The remaining official server scenarios are intentionally excluded for one of these reasons:

- They require synthetic names or payloads such as `test_simple_text`, `test://static-text`, or `test_simple_prompt` that are not part of the ShardingSphere product contract.
- They exercise optional capabilities that this server does not advertise, including resource subscriptions, sampling, audio/image content, progress notifications, and protocol logging.
- They cover pending or transport variants outside the fixed ShardingSphere MCP transport surface.

No production test hooks are added to make an inapplicable fixture-specific scenario pass.
