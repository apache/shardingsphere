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

# Product Path Evidence: Encrypt and Mask

## Scope

- Package: `.specify/specs/019-mcp-encrypt-mask-scorecard-100`.
- Completed tasks: T022 and T023.
- Functional scope: encrypt and mask workflows only.
- Branch constraint: stay on `001-shardingsphere-mcp`; no branch-changing command is needed.
- Score constraint: this evidence does not move any dimension to 100 until T024 and final verification gates pass.

## API Boundary

- Feature modules own encrypt/mask resources, prompts, completions, descriptors, planning, and validation semantics.
- Core owns shared workflow apply, preview, approval, validation dispatch, session isolation, and recovery payload semantics.
- Bootstrap owns MCP Java SDK surfaces for tools, resources, prompts, completions, discovery descriptors, and structured tool-result payloads.
- Tests are mapped at public entry points so SDK transport behavior, core workflow behavior, and feature-specific behavior stay separated.

## Verification

- Test command:

```bash
TESTS=(
  EncryptResourceHandlerTest
  EncryptMCPHandlerProviderTest
  EncryptToolDescriptorValidatorTest
  EncryptAlgorithmCompletionProviderTest
  EncryptToolHandlerTest
  EncryptWorkflowPlanningServiceTest
  EncryptWorkflowValidationServiceTest
  MaskResourceHandlerTest
  MaskMCPHandlerProviderTest
  MaskToolDescriptorValidatorTest
  MaskAlgorithmCompletionProviderTest
  MaskToolHandlerTest
  MaskWorkflowPlanningServiceTest
  MaskWorkflowValidationServiceTest
  WorkflowExecutionServiceTest
  WorkflowExecutionToolHandlerTest
  WorkflowValidationToolHandlerTest
  MCPErrorConverterTest
  MCPTransportPayloadUtilsTest
  MCPToolSpecificationFactoryTest
  MCPPromptSpecificationFactoryTest
  MCPResourceSpecificationFactoryTest
  MCPCompletionSpecificationFactoryTest
  MCPSyncServerFactoryTest
  MCPCompletionServiceTest
)
TEST_FILTER=$(IFS=,; echo "${TESTS[*]}")
./mvnw -pl mcp/features/encrypt,mcp/features/mask,mcp/core,mcp/bootstrap -am -DskipITs -Dspotless.skip=true \
  -Dtest="${TEST_FILTER}" \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

- Exit code: `0`.
- Time: 2026-05-15 23:13 Asia/Shanghai.

- Style gate commands:

```bash
./mvnw -pl mcp/features/encrypt,mcp/features/mask,mcp/core,mcp/bootstrap -am -Pcheck -DskipTests -DskipITs checkstyle:check
./mvnw -pl mcp/features/encrypt,mcp/features/mask,mcp/core,mcp/bootstrap -Pcheck -DskipTests -DskipITs spotless:check
```

- Exit codes: `0`, `0`.
- Time: 2026-05-15 23:13 Asia/Shanghai.

## T022 Encrypt Product Path

- Resources:
  `EncryptResourceHandlerTest`, `EncryptMCPHandlerProviderTest`.
- Prompts:
  `EncryptToolDescriptorValidatorTest.assertPromptUsesGuidanceName`,
  `MCPPromptSpecificationFactoryTest`.
- Completions:
  `EncryptAlgorithmCompletionProviderTest`,
  `MCPCompletionSpecificationFactoryTest`,
  `MCPCompletionServiceTest`.
- Descriptor validation:
  `EncryptToolDescriptorValidatorTest`,
  `MCPSyncServerFactoryTest.assertCreateExposesOfficialDiscoveryDescriptors`.
- Planning:
  `EncryptWorkflowPlanningServiceTest`,
  `EncryptToolHandlerTest`.
- Apply and approval:
  `WorkflowExecutionServiceTest`,
  `WorkflowExecutionToolHandlerTest`.
- Validation:
  `EncryptWorkflowValidationServiceTest`,
  `WorkflowValidationToolHandlerTest`.
- Recovery payloads:
  `MCPErrorConverterTest`.
- Structured MCP tool result and JSON text fallback:
  `MCPTransportPayloadUtilsTest`,
  `MCPToolSpecificationFactoryTest`.

## T023 Mask Product Path

- Resources:
  `MaskResourceHandlerTest`, `MaskMCPHandlerProviderTest`.
- Prompts:
  `MaskToolDescriptorValidatorTest.assertPromptUsesGuidanceName`,
  `MCPPromptSpecificationFactoryTest`.
- Completions:
  `MaskAlgorithmCompletionProviderTest`,
  `MCPCompletionSpecificationFactoryTest`,
  `MCPCompletionServiceTest`.
- Descriptor validation:
  `MaskToolDescriptorValidatorTest`,
  `MCPSyncServerFactoryTest.assertCreateExposesOfficialDiscoveryDescriptors`.
- Planning:
  `MaskWorkflowPlanningServiceTest`,
  `MaskToolHandlerTest`.
- Apply and approval:
  `WorkflowExecutionServiceTest`,
  `WorkflowExecutionToolHandlerTest`.
- Validation:
  `MaskWorkflowValidationServiceTest`,
  `WorkflowValidationToolHandlerTest`.
- Recovery payloads:
  `MCPErrorConverterTest`.
- Structured MCP tool result and JSON text fallback:
  `MCPTransportPayloadUtilsTest`,
  `MCPToolSpecificationFactoryTest`.

## Final Gate Status

- T024 is closed by the scorecard update after unit, product-path, and E2E evidence became current.
- T060 through T063 are closed by the final scoped unit, E2E, Checkstyle, and Spotless evidence recorded in `e2e-llm-operations-evidence.md`.
- T065 is closed after every dimension-specific evidence lane completed; `scorecard.md` records all 12 active dimensions at `100/100`.
