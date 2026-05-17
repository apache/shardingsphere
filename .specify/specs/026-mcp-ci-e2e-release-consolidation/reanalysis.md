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

# Reanalysis: MCP CI E2E And Release Consolidation

This document records follow-up analysis performed after the initial Spec Kit package was created. It does not authorize code changes by itself.

## Reanalysis 1 - Required Check Coverage

**Question**: Is the design still justified in saying JDK 21 CI should not duplicate Checkstyle, Spotless, and RAT?

**Evidence**:

- `required-check.yml` runs repo-wide `checkstyle:check`, `spotless:check`, and `apache-rat:check`.
- Root `pom.xml` includes `mcp`, `test`, and `distribution`.
- `test/e2e/pom.xml` includes `mcp`.
- `distribution/pom.xml` includes `mcp`.

**Conclusion**: The no-duplicate-style-gate decision still stands. Implementation must re-check this if module inclusion or Required Check path filtering changes.

## Reanalysis 2 - Published Image Platform Claims

**Question**: Does release runtime validation prove both linux/amd64 and linux/arm64 images?

**Evidence**:

- GHCR supports pulling published images by tag or digest.
- Docker pull validates the selected platform image on the runner.
- The existing release workflow builds `linux/amd64,linux/arm64`.

**Conclusion**: Runtime validation on the GitHub-hosted runner should be claimed only for the native pulled platform, normally linux/amd64. linux/arm64 should be verified by manifest inspection unless a future implementation explicitly accepts QEMU-based arm64 runtime validation cost.

## Reanalysis 3 - H2 Default Semantics

**Question**: Is removing H2 from real E2E enough if the environment default still enables H2?

**Evidence**:

- `test/e2e/mcp/src/test/resources/env/e2e-env.properties` currently has `mcp.e2e.production.h2.enabled=true`.
- The target design says H2 is not real production E2E evidence.

**Conclusion**: Implementation must also address default semantics. It can change the default, rename/reclassify the flag, or document H2 as lightweight-only. Leaving the current name and default unchanged would conflict with the target design.

## Reanalysis 4 - LLM Workflow Shape

**Question**: Is it enough to remove `LLMSmokeE2ETest` from Maven selectors while leaving a separate smoke workflow?

**Evidence**:

- Current `.github/workflows/mcp-llm-e2e.yml` is named `MCP - LLM E2E` but invokes `LLMSmokeE2ETest`.
- Current `.github/workflows/mcp-llm-usability-e2e.yml` invokes `LLMUsabilitySuiteE2ETest`.
- The accepted target rejects standalone smoke as E2E.

**Conclusion**: Final state must remove any standalone smoke workflow entry after usability covers the topology. A second LLM workflow is acceptable only if it has a documented scheduling or resource reason and runs a complete non-smoke target.

## Final Self-Question Loop

1. Is there another issue worth reanalyzing around smoke deletion? No; topology mapping is already a prerequisite.
2. Is there another issue worth reanalyzing around H2 cleanup? No; default semantics are now captured.
3. Is there another issue worth reanalyzing around release validation? No; native runtime versus manifest validation is now separated.
4. Is there another issue worth reanalyzing around JDK 21 CI? No; Required Check evidence and re-check condition are captured.
5. Is there another issue worth reanalyzing around MCP design? No; implementation-time MCP builder review remains the gate if MCP/E2E modules are touched.

