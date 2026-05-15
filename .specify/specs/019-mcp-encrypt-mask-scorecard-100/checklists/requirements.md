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

- [ ] Current score dimensions are not yet 100/100.
- [ ] No fresh Maven, Checkstyle, Spotless, Jacoco, E2E, distribution, or LLM commands have been recorded for this package.
- [ ] Some historical Speckit files still contain 100/100 claims that require current revalidation or explicit historical labeling.
- [ ] Opt-in MySQL, STDIO, distribution, packaged runtime, and LLM lanes may require local infrastructure.
