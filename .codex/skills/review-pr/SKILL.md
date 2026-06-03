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
  - `Merge Verdict`: `Mergeable` / `Not Mergeable`

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
3. You must provide exactly one `Merge Verdict`.
4. If evidence is insufficient, risk is unclear, or validation is incomplete, always set `Merge Verdict: Not Mergeable`,
   and list the minimum additional information required.
5. Change request replies must be gentle in tone and contain no emojis.
6. If substantive unrelated changes exist, you must explicitly ask for rollback; if none exist, do not output that section.
   Non-behavioral import-only, whitespace-only, formatter-only, or IDE cleanup changes do not count as substantive unrelated changes by default.
   `import-only` includes normal imports, static imports, import ordering, import grouping, and unused-import cleanup when there is no behavior change.
   These changes must not affect `Merge Verdict` unless they cause repository-declared formatting/style gate failures, hide behavior changes, touch broad unrelated areas, or violate an explicit user/repo scope rule.
7. Any "fallback-only without root-cause repair" or "unresolved risk" must not receive `Merge Verdict: Mergeable`.
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
    Do not expose intermediate review rounds; output one consolidated review with exactly one `Merge Verdict`.
15. During the self-iteration loop, include at least one explicit adversarial pass that assumes the PR is unsafe and actively searches for:
    - one cross-dialect or adjacent-feature regression path,
    - one config-disabled or feature-flag-off path,
    - one original symptom path that is only partially covered by tests.
    If any of these remain unresolved, set `Merge Verdict: Not Mergeable`.
16. If a PR changes SQL parser behavior, grammar, visitor logic, supported SQL cases, or parser tests for a dialect,
    you must complete both syntax-validity review and dialect-family impact review before concluding:
    - Every SQL syntax added, changed, accepted, rejected, or suggested in the review must be backed by the target database's official documentation for the exact database family and relevant version.
    - This applies both to the PR's changed SQL syntax and to any SQL syntax examples or recommendations you propose in review comments.
    - Follow the SQL parsing maintenance relationships defined by the repo conventions, for example `MySQL -> MariaDB, Doris`.
    - If the PR changes a trunk dialect parser, inspect whether each branch dialect that reuses or copies that parser logic has the same root cause, regression risk, or missing validation.
    - If the PR changes a branch dialect parser, inspect whether the same root cause also exists in the corresponding trunk dialect or sibling branch dialects because of shared or copied grammar / visitor logic.
    - For each related dialect, classify it as: `same issue confirmed`, `not applicable with evidence`, or `unresolved`.
    - If official documentation support is missing, ambiguous, or contradicts the PR behavior, bias to `Merge Verdict: Not Mergeable`.
    - If any related dialect remains unresolved, or the review skips the family scan, bias to `Merge Verdict: Not Mergeable`.
    - Do not recommend unsupported or undocumented SQL syntax in review feedback.
17. If a method reachable from the Proxy or JDBC DML/DQL high-frequency SQL execution path uses `ConcurrentHashMap#computeIfAbsent`,
    require a preceding `get` lookup and call `computeIfAbsent` only when the value is missing, to avoid the JDK 8 implementation bottleneck.
    If this pattern is absent and the path is high-frequency, bias to `Merge Verdict: Not Mergeable`.
18. Do not use GitHub Actions, CI status, or check-run completion as part of the merge verdict unless explicitly requested by the user.
19. Use repository-declared formatting/style gates as the formatting authority; do not introduce extra formatting blockers outside those gates by default.
20. Treat GitHub PR metadata and `/pulls/{number}/files` as the authoritative scope boundary.
    Before reporting unrelated changes or making any scope-based finding, verify that the local diff file list matches GitHub's file list.
    If the lists differ, stop the review, refresh the PR refs, and resolve the diff-boundary mismatch before drawing conclusions.

## Review Boundary

- Review PR code, tests, behavior, compatibility, regression risk, and scope.
- For GitHub PRs, derive the reviewed file list from the latest PR head and GitHub `/pulls/{number}/files`, then use local git only to reproduce and inspect that scope.
- Do not inspect or use GitHub Actions, CI status, or check-run completion for the merge verdict unless the user explicitly asks for CI review.
- Do not treat CI pending, failing, or passing as a review finding by default; final approvers and mergers handle that gate.
- Use the repository-declared formatting and style gates as authority. For ShardingSphere, Spotless and Checkstyle are the formatting/style gates.
- Do not treat `git diff --check` as a blocker when it conflicts with Spotless/Checkstyle behavior, unless the user explicitly asks for that check.

## Non-Behavioral Churn Rule

- Still include import-only, whitespace-only, and formatter-only files in `Reviewed Scope` when GitHub `/pulls/{number}/files` includes them.
- `import-only` includes normal imports, static imports, import ordering, import grouping, and unused-import cleanup when there is no production or test behavior change.
- Do not report import-only, whitespace-only, or formatter-only changes as `Major Issues`, `Unrelated Changes`, or rollback requests by default.
- Do not set `Merge Verdict: Not Mergeable` solely because of import ordering, unused-import cleanup, whitespace normalization, or IDE/formatter cleanup.
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

- In the current reply, prioritize blocking issues and `Merge Verdict`.
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

Triage policy:

- Information complete: proceed with full review.
- Missing evidence: mark as "not mergeable" and request minimum additional info.
- Any substantive off-topic/unrelated changes: mark as "not mergeable" and require scope narrowing.
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
2. Root-cause modeling: reconstruct "trigger condition -> failure path -> result" from issue and code path.
3. Fix mapping: verify each change covers the root-cause chain, not just symptoms.
4. Risk scan:
   - Design: abstraction level, responsibility boundaries, temporary compatibility branches
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
7. Unrelated-change screening: identify substantive code/config/refactor changes not directly tied to PR goal; require removal or rollback.
   Ignore non-behavioral import-only, whitespace-only, and formatter-only churn for mergeability unless it meets the Non-Behavioral Churn Rule escalation conditions.
8. Version baseline control:
   - Base conclusion only on PR latest code version
   - If new commits are added, current conclusion becomes invalid and must be re-reviewed on latest version
9. Self-iteration gate: repeat internal review passes until the latest pass finds no new actionable findings.
10. Merge decision: output `Merge Verdict`.
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
   - Missed unrelated substantive changes.
   - Missed output-template or evidence gaps.
4. If the self-review finds any new actionable issue, add it to the candidate findings, deduplicate it against existing findings,
   update the merge verdict and next steps if needed, and repeat the loop.
5. Do not treat restatements, optional polish, speculative risks outside the PR scope, or already captured issues as new actionable findings.
6. Stop only when the latest self-review pass finds no new actionable issues compared with the accumulated candidate findings.
7. Do not expose intermediate review rounds, draft verdicts, or self-review transcripts to the user.
8. Produce one consolidated final review with exactly one `Merge Verdict`.

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

If the root-cause chain cannot be fully proven fixed, set `Merge Verdict: Not Mergeable`.

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

## Coverage Statement (Required in Every Review)

Each review must declare:

- `Reviewed Scope`: files/modules actually reviewed this round, plus the latest PR head SHA, local merge-base SHA when local git is used, and whether the local file list matched GitHub `/pulls/{number}/files`.
- `Not Reviewed Scope`: unreviewed or only superficially reviewed areas.
- `Need Expert Review`: whether domain reviewers are required (security, concurrency, performance, protocol, etc.).
- For SQL parser reviews, `Reviewed Scope` must explicitly name the target dialect, any related trunk / branch dialects checked, and the documentation pages / repo doc paths used to validate syntax behavior.

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
- Use Markdown headings (`### Decision`, `### Major Issues`, etc.) with a blank line before and after each heading.
- Keep the GitHub Markdown structure unchanged regardless of output language.
- Keep `Merge Verdict: ...` as a bold bullet near the top, and output exactly one verdict line.
- Keep stable review labels in English, such as `Merge Verdict`, `Reviewed Scope`, `Not Reviewed Scope`, and `Need Expert Review`,
  so they remain searchable and consistent.
- Do not include internal self-iteration rounds, draft verdicts, or self-review transcripts in the GitHub-facing review body.
- Use short unordered bullets under each heading; use bold inline labels such as `Symptom:`, `Risk:`, and `Action:` for issue details.
- Use repo-relative paths with line numbers, for example `infra/.../Foo.java:123`; do not use local absolute file paths in GitHub-facing review text.
- Prefer bullets over tables. Use tables only for compact status summaries that remain readable in GitHub's narrow review pane.
- Keep command evidence in inline code or short fenced blocks; avoid long raw JSON, full logs, or unrendered terminal transcripts.
- Before final output, perform a formatting self-check on the inner GitHub-facing review body:
  - The inner GitHub-facing review body is not wrapped in a code fence, blockquote, XML/HTML container, or transcript.
  - The inner GitHub-facing review body contains the required `###` headings for the selected verdict template.
  - The inner GitHub-facing review body contains exactly one bold `Merge Verdict: ...` line.
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

Use committer tone, gentle wording, no emojis; use this GitHub Markdown skeleton:

```markdown
### Decision

- **Merge Verdict: Not Mergeable**
- **Reviewed Scope:** ...
- **Not Reviewed Scope:** ...
- **Need Expert Review:** ...

### Positive Feedback

- Optional; omit this section when there is no genuinely correct direction-aligned change.

### Major Issues

- **[Severity] Short issue title** (`path/to/File.java:123`)
  - **Symptom:** ...
  - **Risk:** ...
  - **Action:** Please ...

### Newly Introduced Issues

- Include only when the latest PR revision introduces new defects or regression risks.

### Unrelated Changes

- Include only when substantive unrelated changes exist, and explicitly ask for rollback.

### Next Steps

- Provide an executable fix checklist.

### Multi-Round Comparison

- Include only when previous-round feedback exists in GitHub-visible PR comments, review threads, or change requests.

### Evidence Supplement

- Include only when information gaps block mergeability; list the minimum additional information required.
```

### B. Mergeable

Use this GitHub Markdown skeleton:

```markdown
### Decision

- **Merge Verdict: Mergeable**
- **Reviewed Scope:** ...
- **Not Reviewed Scope:** ...
- **Need Expert Review:** ...

### Basis

- Root-cause fix evidence.
- Risk assessment results proving no unresolved risk.

### Verification

- Reviewer-run local verification, if any.
- Test and compatibility evidence from the reviewed code.
- Do not include GitHub Actions / CI status unless explicitly requested.
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
