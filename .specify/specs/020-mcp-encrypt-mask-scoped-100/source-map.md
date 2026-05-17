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
- MCP Registry server metadata schema: `https://modelcontextprotocol.io/registry/versioning`
- Ollama qwen3 baseline: `https://ollama.com/library/qwen3:1.7b`
- llama.cpp server documentation: `https://github.com/ggml-org/llama.cpp/blob/master/tools/server/README.md`
- llama.cpp Docker documentation: `https://github.com/ggml-org/llama.cpp/blob/master/docs/docker.md`
- ggml-org Qwen3 GGUF model card: `https://huggingface.co/ggml-org/Qwen3-1.7B-GGUF`

## Local Version Sources

- MCP Java SDK version: `mcp/bootstrap/pom.xml`
- Protocol constant and server instructions: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportConstants.java`
- Server capability wiring: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`
- HTTP transport: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
- Payload formatting: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java`
- Descriptor validation: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`
- MCP Registry metadata validation: `mcp/registry/src/main/java/org/apache/shardingsphere/mcp/registry/MCPRegistryMetadataCommand.java`

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
- Codex CLI command source: `codex exec --help`, including `--ephemeral`, `--sandbox read-only`, `-C`, and stdin prompt support.

## LLM Docker Runtime Sources

- LLM E2E configuration: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java`
- Dockerized LLM runtime support: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/`
- LLM smoke E2E entry: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/smoke/LLMSmokeE2ETest.java`
- LLM usability E2E entry: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/LLMUsabilitySuiteE2ETest.java`
- LLM runtime rebaseline design: `.specify/specs/020-mcp-encrypt-mask-scoped-100/llm-runtime-rebaseline-design.md`

## Reopened LLM Runtime Target

- Selected score-closing runtime: Docker-owned `llama.cpp` server.
- Selected score-closing model: `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Docker manifest inspected locally for `ghcr.io/ggml-org/llama.cpp:server`.
- Resolved linux/amd64 digest during planning: `sha256:c950f1c4297c272ea95cf85318bcca42ac6a462fb3161d11047303b1d97f4dab`.
- Resolved linux/arm64 digest during planning: `sha256:bbb33c86d3b4dff468aca416859c889f3b0844c3590f0b9c33125b745e5791e0`.
- Rejected previous score-closing runtime: `ollama/ollama:0.23.1`, because the linux/amd64 image includes about `4.01GB` compressed runtime layers, including about `3.86GB` of CUDA-bearing content.

## Non-Goal Evidence

- MCP icons and `Tool.execution` are official fields, but this checkpoint does not require them because the project does not need them and SDK `1.1.2` is fixed.
- Compatibility with protocol revisions other than `2025-11-25` is not part of the coverage target.
- Non-encrypt/mask ShardingSphere MCP features are not part of functional completeness scoring.
- External LLM endpoints are debug-only and are not valid score-closing evidence for this package.
- Ollama full images are no longer valid score-closing evidence for this package.

## Protocol Closure Evidence

- Protocol score closure is source-mapped only to MCP `2025-11-25` and local SDK `1.1.2`.
- `mcp/bootstrap/pom.xml` fixes `mcp-java-sdk.version` at `1.1.2`; no SDK upgrade or dependency drift is part of this package.
- `MCPTransportConstants.PROTOCOL_VERSION` is the score-closing server protocol version for MCP `2025-11-25`.
- `mcp/server.json` uses the official MCP Registry server metadata schema. That registry schema date is packaging metadata and is not the MCP protocol revision scored by this package.
- Existing `2025-06-18` transport compatibility may remain as product compatibility, but it is not required evidence for this scorecard and is not counted toward MCP protocol conformity.
- Descriptor `icons` and `Tool.execution` stay non-goals for this checkpoint. YAML descriptor key validation rejects them today so unsupported optional protocol fields cannot silently become public ShardingSphere descriptor API.
