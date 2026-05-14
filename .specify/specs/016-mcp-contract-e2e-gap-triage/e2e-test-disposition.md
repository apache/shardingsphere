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

# MCP E2E Test Disposition Matrix

## Scope

This matrix covers every Java class currently under `test/e2e/mcp/src/test/java`.
It classifies each class by release-evidence value before implementation tasks are generated.

Disposition keys:

- **KEEP-E2E**: keep as product, protocol, distribution, or model-facing E2E evidence.
- **KEEP-SUPPORT**: keep as fixture or helper required by a KEEP-E2E class.
  Helper-unit tests may stay here until a narrower harness-support module exists.
- **KEEP-GOV**: keep as governance or artifact validation until a better module owns it.
- **REDUCE-CANDIDATE**: keep one canonical gate, but trim duplicated or brittle assertions.

No class is marked for direct deletion in this draft.
Deletion requires a follow-up task that names the preserving lower-level test or artifact.

Each section below is the preliminary `evidence_value`.
Each row's uppercase token is the preliminary `disposition`.
Before task generation, every REDUCE-CANDIDATE row must gain an explicit `preserved_by` target.

## Required Missing Tests

- Strict HTTP `Accept` negotiation:
  target `HttpTransportContractE2ETest#assertRejectInitializeWithoutAcceptHeader` and
  `HttpTransportContractE2ETest#assertRejectInitializeWithUnsupportedAcceptHeader`;
  expanded by reanalysis to `assertRejectInitializeWithSseOnlyAcceptHeader`,
  `assertRejectEventStreamWithoutAcceptHeader`, and `assertRejectEventStreamWithUnsupportedAcceptHeader`;
  support with `StreamableHttpMCPServletTest`.
  Status: implemented by T021/T025 on 2026-05-14; missing and unsupported POST/GET `Accept` requests reject with HTTP 400.
- Remote HTTP origin allowlist:
  target `HttpTransportConfiguration`, `YamlHttpTransportConfiguration`,
  `YamlHttpTransportConfigurationSwapper`, `ServerTransportSecurityValidatorFactory`,
  `YamlHttpTransportConfigurationSwapperTest`, `ServerTransportSecurityValidatorFactoryTest`,
  and `HttpTransportSecurityE2ETest`.
  Required cases: configured allowed remote origin passes; unlisted, missing, malformed, and loopback-only remote origins fail.
  Status: implemented by T022/T026 on 2026-05-14; remote bindings require `allowedOrigins` and E2E rejects disallowed origins with HTTP 403.
- Full initialize lifecycle:
  target `AbstractHttpProgrammaticRuntimeE2ETest#initializeSession`,
  `MCPHttpInteractionClient#open`, and `HttpTransportContractE2ETest`.
  STDIO already sends `notifications/initialized` in `AbstractProcessMCPStdioInteractionClient`.
- Input-schema rejection before handler execution:
  target unit coverage in `MCPToolArgumentContractTest` or `ToolHandlerRegistryTest`,
  SDK-facing rejection in `MCPToolSpecificationFactoryTest`, and one HTTP smoke in `HttpTransportContractE2ETest`.
- Resource URI encoding and missing variables:
  target `MetadataDiscoveryE2ETest`; unit evidence already exists in `MCPUriPatternTest`
  and `SearchMetadataToolServiceTest`.
  Required cases: percent-encoded spaces, slashes, reserved characters, malformed encoding, and missing variables.
- Positive completion:
  target metadata completion in `HttpTransportContractE2ETest`, `MetadataDiscoveryE2ETest`,
  or `ProductionH2RuntimeSmokeE2ETest`; target algorithm completion in
  `HttpProductionProxyEncryptWorkflowE2ETest` and `HttpProductionProxyMaskWorkflowE2ETest`;
  target workflow-plan completion in workflow approval E2E.
- Session deletion, transaction cleanup, and cross-session isolation:
  target `HttpTransportSessionLifecycleE2ETest` and `ExecuteQueryTransactionE2ETest`.
  Required cases: two HTTP clients, cross-session transaction isolation, and workflow completion isolation.
- `mcp/server.json` schema and release publication:
  target `.github/workflows/resources/scripts/prepare-mcp-server-json.py`,
  `.github/workflows/jdk21-subchain-ci.yml`, and `.github/workflows/mcp-build.yml`.
  Use packaged distribution smoke only when runtime startup or plugin discovery behavior is involved.
  Status: implemented by T050/T051 on 2026-05-14; script-level coverage and workflow validation now cover schema URL,
  version and identifier rewrite, stdio and Streamable HTTP package transports, OCI metadata, and release-only SNAPSHOT rejection.

## Resolved Precision Decisions

- Input-schema rejection is split across core unit, bootstrap SDK-facing unit, and one HTTP contract smoke.
- Positive completion is split across provider unit coverage, metadata E2E, algorithm E2E, and workflow-plan E2E.
- Registry validation is split across script rewrite coverage and release workflow schema validation.
- Remote-origin allowlist ownership is assigned to HTTP transport config, YAML swapper, validator factory, and security E2E.
- OAuth inactive, expired, wrong issuer, introspection failure, 401/403 challenge, and no-token-passthrough
  evidence remain tracked by 012 unless reassigned.
- Helper-unit tests inside `test/e2e/mcp` are preserved as `KEEP-SUPPORT` until a narrower harness module exists.

## Environment and Configuration

- `MCPE2ECondition`: KEEP-SUPPORT. Shared enablement condition for opt-in E2E modes.
- `MCPE2ETestConfiguration`: KEEP-SUPPORT. Shared flag parsing and runtime configuration.
- `MCPE2ETestConfigurationTest`: KEEP-SUPPORT. Fixture-unit coverage for shared E2E configuration parsing.

## LLM Harness and Model-Use Tests

- `LLME2EConfiguration`: KEEP-SUPPORT. LLM suite configuration holder.
- `LLMConversationExecutor`: KEEP-SUPPORT. Conversation execution SPI for LLM E2E.
- `LLMMCPActionExecutor`: KEEP-SUPPORT. Bridges LLM tool calls into MCP actions.
- `LLMMCPConversationArtifacts`: KEEP-SUPPORT. Artifact envelope for model-use evidence.
- `LLMMCPConversationRunner`: KEEP-SUPPORT. Core runner for LLM smoke and usability suites.
- `LLMMCPConversationRunnerTest`: KEEP-SUPPORT. Fixture-unit coverage for the LLM harness runner.
- `LLMMCPFinalAnswerValidator`: KEEP-SUPPORT. Validates scenario answers for LLM E2E.
- `LLMMCPInteractionCoverage`: KEEP-SUPPORT. Records model interaction coverage.
- `LLMMCPJsonValues`: KEEP-SUPPORT. JSON helper for LLM harness.
- `LLMMCPNextActions`: KEEP-SUPPORT. Interprets next-action hints for LLM harness.
- `LLMMCPSafetyValidator`: KEEP-SUPPORT. Enforces approval and safety boundaries.
- `LLMMCPSafetyValidatorTest`: KEEP-SUPPORT. Fixture-unit coverage for LLM harness safety boundaries.
- `LLMMCPToolCallValidationFailure`: KEEP-SUPPORT. Harness failure type.
- `LLMMCPToolDefinitionFactory`: KEEP-SUPPORT. Converts MCP descriptors for the LLM client.
- `LLMMCPToolDefinitionFactoryTest`: KEEP-SUPPORT. Fixture-unit coverage for LLM descriptor conversion.
- `LLME2EArtifactBundle`: KEEP-SUPPORT. Artifact data holder.
- `LLME2EArtifactWriter`: KEEP-SUPPORT. Writes LLM E2E artifacts.
- `LLME2EArtifactWriterTest`: KEEP-SUPPORT. Fixture-unit coverage for LLM artifact writing.
- `LLME2EAssertionReport`: KEEP-SUPPORT. Assertion report data holder.
- `LLMChatCompletion`: KEEP-SUPPORT. LLM client response DTO.
- `LLMChatMessage`: KEEP-SUPPORT. LLM client message DTO.
- `LLMChatModelClient`: KEEP-SUPPORT. LLM model client used by live suites.
- `LLMChatModelClientTest`: KEEP-SUPPORT. Fixture-unit coverage for the LLM client used by live suites.
- `LLMToolCall`: KEEP-SUPPORT. LLM tool-call DTO.
- `LLMRuntimeFixtureFactory`: KEEP-SUPPORT. Creates LLM runtime fixtures.
- `OllamaLLMRuntimeSupport`: KEEP-SUPPORT. Starts local LLM runtime for opt-in suites.
- `LLME2EScenario`: KEEP-SUPPORT. Scenario contract for LLM smoke tests.
- `LLMStructuredAnswer`: KEEP-SUPPORT. Expected answer model.
- `MCPBuilderEvaluationArtifactTest`: KEEP-GOV. Validates mcp-builder evaluation artifact.
- `LLMSmokeE2ETest`: KEEP-E2E. Live model smoke over MCP tools.
- `LLMSmokeScenarioFactory`: KEEP-SUPPORT. Scenario provider for LLM smoke.
- `LLMUsabilitySuiteE2ETest`: KEEP-E2E. Live model usability evidence.
- `LLMUsabilitySuiteRunner`: KEEP-SUPPORT. Runs usability scenarios.
- `LLMUsabilityDimension`: KEEP-SUPPORT. Score dimension enum.
- `LLMUsabilityMetricCalculator`: KEEP-SUPPORT. Computes model-use metrics.
- `LLMUsabilityMetricCalculatorTest`: KEEP-SUPPORT. Fixture-unit coverage for LLM usability scoring.
- `LLMUsabilityReportWriter`: KEEP-SUPPORT. Writes usability reports.
- `LLMUsabilityReportWriterTest`: KEEP-SUPPORT. Fixture-unit coverage for usability report output.
- `LLMUsabilityScenarioResult`: KEEP-SUPPORT. Scenario result DTO.
- `LLMUsabilityScorecard`: KEEP-SUPPORT. Scorecard DTO.
- `LLMUsabilityScenario`: KEEP-SUPPORT. Usability scenario contract.
- `LLMUsabilityScenarioCatalog`: KEEP-SUPPORT. Usability scenario catalog.
- `LLMUsabilityScenarioCatalogTest`: KEEP-SUPPORT. Fixture-unit coverage for the usability scenario catalog.

## Production Runtime E2E

- `AbstractProductionProxyWorkflowE2ETest`: KEEP-SUPPORT. Shared Proxy workflow runtime base.
- `AbstractProductionRuntimeE2ETest`: KEEP-SUPPORT. Shared production runtime base.
- `AbstractTransportParameterizedProductionRuntimeE2ETest`: KEEP-SUPPORT. HTTP and STDIO runtime base.
- `HttpProductionProxyEncryptWorkflowE2ETest`: KEEP-E2E. Proxy encrypt workflow product path.
- `HttpProductionProxyMaskWorkflowE2ETest`: KEEP-E2E. Proxy mask workflow product path.
- `PackagedDistributionPluginDiscoveryE2ETest`: KEEP-E2E. Packaged plugin discovery path.
- `PackagedDistributionSmokeE2ETest`: KEEP-E2E. Packaged HTTP and STDIO startup path.
- `ProductionH2AiNativeInteractionE2ETest`: KEEP-E2E. Default AI-native interaction smoke.
- `ProductionH2CapabilityDiscoveryE2ETest`: KEEP-E2E. Default discovery and catalog path.
- `ProductionH2MetadataResourceE2ETest`: KEEP-E2E. Metadata resource product path.
- `ProductionH2RuntimeSmokeE2ETest`: KEEP-SUPPORT. Abstract H2 runtime smoke base.
- `ProductionH2SQLExecutionE2ETest`: KEEP-E2E. SQL execution product path.
- `ProductionMultiDatabaseE2ETest`: KEEP-E2E. Multi-database runtime behavior.
- `ProductionMySQLRuntimeSmokeE2ETest`: KEEP-E2E. MySQL-backed runtime path.

## Programmatic HTTP Protocol E2E

- `AbstractHttpProgrammaticRuntimeE2ETest`: KEEP-SUPPORT. Shared HTTP protocol runtime base.
- `ExecuteQueryTransactionE2ETest`: KEEP-E2E. Transaction behavior and cleanup target.
- `HttpTransportAccessTokenE2ETest`: KEEP-E2E. Bearer-token HTTP gate.
- `HttpTransportApprovalSafetyE2ETest`: KEEP-E2E. Approval boundary evidence.
- `HttpTransportContractE2ETest`: KEEP-E2E. Canonical HTTP contract gate.
- `HttpTransportGoldenContractE2ETest`: REDUCE-CANDIDATE. Keep one canonical snapshot gate.
  `preserved_by`: one canonical transport-visible snapshot, `MCPDescriptorCatalogValidator`,
  and `MCPToolSpecificationFactoryTest#assertToolOutputSchemaExamplesMatchSchemas`.
- `HttpTransportOAuthIntrospectionE2ETest`: KEEP-E2E. OAuth validation gate owned by 012.
- `HttpTransportRecoveryE2ETest`: REDUCE-CANDIDATE. Prefer structured recovery fields.
  `preserved_by`: structured `error_code`, `recovery`, and `next_actions` assertions
  plus `MCPErrorConverterTest` for protocol error conversion.
- `HttpTransportSecurityE2ETest`: KEEP-E2E. Token, origin, version, and remote security gate.
- `HttpTransportSessionLifecycleE2ETest`: KEEP-E2E. Session creation and deletion target.
- `MetadataDiscoveryE2ETest`: KEEP-E2E. Metadata discovery and URI boundary target.

## Shared Assertions, Fixtures, and Runtime Support

- `OfficialMCPToolNames`: KEEP-SUPPORT. Canonical tool-name constants for E2E.
- `MCPGoldenContractAssertions`: KEEP-SUPPORT. Snapshot assertions used by contract gates.
- `MCPModelContractAssertions`: KEEP-SUPPORT. Model-facing contract assertions.
- `PackagedDistributionHttpRuntime`: KEEP-SUPPORT. Packaged HTTP runtime fixture.
- `PackagedDistributionPluginFixtureSupport`: KEEP-SUPPORT. Plugin fixture installer.
- `PackagedDistributionProcessSupport`: KEEP-SUPPORT. Packaged process runner.
- `PackagedDistributionProcessSupportTest`: KEEP-SUPPORT. Fixture-unit coverage for packaged process support.
- `PackagedDistributionTestSupport`: KEEP-SUPPORT. Distribution fixture support.
- `PackagedDistributionTestSupportTest`: KEEP-SUPPORT. Fixture-unit coverage for distribution fixture setup.
- `MCPWorkflowCustomEncryptAlgorithmFixture`: KEEP-SUPPORT. Workflow fixture algorithm.
- `MCPWorkflowCustomMaskAlgorithmFixture`: KEEP-SUPPORT. Workflow fixture algorithm.
- `PluginFixtureHandlerProvider`: KEEP-SUPPORT. Packaged plugin fixture provider.
- `PluginFixturePingToolHandler`: KEEP-SUPPORT. Packaged plugin tool fixture.
- `PluginFixtureStatusResourceHandler`: KEEP-SUPPORT. Packaged plugin resource fixture.
- `AbstractConfigBackedRuntimeE2ETest`: KEEP-SUPPORT. Shared runtime config base.
- `H2RuntimeConfigurationTestSupport`: KEEP-SUPPORT. H2 config fixture.
- `H2RuntimeTestSupport`: KEEP-SUPPORT. H2 runtime fixture.
- `MySQLRuntimeTestSupport`: KEEP-SUPPORT. MySQL runtime fixture.
- `MySQLRuntimeTestSupportTest`: KEEP-SUPPORT. Fixture-unit coverage for opt-in MySQL runtime setup.
- `ProxyEncryptWorkflowRuntimeTestSupport`: KEEP-SUPPORT. Proxy workflow runtime fixture.
- `RuntimeTransport`: KEEP-SUPPORT. Runtime transport enum.
- `MCPInteractionActionNames`: KEEP-SUPPORT. Test action-name constants.
- `MCPInteractionPayloads`: KEEP-SUPPORT. JSON-RPC payload factory.
- `MCPInteractionProtocolSupport`: KEEP-SUPPORT. JSON-RPC protocol helper.
- `MCPInteractionTraceRecord`: KEEP-SUPPORT. Trace record DTO.
- `MCPPayloadAssertions`: KEEP-SUPPORT. Shared payload assertions.
- `AbstractMCPInteractionClient`: KEEP-SUPPORT. Base MCP interaction client.
- `AbstractProcessMCPStdioInteractionClient`: KEEP-SUPPORT. Process-backed STDIO client.
- `AbstractProcessMCPStdioInteractionClientTest`: KEEP-SUPPORT. Fixture-unit coverage for STDIO client behavior.
- `MCPHttpInteractionClient`: KEEP-SUPPORT. HTTP interaction client.
- `MCPHttpTransportTestSupport`: KEEP-SUPPORT. HTTP transport helper.
- `MCPInteractionClient`: KEEP-SUPPORT. Shared interaction-client contract.
- `MCPStdioInteractionClient`: KEEP-SUPPORT. STDIO interaction client.
- `PackagedDistributionStdioInteractionClient`: KEEP-SUPPORT. Packaged STDIO client.

## Deletion and Downscope Rules

- Do not delete a test until its preserving evidence is named.
- Fixture-unit tests stay in the E2E module until a closer unit-test or harness-support module exists.
- REDUCE-CANDIDATE tests stay until a canonical contract gate replaces duplicated assertions.
- KEEP-SUPPORT classes must not be counted as product-path E2E evidence.
- KEEP-GOV classes should eventually move to a governance or artifact-validation module if one exists.
