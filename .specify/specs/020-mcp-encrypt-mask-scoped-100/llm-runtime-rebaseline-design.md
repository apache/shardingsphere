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

## Confirmed Packaging Decision

Use Option A as the next score-closing implementation path:

- Build a project-owned Docker image locally in the GitHub Actions job before running the LLM smoke and usability suites.
- The image contains `llama-server` plus `Qwen3-1.7B-Q4_K_M.gguf`.
- The image is local to the job and does not require a pre-published registry image.
- The model download happens during the Docker build step, not during the Maven test process.
- The Docker build must pin the Hugging Face model revision and exact GGUF file.
- The Docker build must verify the downloaded model file before it can be used as score-closing evidence.

Option B remains a future hardening path for faster CI after registry ownership, license provenance, and release process are settled.
Option C is allowed only as a debug or diagnosis fallback and cannot close the score.

## Source-Driven Constraints

- `llama.cpp` server supports OpenAI-compatible chat completions, schema-constrained JSON response format, and function calling/tool use.
- `llama.cpp` documents that OpenAI API compatibility is practical but not a strong compatibility guarantee; this lane must prove the exact subset used by the current client.
- `llama.cpp` Docker docs provide a server image pattern with `ghcr.io/ggml-org/llama.cpp:server`, model volume, `--port`, and `--host 0.0.0.0`.
- `llama.cpp` server supports `-hf, --hf-repo <user>/<model>[:quant]`, with quantization defaulting to Q4_K_M when available.
- `llama.cpp` `/v1/models` uses the model path as the default model ID unless `--alias` is configured.
- `llama.cpp` OpenAI-style function calling requires the Jinja chat template path.
- `ggml-org/Qwen3-1.7B-GGUF` documents `llama-server -hf ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- `ggml-org/Qwen3-1.7B-GGUF` exposes model revision `daeb8e2d528a760970442092f6bf1e55c3b659eb`.
- The selected file is `Qwen3-1.7B-Q4_K_M.gguf`, with LFS size `1282439264` bytes and LFS SHA-256 `d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5` from Hugging Face model API blob metadata.
- The upstream GGUF model card and base Qwen model metadata declare `apache-2.0`.
- The `ghcr.io/ggml-org/llama.cpp:server` tag is mutable; planning observed linux/amd64 platform digest `sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57` and linux/arm64 platform digest `sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca`.
- The current Ollama baseline uses Qwen3 1.7B / Q4_K_M; the selected model keeps the same capability tier.

## Boundary Design

Keep the existing conversation runner and OpenAI-compatible client boundary unchanged.

Runtime-specific implementation code should live under:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/artifact/`

The LLM suites should depend on a neutral runtime support type instead of an Ollama-named type.
The suites should not build runtime evidence maps themselves; they should obtain evidence from the prepared model runtime.
README files, workflows, and Speckit contracts are allowed to change because they define score evidence rather than runtime implementation.
The implementation should not add a broad provider framework unless a second score-closing provider is required.

## Proposed Runtime Shape

- Replace `OllamaLLMRuntimeSupport` with a neutral `LLMRuntimeSupport` or `LlamaCppLLMRuntimeSupport`.
- Return a small prepared runtime object exposing only:
  - `getConfiguration()`
  - `getEvidence()`
  - `close()`
- Keep `RuntimeMode.DOCKER` as the default score mode.
- Keep `RuntimeMode.EXTERNAL_DEBUG` only for explicit non-score debugging.
- In Docker mode, validate:
  - provider is `openai-compatible`
  - model is `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
  - external endpoint settings are ignored for score ownership
- Start a Testcontainers `GenericContainer` for the server.
- Expose the server on its container port and map the test configuration to `http://<host>:<mapped-port>/v1`.
- Start `llama-server` with:
  - `--host 0.0.0.0`
  - `--port 8080`
  - `-m /models/Qwen3-1.7B-Q4_K_M.gguf`
  - `--alias ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
  - `--jinja`
  - `--reasoning off`
  - `--chat-template-kwargs {"enable_thinking":false}`
  - `--api-key mcp-llm-score`
  - `--no-ui`
  - `-n 512`
- Do not pass llama.cpp `--tools`; built-in server tools must stay disabled so the model can only exercise the ShardingSphere MCP tool surface provided by the E2E harness.
- Bind to `0.0.0.0` only inside the container and expose the port only through Testcontainers' mapped host port.
- In Docker score mode, the runtime support should replace the loaded configuration with the container-owned base URL and internal API key `mcp-llm-score`; user-provided LLM API keys are debug-only and must not enter score artifacts.
- Wait on an HTTP readiness endpoint that proves the server process is listening, then run a readiness probe that verifies:
  - `/v1/models` exposes the configured alias
  - `/v1/chat/completions` accepts the configured model
  - JSON response mode works
  - the exact tool-calling request shape used by the MCP LLM harness works, including `stream=false`, `temperature=0`, `seed=1`, `reasoning_effort=none`, `max_tokens=512`, `tools`, and `tool_choice=required`
  - the final-answer request shape works, including `tool_choice=none` and `response_format={"type":"json_object"}`
  - an optional continuation request with `tool_choice=auto` is accepted
- If llama.cpp rejects one of the current harness fields, do not silently weaken the scenario contract; first isolate the rejected field in a dedicated readiness failure and then update the client/request policy with focused tests.

## Docker Full Package Strategy

Selected score-closing strategy:

- Build a project-owned Docker image in the GitHub Actions job.
- Use the upstream `ghcr.io/ggml-org/llama.cpp:server` image as the base, pinned by platform digest in implementation.
- GitHub Actions score evidence uses the linux/amd64 base digest.
- Local arm64 reproduction may use the linux/arm64 base digest, but it must record that platform-specific digest as non-GitHub Actions reproduction evidence.
- Download `Qwen3-1.7B-Q4_K_M.gguf` from pinned Hugging Face revision `daeb8e2d528a760970442092f6bf1e55c3b659eb`.
- Verify the downloaded file by size and SHA-256 before the image is tagged for test use.
- The locally built image contains:
  - `llama-server`
  - `Qwen3-1.7B-Q4_K_M.gguf`
  - a deterministic entrypoint or command
- The E2E lane starts the locally built image through Testcontainers.
- Runtime metadata marks `modelPackaging=prepackaged`.

Proposed build assets:

- Dockerfile: `test/e2e/mcp/src/test/resources/docker/llm-runtime/Dockerfile`
- Local score image tag: `apache/shardingsphere-mcp-llm-runtime:local`
- Build step: a workflow step before Maven LLM tests runs `docker build` with pinned base image and model arguments.
- Preferred model fetch mechanism: Dockerfile `ADD --checksum=sha256:d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5` from the pinned Hugging Face resolve URL.
- Avoid installing `curl`, `wget`, package managers, or checksum tools into the final image only to fetch the model.
- Runtime configuration key: `mcp.llm.server-image` / `MCP_LLM_SERVER_IMAGE`, defaulting to the local score image tag in Docker score mode.
- Runtime configuration key: `mcp.llm.base-server-image-digest` / `MCP_LLM_BASE_SERVER_IMAGE_DIGEST`, required for score-closing metadata.
- The Maven test process must not download the model in score mode; it only starts the already built local image.

Debug fallback strategy:

- Use `ghcr.io/ggml-org/llama.cpp:server`.
- Start with `-hf ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Runtime metadata marks `modelPackaging=downloaded`.
- This path is useful for local diagnosis but is not valid score-closing evidence.

## Runtime Metadata

Every score-closing LLM artifact must record:

- `runtimeMode`
- `dockerOwned`
- `provider`
- `serverRuntime`
- `serverImage`
- `serverImageId`
- `baseServerImageDigest`
- `modelReference`
- `servedModelId`
- `modelQuantization`
- `modelSizeBytes`
- `modelRevision`
- `modelFileName`
- `modelSha256`
- `modelPackaging`
- `baseUrlOwnedByTest`
- `scoreClosing`

`serverImageId` is the immutable local Docker image ID for the project-owned image built in the GitHub Actions job.
`baseServerImageDigest` is the pinned upstream `llama.cpp` platform digest used by the Dockerfile `FROM`.
`serverImageDigest` is not mandatory for Option A because a local, unpushed Docker image does not have a registry digest.
If Option B is introduced later with a published project image, `serverImageDigest` can become an additional mandatory field for that path.

The score-closing artifact should not keep Ollama-specific `imageName` or `imageDigest` aliases.
All producers and readers must move to the neutral runtime evidence schema together.
The artifact writer should validate mandatory score-closing fields before writing `run-context.json`.

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
- model runtime reuse keys include image, model, revision, packaging mode, and runtime mode
- readiness fails fast when model alias, JSON mode, or tool calling is unsupported

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
- The model server must not enable llama.cpp built-in tools, file-system tools, shell tools, Web UI tools, or MCP proxy helpers; all model actions must flow through the ShardingSphere MCP harness.
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
- Option A local images do not have a registry digest unless pushed; runtime evidence must use local image ID plus base image platform digest instead of pretending a registry digest exists.
- `tool_choice=required` is part of the current harness request shape, but llama.cpp docs do not separately guarantee that string value; readiness must send the exact harness request shape before score evidence is accepted.

Reconciled findings:

- The user confirmed Option A, so packaging policy is no longer open.
- Score-closing evidence must not depend on live Hugging Face availability during the Maven test phase.
- The suite boundary must be neutralized, not left importing an Ollama runtime type.
- README, workflow, and legacy Speckit references to Ollama must be updated in the implementation package because they are score contracts.
- Readiness must prove the exact current client contract, not generic OpenAI-compatible availability.
- Model provenance is acceptable for local CI image construction because the GGUF and base Qwen model declare Apache 2.0, but the evidence must record revision and checksum.
- Option A evidence must record the local image ID plus pinned base image platform digest, not a nonexistent registry digest for the unpushed score image.
- Dockerfile `ADD --checksum` is the preferred model fetch mechanism because it verifies the pinned GGUF without adding downloader tools to the final image.
- llama.cpp built-in tools and UI must stay disabled so MCP usability evidence measures only the ShardingSphere MCP surface.
