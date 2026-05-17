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

# Requirements Checklist: MCP Encrypt/Mask Scoped Scorecard 100

## Scope Checks

- [x] Branch constraint is explicit.
- [x] MCP Java SDK `1.1.2` is fixed.
- [x] MCP protocol scoring is limited to `2025-11-25`.
- [x] MCP icons and `Tool.execution` are non-goals.
- [x] Functional completeness is limited to encrypt and mask.
- [x] Non-encrypt/mask ShardingSphere features are excluded.
- [x] Readability-first implementation elegance is stated.

## Quality Checks

- [x] Every active score dimension has a current score and target score.
- [x] Every dimension below 100 has concrete closing evidence requirements.
- [x] Requirements are testable without branch switching.
- [x] Requirements distinguish default evidence from opt-in infrastructure evidence.
- [x] Requirements avoid hidden dependency upgrades or package-management changes.
- [x] LLM score evidence now rejects heavyweight Ollama runtime closure and requires Docker-owned `llama.cpp` server plus Qwen3 Q4_K_M.
- [x] LLM score evidence requires a GitHub Actions-suitable Docker-full-package path without host LLM installs, local model files, external credentials, or a manually pre-running model server.

## Implementation Readiness

- [x] Tasks name exact source paths or commands.
- [x] Tasks are grouped by dependency order and score dimension.
- [x] Final closure requires tests, style checks, evidence recording, branch verification, and LLM runtime rebaseline evidence.
- [x] Historical Speckit evidence is not treated as automatic closure.
- [x] Previously completed Ollama LLM tasks are documented as historical evidence and do not close the reopened LLM score path.
