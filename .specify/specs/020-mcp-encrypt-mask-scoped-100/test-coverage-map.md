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

# Test Coverage Map: MCP Encrypt/Mask Scoped Scorecard 100

## Scope

This map covers the production public methods whose behavior is exercised by the current score-closing work. Tests stay on public APIs and do not invoke private production methods.

## Phase 4 AI Usability Coverage

- `MCPPromptSpecificationFactory#createPromptSpecifications`
  - Test: `MCPPromptSpecificationFactoryTest#assertCreatePromptSpecifications`.
  - Branch/path: all descriptor-backed prompt names are exposed; safe SQL prompt metadata keeps related tool declarations.
- Prompt handler returned by each `SyncPromptSpecification`
  - Tests: `assertRenderInspectMetadataPromptTemplate`, `assertRenderSafeSQLExecutionPromptTemplate`, `assertRenderRecoverWorkflowPromptTemplate`,
    `assertRenderPlanEncryptRulePromptTemplate`, `assertRenderPlanMaskRulePromptTemplate`.
  - Branch/path: optional arguments render into templates; stop/ask-user guidance remains in descriptor metadata; template resource is not leaked in result meta.
- `PlanEncryptRuleToolHandler#getToolDescriptor`
  - Test: `EncryptToolHandlerTest#assertGetPlanEncryptRuleToolDescriptor`.
- `PlanEncryptRuleToolHandler#handle`
  - Tests: `assertHandlePlanEncryptRule`, `assertHandlePlanEncryptRuleWithMaskedArtifacts`.
  - Branch/path: direct args, structured intent evidence, user overrides, secret masking, derived column payload, resources to read, and preview next action arguments.
- `PlanMaskRuleToolHandler#getToolDescriptor`
  - Test: `MaskToolHandlerTest#assertGetPlanMaskRuleToolDescriptor`.
- `PlanMaskRuleToolHandler#handle`
  - Tests: `assertHandlePlanMaskRule`, `assertHandlePlanMaskRuleWithMaskedArtifacts`.
  - Branch/path: direct args, structured intent evidence, user overrides, property preview, resources to read, and preview next action arguments.
- `mcp-builder-evaluation.xml`
  - Test: `MCPBuilderEvaluationArtifactTest#assertMCPBuilderEvaluationArtifact`.
  - Negative tests: `assertRejectsShallowEvaluationQuestion`, `assertRejectsDestructiveEvaluationQuestion`, `assertRejectsUnverifiableEvaluationAnswer`.

## Phase 5 Safety Coverage

- `WorkflowExecutionService#apply`
  - Tests: `WorkflowExecutionServiceTest#assertApplyRejectsDifferentSession`, `assertApplyRejectsInvalidLifecycleStatus`, `assertApplyRejectsMissingExecutionMode`,
    `assertApplyRejectsUnsupportedExecutionMode`, `assertApplyRejectsUnsupportedApprovedStep`, `assertApplyRejectsUnapprovedExecution`,
    `assertApplyPreviewDoesNotExecuteOrChangeLifecycle`, `assertApplyExecutesApprovedArtifacts`, `assertApplySkipsUnapprovedArtifacts`.
- HTTP approval and session boundaries
  - Tests: `HttpTransportApprovalSafetyE2ETest#assertRejectExecuteUpdateExecutionWithoutApproval`, `assertPreviewExecuteUpdateWithoutExecution`,
    `assertRejectWorkflowExecutionWithoutApproval`, `assertApplyWorkflowSafeAndApprovedModes`, `assertRejectWorkflowApprovalFromOtherSession`.
  - Tests: `HttpTransportCompletionE2ETest#assertCompleteWorkflowPlanIdsWithinCurrentSession`.
  - Tests: `HttpTransportSessionLifecycleE2ETest#assertRejectOpenStreamAfterDelete`, `assertDeleteKeepsOtherSession`.
- Authorization and origin fail-closed
  - Tests: `HttpTransportAccessTokenE2ETest`, `HttpTransportOAuthIntrospectionE2ETest`, `HttpTransportSecurityE2ETest`,
    `HttpBearerAuthorizationHandlerTest`, `OAuthTokenValidatorTest`, and origin constraint tests under `mcp/bootstrap`.
- Redaction
  - Tests: `WorkflowExecutionServiceTest#assertApplyMasksManualArtifactPackage`, `HttpTransportRecoveryE2ETest#assertWorkflowRecoveryRedactsEncryptSecret`,
    `PackagedDistributionSmokeE2ETest#assertRuntimeStatusSecretSafe`, `HttpTransportContractE2ETest#assertRuntimeStatusRedactsSecrets`.
- SQL identifier and literal safety
  - Tests: `PhysicalDDLPlanningServiceTest#assertPlanAddColumnArtifactsRejectsUnsupportedTableIdentifier`,
    `PhysicalDDLPlanningServiceTest#assertPlanAddColumnArtifactsRejectsUnsupportedColumnIdentifier`,
    `EncryptRuleDistSQLPlanningServiceTest#assertPlanEncryptRuleRejectsUnsupportedIdentifier`,
    `MaskRuleDistSQLPlanningServiceTest#assertPlanMaskRuleRejectsUnsupportedIdentifier`.

## Phase 8 Performance and Quality Coverage

- `MCPPerformanceBudgetSmokeTest#assertDescriptorGenerationBudget`: descriptor generation budget.
- `MCPPerformanceBudgetSmokeTest#assertRequestScopeCreationBudget`: request-scope creation budget.
- `MCPPerformanceBudgetSmokeTest#assertMetadataSearchBudget`: metadata search budget.
- `MCPPerformanceBudgetSmokeTest#assertWorkflowPlanPayloadBudget`: workflow planning payload budget.
- `MCPPerformanceBudgetSmokeTest#assertWorkflowPlanIdCompletionBudget`: completion budget for workflow `plan_id`.
- `MCPPerformanceBudgetSmokeTest#assertSQLClassifierBudget`: SQL classification budget.

## Unreachable or Intentionally Unscored Paths

- MCP icons and `Tool.execution` are descriptor validation non-goals for this checkpoint.
- Non-`2025-11-25` protocol compatibility remains product compatibility, not score-closing coverage.
- Non-encrypt/mask workflows are outside this scoped functional scorecard.
- Private helper branches are covered only through public method inputs or documented as implementation detail; tests do not use private-method reflection.
