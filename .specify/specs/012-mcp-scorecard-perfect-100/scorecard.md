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

# Scorecard: MCP Scorecard Perfect 100

## Rule

Every item below has target score **100/100**.
The feature is incomplete while any item is below 100 or lacks evidence.

## MCP Production Module Dimensions

| Dimension | Current | Target | Gap To Close For 100 |
|-----------|--------:|-------:|----------------------|
| Model-use friendliness | 90 | 100 | Reduce descriptor density, remove duplicate override shapes, and prove zero-guessing discovery with model-facing tests. |
| Natural interaction quality | 88 | 100 | Hide low-level SQL, DistSQL, and artifact concepts behind more natural guided paths where possible. |
| Clarity | 91 | 100 | Split or summarize dense README and descriptor surfaces so first-time readers and models get a compact path first. |
| Code readability | 84 | 100 | Reduce oversized classes such as `MCPErrorConverter`, `MCPToolSpecificationFactory`, and HTTP servlet responsibilities. |
| Architecture clarity | 88 | 100 | Move request-scope service assembly and transport concerns behind clearer boundaries. |
| Decoupling | 84 | 100 | Reduce static registry pressure and hardcoded handler context type limits. |
| Protocol correctness | 90 | 100 | Add or record external conformance-style evidence beyond local contract tests. |
| Stability | 86 | 100 | Bound session, workflow, SQL scanner, and runtime cleanup edge cases with repeatable negative tests. |
| Diagnostics | 92 | 100 | Keep recovery useful while shrinking centralized policy sinks and proving diagnostics remain compact. |
| Safety | 88 | 100 | Add stronger audit, approval, auth, and SQL classifier evidence; missing evidence keeps the score below 100. |
| Extensibility | 87 | 100 | Make feature and context extension cheaper without manual synchronization across many surfaces. |
| Performance and resource use | 82 | 100 | Add measurable budgets for request scope creation, metadata lookup, SQL execution, and descriptor generation. |
| Configuration and distribution | 90 | 100 | Make distribution, plugin, driver, HTTP, and STDIO verification repeatable with recorded evidence. |
| Compatibility | 84 | 100 | Prove Java 21, Proxy-only, V1 identifier, dialect, and runtime compatibility decisions with tests or documented evidence. |
| Test quality | 88 | 100 | Replace remaining style risks, add coverage or Jacoco evidence, and keep scoped checks green. |

## MCP E2E Module Dimensions

| Dimension | Current | Target | Gap To Close For 100 |
|-----------|--------:|-------:|----------------------|
| Model-use friendliness | 88 | 100 | Make live LLM evidence repeatable and not dependent on unverified single-run behavior. |
| Natural interaction quality | 86 | 100 | Expand natural scenario diversity beyond harness-shaped prompts. |
| Clarity | 87 | 100 | Make default versus opt-in lane coverage obvious from one reader-facing entry point. |
| Code readability | 84 | 100 | Reduce large LLM conversation/support classes and brittle Map/string payload assertions. |
| Architecture clarity | 88 | 100 | Clarify boundaries between runtime fixtures, LLM harness, distribution support, and production clients. |
| Decoupling | 82 | 100 | Isolate Docker, Ollama, MySQL, STDIO, and distribution assumptions behind configurable fixtures. |
| Protocol correctness | 91 | 100 | Add external conformance-style evidence or document why local golden contracts are sufficient. |
| End-to-end realism | 90 | 100 | Record mandatory MySQL, STDIO, distribution, packaged runtime, and LLM evidence for the perfect gate. |
| Stability | 78 | 100 | Remove flake sources from Docker/model/polling paths or bound them with deterministic diagnostics. |
| Diagnostics | 90 | 100 | Make artifact and trace failures self-triaging enough for reviewers. |
| Safety | 89 | 100 | Add per-user, rate, abuse, and external-model safety evidence; missing evidence keeps the score below 100. |
| Extensibility | 84 | 100 | Make new feature scenarios cheaper by reducing golden, descriptor, fixture, and model-contract friction. |
| Performance and resource use | 76 | 100 | Define and prove acceptable time/resource budgets for full E2E and live LLM lanes. |
| Configuration and distribution | 85 | 100 | Make reproduction commands and prerequisites complete for all mandatory lanes. |
| Compatibility | 83 | 100 | Cover Docker, Ollama, Java 21, MySQL, STDIO, and model-provider compatibility with current evidence. |
| Test quality | 89 | 100 | Strengthen assertions, keep LLM lane evidence current, and add coverage or contract drift evidence. |

## Exit Gate Policy

- A score can move to 100 only after every listed gap for that dimension has passing evidence.
- Evidence can be a scoped Maven command, a golden contract snapshot, a live LLM artifact, or a packaged runtime smoke result.
- Waivers and exception records are not allowed.
- Missing evidence remains an open risk and keeps the dimension below 100.
- Average score is not a gate. The only completion gate is every dimension at 100.

## Current Completion Status

- MCP production dimensions at 100: **0/15** for this new checkpoint.
- MCP E2E dimensions at 100: **0/16** for this new checkpoint.
- Overall status: **open**.

The current scores are intentionally retained until future implementation and verification update them.

## Current Progress Evidence

This checkpoint now has current implementation evidence in `evidence-ledger.md`:

- `EV-005` to `EV-007`: production readability, diagnostics, and concrete surface tests.
- `EV-008`: E2E golden contract drift and runtime negative-state coverage.
- `EV-009`: final default-lane MCP plus MCP E2E regression command.
- `EV-010`: MCP feature module regression command.
- `EV-011` and `EV-012`: final Checkstyle and Spotless gates.

These items close the recommended default-lane implementation slice.
The next runtime evidence slice is also current:

- `EV-014`: E2E readiness diagnostics for STDIO stderr, Docker readiness, and packaged distribution missing-home cases.
- `EV-015`: STDIO runtime command, `84` tests with `0` skipped.
- `EV-016`: MySQL HTTP plus STDIO command, `22` tests with `0` skipped against Docker-backed `mysql:8.0.36`.
- `EV-017` and `EV-018`: packaged distribution assembly plus HTTP, STDIO, and plugin smoke commands.
- `EV-019`: live LLM smoke plus usability command, `5` tests with `0` skipped; core and extended scorecards both `100/100`.
- `EV-020`: live LLM harness guardrails for shared Ollama runtime, required tool-call coverage, and full-score extended-suite enforcement.

They do not mark all dimensions 100 because performance-budget, safety-boundary, protocol-conformance,
historical revalidation, and decoupling gaps remain open.
