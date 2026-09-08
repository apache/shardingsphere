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
4. Apply the `$code-simplification` trigger and limits defined in `.codex/context/cross-cutting-skills.md`. Apply `$code-review-and-quality` when available, or its equivalent review, for the general quality axes; it does not replace the required `$review-pr` Formal Review.
5. Run the applicable verification in `.codex/skills/code-implementation/references/verification.md`, including the final file-filtered Spotless and Checkstyle commands, then verify the functional and performance non-regression gate in `.codex/skills/code-implementation/references/rules/non-regression.md`. Treat any invalidated, unmatched, or inconclusive required evidence as incomplete work, not residual risk.
6. Invoke `$review-pr` in Formal Review Mode on the effective local candidate for every task covered by this loop. For a standalone local task, review the original task baseline plus only the task-owned delta; for a task targeting an existing PR, also apply the PR-specific public-head and remote-evidence checks.
7. If Formal Review returns `Not Mergeable` with safe in-scope required findings, fix them, return to the task-delta audit, rerun every invalidated check, and invoke `$review-pr` again. Do not defer a locally discoverable required finding to another review.
8. If Formal Review returns `Review Incomplete`, `Not Mergeable` with `Feedback Mode: Needs Discussion`, or a finding requires scope expansion, an unresolved architecture choice, or a high-risk action, stop at the existing evidence, decision, or authorization gate and keep the task incomplete.
9. Hand off only after the latest complete Formal Review returns `Review Result: Mergeable`, and include that final formal report as its own fenced `markdown` block. Do not present an earlier or non-passing review as the final report.
10. After the fenced `markdown` formal report, provide the proposed commit message in a fenced `text` block and the Git commands in a fenced shell block labeled `bash` or `sh`, both outside the review report. Generate `git commit --dry-run --only -m <message> -- <exact-task-paths>` and `git commit --only -m <message> -- <exact-task-paths>` for only task-owned files that contain no unrelated hunks; list any task-created untracked file in an exact `git add -- <new-task-paths>` command first because path-limited commit accepts only files already known to Git. If an allowed file contains unrelated user-owned hunks, state that a file-level commit command cannot isolate the task safely and do not provide a misleading commit command. Never execute these Git writes without exact current-task authorization.

If a report or verdict is disproved, fix the highest-leverage rule, schema, validator, prompt, or regression case before correcting the artifact, unless the user explicitly requests a one-off correction.

The final response must lead with the outcome and include changed files and rationale, commands with exit codes, verification status, the final Formal Review report, remaining risks, and the next action only when one is still required. When Git commit authorization is absent, provide the separate proposed commit message and exact task-file commands without staging or committing.
