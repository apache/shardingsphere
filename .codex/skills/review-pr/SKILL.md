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
9. In every review, run the `Hard Compliance Gate` checks and record command evidence with exit codes.
10. If any required check was not executed, output is missing, or evidence is incomplete, set `Merge Verdict: Not Mergeable` with reason `Evidence insufficient`.
11. Enforce `RULE-COD-062` from `CODE_OF_CONDUCT.md:62`: method parameters must not use `Optional`; pass plain values (nullable when needed).
12. If `RULE-COD-062` is violated in changed production/test Java files and no explicit user waiver exists, set `Merge Verdict: Not Mergeable`.
13. Any `RULE-COD-062` judgment must include file/line evidence and cite `CODE_OF_CONDUCT.md:62` in the review.

## Hard Compliance Gate (Non-negotiable)

Run these checks before merge decision:

1. Build changed Java file list from latest PR diff.
2. Execute Optional-parameter scan against changed Java files:
   - `rg -nP '\([^)]*\bOptional<[^>]+>\s+\w+[^)]*\)' <changed_java_files>`
3. Record command(s), exit code(s), and hit summary.
4. If there are no changed Java files, still record the file-list command and explicitly mark `No Java files changed`.

Recommended command template:

```bash
git diff --name-only <base>...<head> | rg '\.java$'
rg -nP '\([^)]*\bOptional<[^>]+>\s+\w+[^)]*\)' <changed_java_files>
```

Decision rules:

- Any hit for method parameter `Optional` -> `Compliance Gate: FAIL` -> `Merge Verdict: Not Mergeable` (unless user explicitly waives).
- Missing command execution or missing output evidence -> `Compliance Gate: FAIL` -> `Merge Verdict: Not Mergeable`.
- No hit with complete evidence -> `Compliance Gate: PASS`.

## Required Evidence Block (Mandatory Output)

Every review response MUST include a compact evidence block:

- `Compliance Gate: PASS/FAIL`
- `Rule ID: RULE-COD-062`
- `Rule Source: CODE_OF_CONDUCT.md:62`
- `Commands:` list each command exactly as executed
- `Exit Codes:` one-to-one mapping with commands
- `Matches:` file:line hits, or `none`, or `No Java files changed`

If any field above is missing, treat it as `Evidence insufficient` and set `Merge Verdict: Not Mergeable`.

## Execution Boundary

- Review output only; do not modify code.

## Evidence Source Strategy

Priority from high to low:

1. PR facts: diff, commit history, review comments, CI status, check results.
2. Related issues in the same repo, relevant module code/tests, historical behavior (optional git blame/log).
3. ShardingSphere official docs and official repo conventions.
4. External authoritative specs only when necessary (official standards/docs only).

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

## Review Workflow

0. Run `Hard Compliance Gate` first and collect command evidence (commands, exit codes, hit summary).
1. Define target and boundary: restate PR goal, impacted modules, target topology (JDBC or Proxy, Standalone or Cluster).
2. Root-cause modeling: reconstruct "trigger condition -> failure path -> result" from issue and code path.
3. Fix mapping: verify each change covers the root-cause chain, not just symptoms.
4. Risk scan:
   - Design: abstraction level, responsibility boundaries, temporary compatibility branches
   - Performance: new loops/remote calls/object allocations on hot paths
   - Compatibility: behavior/config/API-SPI/SQL dialect versions
   - Regression: similar statements, adjacent features, exception branches
5. Test adequacy:
   - Is there a failing case first or reproducible steps?
   - Are major branches, boundaries, and counterexamples covered?
   - Are tests mapped one-to-one with fix points?
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

If the root-cause chain cannot be fully proven fixed, set `Merge Verdict: Not Mergeable`.

## Risk Checklist (Must Cover)

- Design risk: broken layering, duplicated logic, bypassed SPI/metadata cache, implicit state.
- Performance risk: complexity increase, extra hot-path allocations, unbounded retries, blocking I/O.
- Compatibility risk:
  - Behavior compatibility
  - Config compatibility
  - API/SPI compatibility
  - SQL compatibility (database/version/dialect)
- Functional degradation risk: old-scenario regression, boundary input behavior changes, error-code/exception semantic drift.
- Operational risk: config migration complexity, gray-release and rollback complexity.
- Supply-chain risk: vulnerabilities, licenses, transitive dependency changes from new deps.

## Coverage Statement (Required in Every Review)

Each review must declare:

- `Reviewed Scope`: files/modules actually reviewed this round.
- `Not Reviewed Scope`: unreviewed or only superficially reviewed areas.
- `Need Expert Review`: whether domain reviewers are required (security, concurrency, performance, protocol, etc.).

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
   - `Compliance Gate: PASS/FAIL`
   - `Compliance Evidence: commands + exit codes + key matches`
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
   - `Compliance Gate: PASS`
   - `Compliance Evidence: commands + exit codes + key matches`
2. Basis
   - Root-cause fix evidence.
   - Risk assessment results (proving no unresolved risk).
   - Explicit statement that `RULE-COD-062` (`CODE_OF_CONDUCT.md:62`) has no violation in changed Java files.
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
- Do not ignore unrelated changes.
- Do not reuse old conclusions after new commits are added without re-review.
- Do not include emojis in change request text.
