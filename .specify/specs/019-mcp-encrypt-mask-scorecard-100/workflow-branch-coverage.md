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

# Workflow Branch Coverage: Encrypt and Mask

## Scope

- Package: `.specify/specs/019-mcp-encrypt-mask-scorecard-100`.
- Branch constraint: stay on `001-shardingsphere-mcp`; no branch-changing Speckit or Git command is needed.
- Functional scope: encrypt and mask workflows only.
- Contract boundary: feature planning services expose `plan(...)`; feature validation services expose `validate(...)`.
- Shared boundary: `WorkflowPlanningSupport` owns logical context validation; feature services own rule lifecycle, algorithm, artifact, and validation decisions.

## Verification

- Test command:

```bash
./mvnw -pl mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true \
  -Dtest=EncryptWorkflowPlanningServiceTest,MaskWorkflowPlanningServiceTest,EncryptWorkflowValidationServiceTest,MaskWorkflowValidationServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```
- Exit code: `0`.
- Time: 2026-05-15 15:45 Asia/Shanghai.
- Checkstyle command:

```bash
./mvnw -pl mcp/features/encrypt,mcp/features/mask -Pcheck -DskipTests -DskipITs checkstyle:check
```

- Exit code: `0`.
- Spotless command:

```bash
./mvnw -pl mcp/features/encrypt,mcp/features/mask -Pcheck -DskipTests -DskipITs spotless:check
```

- Exit code: `0`.

## T020 Encrypt Branch Map

- Create artifact path:
  `EncryptWorkflowPlanningServiceTest.assertPlanCreatesArtifactsWithoutIndexDdl`.
- Alter lifecycle inference:
  `EncryptWorkflowPlanningServiceTest.assertPlanWithNaturalLanguageInference`, case `alter from english verb`.
- Drop limitation warnings:
  `EncryptWorkflowPlanningServiceTest.assertPlanDropWorkflow`.
- Algorithm missing:
  `EncryptWorkflowPlanningServiceTest.assertPlanStopsOnBlockingAlgorithmIssue`.
- Assisted query:
  `EncryptWorkflowPlanningServiceTest.assertPlanInfersEncryptCapabilitiesFromNaturalLanguage`.
- Like query:
  `EncryptWorkflowPlanningServiceTest.assertPlanAppliesLikeQueryAlgorithmCandidate`.
- Existing rule conflict:
  `EncryptWorkflowPlanningServiceTest.assertPlanRejectsLifecycleMismatchForCreate`.
- Metadata unavailable:
  `EncryptWorkflowPlanningServiceTest.assertPlanWarnsWhenColumnDefinitionUnavailable`.
- Validation failure:
  `EncryptWorkflowValidationServiceTest.assertValidateWhenRuleMissing`.
- Successful validation:
  `EncryptWorkflowValidationServiceTest.assertValidateHappyPath`.

## T021 Mask Branch Map

- Create artifact path:
  `MaskWorkflowPlanningServiceTest.assertPlanCreatesRuleArtifact`.
- Alter lifecycle inference:
  `MaskWorkflowPlanningServiceTest.assertPlanWithNaturalLanguageInference`, case `alter from english verb`.
- Drop:
  `MaskWorkflowPlanningServiceTest.assertPlanDropWorkflow`.
- Algorithm missing:
  `MaskWorkflowPlanningServiceTest.assertPlanStopsOnBlockingAlgorithmIssue`.
- Field semantics omitted by caller:
  `MaskWorkflowPlanningServiceTest.assertPlanInfersMissingFieldSemanticsFromColumn`.
- Existing rule conflict:
  `MaskWorkflowPlanningServiceTest.assertPlanRejectsLifecycleMismatchForCreate`.
- Metadata unavailable:
  `MaskWorkflowPlanningServiceTest.assertPlanRejectsMissingLogicalColumnMetadata`.
- Validation failure:
  `MaskWorkflowValidationServiceTest.assertValidateWhenAlgorithmMismatch`.
- Successful validation:
  `MaskWorkflowValidationServiceTest.assertValidateHappyPath`.

## T022 and T023 Evidence Inventory

- Encrypt resources:
  `EncryptResourceHandlerTest`, `EncryptMCPHandlerProviderTest`.
- Encrypt prompts, completions, and descriptor validation:
  `EncryptToolDescriptorValidatorTest`, `EncryptAlgorithmCompletionProviderTest`.
- Encrypt planning, apply, validation, and recovery payload boundaries:
  `EncryptWorkflowPlanningServiceTest`, `EncryptToolHandlerTest`, `WorkflowExecutionServiceTest`, `EncryptWorkflowValidationServiceTest`.
- Mask resources:
  `MaskResourceHandlerTest`, `MaskMCPHandlerProviderTest`.
- Mask prompts, completions, and descriptor validation:
  `MaskToolDescriptorValidatorTest`, `MaskAlgorithmCompletionProviderTest`.
- Mask planning, apply, validation, and recovery payload boundaries:
  `MaskWorkflowPlanningServiceTest`, `MaskToolHandlerTest`, `WorkflowExecutionServiceTest`, `MaskWorkflowValidationServiceTest`.

T022 and T023 product-path evidence is tracked in `product-path-evidence.md`.
T024 and T060 through T063 are closed by the final E2E, unit, Checkstyle, and Spotless evidence recorded in `e2e-llm-operations-evidence.md`.
