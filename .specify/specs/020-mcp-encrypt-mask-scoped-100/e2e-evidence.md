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

- Score-closing LLM mode: Docker-owned Ollama runtime.
- Runtime image: `ollama/ollama:0.23.1`.
- Required model: `qwen3:1.7b`.
- External endpoints are allowed only with `mcp.llm.runtime-mode=external-debug` or `MCP_LLM_RUNTIME_MODE=external-debug`.
- External debug endpoints do not count as score-closing evidence.
- Docker-owned score mode does not reuse an externally configured API key; it talks to the test-owned Ollama container with the local default key.
- Every LLM conversation writes runtime metadata into `run-context.json` under `runtime`.
- Score-closing runtime metadata includes `runtimeMode=docker`, `dockerOwned=true`,
  `imageName=ollama/ollama:0.23.1`, and the current platform `imageDigest`.
- Generated artifacts under `test/e2e/mcp/target/llm-e2e` are valid score-closing evidence only when they were produced after the Docker-owned runtime metadata writer change.
- Any older `run-context.json` without the top-level `runtime` object is stale execution evidence and must be regenerated before it is used to close SC-008.

## Pinned Image Manifest

Verified command:

```bash
docker manifest inspect ollama/ollama:0.23.1 --verbose
```

Resolved platform digests:

- linux/amd64: `sha256:133a0539e836688c7cb88e318e31232f344a84cff7aab0cf6ac90476bc99c8ed`
- linux/arm64: `sha256:fcaa568338a6b0993c82f259a5072f46814d6de276cf3dea5b91e281b7f9d149`

Runtime implementation note:

- Score evidence still reports the stable tag `ollama/ollama:0.23.1`.
- The Docker pull reference uses `ollama/ollama:0.23.1@<platform digest>` for the current local architecture,
  so score runs do not depend on a mutable `latest` tag and avoid unnecessary multi-architecture layer pulls.

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

## Local LLM Lane Attempt

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

Follow-up to close this lane:

- Reclaim Docker disk space locally.
- Rerun the LLM command above after this digest-pinned implementation change.
- Confirm generated `run-context.json` files include `runtimeMode=docker`, `dockerOwned=true`,
  `imageName=ollama/ollama:0.23.1`, and the platform `imageDigest`.

## GitHub Actions Evidence

- `.github/workflows/mcp-llm-e2e.yml` uses the Maven `llm-e2e` profile.
- `.github/workflows/mcp-llm-usability-e2e.yml` uses the Maven `llm-e2e` profile.
- The workflows do not pre-start an external Ollama container.
- The workflows do not pass `MCP_LLM_BASE_URL`, `MCP_LLM_API_KEY`, or an external model endpoint as score evidence.

## Unit Evidence

The focused unit evidence for T086 through T090 is owned by:

- `LLME2EConfigurationTest`
- `OllamaLLMRuntimeSupportTest`
- `LLME2EArtifactWriterTest`
- `LLMChatModelClientTest`

Focused command:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=MCPBuilderEvaluationArtifactTest,LLME2EArtifactWriterTest,LLME2EConfigurationTest,OllamaLLMRuntimeSupportTest test
```

Result: exit code `0`, `24` tests, `0` failures, `0` errors, `0` skipped.

Mocking note:

- `OllamaLLMRuntimeSupportTest` uses one bounded `mockStatic(MySQLRuntimeTestSupport.class)` try-with-resources block.
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
- LLM lane: starts Docker-owned `ollama/ollama:0.23.1` and pulls or uses `qwen3:1.7b`.

## Local Prerequisites for Opt-In Lanes

- Docker must be installed and running for MySQL, Proxy, distribution smoke with runtime containers, and LLM lanes.
- Testcontainers must be able to pull images and allocate loopback ports.
- The Proxy/MySQL lane must wait for MySQL readiness before JDBC or Proxy workflow calls.
- The LLM lane must not use `MCP_LLM_BASE_URL`, `MCP_LLM_API_KEY`, or external endpoint settings as score evidence.
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
