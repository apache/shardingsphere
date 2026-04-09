---
name: review-pr
description: >-
  Used to review whether an Apache ShardingSphere PR truly fixes the root cause,
  assess side effects and regression risks, and determine whether it can be safely merged.
  If not mergeable, produce committer-tone change request suggestions.
  Supports targeted comparison across multiple review rounds.
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
6. If unrelated changes exist, you must explicitly ask for rollback; if none exist, do not output that section.
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
14. Before outputting `Mergeable`, perform one explicit adversarial pass that assumes the PR is unsafe and actively searches for:
    - one cross-dialect or adjacent-feature regression path,
    - one config-disabled or feature-flag-off path,
    - one original symptom path that is only partially covered by tests.
    If any of these remain unresolved, set `Merge Verdict: Not Mergeable`.
15. If a PR changes SQL parser behavior, grammar, visitor logic, supported SQL cases, or parser tests for a dialect,
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

## Execution Boundary

- Review output only; do not modify code.

## Evidence Source Strategy

Priority from high to low:

1. PR facts: diff, commit history, review comments, CI status, check results.
2. Related issues in the same repo, relevant module code/tests, historical behavior (optional git blame/log).
3. ShardingSphere official docs and official repo conventions.
4. External authoritative specs only when necessary (official standards/docs only).

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
- Any off-topic/unrelated changes: mark as "not mergeable" and require scope narrowing.
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

1. Define target and boundary: restate PR goal, impacted modules, target topology (JDBC or Proxy, Standalone or Cluster).
2. Root-cause modeling: reconstruct "trigger condition -> failure path -> result" from issue and code path.
3. Fix mapping: verify each change covers the root-cause chain, not just symptoms.
4. Risk scan:
   - Design: abstraction level, responsibility boundaries, temporary compatibility branches
   - Performance: new loops/remote calls/object allocations on hot paths
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
7. Unrelated-change screening: identify code/format/refactor changes not directly tied to PR goal; require removal or rollback.
8. Version baseline control:
   - Base conclusion only on PR latest code version
   - If new commits are added, current conclusion becomes invalid and must be re-reviewed on latest version
9. Merge decision: output `Merge Verdict`.
10. Generate feedback: follow the output template below.

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
- Performance risk: complexity increase, extra hot-path allocations, unbounded retries, blocking I/O.
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

## Coverage Statement (Required in Every Review)

Each review must declare:

- `Reviewed Scope`: files/modules actually reviewed this round.
- `Not Reviewed Scope`: unreviewed or only superficially reviewed areas.
- `Need Expert Review`: whether domain reviewers are required (security, concurrency, performance, protocol, etc.).
- For SQL parser reviews, `Reviewed Scope` must explicitly name the target dialect, any related trunk / branch dialects checked, and the documentation pages / repo doc paths used to validate syntax behavior.

## Multi-Round Change Request Comparison Rules

When the user provides previous-round feedback or PR adds new commits, perform incremental comparison:

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

### A. Not Mergeable (Change Request)

Use committer tone, gentle wording, no emojis; structure:

1. Decision block
   - `Merge Verdict: Not Mergeable`
   - `Reviewed Scope / Not Reviewed Scope / Need Expert Review`
2. Positive feedback (optional)
   - Include only when there are genuinely correct direction-aligned changes.
   - Omit if none.
3. Major issues
   - List unreasonable/incorrect points by severity.
   - Each issue includes: label, symptom, risk, recommended action (fix or rollback).
4. Newly introduced issues
   - Point out defects/regression risks introduced by this PR.
   - Clearly require fix or rollback.
5. Unrelated changes (output only when present)
   - List changes unrelated to this PR goal and request rollback.
6. Next-step suggestions
   - Provide executable fix checklist and encourage next revision.
7. Multi-round comparison (only when history exists)
   - Versus previous round: closed, unresolved, and new items.
8. Evidence supplement (only when information is insufficient)
   - Explicitly list minimum additional information and review entry points.

### B. Mergeable

1. Decision block
   - `Merge Verdict: Mergeable`
   - `Reviewed Scope / Not Reviewed Scope / Need Expert Review`
2. Basis
   - Root-cause fix evidence.
   - Risk assessment results (proving no unresolved risk).
3. Pre-merge checks
   - CI, tests, compatibility confirmations.

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
- Do not ignore unrelated changes.
- Do not reuse old conclusions after new commits are added without re-review.
- Do not include emojis in change request text.
- Do not output `Mergeable` for a shared-code change unless you have checked at least one non-target dialect or feature that also uses the changed path.
- Do not output `Mergeable` when local verification omitted `-am` on a module-scoped Maven run and dependency freshness matters.
