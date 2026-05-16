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

# Requirements Checklist: MCP Encrypt/Mask Scorecard 100

## Branch and Scope

- [x] Current branch is `001-shardingsphere-mcp`.
- [x] No branch-changing command is required for this package.
- [x] Functional completeness is scoped to encrypt and mask workflows only.
- [x] Markdown is documented as optional prompt/report readability, not a tool-result requirement.

## Requirement Quality

- [x] Every active score dimension has current and target scores.
- [x] Every below-100 dimension has a closing evidence requirement.
- [x] Historical 100/100 claims are not accepted as current closure without revalidation.
- [x] Official MCP and MCP Java SDK sources are named.
- [x] Local source evidence targets are named.

## Implementation Readiness

- [x] Tasks are grouped by governance, protocol, functionality, safety, cleanliness, E2E, and final verification.
- [x] Coding tasks identify primary paths.
- [x] Verification tasks include Maven, Checkstyle, Spotless, and branch/status evidence.
- [x] No task requires editing generated `target/` files.

## Open Risks

- [x] Current score dimensions are 100/100 with attached evidence.
- [x] Fresh focused Maven evidence exists for `T010` through `T013`.
- [x] Full scoped Maven, Checkstyle, Spotless, E2E, distribution, and LLM commands are recorded for this package.
- [x] Jacoco-specific enforcement was not rerun in the final pass because this phase changed E2E/LLM evaluation contracts and evidence docs,
  while production branch coverage was already mapped in Phase 3 and Phase 4 evidence.
- [x] Focused Phase 2 evidence was collected on a dirty worktree and later covered by final scoped verification before score closure.
- [x] Historical Speckit files with 100/100 claims are explicitly treated as historical evidence rather than automatic current closure.
- [x] Opt-in MySQL, STDIO, distribution, packaged runtime, and LLM lanes had local Docker/Testcontainers infrastructure available and passed.
