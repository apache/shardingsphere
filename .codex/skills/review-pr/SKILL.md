---
name: review-pr
description: >-
  Review Apache ShardingSphere or user-authorized downstream pull requests and PR
  discussions from public or authorized repository evidence. Use for code-correctness or
  mergeability decisions, CI-focused review, root-cause and regression analysis, complete
  consolidated findings, copy-ready committer feedback, challenged findings, multi-round
  review, and formal review of local implementation candidates in the repository completion loop.
---

# Review PR

## Purpose and Modes

Judge the latest reviewed scope from root cause, behavior, contracts, tests, and
public or user-authorized repository evidence. Select one output mode:

- `Formal Review Mode`: return one formal result for a public PR review, authorized local-candidate review, code-readiness judgment, mergeability decision, or CI review.
- `PR Discussion Reply Mode`: return a copy-ready committer reply only when the user explicitly requests a review-thread response, author or maintainer objection reply, or challenged-finding reply. Do not add a formal verdict unless requested.

Use Formal Review Mode for every complete code review result or recommendation, including pre-handoff review of a local candidate.
Identify a local candidate and its local-only delta in `### Coverage`; do not create a separate local-preflight result format or describe local-only work as the public PR state.

## Review Focus

Review focus is independent from output mode.

| Focus | Use when | CI behavior |
|---|---|---|
| `Code Correctness Review` | Default review of code, tests, behavior, scope, or regression risk | Do not query, wait for, or report GitHub Actions, checks, workflow runs, or Actions logs |
| `Mergeability Review` | The user asks whether the PR can be merged, approved, or landed | Review code and required CI or checks |
| `CI Review` | The user asks about checks, Actions, logs, or CI failures | Treat CI evidence as the primary target |

Explicit user scope wins. Formal Review of a local candidate uses `Code Correctness Review` unless the user explicitly requests CI.

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
coverage, or convergence. Never use a previous Formal Review result as
evidence or as a conclusion to match. Treat previous public findings only as
hypotheses whose cited facts must be reverified.

Two reviews with the same effective candidate, requirements, focus, and
evidence must produce the same canonical assessment. Public-PR and local-candidate reviews may resolve different candidates, but they must apply the same code-correctness judgment and Formal Review result mapping. A changed focus, requirement,
or external fact changes the review basis and may legitimately change the
assessment. Mergeability or CI evidence may add external-state findings or
gaps, but it must not change code-correctness findings derived from an otherwise
unchanged basis.

## Core Contracts

1. Review only. Do not modify PR code, post comments, submit reviews, resolve
   threads, rerun workflows, or change remote state without explicit authority.
2. Public-PR Formal Review scope is the latest target PR head and the complete GitHub changed-file list; use local triple-dot semantics when reproducing it. Local-candidate Formal Review scope follows `Local Candidate Scope` below. A discussion reply starts from the latest head, thread context, and affected behavior; expand to complete scope only when the claim depends on it.
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
7. In Formal Review, do not select a verdict, stop at the first blocker, or publish findings before the Completion Gate.
   Consolidate findings by independent fix boundary and return the complete
   current-head set once. Only an explicit request for status, narrow review, or
   early high-risk blockers authorizes a partial result.
8. Follow `AGENTS.md` for repository authority, evidence, scope, safety, and sensitive data, and follow the applicable canonical references below for implementation, testing, contract, non-regression, and verification criteria.

## Repository Code Policy References

Standalone review is read-only and does not activate `code-implementation` or acquire write authority. Before judging the effective candidate, read [implementation rules](../code-implementation/references/rules/implementation.md), [non-regression rules](../code-implementation/references/rules/non-regression.md), and [verification rules](../code-implementation/references/verification.md) through EOF. Also read [testing rules](../code-implementation/references/rules/testing.md) when tests or coverage matter, and [artifact removal and contract impact rules](../code-implementation/references/rules/artifact-removal-and-contract-impact.md) when their trigger matches. Reuse an exact reference already read by the outer implementation workflow.

## Scope and Evidence

Read [evidence-access.md](references/evidence-access.md) and complete its GitHub Access Preflight before any GitHub request. It owns public-read selection, current-head identity, authoritative file scope, local-style evidence, failure attribution, CI, external behavior, and evidence hygiene. Apply its Local Style Verification Evidence to every public-PR or PR-backed local-candidate Formal Review.

For formal reviews, establish the latest head and base, authoritative files and requirements, relevant public discussion, and any authorized local delta. For discussion replies, establish the complete current thread and affected paths; obtain the full file list when scope or readiness is disputed. Earlier findings do not prove current-head readiness.

Treat AI-assistance disclosure only as an explicit mergeability or policy-compliance concern. Apply `AI_POLICY.md` only when public evidence establishes material AI assistance; never infer it from code, prose, metadata, or a classifier.

## Mandatory Style Verification Gate

Apply this gate to every Formal Review of a public PR and every PR-backed local candidate.
This gate is local candidate verification rather than a GitHub CI query, so every Review Focus must complete it even when Code Correctness Review does not read GitHub Actions, checks, workflow runs, or Actions logs.

1. Derive the applicable PR-impact files and their owning Maven modules from the authoritative changed-file list for the effective candidate.
2. Treat production and test Java files as applicable to both Checkstyle and Spotless unless repository configuration proves that a check does not govern a file.
3. Run both checks against the latest public PR head or an accurate PR-backed local candidate that contains the latest public head and only its authorized local delta.
4. Invalidate all prior Checkstyle and Spotless evidence immediately when the public PR head or authorized local delta changes.
5. Use a whole-repository check, complete affected-module checks, or an exact-file check only when the command output and file inventory prove that the selected scope covers every applicable PR-impact file.
6. Expand verification to the complete rule impact, normally the whole repository, when the PR changes global Checkstyle or Spotless configuration, a parent POM, or another cross-module style rule.
7. Treat an exit code of zero as passing evidence only when the output also proves that every applicable PR-impact file was selected; a successful command that matched no intended file is not evidence.
8. Do not use passing CI, required checks, commit statuses, a PR comment summary, or evidence from an older candidate to satisfy or waive this gate.
9. Do not run `spotless:apply` or another file-modifying formatter during review.

Map the gate result before applying the remaining Formal Decision Contract.

- If Checkstyle or Spotless fails on an applicable PR-impact file, return `Not Mergeable` with `Feedback Mode: Change Request`.
- A style-check failure result must include `Feedback Mode: Change Request` and the blocking-issue count in `### Result`, then identify each failed check and affected PR-impact file in `### Blocking Issues`.
- A style-check failure result must also include `### Coverage` with the effective candidate SHA, authoritative files accounted for, failed command, exit code, and affected PR-impact file.
- If environment, dependency, candidate-materialization, tool, or coverage-proof failure leaves any applicable PR-impact file unverified, return `Review Incomplete`.
- If a module-scoped command fails only on unchanged files, narrow the check reliably or compare the base and effective candidate before attribution; do not classify the PR from that module failure alone.
- If a check has no applicable PR-impact file, record `Not Applicable` and the repository-configuration basis in `### Coverage`.
- Only successful current-candidate evidence for both checks, or an explicit not-applicable determination for either check, permits the remaining Formal Decision Contract to select `Mergeable`.

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

## Review Incomplete Proof Gate

`Review Incomplete` is a terminal evidence classification, not a fallback for unfinished analysis.
Classify a gap as incomplete only when a specific outcome-sensitive decisive fact remains unavailable after every admissible evidence route has been attempted; if relevant local or public evidence exists or another allowed route remains, continue the review and classify the candidate.
After authoritative scope is established, record each incomplete gap in the ledger with the missing fact, unavailable-evidence proof, affected full path, strongest alternatives checked, outcome impact, scope proof, and affected files, then run `scripts/review_ledger.py validate-incomplete --ledger <ledger>` before selecting the result.
If authoritative scope cannot be established, state the exact unavailable scope fact and attempted routes in `### Required Evidence`; do not fabricate ledger scope.

## Behavior Clusters and Risk Triage

Map every substantive file to the smallest meaningful behavior cluster and identify each cluster's root cause, owner, entry paths, consumers, contracts, changed decisions, and validation points. Account explicitly for churn-only files.

Triage functional boundaries, ownership and shared contracts, compatibility and rollback, test validity, concurrency and performance, security and operations, dependencies, packaging, and generated artifacts. Read only the triggered sections of [high-risk-review.md](references/high-risk-review.md). Also read [sql-parser-review.md](references/sql-parser-review.md) for SQL grammar, visitors, parser tests, syntax documentation, dialect behavior, or parser baselines.

## Review Workflow

Apply this workflow to the canonical review basis without letting output mode or a previous result influence the assessment:

1. Complete `Repository Code Policy References`, then establish the authoritative effective-candidate scope and applicable requirements.
2. Confirm the selected review focus and admissible evidence.
3. Build behavior clusters and complete the mandatory risk triage.
4. Discover candidates across the complete scope through three lenses: root cause and behavior; blast radius and contracts; tests, runtime, and operations.
5. Apply the Finding Proof Gate to every candidate. Keep discovery notes
   private and classify every candidate before publication.
6. Complete the Mandatory Style Verification Gate for a public PR or PR-backed local candidate.
7. Consolidate findings by independent fix boundary and identify any gap that could change the blocker set.
8. Review the latest delta and run a full-scope convergence pass. If it finds a new independent candidate, return to step 5; otherwise freeze the assessment after the Completion Gate.

If an outcome-sensitive decisive fact passes the Review Incomplete Proof Gate, return the mode-appropriate incomplete result.
Otherwise continue the review or request a split; do not produce a complete verdict from a partial review.

## Completion Gate and Scripts

Apply the Completion Gate to every Formal Review and to a discussion reply that makes or changes an overall readiness conclusion.

- Complete behavior-cluster mapping and risk triage for every authoritative file, including churn-only files.
- Finish all three discovery lenses and classify every candidate.
- Complete the Mandatory Style Verification Gate against the latest effective candidate for every public PR or PR-backed local candidate.
- Leave no unresolved gap that could change the blocker set and require a latest-candidate convergence pass with zero new independent candidates.
- Use `scripts/build_review_inventory.py --format json` when local refs are available; its Markdown is only a bounded summary.
- Use `scripts/review_ledger.py` for multi-file, high-risk, or omission-prone review; it accounts for review state but does not judge correctness.
- For a standalone local candidate with index or working-tree changes, provide the same exact task-path file to both scripts with `--candidate-files <path>`. The scripts resolve those paths from the computed merge-base through the working tree, include matching untracked files, and fail when a listed path is unchanged.
- Mutate one ledger sequentially; do not run ledger commands concurrently.
- Keep ledger data private and remove only the exact current-review ledger.

If the gate cannot pass because a gap satisfies the Review Incomplete Proof Gate, return `Review Incomplete` in Formal Review and state the incomplete evidence without a formal verdict in a discussion reply.
When confirmed blockers coexist with a proven gap that could hide more blockers, list them as confirmed partial facts but do not present them as the complete change-request set.

## Formal Decision Contract

Map the canonical assessment to Formal Review only after the Completion Gate
evaluation:

1. If the gate fails because a gap satisfies the Review Incomplete Proof Gate, use `Review Incomplete`, even when some blockers are already confirmed.
2. If the Mandatory Style Verification Gate is incomplete for any applicable PR-impact file, use `Review Incomplete`, even when some blockers are already confirmed.
3. If Checkstyle or Spotless fails on an applicable PR-impact file, use `Not Mergeable` with `Feedback Mode: Change Request`.
4. If admissible evidence disproves the problem model, expected behavior, ownership,
   protocol or SQL semantics, compatibility assumption, or solution direction,
   use `Not Mergeable` with `Feedback Mode: Needs Discussion`.
5. If at least one candidate passes the Finding Proof Gate, use `Not Mergeable`
   with `Feedback Mode: Change Request`.
6. Otherwise use `Mergeable` for the selected focus.

`Mergeable` in Code Correctness Review means code-scope readiness only. Required
pending CI prevents Mergeable in Mergeability Review. A relevant CI failure is
a blocker when attributable to the PR and an incomplete gap when attribution is
unclear.

## Local Candidate Scope

- First classify the local candidate as standalone or as targeting an existing public PR.
- For a standalone local candidate, use the active task's original working-tree baseline plus only its attributed local commits, index changes, and working-tree changes as the effective candidate. Use the frozen task boundary and task-delta audit as authoritative scope, exclude unrelated local changes, and do not require a public head, GitHub metadata, or public lineage.
- For a local candidate targeting an existing PR, use the latest public PR head plus authorized local commits, index changes, and working-tree changes as the effective candidate. Verify with read-only Git that local `HEAD` equals or descends from the public head; if that relationship cannot be established, record the unavailable lineage as an incomplete gap, and if the refs prove divergence, resolve the correct review basis before selecting a verdict.
- Review a PR-backed local candidate from the public PR merge-base through the working tree, use the union of GitHub files and the authorized local delta, and exclude unrelated local changes.
- Apply Code Correctness Review through the canonical assessment, including the same proof and completion gates, triggered high-risk criteria, convergence loop, and Formal Decision Contract.
- In `### Coverage`, identify whether the candidate is standalone or PR-backed and record its baseline and attributed local delta. For a PR-backed candidate, identify authorized local changes separately from the public PR and never present them as the public PR state.
- Keep this Skill review-only. The outer active implementation loop fixes safe in-scope findings and reruns Formal Review; scope expansion, architecture choices, and high-risk actions return to their existing authorization gates. This Skill never activates that loop.

## Multi-Round and Challenged Findings

Read [review-corrections.md](references/review-corrections.md) for previous feedback, finding-fix commits, or challenged findings. Re-evaluate the latest head, test prior findings against challenger evidence, withdraw unsupported blockers, and classify later findings under that reference.

## Output Contract

Every standalone result returned by this Skill must be exactly one fenced `markdown` block with no prose before or after it. When the repository completion loop invokes Formal Review, this fenced block remains the complete review artifact, but the outer workflow may append only the separately fenced non-review handoff artifacts required by `.codex/context/change-completion.md` outside it. The first non-empty line of the review artifact must be ```` ```markdown ```` and its last non-empty line must be ```` ``` ````.

Use the user's language for formal results. Draft GitHub-facing discussion
replies in English unless the user requests another language. Use stable labels,
repository-relative file references with line numbers, public anchors, and
sanitized command summaries. Never include internal drafts, reasoning traces,
private context, local absolute paths, temporary paths, credentials, raw long
logs, or emojis.

### Formal Review

Use this format for both public-PR and local-candidate reviews.

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

Do not add patch-level changes after selecting Needs Discussion.
Do not include placeholder headings.
In `### Coverage`, report the candidate type, reviewed baseline or head, authoritative requirements and files accounted for, behavior clusters, completed discovery lenses, unresolved gaps, and CI scope.
For every public PR or PR-backed local candidate, also report the effective candidate SHA, each style-verification scope and command, each exit code, every covered applicable PR-impact file, and every `Not Applicable` check with its basis.
For a standalone local candidate, identify the task baseline and attributed local delta; for a PR-backed candidate, identify authorized local commits, index changes, or working-tree changes separately from the public PR state.
In Code Correctness Review, state that the result is code-scope only and CI was not reviewed while distinguishing the completed local style verification from CI.

### PR Discussion Reply

Return only the copy-ready reply in the fenced Markdown block. State whether the
finding is retained, withdrawn, or needs clarification, then give the public
evidence and minimum next action. Do not force a formal verdict.

### Correction

Begin with `### Correction`, then `Previous Finding`, `Current Status`
(`Retained`, `Withdrawn`, or `Changed to Review Incomplete`), and `Reason`.
Follow with the applicable current result while keeping exactly one formal
`Review Result` line when a formal result is requested.
