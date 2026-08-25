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

# Change Completion Loop

For an authorized change, build, implement, or fix request, excluding a standalone restoration or rollback:

1. Perform the post-write task-delta audit required by the Strict Scope and Task-Delta Gate in `AGENTS.md`, using the read-only `git diff`, surrounding context, and acceptance checklist as evidence.
2. Confirm every task-introduced file and hunk is necessary, expected changed files contain only their frozen change intent, prohibited paths have zero task delta, direct reuse was considered, architecture changes were authorized, and every test protects owned behavior. Inspect Java changes for newly added local-variable `final`, scan changed code and Skills for local absolute paths, and remove only current-task violations.
3. Treat scripts, searches, formatting, compilation, passing tests, and benchmarks as evidence, not proof of semantic compliance. Judge the final behavior, contracts, architecture, user request, and affected performance paths directly; fix every safe in-scope violation instead of reporting it as an accepted risk.
4. Apply the `$code-simplification` trigger and limits defined in `.codex/context/cross-cutting-skills.md`.
5. Review the effective local candidate against the same code-correctness gates used by `$review-pr`: root cause and fix mapping, affected behavior, side effects and regressions, contracts and architecture, test validity, and adversarial cases. Apply `$code-review-and-quality` when available, or its equivalent review, for the general quality axes; it does not replace these shared correctness gates.
6. When the implementation targets an existing PR, apply the `$review-pr` pre-handoff workflow defined in `AGENTS.md`. PR-specific public-head and remote-evidence checks remain part of formal PR review.
7. Verify the functional and performance non-regression gate in `.codex/skills/code-implementation/references/rules/non-regression.md` after the last relevant write. Treat any invalidated or inconclusive required evidence as incomplete work, not residual risk.
8. Fix every safe in-scope required finding, rerun invalidated checks, and repeat the applicable reviews. If a finding requires scope expansion, an unresolved architecture choice, or a high-risk action, stop at its existing authorization gate instead of fixing it automatically.
9. Hand off and propose a commit message only after one complete applicable review pass finds zero new required issues. Before that review passes, do not hand off or propose a commit message. Do not defer a locally discoverable required finding to a later formal PR review.
10. Stop when no required in-scope finding remains. Do not iterate for optional polish, broad cleanup, or risky refactoring.

If a report or verdict is disproved, fix the highest-leverage rule, schema, validator, prompt, or regression case before correcting the artifact, unless the user explicitly requests a one-off correction.

The final response must lead with the outcome and include changed files and rationale, commands with exit codes, verification status, remaining risks, and the next action only when one is still required. When Git commit authorization is absent, provide a proposed commit message without staging or committing.
