---
name: review-pr
description: >-
  Review Apache ShardingSphere pull requests and PR discussions from public evidence.
  Use for code-correctness or mergeability decisions, CI-focused review, root-cause and
  regression analysis, copy-ready committer feedback, challenged findings, multi-round
  review, and the repository completion loop's Local Candidate Preflight Mode.
---

# Review PR

## Purpose and Modes

Judge the latest reviewed scope from root cause, behavior, contracts, tests, and
public evidence. Select one output mode:

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

## Core Contracts

1. Review only. Do not modify PR code, post comments, submit reviews, resolve
   threads, rerun workflows, or change remote state without explicit authority.
2. Formal review scope is the latest public PR head and the complete GitHub
   changed-file list. Use local triple-dot semantics when reproducing it. A
   discussion reply starts from the latest head, thread context, and affected
   behavior; expand to complete scope only when the claim depends on it.
3. Community-visible conclusions use only public evidence and sanitized
   verification summaries.
4. Reconstruct `trigger -> failing path -> observed result -> expected
   behavior` before judging the patch. A fallback, default, null check,
   try-catch, or swallowed error is not a root-cause repair unless it fixes the
   owning contract.
5. Treat every concern as a candidate until it passes the Finding Proof Gate.
6. Do not turn uncertainty, inaccessible evidence, tool failure, skipped
   verification, or uninspected counter-evidence into a blocker.
7. Consolidate findings by independent fix boundary. Do not publish draft
   findings or drip-feed issues unless the user asks for status or early
   high-risk blockers.
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

For discussion replies, establish the latest public head, complete thread
context, relevant earlier review, and affected production or test paths. Fetch
the complete file list when scope is disputed or the reply changes an overall
readiness conclusion.

Read [evidence-access.md](references/evidence-access.md) whenever current GitHub,
CI, Actions, or third-party behavior evidence is required.

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

## Decision Contract

Choose the result in this order:

1. If public evidence disproves the problem model, expected behavior, ownership,
   protocol or SQL semantics, compatibility assumption, or solution direction,
   use `Not Mergeable` with `Feedback Mode: Needs Discussion`.
2. If at least one candidate passes the Finding Proof Gate, use `Not Mergeable`
   with `Feedback Mode: Change Request`.
3. If no blocker is confirmed but a public fact required by the selected focus
   is unavailable, stale, inaccessible, or unattributable, use
   `Review Incomplete`.
4. Otherwise use `Mergeable` for the selected focus.

`Mergeable` in Code Correctness Review means code-scope readiness only. Required
pending CI prevents Mergeable in Mergeability Review. A relevant CI failure is
a blocker when attributable to the PR and an incomplete gap when attribution is
unclear.

When a confirmed blocker and an additional evidence gap coexist, keep
`Not Mergeable` and disclose the unreviewed scope under `Coverage and Limits`.
`Review Incomplete` is not a substitute for a proven blocker.

## Review Criteria

Apply only criteria triggered by the changed behavior:

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

Read only the triggered sections in
[high-risk-review.md](references/high-risk-review.md). For SQL grammar,
visitors, parser tests, syntax documentation, dialect behavior, or parser
baselines, also read
[sql-parser-review.md](references/sql-parser-review.md).

## Review Workflow

1. Select output mode and review focus.
2. Establish the authoritative latest-head scope and linked requirements.
3. Model the root cause and map every required behavior to changed code and a
   validation point.
4. Build the applicable risk inventory and search for counterexamples,
   adjacent paths, compatibility changes, and unrelated substantive changes.
5. Trace tests and verification through real entry paths; apply the Finding
   Proof Gate to every candidate.
6. Deduplicate findings, review the latest delta, and run one adversarial pass.
   Stop only after a full pass finds no new independent actionable finding.

If the scope cannot be reviewed honestly, return the mode-appropriate incomplete
result or request a split. Do not produce a complete verdict from a partial
review.

## Coverage Audit and Scripts

Use Coverage Audit when the user requests complete or non-drip review, or when
the triggered high-risk criteria make omission materially likely.

- Use `scripts/build_review_inventory.py` with local refs to establish a bounded
  deterministic scope inventory.
- Use `scripts/review_ledger.py` only to account for authoritative files,
  finding classifications, and final audit passes.
- Mutate one ledger sequentially; do not run ledger commands concurrently.
- Treat script output as mechanical evidence, not semantic proof.
- Before a complete result, require every authoritative file to have a final
  coverage state, every finding to have a final classification, and the latest
  audit pass to report zero new independent findings.
- Keep temporary ledger data private and remove only the exact ledger created
  for the current review.

## Local Candidate Preflight Mode

- Use the latest public PR head plus authorized local commits, index changes,
  and working-tree changes as the effective candidate.
- Verify with read-only Git that local `HEAD` equals or descends from the public
  head. Otherwise return `Local Preflight Result: Incomplete`.
- Review from the public PR merge-base through the working tree. Scope is the
  union of GitHub files and the authorized local delta; exclude unrelated local
  changes.
- Apply Code Correctness Review, the same proof gate, triggered high-risk
  criteria, and final adversarial pass.
- Return exactly one status: `Local Preflight Result: Pass`, `Local Preflight
  Result: Changes Required`, or `Local Preflight Result: Incomplete`.
- Keep this Skill review-only. The active implementation loop fixes safe
  in-scope findings and reruns preflight; scope expansion, architecture choices,
  and high-risk actions return to their existing authorization gates.

## Multi-Round and Challenged Findings

Read [review-corrections.md](references/review-corrections.md) when previous
public feedback exists, new commits address earlier findings, or any finding is
challenged.

Always re-evaluate the latest head. Treat a prior finding as a hypothesis to
disprove, inspect challenger-provided public evidence first, and withdraw or
downgrade any unsupported blocker rather than defending it.

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

- `Mergeable`: `### Summary` with exactly one bold `Review Result: Mergeable`
  line and a concise reason; `### Evidence`; `### Coverage and Limits`.
- `Not Mergeable`: `### Summary` with exactly one bold
  `Review Result: Not Mergeable` line, one `Feedback Mode`, and a concise
  reason; `### Blocking Issues`; `### Coverage and Limits`.
- `Review Incomplete`: `### Summary` with exactly one bold
  `Review Result: Review Incomplete` line and a concise reason;
  `### Verified Facts`; `### Required Evidence`; `### Coverage and Limits`.

For each blocking issue include:

- `Evidence`: current public or sanitized verification anchor.
- `Impact`: concrete failing behavior or contract.
- `Required Change` for Change Request, or `Discussion Needed` for Needs
  Discussion.

Do not add patch-level changes after selecting Needs Discussion. Do not include
placeholder or optional headings. In Code Correctness Review, state that the
result is code-scope only and CI was not reviewed.

### PR Discussion Reply

Return only the copy-ready reply in the fenced Markdown block. State whether the
finding is retained, withdrawn, or needs clarification, then give the public
evidence and minimum next action. Do not force a formal verdict.

### Local Candidate Preflight

Return `### Local Preflight`, exactly one bold Local Preflight Result line,
confirmed required findings when present, and `### Coverage and Limits`.

### Correction

Begin with `### Correction`, then `Previous Finding`, `Current Status`
(`Retained`, `Withdrawn`, or `Changed to Review Incomplete`), and `Reason`.
Follow with the applicable current result while keeping exactly one formal
`Review Result` line when a formal result is requested.
