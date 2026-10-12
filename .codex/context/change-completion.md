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

This file is the sole owner of completion-loop orchestration. Other policy sources may route here or define their own implementation and verification rules, but must not duplicate this sequence or its review selection.

For every authorized code-writing task, including a restoration or rollback that changes production, test, script, build, generated, or behavior-affecting configuration artifacts:

1. Confirm the post-write task-delta audit required by `AGENTS.md` is current for the read-only diff, surrounding context, and acceptance checklist.
   Confirm every task-introduced hunk is necessary and allowlisted, prohibited paths have no task delta, architecture changes were authorized, tests protect owned behavior, and unattributed work remains untouched.
2. Apply any missing or invalidated triggered coding-standard and simplification checks to the effective task delta.
   Treat scripts, searches, formatters, compilation, tests, benchmarks, and review tools as evidence rather than proof, and fix every safe in-scope violation.
3. Run the applicable verification and non-regression checks that lack valid current-state evidence.
   An invalidated, unmatched, missing, or inconclusive required result keeps the task incomplete.
4. Select the review route from the existing code-implementation risk preflight and current owner, contract, and verification evidence.
   Use one bounded self-review only for an ordinary local implementation whose preflight and current evidence establish low risk, with no public or shared contract change, cross-owner impact, architecture change, security, concurrency, lifecycle or SPI impact, or credible functional or performance risk.
   Invoke `$review-pr` in Formal Review Mode for explicit code or PR reviews, PR-backed candidates, policy or harness changes, and tasks with security, concurrency, lifecycle or SPI impact or high or unproved risk.
   Existing authority, architecture, safety, and triggered high-risk gates remain required on either route.
   For Formal Review, use Code Correctness Review for a standalone local candidate unless the user requests another focus.
   For Formal Review of a PR-backed candidate, apply the public-head, remote-evidence, and mandatory style-verification gates.
   For Formal Review of a standalone candidate, review only the original task baseline and attributed task-owned delta.
   - When the effective final task delta adds, extends or strengthens defensive code, assign an independent reviewer within the current task to audit that delta read-only under the existing defensive-code rules.
     Do not assign this audit when no such defensive change remains in the final task delta.
5. Hand off only when the selected review and all required current-state checks pass and no later code-affecting write or new evidence invalidates them.
   For a required Formal Review, the latest complete review must return `Review Result: Mergeable`.
6. When either review route identifies a safe in-scope required finding, fix it, rerun only invalidated audits and checks, and repeat the selected review.
   Retain the selected Formal Review route through its finding-fix loop.
   Continue until the selected review passes; a Formal Review must return `Review Result: Mergeable`.
7. A scope expansion, missing authority, unresolved architecture choice, high-risk action, `Review Incomplete`, or `Needs Discussion` result keeps the task incomplete.
   Yield to the user only for the exact decision, authority, or unavailable evidence needed to continue; do not claim successful completion or provide commit artifacts while the task remains incomplete.
8. Provide a commit message or manual Git commands only when the user requests them; this never authorizes Git mutation.
   For requested manual Git commands, propose `git commit --dry-run --only` and `git commit --only` for only task-owned files without unrelated hunks.
   Include `git add -- <new-task-paths>` first only when requested manual instructions need to add untracked task files.
   Never execute a Git write without exact current-task authorization.

Every Formal Review in this loop, including the first review and each rerun after a fix, must execute the complete `$review-pr` workflow over the entire current task-owned candidate from the original task baseline and its affected behavior and contracts.
Defensive-code audits, known-fix rechecks, passing checks, and ledger validation provide evidence but cannot replace that review.

When the delta audit, required current-state checks, and applicable review pass and no later write or new evidence invalidates them, the loop is complete. Do not repeat an unchanged check, audit, or review.

If a report or verdict is disproved, correct it first and identify the evidence that changed the conclusion.
Repair a policy, schema, validator, prompt or regression case only when the task already authorizes that target and evidence proves an independent underlying defect.
Otherwise report the minimum additional scope instead of changing policy to justify the correction.

The final response must lead with the outcome and include changed files and rationale, verification status, remaining risks, and the next action only when one is still required.
For a completed task, include exactly one `Task Status: Completed` line and a non-empty `Evidence:` summary of the current audit, verification and selected review.
When Formal Review is required, also include exactly one `Review Result: Mergeable` line from the actual completed review and retain its substantive required evidence and coverage without repeating its complete report.
Do not manufacture a Formal Review verdict for a task completed by bounded self-review.

The project Stop hook checks the textual completion state after supported write tools change a non-prose repository artifact.
Any later code-affecting write marks the task pending again and requires current checks and the applicable passing review before completion.
When required user input blocks completion, include exactly one `Task Status: Awaiting User` line, non-empty `Required User Action:` and `Evidence:` summaries, and any actual non-passing Formal Review result relevant to that blocker.
If no Formal Review has run, state that fact instead of fabricating a report.
The hook may yield that turn but must retain the pending state.
Omit completed status and commit artifacts while the task remains incomplete.
The hook checks textual states; it does not prove risk classification, verification or review execution.
