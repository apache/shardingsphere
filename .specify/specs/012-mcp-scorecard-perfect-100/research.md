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

# Research: MCP Scorecard Perfect 100

## Decision 1: Create a New Speckit Package

- Decision: Create `.specify/specs/012-mcp-scorecard-perfect-100/`.
- Rationale: Existing `011-mcp-llm-product-quality-100` records historical 100-point evidence, while the latest independent review scored the current surface below 100.
- Alternatives considered: Update `011`; rejected because it would blur historical checkpoint evidence with the new gap-driven requirement.

## Decision 2: Avoid Branch-Changing Speckit Commands

- Decision: Maintain Speckit files manually from templates.
- Rationale: The user explicitly forbids switching git branches, and the standard Speckit create-feature flow creates and checks out a branch.
- Alternatives considered: Run `.specify/scripts/bash/create-new-feature.sh`; rejected because it violates the branch constraint.

## Decision 3: Use All-Dimensions-Full-Score Semantics

- Decision: Every score dimension is an independent 100/100 gate.
- Rationale: The user requires every item to be 100. Average scores can hide individual weaknesses.
- Alternatives considered: Weighted aggregate score; retained only as diagnostic context, not as completion logic.

## Decision 4: Evidence Is Required for Every 100 Claim

- Decision: A dimension can reach 100 only with command output, artifact evidence, or contract evidence.
- Rationale: `CODE_OF_CONDUCT.md` and `AGENTS.md` emphasize traceability, verification, and build quality.
- Alternatives considered: Manual reviewer judgment only; rejected because it is not repeatable enough for a strict 100 target.
- Waivers and exception records are not allowed.
  Missing evidence stays as an open risk and keeps the dimension below 100.

## Decision 5: Keep Project PR Gates Unchanged

- Decision: This package does not change the project's existing PR gate.
- Rationale: Live LLM, Docker, MySQL, STDIO, and distribution lanes are score evidence, not new default PR requirements.
- Alternatives considered: Make every lane a default PR gate; rejected because the user explicitly forbids adding PR-gate scope.
