---
name: review-pr
description: >-
  Used to review whether an Apache ShardingSphere PR truly fixes the root cause,
  assess side effects and regression risks, and determine whether it can be safely merged.
  If not mergeable, produce committer-tone change requests, or needs-discussion feedback when
  the PR direction, root-cause model, or problem framing should be reopened before implementation continues.
  Also use when asked to assess GitHub-visible PR discussion, review comments, author or maintainer pushback,
  challenged findings, or to draft copy-ready committer replies where review correctness, mergeability,
  change-request validity, or root-cause evidence is being judged.
  If review cannot be completed from available public evidence, produce a Review Incomplete result without patch-level advice.
  Supports targeted comparison across GitHub-visible review rounds when prior PR comments or review threads exist.
  Before final output, internally self-iterate the review until no new actionable findings are discovered.
---

# Review PR

## Objective

- Review ShardingSphere PRs and review-related PR discussions with a root-cause-first, evidence-first approach.
- Select the output mode from the user request:
  - `Formal Review Mode`: use when the user asks for a PR review, mergeability decision, or formal GitHub review body. Output exactly one `Review Result`.
  - `PR Discussion Reply Mode`: use when the user asks how to reply to PR comments, review threads, author or maintainer pushback, or challenged findings.
    Output a copy-ready committer reply draft by default, and do not force a formal `Review Result` unless the user asks for one.
- In `Formal Review Mode`, output exactly one `Review Result`:
  - `Mergeable`: public code, tests, documentation, and relevant verification support merge readiness.
  - `Not Mergeable`: public evidence confirms a blocker in the current PR scope.
  - `Review Incomplete`: the reviewer cannot make a reliable mergeability judgment because required public facts are unavailable, inaccessible, or not attributable.
- For `Not Mergeable`, choose exactly one feedback mode:
  - `Change Request`: the direction is valid, but the patch needs implementation, test, scope, compatibility, or evidence changes.
  - `Needs Discussion`: public evidence shows the PR direction, root-cause model, or problem framing must be reopened before implementation continues.
- For `Review Incomplete`, do not output patch-level change requests. State what was verified, what required public fact is missing, and what must be checked next.

## Trigger Scenarios

- The user asks you to review a PR.
- The user asks whether a PR "can be merged" or "fixes the root cause."
- The user asks you to generate committer-tone change request comments.
- The user asks whether a PR direction should be reconsidered or discussed before more patch work.
- The user asks how to reply to a PR comment, review thread, author or maintainer objection, or challenged review finding.

## Mandatory Constraints

Before applying the numbered review gates, enforce the public evidence boundary:

- Treat every GitHub-facing review body as community-visible output that may be copied directly to a public Apache PR or issue.
- Base community-visible output only on public PR / issue facts, public commits, public diff, public review threads, public documentation, repository code, and sanitized local verification summaries.
- Do not include non-public downstream project names, private or internal repository names, customer-specific or vendor-specific context, private chats,
  intermediate discussion results, prompt process, internal self-review notes, local-only archive details, or private migration background in community-visible output.
- Do not use non-public context as review evidence. If such context helps orient the reviewer privately, convert it into a public-evidence question
  and verify it against public artifacts before output.

1. Verify root-cause repair first; fallback logic, defaults, null checks, try-catch blocks, or swallowed errors cannot substitute for root-cause repair.
2. In `Formal Review Mode`, output exactly one `Review Result`. Choose one `Feedback Mode` only when the result is `Not Mergeable`.
3. Do not convert reviewer uncertainty, tool failure, inaccessible GitHub data, or missing local verification into a PR blocker.
   Use `Review Incomplete` in `Formal Review Mode`, or a clarification-style reply in `PR Discussion Reply Mode`, when required public facts cannot be checked or attributed.
4. In `Formal Review Mode`, if public evidence confirms a wrong root-cause model, problem framing, expected behavior, ownership boundary, or solution direction,
   use `Review Result: Not Mergeable` with `Feedback Mode: Needs Discussion`.
5. Review only the latest PR code version, and use GitHub PR metadata plus `/pulls/{number}/files` as the authoritative scope boundary.
6. Use repository-declared formatting/style gates as the formatting authority.
7. Treat substantive unrelated changes or substantive scope expansion as blockers; ignore non-behavioral import-only, whitespace-only, formatter-only, or IDE cleanup churn unless it hides behavior,
   fails declared gates, touches broad unrelated areas, or violates explicit scope rules.
8. Before considering `Mergeable`, apply all triggered hard gates and specialized review gates, including semantic compatibility, counterexamples, blast-radius/shared-layer ownership,
   linked-issue completeness, implicit-state review, high-frequency `computeIfAbsent` review, CI evidence judgment when relevant, and local verification freshness.
9. Before final output, complete the `Pre-Publication Finding Audit`; do not expose intermediate findings, and output one consolidated review.

## Evidence Sufficiency and CI Judgment

- `Not Mergeable` requires confirmed public evidence in the reviewed scope. A blocker must be supported by at least one of:
  - code, diff, or contract evidence that proves a defect or scope violation;
  - relevant test, CI, check-run, log, or reproduction evidence that proves failure;
  - a required core behavior validation gap after the reviewer has checked available public facts;
  - compatibility, API/SPI, protocol, data, security, lifecycle, dependency, or distribution risk with a clear executable path;
  - confirmed scope, ownership, documentation, release-note, or migration impact that the PR must resolve.
- `Review Incomplete` is required when mergeability depends on public facts that are unavailable, inaccessible, stale, or not attributable to the PR.
- `Review Incomplete` is not a soft approval and must not contain patch-level code requests. If a code issue is already confirmed, use `Not Mergeable`.
- CI success never replaces code review, root-cause review, scope review, or test adequacy review.
- Relevant CI failure means the PR cannot be `Mergeable`. If the failure is attributable to the PR, use `Not Mergeable`; if attribution is unclear, use `Review Incomplete`.
- Inspect CI, check-runs, or workflow logs when the PR goal, linked issue, author/user statement, generated artifact, native image, E2E, test-infra, or a candidate blocker depends on runtime verification.
- Do not wait for or query CI when code, docs, or static evidence is sufficient for the current review result. State the reason in `Verification` when CI was not reviewed.

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
- If target-tool evidence is unavailable and mergeability or reply correctness depends on it, use `Review Result: Review Incomplete` in `Formal Review Mode`,
  or draft a clarification-style reply in `PR Discussion Reply Mode`; do not emit or preserve a blocker.
- Emit `Not Mergeable`, or a firm copy-ready assertion that asks the author to change the PR, only when target-tool public evidence or a public reproduction
  proves the changed command flow is unreliable in the PR's documented environment.

## Mergeable Hard Gates

Apply these gates before considering `Review Result: Mergeable`.
If a gate is not applicable to the PR, state the reason briefly in the review evidence or details.
Do not turn speculative risks, personal style preferences, or out-of-scope polish into merge blockers.

1. Root cause: the PR repairs the true trigger point or required propagation path, not only the final error point. Fallbacks, null checks, defaults, try-catch blocks, or swallowed errors do not substitute for root-cause repair.
2. Linked issue completeness: every claimed fixed/closed/addressed issue requirement maps to code and validation, or unresolved scope becomes `Not Mergeable` or `Review Incomplete` under the evidence rules.
3. Scope and ownership: no substantive unrelated changes, scope expansion, target-specific leakage into shared code, or ownership bypass remains.
4. Regression and side effects: no unresolved functional degradation, compatibility risk, performance risk, config/API/SPI risk, dialect risk, feature-disabled path risk, or adjacent-feature risk remains.
5. Test adequacy: changed behavior has meaningful direct or existing coverage of the root-cause path and important boundaries. Do not require coverage-rate proof; high-cost environment proof may be delegated only when lower-level evidence proves the current PR path.
6. Code quality: no concrete maintainability blocker remains, such as unclear responsibility, duplicated logic, dead code, over-complex control flow, hidden state, magic values, or hard-to-read temporary design.
7. Architecture: shared modules, public/shared APIs, SPI contracts, metadata, rule owners, dialect owners, session/executor state, and lifecycle state preserve explicit ownership and contracts.
8. Release note and user docs: required entries are present when users need upgrade, troubleshooting, configuration, compatibility, migration, rollback, or release-awareness guidance; low-signal entries are not required for internal/test-only fixes.
9. Breaking change and migration: default behavior, config keys, API/SPI contracts, protocols, metadata storage, SQL semantics, and released artifacts have clear compatibility, migration, upgrade, and rollback evidence when touched.
10. Diagnostics: changed errors, logs, or diagnostic output remain accurate, actionable, and safe.
11. Dependency and distribution: manifests, lockfiles, packaging, native-image metadata, LICENSE, NOTICE, and release artifacts are checked for security, license, compatibility, packaging, and release impact when touched.
12. Local verification freshness: reviewer-run verification, when used to support mergeability, must exercise the latest PR head or a clearly identified current-head artifact.
    Do not require Maven `-am` when IDE/MCP current-source runs, current-head installs, CI artifacts,
    or an explicit `-pl` module set already prove freshness for the reviewed path.

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
- Do not base the review result solely on GitHub Actions, CI status, or check-run completion.
- Inspect CI when it is relevant under `Evidence Sufficiency and CI Judgment`; otherwise do not wait for CI just for formality.
- Use the repository-declared formatting and style gates as authority. For ShardingSphere, Spotless and Checkstyle are the formatting/style gates.
- Do not treat `git diff --check` as a blocker when it conflicts with Spotless/Checkstyle behavior, unless the user explicitly asks for that check.

## Non-Behavioral Churn Rule

- Still include import-only, whitespace-only, and formatter-only files in `Reviewed Scope` when GitHub `/pulls/{number}/files` includes them.
- `import-only` includes normal imports, static imports, import ordering, import grouping, and unused-import cleanup when there is no production or test behavior change.
- Do not report import-only, whitespace-only, or formatter-only changes as `Issues`, `Unrelated Changes`, or rollback requests by default.
- Do not set `Review Result: Not Mergeable` solely because of import ordering, unused-import cleanup, whitespace normalization, or IDE/formatter cleanup.
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

## GitHub Access Strategy

Use authenticated GitHub evidence before treating PR facts as unavailable:

1. Prefer the GitHub connector or app tools when available.
2. Otherwise use `gh` when it is installed and has either an existing login or an available environment token.
3. If `gh` is unavailable, check whether a GitHub token environment variable exists without printing its value. When one exists, use authenticated REST requests with an `Authorization: Bearer <token>` header and `Accept: application/vnd.github+json`.
4. Use anonymous GitHub API or HTML only after authenticated connector, `gh`, or token-backed REST access is unavailable or fails.

Safety and completeness rules:

- Never print token values, auth headers, credential files, raw environment dumps, verbose curl traces, or token-bearing commands.
- Do not treat an anonymous `404` as proof that PR facts are unavailable until authenticated access has been tried or shown unavailable.
- Fetch all pages for `/pulls/{number}/files`, commits, issue comments, review comments, reviews, and other paginated endpoints. With `gh`, use pagination support; with REST, follow the `Link` header.
- Classify access by endpoint. For example, PR metadata and files may be available while checks or workflow logs return `403`.
- A `403` or `404` on checks, workflow logs, or another secondary endpoint blocks mergeability only when that endpoint is required under `Evidence Sufficiency and CI Judgment`; otherwise record the inaccessible endpoint in `Review Details`.
- Do not include authentication method, token variable names, temporary API files, raw response bodies, or private access diagnostics in GitHub-facing review output.

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

CI status and check-runs are review evidence only when relevant under `Evidence Sufficiency and CI Judgment`.

For SQL parser reviews:

- If SQL grammar, visitors, parser tests, SQL syntax docs, dialect parser behavior, or parser-generated baselines are touched, read `references/sql-parser-review.md` before reviewing.
- Apply the official-documentation, dialect-family, docs/example, and parser-baseline rules from that reference.

Forbidden sources:

- Unverifiable blogs, forum posts, or AI-reposted content.

## Local Verification Freshness Strategy

Before choosing a local verification command, decide which evidence is needed for the latest PR head:

1. Prefer IDE/MCP runs for focused module tests, inspections, Proxy startup, or run configurations when available and appropriate,
   because they compile and run current project sources without relying on stale local Maven artifacts.
2. Prefer precise Maven module scopes over reactor expansion: identify changed modules, affected test modules, and runtime entry modules,
   then use an explicit `-pl <moduleA>,<moduleB>` set when that covers the reviewed path.
3. When reactor participation is still needed, keep it to one current-head freshness gate before final mergeability judgment,
   except when the first run fails and the PR head changes after a fix.
4. Use Maven `-am` only when explicit module selection cannot prove dependency freshness, a required upstream module is missing from local artifacts,
   or CI-equivalent reactor behavior is itself the evidence.
5. For multi-module verification, prefer a bottom-up order: validate changed lower-level modules first,
   then higher-level adapter or runtime modules that consume them. Stop on the first relevant failure and update the review evidence before widening scope.
6. Record the freshness reason in `Verification`, such as `IDE/MCP current project sources`, `explicit -pl module set covers changed and consuming modules`,
   `current-head install already performed`, or `Maven -am used because dependency freshness was otherwise uncertain`.

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
- If full review cannot be completed immediately, provide only confirmed high-risk blockers; use `Review Incomplete` when required public facts are still missing.

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
  Ignore non-behavioral import-only, whitespace-only, and formatter-only churn for mergeability unless it meets the Non-Behavioral Churn Rule escalation conditions.
- Change set too large: request split first, and provide only blocker-level feedback for current version.

## Minimum Required Information

When information gaps prevent a reliable review result:

- Request only facts required by the unresolved gate; do not ask for a fixed checklist by default.
- Do not ask the author for evidence the reviewer can obtain from public PR, issue, code, CI, or workflow data.
- Map every requested item to the unresolved review gate.
- Common requests include latest changed-file scope, runtime topology, database type/version, minimal reproducible input, key logs, targeted test evidence, docs/release impact, or SQL parser official documentation.

## Review Workflow

CI/check-run review is not a substitute for code review. Query and report CI only when it is relevant under `Evidence Sufficiency and CI Judgment`.

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
   - For SQL parser changes, read and apply `references/sql-parser-review.md`
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
   - For SQL parser family scans, use `references/sql-parser-review.md` to check validation or non-applicability evidence
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
12. Pre-publication finding audit: verify every blocker against the evidence threshold before output.
13. Review result: output exactly one `Review Result`.
14. Generate feedback: follow the output template below.

## Pre-Publication Finding Audit

Before producing the final review, build and freeze an internal review inventory for the latest PR head.
Do not expose intermediate findings, draft issue lists, or candidate blockers unless the user explicitly asks for status or early high-risk blockers.

The inventory must cover:

- Authoritative scope: latest head SHA, base ref/SHA, merge-base, local file list, and GitHub `/pulls/{number}/files` match status when available.
- Changed file categories, entry points, execution paths, public/shared APIs, stateful registries or lifecycle handles, tests, docs, release notes, generated resources, dependencies, and distribution impact.
- Boundary cases relevant to the change: empty, null, invalid, stale, repeated, disabled, fallback, cleanup, release/free, and error paths.
- Latest-commit delta risks when previous public feedback or review rounds exist.

For each candidate finding, record internally:

- Evidence type: code/diff, test/CI/log, linked issue, docs/spec, generated artifact, or local verification.
- Evidence path, line, command, CI run, or public anchor.
- Whether the finding is caused by this PR, pre-existing on base, exposed by this PR, newly introduced by latest commits, or newly discovered but present earlier.
- Minimum independent fix boundary and duplicate relationship.
- Classification: confirmed blocker, review-incomplete gap, non-blocking observation, or out-of-scope note.

Before any candidate enters `### Issues`, verify:

- The evidence type matches the claimed root cause and linked issue.
- No public counter-evidence invalidates the claim.
- If the claim depends on third-party tool behavior, the `Third-Party Tool Behavior Evidence Gate` has been satisfied.
- Reviewer uncertainty, skipped local verification, unavailable tools, or inaccessible GitHub data are not being converted into a PR blocker.
- The requested action is necessary in the current PR scope.
- The blocker satisfies `Evidence Sufficiency and CI Judgment`.

Run an adversarial pass on the latest head that looks for missed root-cause gaps, side effects, feature-disabled paths, adjacent-feature regressions, ownership issues, release/doc impacts, and required verification gaps.
If the pass finds any new actionable finding with an independent fix boundary, add it to the inventory, deduplicate and classify it, update the review result if needed, and repeat the pass.
Stop only after one full adversarial pass finds no new actionable finding.
If the inventory cannot be completed because required public evidence is unavailable or unattributable, output `Review Result: Review Incomplete`.
Produce one consolidated review with exactly one `Review Result`.

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

If public evidence proves the root-cause chain is not fixed, set `Review Result: Not Mergeable`.
If the root-cause chain depends on required public facts that are unavailable or unattributable, set `Review Result: Review Incomplete`.

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
- What evidence type would prove the issue's root cause still exists, such as generated metadata, protocol trace, SQL parser official syntax, runtime log, config output, or changed code path?
- Does the review claim use that evidence type, or is it inferring from an adjacent fact that does not prove the root cause?
- If the issue expectation, compatibility boundary, or ownership boundary is itself contradicted or unresolved in public evidence,
  should the PR pause implementation and reopen discussion before patch-level requests continue?

If public evidence proves that a required issue behavior is only partially fixed or unvalidated after reasonable public verification,
set `Review Result: Not Mergeable` and list the minimum missing implementation or evidence.
If the linked issue cannot be read, issue scope is ambiguous, or required public evidence is unavailable or unattributable,
set `Review Result: Review Incomplete`.
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

If target-specific semantics leak into shared code, or if implicit state is used as a mode switch in new/changed shared APIs, set `Review Result: Not Mergeable`.

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
- Operational risk: config migration complexity, staged rollout and rollback complexity.
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
- `Verification`: reviewer-run commands and exit codes, or a short reason why local verification was not run. Also state any relevant GitHub endpoint that was inaccessible and whether that gap affects mergeability.
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

## Challenged Finding Correction Rules

Apply this section whenever an author, maintainer, or user challenges, contradicts, or disproves a prior finding or review result.

1. Treat the prior finding as a hypothesis to disprove, not as a conclusion to defend.
2. Rebuild the evidence chain from public facts and identify every assumption required for the finding to remain true.
3. Inspect challenger-provided public evidence first, including linked source PRs, version-specific behavior, reproduction notes, CI logs, and implementation details.
4. Use private or off-platform discussion only as an orientation signal. Convert it into public-evidence questions or verify it against public artifacts before GitHub-facing output.
5. If any required assumption is disproved or lacks evidence, withdraw the blocker or change it to `Review Incomplete`; do not narrow the wording while preserving the same unsupported request.
6. If the prior finding remains valid, restate the target-tool, code, log, or reproduction evidence that directly proves it, and address the counter-evidence explicitly.
7. Use the `Correction` output structure for formal GitHub review follow-up. Set `Current Status` to `Withdrawn`, `Retained`, or `Changed to Review Incomplete`.
8. Do not publish another `Not Mergeable` result after a challenged finding until the `Pre-Publication Finding Audit` and all triggered evidence gates have been rerun.

## PR Discussion Reply Mode

Use this mode when the user asks for a committer reply to a PR comment, review thread, author or maintainer objection, or challenged review finding.

- Apply the same evidence gates used by formal reviews before making any firm public assertion.
- Default to a copy-ready GitHub reply draft, not a formal `Review Result`.
- In Codex chat, explain the reasoning in the user's language, but draft GitHub-facing replies in English unless the user requests another language.
- Keep private chats, private reproduction details, prompt process, and local-only analysis out of the copy-ready reply.
  Use them only to guide public re-verification or to ask for public evidence.
- If the evidence gate is closed, provide a concise reply that states whether to retain, withdraw, or revise the finding.
- If evidence is incomplete or conflicting, draft a clarification-style reply instead of an assertive change request.
- Do not ask the author for evidence the reviewer can obtain from public PR, issue, code, CI, workflow data, target-tool source, or official docs.
- Protect committer credibility: do not provide a strong public claim, blame, or merge-blocking request unless the evidence chain is closed.

## Output Structure

In `Formal Review Mode`, GitHub-facing review bodies must be GitHub Markdown, use the user's language unless requested otherwise,
and contain exactly one bold `Review Result: ...` line under `### Summary`.
Do not wrap GitHub-facing text in a code fence, blockquote, XML/HTML container, or transcript.
Use stable English labels: `Review Result`, `Feedback Mode`, `Reason`, `Reviewed Scope`, `Not Reviewed Scope`, `Verification`, and `Release Note / User Docs`.
Use repo-relative file references with line numbers, public anchors for non-file evidence, and sanitized verification summaries.
Do not include internal drafts, self-review notes, private context, local absolute paths, temp paths, tokens, or raw long logs.

When returning a formal review in Codex chat for the user to copy, wrap only the GitHub-facing body in a fenced `markdown` block.
When posting directly through a tool, submit only the inner GitHub-facing body.

Use these required structures:

- `Mergeable`: `### Summary` with `Review Result` and `Reason`; `### Evidence`; `### Review Details`.
- `Not Mergeable`: `### Summary` with `Review Result`, exactly one `Feedback Mode`, and `Reason`; `### Issues`; `### Review Details`.
- `Review Incomplete`: `### Summary` with `Review Result` and `Reason`; `### Incomplete Reason`; `### Verified Facts`; `### Required Evidence`; `### Review Details`.
- `Correction`: prepend `### Correction` with `Previous Finding`, `Current Status` (`Retained`, `Withdrawn`, or `Changed to Review Incomplete`), and `Reason`;
  then output the applicable current result structure with exactly one bold `Review Result` line under `### Summary`.

For `Not Mergeable`, choose `Feedback Mode: Change Request` for patch-level required changes or `Feedback Mode: Needs Discussion` when the current direction, root-cause model, problem framing, expected behavior, or ownership boundary must be reopened.
Do not include patch-level `Required Change` bullets after choosing `Needs Discussion`.
Use `P0`, `P1`, or `P2` issue severity, and include `Problem`, `Impact`, and either `Required Change` or `Discussion Needed`.
Use `status: need more info` instead of `type: discussion` only when missing public evidence blocks root-cause or scope classification.
Optional `Not Mergeable` sections are `Positive Feedback`, `Unrelated Changes`, `Next Steps`, and `Multi-Round Comparison`; omit placeholder headings.
`Review Incomplete` must not contain patch-level code suggestions. If evidence already supports a concrete code change, use `Review Result: Not Mergeable`.

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

- Do not output `Review Result: Mergeable` when evidence is insufficient, risks are unclear, or relevant CI is failing.
- Do not output `Review Result: Not Mergeable` for reviewer uncertainty, inaccessible public facts, unavailable tools, or skipped local verification; use `Review Incomplete` when those facts are required.
- Do not use "fallback logic passes tests" to replace proof of root-cause repair.
- Do not treat fixture-injected or mocked-path tests as full end-to-end proof without explicitly stating the gap.
- Do not output `Review Result: Mergeable` only because previous-round blockers were closed; always do one fresh-pass semantic and regression scan on the latest head.
- Do not ignore substantive unrelated changes.
- Do not reuse old conclusions after new commits are added without re-review.
- Do not include emojis in review feedback text.
- Do not use CI success as the sole reason for `Mergeable`.
- Do not ignore CI/check-run evidence when it is relevant under `Evidence Sufficiency and CI Judgment`.
- Do not include non-public downstream project names, private/internal repository names, customer-specific or vendor-specific context, private chats, prompt process, internal archive details,
  private migration background, intermediate discussion results, local absolute paths, or unsanitized temporary file names in GitHub-facing review output.
- Do not output patch-level `Required Change` requests after selecting `Feedback Mode: Needs Discussion`.
- Do not output `Review Result: Mergeable` when a required hard gate remains unresolved, including missing required test evidence, release notes, user docs, migration guidance, or diagnostic quality evidence.
- Do not output `Review Result: Mergeable` for a shared-code change unless you have checked at least one non-target dialect or feature that also uses the changed path.
- Do not output `Review Result: Mergeable` when local verification used stale artifacts, freshness-unclear module-scoped Maven output,
  or an incomplete explicit module set while dependency freshness matters.
- Do not output `Review Result: Mergeable` when Proxy/JDBC DML/DQL high-frequency SQL paths directly call `ConcurrentHashMap#computeIfAbsent` without a preceding `get` miss check.
