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

1. Perform the post-write task-delta audit required by `AGENTS.md` with the read-only diff, surrounding context, and acceptance checklist. Confirm every task-introduced hunk is necessary and allowlisted, prohibited paths have no task delta, architecture changes were authorized, tests protect owned behavior, and unattributed work remains untouched.
2. Apply every triggered coding-standard and simplification rule to the effective task delta. Treat scripts, searches, formatters, compilation, tests, benchmarks, and review tools as evidence rather than proof, and fix every safe in-scope violation.
3. Run the applicable verification and non-regression checks. An invalidated, unmatched, missing, or inconclusive required result keeps the task incomplete.
4. Invoke `$review-pr` in Formal Review Mode for the complete task-owned candidate. Use Code Correctness Review for a standalone local candidate unless the user requests another focus. For a PR-backed candidate, apply the public-head, remote-evidence, and mandatory style-verification gates; for a standalone candidate, review only the original task baseline and attributed task-owned delta.
5. Hand off only when the latest complete Formal Review returns `Review Result: Mergeable` and no later code-affecting write or new evidence invalidates it. Include that complete passing Formal Review report unchanged in the final handoff.
6. When Formal Review returns `Not Mergeable` with a safe in-scope Change Request, fix every required finding, repeat the task-delta audit, rerun invalidated checks, and rerun Formal Review. Continue this loop until it returns `Review Result: Mergeable`.
7. A scope expansion, missing authority, unresolved architecture choice, high-risk action, `Review Incomplete`, or `Needs Discussion` result keeps the task incomplete. Yield to the user only for the exact decision, authority, or unavailable evidence needed to continue; do not claim successful completion or provide commit artifacts while the task remains incomplete.
8. After a code-writing task completes, provide a commit message without being asked; this never authorizes Git mutation. Provide manual Git commands only when the user requests them. Propose `git commit --dry-run --only` and `git commit --only` for only task-owned files without unrelated hunks, list a required `git add -- <new-task-paths>` first for untracked task files, and never execute a Git write without exact current-task authorization.

When the delta audit, required current-state checks, and applicable review pass and no later write or new evidence invalidates them, the loop is complete. Do not repeat an unchanged check, audit, or review.

If a report or verdict is disproved, fix the highest-leverage rule, schema, validator, prompt, or regression case before correcting the artifact, unless the user explicitly requests a one-off correction.

The final response must lead with the outcome and include changed files and rationale, verification status, remaining risks, and the next action only when one is still required. Every successful code-writing handoff must include the latest complete passing Formal Review as the exact fenced `markdown` artifact required by `$review-pr`, with `### Result`, exactly one `Review Result: Mergeable`, `### Evidence`, and `### Coverage`. Put required non-review handoff details, including a non-empty `Commit Message:`, in a separate fenced text block so the Formal Review artifact remains unchanged. Validation results, passing tests, a bare verdict, a bounded review summary, or commit artifacts do not substitute for the Formal Review report.

The project Stop hook validates this contract after supported write tools change a non-prose repository artifact. Any later code-affecting write marks the task pending again and requires a new passing Formal Review. When step 7 requires user input, include the complete non-passing Formal Review plus separate `Task Status: Awaiting User` and non-empty `Required User Action:` lines; the hook may yield that turn but must retain the pending state. Omit `Commit Message:` until the task succeeds.
