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

# Workflow Coverage: Encrypt and Mask

## Scope

This matrix closes the Phase 3 functional-completeness slice for encrypt and mask only.
It does not score MCP icons, `Tool.execution`, SDK upgrades, non-`2025-11-25` compatibility, or non-encrypt/mask features.

## MCP Builder Check

- Tool boundaries remain workflow-oriented and discoverable: plan tools create reviewable plans, shared apply/validate tools execute and verify only by `plan_id`.
- Side-effecting apply remains explicit: `execution_mode=preview` shows review payloads, and `execution_mode=review-then-execute` requires `approved_by_user=true`.
- Tool responses keep machine-readable payloads for agents: `next_actions`, `resources_to_read`, `review_focus`, `masked_property_preview`, artifacts, validation sections, and mismatch payloads.
- Errors stay actionable: planning issues carry codes, stage, message, user action, and retryable metadata; validation failure returns recovery guidance and next actions.
- No broad abstraction was added for Phase 3. The only new code path is a product-path E2E that proves an existing mask workflow recovery branch.

## Encrypt Branch Matrix

| Branch or path | Owning evidence | Status |
| --- | --- | --- |
| Create lifecycle success | `EncryptWorkflowPlanningServiceTest#assertPlanCreatesArtifactsWithoutIndexDdl`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanApplyAndValidateEncryptWorkflowThroughProxy` | Covered |
| Alter lifecycle success | `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanApplyAndValidateEncryptAlterWorkflowThroughProxy` | Covered |
| Drop lifecycle success and non-goal physical cleanup warning | `EncryptWorkflowPlanningServiceTest#assertPlanDropWorkflow`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanApplyAndValidateEncryptDropWorkflowThroughProxy` | Covered |
| Drop target missing | `WorkflowPlanningSupportTest#assertEnsureLifecycleState` | Covered |
| Missing algorithm | `EncryptWorkflowPlanningServiceTest#assertPlanStopsOnBlockingAlgorithmIssue` | Covered |
| Missing algorithm property | `EncryptWorkflowPlanningServiceTest#assertPlanRequiresMissingProperties`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanRecommendsAssistedQueryEncryptWorkflowThroughProxy` | Covered |
| Assisted query recommendation | `EncryptWorkflowPlanningServiceTest#assertPlanInfersEncryptCapabilitiesFromNaturalLanguage`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanRecommendsAssistedQueryEncryptWorkflowThroughProxy` | Covered |
| LIKE query capability conflict | `EncryptWorkflowPlanningServiceTest#assertPlanAppliesLikeQueryAlgorithmCandidate`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanReportsLikeQueryCapabilityConflictThroughProxy` | Covered |
| Rule conflict on create | `EncryptWorkflowPlanningServiceTest#assertPlanRejectsLifecycleMismatchForCreate` | Covered |
| Missing database, table, column, or unsafe identifier context | `EncryptWorkflowPlanningServiceTest#assertPlanRejectsMissingPlanningContext`, `WorkflowPlanningSupportTest#assertEnsurePlanningContextRejectsMissingDatabase`, `WorkflowPlanningSupportTest#assertEnsurePlanningContextRejectsUnsupportedIdentifier` | Covered |
| Logical metadata unavailable while deriving physical DDL | `EncryptWorkflowPlanningServiceTest#assertPlanWarnsWhenColumnDefinitionUnavailable` | Covered |
| Validation success | `EncryptWorkflowValidationServiceTest#assertValidateHappyPath`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanApplyAndValidateEncryptWorkflowThroughProxy` | Covered |
| Validation failure after partial apply | `EncryptWorkflowValidationServiceTest#assertValidateWhenRuleMissing`, `HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy` | Covered |
| Preview and approval apply | `WorkflowExecutionServiceTest#assertApplyPreviewDoesNotExecuteOrChangeLifecycle`, `WorkflowExecutionServiceTest#assertApplyExecutesApprovedArtifacts` | Covered |
| Manual-only apply | `WorkflowExecutionServiceTest#assertApplyMasksManualArtifactPackage`, `HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsManualOnlyExecutionModeThroughProxy` | Covered |
| Resource readback and custom algorithm | `EncryptResourceHandlerTest`, `HttpProductionProxyEncryptWorkflowE2ETest#assertPlanApplyValidateAndReadEncryptResourcesWithCustomAlgorithmThroughProxy` | Covered |
| Completion | `EncryptAlgorithmCompletionProviderTest`, `HttpProductionProxyEncryptWorkflowE2ETest#assertCompleteEncryptAlgorithmThroughProxy` | Covered |

## Mask Branch Matrix

| Branch or path | Owning evidence | Status |
| --- | --- | --- |
| Create lifecycle success | `MaskWorkflowPlanningServiceTest#assertPlanCreatesRuleArtifact`, `HttpProductionProxyMaskWorkflowE2ETest#assertPlanApplyAndValidateMaskCreateAlterWorkflowThroughProxy` | Covered |
| Alter lifecycle success | `HttpProductionProxyMaskWorkflowE2ETest#assertPlanApplyAndValidateMaskCreateAlterWorkflowThroughProxy` | Covered |
| Drop lifecycle success | `MaskWorkflowPlanningServiceTest#assertPlanDropWorkflow`, `MaskWorkflowValidationServiceTest#assertValidateDropWorkflowAfterRuleRemoval`, `HttpProductionProxyMaskWorkflowE2ETest#assertPlanApplyAndValidateMaskDropWorkflowThroughProxy` | Covered |
| Drop target missing | `WorkflowPlanningSupportTest#assertEnsureLifecycleState` | Covered |
| Missing algorithm | `MaskWorkflowPlanningServiceTest#assertPlanStopsOnBlockingAlgorithmIssue` | Covered |
| Missing field semantics | `MaskWorkflowPlanningServiceTest#assertPlanWithNaturalLanguageInference` | Covered |
| Field semantics inferred from column | `MaskWorkflowPlanningServiceTest#assertPlanInfersMissingFieldSemanticsFromColumn` | Covered |
| Missing algorithm property | `MaskWorkflowPlanningServiceTest#assertPlanRequiresMissingProperties`, `HttpProductionProxyMaskWorkflowE2ETest#assertPlanRecommendApplyAndValidateMaskWorkflowFromNaturalLanguageThroughProxy` | Covered |
| Existing rule conflict on create | `MaskWorkflowPlanningServiceTest#assertPlanRejectsLifecycleMismatchForCreate` | Covered |
| Metadata unavailable or missing target column | `MaskWorkflowPlanningServiceTest#assertPlanRejectsMissingLogicalColumnMetadata` | Covered |
| Validation success | `MaskWorkflowValidationServiceTest#assertValidateHappyPath`, `HttpProductionProxyMaskWorkflowE2ETest#assertPlanApplyAndValidateMaskCreateAlterWorkflowThroughProxy` | Covered |
| Validation failure after partial apply | `MaskWorkflowValidationServiceTest#assertValidateWhenAlgorithmMismatch`, `HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy` | Covered |
| Preview and approval apply | `WorkflowExecutionServiceTest#assertApplyPreviewDoesNotExecuteOrChangeLifecycle`, `WorkflowExecutionServiceTest#assertApplyExecutesApprovedArtifacts` | Covered |
| Recovery by applying skipped rule step | `HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy` | Covered |
| Resource readback and custom algorithm | `MaskResourceHandlerTest`, `HttpProductionProxyMaskWorkflowE2ETest#assertPlanApplyValidateAndReadMaskResourcesWithCustomAlgorithmThroughProxy` | Covered |
| Completion | `MaskAlgorithmCompletionProviderTest`, `HttpProductionProxyMaskWorkflowE2ETest#assertCompleteMaskAlgorithmThroughProxy` | Covered |

## Product-Path Evidence

- `MySQLRuntimeTestSupport` waits for the MySQL `ready for connections` log on port `3306`; this avoids a false-positive Testcontainers port-listening state before JDBC accepts connections.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `1` because dependency modules were not rebuilt with the selected module.
- `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; after the MySQL readiness fix, the test ran through Docker/Testcontainers MySQL, embedded ShardingSphere-Proxy, HTTP MCP, plan, skipped apply, failed validation, recovery apply, and passed validation.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `1` because dependency modules were not rebuilt with the selected module.
- `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyMaskWorkflowE2ETest#assertApplySupportsApprovedStepsThroughProxy -Dsurefire.failIfNoSpecifiedTests=false test`
  exited `0`; after the MySQL readiness fix, the test ran through Docker/Testcontainers MySQL, embedded ShardingSphere-Proxy, HTTP MCP, plan, skipped apply, failed validation, recovery apply, and passed validation.
