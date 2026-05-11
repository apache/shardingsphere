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

# Requirements Checklist: MCP LLM Product Quality 100

## Completeness

- [x] Branch constraint is explicit.
- [x] Full MCP production and E2E scope is explicit.
- [x] Strict baseline score is recorded as 78/100.
- [x] 100-point target uses weighted dimensions.
- [x] Natural LLM usability is separated from protocol contract testing.
- [x] Core blocking LLM scenarios are separated from extended scored LLM scenarios.
- [x] Extended scenarios retain hard assertions for deterministic failures.
- [x] Golden contract and schema drift protection are required.
- [x] Recovery self-healing is required.
- [x] Safety and approval boundaries are required.
- [x] Runtime diagnostics and packaged readiness are required.
- [x] Live LLM tests are opt-in and not default CI blockers.
- [x] Mandatory live LLM gate model is fixed to Dockerized Ollama `qwen3:1.7b`.
- [x] MCP E2E owns Ollama startup and model pull for the live gate.
- [x] Mandatory live gate does not require a paid external API key.
- [x] Compatibility is explicitly out of scope for the unreleased MCP contract.
- [x] Packaged HTTP diagnostics are mandatory and STDIO diagnostics are practical-scope.
- [x] The independent 80-dimension score model is captured in `eighty-dimension-requirements.md`.
- [x] The target is every independent score item reaching 100, not an additive total.
- [x] The latest MCP and MCP E2E independent baselines are recorded.
- [x] Current blocking findings are captured as explicit requirements and checkpoint evidence records the resolved blockers.
- [x] Recommended defaults are recorded as confirmed decisions.
- [x] Automated evidence is mandatory for every final 100-point score claim.
- [x] H2 and MySQL are mandatory evidence; PostgreSQL and openGauss are optional unless touched.
- [x] Standalone runtime is mandatory evidence; cluster/governance is risk-tracked unless touched.

## Clarity

- [x] Requirements avoid implementation-by-comment or JavaDoc.
- [x] Requirements exclude compatibility shims unless the user changes the unreleased-contract decision.
- [x] Tasks are grouped by P0/P1/P2 priority.
- [x] Tasks name likely target paths where useful.
- [x] Verification tasks are separated from implementation tasks.
- [x] Speckit branch creation is explicitly forbidden for this sweep.
- [x] Native LLM tool-call scoring is separated from harness recovery scoring.
- [x] Manual review cannot be the only evidence for a 100 score.
- [x] Refactoring scope is constrained to small reviewable slices with a clear quality reason.

## Risk Coverage

- [x] Secret leakage is covered.
- [x] Docker/Ollama availability and model pull are covered.
- [x] Public contract drift is covered.
- [x] SQL side effects are covered.
- [x] Workflow approval and stale context are covered.
- [x] Runtime and packaged diagnostic failures are covered.
- [x] Side-effect execution requires explicit approval while preview remains allowed.
- [x] Extended scenarios hard-fail if unapproved side effects are not blocked.
- [x] Stale descriptor contract assertions are called out as a P0 gate and fixed by semantic assertions.
- [x] Fixed-sleep live-model readiness is called out as a stability risk and fixed by bounded polling.
- [x] Native tool-call scoring is protected from harness recovery over-crediting.
- [x] The remaining `git diff --check` and Spotless conflict is documented as a final-gate caveat.
