<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Optional Cross-Cutting Skills

The Skills in this file are optional workflow accelerators, not prerequisites. Availability alone is not a trigger. When an optional Skill is available and its trigger below applies, prefer it and use only the smallest matching Skill set. If it is unavailable, skip the Skill itself and continue with the applicable repository gates and ordinary workflow. Do not install, create, reconstruct, or treat the absence of an optional Skill as a blocker. Mention the absence only when the user explicitly requested the Skill or it leaves a material residual risk.

Skipping a Skill never waives compatibility, correctness, security, scope, verification, or completion requirements. Using a Skill never expands the frozen task scope, allowed modules or files, acceptance checklist, write or remote authority, Git authority, or tool permissions. `AGENTS.md` prevails when a Skill conflicts with it.

- Implementation or review whose correctness depends on the current version of an external framework or library: use `$source-driven-development` when available. Identify the applicable version from repository dependency metadata, verify the relevant decision against authoritative primary documentation, record the source, and mark anything that cannot be verified as unverified. This verification remains required without the Skill. Do not invoke it for version-independent local logic.
- Designing, adding, or changing a public API, SPI, extension, loading or registration contract, module boundary, externally visible interface, or cross-module type contract: use `$api-and-interface-design` when available before implementation. Apply the Architecture Change Gate in `AGENTS.md` and the contract rules in `.codex/skills/code-implementation/references/rules/contracts-and-removal.md` whether or not the Skill is available; using it does not authorize the change or expand the confirmed scope.
- An unexpected test, build, runtime, or behavior failure: use `$debugging-and-error-recovery` when available. Stop unrelated implementation, preserve evidence, reproduce, localize, reduce, identify the root cause, and verify the result. Add focused regression protection only when the root cause is an in-scope production defect and the test protects meaningful owned behavior; do not add a test for an environment, infrastructure, or unrelated failure. A failure never authorizes an out-of-boundary fix or a Git write.
- A task-specific performance requirement beyond the repository-wide non-regression gate, reproducible reported slowness or suspected regression, profiling evidence, or a requested change that can materially alter the cost of a confirmed performance-sensitive path: use `$performance-optimization` when available to measure first. A path being high-volume by itself is not a trigger. Freeze the workload, metric, environment, and baseline; identify the owning bottleneck before editing; test one hypothesis at a time; and repeat the same measurement with correctness checks. Keep an optimization experiment only when the improvement exceeds run-to-run variance and correctness remains intact. Remove neutral, worse, or incorrect current-task optimization experiments. Record attempts in the task unless an exact file or remote target is separately authorized. Do not optimize by intuition or follow adjacent hotspots outside the frozen boundary. This experiment rule does not prohibit a performance-neutral feature or correctness change that passes `.codex/skills/code-implementation/references/rules/non-regression.md`.
- After scoped behavior and checks pass, when the effective task delta contains concrete unnecessary complexity or a review identifies it: use `$code-simplification` when available on that delta only. Preserve behavior exactly and never perform a drive-by refactor or gain Git authority from the Skill. Do not invoke it merely because the completion loop is running or when the candidate is already clear.
- An explicit user request to threat model a repository or path, enumerate threats or abuse paths, or perform AppSec threat modeling: use `$security-threat-model` when available. Do not invoke it for an ordinary architecture summary, code review, security check, or non-security design. The request remains read-only unless the user also authorizes the exact output file; the Skill's default artifact creation grants no write authority.
- A non-trivial decision involving module boundaries, public contracts, unfamiliar code, or high-risk behavior, or a branching invariant that is not directly established by types, existing tests, or an explicit contract and whose failure would materially affect correctness, compatibility, security, or another high-risk behavior: use `$doubt-driven-development` when available only within the active task. An ordinary branch is not a trigger. The prohibition on additional Codex tasks and external review overrides its fresh-context and cross-model steps. Perform bounded adversarial self-review instead and do not report the full Skill as completed.

Do not include sensitive repository data in external searches.
