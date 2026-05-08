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

# Tasks: MCP AI-Native Perfect 100

## Phase 0: Speckit Baseline

- [ ] T001 Confirm no branch switch occurred before implementation.
- [ ] T002 Keep this Speckit package aligned with `spec.md`, `requirements.md`, and `checklists/requirements.md`.
- [ ] T003 Record every future in-scope usability gap as a requirement before implementation.

## Phase 1: P0 Zero-Guessing Contract

- [ ] T010 Locate MCP descriptor, prompt, tool, resource, and structured error builders.
- [ ] T011 Add shared `response_mode` vocabulary and validation.
- [ ] T012 Apply `response_mode` to every public tool/resource/prompt/descriptor/error example.
- [ ] T013 Normalize `next_actions` entries with `order`, `type`, `title`, and `requires_user_approval`.
- [ ] T014 Add conditional `depends_on`, `tool_name`, `arguments`, `resource_uri`, and `question` validation.
- [ ] T015 Fix large metadata continuation semantics for direct list responses.
- [ ] T016 Add explicit `continuation_mode=search_metadata` where real pagination is absent.
- [ ] T017 Route missing `database`, `schema`, `table`, `column`, and `index` completion to nearest resources.
- [ ] T018 Add deterministic single-schema auto-fill to completion and workflow planning.
- [ ] T019 Normalize not-found, empty-scope, ambiguous, missing-context, validation, and terminal recovery categories.
- [ ] T020 Add contract tests for `MCP100-P0-01` through `MCP100-P0-07`.

## Phase 2: P1 Comfortable Native Use

- [ ] T030 Add secret-free readiness response or resource.
- [ ] T031 Add per-database runtime visibility resource.
- [ ] T032 Add workflow argument provenance for planned arguments.
- [ ] T033 Standardize redaction markers and summaries.
- [ ] T034 Add compact Chinese data-governance lexicon for planning/search hints.
- [ ] T035 Align terminology across prompts, tool descriptions, resources, and capability descriptors.
- [ ] T036 Add deterministic local MCP client smoke coverage for discovery, read-only calls, completion, and recovery.
- [ ] T037 Add focused tests for `MCP100-P1-01` through `MCP100-P1-07`.

## Phase 3: P2 Proof and Polish

- [ ] T040 Add optional request/correlation id propagation for recovery-safe responses where runtime context exists.
- [ ] T041 Add MCP packaging metadata hints for existing STDIO and Streamable HTTP entry points.
- [ ] T042 Add descriptor authoring lint for response modes, navigation/action types, and examples.
- [ ] T043 Add maintained `100` scorecard artifact.
- [ ] T044 Add focused tests or lint coverage for `MCP100-P2-01` through `MCP100-P2-04`.

## Phase 4: Verification and Final Score

- [ ] T050 Run branch check and confirm current branch remains `001-shardingsphere-mcp`.
- [ ] T051 Run documentation whitespace checks for this Speckit package.
- [ ] T052 Run scoped MCP unit tests after runtime changes.
- [ ] T053 Run scoped Checkstyle/Spotless after Java changes.
- [ ] T054 Update the scorecard with command evidence.
- [ ] T055 Answer the repeated review prompt as `100/100` only if every required gate is passing.

