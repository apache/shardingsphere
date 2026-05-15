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

# Source Map: MCP Encrypt/Mask Scorecard 100

## Official Sources

- MCP Specification `2025-11-25`: `https://modelcontextprotocol.io/specification/2025-11-25`
- MCP Tools: `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- MCP Resources: `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- MCP Prompts: `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- MCP Transports: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- MCP Authorization: `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`
- MCP Java SDK Server: `https://java.sdk.modelcontextprotocol.io/latest/server/`

## Source-Driven Decisions

- Tool results do not require Markdown. Official MCP tool results may contain text, resource links, embedded resources, and structured content.
  When structured content is returned, serialized JSON text fallback is recommended for compatibility.
- Tool `outputSchema` is optional, but when present the server must return schema-conforming `structuredContent`.
- Tool annotations are hints and must not replace security enforcement.
- Resource subscriptions, list-changed notifications, progress, cancellation, roots, sampling, logging, icons, and task execution support are score-affecting
  only when advertised, required by this package, or claimed as implemented.
- SDK-deferred fields must be documented instead of emitted through ShardingSphere-only metadata as if they were official MCP fields.

## Local Source Evidence Targets

- MCP module chain: `mcp/pom.xml`
- MCP Java SDK version: `mcp/bootstrap/pom.xml`
- Server capabilities and registration: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`
- Tool result payload mapping: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java`
- Tool descriptor and output-schema enforcement: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java`
- Input-schema contract: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java`
- Encrypt descriptor: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-encrypt.yaml`
- Mask descriptor: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-mask.yaml`
- Encrypt workflow planning: `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/tool/service/EncryptWorkflowPlanningService.java`
- Mask workflow planning: `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/tool/service/MaskWorkflowPlanningService.java`
- Default E2E switches: `test/e2e/mcp/src/test/resources/env/e2e-env.properties`
- mcp-builder evaluation artifact: `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml`

## Repository Standards

- `CODE_OF_CONDUCT.md` requires readability, cleanliness, consistency, simplicity, abstraction, successful build checks, and full unit-test coverage except simple getters/setters.
- `AGENTS.md` requires module-scoped verification, Checkstyle/Spotless, no branch switching for this task, and evidence-first reporting.
- `.specify/memory/constitution.md` requires Proxy-first logical abstraction, explicit operator control, minimal safe automation, deterministic naming, and complete verification before completion.
