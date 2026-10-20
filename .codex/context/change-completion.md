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
4. Invoke `$review-pr` in Formal Review Mode for every code-writing task, including ordinary low-risk local changes, without requiring a separate user request.
   Policy and harness changes also require this Formal Review route.
   Use [review-pr](../skills/review-pr/SKILL.md) as the sole review entry and apply its Review Focus, Local Candidate Scope, Scope and Evidence, and Independent Review Coordination requirements.
   Existing authority, architecture, safety, and triggered high-risk gates remain required.
   - When the effective final task delta adds, extends or strengthens defensive code, assign an independent reviewer within the current task to audit that delta read-only under the existing defensive-code rules.
     Do not assign this audit when no such defensive change remains in the final task delta.
5. Hand off only when the selected review is confirmed, all required current-state checks pass, and no later code-affecting write or new evidence invalidates them.
   For a required Formal Review, the latest confirmed complete review must return `Review Result: Mergeable`.
6. When a confirmed Formal Review returns `Not Mergeable` with a safe in-scope required finding, fix it, rerun only invalidated audits and checks, and invoke the same review entry for the changed full candidate.
   Preserve the original task baseline and attributed scope through this fix loop, and continue until step 5 passes.
7. A scope expansion, missing authority, unresolved architecture choice, high-risk action, `Review Incomplete`, `Needs Discussion`, or unconfirmed review keeps the task incomplete.
   Report the exact decision, authority, unavailable evidence or concrete failure preventing continuation; yield for user action only when that action is required.
   Do not claim successful completion or provide commit artifacts while the task remains incomplete.
8. After every code-writing task completes, provide a nonempty `Commit Message:` without requiring a separate user request; this never authorizes Git mutation.
   Provide manual Git commands only when the user requests them.
   For requested manual Git commands, propose `git commit --dry-run --only` and `git commit --only` for only task-owned files without unrelated hunks.
   Include `git add -- <new-task-paths>` first only when requested manual instructions need to add untracked task files.
   Never execute a Git write without exact current-task authorization.

Each invocation in this loop, including the first and each call after a fix, must complete the review entry's full workflow for the current task-owned candidate.
Defensive-code audits, known-fix rechecks, passing checks, and ledger validation provide evidence but cannot replace that review.

Do not repeat a still-valid completed check, audit or review-confirmation process; required rounds within an unfinished confirmation follow the review entry.

If a report or verdict is disproved, correct it first and identify the evidence that changed the conclusion.
Repair a policy, schema, validator, prompt or regression case only when the task already authorizes that target and evidence proves an independent underlying defect.
Otherwise report the minimum additional scope instead of changing policy to justify the correction.

The final response must lead with the outcome and include changed files and rationale, verification status, remaining risks, and the next action only when one is still required.
For a completed task, include exactly one `Task Status: Completed` line and a non-empty `Evidence:` summary of the current audit, verification and selected review.
Include the actual completed Formal Review as one complete fenced `markdown` report with exactly one `Review Result: Mergeable` line and nonempty `### Result`, `### Evidence`, and `### Coverage` sections.
Report confirmed findings and required changes, or explicitly state that no blocking issues or recommended changes remain.
Keep `Task Status: Completed`, the outer `Evidence:` summary, and `Commit Message:` outside the review block.
Do not substitute bounded self-review for the required Formal Review or fabricate a passing verdict.

The project Stop hook checks the textual completion state after supported write tools change a non-prose repository artifact.
Any later code-affecting write marks the task pending again and requires current checks and the applicable passing review before completion.
When required user input blocks completion, include exactly one `Task Status: Awaiting User` line, non-empty `Required User Action:` and `Evidence:` summaries, and any actual non-passing Formal Review result relevant to that blocker.
If no Formal Review has run, state that fact instead of fabricating a report.
The hook may yield that turn but must retain the pending state.
Omit completed status and commit artifacts while the task remains incomplete.
The hook checks textual states; it does not prove risk classification, verification or review execution.
