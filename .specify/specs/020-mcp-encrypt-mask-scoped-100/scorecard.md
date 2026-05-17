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

# Scorecard: MCP Encrypt/Mask Scoped Scorecard 100

## Scoring Rule

Every dimension targets **100/100**. A dimension can reach 100 only when all scoped gaps are closed with evidence. Non-goals do not reduce the score.

## Current Baseline

- Assessment date: 2026-05-17.
- Original scoped baseline: **88/100**.
- Previous scoped score: **100/100** after Phase 1 through Phase 10 evidence closure.
- Current scoped score: **100/100** after the Docker-owned `llama.cpp` LLM runtime rebaseline passed focused unit, smoke, usability, and style evidence.
- Protocol scope: MCP `2025-11-25` only.
- SDK scope: MCP Java SDK `1.1.2`, fixed.
- Functional scope: encrypt and mask workflows only.
- Elegance rule: readability and clear structure outrank broad abstraction.

## Explicit Non-Goals

- MCP `icons` and `Tool.execution`.
- SDK upgrade or dependency version change.
- Protocol compatibility proof for revisions other than `2025-11-25`.
- Sharding, readwrite-splitting, shadow, traffic governance, mode governance, observability, and general administration.
- Data migration, backfill, rollback orchestration, and persistent audit storage.

## Active Dimensions

| Dimension | Current | Target | Closing evidence needed |
| --- | ---: | ---: | --- |
| MCP protocol conformity | 100 | 100 | Closed by contract tests for declared `2025-11-25` methods, SDK `1.1.2` scope documentation, structured content/output schema checks, and transport/session negative cases. |
| Encrypt/mask functional completeness | 100 | 100 | Closed by branch matrices plus unit and Proxy E2E evidence for resources, prompts, completions, plan, preview, approval apply, validation, and recovery for encrypt and mask. |
| Implementation elegance | 100 | 100 | Closed by readability-first handler response assertions, prompt rendering tests, performance smoke coverage, and explicit rejection of broad framework extraction. |
| AI usability and MCP ergonomics | 100 | 100 | Closed by mcp-builder artifact validation plus LLM smoke/usability evidence under Docker-owned `llama.cpp` server with `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`. |
| Safety and approval control | 100 | 100 | Closed by approval bypass, session isolation, redaction, token/origin fail-closed, and encrypt/mask SQL safety tests. |
| Architecture cleanliness | 100 | 100 | Closed by lightweight dependency boundary test and documented feature/core/bootstrap ownership boundaries. |
| Code cleanliness | 100 | 100 | Closed by reflection, static-mock, broad assertion, and Checkstyle suppression review plus passing scoped style gate. |
| Test coverage and quality | 100 | 100 | Closed by focused public-method tests around the `llama.cpp` Docker runtime boundary, runtime metadata, chat client readiness, and conversation recovery branches. |
| Documentation and operations handoff | 100 | 100 | Closed by README, workflow, and Speckit evidence alignment that treats Ollama as rejected historical evidence and `llama.cpp` Qwen3 Q4_K_M as score-closing. |
| Performance and reliability evidence | 100 | 100 | Closed by local Docker-full-package smoke/usability lanes without external credentials, host LLM install, or operator-managed model endpoint. |

## Evidence Policy

Valid evidence:

- Scoped Maven command with exit code.
- Checkstyle, Spotless, or Jacoco report.
- E2E or LLM evaluation artifact. LLM score evidence must record Docker-owned `llama.cpp` server runtime usage with `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Source map to official MCP `2025-11-25` documentation or local SDK `1.1.2` behavior.
- Explicit non-goal decision captured in `spec.md`, `scorecard.md`, and README.

Invalid evidence:

- Historical 100/100 claims without current revalidation.
- Average score alone.
- Generated `target/` content as a source edit.
- Claims for optional MCP capabilities that are not declared, implemented, or intentionally excluded.

## Score Closure Rule

- A task may be marked complete only after its evidence is recorded in `tasks.md` or a linked evidence file.
- A dimension may be moved to 100 only after every task mapped to that dimension is complete.
- Final closure requires `git branch --show-current` to remain `001-shardingsphere-mcp`.

## Evidence Ledger

### Phase 1: Scoped Baseline and Governance

- T001: `git branch --show-current` returned `001-shardingsphere-mcp` with exit code `0`.
- T002: `mcp/README.md` and `mcp/README_ZH.md` already state SDK `1.1.2`, MCP `2025-11-25`, icons/`Tool.execution` non-goals, and the narrowed public MCP surface.
- T003: historical `.specify/specs/019-mcp-encrypt-mask-scorecard-100/` files are retained as previous evidence only; they do not automatically close this scoped package.
- T004: this ledger records every current score dimension and its closing evidence state before further score movement.

### Phase 2: MCP Protocol Conformity

- T010: `MCPSyncServerFactoryTest` verifies declared resource, resource template, tool, prompt, and completion capabilities exposed through SDK `1.1.2`.
- T011: `StreamableHttpMCPServletTest`, `StreamableHttpMCPServerWireTest`, `ProtocolVersionHeaderConstraintTest`, and `ShardingSphereServerTransportSecurityValidatorTest`
  verify POST, GET, DELETE, session id, negotiated protocol header, missing protocol header, and unsupported HTTP content type behavior.
- T012: `MCPTransportPayloadUtilsTest` verifies schema-conforming `structuredContent` plus serialized JSON text fallback for tool result payloads.
- T013: `MCPDescriptorCatalogValidatorTest` and `MCPDescriptorYamlKeyValidatorTest` verify scoped non-goal fields are not required, unsupported `icons` and `Tool.execution`
  descriptor keys are rejected, and legacy public alias fields remain rejected.
- T014: `source-map.md` records that non-`2025-11-25` compatibility tests are not score-closing evidence for this package.
- Verification: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest=MCPSyncServerFactoryTest,StreamableHttpMCPServletTest,StreamableHttpMCPServerWireTest,MCPTransportPayloadUtilsTest,ProtocolVersionHeaderConstraintTest,ShardingSphereServerTransportSecurityValidatorTest,MCPDescriptorCatalogValidatorTest,MCPDescriptorYamlKeyValidatorTest test` exited `0`.
- Regression verification: `./mvnw -pl mcp/support,mcp/bootstrap -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`.
- Coverage verification: `./mvnw -pl mcp/support,mcp/bootstrap -DskipITs -Dspotless.skip=true -Djacoco.skip=false -Dsurefire.failIfNoSpecifiedTests=false test jacoco:report` exited `0`;
  `StreamableHttpMCPServlet` has JaCoCo `METHOD missed=0 covered=24`, `BRANCH missed=0 covered=18`, and `LINE missed=0 covered=66`.
- Style verification: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check` exited `0`.

### Phase 3: Encrypt/Mask Functional Completeness

- T020: `workflow-coverage.md` records the encrypt branch matrix for create, alter, drop non-goal cleanup, missing algorithm, missing properties,
  assisted query, LIKE query conflict, rule conflict, missing metadata, validation failure, and validation success.
- T021: `workflow-coverage.md` records the mask branch matrix for create, alter, drop, missing algorithm, missing field semantics,
  existing rule conflict, missing metadata, validation failure, recovery, and validation success.
- T022: existing encrypt descriptor, resource, completion, handler, planning, validation, and DistSQL tests are mapped in `workflow-coverage.md`.
- T023: existing mask descriptor, resource, completion, handler, planning, validation, and DistSQL tests are mapped in `workflow-coverage.md`.
- T024: encrypt preview, approval apply, validation, partial-apply failure, and recovery coverage is mapped through `WorkflowExecutionServiceTest`,
  `EncryptWorkflowValidationServiceTest`, and `HttpProductionProxyEncryptWorkflowE2ETest`.
- T025: mask preview, approval apply, validation, drop, and recovery coverage is mapped through `WorkflowExecutionServiceTest`,
  `MaskWorkflowValidationServiceTest`, and `HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy`.
- T026: Proxy-backed product-path E2E coverage exists for encrypt and mask workflows; approved-step recovery was verified through Docker/Testcontainers.
- Reliability closure: `MySQLRuntimeTestSupport` now waits for the MySQL `ready for connections` log on port `3306`, so Proxy-backed E2E starts only after JDBC can accept connections.
- Verification attempt: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `1` because dependency modules were not rebuilt with the selected module.
- Verification: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; after the MySQL readiness fix, it ran 1 test with 0 failures, 0 errors, and 0 skipped tests through local Docker/Testcontainers MySQL, embedded ShardingSphere-Proxy, and HTTP MCP.
- Verification attempt: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `1` because dependency modules were not rebuilt with the selected module.
- Verification: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; after the MySQL readiness fix, it ran 1 test with 0 failures, 0 errors, and 0 skipped tests through local Docker/Testcontainers MySQL, embedded ShardingSphere-Proxy, and HTTP MCP.
- Scoped unit verification: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`.
- Scoped style verification: `./mvnw -pl mcp/support,mcp/core,test/e2e/mcp -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check`
  exited `0`.

### Phase 4: AI Usability and MCP Ergonomics

- T030: `mcp-builder-evaluation.xml` now contains 10 read-only, independent, complex, realistic, verifiable encrypt/mask questions with stable answer keys.
- T031: `MCPBuilderEvaluationArtifactTest` validates stable answer keys and rejects shallow exact-name questions, destructive questions, and unverifiable answer keys.
- T032: `EncryptToolHandlerTest` and `MaskToolHandlerTest` assert structured `resources_to_read`, `next_actions.tool_name`, and `next_actions.arguments`.
- T033: `MCPPromptSpecificationFactoryTest` asserts inspect, safe SQL execution, recovery, plan encrypt, and plan mask prompt rendering without broad string matching.
- Verification: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=MCPBuilderEvaluationArtifactTest -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`; 4 tests, 0 failures, 0 errors, 0 skipped.
- Verification: `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=MCPPromptSpecificationFactoryTest,MCPArchitectureBoundaryTest,MCPDocumentationContractTest -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`; 10 tests, 0 failures, 0 errors.
- Verification: `./mvnw -pl mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dtest=EncryptToolHandlerTest,MaskToolHandlerTest,EncryptRuleDistSQLPlanningServiceTest,MaskRuleDistSQLPlanningServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`; 18 tests, 0 failures, 0 errors.

### Phase 5: Safety and Approval Control

- T040: `WorkflowExecutionServiceTest` and `HttpTransportApprovalSafetyE2ETest` cover unapproved apply rejection, preview non-execution, unsupported execution mode, invalid lifecycle, approved-step handling, and cross-session approval rejection.
- T041: `HttpTransportSessionLifecycleE2ETest` and `HttpTransportCompletionE2ETest` cover DELETE cleanup, preserved unrelated sessions, and session-scoped plan completion.
- T042: `HttpTransportRecoveryE2ETest`, workflow service tests, and packaged runtime status checks cover redaction for recovery payloads, manual artifacts, runtime diagnostics, and logs-facing payloads.
- T043: `HttpTransportAccessTokenE2ETest` and `HttpTransportSecurityE2ETest` cover missing/invalid token, origin rejection, protected resource metadata, and no token passthrough.
- T044: `EncryptRuleDistSQLPlanningServiceTest`, `MaskRuleDistSQLPlanningServiceTest`, and physical DDL planning tests cover unsupported identifier rejection and literal-safe DistSQL planning.
- Verification: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=MCPBuilderEvaluationArtifactTest,HttpTransportApprovalSafetyE2ETest,HttpTransportSessionLifecycleE2ETest,HttpTransportSecurityE2ETest,HttpTransportAccessTokenE2ETest,HttpTransportCompletionE2ETest,HttpTransportRecoveryE2ETest -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`; 39 tests, 0 failures, 0 errors.

### Phase 6: Architecture Cleanliness and Implementation Elegance

- T050: No new broad payload framework was added; repeated response expectations are protected by structured public tests around existing readable handlers.
- T051/T052: `MCPArchitectureBoundaryTest` scans generic MCP production modules and fails if bootstrap/core/support/api import encrypt or mask feature packages.
- T053: stale `next_actions` aliases were removed from docs and remain rejected by descriptor validators; no legacy compatibility shim was retained for the scoped contract.
- T054: `architecture-evidence.md` records the boundary, local-cleanup decision, and why broader extraction would be over-design.

### Phase 7: Code Cleanliness

- T060: `rg "getDeclaredMethod|setAccessible|invoke\\(" mcp test/e2e/mcp` found no private-method reflection or `setAccessible`; remaining public `getMethod` calls use `Plugins.getMemberAccessor()`.
- T061: `rg "mockStatic|mockConstruction" mcp test/e2e/mcp` was reviewed; remaining direct static/constructor mocks are documented bounded exceptions where migration would add wider dependencies or reduce readability.
- T062: touched prompt, encrypt, mask, and mcp-builder tests use structured assertions instead of broad `containsString`.
- T063: `rg "CHECKSTYLE:OFF|CHECKSTYLE:ON" mcp test/e2e/mcp` was reviewed and documented in `code-cleanliness-evidence.md`.
- T064: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check` exited `0`.

### Phase 8: Test Coverage and Quality

- T070/T071: `test-coverage-map.md` maps touched public production methods and branch paths to owning tests and documents intentionally unscored paths.
- T072: scoped MCP unit verification commands for bootstrap/core/encrypt/mask completed with exit code `0`.
- T073: `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Djacoco.skip=false -Dtest=MCPPromptSpecificationFactoryTest,MCPArchitectureBoundaryTest,MCPDocumentationContractTest,MCPPerformanceBudgetSmokeTest,WorkflowExecutionServiceTest,MCPErrorConverterTest,WorkflowPlanIdCompletionProviderTest,EncryptToolHandlerTest,MaskToolHandlerTest,EncryptRuleDistSQLPlanningServiceTest,MaskRuleDistSQLPlanningServiceTest -Dsurefire.failIfNoSpecifiedTests=false test jacoco:report` exited `0`.
- T074: default MCP security/artifact lane exited `0`; report-owning tests are listed in `e2e-evidence.md`.

### Phase 9: Documentation, Operations, Performance

- T080: `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md` now uses canonical `next_actions.type`, `tool_name`, `resource_uri`, and `arguments` names.
- T081: `quickstart.md`, `mcp/README.md`, and `mcp/README_ZH.md` point encrypt/mask workflow reproduction to discover, plan, preview, approve apply, validate, and recover.
- T082: `performance-budget.md` records budgets for descriptor loading, metadata search, workflow planning, completion, default E2E, and distribution smoke.
- T083: `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dmcp.e2e.distribution.enabled=true -Dtest=PackagedDistributionSmokeE2ETest -Dsurefire.failIfNoSpecifiedTests=false test` exited `0`; 2 tests, 0 failures, 0 errors, 0 skipped.
- T084/T085: `e2e-evidence.md` separates default and opt-in lanes and documents Docker/Testcontainers prerequisites for Proxy/MySQL, STDIO, distribution, and LLM lanes.

### Phase 9A: Reopened LLM Runtime Rebaseline

- T091 through T099 reopened the previous Ollama score-closing route because its image was too large for reliable local and GitHub Actions execution.
- Selected target: Docker-owned `llama.cpp` server plus `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- T091/T092: `llm-docker-runtime-analysis.md`, `llm-runtime-rebaseline-design.md`, `spec.md`, and `plan.md` lock the minimal OpenAI-compatible boundary and reject host LLM installs, external credentials, and Ollama score closure.
- T093/T094: `LLMRuntimeSupport` starts the test-owned `llama.cpp` server image, readiness-checks `/v1/models`, JSON response mode, and tool-choice behavior, and never reuses external endpoints in score mode.
- T095: `LLME2EArtifactWriterTest` verifies runtime metadata and secret redaction for score-closing artifacts.
- T096: `mcp/README.md`, `mcp/README_ZH.md`, GitHub workflows, and this Speckit package now present `llama.cpp` plus Qwen3 Q4_K_M as the score-closing runtime.
- T097/T098: `./mvnw -pl mcp/bootstrap,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false -Dtest=LLME2EConfigurationTest,LLMRuntimeSupportTest,LLME2EArtifactWriterTest,LLMChatModelClientTest,LLMMCPConversationRunnerTest,LLMUsabilityScenarioCatalogTest test`
  exited `0`; 78 tests, 0 failures, 0 errors, 0 skipped.
- T098 style: `./mvnw -pl mcp/core,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check`
  exited `0`.
- T099 smoke: `./mvnw -pl mcp/bootstrap,test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true -Dtest=LLMSmokeE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; 4 tests, 0 failures, 0 errors, 0 skipped; run id `20260517210907-f484ebb5`.
- T099 usability: `./mvnw -pl mcp/bootstrap,test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilitySuiteE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; 1 suite test, 0 failures, 0 errors, 0 skipped; run id `20260517210034-81719143`.
- Usability scorecards for run `20260517210034-81719143` record `overallScore=100.0`, `fullScore=true`, `invalidCallRate=0.0`, `boundaryConfusionRate=0.0`,
  `approvalViolationRate=0.0`, `nativeToolCallRate=1.0`, and `harnessRecoveryRate=0.0` for both `llm-usability-h2/core` and `llm-usability-h2/extended`.
- Smoke and usability `run-context.json` artifacts record `runtimeMode=docker`, `dockerOwned=true`, `serverRuntime=llama.cpp`,
  `serverImage=apache/shardingsphere-mcp-llm-runtime:local`, local image ID `sha256:3379ed38c3cc229afdc5b758527666c0e2bcef4cf4f9756978a45ebb4b6b3a71`,
  base arm64 image digest `sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca`,
  model `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`, model size `1282439264`, model revision `daeb8e2d528a760970442092f6bf1e55c3b659eb`,
  model SHA-256 `d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5`, `modelPackaging=prepackaged`, and `scoreClosing=true`.

### Phase 10: Final Score Closure

- T100/T104: all ten score dimensions are updated to `100/100` after T091 through T099 evidence passed.
- T101: final scoped unit, E2E, Checkstyle, and Spotless commands passed after the LLM runtime rebaseline.
- T102/T103: final branch and worktree status are verified in the handoff; branch remains `001-shardingsphere-mcp`.
