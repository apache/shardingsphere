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

Apply this file only when the user authorizes changes to `AGENTS.md`, a canonical policy source listed in `.codex/harness/agents/policy-sources.toml`, or the harness that validates those sources. `cases.toml` is the source of the harness catalog: each case records its group, purpose, enforcement phase, and whether the ordinary completion loop rechecks it. Run `python3 .codex/harness/agents/run.py --list-cases` to render the table.

1. Treat every explicit requirement, prohibition, exception, authorization boundary, and verification step as an independent policy capability.
2. Before editing, build an old-to-new capability ledger and classify every capability as exactly preserved, changed with the user's explicit authorization in the current task, or restored before handoff.
3. Reconcile every deleted or weakened rule explicitly. A replacement is equivalent only when it preserves the same trigger, required or forbidden action, scope, exceptions, and verification obligation. A positive general rule does not replace a specific negative prohibition. Ordinary Codex competence, implication, nearby prose, a Skill, or a canary is not evidence that any explicit rule is preserved. Partial or implicit coverage is a regression.
4. Never remove, weaken, merge away, or broaden an exception to a capability unless the user explicitly authorizes that exact policy change. When equivalence is uncertain, keep the existing rule.
5. Before editing, capture a V0 policy baseline and a source snapshot for every manifest entry; then change one coherent instruction group. Keep the original V0 and source snapshot across every candidate in the active task.
6. Count a canary as passing only when its decision matches, every required action and reason is present, and every action outside its complete allowed-action set is absent. A partial forbidden-action list is insufficient.
7. Run all policy canaries and reject any critical regression. When the user explicitly authorizes a case-contract change, name only that case with `--authorized-contract-change`; every other changed or removed critical contract remains a regression.
8. Compare pass rate, duration, input tokens, and uncached input tokens with V0 for every candidate. These policy-candidate comparisons are unconditional; application-code performance risk classification does not waive them. Accept only candidates that preserve correctness, and count efficiency improvements only after quality gates pass.
9. Validate the manifest, exact-path reference graph, root byte limit, source hashes, staged policy bundle, and source-read traces. A source-read trace passes only when it proves that each required exact file was locally available and read through EOF from both root and applicable nested working directories; it does not prove semantic interpretation by a separate Codex evaluation. Semantic canaries evaluate the root `AGENTS.md` decision surface unless a separately authorized harness mode explicitly supplies other non-sensitive policy sources. Report these proof boundaries directly; automatic Skill catalog discovery is diagnostic and does not replace the exact-path route.
10. Bind every legacy critical case whose controlling rule moves out of `AGENTS.md` to its routing profile, manifest source, and complete normative sentences with `policy_binding_profile` and `policy_assertions` in `cases.toml`. The normalized binding set and digest prove that each routed source still contains the bound rule, but they do not prove that a model interpreted it. Reject a missing, shortened, moved, or changed established binding even when the source and binding are edited together.
11. After the first policy-bundle candidate passes, run `--mode validate` without a policy-bundle baseline and retain its summary as the explicit local inventory-and-binding bootstrap. Pass that summary with `--baseline` to every later deterministic candidate; the original semantic V0 remains the independent behavior baseline and is not replaced by this bootstrap.
12. Change an established binding only for an explicitly authorized equivalent rule migration, name only that case with `--authorized-policy-binding-change`, and preserve the old-to-new sentence audit as task evidence. Never use binding authorization to weaken a rule, bypass a semantic canary, or change an unrelated case.
13. After the bootstrap, treat every source addition, removal, relocation, metadata change, or content hash change as a policy-source change. Accept it only when the user explicitly authorized that exact source and the run names its source ID with `--authorized-policy-source-change`; this source authorization does not replace the capability ledger, binding migration audit, or semantic canaries.
14. If the same failure appears twice, add a focused case instead of generic prose. Stop after five candidates or when no measurable improvement remains.
15. After the canaries pass, complete the applicable simplification, internal candidate review, finding-fix, and re-review steps in `.codex/context/change-completion.md` before handoff.

## Canonical-Source Repair

If a canonical policy source required by `AGENTS.md` is missing or unreadable, ordinary repository writes remain read-only. Only a current user request that explicitly authorizes repair of the exact policy or harness paths may use this exception. Repair from the active task's preserved source snapshot or read-only `git show` evidence under the last readable root policy; never use a Git state-changing restore, never apply this exception to production or test code, and never broaden the frozen repair boundary. Re-run deterministic manifest and reference validation before relying on the repaired source.
