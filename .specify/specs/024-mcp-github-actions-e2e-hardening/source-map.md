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

# Source Map: MCP LLM Docker E2E Environment Hardening

## Workflow Paths

- `.github/workflows/mcp-llm-e2e.yml`
- `.github/workflows/mcp-llm-usability-e2e.yml`

## Docker Runtime Paths

- `test/e2e/mcp/src/test/resources/docker/llm-runtime/Dockerfile`
- `test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh`

## LLM Runtime Configuration Paths

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/LLMRuntimeSupport.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfigurationTest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/fixture/LLMRuntimeSupportTest.java`

## Documentation Paths

- `mcp/README.md`
- `mcp/README_ZH.md`
- `.specify/specs/024-mcp-github-actions-e2e-hardening/spec.md`
- `.specify/specs/024-mcp-github-actions-e2e-hardening/plan.md`
- `.specify/specs/024-mcp-github-actions-e2e-hardening/tasks.md`
- `.specify/specs/024-mcp-github-actions-e2e-hardening/checklists/requirements.md`
- `specs/014-mcp-github-actions-e2e-hardening/requirements.md`

## Current Fixed Runtime Inputs

- Score image tag: `apache/shardingsphere-mcp-llm-runtime:local`
- Server runtime: `llama.cpp`
- Model reference: `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`
- Model file: `Qwen3-1.7B-Q4_K_M.gguf`
- Model revision: `daeb8e2d528a760970442092f6bf1e55c3b659eb`
- Model SHA-256: `d2387ca2dbfee2ffabce7120d3770dadca0b293052bc2f0e138fdc940d9bc7b5`
- Model size: `1282439264`
- linux/amd64 base digest: `sha256:988d2695631987e28a29d98970aaf0e979e23b843a26824abb790ac4245d1d57`
- linux/arm64 base digest: `sha256:a478a81b2606aa5bb4c5864c01894fe1d8851adad8b6710f14b9519944d013ca`

## Search And Verification Targets

- `rg -n "pull_request|paths:" .github/workflows/mcp-llm*.yml`
- `rg -n "'pom.xml'|'distribution/pom.xml'|'test/e2e/pom.xml'|'.specify/specs/\\*mcp\\*/\\*\\*'" .github/workflows/mcp-llm*.yml`
- `rg -n "context:|file:|load:|cache-from:|cache-to:|ignore-error" .github/workflows/mcp-llm*.yml`
- `rg -n "platforms:" .github/workflows/mcp-llm*.yml`
- `rg -n "docker buildx version" .github/workflows/mcp-llm*.yml`
- `sh -n test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh`
- `sh test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh --dry-run`
- `rg "mcp-llm-runtime:local|Qwen3-1.7B-GGUF|MCP_LLM_BASE_SERVER_IMAGE_DIGEST" .github mcp test/e2e/mcp/src/test`
- `docker system df`

## Future Review Gates

- Use `mcp-builder` after implementation because workflow/docs/helper changes affect MCP E2E score evidence.
- Use code review to confirm the helper is simple, readable, architecture-limited, and does not introduce a second model/runtime selection path.
