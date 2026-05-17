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

- Assessment date: 2026-05-16.
- Original scoped baseline: **88/100**.
- Previous scoped score: **100/100** after Phase 1 through Phase 10 evidence closure.
- Current scoped score: **98/100** after the LLM runtime rebaseline reopened score-closing LLM evidence.
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
| AI usability and MCP ergonomics | 98 | 100 | Reopened for LLM smoke/usability evidence under Docker-owned `llama.cpp` server with `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`; static mcp-builder evidence remains closed. |
| Safety and approval control | 100 | 100 | Closed by approval bypass, session isolation, redaction, token/origin fail-closed, and encrypt/mask SQL safety tests. |
| Architecture cleanliness | 100 | 100 | Closed by lightweight dependency boundary test and documented feature/core/bootstrap ownership boundaries. |
| Code cleanliness | 100 | 100 | Closed by reflection, static-mock, broad assertion, and Checkstyle suppression review plus passing scoped style gate. |
| Test coverage and quality | 98 | 100 | Reopened for focused tests around the new `llama.cpp` Docker runtime boundary and runtime metadata. |
| Documentation and operations handoff | 95 | 100 | Reopened because README, workflows, and Speckit evidence must stop presenting Ollama as score-closing LLM runtime. |
| Performance and reliability evidence | 92 | 100 | Reopened until the lightweight Docker-full-package LLM lane passes locally or in GitHub Actions without external credentials or host LLM state. |

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

- T091 through T099 are newly opened because the previous Ollama score-closing route is too large for reliable local and GitHub Actions execution.
- Selected target: Docker-owned `llama.cpp` server plus `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Required closure evidence: runtime support implementation, metadata tests, README/workflow updates, focused style checks, and passing LLM smoke/usability score lane.
- Until this evidence exists, AI usability, test coverage, documentation/operations, and performance/reliability dimensions remain below 100.

### Phase 10: Final Score Closure

- T100/T104: reopened and must run again after T091 through T099.
- T101: final scoped unit, E2E, Jacoco, Checkstyle, Spotless, and packaged distribution smoke commands must be rerun after the LLM runtime rebaseline.
- T102/T103: final branch and worktree status must be verified again in the handoff.
