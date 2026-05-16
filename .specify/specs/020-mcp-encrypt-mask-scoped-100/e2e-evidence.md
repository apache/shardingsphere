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
  and `imageName=ollama/ollama:0.23.1`.

## Pinned Image Manifest

Verified command:

```bash
docker manifest inspect ollama/ollama:0.23.1 --verbose
```

Resolved platform digests:

- linux/amd64: `sha256:133a0539e836688c7cb88e318e31232f344a84cff7aab0cf6ac90476bc99c8ed`
- linux/arm64: `sha256:fcaa568338a6b0993c82f259a5072f46814d6de276cf3dea5b91e281b7f9d149`

## Reproduction Commands

LLM smoke lane:

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

LLM usability lane:

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e test -DskipITs -Dspotless.skip=true \
  -Dtest=LLMUsabilitySuiteE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=true
```

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
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest=LLME2EConfigurationTest,OllamaLLMRuntimeSupportTest,LLME2EArtifactWriterTest,LLMChatModelClientTest test
```

Result: exit code `0`, `15` tests, `0` failures, `0` errors, `0` skipped.

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
./mvnw -pl test/e2e/mcp -Pcheck -DskipTests -DskipITs checkstyle:check spotless:check
```

Result: exit code `0`, `0` Checkstyle violations, `0` Spotless changes needed.
