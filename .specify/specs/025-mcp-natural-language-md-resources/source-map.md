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

# Source Map: MCP Natural Language Markdown Resources

## Official MCP Evidence

- The `latest` specification alias currently redirects to protocol version `2025-11-25`.
  This was rechecked on 2026-05-17; implementation evidence should cite explicit versioned URLs to avoid future drift.
- [MCP Lifecycle](https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle) shows the server `initialize` result may include `instructions`.
  This supports keeping server instructions as a first-class MCP server initialization field, not as a tool or prompt descriptor.
- [MCP Schema](https://modelcontextprotocol.io/specification/2025-11-25/schema) defines `InitializeResult.instructions` as optional guidance for using the server and its features.
  This supports treating the text as model-facing guidance and preserving it during migration.
- [MCP Prompts](https://modelcontextprotocol.io/specification/2025-11-25/server/prompts) defines prompts as discoverable templates that clients can list and retrieve.
  This supports keeping reusable prompt templates in `prompts/*.md` and not merging them into server instructions.
  It also requires prompt inputs and outputs to be validated, so loader changes must not weaken existing prompt argument validation.
- [MCP Discovery Draft](https://modelcontextprotocol.io/specification/draft/server/discover) also includes optional `instructions` in discovery results.
  This is draft-only evidence for a future compatibility direction, not an implementation requirement for this package.
  If ShardingSphere later adds `server/discover`, it should reuse the same canonical instruction resource.
- [MCP Tools](https://modelcontextprotocol.io/specification/2025-11-25/server/tools) defines tool metadata, input schemas, output schemas, structured content, and error handling.
  This supports keeping tool descriptions, schema descriptions, `structuredContent`, and tool execution errors outside free-form Markdown resources.
- [MCP Resources](https://modelcontextprotocol.io/specification/2025-11-25/server/resources) defines resources and resource templates as URI-identified structured entries.
  This supports keeping resource titles, descriptions, URI templates, annotations, and content metadata in descriptor resources.
  It also clarifies that a Java classpath resource file is not automatically an MCP resource exposed through `resources/list`.
- [Server Instructions Blog](https://blog.modelcontextprotocol.io/posts/2025-11-03-using-server-instructions/) recommends using instructions for cross-feature relationships,
  operational patterns, and constraints, while avoiding critical deterministic behavior or personality changes.
  This supports moving prose to Markdown without moving security checks or runtime validation into prose.

## MCP-Builder Design Evidence

- MCP best practices require tool descriptions to match actual functionality and remain unambiguous.
  Therefore tool, resource, prompt, and schema descriptions stay in descriptor YAML rather than a central Markdown file.
- MCP best practices distinguish JSON for machine processing from Markdown for human readability.
  Therefore structured workflow payloads, JSON keys, status labels, and next-action objects stay structured.
- MCP best practices treat annotations as hints, not security guarantees.
  Therefore read-only and destructive behavior must remain deterministic in tool code and metadata, not only in instructions.
- MCP best practices require helpful error messages.
  Therefore runtime diagnostics should stay with code paths or a future message catalog, not server instruction Markdown.

## Current Code Sources

- `mcp/bootstrap/pom.xml` uses MCP Java SDK `1.1.2` through `io.modelcontextprotocol.sdk:mcp-core` and `mcp-json-jackson2`.
  The bootstrap module currently depends on `shardingsphere-mcp-core`, while core depends on `shardingsphere-mcp-support`.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportConstants.java`
  currently owns `SERVER_INSTRUCTIONS` as a long Java string.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactory.java`
  passes that string into `.instructions(...)` during server creation.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPPromptTemplateLoader.java`
  already loads Markdown prompt templates from the classpath.
  It uses the thread context class loader and currently returns raw UTF-8 text.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/prompt/MCPPromptSpecificationFactory.java`
  renders prompt Markdown and exposes it as `TextContent`.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java`
  extracts prompt placeholders from loaded templates during descriptor validation.
  Header stripping must happen before placeholder extraction without changing placeholder semantics.
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactoryTest.java`
  verifies server info, capabilities, tools, resources, resource templates, and prompts, but does not currently assert instructions.
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/AbstractMCPWireBehaviorTest.java`
  already exercises `initialize` over transport paths and is a candidate location for public wire-level instruction assertions.
  The helper currently consumes the initialize response during client opening, so future tests may need a small public test-helper hook.
- `distribution/mcp/pom.xml` packages `shardingsphere-mcp-bootstrap` as a runtime dependency into the MCP distribution `lib` directory.
  Package verification should inspect the bootstrap jar included in the distribution, not only source resources.
- `mcp/bootstrap/src/main/resources` does not currently contain MCP resources.
  The accepted instruction path will introduce the bootstrap resource tree.

## Current Resource Sources

- Descriptor YAML resources:
  - `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-support.yaml`
  - `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-encrypt.yaml`
  - `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-mask.yaml`
- Prompt Markdown resources:
  - `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/prompts/inspect-metadata.md`
  - `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/prompts/recover-workflow.md`
  - `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/prompts/safe-sql-execution.md`
  - `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/prompts/plan-encrypt-rule.md`
  - `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/prompts/plan-mask-rule.md`
- Accepted new instruction resource path:
  - `mcp/bootstrap/src/main/resources/META-INF/shardingsphere-mcp/instructions/server-instructions.md`
  - This is an internal classpath resource file, not a new MCP resource URI.

## Natural Language Ownership Map

- Markdown-owned content:
  - Server-level MCP instructions.
  - Existing prompt templates under `META-INF/shardingsphere-mcp/prompts`.
  - Future multi-step model guidance only after it is proven to be authored prose rather than structured payload data.
- Descriptor-owned content:
  - Tool, resource, resource template, prompt, argument, completion, and JSON Schema descriptions.
  - Descriptor extensions that drive catalog, completion, or workflow behavior.
- Code or message-catalog-owned content:
  - Exceptions, validation errors, HTTP errors, and logs.
  - Elicitation copy if it is a short operational UI message rather than reusable model guidance.
- Machine-contract-owned content:
  - JSON keys, tool names, prompt names, resource URI templates, operation IDs, enum values, and status labels.
  - `next_actions`, `recovery.next_actions`, and workflow response structures.
- Structured-dictionary-owned content:
  - Intent aliases, keyword matching rules, and algorithm/property recommendation records.

## Source-Control Header Note

Existing Markdown prompt resources include ASF license headers.
Future model-facing Markdown loading must strip source-control headers before exposing text to MCP clients.
This is especially important for `instructions`, because `InitializeResult.instructions` may be inserted into model context by clients.
The safe stripping rule should be narrow: remove only a leading ASF HTML comment block in Markdown resources, not arbitrary comments inside authored content.
The matcher should verify ASF license markers before stripping so ordinary leading HTML comments stay intact.

## Reanalysis Notes

- The current protocol version in code is `2025-11-25`, so source citations should prefer `2025-11-25` pages when available.
- `server/discover` is still a draft source. Treat it as future-direction evidence only.
- Use explicit `2025-11-25` source URLs for implementation decisions and use `latest` only as a periodic drift check.
- Prompt Markdown already appears to be loaded as raw text. Header stripping should therefore be considered for prompt templates as well as server instructions.
- Public behavior should be verified through initialization responses over HTTP and STDIO where practical.
  SDK-private or reflection-based assertions are fallback evidence, not the preferred proof.
- Packaged artifact checks matter because the new resource lives under `src/main/resources` and must survive bootstrap and distribution packaging.
  In the distribution, the resource should be present inside the copied bootstrap jar under `lib`.
- A shared loader should not use path-only global caching because class loader context can affect which classpath resource is visible.
- Markdown resources in this package should not gain YAML front matter.
  Descriptor metadata already has structured YAML ownership.
- Server instructions should remain static server-level guidance.
  Runtime database metadata, feature inventories, and dynamic capabilities belong in resources, descriptors, or tool results.
- Server instructions are loaded as startup/session guidance.
  This package does not design hot reload or in-session instruction mutation.
- The accepted Markdown path is a source resource file.
  It should not add a new `shardingsphere://...` URI or appear in `resources/list`.
