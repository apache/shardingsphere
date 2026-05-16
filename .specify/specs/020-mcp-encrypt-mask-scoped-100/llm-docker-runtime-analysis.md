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

# LLM Docker Runtime Analysis

## Decision

Score-closing LLM evidence must use online Docker full package mode:

- The E2E support layer starts and owns an Ollama container.
- The container serves `qwen3:1.7b`.
- The lane does not require external model credentials or a pre-running external model endpoint.
- The container may pull `ollama/ollama:0.23.1` and `qwen3:1.7b` online when local caches are empty.
- External OpenAI-compatible endpoints may remain available only as explicit debug mode and cannot close score evidence.

## Baseline Before T086 Through T090

Before this implementation, the LLM E2E support was close to Docker full package mode but was not strict enough.

- `LLMSmokeE2ETest` and `LLMUsabilitySuiteE2ETest` call `OllamaLLMRuntimeSupport.prepare(LLME2EConfiguration.load())`.
- `OllamaLLMRuntimeSupport` can start an Ollama container and pull `qwen3:1.7b` inside the container.
- It used a floating Ollama image tag, which was not stable enough for score-closing evidence.
- `LLME2EConfiguration` allows `MCP_LLM_BASE_URL`, `MCP_LLM_MODEL`, and `MCP_LLM_API_KEY` overrides.
- `OllamaLLMRuntimeSupport.prepare()` first probed the configured endpoint.
  If the endpoint was ready, it returned an external runtime instead of starting Docker.
- `mcp/README.md` and `mcp/README_ZH.md` documented manual Ollama startup and the obsolete LLM enable environment flag.
  The E2E gate is the Maven `llm-e2e` profile or `mcp.e2e.llm.enabled`, so those README steps were stale.

## Gap

The baseline behavior could produce valid LLM E2E results from an operator-managed model service.
That remains useful for debugging, but it is not valid score-closing evidence under this package.

## Required Implementation

1. Add an explicit runtime mode, with Docker as the default score mode.
   Suggested values: `docker` and `external-debug`.
2. In Docker mode, `OllamaLLMRuntimeSupport.prepare()` must always start or reuse a Docker-owned container runtime.
3. In Docker mode, ignore external endpoint readiness for score evidence.
4. In external debug mode, allow the existing endpoint probing behavior but mark the runtime as debug-only.
5. Expose enough runtime metadata for tests and evidence to distinguish Docker-owned from external debug runtime.
6. Pin the score-closing image to `ollama/ollama:0.23.1` and record the resolved image digest in evidence.
7. Update README and README_ZH so LLM score reproduction uses the test-owned Docker path, `-Pllm-e2e`, and no score-closing external endpoint variables.

## Implementation Status

- Default runtime mode is `docker`.
- Docker mode starts or reuses test-owned Ollama and does not treat a ready external endpoint as score evidence.
- External endpoint probing is available only with explicit `external-debug` runtime mode.
- Score-closing image is pinned to `ollama/ollama:0.23.1`.
- Score-closing model is fixed to `qwen3:1.7b`.
- README, README_ZH, and E2E evidence now describe the Maven `llm-e2e` profile path and the debug-only external endpoint path separately.

## Test Plan

- Unit test that default configuration selects Docker score mode.
- Unit test that Docker mode does not return an external runtime when a configured endpoint is ready.
- Unit test that external debug mode can return an external runtime.
- Unit test that non-default provider or model is rejected for Docker score mode.
- Unit test or evidence assertion that the score-closing image is `ollama/ollama:0.23.1`.
- LLM smoke E2E evidence must record Docker-owned runtime metadata.
- LLM usability E2E evidence must record Docker-owned runtime metadata.

## Non-Goals

- Offline model bundling.
- Persistent model cache management beyond Docker/Ollama defaults.
- Supporting arbitrary model providers for score evidence.
- Requiring external API keys for score evidence.
