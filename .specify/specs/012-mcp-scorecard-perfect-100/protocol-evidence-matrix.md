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

Protocol correctness reaches 100 only when the matrix above is backed by a
green current command in `evidence-ledger.md`. Missing rows are open risks; no
waiver is allowed.
