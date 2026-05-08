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

# Research: MCP AI-Native Perfect 100

## Decision 1: Treat 100 as a Gate, Not an Average

Decision:

- `100` means every in-scope AI-native usability gate passes.
- A single missing required gate keeps the score below `100`.

Rationale:

- Averages hide sharp failures that are painful for LLM clients.
- The user's requirement is absolute: `100` means no known optimization space.

Rejected alternative:

- Weighted scoring by category. It allows high scores while known defects remain.

## Decision 2: Make Guessing the Primary Enemy

Decision:

- P0 focuses on removing every known place where a model has to infer server intent.

Rationale:

- MCP clients are most comfortable when the next valid action is encoded, not narrated.
- `response_mode`, `next_actions`, continuation semantics, and recovery categories are the central contract points.

Rejected alternative:

- Add more prose to prompts only. Prose helps humans but does not provide a durable machine contract.

## Decision 3: Use Search Continuation When Pagination Is Not Real

Decision:

- If direct metadata lists are capped and real pagination is not implemented, expose `continuation_mode=search_metadata`.

Rationale:

- A false `has_more` path is worse than a smaller result because it teaches the model to call non-existent pages.
- Search/narrow guidance is enough for the current MCP usability goal.

Rejected alternative:

- Build full pagination everywhere before reaching `100`. That expands implementation scope and may not be needed for clear model use.

## Decision 4: Auto-Fill Only When the Choice Is Deterministic

Decision:

- Single-schema auto-fill is allowed only when exactly one visible schema exists.
- Multiple schemas require a structured question.

Rationale:

- This removes repetitive user prompts without introducing hidden assumptions.

Rejected alternative:

- Guess the most common schema name. That is convenient but unsafe.

## Decision 5: Runtime Readiness Must Be Secret-Free

Decision:

- Readiness and runtime resources must expose status, not raw configuration secrets.

Rationale:

- LLM-native workflows frequently include diagnostic echoing. Secret-free status is safer and easier to share.

Rejected alternative:

- Return raw server properties and rely on the model to avoid leaking them.

## Decision 6: Keep the Chinese Lexicon Small

Decision:

- Add a compact lexicon for common governance terms only.

Rationale:

- The goal is to help search and planning, not to build a general natural-language understanding subsystem.

Rejected alternative:

- Add broad synonym dictionaries or embedding-based intent classification.

## Decision 7: Avoid Live LLM Gates

Decision:

- Required verification is deterministic and local.
- Live LLM checks may be used manually but do not block `100`.

Rationale:

- Live model behavior changes over time and can make CI flaky.
- Contract tests and descriptor linting are more stable evidence.

Rejected alternative:

- Use a live model prompt benchmark as the only proof of `100`.

