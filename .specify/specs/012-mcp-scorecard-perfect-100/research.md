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

## Decision 6: Make Official MCP Specification the Protocol Source of Truth

- Decision: Use MCP Specification `2025-11-25` as the authoritative source for protocol semantics.
- Rationale: The user requires all implementation to use MCP standards and forbids project-specific protocol invention.
- Sources: `https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle`, `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`,
  `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`, `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`,
  `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`, and `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion`.
- Alternatives considered: Treat existing `shardingsphere://capabilities` payloads as protocol discovery; rejected because domain resources cannot replace official MCP discovery methods.

## Decision 7: Verify SDK Usage Against Detected Version

- Decision: Treat MCP Java SDK documentation as implementation guidance only after checking compatibility with SDK `1.1.2` or local dependency source.
- Rationale: Current online SDK docs can describe newer versions, while this branch uses `mcp-core` and `mcp-json-jackson2` version `1.1.2`.
- Alternatives considered: Follow the latest SDK snippets directly; rejected because source-driven-development requires version-aware implementation.

## Decision 8: Reopen Perfect-100 Status Under the Standard-First Gate

- Decision: Previous 100-point score evidence remains historical until it is mapped to official MCP standard evidence.
- Rationale: A perfect score must prove official MCP conformance, not only local ShardingSphere contract success.
- Alternatives considered: Leave the 2026-05-11 closed status unchanged; rejected because the user added a stricter MCP-standard-only requirement.
