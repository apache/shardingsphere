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

# Repository Policy Maintenance

Apply this file only to an authorized change to `AGENTS.md`, a source listed in `policy-sources.toml`, or the harness that validates those sources. `cases.toml` owns the case catalog; use `run.py --list-cases` when its rendered inventory is needed.

1. Treat each explicit requirement, prohibition, exception, authorization boundary, and verification step as an independent capability. Before editing, build an old-to-new ledger and classify each capability as preserved, authorized to change, or restored before handoff.
2. A replacement is equivalent only when it preserves the trigger, required and forbidden actions, scope, exceptions, and verification. Do not substitute implication, nearby prose, ordinary Codex behavior, a Skill, or a canary for an explicit rule, and retain the original when equivalence is uncertain.
3. Inspect only the affected manifest entries and profiles before editing; `--mode validate` remains responsible for validating the complete manifest and reference graph. Capture the current source snapshot and keep one original baseline across every candidate in the active task.
4. Classify the change as deterministic-only, semantic policy, semantic case or binding, shared semantic evaluation, or transport-only. For any category containing semantic behavior, read [semantic-verification.md](semantic-verification.md) through EOF before editing and follow its baseline, canary, promotion, stability, and bootstrap rules.
5. For deterministic-only changes, run `--mode validate` and applicable source-read traces. For transport-only changes, also prove batch coverage, limits, concurrency, failure propagation, no-retry behavior, and aggregation with deterministic tests before the smallest live `--transport-check`.
6. Validate root size, source hashes, staged bundle, exact-path references, and applicable traces. A trace proves local availability and EOF reading, not semantic interpretation.
7. After required checks pass, complete `.codex/context/change-completion.md`. Validate changed `AGENTS.md` behavior in a new task after merge or installation because the active task may retain its original instructions.

## Canonical-Source Repair

If a canonical policy source required by `AGENTS.md` is missing or unreadable, ordinary repository writes remain read-only. Only a current user request that explicitly authorizes repair of the exact policy or harness paths may use this exception. Repair from the active task's preserved source snapshot or read-only `git show` evidence under the last readable root policy; never use a Git state-changing restore, never apply this exception to production or test code, and never broaden the frozen repair boundary. Re-run deterministic manifest and reference validation before relying on the repaired source.
