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

# Quickstart: AI-Friendly MCP Experience Hardening

This quickstart defines the validation path for the next increment. It is not an implementation script.

## Prerequisites

- `git branch --show-current` reports `001-shardingsphere-mcp`.
- MCP prompt, completion, resource, tool, workflow, and preview baseline is available.
- Real-model E2E is disabled by default and enabled only by an explicit profile or environment switch.
- Side-effecting tools require explicit preview, execute, or approval-bound execution mode before state changes.
- Descriptor lint, golden transcripts, transport contracts, and model-confusion tests are deterministic and run without real-model credentials.

## Scenario 1: Documentation Correction

1. Read `mcp/README.md` and `mcp/README_ZH.md`.
2. Verify prompt and completion support is documented as implemented, not deferred.
3. Verify preview and approval guidance remains explicit.
4. Verify `execute_update` no longer describes omitted `execution_mode` as a direct execution path.

Expected result:

- Documentation matches the current protocol-visible MCP surface.

## Scenario 2: Transcript Golden Guard

1. Start the MCP runtime through an existing deterministic test harness.
2. Capture `initialize`, `tools/list`, `resources/list`, `resources/templates/list`, `prompts/list`, representative `completion/complete`, and `shardingsphere://capabilities`.
3. Capture descriptor, prompt, navigation, and schema fingerprints.
4. Normalize dynamic fields and unordered collections.
5. Compare the payloads with approved golden fixtures.

Expected result:

- Tests fail if model-facing contract metadata regresses.

## Scenario 3: Opt-In Real-Model E2E

1. Enable the real-model E2E profile outside default CI.
2. Provide non-production model credentials and deterministic ShardingSphere fixtures.
3. Run metadata, safe SQL, workflow, and recovery scenarios.
4. Assert prompt calls, completion calls, resource reads, tool order, schema-valid arguments, preview-before-execution, approval boundary, recovery path, and final validation.
5. Verify reports include provider, model identifier, scenario ID, failure classification, and model-facing fingerprints.
6. Verify logs and artifacts redact credentials.

Expected result:

- A real model can use the MCP surface naturally, and the assertion result is based on observable MCP traces.

## Scenario 4: Completion Ranking

1. Request completions with exact prefixes, partial prefixes, and missing context.
2. Request table and column completions with database/schema/table context.
3. Request `plan_id` completion with multiple current-session plans.
4. Request algorithm completion from encrypt and mask references.
5. Trigger missing-context completion.

Expected result:

- Returned values are deterministic, directly reusable, and ordered by exact match, context strength, plan recency, or feature context.
- Diagnostic metadata explains ranking source or missing context without changing returned values.

## Scenario 5: Structured Recovery

1. Trigger missing argument, invalid enum, unsupported resource, wrong SQL tool, unsupported metadata object, and unavailable plan errors.
2. Verify each recoverable error includes a consistent recovery envelope.
3. Verify safe suggested arguments are present only when the server knows them.
4. Verify side-effecting recovery keeps preview and approval requirements.
5. Verify native elicitation or structured fallback fields identify pending user questions when the server cannot infer intent.

Expected result:

- A model-like caller can repair common failures without guessing hidden values.

## Scenario 6: Resource Navigation

1. Read the capability payload.
2. Inspect `resourceNavigation`.
3. Verify relationships are loaded from descriptor-owned metadata.
4. Verify metadata hierarchy, algorithm-to-planning, and workflow next-hop relationships.
5. Verify every navigation endpoint resolves to a public resource, prompt, or tool.

Expected result:

- A model can identify the next public MCP hop without a runtime graph engine.

## Scenario 7: Explicit Side-Effect Mode

1. Call `execute_update` without `execution_mode`.
2. Verify the call is rejected before execution.
3. Verify recovery recommends `execution_mode=preview`.
4. Call `execute_update` with `execution_mode=preview`.
5. Verify only preview output is returned.

Expected result:

- Omitted mode never executes a side-effecting statement.

## Scenario 8: Descriptor Lint and Fingerprints

1. Run descriptor lint tests.
2. Verify title, description, field description, enum description, output schema, annotation, safety hint, completion target, prompt link, navigation, and example rules.
3. Read capabilities and verify descriptor, prompt, navigation, and schema fingerprints.

Expected result:

- Model-facing descriptor quality is enforced and capability versions are traceable.

## Scenario 9: Next-Action and Prompt Stop Conditions

1. Read representative tool outputs, resource outputs, prompt outputs, and recovery envelopes.
2. Verify shared fields such as `suggested_next_tool`, `suggested_arguments`, `read_resources_first`, `requires_user_approval`, and `ask_user_when_uncertain`.
3. Retrieve workflow prompts and verify stop conditions and ask-user conditions.

Expected result:

- Models see the same next-step vocabulary across the MCP surface.

## Scenario 10: Transport Contracts

1. Call `prompts/get` through HTTP and STDIO transport where available.
2. Call `completion/complete` through transport.
3. Read capabilities through transport.
4. Verify prompt stop conditions, completion diagnostics, fingerprints, navigation, and explicit side-effect mode requirements are visible.

Expected result:

- Model-facing fields are proven at the protocol layer, not only by factory tests.

## Scenario 11: Ergonomics and Model-Confusion Tests

1. Run naming clarity checks for read-only, preview, side-effecting, planning, validation, and lookup actions.
2. Verify pagination fields for large result surfaces.
3. Verify progress or structured progress metadata for long-running workflows when supported.
4. Verify sampling and logging are used only when stable SDK support and concrete workflow need exist.
5. Verify future file or configuration resources expose roots or permission boundaries before access.
6. Run model-confusion tests for apply-before-plan, execute-before-preview, missing execution mode, stale `plan_id`, unknown database, ambiguous metadata, invalid enum, and wrong SQL tool.

Expected result:

- Common model mistakes recover through structured fields and no unsafe side effect occurs.

## Verification Commands

Use scoped commands after implementation:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests checkstyle:check
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -Pcheck -DskipTests spotless:check
```

Also verify no branch switch occurred:

```bash
git branch --show-current
```
