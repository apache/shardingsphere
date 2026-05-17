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

Score-closing LLM evidence must move from Docker-owned Ollama to a lightweight Docker-owned `llama.cpp` server runtime:

- The E2E support layer starts and owns the model server container.
- The score-closing runtime serves `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- The lane does not require external model credentials, host-level LLM installs, host model files, or a pre-running external model endpoint.
- External OpenAI-compatible endpoints may remain available only as explicit debug mode and cannot close score evidence.
- The accepted CI path is a project-owned local Docker image built in GitHub Actions before the LLM tests run.
- Online `-hf ggml-org/Qwen3-1.7B-GGUF:Q4_K_M` retrieval inside the Docker-owned runtime may remain as a documented debug fallback, not score-closing evidence.

## Source-Driven Evidence

- Ollama `qwen3:1.7b` is `qwen3`, about `2.03B` parameters, `Q4_K_M`, and about `1.4GB`.
- `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M` is also the Qwen3 1.7B capability tier with `Q4_K_M` quantization and `1282439264` bytes for `Qwen3-1.7B-Q4_K_M.gguf`.
- Hugging Face model API evidence records revision `daeb8e2d528a760970442092f6bf1e55c3b659eb`, LFS SHA-256 `d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5`, and Apache 2.0 model card metadata.
- `llama.cpp` server supports OpenAI-compatible chat completions, schema-constrained JSON response format, and function calling/tool use.
- Local manifest inspection showed `ghcr.io/ggml-org/llama.cpp:server` linux/amd64 compressed runtime layers total about `47MB`.
- Reinspection showed the mutable `server` tag moved to linux/amd64 platform digest `sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57`, so implementation must pin a platform digest instead of relying on the tag.
- Local manifest inspection showed `ollama/ollama:0.23.1` linux/amd64 compressed runtime layers total about `4.01GB`, including a CUDA-bearing layer of about `3.86GB`.

## Rebaseline Rationale

The current Docker-owned Ollama implementation proved that the LLM lane can avoid external credentials and operator-managed endpoints, but it is not acceptable as the final score-closing runtime.
It failed locally during image registration with `no space left on device` while writing `/usr/lib/ollama/cuda_v12/libggml-cuda.so`.
That is not just a local cleanup problem: the large runtime layer makes GitHub Actions execution brittle and slow.

Switching to `llama.cpp` server keeps the model tier aligned with the current Ollama baseline while removing the heavy runtime image.
This is a runtime replacement, not a downgrade from Qwen3 1.7B Q4_K_M.

## Baseline Before Rebaseline

- `LLMSmokeE2ETest` and `LLMUsabilitySuiteE2ETest` call the LLM runtime support through `LLME2EConfiguration.load()`.
- The current implementation can start a Docker-owned Ollama container and reject external endpoints for score mode.
- It pins `ollama/ollama:0.23.1` and records a platform image digest.
- `LLME2EConfiguration` allows explicit external debug mode for operator-managed OpenAI-compatible endpoints.
- Existing LLM artifacts can record runtime metadata, but the metadata is Ollama-specific.

## Gap

The score-closing lane still depends on a runtime image that is too large for reliable local and GitHub Actions execution.
The lane must be redesigned so Docker ownership remains strict while the runtime becomes lightweight and Action-suitable.

## Required Implementation

1. Keep an explicit runtime mode with Docker as the default score mode and external debug as non-score evidence.
2. Replace the Ollama-specific score runtime with a `llama.cpp` server runtime that exposes the existing OpenAI-compatible `/v1/chat/completions` contract.
3. Use `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M` as the fixed score-closing model.
4. Build a project-owned local Docker image in GitHub Actions containing the server binary and pinned GGUF model so Maven tests run without runtime model download.
5. Keep online Hugging Face model retrieval only as a documented debug fallback and never as score-closing evidence.
6. Reject unsupported score-closing model changes in Docker mode so the lane cannot silently downgrade capability.
7. Record runtime metadata: provider, server image, local server image ID, base server image platform digest, model reference, served model ID, quantization, model file size, model revision, model SHA-256, prepackaged/downloaded mode, runtime mode, score-closing flag, and Docker ownership.
8. Update README, README_ZH, workflows, and E2E evidence so Ollama is no longer presented as score-closing LLM evidence.

## Implementation Status

- Rebaseline requirements are documented.
- Runtime implementation is still pending.
- Docker-owned external-endpoint isolation from the prior Ollama baseline should be preserved.
- Final score closure is reopened until `llama.cpp` server smoke and usability lanes pass and artifacts record the new runtime metadata.

## Test Plan

- Unit test that default configuration selects Docker score mode.
- Unit test that Docker mode does not return an external runtime when a configured endpoint is ready.
- Unit test that external debug mode can return an external runtime.
- Unit test that non-default provider or model is rejected for Docker score mode.
- Unit test or artifact assertion that the score-closing server is `llama.cpp` and the model is `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Unit test that runtime metadata records model reference, served model ID, revision, SHA-256, quantization, model size, and prepackaged/downloaded mode.
- LLM smoke E2E evidence must record Docker-owned `llama.cpp` runtime metadata.
- LLM usability E2E evidence must record Docker-owned `llama.cpp` runtime metadata.

## Non-Goals

- Using `ollama/ollama` as score-closing LLM runtime.
- Upgrading the score-closing model above Qwen3 1.7B Q4_K_M unless LLM usability evidence proves Q4_K_M cannot satisfy the MCP scenarios.
- Requiring external API keys for score evidence.
- Supporting arbitrary model providers for score evidence.
- Host-level installation of llama.cpp, Ollama, Python inference stacks, or local model files for score evidence.
