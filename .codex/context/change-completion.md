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

1. Perform the post-write task-delta audit required by `AGENTS.md` with the read-only diff, surrounding context, and acceptance checklist. Confirm every task-introduced hunk is necessary and allowlisted, prohibited paths have no task delta, architecture changes were authorized, tests protect owned behavior, and unattributed work remains untouched.
2. Apply every triggered coding-standard and simplification rule to the effective task delta. Treat scripts, searches, formatters, compilation, tests, benchmarks, and review tools as evidence rather than proof, and fix every safe in-scope violation.
3. Run the applicable verification and non-regression checks. An invalidated, unmatched, missing, or inconclusive required result keeps the task incomplete.
4. Use bounded self-review only when inspection proves that the task is a standalone local change confined to one owning module, has no public or externally visible contract impact, and has no architecture, SPI, signature, visibility, module-dependency, shared-owner, cross-module, protocol, dialect, concurrency, security, performance, deletion, last-consumer, or unresolved-evidence risk. Review root-cause and fix mapping, affected behavior, side effects and regressions, contracts, tests, and adversarial cases against the original task baseline and task-owned delta.
5. Invoke `$review-pr` in Formal Review Mode when the user requests a PR, mergeability, or formal review; the task targets or backs a PR; or any risk excluded from bounded self-review is present. For a PR-backed candidate, apply its public-head and remote-evidence checks; for a standalone high-risk candidate, review only the original task baseline and task-owned delta.
6. Fix every safe in-scope required finding, repeat the task-delta audit, rerun invalidated checks, and repeat the applicable review. Stop at the existing gate for a scope expansion, unresolved architecture choice, high-risk action, `Review Incomplete`, or `Needs Discussion` result.
7. Hand off a bounded-review candidate only after the review finds no required issue. Hand off a Formal Review candidate only after the latest complete report returns `Review Result: Mergeable`; include that report when the user requested review output or when it materially explains a high-risk decision.
8. If the task wrote code, provide a commit message before handoff without waiting for a user request. Provide manual Git commands only when the user requests them. Propose `git commit --dry-run --only` and `git commit --only` for only task-owned files without unrelated hunks, list a required `git add -- <new-task-paths>` first for untracked task files, and never execute a Git write without exact current-task authorization.

If a report or verdict is disproved, fix the highest-leverage rule, schema, validator, prompt, or regression case before correcting the artifact, unless the user explicitly requests a one-off correction.

The final response must lead with the outcome and include changed files and rationale, verification status, remaining risks, and the next action only when one is still required. Include command details, review reports, and commit artifacts only when required by the applicable review path or requested by the user.
