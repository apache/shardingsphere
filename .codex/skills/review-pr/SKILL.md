---
name: review-pr
description: >-
  Used to review whether an Apache ShardingSphere PR truly fixes the root cause,
  assess side effects and regression risks, and determine whether it can be safely merged.
  If not mergeable, produce committer-tone change requests, or needs-discussion feedback when
  the PR direction, root-cause model, or problem framing should be reopened before implementation continues.
  Supports targeted comparison across GitHub-visible review rounds when prior PR comments or review threads exist.
  Before final output, internally self-iterate the review until no new actionable findings are discovered.
---

# Review PR

## Objective

- Make merge decisions for ShardingSphere PRs with a "root-cause-first, evidence-first" approach.
- Output a single merge decision:
  - `Merge Decision`: `Mergeable` / `Not Mergeable`
- For `Not Mergeable`, choose exactly one feedback mode:
  - `Change Request`: the direction is valid but the current patch needs implementation, test, scope, or evidence changes.
  - `Needs Discussion`: public evidence shows the PR direction, root-cause model, or problem framing must be reopened before implementation continues.

## Trigger Scenarios

- The user asks you to review a PR.
- The user asks whether a PR "can be merged" or "fixes the root cause."
- The user asks you to generate committer-tone change request comments.
- The user asks whether a PR direction should be reconsidered or discussed before more patch work.

## Mandatory Constraints

Before applying the numbered review gates, enforce the public evidence boundary:

- Treat every GitHub-facing review body as community-visible output that may be copied directly to a public Apache PR or issue.
- Base community-visible output only on public PR / issue facts, public commits, public diff, public review threads, public documentation, repository code, and sanitized local verification summaries.
- Do not include non-public downstream project names, private or internal repository names, customer-specific or vendor-specific context, private chats,
  intermediate discussion results, prompt process, internal self-review notes, local-only archive details, or private migration background in community-visible output.
- Do not use non-public context as review evidence. If such context helps orient the reviewer privately, convert it into a public-evidence question
  and verify it against public artifacts before output.

1. Verify root-cause repair first; fallback logic, defaults, null checks, try-catch blocks, or swallowed errors cannot substitute for root-cause repair.
2. Output exactly one `Merge Decision`, and choose one `Feedback Mode` for every `Not Mergeable` result.
3. If evidence is insufficient, risk is unclear, validation is incomplete, or any required hard gate fails, set `Merge Decision: Not Mergeable`.
4. If public evidence shows a wrong root-cause model, problem framing, expected behavior, ownership boundary, or solution direction, use `Feedback Mode: Needs Discussion`.
5. Review only the latest PR code version, and use GitHub PR metadata plus `/pulls/{number}/files` as the authoritative scope boundary.
6. Do not use GitHub Actions, CI status, or check-run completion for the merge decision unless explicitly requested.
7. Use repository-declared formatting/style gates as the formatting authority.
8. Treat substantive unrelated changes or substantive scope expansion as merge blockers; ignore non-behavioral import-only, whitespace-only, formatter-only, or IDE cleanup churn unless it hides behavior,
   fails declared gates, touches broad unrelated areas, or violates explicit scope rules.
9. Before considering `Mergeable`, apply all triggered hard gates and specialized review gates, including semantic compatibility, counterexamples, blast-radius/shared-layer ownership,
   SQL parser official-doc and dialect-family checks, linked-issue completeness, implicit-state review, high-frequency `computeIfAbsent` review, and local verification freshness.
10. Before any final output, complete the `Anti-Drip Review Gate` and `Self-Iteration Gate`; do not expose intermediate findings, and output one consolidated review.

## Mergeable Hard Gates

Apply these gates before considering `Merge Decision: Mergeable`.
If a gate is not applicable to the PR, state the reason briefly in the review evidence or details.
Do not turn speculative risks, personal style preferences, or out-of-scope polish into merge blockers.

1. Root Cause Gate:
   - The PR must repair the true trigger point or required propagation path, not only the final error point.
   - Fallbacks, defaults, null checks, try-catch blocks, or swallowed errors cannot substitute for root-cause repair.
   - If the root-cause chain cannot be proven fixed, set `Merge Decision: Not Mergeable`.
2. Linked Issue Completeness Gate:
   - Keep and apply the existing linked-issue completeness rules whenever the PR claims to fix, close, resolve, or address an issue.
3. Scope & Ownership Gate:
   - Substantive unrelated changes, substantive scope expansion, and broad cleanup outside the PR goal block mergeability.
   - For pluggable features, dialects, rules, registry centers, or protocol modules, the fix should stay in the owning module by default.
   - Shared modules may be changed only for generic contracts or hooks that make sense for all affected owners.
   - Target-specific names, lifecycle concepts, protocol state, database strings, or comments in shared code are blockers unless proven to be an intentional generic contract.
4. Regression & Side Effect Gate:
   - The PR must not leave unresolved functional degradation, compatibility, performance, config, API/SPI, SQL dialect, feature-disabled path, or adjacent-feature risks.
   - "No side effects" means no identified but unresolved or unvalidated side-effect risk in the reviewed scope; do not require impossible exhaustive proof.
5. Test Adequacy Gate:
   - New or changed production code or behavior needs corresponding test evidence.
   - Bug fixes should have regression tests for the reported symptom or root-cause path.
   - New features should cover the main success path and important boundary, disabled, or error paths.
   - Existing tests may satisfy this gate only when they clearly exercise the changed behavior.
   - Judge tests by behavior and root-cause coverage, not by coverage-rate or environment breadth alone.
   - High-cost environment, native-image, distributed-system, or end-to-end validation blocks mergeability only when lower-level public-path tests and code evidence cannot prove the root-cause repair,
     or when the current PR itself owns that environment integration behavior.
   - For narrow split PRs whose code path can be proven locally, environment validation may be delegated to the umbrella PR or integration test scope; state that boundary in `Review Details` instead of turning it into a blocker.
   - Do not require coverage-rate proof, and do not block mergeability solely because a coverage report was not produced.
6. Code Quality Gate:
   - Block only concrete maintainability problems, such as unclear responsibility, duplicated logic, dead code, over-complex control flow, hidden state, magic values, or hard-to-read temporary design.
   - Do not turn ordinary naming/style preferences or optional nits into blockers unless they violate repository rules or create real maintenance risk.
7. Architecture Gate:
   - Trigger a deeper architecture review when the PR touches shared modules, public/shared APIs, SPI contracts, metadata, rule owners, dialect owners, session/executor state, or lifecycle state.
   - The PR must preserve module ownership, explicit contracts, SPI/metadata boundaries, and clear state models.
   - Broken layering, bypassed owner modules, target-specific semantics in shared code, or implicit lifecycle states block mergeability.
8. Release Note Gate:
   - Required when the PR introduces or changes user-visible behavior that users, DBAs, operators, or application developers need to know for upgrade, troubleshooting, configuration,
     migration, rollback, compatibility assessment, or meaningful release awareness.
   - Usually not required for test-only changes, pure refactoring with no behavior change, formatter/import/typo/comment-only changes,
     internal bug fixes that restore already documented behavior without new user action, or CI/build changes that do not affect released artifacts, supported platforms, dependencies,
     or user-visible build behavior.
   - For split or staging PRs, a release note may be deferred to the umbrella PR when the umbrella PR owns the user-facing release story and the split PR is not expected to ship independently.
     If deferring, verify and state the delegation reason; do not require duplicate release-note entries that would only create low-signal changelog noise.
   - If the split PR can be released independently and the fix has meaningful user-facing impact, require the release note in the split PR.
   - Required release notes must update `RELEASE-NOTES.md` in the proper category and describe the user-visible outcome, affected module or feature,
     and important compatibility, configuration, upgrade, or rollback impact when applicable.
   - Release notes must be understandable to users, DBAs, operators, and application developers, not only maintainers.
   - Missing, misleading, implementation-only, wrong-category, or over-claimed release notes block mergeability only after the gate determines that a release note is required for the current PR.
9. User Documentation Impact Gate:
   - If users need documentation to correctly use, configure, upgrade, troubleshoot, or understand the changed behavior, check the relevant user docs.
   - Missing required docs for user-facing configuration, DistSQL, SQL support, API/SPI usage, Proxy/JDBC behavior, or upgrade flow block mergeability.
10. Breaking Change / Migration Impact Gate:
   - If the PR changes default behavior, config keys, API/SPI contracts, protocols, metadata storage, SQL semantics, or released artifacts,
     require explicit compatibility, migration, upgrade, and rollback evidence.
   - Unexplained breaking or migration impact blocks mergeability.
11. Error Message / Diagnostics Quality Gate:
   - If the PR changes exceptions, error codes, logs, or diagnostic output, the result must be accurate, actionable, and safe for users.
   - Diagnostics that hide the real failure, mislead users, regress troubleshooting, or expose sensitive information block mergeability.
12. Dependency / Distribution Impact Gate:
   - If dependency manifests, lockfiles, distribution packaging, native-image metadata, LICENSE, NOTICE, or release artifacts change,
     check security, license, compatibility, packaging, and release impact before considering `Mergeable`.

## Not Mergeable Feedback Mode

Choose the feedback mode before writing the GitHub-facing review:

- Use `Change Request` when the PR direction is aligned with the confirmed root cause, but the current patch still needs implementation, test, scope, compatibility, or evidence changes.
- Use `Needs Discussion` when public evidence shows the PR is built on a confirmed-wrong or publicly disputed problem model, root-cause model, expected behavior, ownership boundary, protocol or SQL semantics,
  compatibility assumption, or solution direction.
- Do not use `Needs Discussion` for ordinary incomplete patches, missing tests, or missing logs when the direction is otherwise correct; request the minimum missing information or changes instead.
- Do not ask for patch-level refinement after selecting `Needs Discussion`; ask maintainers and the author to pause the current implementation direction and resolve the discussion first.
- For label recommendations, suggest existing labels only:
  - Use `type: discussion` when the current direction should be reopened or confirmed.
  - Use `status: need more info` only when missing public evidence blocks root-cause or scope classification.

## Review Boundary

- Review PR code, tests, behavior, compatibility, regression risk, and scope.
- For GitHub PRs, derive the reviewed file list from the latest PR head and GitHub `/pulls/{number}/files`, then use local git only to reproduce and inspect that scope.
- Do not inspect or use GitHub Actions, CI status, or check-run completion for the merge decision unless the user explicitly asks for CI review.
- Do not treat CI pending, failing, or passing as a review finding by default; final approvers and mergers handle that gate.
- Use the repository-declared formatting and style gates as authority. For ShardingSphere, Spotless and Checkstyle are the formatting/style gates.
- Do not treat `git diff --check` as a blocker when it conflicts with Spotless/Checkstyle behavior, unless the user explicitly asks for that check.

## Non-Behavioral Churn Rule

- Still include import-only, whitespace-only, and formatter-only files in `Reviewed Scope` when GitHub `/pulls/{number}/files` includes them.
- `import-only` includes normal imports, static imports, import ordering, import grouping, and unused-import cleanup when there is no production or test behavior change.
- Do not report import-only, whitespace-only, or formatter-only changes as `Issues`, `Unrelated Changes`, or rollback requests by default.
- Do not set `Merge Decision: Not Mergeable` solely because of import ordering, unused-import cleanup, whitespace normalization, or IDE/formatter cleanup.
- Mention them only when they are excessive, obscure the real diff, fail Spotless/Checkstyle, touch many unrelated files, or conflict with an explicit reviewer/user/repo scope rule.

## PR Diff Boundary Rule

When reviewing a GitHub PR locally, never use `base.sha..head.sha` or the current base branch tip as the PR scope boundary.
Those ranges can include changes that landed on the base branch after the PR branch diverged.

Always reproduce GitHub PR "Files changed" with triple-dot semantics:

1. Fetch the latest base ref and PR head ref.
2. Record the latest PR `head.sha`, base ref/SHA, and local `MERGE_BASE=$(git merge-base <base-ref> <head-ref>)`.
3. Review local changes with `git diff <MERGE_BASE>..<head-ref>` or `git diff <base-ref>...<head-ref>`.
4. Cross-check `git diff --name-status <MERGE_BASE>..<head-ref>` against GitHub `/pulls/{number}/files`.
5. If the file count or path list differs, do not report unrelated changes or scope findings; refresh refs or use the GitHub file list until the mismatch is explained.

## Execution Boundary

- Review output only; do not modify code.

## Evidence Source Strategy

Priority from high to low:

1. PR facts: diff, commit history, GitHub-visible review comments, and code/test changes.
2. Related issues in the same repo, relevant module code/tests, historical behavior (optional git blame/log).
3. ShardingSphere official docs and official repo conventions.
4. External authoritative specs only when necessary (official standards/docs only).

For the GitHub-facing review body, cite or summarize only public evidence from the list above.
Local verification may support the review, but output only sanitized command summaries, exit codes, and repository-relative paths.
Do not output local absolute paths, random temporary file or directory names, private/internal/downstream identifiers, private chat content, prompt text, or intermediate reasoning notes.

CI status and check-runs are out of scope unless explicitly requested by the user.

For SQL parser reviews:

- Prefer the target database's official SQL reference/manual as first-class evidence.
- When the PR or your review comment mentions a concrete SQL syntax form, cite the exact official doc page that supports that form.
- If the official docs do not clearly support the syntax, do not infer support from another dialect, secondary article, or parser implementation alone.
- Build a dialect-family evidence set when parser logic changes:
  - Check repo conventions for trunk/branch relationships first.
  - If the touched dialect is a trunk parser, inspect affected branch dialect parser files, tests, and doc paths.
  - If the touched dialect is a branch parser, inspect the trunk parser and any sibling branch dialects that may share or copy the same logic.
- Check ShardingSphere docs/examples/release notes when parser behavior changes, especially if the PR alters supported syntax, unsupported syntax, or dialect-specific examples.

Forbidden sources:

- Unverifiable blogs, forum posts, or AI-reposted content.

## Inventory Script Usage

When local git refs are available, use `scripts/build_review_inventory.py` to generate a bounded local review-inventory draft before deep review.
The script is a heuristic input, not review evidence and not a finding source by itself.

Recommended command shape:

```bash
python3 .codex/skills/review-pr/scripts/build_review_inventory.py \
  --base-ref <base-ref-or-sha> \
  --head-ref <head-ref-or-sha> \
  --previous-head <previous-reviewed-head-if-any> \
  --github-files <optional-file-containing-GitHub-PR-file-list> \
  --format markdown
```

Rules:

- The script must not replace manual review. Confirm each candidate risk through code, tests, docs, or authoritative specs before reporting it.
- The script is local-only. It does not query GitHub; if GitHub `/pulls/{number}/files` is available, pass a one-path-per-line file list with `--github-files`.
- If the script cannot run, complete the same inventory manually and mention the fallback in `Review Details`.
- Treat dirty-worktree warnings as contamination warnings. Review PR refs, not unrelated local modifications.
- Keep script output out of GitHub-facing review bodies unless it has been converted into public, verified, repo-relative evidence.

## Review Efficiency Rules

- In the current reply, prioritize `Summary`, blocking issues, and minimum next actions.
- If the PR is obviously too large (too many files or too much churn), suggest splitting first.
- If full review cannot be completed immediately, provide high-risk blockers first to avoid blocking the delivery chain.

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
- Missing evidence: mark as "not mergeable" and request minimum additional info.
- Wrong problem model, root-cause model, or direction: mark as "not mergeable", use `Feedback Mode: Needs Discussion`, and recommend `type: discussion`.
- Any substantive off-topic/unrelated changes or substantive scope expansion: mark as "not mergeable" and require rollback or scope narrowing.
  Ignore non-behavioral import-only, whitespace-only, and formatter-only churn for mergeability unless it meets the Non-Behavioral Churn Rule escalation conditions.
- Change set too large: request split first, and provide only blocker-level feedback for current version.

## Minimum Additional Information List (Fixed Template)

When information gaps block mergeability, request at least:

- Recheck scope for the PR latest version (files/modules).
- ShardingSphere version and runtime topology (JDBC/Proxy + Standalone/Cluster).
- Database type and version.
- Minimal reproducible input (SQL/request/config snippet) with expected vs actual behavior.
- Key logs or stack traces.
- Test evidence mapped one-to-one with fix points (new or adjusted tests).
- Release note, user documentation, migration, or diagnostics evidence when the PR has user-visible impact.
- For SQL parser reviews: official documentation links/pages for the exact syntax and version, plus any affected ShardingSphere doc paths or examples.

## Review Workflow

CI/check-run review is not part of this workflow unless explicitly requested; do not query or report it by default.

1. Define target and boundary: restate PR goal, impacted modules, target topology (JDBC or Proxy, Standalone or Cluster).
2. Root-cause and linked-issue modeling: reconstruct "trigger condition -> failure path -> result" from issue and code path.
   If the PR links an issue, decompose the issue body and relevant issue comments into required symptoms, expected behaviors, affected runtime topology, inputs,
   boundary cases, and maintainer-requested constraints before assessing the patch.
3. Fix mapping: verify each change covers the root-cause chain, not just symptoms.
   For linked issues, map every required issue behavior to a concrete PR change and at least one validation point; do not infer complete issue closure from one happy path.
   After this mapping, choose `Change Request` or `Needs Discussion` for any `Not Mergeable` result.
4. Risk scan:
   - Design: abstraction level, responsibility boundaries, temporary compatibility branches
   - Shared-layer ownership: if the PR is scoped to one dialect/feature but touches shared modules, separate required generic hooks from target-specific logic.
     Check whether shared code now imports, names, stores, or branches on the target dialect/feature, and whether the shared abstraction would still make sense without that target.
   - Constructor/API invariants: inspect every new/changed constructor and public/shared method. Check whether objects can now be constructed in partial, nullable, or hidden-mode states.
   - Implicit state encoding: inspect whether ordinary values are used as hidden mode switches. Search the PR diff for `null`, magic ids, empty strings,
     overloaded booleans, `UNKNOWN`/`NONE`/`DEFAULT`, empty collections, no-op implementations, type-name string checks, and begin/finish side effects.
     Ask what state the value really represents and whether that state should instead be a named object, enum, key, token, or target-module-owned lifecycle API.
   - Performance: new loops/remote calls/object allocations on hot paths
   - Performance: in Proxy/JDBC DML/DQL high-frequency SQL paths, flag direct `ConcurrentHashMap#computeIfAbsent` use without a preceding `get` miss check
   - Compatibility: behavior/config/API-SPI/SQL dialect versions
   - Regression: similar statements, adjacent features, exception branches
   - For parser, binder, routing, and default-schema changes, explicitly compare the new behavior against official dialect semantics and check whether precedence or shadowing rules changed
   - For SQL parser changes, build the dialect-family map first using repo conventions, then expand the review to related trunk / branch dialects that reuse, share, or copy the touched parser logic
   - For SQL parser changes, verify every changed syntax form in the PR against the target database's official documentation, and reject syntax support that cannot be proven from official docs
   - For SQL parser review comments, ensure every suggested SQL syntax example or recommended acceptance/rejection rule is also supported by the target database's official documentation; do not suggest parser behavior that official docs do not support
   - For each related dialect in the same parser family, decide whether the same root cause exists, whether the PR also fixes it, and whether extra review feedback is required; do not silently treat unreviewed related dialects as safe
   - Check ShardingSphere docs, examples, and release-note expectations for parser behavior changes; if docs and parser behavior diverge, require correction or explicit explanation
   - If shared code is touched, build a blast-radius list of affected dialects/features and review at least one non-target example against its documented semantics
   - If config flags or temporary properties exist on the touched path, review both enabled and disabled states
   - If exceptions, error codes, logs, or diagnostics changed, check that users can understand and act on the new output and that no sensitive data is exposed
5. Test adequacy:
   - Is there a failing case first or reproducible steps?
   - Are major branches, boundaries, and counterexamples covered?
   - Are tests mapped one-to-one with fix points?
   - Does new or changed production code or behavior have corresponding test evidence, either from new/adjusted tests or existing tests that clearly cover the changed behavior?
   - Does the requested validation prove a real behavior branch or root-cause path, rather than merely improving coverage percentage?
   - If native-image, container, cluster, performance, or other expensive environment validation is missing, decide whether the current PR truly owns that integration proof,
     or whether public-path tests plus code evidence are enough for this split PR and the environment proof belongs to a broader umbrella PR.
   - Do not require coverage-rate proof as part of this review.
   - For SQL parser family scans, check whether each related dialect with the same root cause has direct validation or explicit evidence for non-applicability
   - Distinguish fixture-assisted validation from production-path validation; if tests bypass the real assembly chain,
     metadata loader, SPI discovery path, or routing path, state that gap explicitly and downgrade confidence
   - If the PR adds multiple static metadata definitions, verify that regression tests cover the originally reported objects one-to-one; do not infer coverage from a single representative object unless the code path is truly identical and that equivalence is stated
6. Supply-chain and license gates (triggered by changes):
   - If dependency manifests, lockfiles, distribution packaging, native-image metadata, LICENSE, NOTICE, or release artifacts changed, check vulnerability severity, license constraints,
     compatibility, packaging, and release impact
   - Mark whether extra security review is required
7. Unrelated-change screening: identify substantive code/config/refactor changes not directly tied to PR goal; require removal, rollback, or scope narrowing.
   Ignore non-behavioral import-only, whitespace-only, and formatter-only churn for mergeability unless it meets the Non-Behavioral Churn Rule escalation conditions.
8. User-facing impact screening:
   - Decide whether `RELEASE-NOTES.md` is valuable and required for the current PR, not required with reason, delegated to an umbrella PR, missing, misleading, or over-claimed.
   - Do not require low-signal release-note entries for narrow internal fixes that only restore already documented behavior and do not change what users must do.
   - Decide whether user docs, migration notes, compatibility notes, or rollback guidance are required for the changed behavior.
9. Anti-drip review inventory:
   - Build an internal inventory before producing final review output.
   - Cover authoritative changed files, changed entry points, new public production types, public/shared APIs, stateful registries/caches/session fields,
     lifecycle cleanup paths, supported-vs-rejected feature boundaries, tests, release notes, docs, and verification.
   - For progress updates while reviewing, describe areas being inspected rather than releasing candidate blockers before the findings are frozen,
     unless the user explicitly asks for status or high-risk early blockers.
10. Version baseline control:
   - Base conclusion only on PR latest code version
   - If new commits are added, current conclusion becomes invalid and must be re-reviewed on latest version
11. Latest delta plus full-path review:
   - When new commits arrive after previous feedback, review the latest delta to classify newly introduced risk.
   - Re-run full-path review on the latest PR head; do not conclude mergeability only because earlier comments were fixed.
12. Self-iteration gate: repeat internal review passes until the latest pass finds no new actionable findings with an independent fix boundary.
13. Merge decision: output `Merge Decision`.
14. Generate feedback: follow the output template below.

## Anti-Drip Review Gate

Before producing the final review, build and freeze an internal review inventory for the latest PR head.
Do not expose intermediate findings, draft issue lists, or candidate blockers before the inventory is frozen, unless the user explicitly asks for status or early high-risk blockers.

The inventory must cover:

- Authoritative scope: latest head SHA, base ref/SHA, merge-base, local file list, and GitHub `/pulls/{number}/files` match status when available.
- Changed file categories: production, tests, docs, release notes, build/config, distribution, generated/baseline resources, and non-behavioral churn.
- Entry points and execution paths changed by the PR.
- New public production types and whether each has direct focused tests.
- New or changed public/shared methods, constructors, fields, return values, cache keys, and session/executor state.
- Stateful registries, caches, session fields, handles, lifecycle begin/use/free/release/error paths, and cleanup ownership.
- Supported feature matrix: what the PR accepts, rejects, or leaves unsupported; flag unsupported-but-accepted inputs.
- Boundary cases: empty, null, invalid, stale, repeated, split/coalesced, disabled, fallback, release/free, and error paths.
- Latest-commit delta risks when previous public feedback or review rounds exist.
- Release note, user documentation, migration, diagnostics, dependency, and distribution impact.

For each candidate issue, record internally:

- Evidence path and line.
- Whether it is caused by this PR, pre-existing on base, exposed by this PR, newly introduced by latest commits, or newly discovered but present in earlier PR revisions.
- The minimum independent fix boundary.
- Whether it duplicates another candidate issue.
- Whether it is a confirmed blocker, missing-evidence blocker, non-blocking risk, or out-of-scope note.

Deduplicate findings by independent fix boundary before output.
A fix boundary is independent when it requires a different code owner, lifecycle hook, protocol/model contract, validation boundary, or test contract to close safely.
Merge duplicate symptoms into one issue; split only when fixes are genuinely independent.

## Self-Iteration Gate

Before producing the final review output, run an internal self-review loop on the latest PR version:

1. Build the current candidate findings from the frozen review inventory.
2. Ask explicitly:
   "If I review this same latest PR again from a fresh critical perspective, can I find any new actionable issue, unresolved risk, missing evidence, or scope problem not already captured?"
3. Re-run the review against the authoritative PR scope, focusing on:
   - Missed root-cause, problem-model, or feedback-mode gaps.
   - Missed side effects, regression paths, test adequacy gaps, cross-dialect paths, feature-disabled paths, fallback paths, or boundary cases.
   - Missed ownership, implicit-state, unrelated-change, release note, user documentation, migration, diagnostics, dependency, distribution, output-template, or evidence gaps.
4. Include at least one explicit adversarial pass that assumes the PR is unsafe and actively searches for:
   - one cross-dialect or adjacent-feature regression path,
   - one config-disabled or feature-flag-off path,
   - one original symptom path that is only partially covered by tests.
   If any of these remain unresolved, set `Merge Decision: Not Mergeable`.
5. Include at least one latest-delta pass when new commits were added after previous public feedback, and one full-path pass on the latest PR head.
6. If the self-review finds any new actionable issue with an independent fix boundary, add it to the inventory, deduplicate it against existing findings,
   update the merge decision and next steps if needed, and repeat the loop.
7. Do not reset the loop for duplicate symptoms, optional polish, speculative risks outside the PR scope, or already captured issues.
8. Stop only after one full adversarial pass finds no new actionable issue with an independent fix boundary.
9. If the inventory cannot be completed because public evidence is unavailable, state the minimum missing evidence and set `Merge Decision: Not Mergeable` rather than emitting a partial approval.
10. Do not expose intermediate review rounds, draft decisions, raw inventory, or self-review transcripts in GitHub-facing output.
11. Produce one consolidated final review with exactly one `Merge Decision`.

## Root-Cause Validation Checklist (Must Answer Each)

- Was the true trigger point identified (not just the final error point)?
- Do changes directly fix the trigger point or key propagation path?
- Is it only adding null checks/defaults/try-catch without fixing root cause?
- Does it introduce silent error swallowing or downgrade to wrong semantics?
- Are adjacent paths sharing the same root cause also validated?
- If SQL parser is touched, were related trunk / branch dialects checked for the same root cause or divergence in shared / copied parser logic?
- Does the fix preserve the original precedence or lookup semantics for adjacent valid cases?
- Would a same-name or shadowing case take a different path after this patch?
- If the fix sits in shared code, would another dialect or feature now take a different path from its documented semantics?
- Do the target database official docs and ShardingSphere docs/examples both support the resulting parser behavior, or is a documentation mismatch still open?
- Does the config-disabled or feature-flag-off path still behave correctly?

If the root-cause chain cannot be fully proven fixed, set `Merge Decision: Not Mergeable`.

## Linked Issue Completeness Gate

Apply this gate whenever a PR title, body, commit message, label, or author comment claims to fix, close, resolve, or address one or more issues.

Must answer:

- Which issue(s) does the PR claim to fix or close?
- What are the issue's stated symptoms, expected behavior, affected topology, database type/version, configuration, and reproduction path?
- Are there implicit requirements in issue comments, maintainer replies, linked discussions, reproduction snippets, or follow-up clarifications?
- Does the PR fix every required part of the issue, or only the primary happy path?
- Does each issue requirement map to a concrete code change and at least one validation point?
- Are any issue requirements left as future work, partial support, unsupported topology, or untested behavior?
- Does the PR title/body over-claim the fix compared with the actual implementation scope?
- If the PR narrows the issue scope, is that limitation explicit, technically justified, and accepted by maintainers or the issue context?
- If the issue expectation, compatibility boundary, or ownership boundary is itself contradicted or unresolved in public evidence,
  should the PR pause implementation and reopen discussion before patch-level requests continue?

If the linked issue cannot be read, the issue scope is ambiguous, or any required issue behavior is only partially fixed or unvalidated,
set `Merge Decision: Not Mergeable` and list the minimum missing implementation or evidence.
If public issue or PR evidence shows the current implementation direction is based on a wrong root-cause model or unresolved expected behavior,
use `Feedback Mode: Needs Discussion` instead of patch-level change requests.

## Shared Scope & Implicit State Gate

Apply this gate whenever a PR touches shared modules, session state, executor/connector paths, cache contexts, SPI contracts, or public constructors/methods.

Must answer:

- What is the target scope stated by the issue/PR?
- Which changed files are shared by other dialects/features?
- For each shared change, is it a generic mechanism or target-specific semantic leakage?
- Does any shared class now contain target dialect names, string checks, protocol ids, target lifecycle methods, or target-specific comments/Javadocs?
- Did any constructor become nullable, delegate to `this(null)`, or allow partial initialization?
- Did any public/shared method start returning or accepting values that encode absence, active/inactive state, fallback, or feature enablement implicitly?
- Did any field/cache key/token use ordinary values as sentinels, such as `null`, magic numbers, empty strings, special enum values, overloaded booleans,
  empty collections, no-op objects, or type-name strings?
- Is there a begin/finish temporal side effect or context/global side channel whose state is not explicit in the API contract?
- Could the same behavior be expressed with a non-null generic key, explicit state object, clear enum, target-module-owned lifecycle API, or documented absence-return contract?
- Do tests validate generic shared behavior separately from target dialect activation?

If target-specific semantics leak into shared code, or if implicit state is used as a mode switch in new/changed shared APIs, set `Merge Decision: Not Mergeable`.

## Risk Checklist (Must Cover)

- Design risk: broken layering, duplicated logic, bypassed SPI/metadata cache, implicit state.
- Performance risk: complexity increase, extra hot-path allocations, unbounded retries, blocking I/O, or direct `ConcurrentHashMap#computeIfAbsent` in Proxy/JDBC DML/DQL high-frequency SQL paths without a preceding `get` miss check.
- Compatibility risk:
  - Behavior compatibility
  - Config compatibility
  - API/SPI compatibility
  - SQL compatibility (database/version/dialect)
  - Name-resolution or fallback-precedence compatibility
  - Feature-flag or disabled-path compatibility
- Functional degradation risk: old-scenario regression, boundary input behavior changes, error-code/exception semantic drift.
- Cross-dialect/shared-path risk: a target-specific fix changing generic behavior for other databases or features.
- SQL parser family risk: the target dialect is fixed but branch dialects or copied parser variants still retain the same defect or become inconsistent.
- Documentation risk: user-visible behavior changes without matching official-doc support, ShardingSphere docs/examples, or release notes that are valuable and required for the current PR.
- Migration risk: breaking behavior, config, API/SPI, protocol, metadata storage, SQL semantic, distribution, or rollback impact without clear user-facing guidance.
- Diagnostics risk: error messages, error codes, logs, or troubleshooting output that is misleading, unactionable, regressed, or unsafe.
- Operational risk: config migration complexity, gray-release and rollback complexity.
- Supply-chain/distribution risk: vulnerabilities, licenses, transitive dependency changes, packaging changes, native-image metadata, LICENSE, NOTICE, or release artifact changes.

## Boundary Validation Review Guidance

- Identify the authoritative input boundary before requiring validation changes.
  Examples: YAML swappers/validators, CLI parsers, REST/API request binders, SPI loaders, protocol decoders, SQL parsers, or config-center loaders.
- If the authoritative boundary already rejects invalid input and all production entry paths pass through it,
  do not require duplicate validation in downstream value holders, runtime contexts, or internal DTOs by default.
- Require downstream validation only when there is evidence of another production path that bypasses the boundary, public/shared API exposure, untrusted deserialization,
  asynchronous/shared ownership risk, or a documented invariant owned by the downstream type.
- Prefer tests that prove boundary-to-runtime propagation and adjacent valid values over adding defensive checks at every layer.
- If boundary ownership is unclear, ask for production entry-path evidence and treat duplicate validation as a design question, not an automatic blocker.

## Review Details Statement (Required in Every Review)

Each review must include a `Review Details` section with:

- `Reviewed Scope`: files/modules actually reviewed this round, plus the latest PR head SHA, local merge-base SHA when local git is used, and whether the local file list matched GitHub `/pulls/{number}/files`.
- `Not Reviewed Scope`: unreviewed or only superficially reviewed areas.
- `Verification`: reviewer-run commands and exit codes, or a short reason why local verification was not run.
- `Release Note / User Docs`: required and verified, delegated to an umbrella PR with reason, missing, not required with reason, or not reviewed.
- For SQL parser reviews, `Reviewed Scope` must explicitly name the target dialect, any related trunk / branch dialects checked, and the documentation pages / repo doc paths used to validate syntax behavior.

If a required domain expert review is still needed, include `Expert Review Needed` in `Summary`; omit it when no expert review is required.
Treat expert review as blocking only when the current PR's merge safety cannot be decided from available code, tests, docs, and authoritative evidence.
Do not mark expert review as blocking merely because the PR is adjacent to a specialized domain, or because broader umbrella/integration validation remains outside this PR's scope.
When the remaining specialized review is advisory, delegated to an umbrella PR, or only needed before a larger feature release, state that boundary in `Review Details` instead of blocking mergeability.
Require blocking expert review when merge safety for the current PR depends on specialized domains such as security, parser grammar or dialect semantics, Proxy protocol/authentication/packet behavior,
high-concurrency or high-frequency performance paths, transaction/pipeline/data consistency, shared metadata/binder/routing/default-schema behavior, dependency/license changes,
or any area the reviewer cannot confidently validate from available evidence.

## Multi-Round Change Request Comparison Rules

Apply this section only when previous feedback exists in GitHub-visible PR review comments, review threads, or change requests.
Do not output `Multi-Round Comparison` for local chat-only iterations, private reviewer analysis, or commit-history-only changes.

When GitHub-visible previous-round feedback exists, perform incremental comparison:

1. Build a "previous issues list" and mark each as:
   - Fixed
   - Partially fixed
   - Not fixed
   - Newly introduced issue
2. Classify every current blocker privately as one of:
   - Previously reported and still unresolved
   - Previously reported and partially fixed
   - Newly introduced by latest commits
   - Newly discovered but present in earlier PR revisions
   - Pre-existing on base and only exposed by this PR
   - Out of PR scope
3. Keep only unresolved, partially fixed, and newly discovered current blockers in the GitHub-facing review.
   Do not repeat closed items except in a concise `Multi-Round Comparison`.
4. Every suggestion must cite corresponding diff evidence.
5. For partially fixed items, specify exactly what is still needed to close.
6. If new commits were added, continue review only on the latest version, but use prior heads only to classify origin; no need to output historical commit SHA unless useful and public-safe.
7. If the latest review changes the feedback mode to `Needs Discussion`, stop tracking old patch-level requests as the primary review path;
   summarize only the public evidence that makes the current direction need discussion.
8. Do not include private reviewer accountability, local chat context, raw inventory, or internal origin notes in GitHub-facing review.

## Output Structure

### GitHub Review Markdown Requirements

- Format every review as GitHub-flavored Markdown that can be pasted directly into a PR comment or review body.
- The GitHub-facing review body must not be wrapped in a code fence, blockquote, XML/HTML container, or plain-text transcript.
- Use the same natural language as the user request unless the user explicitly asks for another language.
- Use Markdown headings (`### Summary`, `### Issues`, etc.) with a blank line before and after each heading.
- Keep the GitHub Markdown structure unchanged regardless of output language.
- Keep `Merge Decision: ...` as a bold bullet in `Summary`, and output exactly one merge-decision line.
- Keep stable review labels in English, such as `Merge Decision`, `Feedback Mode`, `Reason`, `Expert Review Needed`, `Reviewed Scope`, `Not Reviewed Scope`, and `Verification`,
  so they remain searchable and consistent.
- Do not include internal self-iteration rounds, draft decisions, or self-review transcripts in the GitHub-facing review body.
- Keep `Reason` to one sentence or one short bullet; put proof in `Evidence`, and put confirmed blockers plus missing required proof in `Issues`.
- Use `Evidence` for facts found in the reviewed artifacts, such as code paths, tests, documentation, compatibility checks, and root-cause proof.
- Use `Verification` only for reviewer-run local commands and exit codes, or why those commands were not run.
- Use short unordered bullets under each heading; use bold inline labels such as `Problem:`, `Impact:`, `Required Change:`, and `Discussion Needed:` for issue details.
- In `### Issues`, list every merge-blocking issue discovered in the reviewed scope; do not imply that other unlisted blocking issues exist.
- Use only `P0`, `P1`, or `P2` for issue severity: `P0` for security, data-loss, metadata corruption, or broken core behavior; `P1` for confirmed functional regressions,
  incomplete root-cause fixes, incompatible behavior, or high-risk side effects; `P2` for lower-risk but still merge-blocking defects, missing targeted validation, or required scope cleanup.
- Omit optional nits from `### Issues` unless they are part of a merge-blocking pattern.
- If missing required evidence blocks mergeability, mention that in `Summary` -> `Reason`, and include the detailed blocker in `### Issues`.
- Only missing evidence that blocks mergeability belongs in `### Issues`; non-blocking verification gaps belong in `Review Details` -> `Verification`.
- Do not put blocking missing-evidence requests only in `Review Details`; that section is for review scope and verification facts.
- Omit `### Next Steps` unless it adds non-duplicative cross-issue sequencing, verification commands, or minimum missing information.
- Use repo-relative paths with line numbers for file evidence, for example `infra/.../Foo.java:123`; do not use local absolute file paths in GitHub-facing review text.
- For non-file direction evidence, use public anchors such as `PR description`, `linked issue`, `review thread`, or official documentation links.
- Prefer bullets over tables. Use tables only for compact status summaries that remain readable in GitHub's narrow review pane.
- Keep command evidence in inline code or short fenced blocks; avoid long raw JSON, full logs, or unrendered terminal transcripts.
- Before final output, perform a formatting self-check on the inner GitHub-facing review body:
  - The inner GitHub-facing review body is not wrapped in a code fence, blockquote, XML/HTML container, or transcript.
  - The inner GitHub-facing review body contains the required `###` headings for the selected decision template.
  - The inner GitHub-facing review body contains exactly one bold `Merge Decision: ...` line.
  - File references are repo-relative paths with line numbers, and non-file direction evidence uses public anchors.
  - Stable labels remain in English.
  - The inner GitHub-facing review body contains no non-public downstream project names, private/internal repository names, customer-specific or vendor-specific context,
    private chat content, prompt process, internal self-review notes, local-only archive details, private migration background, or intermediate discussion results.
  - Local verification evidence is sanitized: no local absolute paths, home directories, random temporary file or directory names, tokens, private addresses, or undisclosed vulnerability details.

### Codex Chat Delivery

- When returning the review in Codex chat for the user to copy, wrap the GitHub-facing review body in a fenced `markdown` code block.
- The fenced code block is only a chat delivery wrapper; it is not part of the GitHub-facing review body.
- Tell the user to copy only the content inside the fenced block.
- Keep any copy instruction outside the fenced block, and keep it free of non-public downstream, private/internal, customer-specific, vendor-specific,
  prompt-process, or intermediate-discussion details.
- When posting directly to GitHub through an API or tool, submit only the inner GitHub-facing review body and do not include the outer fence.
- Apply the formatting self-check to the inner GitHub-facing review body, not to the chat delivery wrapper.

### A. Not Mergeable (Change Request)

Use committer tone, gentle wording, no emojis; use this GitHub Markdown skeleton for required sections:

When required, add `- **Expert Review Needed:** ...` under `Summary`; omit this bullet when no expert review is required.

```markdown
### Summary

- **Merge Decision: Not Mergeable**
- **Feedback Mode: Change Request**
- **Reason:** ...

### Issues

- **[P0|P1|P2] Short issue title** (`path/to/File.java:123`)
  - **Problem:** ...
  - **Impact:** ...
  - **Required Change:** Please ...

### Review Details

- **Reviewed Scope:** ...
- **Not Reviewed Scope:** ...
- **Verification:** ...
- **Release Note / User Docs:** ...
```

Optional sections for `Not Mergeable`; do not output optional headings with placeholder text:

- `### Positive Feedback`: insert after `Summary` only when there is a genuinely correct direction-aligned change.
- `### Unrelated Changes`: insert after `Issues` only when substantive unrelated changes or substantive scope expansion exist, and explicitly ask for rollback or scope narrowing.
- `### Next Steps`: insert before `Review Details` only when it adds non-duplicative cross-issue sequencing, verification commands, or minimum missing information.
- `### Multi-Round Comparison`: insert before `Review Details` only when previous-round feedback exists in GitHub-visible PR comments, review threads, or change requests.

### B. Not Mergeable (Needs Discussion)

Use this template when the current PR direction, root-cause model, problem framing, expected behavior, or ownership boundary must be reopened before implementation continues.
Do not include patch-level `Required Change` bullets for the current implementation direction.

When required, add `- **Expert Review Needed:** ...` under `Summary`; omit this bullet when no expert review is required.

```markdown
### Summary

- **Merge Decision: Not Mergeable**
- **Feedback Mode: Needs Discussion**
- **Reason:** ...

### Discussion Needed

- **Problem:** ...
- **Impact:** ...
- **Discussion Needed:** Please pause the current implementation direction and confirm ...
- **Suggested Label:** `type: discussion`

### Review Details

- **Reviewed Scope:** ...
- **Not Reviewed Scope:** ...
- **Verification:** ...
- **Release Note / User Docs:** ...
```

Use `status: need more info` instead of `type: discussion` only when missing public evidence blocks root-cause or scope classification.

### C. Mergeable

Use this GitHub Markdown skeleton:

When required, add `- **Expert Review Needed:** ...` under `Summary`; omit this bullet when no expert review is required.

```markdown
### Summary

- **Merge Decision: Mergeable**
- **Reason:** ...

### Evidence

- Root-cause fix evidence.
- Hard gate evidence covering scope/ownership, regression risk, tests, code quality, architecture, and user-facing release/docs impact.

### Review Details

- **Reviewed Scope:** ...
- **Not Reviewed Scope:** ...
- **Verification:** Reviewer-run local verification and exit codes, or why local verification was not run.
- **Release Note / User Docs:** Required and verified, delegated to an umbrella PR with reason, missing, not required with reason, or not reviewed.
```

## Feedback Tone Guidelines

- Use "suggest / please / need" rather than accusatory commands.
- Facts first, judgment second; avoid emotional wording.
- For `Change Request`, suggested sentence patterns:
  - "This part is in the right direction, especially ..."
  - "There are still several issues affecting mergeability; please address them first: ..."
  - "This introduces new risk; please fix or roll back this part."
  - "Please continue refining it, and I will do another focused review after that."
- For `Needs Discussion`, state that the current direction does not appear to address the confirmed root cause or expected behavior.
  Ask maintainers and the author to pause implementation and reopen discussion; avoid wording like "wrong solution" or requests to keep refining the current patch.

## Prohibited Items

- Do not output `Mergeable` when evidence is insufficient or risks are unclear.
- Do not use "fallback logic passes tests" to replace proof of root-cause repair.
- Do not treat fixture-injected or mocked-path tests as full end-to-end proof without explicitly stating the gap.
- Do not output `Mergeable` only because previous-round blockers were closed; always do one fresh-pass semantic and regression scan on the latest head.
- Do not ignore substantive unrelated changes.
- Do not reuse old conclusions after new commits are added without re-review.
- Do not include emojis in review feedback text.
- Do not inspect or report GitHub Actions / CI status unless explicitly requested.
- Do not include non-public downstream project names, private/internal repository names, customer-specific or vendor-specific context, private chats, prompt process, internal archive details,
  private migration background, intermediate discussion results, local absolute paths, or unsanitized temporary file names in GitHub-facing review output.
- Do not output patch-level `Required Change` requests after selecting `Feedback Mode: Needs Discussion`.
- Do not output `Mergeable` when a required hard gate remains unresolved, including missing required test evidence, release notes, user docs, migration guidance, or diagnostic quality evidence.
- Do not output `Mergeable` for a shared-code change unless you have checked at least one non-target dialect or feature that also uses the changed path.
- Do not output `Mergeable` when local verification omitted `-am` on a module-scoped Maven run and dependency freshness matters.
- Do not output `Mergeable` when Proxy/JDBC DML/DQL high-frequency SQL paths directly call `ConcurrentHashMap#computeIfAbsent` without a preceding `get` miss check.
