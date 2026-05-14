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

# Protocol Evidence Matrix

This checkpoint treats protocol correctness as evidence-backed only when the
same protocol surface has a production-level contract test and an E2E transport
or golden-contract proof.

The 2026-05-13 standard-first gate adds another condition: every protocol row
must also map to the official MCP Specification sources listed in
`source-driven-mcp-standard-map.md`.

## Source-Driven Closed Mapping

- Lifecycle: official initialization, protocol-version negotiation, and capability negotiation are covered by `StreamableHttpMCPServletTest`,
  `SessionManagedStdioTransportProviderTest`, and production H2 E2E initialization flows.
- HTTP transport: Streamable HTTP POST/GET endpoint behavior is covered by `StreamableHttpMCPServletTest`, `HttpTransportContractE2ETest`,
  and `ProductionH2CapabilityDiscoveryE2ETest`; `Origin` and loopback expectations remain covered by `LoopbackOriginHeaderConstraintTest`
  and `HttpTransportSecurityE2ETest`.
- Authorization: protected resource metadata, deployment-level bearer compatibility, OAuth introspection validation, and bearer challenge behavior are covered by
  `HttpBearerAuthorizationHandlerTest`, `OAuthTokenValidatorTest`, `OAuthProtectedResourceMetadataServletTest`, `MCPLaunchConfigurationTest`,
  `HttpTransportAccessTokenE2ETest`, and `HttpTransportOAuthIntrospectionE2ETest`.
- Tools: `tools/list`, `tools/call`, `inputSchema`, `outputSchema`, `structuredContent`, text fallback, annotations, and `isError`
  behavior are covered by `MCPToolSpecificationFactoryTest`, `MCPToolControllerTest`, `HttpTransportGoldenContractE2ETest`, and production H2 E2E tests.
- Resources: `resources/list`, `resources/read`, `resources/templates/list`, URI handling, and templates are covered by
  `MCPResourceSpecificationFactoryTest`, `MCPResourceControllerTest`, `HttpTransportGoldenContractE2ETest`, and `ProductionH2CapabilityDiscoveryE2ETest`.
- Prompts and completion: `prompts/list`, `prompts/get`, `completion/complete`, `ref/prompt`, `ref/resource`, `values`, `total`, and `hasMore`
  are covered by specification factory tests and `HttpTransportGoldenContractE2ETest`.
- Pagination: official MCP list pagination remains constrained to opaque `cursor` request fields and `nextCursor` response fields; application-level
  search pagination stays inside structured tool payloads and is not treated as MCP protocol pagination.
- Errors: JSON-RPC protocol errors are covered by transport/resource negative tests, while tool execution errors remain tool results with standard
  `isError` behavior and structured recovery payloads.

## Non-Standard Behavior Rejected In Current Contract

- `shardingsphere://capabilities` is a ShardingSphere domain catalog resource only.
- Official protocol discovery uses `tools/list`, `resources/list`, `resources/templates/list`, `prompts/list`, and `completion/complete`.
- Recovery guidance now asks clients to prefer official discovery before reading the domain catalog resource.
- HTTP authorization uses bearer headers, protected resource metadata, and either a configured deployment token or RFC 7662 OAuth introspection scoped to the
  protected MCP resource; it does not encode credentials in query parameters or pass MCP tokens to downstream services.

## Production Protocol Surface

- HTTP Streamable transport:
  `StreamableHttpMCPServletTest`, `ShardingSphereServerTransportSecurityValidatorTest`,
  `ServerTransportSecurityValidatorFactoryTest`, `AccessTokenHeaderConstraintTest`,
  `ProtocolVersionHeaderConstraintTest`, and `LoopbackOriginHeaderConstraintTest`.
- STDIO transport:
  `SessionManagedStdioTransportProviderTest` and `StdioMCPServerTest`.
- Session lifecycle:
  `MCPSessionManagerTest`, `MCPSessionExecutionCoordinatorTest`,
  `StreamableHttpMCPServletTest`, and `SessionManagedStdioTransportProviderTest`.
- Tools:
  `MCPToolSpecificationFactoryTest`, `MCPToolControllerTest`,
  `ToolHandlerRegistryTest`, `ExecuteQueryToolHandlerTest`,
  `ExecuteUpdateToolHandlerTest`, `WorkflowExecutionToolHandlerTest`,
  and `WorkflowValidationToolHandlerTest`.
- Resources:
  `MCPResourceSpecificationFactoryTest`, `MCPResourceControllerTest`,
  `ResourceHandlerRegistryTest`, `CoreResourceHandlerSurfaceTest`,
  `ServerCapabilitiesHandlerTest`, and metadata resource handler tests.
- Prompts and completions:
  `MCPPromptSpecificationFactoryTest` and `MCPCompletionSpecificationFactoryTest`.
- Protocol errors and recovery payloads:
  `MCPErrorConverterTest`.
- Model-facing descriptor contract:
  `MCPModelFirstContractPayloadBuilderTest`, `ServerCapabilitiesHandlerTest`,
  and `MCPDocumentationContractTest`.

## E2E Protocol Surface

- HTTP JSON-RPC contract:
  `HttpTransportContractE2ETest`, `HttpTransportSessionLifecycleE2ETest`,
  `HttpTransportSecurityE2ETest`, `HttpTransportAccessTokenE2ETest`,
  and `HttpTransportApprovalSafetyE2ETest`.
- Golden contract drift:
  `HttpTransportGoldenContractE2ETest` covers capabilities, resources,
  resource templates, tools, prompts, completions, recovery, and workflow payloads.
- Production H2 transport behavior:
  `ProductionH2CapabilityDiscoveryE2ETest`, `ProductionH2MetadataResourceE2ETest`,
  `ProductionH2SQLExecutionE2ETest`, `ProductionH2AiNativeInteractionE2ETest`,
  and `ProductionMultiDatabaseE2ETest`.
- STDIO runtime behavior:
  `EV-015` revalidates the same H2 production surface over STDIO.
- MySQL runtime behavior:
  `EV-016` revalidates HTTP and STDIO against Docker-backed MySQL 8.0.36.
- Packaged runtime behavior:
  `EV-018` revalidates packaged HTTP, STDIO, and plugin discovery.
- Live model interaction behavior:
  `EV-019` and `EV-020` require native tool-call coverage, zero invalid calls,
  zero approval violations, and full core plus extended usability scorecards.

## Completion Rule

Protocol correctness reaches 100 because the matrix above is backed by green
current commands in `EV-029`, `EV-031`, and `EV-032`. Missing rows are not
waived; no open protocol risks remain for this scoped gate.
