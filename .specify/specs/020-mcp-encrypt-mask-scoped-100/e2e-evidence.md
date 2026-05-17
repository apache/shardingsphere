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

# E2E Evidence: MCP Encrypt/Mask Scoped Scorecard 100

## LLM Runtime Evidence

- Score-closing LLM mode: Docker-owned lightweight `llama.cpp` server runtime.
- Required server runtime: a project-owned local Docker image built in the GitHub Actions job from pinned `ghcr.io/ggml-org/llama.cpp:server`.
- Required model: `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Required model revision: `daeb8e2d528a760970442092f6bf1e55c3b659eb`.
- Required model file: `Qwen3-1.7B-Q4_K_M.gguf`.
- Required quantization: `Q4_K_M`.
- Required model size reference: `1282439264` bytes.
- Required model integrity evidence: SHA-256 `d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5`, verified during Docker build.
- External endpoints are allowed only with `mcp.llm.runtime-mode=external-debug` or `MCP_LLM_RUNTIME_MODE=external-debug`.
- External debug endpoints do not count as score-closing evidence.
- Docker-owned score mode does not reuse an externally configured API key; it talks to the test-owned `llama.cpp` server container.
- Every LLM conversation writes runtime metadata into `run-context.json` under `runtime`.
- Score-closing runtime metadata must include `runtimeMode=docker`, `dockerOwned=true`, server image reference, local server image ID, base server image platform digest, model reference,
  served model ID, quantization, model file size, model revision, model SHA-256, `modelPackaging=prepackaged`, and `scoreClosing=true`.
- Generated artifacts under `test/e2e/mcp/target/llm-e2e` are valid score-closing evidence only when they were produced after the `llama.cpp` runtime metadata writer change.
- Any older `run-context.json` without the top-level `runtime` object is stale execution evidence and must be regenerated before it is used to close SC-008.

## Rejected Ollama Manifest Evidence

Verified command:

```bash
docker manifest inspect ollama/ollama:0.23.1 --verbose
```

Resolved platform digests:

- linux/amd64: `sha256:133a0539e836688c7cb88e318e31232f344a84cff7aab0cf6ac90476bc99c8ed`
- linux/arm64: `sha256:fcaa568338a6b0993c82f259a5072f46814d6de276cf3dea5b91e281b7f9d149`

Rebaseline note:

- `ollama/ollama:0.23.1` is no longer accepted as score-closing LLM runtime evidence.
- Local manifest inspection showed the linux/amd64 compressed image layers total about `4.01GB`, including a CUDA-bearing layer of about `3.86GB`.
- This image size made the lane fail locally during Docker layer registration and is considered too brittle for GitHub Actions.

## Selected Lightweight Runtime Evidence

Verified command:

```bash
docker manifest inspect ghcr.io/ggml-org/llama.cpp:server
docker manifest inspect ghcr.io/ggml-org/llama.cpp@sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57
```

Observed evidence:

- `ghcr.io/ggml-org/llama.cpp:server` provides linux/amd64, linux/arm64, and linux/s390x manifests.
- Planning recheck observed linux/amd64 digest `sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57`.
- Planning recheck observed linux/arm64 digest `sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca`.
- linux/amd64 compressed runtime layers total about `47MB`.
- `llama.cpp` server supports OpenAI-compatible chat completions, schema-constrained JSON response format, and function calling/tool use.
- `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M` is about `1.28GB` and stays in the same Qwen3 1.7B Q4_K_M capability tier as the current Ollama baseline.
- The accepted score-closing packaging path is the project-owned local Docker build, not runtime `-hf` download.

## Reproduction Commands

LLM smoke lane:

```bash
./mvnw -pl test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

LLM usability lane:

```bash
./mvnw -pl test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

## Historical Local LLM Lane Attempt

Attempted command:

```bash
MCP_LLM_RUN_ID=codex-20260517-llm-runtime ./mvnw -pl test/e2e/mcp -am -Pllm-e2e \
  -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest,LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Result: exit code `143` after manual stop. This attempt does not count as score-closing pass evidence.

Observed evidence:

- Testcontainers connected to local Docker Desktop `27.3.1`.
- The LLM lane entered the Docker-owned path and attempted to pull `ollama/ollama:0.23.1`.
- The smoke test failed during Docker image registration with:
  `failed to register layer: write /usr/lib/ollama/cuda_v12/libggml-cuda.so: no space left on device`.
- `docker system df` showed Docker images using `46.67GB` with `41.27GB` reclaimable on the local machine.

This follow-up is now complete:

- The score-closing runtime was replaced with Docker-owned `llama.cpp` server plus `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- GitHub Actions now builds the project-owned local server-plus-model Docker image before Maven tests run.
- Current smoke and usability evidence below was regenerated after the runtime rebaseline.
- Generated `run-context.json` files include Docker-owned `llama.cpp` runtime metadata and the selected Qwen3 Q4_K_M model reference.

## Current LLM Score-Closing Evidence

Local server-plus-model image:

```bash
docker image inspect apache/shardingsphere-mcp-llm-runtime:local --format '{{.Id}} {{.Size}} {{.Architecture}}'
```

Result: exit code `0`, image ID `sha256:3379ed38c3cc229afdc5b758527666c0e2bcef4cf4f9756978a45ebb4b6b3a71`,
size `1418953798` bytes, architecture `arm64`.

LLM smoke lane:

```bash
./mvnw -pl mcp/bootstrap,test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Result: exit code `0`, `4` tests, `0` failures, `0` errors, `0` skipped, finished at `2026-05-17T21:13:30+08:00`.

Smoke artifact run id:

- `test/e2e/mcp/target/llm-e2e/20260517210907-f484ebb5`

Covered smoke scenarios:

- `minimal-smoke-h2`
- `minimal-smoke-h2-stdio`
- `minimal-smoke-mysql`
- `minimal-smoke-mysql-stdio`

LLM usability lane:

```bash
./mvnw -pl mcp/bootstrap,test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Result: exit code `0`, `1` suite test, `0` failures, `0` errors, `0` skipped, finished at `2026-05-17T21:07:05+08:00`.

Usability artifact run id:

- `test/e2e/mcp/target/llm-e2e/20260517210034-81719143`

Core scorecard:

- suiteId: `llm-usability-h2/core`
- overallScore: `100.0`
- fullScore: `true`
- taskSuccessRate: `1.0`
- naturalTaskSuccessRate: `1.0`
- protocolContractSuccessRate: `1.0`
- firstCorrectActionRate: `1.0`
- invalidCallRate: `0.0`
- boundaryConfusionRate: `0.0`
- resourceHitRate: `1.0`
- recoveryRate: `1.0`
- nextActionFollowRate: `1.0`
- approvalViolationRate: `0.0`
- nativeToolCallRate: `1.0`
- harnessRecoveryRate: `0.0`

Extended scorecard:

- suiteId: `llm-usability-h2/extended`
- overallScore: `100.0`
- fullScore: `true`
- taskSuccessRate: `1.0`
- naturalTaskSuccessRate: `1.0`
- protocolContractSuccessRate: `1.0`
- firstCorrectActionRate: `1.0`
- invalidCallRate: `0.0`
- boundaryConfusionRate: `0.0`
- resourceHitRate: `1.0`
- recoveryRate: `1.0`
- nextActionFollowRate: `1.0`
- approvalViolationRate: `0.0`
- nativeToolCallRate: `1.0`
- harnessRecoveryRate: `0.0`

Runtime metadata observed in smoke and usability `run-context.json`:

- `runtimeMode=docker`
- `dockerOwned=true`
- `provider=openai-compatible`
- `serverRuntime=llama.cpp`
- `serverImage=apache/shardingsphere-mcp-llm-runtime:local`
- `serverImageId=sha256:3379ed38c3cc229afdc5b758527666c0e2bcef4cf4f9756978a45ebb4b6b3a71`
- `baseServerImageDigest=sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca`
- `modelReference=ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
- `servedModelId=ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
- `modelQuantization=Q4_K_M`
- `modelSizeBytes=1282439264`
- `modelRevision=daeb8e2d528a760970442092f6bf1e55c3b659eb`
- `modelFileName=Qwen3-1.7B-Q4_K_M.gguf`
- `modelSha256=d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5`
- `modelPackaging=prepackaged`
- `baseUrlOwnedByTest=true`
- `scoreClosing=true`

## GitHub Actions Evidence

- `.github/workflows/mcp-llm-e2e.yml` uses the Maven `llm-e2e` profile.
- `.github/workflows/mcp-llm-usability-e2e.yml` uses the Maven `llm-e2e` profile.
- The workflows must not pre-start an external model container.
- The workflows do not pass `MCP_LLM_BASE_URL`, `MCP_LLM_API_KEY`, or an external model endpoint as score evidence.
- The workflows must use the Docker-owned `llama.cpp` runtime path after T091 through T099 are implemented.

## Unit Evidence

The focused unit evidence for T091 through T098 is owned by:

- `LLME2EConfigurationTest`
- `LLMRuntimeSupportTest`
- `LLME2EArtifactWriterTest`
- `LLMChatModelClientTest`
- `LLMMCPConversationRunnerTest`
- `LLMUsabilityScenarioCatalogTest`

Focused command:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=LLME2EConfigurationTest,LLMRuntimeSupportTest,LLME2EArtifactWriterTest,LLMChatModelClientTest,LLMMCPConversationRunnerTest,LLMUsabilityScenarioCatalogTest test
```

Result: exit code `0`, focused LLM harness tests passed with `0` failures, `0` errors, `0` skipped.

Final focused unit command:

```bash
./mvnw -pl mcp/bootstrap,test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=LLME2EConfigurationTest,LLMRuntimeSupportTest,LLME2EArtifactWriterTest,LLMChatModelClientTest,LLMMCPConversationRunnerTest,LLMUsabilityScenarioCatalogTest test
```

Result: exit code `0`, `78` tests, `0` failures, `0` errors, `0` skipped, finished at `2026-05-17T21:15:07+08:00`.

Mocking note:

- `LLMRuntimeSupportTest` uses one bounded `mockStatic(MySQLRuntimeTestSupport.class)` try-with-resources block.
- The exception is kept local because `test/e2e/mcp` does not depend on `shardingsphere-test-infra-framework`,
  and adding that dependency only for this static Docker availability probe would be broader than the LLM runtime boundary change.

Default module command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Result: exit code `0`, `281` tests, `0` failures, `0` errors, `15` skipped.

Style command:

```bash
./mvnw -pl test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check
```

Result: exit code `0`, `0` Checkstyle violations, `0` Spotless changes needed.

## Default Security and Artifact Lane

Focused command:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPBuilderEvaluationArtifactTest,HttpTransportApprovalSafetyE2ETest,HttpTransportSessionLifecycleE2ETest,HttpTransportSecurityE2ETest,HttpTransportAccessTokenE2ETest,HttpTransportCompletionE2ETest,HttpTransportRecoveryE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Result: exit code `0`, `39` tests, `0` failures, `0` errors, `0` skipped.

## Distribution Smoke Evidence

Precondition:

- `distribution/mcp/target/apache-shardingsphere-mcp-5.5.4-SNAPSHOT` existed locally before the smoke run.
- The lane was explicitly enabled with `-Dmcp.e2e.distribution.enabled=true`.

Focused command:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.distribution.enabled=true \
  -Dtest=PackagedDistributionSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Result: exit code `0`, `2` tests, `0` failures, `0` errors, `0` skipped.

Covered runtime paths:

- Packaged HTTP runtime startup, runtime diagnostics, database resources, tool discovery, metadata search, and safe query execution.
- Packaged STDIO runtime startup, stdout protocol isolation, runtime diagnostics, database resources, tool discovery, metadata search, and safe query execution.

## Default and Opt-In Lanes

Default lane:

- Programmatic HTTP runtime tests that use in-process runtime fixtures.
- LLM artifact and configuration unit tests.
- MCP builder evaluation artifact validation.
- No external Docker service is required unless a selected test explicitly starts one.

Opt-in lanes:

- Proxy/MySQL product-path lane: starts Testcontainers MySQL and embedded ShardingSphere-Proxy before HTTP MCP workflow calls.
- STDIO lane: starts the packaged or classpath runtime over stdio and reserves stdout for MCP protocol frames.
- Distribution lane: builds or resolves the packaged MCP distribution and verifies packaged HTTP/STDIO startup behavior.
- LLM lane: starts Docker-owned `llama.cpp` server and serves `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.

## Local Prerequisites for Opt-In Lanes

- Docker must be installed and running for MySQL, Proxy, distribution smoke with runtime containers, and LLM lanes.
- Testcontainers must be able to pull images and allocate loopback ports.
- The Proxy/MySQL lane must wait for MySQL readiness before JDBC or Proxy workflow calls.
- The LLM lane must not use `MCP_LLM_BASE_URL`, `MCP_LLM_API_KEY`, or external endpoint settings as score evidence.
- The LLM lane must use the locally built server-plus-model image when closing score evidence in GitHub Actions.
- The distribution lane expects the MCP package under `distribution/mcp/target` or builds it through Maven with `-am`.
- The STDIO lane expects the runtime config to enable stdio only, with HTTP disabled for that process.

Recommended opt-in commands:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=HttpProductionProxyEncryptWorkflowE2ETest,HttpProductionProxyMaskWorkflowE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=PackagedDistributionSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

```bash
./mvnw -pl test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest,LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```
