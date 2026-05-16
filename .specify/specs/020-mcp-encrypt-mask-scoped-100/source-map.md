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

# Source Map: MCP Encrypt/Mask Scoped Scorecard 100

## Official Sources

- MCP specification `2025-11-25`: `https://modelcontextprotocol.io/specification/2025-11-25`
- MCP tools: `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- MCP resources: `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- MCP prompts: `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- MCP completions: `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion`
- MCP transports: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`

## Local Version Sources

- MCP Java SDK version: `mcp/bootstrap/pom.xml`
- Protocol constant and server instructions: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportConstants.java`
- Server capability wiring: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`
- HTTP transport: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
- Payload formatting: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java`
- Descriptor validation: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`

## Encrypt/Mask Sources

- Encrypt descriptor: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptor-encrypt.yaml`
- Mask descriptor: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptor-mask.yaml`
- Encrypt feature code: `mcp/features/encrypt/src/main/java/org/apache/shardingsphere/mcp/feature/encrypt/`
- Mask feature code: `mcp/features/mask/src/main/java/org/apache/shardingsphere/mcp/feature/mask/`
- Core workflow support: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/`
- E2E harness: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/`

## Local Standards

- Repository agent guide: `AGENTS.md`
- Binding code standards: `CODE_OF_CONDUCT.md`
- Spec Kit constitution: `.specify/memory/constitution.md`
- Spec Kit templates: `.specify/templates/spec-template.md`, `.specify/templates/tasks-template.md`

## Non-Goal Evidence

- MCP icons and `Tool.execution` are official fields, but this checkpoint does not require them because the project does not need them and SDK `1.1.2` is fixed.
- Compatibility with protocol revisions other than `2025-11-25` is not part of the coverage target.
- Non-encrypt/mask ShardingSphere MCP features are not part of functional completeness scoring.
