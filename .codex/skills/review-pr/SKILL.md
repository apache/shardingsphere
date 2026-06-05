---
name: review-pr
description: >-
  Used to review whether an Apache ShardingSphere PR truly fixes the root cause,
  assess side effects and regression risks, and determine whether it can be safely merged.
  If not mergeable, produce committer-tone change request suggestions.
  Supports targeted comparison across GitHub-visible review rounds when prior PR comments or review threads exist.
  Before final output, internally self-iterate the review until no new actionable findings are discovered.
---

# Review PR

## Objective

- Make merge decisions for ShardingSphere PRs with a "root-cause-first, evidence-first" approach.
- Output a single merge decision:
  - `Merge Decision`: `Mergeable` / `Not Mergeable`

## Trigger Scenarios

- The user asks you to review a PR.
- The user asks whether a PR "can be merged" or "fixes the root cause."
- The user asks you to generate committer-tone change request comments.

## Mandatory Constraints

1. Verify root-cause repair first, then fallback logic; do not accept "fallback only" as a substitute for root-cause repair.
2. You must scan side effects and risks:
   - Design consistency
   - Performance (complexity, hot paths, memory, and I/O)
   - Compatibility (behavior, config, API-SPI, SQL dialect)
   - Functional degradation and regression surface
3. You must provide exactly one `Merge Decision`.
4. If evidence is insufficient, risk is unclear, or validation is incomplete, always set `Merge Decision: Not Mergeable`,
   and list the minimum additional information required.
5. Change request replies must be gentle in tone and contain no emojis.
6. If substantive unrelated changes or substantive scope expansion exist, you must explicitly ask for rollback or scope narrowing; if none exist, do not output that section.
   Non-behavioral import-only, whitespace-only, formatter-only, or IDE cleanup changes do not count as substantive unrelated changes by default.
   `import-only` includes normal imports, static imports, import ordering, import grouping, and unused-import cleanup when there is no behavior change.
   These changes must not affect `Merge Decision` unless they cause repository-declared formatting/style gate failures, hide behavior changes, touch broad unrelated areas, or violate an explicit user/repo scope rule.
7. Any "fallback-only without root-cause repair" or "unresolved risk" must not receive `Merge Decision: Mergeable`.
8. Review only the PR's latest code version; do not reuse conclusions from older versions.
9. If a patch changes name resolution, default schema, fallback binding, routing precedence, or identifier interpretation,
   you must perform an explicit semantic-compatibility review against the documented behavior of the target database or framework
   before considering `Mergeable`.
10. For any change that alters lookup order or fallback targets, you must validate at least one counterexample scenario,
    for example: same-name object, shadowing, disabled-flag path, or adjacent valid input.
    If no such validation exists, bias to `Not Mergeable`.
11. Closing previous-round blockers and passing happy-path tests is not sufficient for `Mergeable`; you must still run a fresh-pass risk scan on the latest head.
12. If a PR touches any shared execution path, reusable SPI, metadata assembly path, or other code that can affect multiple dialects or features,
    treat it as a blast-radius review.
    Trigger signals (examples only, not a whitelist): `infra/common`, `infra/binder/core`, shared kernel entrypoints, common SPI, reusable metadata loaders,
    shared name-resolution logic, or shared default-schema / fallback logic.
    You must explicitly enumerate the non-target dialects or features that also execute that path, and review at least one concrete counterexample outside the PR's stated target.
    Do not interpret the trigger signals above as a complete list. If this blast-radius scan is missing, bias to `Not Mergeable`.
13. If local verification is used to support mergeability and the command is module-scoped, include `-am` by default unless you can prove all dependent modules were built from the same latest PR head.
    Do not treat a scoped run without dependent modules as conclusive evidence for `Mergeable`.
14. Before any final output, complete the `Self-Iteration Gate` and stop only after the latest internal review pass finds no new actionable issues.
    Do not expose intermediate review rounds; output one consolidated review with exactly one `Merge Decision`.
15. During the self-iteration loop, include at least one explicit adversarial pass that assumes the PR is unsafe and actively searches for:
    - one cross-dialect or adjacent-feature regression path,
    - one config-disabled or feature-flag-off path,
    - one original symptom path that is only partially covered by tests.
    If any of these remain unresolved, set `Merge Decision: Not Mergeable`.
16. If a PR changes SQL parser behavior, grammar, visitor logic, supported SQL cases, or parser tests for a dialect,
    you must complete both syntax-validity review and dialect-family impact review before concluding:
    - Every SQL syntax added, changed, accepted, rejected, or suggested in the review must be backed by the target database's official documentation for the exact database family and relevant version.
    - This applies both to the PR's changed SQL syntax and to any SQL syntax examples or recommendations you propose in review comments.
    - Follow the SQL parsing maintenance relationships defined by the repo conventions, for example `MySQL -> MariaDB, Doris`.
    - If the PR changes a trunk dialect parser, inspect whether each branch dialect that reuses or copies that parser logic has the same root cause, regression risk, or missing validation.
    - If the PR changes a branch dialect parser, inspect whether the same root cause also exists in the corresponding trunk dialect or sibling branch dialects because of shared or copied grammar / visitor logic.
    - For each related dialect, classify it as: `same issue confirmed`, `not applicable with evidence`, or `unresolved`.
    - If official documentation support is missing, ambiguous, or contradicts the PR behavior, bias to `Merge Decision: Not Mergeable`.
    - If any related dialect remains unresolved, or the review skips the family scan, bias to `Merge Decision: Not Mergeable`.
    - Do not recommend unsupported or undocumented SQL syntax in review feedback.
17. If a method reachable from the Proxy or JDBC DML/DQL high-frequency SQL execution path uses `ConcurrentHashMap#computeIfAbsent`,
    require a preceding `get` lookup and call `computeIfAbsent` only when the value is missing, to avoid the JDK 8 implementation bottleneck.
    If this pattern is absent and the path is high-frequency, bias to `Merge Decision: Not Mergeable`.
18. Do not use GitHub Actions, CI status, or check-run completion as part of the merge decision unless explicitly requested by the user.
19. Use repository-declared formatting/style gates as the formatting authority; do not introduce extra formatting blockers outside those gates by default.
20. Treat GitHub PR metadata and `/pulls/{number}/files` as the authoritative scope boundary.
    Before reporting unrelated changes or making any scope-based finding, verify that the local diff file list matches GitHub's file list.
    If the lists differ, stop the review, refresh the PR refs, and resolve the diff-boundary mismatch before drawing conclusions.
21. If a target-dialect or target-feature fix touches shared modules, run a shared-layer ownership gate before considering `Mergeable`.
    Classify every shared change as one of: required generic hook, target-specific semantic leakage, or unrelated/substantive scope expansion.
    If shared code contains target dialect names, target protocol state, target-specific method names, hard-coded target database type strings, or target lifecycle concepts,
    bias to `Merge Decision: Not Mergeable` unless the PR proves this is an intentional generic contract for all affected dialects/features.
22. For new or changed constructors, public/shared methods, return values, fields, cache keys, and session/executor state, run an implicit-state review.
    Look for ordinary values used as hidden business states, mode switches, lifecycle markers, or feature flags, including but not limited to:
    `null`, magic numbers, empty strings, overloaded booleans, special enum values, empty collections, no-op objects, type-name string checks,
    partially initialized objects, begin/finish temporal side effects, and context/global side channels.
    If such implicit state controls behavior across shared modules or public APIs, require an explicit model such as a non-null key/token,
    a dedicated state object, a clear enum with one meaning per value, or an explicit absence-return contract where repository rules allow it.
    If the implicit state leaks target-specific semantics into shared code, bias to `Merge Decision: Not Mergeable`.
23. If a PR claims to fix, close, or resolve one or more issues, run a linked-issue completeness gate before considering `Mergeable`.
    Read the linked issue body and relevant issue comments, decompose the issue into required symptoms, expected behaviors, affected topologies, inputs, and edge cases,
    and map each required issue behavior to concrete PR code changes and validation evidence.
    If the issue cannot be read, the issue scope is ambiguous, any required issue behavior is only partially fixed, or the PR over-claims the fix scope,
    bias to `Merge Decision: Not Mergeable` and list the minimum missing implementation or evidence.

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

## Review Efficiency Rules

- In the current reply, prioritize `Summary`, blocking issues, and minimum next actions.
- If the PR is obviously too large (too many files or too much churn), suggest splitting first.
- If full review cannot be completed immediately, provide high-risk blockers first to avoid blocking the delivery chain.

## Quick Triage

Before deep review, answer:

1. Does the PR clearly state the problem and expected behavior?
2. Do current changes directly touch the suspected root-cause path?
3. Is there corresponding validation (unit/integration/regression tests)?
4. Are there file changes unrelated to the stated goal?
5. Does the patch change documented semantics such as name resolution, default schema, fallback precedence, or routing order?
6. Is there at least one counterexample or negative scenario validated, not only the reported happy path?
7. If SQL parser is changed, has the review checked related trunk / branch dialects and confirmed whether the same issue also exists there?
8. If SQL parser is changed, do official docs and ShardingSphere docs both support the new behavior, or is a doc update required?
9. If the PR claims to fix one dialect/feature, do shared modules contain target-specific names, strings, state, or lifecycle methods?
10. Are all shared-module changes generic hooks, or did the target-specific semantic owner move into shared code?
11. Did the PR add or change constructors/public APIs in a way that allows partial initialization or hidden modes?
12. Does the PR use ordinary values such as `null`, magic ids, empty strings, booleans, empty collections, no-op objects, or special enum values to represent feature-disabled,
    cache-inactive, no-current-context, fallback, or other business state?
13. If the PR links or claims to fix an issue, has the review decomposed the issue into required behaviors and verified that the PR fully covers each one?

Triage policy:

- Information complete: proceed with full review.
- Missing evidence: mark as "not mergeable" and request minimum additional info.
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
- For SQL parser reviews: official documentation links/pages for the exact syntax and version, plus any affected ShardingSphere doc paths or examples.

## Review Workflow

CI/check-run review is not part of this workflow unless explicitly requested; do not query or report it by default.

1. Define target and boundary: restate PR goal, impacted modules, target topology (JDBC or Proxy, Standalone or Cluster).
2. Root-cause and linked-issue modeling: reconstruct "trigger condition -> failure path -> result" from issue and code path.
   If the PR links an issue, decompose the issue body and relevant issue comments into required symptoms, expected behaviors, affected runtime topology, inputs,
   boundary cases, and maintainer-requested constraints before assessing the patch.
3. Fix mapping: verify each change covers the root-cause chain, not just symptoms.
   For linked issues, map every required issue behavior to a concrete PR change and at least one validation point; do not infer complete issue closure from one happy path.
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
5. Test adequacy:
   - Is there a failing case first or reproducible steps?
   - Are major branches, boundaries, and counterexamples covered?
   - Are tests mapped one-to-one with fix points?
   - For SQL parser family scans, check whether each related dialect with the same root cause has direct validation or explicit evidence for non-applicability
   - Distinguish fixture-assisted validation from production-path validation; if tests bypass the real assembly chain,
     metadata loader, SPI discovery path, or routing path, state that gap explicitly and downgrade confidence
   - If the PR adds multiple static metadata definitions, verify that regression tests cover the originally reported objects one-to-one; do not infer coverage from a single representative object unless the code path is truly identical and that equivalence is stated
6. Supply-chain and license gates (triggered by changes):
   - If dependency manifests or lockfiles changed, check vulnerability severity and license constraints
   - Mark whether extra security review is required
7. Unrelated-change screening: identify substantive code/config/refactor changes not directly tied to PR goal; require removal, rollback, or scope narrowing.
   Ignore non-behavioral import-only, whitespace-only, and formatter-only churn for mergeability unless it meets the Non-Behavioral Churn Rule escalation conditions.
8. Version baseline control:
   - Base conclusion only on PR latest code version
   - If new commits are added, current conclusion becomes invalid and must be re-reviewed on latest version
9. Self-iteration gate: repeat internal review passes until the latest pass finds no new actionable findings.
10. Merge decision: output `Merge Decision`.
11. Generate feedback: follow the output template below.

## Self-Iteration Gate

Before producing the final review output, run an internal self-review loop on the latest PR version:

1. Build the current candidate findings from the completed review pass.
2. Ask explicitly:
   "If I review this same latest PR again from a fresh critical perspective, can I find any new actionable issue, unresolved risk, missing evidence, or scope problem not already captured?"
3. Re-run the review against the authoritative PR scope, focusing on:
   - Missed root-cause gaps.
   - Missed side effects or regression paths.
   - Missed test adequacy gaps.
   - Missed cross-dialect, feature-disabled, fallback, or boundary cases.
   - Missed shared-layer ownership problems: target-specific concepts placed in shared modules, unclear owners for shared state, or generic hooks mixed with dialect semantics.
   - Missed implicit-state problems: nullable constructors, partial object states, magic values, overloaded booleans, no-op objects, type-name switches, or other hidden mode encodings.
   - Missed unrelated substantive changes.
   - Missed output-template or evidence gaps.
4. If the self-review finds any new actionable issue, add it to the candidate findings, deduplicate it against existing findings,
   update the merge decision and next steps if needed, and repeat the loop.
5. Do not treat restatements, optional polish, speculative risks outside the PR scope, or already captured issues as new actionable findings.
6. Stop only when the latest self-review pass finds no new actionable issues compared with the accumulated candidate findings.
7. Do not expose intermediate review rounds, draft decisions, or self-review transcripts to the user.
8. Produce one consolidated final review with exactly one `Merge Decision`.

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

If the linked issue cannot be read, the issue scope is ambiguous, or any required issue behavior is only partially fixed or unvalidated,
set `Merge Decision: Not Mergeable` and list the minimum missing implementation or evidence.

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
- Documentation risk: parser behavior changes without matching official-doc support or without aligning ShardingSphere docs/examples.
- Operational risk: config migration complexity, gray-release and rollback complexity.
- Supply-chain risk: vulnerabilities, licenses, transitive dependency changes from new deps.

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
- For SQL parser reviews, `Reviewed Scope` must explicitly name the target dialect, any related trunk / branch dialects checked, and the documentation pages / repo doc paths used to validate syntax behavior.

If a required domain expert review is still needed, include `Expert Review Needed` in `Summary`; omit it when no expert review is required.
Treat required expert review as blocking unless the evidence proves the remaining review is advisory only.
Require expert review when merge safety depends on specialized domains such as security, parser grammar or dialect semantics, Proxy protocol/authentication/packet behavior,
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
2. Keep only unresolved and newly introduced items as current focus; do not repeat closed items.
3. Every suggestion must cite corresponding diff evidence.
4. For partially fixed items, specify exactly what is still needed to close.
5. If new commits were added, continue review only on the latest version; no need to output historical commit SHA.

## Output Structure

### GitHub Review Markdown Requirements

- Format every review as GitHub-flavored Markdown that can be pasted directly into a PR comment or review body.
- The GitHub-facing review body must not be wrapped in a code fence, blockquote, XML/HTML container, or plain-text transcript.
- Use the same natural language as the user request unless the user explicitly asks for another language.
- Use Markdown headings (`### Summary`, `### Issues`, etc.) with a blank line before and after each heading.
- Keep the GitHub Markdown structure unchanged regardless of output language.
- Keep `Merge Decision: ...` as a bold bullet in `Summary`, and output exactly one merge-decision line.
- Keep stable review labels in English, such as `Merge Decision`, `Reason`, `Expert Review Needed`, `Reviewed Scope`, `Not Reviewed Scope`, and `Verification`,
  so they remain searchable and consistent.
- Do not include internal self-iteration rounds, draft decisions, or self-review transcripts in the GitHub-facing review body.
- Keep `Reason` to one sentence or one short bullet; put proof in `Evidence`, and put confirmed blockers plus missing required proof in `Issues`.
- Use `Evidence` for facts found in the reviewed artifacts, such as code paths, tests, documentation, compatibility checks, and root-cause proof.
- Use `Verification` only for reviewer-run local commands and exit codes, or why those commands were not run.
- Use short unordered bullets under each heading; use bold inline labels such as `Problem:`, `Impact:`, and `Required Change:` for issue details.
- In `### Issues`, list every merge-blocking issue discovered in the reviewed scope; do not imply that other unlisted blocking issues exist.
- Use only `P0`, `P1`, or `P2` for issue severity: `P0` for security, data-loss, metadata corruption, or broken core behavior; `P1` for confirmed functional regressions,
  incomplete root-cause fixes, incompatible behavior, or high-risk side effects; `P2` for lower-risk but still merge-blocking defects, missing targeted validation, or required scope cleanup.
- Omit optional nits from `### Issues` unless they are part of a merge-blocking pattern.
- If missing required evidence blocks mergeability, mention that in `Summary` -> `Reason`, and include the detailed blocker in `### Issues`.
- Only missing evidence that blocks mergeability belongs in `### Issues`; non-blocking verification gaps belong in `Review Details` -> `Verification`.
- Do not put blocking missing-evidence requests only in `Review Details`; that section is for review scope and verification facts.
- Omit `### Next Steps` unless it adds non-duplicative cross-issue sequencing, verification commands, or minimum missing information.
- Use repo-relative paths with line numbers, for example `infra/.../Foo.java:123`; do not use local absolute file paths in GitHub-facing review text.
- Prefer bullets over tables. Use tables only for compact status summaries that remain readable in GitHub's narrow review pane.
- Keep command evidence in inline code or short fenced blocks; avoid long raw JSON, full logs, or unrendered terminal transcripts.
- Before final output, perform a formatting self-check on the inner GitHub-facing review body:
  - The inner GitHub-facing review body is not wrapped in a code fence, blockquote, XML/HTML container, or transcript.
  - The inner GitHub-facing review body contains the required `###` headings for the selected decision template.
  - The inner GitHub-facing review body contains exactly one bold `Merge Decision: ...` line.
  - File references are repo-relative paths with line numbers, not local absolute paths.
  - Stable labels remain in English.

### Codex Chat Delivery

- When returning the review in Codex chat for the user to copy, wrap the GitHub-facing review body in a fenced `markdown` code block.
- The fenced code block is only a chat delivery wrapper; it is not part of the GitHub-facing review body.
- Tell the user to copy only the content inside the fenced block.
- Keep any copy instruction outside the fenced block.
- When posting directly to GitHub through an API or tool, submit only the inner GitHub-facing review body and do not include the outer fence.
- Apply the formatting self-check to the inner GitHub-facing review body, not to the chat delivery wrapper.

### A. Not Mergeable (Change Request)

Use committer tone, gentle wording, no emojis; use this GitHub Markdown skeleton for required sections:

When required, add `- **Expert Review Needed:** ...` under `Summary`; omit this bullet when no expert review is required.

```markdown
### Summary

- **Merge Decision: Not Mergeable**
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
```

Optional sections for `Not Mergeable`; do not output optional headings with placeholder text:

- `### Positive Feedback`: insert after `Summary` only when there is a genuinely correct direction-aligned change.
- `### Unrelated Changes`: insert after `Issues` only when substantive unrelated changes or substantive scope expansion exist, and explicitly ask for rollback or scope narrowing.
- `### Next Steps`: insert before `Review Details` only when it adds non-duplicative cross-issue sequencing, verification commands, or minimum missing information.
- `### Multi-Round Comparison`: insert before `Review Details` only when previous-round feedback exists in GitHub-visible PR comments, review threads, or change requests.

### B. Mergeable

Use this GitHub Markdown skeleton:

When required, add `- **Expert Review Needed:** ...` under `Summary`; omit this bullet when no expert review is required.

```markdown
### Summary

- **Merge Decision: Mergeable**
- **Reason:** ...

### Evidence

- Root-cause fix evidence.
- Risk assessment results proving no unresolved risk.

### Review Details

- **Reviewed Scope:** ...
- **Not Reviewed Scope:** ...
- **Verification:** Reviewer-run local verification and exit codes, or why local verification was not run.
```

## Change Request Tone Guidelines

- Use "suggest / please / need" rather than accusatory commands.
- Facts first, judgment second; avoid emotional wording.
- Suggested sentence patterns:
  - "This part is in the right direction, especially ..."
  - "There are still several issues affecting mergeability; please address them first: ..."
  - "This introduces new risk; please fix or roll back this part."
  - "Please continue refining it, and I will do another focused review after that."

## Prohibited Items

- Do not output `Mergeable` when evidence is insufficient or risks are unclear.
- Do not use "fallback logic passes tests" to replace proof of root-cause repair.
- Do not treat fixture-injected or mocked-path tests as full end-to-end proof without explicitly stating the gap.
- Do not output `Mergeable` only because previous-round blockers were closed; always do one fresh-pass semantic and regression scan on the latest head.
- Do not ignore substantive unrelated changes.
- Do not reuse old conclusions after new commits are added without re-review.
- Do not include emojis in change request text.
- Do not inspect or report GitHub Actions / CI status unless explicitly requested.
- Do not output `Mergeable` for a shared-code change unless you have checked at least one non-target dialect or feature that also uses the changed path.
- Do not output `Mergeable` when local verification omitted `-am` on a module-scoped Maven run and dependency freshness matters.
- Do not output `Mergeable` when Proxy/JDBC DML/DQL high-frequency SQL paths directly call `ConcurrentHashMap#computeIfAbsent` without a preceding `get` miss check.
