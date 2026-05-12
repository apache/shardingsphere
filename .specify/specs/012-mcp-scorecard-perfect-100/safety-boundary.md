<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Safety Boundary

This checkpoint keeps safety simple and explicit. It does not introduce a user
management subsystem. The enforced identity boundary is the MCP session, which
matches the state boundary already used for workflow plans and transactions.

## Enforced Controls

- HTTP remote exposure requires the existing bearer-token and loopback/remote
  access checks.
- STDIO inherits the local process boundary and keeps protocol frames on stdout.
- Side-effecting SQL and workflows still require preview plus explicit
  `approved_by_user=true` before execution.
- SQL classification rejects unsupported, banned, metadata-introspection, and
  mismatched tool usage before execution.
- `MCPToolCallLimiter` counts every tool call before dispatch, including invalid
  calls, and returns `rate_limited` with recovery guidance when the session quota
  is exhausted.
- `MCPClientSafetyPolicy` exposes the session identity scope, tool-call quota,
  abuse guard, and external-model boundary through `security_hints` in
  `shardingsphere://capabilities`.

## External Model Boundary

The MCP production runtime never calls external model providers. Live LLM E2E
tests call model endpoints from the test harness only. The default LLM endpoint
is the local OpenAI-compatible Ollama endpoint, and the current live evidence
uses Docker-backed Ollama.

## Verification

- Production safety policy:
  `MCPClientSafetyPolicyTest`, `MCPToolCallLimiterTest`,
  `MCPToolControllerTest`, `MCPErrorConverterTest`, and
  `ServerCapabilitiesHandlerTest`.
- E2E safety:
  `HttpTransportSecurityE2ETest`, `HttpTransportAccessTokenE2ETest`,
  `HttpTransportApprovalSafetyE2ETest`, live LLM scorecards in `EV-019`, and
  harness guardrails in `EV-020`.
