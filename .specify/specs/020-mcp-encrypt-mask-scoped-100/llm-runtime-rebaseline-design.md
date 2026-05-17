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

# LLM Runtime Rebaseline Design

## Goal

Replace the score-closing MCP LLM E2E runtime from Docker-owned Ollama to Docker-owned `llama.cpp` server while preserving the current Qwen3 1.7B Q4_K_M capability tier.

The next implementation must make the LLM lane runnable from Docker without host-level LLM installs, host model files, external credentials, or a manually pre-running model service.

## Source-Driven Constraints

- `llama.cpp` server supports OpenAI-compatible chat completions, schema-constrained JSON response format, and function calling/tool use.
- `llama.cpp` Docker docs provide a server image pattern with `ghcr.io/ggml-org/llama.cpp:server`, model volume, `--port`, and `--host 0.0.0.0`.
- `llama.cpp` server supports `-hf, --hf-repo <user>/<model>[:quant]`, with quantization defaulting to Q4_K_M when available.
- `ggml-org/Qwen3-1.7B-GGUF` documents `llama-server -hf ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- The current Ollama baseline uses Qwen3 1.7B / Q4_K_M; the selected model keeps the same capability tier.

## Boundary Design

Keep the existing conversation runner and OpenAI-compatible client boundary unchanged.

The only runtime-specific code should live under:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/artifact/`

The LLM suites should depend on a neutral runtime support type instead of an Ollama-named type.
The implementation should not add a broad provider framework unless a second score-closing provider is required.

## Proposed Runtime Shape

- Replace `OllamaLLMRuntimeSupport` with a neutral `LLMRuntimeSupport` or `LlamaCppLLMRuntimeSupport`.
- Keep `RuntimeMode.DOCKER` as the default score mode.
- Keep `RuntimeMode.EXTERNAL_DEBUG` only for explicit non-score debugging.
- In Docker mode, validate:
  - provider is `openai-compatible`
  - model is `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
  - external endpoint settings are ignored for score ownership
- Start a Testcontainers `GenericContainer` for the server.
- Expose the server on its container port and map the test configuration to `http://<host>:<mapped-port>/v1`.
- Wait on an HTTP readiness endpoint that proves the server process is listening, then run the existing `LLMChatModelClient.waitUntilReady()` readiness probe.

## Docker Full Package Strategy

Preferred score-closing strategy:

- Use a project-owned Docker image that already contains:
  - `llama-server`
  - `Qwen3-1.7B-Q4_K_M.gguf`
  - a deterministic entrypoint or command
- The E2E lane pulls that one image and starts it through Testcontainers.
- Runtime metadata marks `modelPackaging=prepackaged`.

Fallback strategy:

- Use `ghcr.io/ggml-org/llama.cpp:server`.
- Start with `-hf ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Runtime metadata marks `modelPackaging=downloaded`.
- This path is useful for local diagnosis but should not be the preferred GitHub Actions score-closing path.

## Runtime Metadata

Every score-closing LLM artifact must record:

- `runtimeMode`
- `dockerOwned`
- `provider`
- `serverRuntime`
- `serverImage`
- `serverImageDigest` when available
- `modelReference`
- `modelQuantization`
- `modelSizeBytes`
- `modelPackaging`
- `baseUrlOwnedByTest`

External debug artifacts must keep enough metadata to make them visibly non-score-closing.

## Test Plan

Unit tests:

- default configuration selects Docker score mode and the Qwen3 Q4_K_M model
- Docker score mode rejects unsupported provider values
- Docker score mode rejects unsupported model values
- external debug mode can still use a ready OpenAI-compatible endpoint
- Docker score mode does not reuse a configured external endpoint
- runtime metadata contains all score-closing fields
- artifact writer preserves and redacts runtime metadata correctly

E2E tests:

- LLM smoke lane starts the Docker-owned `llama.cpp` server runtime and writes runtime metadata
- LLM usability lane starts or reuses the Docker-owned runtime and writes runtime metadata
- generated artifacts show no external API key and no external endpoint reuse

Style and quality:

- run focused `test/e2e/mcp` unit tests
- run `checkstyle:check` and `spotless:check` for `test/e2e/mcp`
- run the opt-in LLM smoke/usability command once Docker image availability is confirmed

## MCP-Builder Design Check

- MCP tool schemas and server behavior are unchanged by this runtime swap.
- The LLM E2E lane remains a validation lane for read-only, independent, non-destructive MCP usage.
- The evaluation artifact remains valid only if the model still performs resource discovery, tool calling, JSON final answer generation, and required-tool coverage.
- Runtime metadata must make score evidence audit-friendly so reviewers can distinguish official score evidence from debug-only external endpoint runs.
- The design avoids making MCP tools easier only for the test model; it tests the same model-facing MCP surface that clients use.

## Doubt-Driven Review Notes

Claim: The runtime can be made lighter without lowering the current score-closing capability tier.

Why it matters: If the model/runtime change silently weakens function calling or JSON behavior, the scorecard would become easier instead of more reliable.

Adversarial findings to guard against:

- A prepackaged image may be unavailable to forks unless it is built locally or published under an accessible registry.
- Online `-hf` download is Docker-owned but not fully self-contained at runtime.
- `llama.cpp` server image tags are mutable unless a digest or project-owned image version is pinned.
- `/v1/models` readiness may differ from Ollama and must be verified with the existing client probe, not assumed.
- The model name seen by OpenAI-compatible responses may not exactly equal the Hugging Face reference; readiness checks must use the configured model only if the server exposes that ID.

## Open Confirmation

Before coding, confirm the score-closing image packaging policy:

- Option A: build a project-owned Docker image locally in the GitHub Actions job before running the LLM tests
- Option B: require a pre-published project-owned image from a registry
- Option C: use `ghcr.io/ggml-org/llama.cpp:server` plus in-container `-hf` download as the first implementation, and treat prepackaging as a follow-up hardening task
