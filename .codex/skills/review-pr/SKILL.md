---
name: review-pr
description: >-
  Review Apache ShardingSphere or user-authorized downstream pull requests and PR
  discussions from public or authorized repository evidence. Use for code-correctness or
  mergeability decisions, CI-focused review, root-cause and regression analysis, complete
  consolidated findings, copy-ready committer feedback, challenged findings, multi-round
  review, and the repository completion loop's Local Candidate Preflight Mode.
---

# Review PR

## Purpose and Modes

Judge the latest reviewed scope from root cause, behavior, contracts, tests, and
public or user-authorized repository evidence. Select one output mode:

- `Formal Review Mode`: return one formal result for a PR review, code-readiness
  judgment, mergeability decision, or CI review.
- `PR Discussion Reply Mode`: return a copy-ready committer reply for a review
  thread, author or maintainer objection, or challenged finding. Do not add a
  formal verdict unless requested.
- `Local Candidate Preflight Mode`: review an authorized local implementation
  targeting an existing PR and return findings to the repository completion
  loop. Do not describe local-only work as the public PR state.

## Review Focus

Review focus is independent from output mode.

| Focus | Use when | CI behavior |
|---|---|---|
| `Code Correctness Review` | Default review of code, tests, behavior, scope, or regression risk | Do not query, wait for, or report GitHub Actions, checks, workflow runs, or Actions logs |
| `Mergeability Review` | The user asks whether the PR can be merged, approved, or landed | Review code and required CI or checks |
| `CI Review` | The user asks about checks, Actions, logs, or CI failures | Treat CI evidence as the primary target |

Explicit user scope wins. Local Candidate Preflight uses `Code Correctness
Review` unless the user explicitly requests CI.

In Code Correctness Review, unreviewed CI is not an evidence gap. Runtime facts
may still be required from code, official specifications, public reproductions,
or local verification. If such a decisive fact is unavailable, identify that
fact—not CI—as the incomplete reason.

## Canonical Assessment

Resolve one review basis before discovery: the effective candidate, applicable
requirements, selected review focus, and admissible evidence. Run the Review
Workflow against that basis and produce one mode-independent assessment:
confirmed findings consolidated by fix boundary, needs-discussion conditions,
incomplete-evidence gaps, and Completion Gate state.

Output mode must not affect candidate discovery, proof, classification,
coverage, or convergence. Never use a previous Local or Formal result as
evidence or as a conclusion to match. Treat previous public findings only as
hypotheses whose cited facts must be reverified.

Two reviews with the same effective candidate, requirements, focus, and
evidence must produce the same canonical assessment. Local and Formal modes may
resolve different candidates and render different status labels, but they must
not apply different code-correctness judgment. A changed focus, requirement,
or external fact changes the review basis and may legitimately change the
assessment. Mergeability or CI evidence may add external-state findings or
gaps, but it must not change code-correctness findings derived from an otherwise
unchanged basis.

## Core Contracts

1. Review only. Do not modify PR code, post comments, submit reviews, resolve
   threads, rerun workflows, or change remote state without explicit authority.
2. Formal review scope is the latest target PR head and the complete GitHub
   changed-file list. Use local triple-dot semantics when reproducing it. A
   discussion reply starts from the latest head, thread context, and affected
   behavior; expand to complete scope only when the claim depends on it.
3. Public community conclusions use only public evidence and sanitized
   verification summaries. Private-repository conclusions may use authorized
   repository evidence but must remain within the user-authorized task and
   target repository.
4. Reconstruct `trigger -> failing path -> observed result -> expected
   behavior` before judging the patch. A fallback, default, null check,
   try-catch, or swallowed error is not a root-cause repair unless it fixes the
   owning contract.
5. Treat every concern as a candidate until it passes the Finding Proof Gate.
6. Do not turn uncertainty, inaccessible evidence, tool failure, skipped
   verification, or uninspected counter-evidence into a blocker.
7. In Formal Review and Local Candidate Preflight, do not select a verdict,
   stop at the first blocker, or publish findings before the Completion Gate.
   Consolidate findings by independent fix boundary and return the complete
   current-head set once. Only an explicit request for status, narrow review, or
   early high-risk blockers authorizes a partial result.
8. Follow `AGENTS.md` for repository authority, command execution, local
   verification, sensitive data, and completion-loop rules.

## Scope and Evidence

For formal reviews:

- Resolve PR metadata, latest head SHA, base ref and SHA, authoritative GitHub
  file list, linked issue scope, public comments, and relevant reviews.
- Fetch every required page. When local Git is used, record the merge-base and
  whether the local file list matches GitHub.
- Review latest deltas after new commits; earlier findings never establish
  current-head readiness.
- Prefer PR facts, same-repository issues, code and tests, ShardingSphere
  documentation and conventions, then external official specifications.

For Local Candidate Preflight targeting an existing PR, resolve the same public
requirements and code-correctness evidence before applying the authorized local
delta. Record any explicit local requirement that extends the public PR scope
as a distinct part of the review basis.

For discussion replies, establish the latest public head, complete thread
context, relevant earlier review, and affected production or test paths. Fetch
the complete file list when scope is disputed or the reply changes an overall
readiness conclusion.

Before the first GitHub request, read and complete `GitHub Access Preflight` in
[evidence-access.md](references/evidence-access.md). Do not invoke a browser,
search, connector, `gh`, or anonymous HTTP route before the preflight selects
the read route. Read the remaining reference whenever CI, Actions, or
third-party behavior evidence is required.

AI-assistance disclosure is a mergeability concern, not a code-correctness
signal. In Mergeability Review or an explicit policy-compliance review, apply
`AI_POLICY.md` only when public PR evidence explicitly establishes material AI
assistance. Verify that the PR description names the tool and affected files or
scope. Never infer AI use from code, prose style, metadata, or an automated
classifier; without explicit public evidence, missing disclosure is neither a
finding nor an evidence gap.

## Finding Proof Gate

A candidate may become a blocking issue only when all five conditions hold:

1. `Evidence`: current code, diff, contract, test, log, CI, public reproduction,
   official documentation, or generated artifact directly supports the claim.
2. `Full path`: trace the relevant production or test entry path end to end;
   inspect setup, wrappers, earlier calls, generators, and consuming runtime.
3. `Counter-evidence`: check the strongest evidence that could disprove the
   finding, especially author or maintainer replies and version-specific facts.
4. `Necessity`: the requested change is required for safety or correctness in
   the selected focus, not merely cleaner or preferable.
5. `Scope`: this PR causes the problem, exposes it through behavior it owns, or
   must address it to satisfy the linked issue.

Classify failed candidates as an incomplete-evidence gap, non-blocking
observation, clarification question, pre-existing issue, or no issue. Do not
publish non-blocking observations unless they materially help the user.

## Behavior Clusters and Risk Triage

Before deep review, group the scope into the smallest independently meaningful
behavior changes. A behavior cluster may cross several files, and one file may
belong to several clusters. Map every substantive changed file to at least one
cluster; treat churn-only files explicitly rather than silently dropping them.

For every cluster, identify its root cause, behavior owner, entry paths,
callers or consumers, contracts, changed conditions or state transitions, and
validation points. Then consider every risk axis and deepen only the triggered
ones:

- Root cause and linked-issue completeness.
- Functional behavior, important boundaries, disabled paths, adjacent features,
  and old-scenario regression.
- Ownership, module boundaries, public APIs and SPIs, metadata, implicit state,
  lifecycle, and shared-code blast radius.
- Configuration, protocol, SQL and dialect semantics, supported versions,
  compatibility, migration, and rollback.
- Test validity and meaningful coverage of the owning production path.
- Concurrency, performance, allocation, I/O, memory, and resource cleanup.
- Diagnostics, security, user documentation, and release-note necessity.
- Dependencies, licenses, packaging, native metadata, generated resources, and
  distribution impact.

This triage is mandatory even when no risk is ultimately found. Read only the
triggered sections in
[high-risk-review.md](references/high-risk-review.md). For SQL grammar,
visitors, parser tests, syntax documentation, dialect behavior, or parser
baselines, also read
[sql-parser-review.md](references/sql-parser-review.md).

## Review Workflow

Apply this workflow to the canonical review basis without using output mode or
a previous result to influence the assessment:

1. Establish the authoritative effective-candidate scope and applicable
   requirements.
2. Confirm the selected review focus and admissible evidence.
3. Build behavior clusters and complete the mandatory risk triage.
4. Discover candidates across the complete scope before classifying the
   assessment.
   Use three distinct lenses:
   - `Root Cause and Behavior`: intended fix, changed decisions, boundaries,
     disabled paths, adjacent cases, and old-scenario regression.
   - `Blast Radius and Contracts`: callers, consumers, shared state, public
     contracts, compatibility, dependencies, packaging, and generated outputs.
   - `Tests, Runtime, and Operations`: realistic regressions that can still
     pass, error and lifecycle paths, runtime verification, diagnostics,
     documentation, rollout, and rollback.
5. Apply the Finding Proof Gate to every candidate. Keep discovery notes
   private and classify every candidate before publication.
6. Consolidate confirmed findings by independent fix boundary and identify any
   evidence or coverage gap that could still change the blocker set.
7. Review the latest delta and run a full-scope convergence pass after the most
   recent candidate change. If it finds a new independent candidate, return to
   step 5 and repeat. Freeze the canonical assessment only after the Completion
   Gate evaluation, then map it to the selected mode's status.

If the scope cannot be reviewed honestly, return the mode-appropriate incomplete
result or request a split. Do not produce a complete verdict from a partial
review.

## Completion Gate and Scripts

Apply the Completion Gate to every Formal Review and Local Candidate Preflight.
Apply it to a discussion reply only when the reply makes or changes an overall
readiness conclusion.

- Complete the behavior-cluster mapping and risk triage for every authoritative
  file; classify churn-only files explicitly.
- Finish all three discovery lenses and classify every candidate.
- Leave no unresolved evidence or coverage gap that could change the blocker
  set.
- Require a full-scope convergence pass after the latest candidate change with
  zero new independent candidates.
- Use `scripts/build_review_inventory.py --format json` when local refs are
  available. Its Markdown output is a bounded human summary, not the
  authoritative file list.
- Use `scripts/review_ledger.py` for multi-file, high-risk, or otherwise
  omission-prone review. It mechanically accounts for files, clusters, risk
  axes, finding classifications, proof fields, and review passes; it does not
  judge semantic correctness.
- Mutate one ledger sequentially; do not run ledger commands concurrently.
- Keep temporary ledger data private and remove only the exact ledger created
  for the current review.

If the gate cannot pass, return the mode-appropriate incomplete result. In
Formal Review, when confirmed blockers coexist with a gap that could hide more
blockers, list them as confirmed partial facts but do not present them as the
complete change-request set.

## Formal Decision Contract

Map the canonical assessment to Formal Review only after the Completion Gate
evaluation:

1. If the gate fails, use `Review Incomplete`, even when some blockers are
   already confirmed.
2. If public evidence disproves the problem model, expected behavior, ownership,
   protocol or SQL semantics, compatibility assumption, or solution direction,
   use `Not Mergeable` with `Feedback Mode: Needs Discussion`.
3. If at least one candidate passes the Finding Proof Gate, use `Not Mergeable`
   with `Feedback Mode: Change Request`.
4. Otherwise use `Mergeable` for the selected focus.

`Mergeable` in Code Correctness Review means code-scope readiness only. Required
pending CI prevents Mergeable in Mergeability Review. A relevant CI failure is
a blocker when attributable to the PR and an incomplete gap when attribution is
unclear.

## Local Candidate Preflight Mode

- Use the latest public PR head plus authorized local commits, index changes,
  and working-tree changes as the effective candidate.
- Verify with read-only Git that local `HEAD` equals or descends from the public
  head. Otherwise return `Local Preflight Result: Incomplete`.
- Review from the public PR merge-base through the working tree. Scope is the
  union of GitHub files and the authorized local delta; exclude unrelated local
  changes.
- Apply Code Correctness Review through the canonical assessment, including the
  same proof and completion gates, triggered high-risk criteria, and
  convergence loop.
- Map a failed Completion Gate or needs-discussion condition to `Local Preflight
  Result: Incomplete`, confirmed findings to `Local Preflight Result: Changes
  Required`, and a complete assessment with neither to `Local Preflight Result:
  Pass`.
- Keep this Skill review-only. The active implementation loop fixes safe
  in-scope findings and reruns preflight; scope expansion, architecture choices,
  and high-risk actions return to their existing authorization gates.

## Multi-Round and Challenged Findings

Read [review-corrections.md](references/review-corrections.md) when previous
public feedback exists, new commits address earlier findings, or any finding is
challenged.

Always re-evaluate the latest head. Treat a prior finding as a hypothesis to
disprove, inspect challenger-provided public evidence first, and withdraw or
downgrade any unsupported blocker rather than defending it. Classify every
finding first reported after an earlier formal review as introduced by the
latest commits, exposed by the previous fix, or missed in the previous review.

## Output Contract

Every result returned by this Skill must be exactly one fenced `markdown` block
with no prose before or after it. The first non-empty line must be
```` ```markdown ```` and the last non-empty line must be ```` ``` ````.

Use the user's language for formal results. Draft GitHub-facing discussion
replies in English unless the user requests another language. Use stable labels,
repository-relative file references with line numbers, public anchors, and
sanitized command summaries. Never include internal drafts, reasoning traces,
private context, local absolute paths, temporary paths, credentials, raw long
logs, or emojis.

### Formal Review

- `Mergeable`: `### Result` with exactly one bold
  `Review Result: Mergeable` line and a concise reason; `### Evidence`;
  `### Coverage`.
- `Not Mergeable`: `### Result` with exactly one bold
  `Review Result: Not Mergeable` line, one `Feedback Mode`, a bold
  `Blocking Issues: N` line, and a concise reason; `### Blocking Issues`;
  `### Coverage`.
- `Review Incomplete`: `### Result` with exactly one bold
  `Review Result: Review Incomplete` line and a concise reason;
  `### Confirmed Issues` when any are already proven; `### Verified Facts`;
  `### Required Evidence`; `### Coverage`.

For each blocking issue include:

- `Evidence`: current public or sanitized verification anchor.
- `Impact`: concrete failing behavior or contract.
- `Required Change` for Change Request, or `Discussion Needed` for Needs
  Discussion.

Do not add patch-level changes after selecting Needs Discussion. Do not include
placeholder headings. In `### Coverage`, report the reviewed head, authoritative
requirements and files accounted for, behavior clusters, completed discovery
lenses, unresolved gaps, and CI scope. In Code Correctness Review, state that
the result is code-scope only and CI was not reviewed.

### PR Discussion Reply

Return only the copy-ready reply in the fenced Markdown block. State whether the
finding is retained, withdrawn, or needs clarification, then give the public
evidence and minimum next action. Do not force a formal verdict.

### Local Candidate Preflight

Return `### Local Preflight`, exactly one bold Local Preflight Result line,
confirmed required findings or needs-discussion conditions when present, and
`### Coverage`. Identify the effective candidate, applicable requirements,
review focus, and unresolved gaps in Coverage.

### Correction

Begin with `### Correction`, then `Previous Finding`, `Current Status`
(`Retained`, `Withdrawn`, or `Changed to Review Incomplete`), and `Reason`.
Follow with the applicable current result while keeping exactly one formal
`Review Result` line when a formal result is requested.
