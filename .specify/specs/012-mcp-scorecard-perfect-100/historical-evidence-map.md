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

# Historical Evidence Map

Historical scorecard material from `011-mcp-llm-product-quality-100` is useful
only as traceability. This checkpoint can reuse a historical claim only when a
current `012` evidence entry revalidates the same behavior.

## Revalidated Mappings

- Production readability and diagnostics:
  historical 011 refactoring intent is revalidated by `EV-005`, `EV-006`,
  `EV-007`, and the current safety/limiter tests.
- Model-use friendliness and natural interaction:
  historical 011 model-first requirements are revalidated by `EV-019`,
  `EV-020`, `MCPModelFirstContractPayloadBuilderTest`,
  `ServerCapabilitiesHandlerTest`, and `protocol-evidence-matrix.md`.
- Default E2E lane:
  historical 011 default-lane confidence is revalidated by `EV-008` and
  `EV-009`.
- STDIO and packaged runtime:
  historical 011 transport evidence is revalidated by `EV-015`, `EV-017`,
  and `EV-018`.
- MySQL compatibility:
  historical 011 MySQL runtime assumptions are revalidated by `EV-016`.
- Live LLM usability:
  historical 011 live LLM score expectations are revalidated by `EV-019` and
  `EV-020`.

## Non-Reuse Rule

Historical artifacts that do not map to a current command above stay advisory
only and cannot raise a score to 100. The current gate is the evidence ledger in
this directory.
