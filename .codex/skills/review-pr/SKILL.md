---
name: review-pr
description: >-
  Used to review whether an Apache ShardingSphere PR truly fixes the root cause,
  assess side effects and regression risks, and judge code correctness or mergeability from public evidence.
  If not mergeable, produce committer-tone change requests or needs-discussion feedback when the PR direction,
  root-cause model, or problem framing should be reopened. Also use for GitHub-visible PR discussion, review
  comments, challenged findings, and copy-ready committer replies where review correctness, mergeability,
  change-request validity, or root-cause evidence is being judged. Supports full-coverage ledger review for
  high-risk or explicitly anti-drip requests, and self-iterates before final output.
---

# Review PR

## Objective

- Review ShardingSphere PRs and review-related PR discussions with a root-cause-first, evidence-first approach.
- Select the output mode from the user request:
  - `Formal Review Mode`: use when the user asks for a PR review, mergeability decision, or formal GitHub review body. Output exactly one `Review Result`.
  - `PR Discussion Reply Mode`: use when the user asks how to reply to PR comments, review threads, author or maintainer pushback, or challenged findings.
    Output a copy-ready committer reply draft by default, and do not force a formal `Review Result` unless the user asks for one.
- In `Formal Review Mode`, choose the result and feedback mode from the `Verdict Matrix`.

## Review Focus Selection

Classify the review focus before applying CI, mergeability, or GitHub Actions gates. This focus is independent from `Formal Review Mode` versus `PR Discussion Reply Mode`.

- `Code Correctness Review`: default for `$review-pr <PR>` and for requests that ask whether the code, implementation, tests, behavior, or scope is correct,
  including requests that exclude CI or GitHub Actions review.
  Review the latest PR code, tests, behavior, scope, regression risk, and local or static evidence. Do not query, wait for, or block on GitHub Actions, check-runs, workflow runs, or Actions logs.
  In this focus, `Review Result: Mergeable` means no code-level blocker was found in the reviewed scope; it is not a final CI or repository merge-gate decision.
- `Mergeability Review`: use only when the user asks whether the PR can be merged, approved, or is ready to land. Apply code review plus relevant CI and check-run gates.
  Relevant failed CI means the PR is not mergeable; required pending CI means mergeability is incomplete until it finishes.
- `CI Review`: use when the user asks to inspect CI, checks, Actions, logs, or failures. Treat CI evidence as the primary target.

Explicit user scope wins. If the newest user message excludes CI or GitHub Actions, keep the result inside `Code Correctness Review` and state `CI not reviewed by request` in `Review Details`.

## Trigger Scenarios
Use when the user asks to review a PR, decide mergeability or root-cause repair, write committer feedback,
reconsider PR direction, or reply to PR comments, review threads, author pushback, maintainer objections, or challenged findings.

## Mandatory Constraints

1. Verify root-cause repair first; fallback logic, defaults, null checks, try-catch blocks, or swallowed errors cannot substitute for root-cause repair.
2. In `Formal Review Mode`, output exactly one `Review Result` from the `Verdict Matrix`.
3. Enforce the `GitHub and Evidence Access` boundary before using or reporting evidence.
4. Treat every issue as a candidate until it passes the `Blocker Proof Gate`.
   Do not convert reviewer uncertainty, tool failure, inaccessible GitHub data, or missing local verification into a PR blocker.
   Use `Review Incomplete` in `Formal Review Mode`, or a clarification-style reply in `PR Discussion Reply Mode`, when required public facts cannot be checked or attributed.
5. In `Formal Review Mode`, if public evidence confirms a wrong root-cause model, problem framing, expected behavior, ownership boundary, or solution direction,
   use `Review Result: Not Mergeable` with `Feedback Mode: Needs Discussion`.
6. Review only the latest PR code version, and use GitHub PR metadata plus `/pulls/{number}/files` as the authoritative scope boundary.
7. Apply the `Style and Non-Behavioral Churn Authority` before reporting formatting, whitespace, import-only, or formatter-only findings.
8. Before considering `Mergeable`, apply all hard gates and specialized review gates triggered by the selected review focus, including semantic compatibility, counterexamples,
   blast-radius/shared-layer ownership, linked-issue completeness, implicit-state review, high-frequency `computeIfAbsent` review,
   focus-required CI evidence judgment, and local verification freshness.
   In `Code Correctness Review`, do not treat CI or check-run state as a triggered gate unless the user requested CI or mergeability review.
9. Before final output, complete the `Pre-Publication Finding Audit`; do not expose intermediate findings, and output one consolidated review.

## Verdict Matrix

The goal is the correct judgment for the selected review focus, not conservative avoidance or aggressive blocking.

- Use `Mergeable` when the reviewed latest scope, all triggered gates, and public evidence support readiness for the selected review focus.
  In `Code Correctness Review`, this means code-scope readiness only, not final CI or repository merge-gate readiness.
- Use `Not Mergeable` only for confirmed, necessary, in-scope blockers.
  - Use `Feedback Mode: Change Request` when the PR direction is valid, but the patch needs implementation, test, scope, compatibility, or evidence changes.
  - Use `Feedback Mode: Needs Discussion` when public evidence shows the PR direction, root-cause model, problem framing, expected behavior, ownership boundary, protocol or SQL semantics,
    compatibility assumption, or solution direction must be reopened before implementation continues.
- Use `Review Incomplete` only when a required public fact is unavailable, inaccessible, stale, or unattributable and that gap affects the selected review focus.
  Do not include patch-level code requests; state what was verified, what required public fact is missing, and what must be checked next.
- If a concern is real but not required for merge safety, classify it as non-blocking, ask a scoped question, or omit it from GitHub-facing output.

## Blocker Proof Gate

A candidate finding can enter `### Issues` or drive `Review Result: Not Mergeable` only after all checks pass:

1. Direct evidence: cite code, diff, contract, test, CI/log, public reproduction, official documentation, or generated artifact evidence that proves the claim.
2. Full path: for missing-behavior, missing-test, integration, metadata, SPI, or runtime claims, trace the production or test entry path end to end.
   Do not infer a missing branch from one helper method when setup, wrappers, prior calls, generated resources, or CI may already cover it.
3. Counter-evidence: inspect the strongest public evidence that could disprove the finding, especially author/maintainer replies, linked docs, source code,
   and CI when required by the selected review focus.
4. Necessity: prove the requested change is needed for merge safety, not merely cleaner, more precise, easier to read, or a release-note preference.
5. Scope: prove the problem is caused by this PR, exposed by this PR in a way this PR owns, or required by the linked issue scope.

If any check fails, downgrade the candidate to `Review Incomplete`, a non-blocking observation, a clarification question, or remove it.

## Evidence Sufficiency and CI Judgment

- CI success never replaces code review, root-cause review, scope review, or test adequacy review.
- In `Code Correctness Review`, do not query or wait for CI, check-runs, workflow runs, or Actions logs, and do not turn pending, skipped,
  unavailable, inaccessible, or uninspected CI into `Review Incomplete` or `Not Mergeable`.
  State `CI not reviewed by request` in `Review Details`.
- In `Mergeability Review` or `CI Review`, relevant CI failure means the PR cannot be `Mergeable`.
  If the failure is attributable to the PR, use `Not Mergeable`; if attribution is unclear, use `Review Incomplete`.
- Inspect CI, check-runs, or workflow logs when the PR goal, linked issue, author/user statement, generated artifact, native image, E2E,
  test-infra, or a candidate blocker depends on runtime verification and the selected review focus requires CI evidence.
- If required CI or Actions logs are unavailable after the authenticated access ladder in `GitHub and Evidence Access`, classify the effect through the `Verdict Matrix`.
- Do not wait for or query CI when code, docs, or static evidence is sufficient for the current review result.
  State the reason in `Verification` when CI was not reviewed.

## Specialized Proof Mini-Gates

Apply these only when the candidate finding depends on the named area.

- Test coverage claims:
  - Trace the full test entry, fixture setup, helper calls, expected assertions, and earlier invocations before saying a branch is uncovered.
  - Distinguish production-path validation from fixture-injected or mocked-path validation.
  - Consider existing tests, focus-required or already-reviewed CI jobs, native/client smoke coverage, and E2E ownership before requiring a new test in the current PR.
- Generated metadata and native-image claims:
  - Classify the metadata first: reflection, `ServiceLoader`, resource include, proxy, JNI, serialization, tracing-agent noise, or packaging artifact.
  - Check the production access path, generator/source of truth, automatic native-image features, disabled flags, and current-head native/GraalVM CI when the selected review focus requires it.
  - Block only when a concrete reachable path is not otherwise covered and the current PR owns that metadata.
- Docs and release-note necessity:
  - Classify the finding as blocker, change request, non-blocking suggestion, delegated umbrella work, or omitted.
  - Release notes are high-level user-facing change records; do not require fine-grained implementation boundaries there.
  - Require release notes or docs only when users need upgrade, troubleshooting, configuration, compatibility, migration, rollback, or release-awareness guidance.

## Third-Party Tool Behavior Evidence Gate

Apply this gate when a candidate blocker, correction, or copy-ready reply depends on external runtime, shell, package manager, driver, container, CI image,
native utility, or third-party CLI behavior.

- Separate the evidence layers before judging: platform/runtime behavior, package-manager behavior, target tool CLI behavior, shell hook/profile behavior,
  project command flow, and CI/user environment.
- Do not prove a target-tool workflow failure only from adjacent platform documentation.
  Platform docs may support the analysis, but the blocker must also be checked against target-tool public evidence.
- Prefer target-tool official docs, release notes, source code, linked PRs/issues, CI logs, or a public reproduction for the exact command path.
- Align the evidence to the actual version, tag, release, installation source, or CI image used by the PR.
  Do not rely only on the target tool's latest `main` branch unless the PR actually uses that version or the behavior is proven unchanged.
- When the author or maintainer cites a target-tool version, linked PR, implementation detail, or reproduction that contradicts the finding,
  suspend the blocker and inspect that counter-evidence before responding.
- Search the target tool for platform-specific fallbacks around the candidate path, such as command handlers, shell hook detection, environment variables,
  global/session/project scope, registry or PATH updates, subprocess spawning, shell re-entry, and unsupported-shell behavior.
- If target-tool evidence is unavailable and the selected review result or reply correctness depends on it, use `Review Result: Review Incomplete` in `Formal Review Mode`,
  or draft a clarification-style reply in `PR Discussion Reply Mode`; do not emit or preserve a blocker.
- Emit `Not Mergeable`, or a firm copy-ready assertion that asks the author to change the PR, only when target-tool public evidence or a public reproduction
  proves the changed command flow is unreliable in the PR's documented environment.

## Mergeable Hard Gates

Apply these gates before considering `Review Result: Mergeable`.
If a gate is not applicable to the PR, state the reason briefly in the review evidence or details.
Do not turn speculative risks, personal style preferences, or out-of-scope polish into merge blockers.
For `Code Correctness Review`, apply these gates only to code, tests, behavior, scope, compatibility, docs or release necessity, and local or static evidence inside the requested boundary.
CI and check-run completion are not hard gates in this focus.

1. Root cause: the PR repairs the true trigger point or required propagation path, not only the final error point.
   Fallbacks, null checks, defaults, try-catch blocks, or swallowed errors do not substitute for root-cause repair.
2. Linked issue completeness: every claimed fixed/closed/addressed issue requirement maps to code and validation,
   or unresolved scope becomes `Not Mergeable` or `Review Incomplete` under the evidence rules.
3. Scope and ownership: no substantive unrelated changes, scope expansion, target-specific leakage into shared code, or ownership bypass remains.
4. Regression and side effects: no unresolved functional degradation, compatibility, performance, config/API/SPI, dialect, feature-disabled, or adjacent-feature risk remains.
5. Test adequacy: changed behavior has meaningful direct or existing coverage of the root-cause path and important boundaries.
   Do not require coverage-rate proof; high-cost environment proof may be delegated only when lower-level evidence proves the current PR path.
6. Code quality: no concrete maintainability blocker remains, such as unclear responsibility, duplicated logic, dead code,
   over-complex control flow, hidden state, magic values, or hard-to-read temporary design.
7. Architecture: shared modules, public/shared APIs, SPI contracts, metadata, rule owners, dialect owners, session/executor state, and lifecycle state preserve explicit ownership and contracts.
8. Release note and user docs: required entries are present when users need upgrade, troubleshooting, configuration, compatibility, migration, rollback, or release-awareness guidance.
   Low-signal entries are not required for internal/test-only fixes.
9. Breaking change and migration: default behavior, config keys, API/SPI contracts, protocols, metadata storage, SQL semantics,
   and released artifacts have clear compatibility, migration, upgrade, and rollback evidence when touched.
10. Diagnostics: changed errors, logs, or diagnostic output remain accurate, actionable, and safe.
11. Dependency and distribution: manifests, lockfiles, packaging, native-image metadata, LICENSE, NOTICE, and release artifacts are checked
    for security, license, compatibility, packaging, and release impact when touched.
12. Local verification freshness: reviewer-run verification, when used to support the selected review result, must exercise the latest PR head or a clearly identified current-head artifact.
    Do not require Maven `-am` when IDE/MCP current-source runs, current-head installs, CI artifacts,
    or an explicit `-pl` module set already prove freshness for the reviewed path.

## Not Mergeable Feedback Mode

Choose the feedback mode from the `Verdict Matrix` before writing the GitHub-facing review.

- Do not use `Needs Discussion` for ordinary incomplete patches, missing tests, or missing logs when the direction is otherwise correct; request the minimum missing information or changes instead.
- Do not ask for patch-level refinement after selecting `Needs Discussion`; ask maintainers and the author to pause the current implementation direction and resolve the discussion first.
- For label recommendations, suggest existing labels only:
  - Use `type: discussion` when the current direction should be reopened or confirmed.
  - Use `status: need more info` only when missing public evidence blocks root-cause or scope classification.

## Review Boundary

- Review only the latest PR code, tests, behavior, compatibility, regression risk, and scope.
- Derive authoritative scope from GitHub PR metadata and `/pulls/{number}/files`; reproduce locally with triple-dot semantics.
  Record head SHA, base ref/SHA, merge-base, and whether the local file list matches GitHub.

## Style and Non-Behavioral Churn Authority

- Use repository-declared formatting/style gates as authority. For ShardingSphere, Spotless and Checkstyle are authoritative.
- Do not run `git diff --check`, editor whitespace lint, or other generic whitespace diagnostics as routine review verification.
  Use them only when repository workflows explicitly require them, the user asks for whitespace review, the review target is formatting rules, or whitespace has direct semantic impact.
- Do not report Spotless-stable whitespace, including formatter-preserved blank-line indentation, as a blocker.
- Include import-only, whitespace-only, and formatter-only files in `Reviewed Scope` when GitHub lists them.
- Do not report them as blockers or rollback requests unless they hide behavior, fail style gates, touch broad unrelated areas, or violate explicit scope rules.

## GitHub and Evidence Access

- Treat every GitHub-facing review body as community-visible output that may be copied directly to a public Apache PR or issue.
- Base community-visible output only on public PR / issue facts, public commits, public diff, public review threads, public documentation, repository code, and sanitized local verification summaries.
- Do not include non-public downstream project names, private or internal repository names, customer-specific or vendor-specific context, private chats,
  intermediate discussion results, prompt process, internal self-review notes, local-only archive details, private migration background, tokens, auth method details,
  temporary file paths, raw private diagnostics, local absolute paths, private identifiers, or internal reasoning in community-visible output.
- Do not use non-public context as review evidence. If such context helps orient the reviewer privately, convert it into a public-evidence question
  and verify it against public artifacts before output.
- Prefer authenticated GitHub connector/app tools, then authenticated `gh`, then token-backed REST, then anonymous API/HTML.
- For `gh` and REST fallback, prefer the local configured token sources in this order: `gh auth token`, `GH_TOKEN`, then `GITHUB_TOKEN`.
  Never print token values, write them to the skill, ledger, temporary logs, review output, shell history, debug traces, or command summaries.
- Fetch all pages for PR files, commits, comments, reviews, and required check data.
- Do not treat anonymous `404` or secondary-endpoint `403` as unavailable evidence until authenticated access has been tried or shown unavailable.
- Classify access by endpoint; inaccessible checks/logs block the selected review result only when required under `Evidence Sufficiency and CI Judgment`.
- When GitHub Actions logs are required evidence, do not stop after `gh run view --log` or anonymous API/HTML failure.
  Try authenticated `gh` first, then token-backed REST for workflow or job logs, and record authenticated access as unavailable only after the configured token sources are missing,
  expired, unauthorized, or still cannot access the endpoint.
- Keep Actions log retrieval read-only. Do not rerun workflows, write comments, alter PR state, or request new token permissions without explicit user confirmation.
- Download large logs to a temporary file and report only status, exit code, log path for internal use, and a small sanitized summary. Do not expose redirect URLs or raw long logs.
- Official references for the access behavior:
  - GitHub CLI environment variables: https://cli.github.com/manual/gh_help_environment
  - GitHub CLI run log limitations: https://cli.github.com/manual/gh_run_view
  - GitHub REST workflow run logs: https://docs.github.com/en/rest/actions/workflow-runs?apiVersion=2022-11-28#download-workflow-run-logs

## Execution Boundary

- Review output only; do not modify PR code.
- Write internal review state only to the system temporary directory when `Full Coverage Ledger Mode` is active.
- Do not place ledgers, scratch files, or validator artifacts in the repository unless the user explicitly asks for a persisted artifact.
- Clean temporary ledger directories before ending a normally completed review. If cleanup fails, do not expose the temp path in GitHub-facing text.
- If the user asks for console-only output, return the copy-ready review or reply in Codex chat only.

## Evidence Source Strategy

- Evidence priority: PR facts; same-repo issues/code/tests; ShardingSphere docs and conventions; external official specs only when needed.
- For SQL parser reviews, read `references/sql-parser-review.md` when SQL grammar, visitors, syntax docs, dialect behavior, or parser baselines are touched.
- Do not rely on unverifiable blogs, forum posts, or AI-reposted content.

## Local Verification Freshness Strategy

- Choose commands that prove the latest PR head. Prefer focused IDE/MCP runs or explicit Maven `-pl` module sets when they cover changed and consuming modules.
- Use Maven `-am` only when explicit module selection cannot prove dependency freshness, an upstream artifact is missing, or reactor behavior is itself the evidence.
- For multi-module checks, run lower-level modules before adapters/runtimes, stop on the first relevant failure, and record the freshness reason in `Verification`.

## Inventory Script Usage

- When local git refs are available, use `scripts/build_review_inventory.py` as a bounded inventory draft before deep review.
- The script is local-only and heuristic; confirm every candidate through code, tests, docs, or authoritative specs before reporting it.
- If GitHub `/pulls/{number}/files` is available, pass a one-path-per-line file list with `--github-files`.
- Treat dirty-worktree warnings as contamination warnings. Review PR refs, not unrelated local modifications.
- Keep script output out of GitHub-facing review bodies unless converted into public, verified, repo-relative evidence.

## Full Coverage Ledger Mode

Use this mode when the user asks to find all issues, prevent drip-feed review, review from beginning to end, avoid piecemeal findings,
or otherwise requests full coverage. Also use it by default for high-risk reviews that touch parser grammar, SQL visitors, proxy protocol,
authentication, transaction/session/blob/handle lifecycle, shared APIs, cache or registry state, dependency or distribution files,
or more than 20 changed files.

When this mode is active:

1. Initialize a ledger in the system temporary directory with `scripts/review_ledger.py init` after confirming the latest PR head and GitHub file list.
2. Clean stale ledgers for the same repository and PR before creating the current ledger.
3. Track every authoritative changed file with one final state: `reviewed`, `churn-only`, `test-only-reviewed`, `not-applicable`, or `blocked`.
   Do not leave `pending` files before final output.
4. Record candidate findings in the ledger and classify each as `confirmed`, `withdrawn`, `review-incomplete-gap`, `non-blocking`, or `out-of-scope`
   before final output.
   A `confirmed` finding must record direct evidence, counter-evidence checked, necessity, scope proof, and `Blocker Proof Gate` result.
5. Record each adversarial pass with its focus and new independent finding count. The final pass must have `new_findings` equal to `0`.
6. Run `scripts/review_ledger.py validate` before drafting the final review or discussion reply.
7. If ledger validation fails because coverage or evidence is incomplete, continue reviewing or output `Review Incomplete` in `Formal Review Mode`;
   do not emit a complete review with hidden gaps.
8. Do not use the fast-triage shortcut, early blocker exit, or "confirmed high-risk blockers only" behavior while this mode is active.
9. In Codex chat, report only concise progress and the final review or reply.
   Do not paste the ledger contents unless the user explicitly asks for the internal audit trail.
10. Clean the temporary ledger directory with `scripts/review_ledger.py cleanup` after the final response has been prepared and before ending the task.

The ledger is an internal audit aid, not public evidence. GitHub-facing text must cite public files, public docs, public CI/logs,
or sanitized verification summaries, never the temporary ledger path.

## Review Efficiency Rules

- Prioritize `Summary`, blocking issues, and minimum next actions.
- If the PR is too large, suggest splitting first.
- These rules do not override `Full Coverage Ledger Mode`; ledger validation or `Review Incomplete` still controls final output.

## Quick Triage

Before deep review, answer the smallest set of questions needed to choose review depth and feedback mode:

- Problem model: is the problem, expected behavior, linked-issue scope, and suspected root-cause path clear from public evidence?
- Patch direction: do the changes directly repair that root-cause path, or does the PR need `Needs Discussion` before implementation continues?
- Validation: is there behavior validation for the root cause, important boundaries, counterexamples, and feature-disabled or adjacent paths?
- Scope: are there substantive unrelated changes, broad cleanup, or a change set too large to review safely?
- Semantics and ownership: did the PR affect SQL/parser semantics, name resolution, routing or fallback precedence, shared modules, public APIs, or implicit state?
- User-facing impact: are release notes, user docs, migration, diagnostics, dependency, distribution, or compatibility evidence needed for this PR?

Triage policy:

- Information complete: proceed with full review.
- Confirmed blocker: set `Review Result: Not Mergeable` and request the minimum required change or discussion.
- Required public facts unavailable, inaccessible, stale, or unattributable: set `Review Result: Review Incomplete` and request only the facts needed to complete the review.
- Wrong problem model, root-cause model, or direction: set `Review Result: Not Mergeable`, use `Feedback Mode: Needs Discussion`, and recommend `type: discussion`.
- Any substantive off-topic/unrelated changes or substantive scope expansion: set `Review Result: Not Mergeable` and require rollback or scope narrowing.
  Ignore non-behavioral import-only, whitespace-only, and formatter-only churn for the selected review result unless it meets the `Style and Non-Behavioral Churn Authority`.
- Change set too large: request split first, and provide only blocker-level feedback for current version.
- If `Full Coverage Ledger Mode` is active, triage decisions select review depth and ledger setup only. They do not authorize early final output.

## Minimum Required Information

- Request only facts required by the unresolved gate, and map every requested item to that gate.
- Do not ask the author for evidence the reviewer can obtain through `GitHub and Evidence Access`, repository code, public docs, target-tool source, or official specs.
- Common requests: latest changed-file scope, runtime topology, database type/version, minimal reproducible input, key logs,
  targeted test evidence, docs/release impact, or SQL parser official documentation.

## Review Workflow

CI/check-run review is not a substitute for code review. Query and report CI only when the selected review focus requires it under `Evidence Sufficiency and CI Judgment`.

1. Select the review focus. In `Code Correctness Review`, fetch PR metadata, files, comments, and reviews needed for code scope, but do not fetch check-runs, workflow runs, or Actions logs.
2. Define target and boundary: latest PR head, base/merge-base, GitHub file list, linked issue scope, modules, and runtime topology.
3. Model the root cause: reconstruct "trigger condition -> failing path -> observed result -> expected behavior" from public issue, PR, code, and docs.
4. Map the fix: connect each required issue behavior to the changed code and validation point; choose `Needs Discussion` only when the direction itself is contradicted.
5. Build the risk inventory: scope, shared ownership, implicit state, compatibility, performance, diagnostics, docs, release notes, dependencies, and distribution.
   For parser, binder, routing, and default-schema changes, compare official dialect semantics and use `references/sql-parser-review.md` when SQL parser code changes.
6. Verify validation: trace tests through real entry paths where applicable, map tests to fix points,
   and apply the `Specialized Proof Mini-Gates` for test, metadata, native, and docs claims.
7. Screen unrelated change: require rollback or scope narrowing only for substantive unrelated code, config, behavior, or broad cleanup.
   Ignore non-behavioral churn unless it meets the `Style and Non-Behavioral Churn Authority`.
8. Materialize the anti-drip inventory when `Full Coverage Ledger Mode` is active;
   otherwise keep an internal inventory with the same evidence categories.
9. Apply the `Blocker Proof Gate` to every candidate finding before it can become a public blocker.
10. Re-review latest deltas when new commits arrive; never conclude mergeability only because earlier comments were fixed.
11. Run the `Pre-Publication Finding Audit`, then output exactly one formal result or one copy-ready PR discussion reply.

## Pre-Publication Finding Audit

Before producing the final review, build and freeze an internal review inventory for the latest PR head.
Do not expose intermediate findings, draft issue lists, or candidate blockers unless the user explicitly asks for status or early high-risk blockers.
When `Full Coverage Ledger Mode` is active, the inventory must be represented by a system-temporary ledger and pass `scripts/review_ledger.py validate`
before final output.

The inventory must cover:

- Authoritative scope: latest head SHA, base ref/SHA, merge-base, local file list, and GitHub `/pulls/{number}/files` match status when available.
- Changed file categories, entry points, execution paths, public/shared APIs, stateful registries or lifecycle handles, tests, docs,
  release notes, generated resources, dependencies, and distribution impact.
- Boundary cases relevant to the change: empty, null, invalid, stale, repeated, disabled, fallback, cleanup, release/free, and error paths.
- Latest-commit delta risks when previous public feedback or review rounds exist.

For each candidate finding, record internally:

- Evidence type: code/diff, test/CI/log, linked issue, docs/spec, generated artifact, or local verification.
- Evidence path, line, command, CI run, or public anchor.
- Whether the finding is caused by this PR, pre-existing on base, exposed by this PR, newly introduced by latest commits, or newly discovered but present earlier.
- Minimum independent fix boundary and duplicate relationship.
- Counter-evidence checked, necessity for merge safety, scope proof, and triggered specialized proof gate result.
- Classification: confirmed blocker, review-incomplete gap, non-blocking observation, or out-of-scope note.

Before any candidate enters `### Issues`, verify it passed the `Blocker Proof Gate` and every triggered specialized gate.
Reviewer uncertainty, skipped local verification, unavailable tools, inaccessible GitHub data, or uninspected counter-evidence must not become blockers.

Run an adversarial pass on the latest head that looks for missed root-cause gaps, side effects, feature-disabled paths,
adjacent-feature regressions, ownership issues, release/doc impacts, and required verification gaps.
If the pass finds any new actionable finding with an independent fix boundary, add it to the inventory, deduplicate and classify it, update the review result if needed, and repeat the pass.
Stop only after one full adversarial pass finds no new actionable finding.
If the inventory cannot be completed because public evidence required by the selected review focus is unavailable or unattributable, output `Review Result: Review Incomplete`.
Produce one consolidated review with exactly one `Review Result`.

## Root-Cause and Issue Gates

- Identify the true trigger point, propagation path, and expected behavior; do not accept null checks, defaults, fallback, try-catch, or swallowed errors as root-cause repair.
- Check adjacent paths that share the same root cause, including disabled/feature-flag-off paths and precedence or shadowing cases.
- For SQL parser changes, verify target and related dialect semantics with official docs plus `references/sql-parser-review.md`.
- When a PR claims to fix or close issues, map every stated symptom, topology, input, expectation, comment clarification, and maintainer constraint to code and validation.
- If the PR narrows issue scope, verify the limitation is explicit, technically justified, and accepted by public issue/PR context.
- If public evidence proves the root-cause chain or claimed issue scope is not fixed, use `Not Mergeable`.
- If required public issue facts are unavailable or ambiguous, use `Review Incomplete`.
- If public evidence contradicts the problem model or expected behavior, use `Needs Discussion` instead of patch-level requests.

## Shared Scope & Implicit State Gate

Apply this gate when shared modules, session/executor/connector state, cache contexts, SPI contracts, or public constructors/methods are touched.

- Separate generic mechanisms from target-specific leakage; shared code must not gain dialect names, protocol ids, type-name string checks,
  or target lifecycle comments.
- Check new nullable construction, `this(null)`, partial initialization, hidden modes, overloaded booleans, magic values,
  empty sentinels, no-op implementations, and temporal side effects.
- Prefer explicit state objects, enums, keys, tokens, target-module-owned lifecycle APIs, or documented absence-return contracts over implicit value encoding.
- Validate generic shared behavior separately from target activation when shared behavior changes.
- If target-specific semantics leak into shared code, or implicit state becomes a mode switch in shared APIs, use `Not Mergeable`.

## Risk Checklist (Must Cover)

- Root-cause and functional risk: true trigger repair, linked issue completeness, old-scenario regression, boundary inputs, feature-disabled paths, and adjacent features.
- Ownership and design risk: broken layering, target-specific shared-code leakage, bypassed SPI/metadata cache, implicit state,
  public API/SPI contract drift, and unnecessary complexity.
- Compatibility and performance risk: behavior, config, SQL dialect/version, name resolution, fallback precedence, hot-path allocations,
  blocking I/O, and high-frequency `computeIfAbsent`.
- User-facing and operational risk: diagnostics, docs, release notes when necessary, migration, rollback, config migration, and staged rollout impact.
- Supply-chain and distribution risk: dependency, license, packaging, native-image metadata, LICENSE, NOTICE, generated artifacts, and release artifacts.

## Boundary Validation Review Guidance

- Identify the authoritative input boundary before requiring validation changes.
  Examples: YAML swappers/validators, CLI parsers, REST/API request binders, SPI loaders, protocol decoders, SQL parsers, or config-center loaders.
- If the authoritative boundary already rejects invalid input and all production entry paths pass through it,
  do not require duplicate validation in downstream value holders, runtime contexts, or internal DTOs by default.
- Require downstream validation only when there is evidence of another production path that bypasses the boundary,
  public/shared API exposure, untrusted deserialization,
  asynchronous/shared ownership risk, or a documented invariant owned by the downstream type.
- Prefer tests that prove boundary-to-runtime propagation and adjacent valid values over adding defensive checks at every layer.
- If boundary ownership is unclear, ask for production entry-path evidence and treat duplicate validation as a design question, not an automatic blocker.

## Review Details Statement (Required in Every Review)

Each review must include a `Review Details` section with:

- `Review Focus`: `Code Correctness Review`, `Mergeability Review`, or `CI Review`.
  In `Code Correctness Review`, include the exact statement `CI not reviewed by request`; this is not an incomplete-evidence gap.
- `Reviewed Scope`: files/modules reviewed this round, latest PR head SHA, local merge-base SHA when local git is used,
  and whether the local file list matched GitHub `/pulls/{number}/files`.
- `Not Reviewed Scope`: unreviewed or only superficially reviewed areas.
- `Verification`: reviewer-run commands and exit codes, or a short reason why local verification was not run.
  Also state any relevant GitHub endpoint that was inaccessible and whether that gap affects the selected review result.
- `Release Note / User Docs`: required and verified, delegated to an umbrella PR with reason, missing, not required with reason, or not reviewed.
- For SQL parser reviews, `Reviewed Scope` must name the target dialect, related trunk / branch dialects checked,
  and the documentation pages / repo doc paths used to validate syntax behavior.

If a required domain expert review is still needed, include `Expert Review Needed` in `Summary`; omit it when no expert review is required.
Treat expert review as blocking only when the current PR's merge safety cannot be decided from available code, tests, docs, and authoritative evidence.
Do not mark expert review as blocking merely because the PR is adjacent to a specialized domain,
or because broader umbrella/integration validation remains outside this PR's scope.
When the remaining specialized review is advisory, delegated to an umbrella PR, or only needed before a larger feature release,
state that boundary in `Review Details` instead of blocking mergeability.
Require blocking expert review when merge safety for the current PR depends on specialized domains such as security,
parser grammar or dialect semantics, Proxy protocol/authentication/packet behavior,
high-concurrency or high-frequency performance paths, transaction/pipeline/data consistency, shared metadata/binder/routing/default-schema behavior, dependency/license changes,
or any area the reviewer cannot confidently validate from available evidence.

## Multi-Round Change Request Comparison Rules

Apply this section only when previous feedback exists in GitHub-visible PR review comments, review threads, or change requests.
Do not output `Multi-Round Comparison` for local chat-only iterations, private reviewer analysis, or commit-history-only changes.

When GitHub-visible previous-round feedback exists:

1. Mark previous issues as fixed, partially fixed, not fixed, newly introduced, out of scope, or newly discovered.
2. Keep only unresolved, partially fixed, and currently confirmed blockers in the GitHub-facing review.
3. Cite current diff evidence for every retained request and specify the minimum remaining work for partial fixes.
4. If new commits arrived, review the latest version; use prior heads only to classify origin.
5. If the latest result becomes `Needs Discussion`, stop treating old patch-level requests as the main path.
6. Do not include private reviewer accountability, local chat context, raw inventory, or internal origin notes.

## Challenged Finding Correction Rules

Apply this section whenever an author, maintainer, or user challenges, contradicts, or disproves a prior finding or review result.

1. Treat the prior finding as a hypothesis to disprove, not as a conclusion to defend.
2. Rebuild the evidence chain from public facts and identify every assumption required for the finding to remain true.
3. Inspect challenger-provided public evidence first, including linked source PRs, version-specific behavior, reproduction notes, CI logs, and implementation details.
4. Convert private or off-platform context into public-evidence questions before GitHub-facing output.
5. If any required assumption is disproved or unsupported, withdraw the blocker or change it to `Review Incomplete`.
   Do not narrow the wording while preserving the same unsupported request.
6. If the finding remains valid, restate direct evidence and address counter-evidence explicitly.
7. Do not publish another `Not Mergeable` result until the `Pre-Publication Finding Audit` and triggered gates have been rerun.

## PR Discussion Reply Mode

Use this mode when the user asks for a committer reply to a PR comment, review thread, author or maintainer objection, or challenged review finding.

- Apply the same evidence gates used by formal reviews before making any firm public assertion.
- Default to a copy-ready GitHub reply draft, not a formal `Review Result`.
- Explain reasoning in the user's language, but draft GitHub-facing replies in English unless the user requests another language.
- Keep private chats, private reproduction details, prompt process, and local-only analysis out of the copy-ready reply.
- If evidence is closed, state whether to retain, withdraw, or revise; if incomplete or conflicting, draft a clarification-style reply.
- Do not ask the author for evidence the reviewer can obtain under `Minimum Required Information`.
- Protect committer credibility: do not provide a strong public claim, blame, or merge-blocking request unless the evidence chain is closed.

## Output Structure

GitHub-facing reviews must be GitHub Markdown, use the user's language unless requested otherwise,
and contain exactly one bold `Review Result: ...` line under `### Summary`.
Use stable English labels, repo-relative file references with line numbers, public anchors, and sanitized verification summaries.
Do not wrap posted GitHub text in fences, blockquotes, XML/HTML, or transcripts.
Do not include internal drafts, self-review notes, private context, local absolute paths, temp paths, tokens, or raw long logs.
In Codex chat, Formal Review Mode final output MUST be exactly one fenced `markdown` block containing the entire GitHub-facing review body, with no prose before or after the block.
Before final output, verify the first non-empty line is ```markdown and the last non-empty line is ```.

Use these required structures:

- `Mergeable`: `### Summary` with `Review Result` and `Reason`; `### Evidence`; `### Review Details`.
  In `Code Correctness Review`, the `Reason` must say the result is code-scope only and CI was not reviewed by request.
- `Not Mergeable`: `### Summary` with `Review Result`, exactly one `Feedback Mode`, and `Reason`; `### Issues`; `### Review Details`.
- `Review Incomplete`: `### Summary` with `Review Result` and `Reason`; `### Incomplete Reason`; `### Verified Facts`;
  `### Required Evidence`; `### Review Details`.
- `Correction`: prepend `### Correction` with `Previous Finding`, `Current Status` (`Retained`, `Withdrawn`, or `Changed to Review Incomplete`), and `Reason`;
  then output the applicable current result structure with exactly one bold `Review Result` line under `### Summary`.

For `Not Mergeable`, choose the feedback mode from the `Verdict Matrix`.
Do not include patch-level `Required Change` bullets after choosing `Needs Discussion`.
Use `P0`, `P1`, or `P2` issue severity, and include `Problem`, `Impact`, and either `Required Change` or `Discussion Needed`.
Use `status: need more info` instead of `type: discussion` only when missing public evidence blocks root-cause or scope classification.
Optional `Not Mergeable` sections are `Positive Feedback`, `Unrelated Changes`, `Next Steps`, and `Multi-Round Comparison`; omit placeholder headings.
`Review Incomplete` must follow the `Verdict Matrix`: no patch-level code suggestions, and use `Not Mergeable` once evidence supports a concrete required change.

## Feedback Tone Guidelines
- Use "suggest / please / need" rather than accusatory commands.
- Facts first, judgment second; avoid emotional wording.
- For `Change Request`, acknowledge aligned direction when true and ask for the minimum required change.
- For `Needs Discussion`, ask maintainers and the author to pause implementation and reopen discussion.
  Avoid wording like "wrong solution" or requests to keep refining the current patch.

## Prohibited Items

- Do not output `Mergeable` when evidence required by the selected review focus is insufficient, triggered gates for that focus remain unresolved,
  or latest-head freshness for the reviewed code is unclear.
  In `Mergeability Review` or `CI Review`, relevant CI failure or required pending CI prevents `Mergeable`; in `Code Correctness Review`, unreviewed CI does not.
- Do not output `Not Mergeable` unless the candidate passed the `Blocker Proof Gate`.
- Do not turn uncertainty, inaccessible facts, unavailable tools, skipped verification, or uninspected counter-evidence into a blocker.
- Do not use fallback logic, fixture-injected tests, mocked paths, or CI success as substitutes for root-cause and production-path evidence.
- Do not reuse old conclusions after new commits or previous-round fixes; always run a fresh latest-head semantic and regression scan.
- Do not ignore substantive unrelated changes, shared-code blast radius, required docs/migration/diagnostics, or CI/check-run evidence required by the selected review focus.
- Do not include private identifiers, private chats, prompt process, local absolute paths, temp paths, credentials, or emojis in GitHub-facing output.
- Do not output patch-level `Required Change` requests after selecting `Feedback Mode: Needs Discussion`.
- Do not require low-value release-note or doc details that fail the necessity check.
- Do not approve high-frequency Proxy/JDBC SQL paths that directly call `ConcurrentHashMap#computeIfAbsent` without a preceding `get` miss check.
