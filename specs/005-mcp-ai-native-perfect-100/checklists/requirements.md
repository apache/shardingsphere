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

# Requirements Checklist: MCP AI-Native Perfect 100

## P0 Gate: Zero Guessing

- [ ] MCP100-P0-01: Universal `response_mode` is implemented and validated.
- [ ] MCP100-P0-02: Every `next_actions` entry is deterministic and approval-aware.
- [ ] MCP100-P0-03: Large metadata continuation semantics are unambiguous.
- [ ] MCP100-P0-04: Completion recovery points to nearest known resources.
- [ ] MCP100-P0-05: Single-schema auto-fill is deterministic and visible.
- [ ] MCP100-P0-06: Not-found and empty-state recovery has safe next moves.
- [ ] MCP100-P0-07: Public contract drift guard is deterministic and local.

## P1 Gate: Comfortable Native Use

- [ ] MCP100-P1-01: Readiness is secret-free and model-usable.
- [ ] MCP100-P1-02: Runtime visibility exists per known database.
- [ ] MCP100-P1-03: Workflow argument provenance is present.
- [ ] MCP100-P1-04: Redaction markers and summaries are consistent.
- [ ] MCP100-P1-05: Compact Chinese data-governance lexicon is available.
- [ ] MCP100-P1-06: Terminology is aligned across MCP surfaces.
- [ ] MCP100-P1-07: Deterministic local MCP client smoke exists.

## P2 Gate: Proof and Polish

- [ ] MCP100-P2-01: Optional correlation id is supported where runtime context exists.
- [ ] MCP100-P2-02: MCP packaging metadata hints are present where supported.
- [ ] MCP100-P2-03: Descriptor authoring lint catches contract drift.
- [ ] MCP100-P2-04: Maintained `100` scorecard exists with evidence.

## Final 100 Decision

- [ ] All required P0 gates pass.
- [ ] All required P1 gates pass.
- [ ] All required P2 gates pass.
- [ ] No known in-scope optimization remains after repeating the original review prompt.
- [ ] Final repeated answer can honestly be `100/100`.
