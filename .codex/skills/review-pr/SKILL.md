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

Use Formal Review Mode for every explicit code or PR review and every Formal Review required by the repository completion route.
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

Resolve one review basis before discovery: the effective candidate, applicable requirements, accepted behavior and project commitments, selected review focus, and admissible evidence. Run the Review Workflow against that basis and produce one mode-independent assessment:
confirmed findings consolidated by fix boundary, needs-discussion conditions,
incomplete-evidence gaps, and Completion Gate state.

Record each applicable requirement's source, content version, scope and valid exceptions in readable basis source material; a task-only requirement applies only to its authorized task candidate and must not become a universal repository or public-PR rule.
Reconstruct those requirements from original user requests and maintained contracts before checking the author's proposed solution, distinguishing author choices and unverified assumptions from confirmed constraints.
Do not promote an unverified inference into a requirement for a new interface, SPI, behavior, or test.
Preserve later user approvals with their actual provenance instead of treating approval of an implementation choice as proof of an original version requirement or of its necessity.
Keep effective content and delta identity separate from Git revision lineage so equal local and public candidates retain one code-correctness basis.

Output mode must not affect candidate discovery, proof, classification,
coverage, or convergence. Never use a previous Formal Review result as
evidence or as a conclusion to match. Treat previous public findings only as
hypotheses whose cited facts must be reverified.

Completed coordinated reviews with the same effective candidate, requirements, focus, and evidence must produce the same canonical overall assessment.
Individual independent assessments may discover different candidates; the coordinator verifies and adjudicates their cumulative findings under Independent Review Coordination.
Public-PR and local-candidate reviews may resolve different candidates, but they must apply the same code-correctness judgment and Formal Review result mapping. A changed focus, requirement,
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
7. In Formal Review, complete the required review and do not stop at the first blocker.
   Consolidate findings by independent fix boundary and return the complete current-head set once after the Completion Gate.
   Before that gate, publish only an explicitly requested status, narrow review or early high-risk blocker, or the qualified negative result defined in the Formal Decision Contract.
8. Follow `AGENTS.md` for repository authority, evidence, scope, safety, and sensitive data, and follow the applicable canonical references below for implementation, testing, contract, non-regression, and verification criteria.

## Repository Code Policy References

Standalone review is read-only and does not activate `code-implementation` or acquire write authority. Before judging the effective candidate, read [implementation rules](../code-implementation/references/rules/implementation.md), [non-regression rules](../code-implementation/references/rules/non-regression.md), and [verification rules](../code-implementation/references/verification.md) through EOF. Also read [testing rules](../code-implementation/references/rules/testing.md) when tests or coverage matter, and [artifact removal and contract impact rules](../code-implementation/references/rules/artifact-removal-and-contract-impact.md) when their trigger matches. Reuse an exact reference already read by the outer implementation workflow.
Before judging reviewed code or Maven POM changes, read [coding standards](../coding-standards/SKILL.md) through EOF and use its Implementation Guidance Mode.

## Problem and Project Commitment Gate

Complete this gate before implementation-detail discovery; it establishes the review basis but does not waive the Finding Proof, Mandatory Style Verification, Completion, or convergence gates.

1. Reconstruct the problem and expected behavior from the PR, linked issue when present, official documentation, maintained contracts, current code, and tests.
2. For a defect, trace the failing path to the violated contract and its maintained owner; for another PR type, establish the goal and responsibilities it must preserve.
3. Trace the patch's actual mechanism and compare it with a repair or reuse path in that owner; record why the chosen boundary addresses the cause or accepted goal and what remains outside scope.
4. Verify outcome-sensitive assumptions about external calls, dependency versions, configuration and lifecycle, including side effects and resource cost on already-correct supported paths, before accepting the direction.
5. Classify the requested outcome as preserving an accepted contract or adding a project commitment such as new semantics, configuration, API or SPI, compatibility, topology, database, dialect, or cross-module support.
6. For Apache ShardingSphere, establish that ShardingSphere owns the behavior and that official project positioning, maintained contracts, or an explicit public maintainer decision accepts every new commitment; for an authorized downstream repository, apply equivalent target-project evidence available within the user-authorized repository boundary.
7. Identify the narrowest accepted user-visible behavior and compare it with the patch's public contract, shared abstractions, compatibility surface, documentation, and tests.
8. Check whether the change creates precedent or consistency pressure beyond the accepted behavior; an exact existing behavior or special case does not authorize broader generalization.

An open or labeled issue, popularity, contributor effort, available code, passing tests, or a small diff do not establish project acceptance.
When evidence disproves the problem, expected behavior, applicable project ownership, or an asserted accepted commitment, or when a new commitment still requires a maintainer decision, record a `Needs Discussion` condition.
When the behavior is accepted but the patch adds unsupported generalization, public surface, parallel models, or abstractions without a real stable boundary, treat the excess as a finding candidate under the implementation rules; patch size or novelty alone is not evidence of overdesign.
When a decisive acceptance or ownership fact is unavailable after every admissible route, apply the Review Incomplete Proof Gate instead of converting uncertainty into `Needs Discussion`.

## Scope and Evidence

Read [evidence-access.md](references/evidence-access.md) and complete its GitHub Access Preflight before any GitHub request. It owns public-read selection, current-head identity, authoritative file scope, local-style evidence, failure attribution, CI, external behavior, and evidence hygiene. Apply its Local Style Verification Evidence to every public-PR or PR-backed local-candidate Formal Review.

For public-PR or PR-backed local-candidate formal reviews, establish the latest public head and base, authoritative files and requirements, relevant public discussion, and any authorized local delta.
Standalone candidates follow `Local Candidate Scope`.
For discussion replies, establish the complete current thread and affected paths; obtain the full file list when scope or readiness is disputed.
Earlier findings do not prove current-head readiness.

Treat AI-assistance disclosure only as an explicit mergeability or policy-compliance concern. Apply `AI_POLICY.md` only when public evidence establishes material AI assistance; never infer it from code, prose, metadata, or a classifier.

## Mandatory Style Verification Gate

Apply this gate to every Formal Review of a public PR and every PR-backed local candidate.
This gate is local candidate verification rather than a GitHub CI query, so every Review Focus must complete it even when Code Correctness Review does not read GitHub Actions, checks, workflow runs, or Actions logs.

1. Derive the applicable PR-impact files and their owning Maven modules from the authoritative changed-file list for the effective candidate.
2. Treat production and test Java files as applicable to both Checkstyle and Spotless unless repository configuration proves that a check does not govern a file.
3. Run both checks against the latest public PR head or an accurate PR-backed local candidate that contains the latest public head and only its authorized local delta.
4. Invalidate Checkstyle and Spotless evidence only where checked files, effective configuration, tool identity and inputs or applicable coverage changed.
   Existing runs satisfy step 3 only after proof that the current accurate candidate contains the latest public head and that checked files, effective configuration, tool identity and inputs, and the complete applicable scope are unchanged.
5. Use a whole-repository check, complete affected-module checks, or an exact-file check only when the command output and file inventory prove that the selected scope covers every applicable PR-impact file.
6. Expand verification to the complete rule impact, normally the whole repository, when the PR changes global Checkstyle or Spotless configuration, a parent POM, or another cross-module style rule.
7. Treat an exit code of zero as passing evidence only when the output also proves that every applicable PR-impact file was selected; a successful command that matched no intended file is not evidence.
8. Do not use passing CI, required checks, commit statuses or a PR comment summary to satisfy or waive this gate.
   An older local result counts only when the unchanged-input and current-candidate coverage proof above is complete.
9. Do not run `spotless:apply` or another file-modifying formatter during review.

Map the gate result before applying the remaining Formal Decision Contract.

- If Checkstyle or Spotless fails on an applicable PR-impact file, return `Not Mergeable` with `Feedback Mode: Change Request`.
- A style-check failure result must include `Feedback Mode: Change Request` and the blocking-issue count in `### Result`, then identify each failed check and affected PR-impact file in `### Blocking Issues`.
- A style-check failure result must also include `### Coverage` with the effective candidate SHA, authoritative files accounted for, failed command, exit code, and affected PR-impact file.
- If environment, dependency, candidate-materialization, tool, or coverage-proof failure leaves any applicable PR-impact file unverified, apply the Formal Decision Contract's qualified negative rule; otherwise return `Review Incomplete`.
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
4. `Necessity`: the requested change is required for safety, correctness, or an applicable repository design or contract rule in the selected focus, not merely cleaner or preferable.
5. `Scope`: this PR causes the problem, exposes it through behavior it owns, or
   must address it to satisfy the linked issue.

Classify failed candidates as an incomplete-evidence gap, non-blocking
observation, clarification question, pre-existing issue, or no issue. Do not
publish non-blocking observations unless they materially help the user.

## Review Incomplete Proof Gate

`Review Incomplete` is a terminal evidence classification, not a fallback for unfinished analysis.
Classify a gap as incomplete only when a specific outcome-sensitive decisive fact remains unavailable after every admissible evidence route has been attempted; if relevant local or public evidence exists or another allowed route remains, continue the review and classify the candidate.
After authoritative scope is established, record each incomplete gap in the ledger with the missing fact, unavailable-evidence proof, affected full path, strongest alternatives checked, outcome impact, scope proof, and affected files, then run `scripts/manage_review_ledger.py validate-incomplete --ledger <ledger>` before selecting the result.
If authoritative scope cannot be established, state the exact unavailable scope fact and attempted routes in `### Required Evidence`; do not fabricate ledger scope.

## Behavior Clusters and Risk Triage

Map every substantive file to the smallest meaningful behavior cluster and identify each cluster's root cause, owner, entry paths, consumers, contracts, project-commitment delta, changed decisions, and validation points. Account explicitly for churn-only files.

Triage functional boundaries, ownership and shared contracts, compatibility and rollback, test validity, concurrency and performance, security and operations, dependencies, packaging, and generated artifacts. Read only the triggered sections of [high-risk-review.md](references/high-risk-review.md). Also read [sql-parser-review.md](references/sql-parser-review.md) for SQL grammar, visitors, parser tests, syntax documentation, dialect behavior, or parser baselines.

## Independent Review Coordination

Read [review-coordination.md](references/review-coordination.md) through EOF for every top-level Formal Review, discussion reply that makes or changes an overall readiness conclusion, and assigned independent assessment.
That reference owns reviewer roles, cross-round confirmation, session records and review-material lifecycle; the single-assessment rules remain below.

## Review Workflow

Apply this workflow to the canonical review basis without letting output mode or a previous result influence the assessment:

Start every Formal Review round with fresh coverage of the complete effective candidate and its affected behavior and contracts, preserving the original task baseline and attributed scope.
Reuse only facts and verification results whose current-candidate validity is established; do not inherit reviewed-file statuses, discovery passes, proof conclusions, or convergence results.
Before repeating evidence acquisition, consult the current ledger's sources, block coverage, candidate findings and verification material, and reuse facts only while their supporting inputs and applicable coverage remain valid.
All three discovery lenses may share the same current source material, but each must still cover the complete candidate and sharing material does not replace the convergence pass.

1. Complete `Repository Code Policy References`, then establish the authoritative effective-candidate scope and applicable requirements.
2. Complete the Problem and Project Commitment Gate and establish the accepted behavior boundary.
3. Confirm the selected review focus and admissible evidence.
4. Build behavior clusters and complete the mandatory risk triage.
5. Discover candidates across the complete scope through three lenses: root cause and behavior; blast radius and contracts; tests, runtime, and operations.
6. Apply the Finding Proof Gate to every candidate. Keep discovery notes
   private and classify every candidate before publication.
7. Complete the Mandatory Style Verification Gate for a public PR or PR-backed local candidate.
8. Consolidate findings by independent fix boundary and identify any gap that could change the blocker set.
9. Run a convergence pass across the complete candidate and its affected behavior and contracts, including the latest delta.
   If it finds a new independent candidate, return to step 6; otherwise freeze the assessment after the Completion Gate.

If an outcome-sensitive decisive fact passes the Review Incomplete Proof Gate, apply the Formal Decision Contract to the proven findings and remaining gap.
Otherwise continue the review or request a split; do not produce a complete verdict from a partial review.

## Completion Gate and Scripts

Apply the Completion Gate to every Formal Review and to a discussion reply that makes or changes an overall readiness conclusion.

- Complete behavior-cluster mapping and risk triage for every authoritative file, including churn-only files.
- Finish all three discovery lenses and classify every candidate.
- Before accepting a behavior conclusion, identify the premises whose failure would change that conclusion and verify them against current behavior and the strongest applicable counter-evidence.
- When such a premise remains unverified and admissible evidence is available, continue the review instead of treating the absence of a proven blocker as evidence for acceptance.
- Complete the Mandatory Style Verification Gate against the latest effective candidate for every public PR or PR-backed local candidate.
- Leave no unresolved gap that could change the blocker set and require a latest-candidate convergence pass with zero new independent candidates.
- Use `scripts/build_review_inventory.py --format json` when local refs are available; its Markdown is only a bounded summary.
- Use `scripts/manage_review_ledger.py` for each independent assessment; it accounts for review state but does not judge correctness.
- Initialize a new ledger for each round that requires one, from the full current inventory with pending file statuses and no inherited review passes.
- Use the existing private system temporary directory and record the exact ledger path returned by initialization for this review.
- Initialize with `--review-basis <JSON>` containing the selected focus and readable sources identified by role and origin, so the ledger binds their actual content versions to the effective candidate, original baseline and authoritative files.
- Record each substantive changed block's behavior, applicable risk axes and requirement origins, strongest counter-evidence and current source anchors with `mark-file --coverage <JSON>`, and attach finding anchors with `add-finding --anchors <JSON>`.
- Record each evidenced `Needs Discussion` condition as a `confirmed` ledger issue about the project commitment or required decision, using `origin`, `evidence`, `full_path`, `necessity` and `scope_proof` to prove that requirement and `fix_boundary` for its specific decision boundary; do not invent a code defect.
- For each newly introduced local override, compatibility exception or parallel mechanism, use the existing basis sources and block `requirements`, `counter_evidence` and `anchors` to record its constraint origins, verified limits of direct reuse and necessity conclusion separately from behavior correctness.
- Keep author-only choices and unverified assumptions as `evidence` sources.
- Record partial coverage with pending files and keep unproved findings as candidates; recording evidence does not establish a completed review.
- Rebuild ledgers with an older schema or stale inputs without inheriting their completion state.
- Validate the current basis and coverage before selecting a result; a passing ledger proves mechanical coverage and evidence freshness, while the reviewer must still establish semantic proof under the existing gates.
- For a standalone local candidate with index or working-tree changes, provide the same exact task-path file to both scripts with `--candidate-files <path>`. The scripts resolve those paths from the computed merge-base through the working tree, include matching untracked files, and fail when a listed path is unchanged.
- Mutate one ledger sequentially; do not run ledger commands concurrently.
- Batch related updates to one reviewer ledger with `scripts/manage_review_ledger.py batch --ledger <ledger> --operations <JSON>`, where the JSON is a nonempty ordered array of argument arrays for `mark-file`, `add-finding` and `add-pass`, using their existing flags and input files without `--ledger`.
- Each batch applies the existing operations in order and writes once only after all per-operation checks and both freshness checks pass.
- Batch freshness checks verify the current basis and captured source anchors before applying operations and again before writing; any failed operation or freshness check leaves the original ledger unchanged.
- Batching does not replace final ledger or session validation.

Use these JSON shapes in local files passed to the arguments, replacing the example values with current evidence and block IDs from the initialized ledger:
Sources use `rule`, `requirement` or `evidence` roles; `--anchors` takes an anchor-array file shaped like `anchors` below.
For actual-value naming findings, pass `--rule java-testing.actual-value-naming --applicability <JSON>` with one list entry per affected variable containing `variable`, `operation`, `role`, `declaration`, `operation_call`, and `use`.
The last three fields are zero-based indices into the finding's existing `anchors`, locating the declaration, tested call, and use or assertion in current candidate code lines; retain the complete test-method context needed to determine their roles.
Use `actual-value`, `setup`, `expected`, or `unknown` for `role`; confirmation requires complete evidence with `actual-value`, while candidate and withdrawn findings may retain missing or contrary evidence.
These checks validate the recorded premises and their consistency, not Java semantics; derive applicability from the governing test rule and the complete method.

```json
{"focus": "Code Correctness Review", "sources": [{"role": "rule", "origin": "CODE_OF_CONDUCT.md", "path": "CODE_OF_CONDUCT.md"}]}
```

```json
[
  {
    "block": "<block ID>", "behavior": "<verified behavior>", "risk_axes": ["contracts"],
    "requirements": ["CODE_OF_CONDUCT.md"], "counter_evidence": "<contrary scenario checked and result>",
    "anchors": [{"side": "candidate", "path": "<changed repository-relative path>", "start_line": 1, "end_line": 2}]
  }
]
```

Use `base` for original content and `candidate` for effective content; text blocks need an anchor overlapping their changed lines, whole-object binary, mode or rename changes omit the line bounds, and `source` anchors require the declared source's matching origin and path.

If the gate cannot pass because a gap satisfies the Review Incomplete Proof Gate, apply the Formal Decision Contract's qualified negative rule before selecting the incomplete result.
When no qualified negative result applies, return `Review Incomplete` in Formal Review and state the incomplete evidence without a formal verdict in a discussion reply.
When confirmed blockers coexist with a proven gap that could hide more blockers, list them as confirmed partial facts but do not present them as the complete change-request set.

## Formal Decision Contract

In Formal Review Mode, when a concrete failure prevents completing the required review or the current basis reaches its confirmation limit, automatically publish `Not Mergeable` if at least one current-candidate finding passes the Finding Proof Gate and substantive coordinator review.
The coordinator must verify candidate identity, source versions, the full failing path and strongest counter-evidence; unresolved facts or differences must not be capable of invalidating that finding.
This qualified negative result also applies when local Git objects, ledgers or session validation are unavailable but admissible immutable evidence independently proves the finding.
Use the existing feedback-mode rules for the proven findings, including `Needs Discussion` for an evidenced required maintainer decision.
A candidate, disputed, withdrawn or stale finding, a tool failure, or non-convergence alone cannot justify this result.
The exception permits a qualified negative report while full coverage or confirmation remains unfinished; it does not permit `Mergeable`, an exhaustive finding-set claim, a successful ledger or session-validation claim, or coding-task completion.

For a complete result, evaluate the Completion Gate and Independent Review Coordination, then apply the following mapping:

1. If the gate fails because a gap satisfies the Review Incomplete Proof Gate and no qualified negative result applies, use `Review Incomplete`.
2. If the Mandatory Style Verification Gate is incomplete for any applicable PR-impact file and no qualified negative result applies, use `Review Incomplete`.
3. If Checkstyle or Spotless fails on an applicable PR-impact file, use `Not Mergeable` with `Feedback Mode: Change Request`.
4. If admissible evidence disproves the problem model, expected behavior, ownership, protocol or SQL semantics, compatibility assumption, accepted project commitment, or solution direction, or establishes that a new project commitment still requires a maintainer decision, use `Not Mergeable` with `Feedback Mode: Needs Discussion`.
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
- Record the current round's authorized files and change intents separately from the full cumulative task-owned candidate and affected behavior and contracts to review.
  A narrow fix authorization limits writes without shrinking that review scope or treating earlier task-owned changes as unrelated existing code.
- For a local candidate targeting an existing PR, use the latest public PR head plus authorized local commits, index changes, and working-tree changes as the effective candidate. Verify with read-only Git that local `HEAD` equals or descends from the public head; if that relationship cannot be established, record the unavailable lineage as an incomplete gap, and if the refs prove divergence, resolve the correct review basis before selecting a verdict.
- Review a PR-backed local candidate from the public PR merge-base through the working tree, use the union of GitHub files and the authorized local delta, and exclude unrelated local changes.
- Apply Code Correctness Review through the canonical assessment, including the same proof and completion gates, triggered high-risk criteria, convergence loop, and Formal Decision Contract.
- In `### Coverage`, identify whether the candidate is standalone or PR-backed and record its baseline and attributed local delta. For a PR-backed candidate, identify authorized local changes separately from the public PR and never present them as the public PR state.
- Keep this Skill review-only. The outer active implementation loop fixes safe in-scope findings and reruns Formal Review; scope expansion, architecture choices, and high-risk actions return to their existing authorization gates. This Skill never activates that loop.

## Multi-Round and Challenged Findings

Read [review-corrections.md](references/review-corrections.md) for previous feedback, finding-fix commits, challenged findings, or a disputed overall readiness conclusion.
Re-evaluate the latest head, test prior findings against challenger evidence, withdraw unsupported blockers, and classify later findings under that reference.

## Output Contract

Return every review output as one complete copy-ready Markdown report in a fenced block labelled `markdown`, in both output modes and every review round.
Keep the selected mode's complete result inside that block, including corrections, findings, evidence, and coverage, without requiring a separate user request.
The outer completion workflow adds its required task status and evidence without repeating the complete review report.

Use the user's language for formal results. Draft GitHub-facing discussion
replies in English unless the user requests another language. Use stable labels,
repository-relative file references with line numbers, public anchors, and
sanitized command summaries. Never include internal drafts, reasoning traces,
private context, local absolute paths, temporary paths, credentials, raw long
logs, or emojis.

### Formal Review

Use this format for both public-PR and local-candidate reviews.

Begin the Formal Review with `### Summary` before `### Result`, explaining the concrete problem or goal motivating the PR, the affected scenario or impact, the actual change mechanism, the direction assessment and material limits in two to four concise sentences.
Explain how the mechanism addresses that purpose, and distinguish verified effects from intended benefits that the evidence does not establish.
Keep a required `### Correction` first, then give the current summary and result using only verified facts.

- `Mergeable`: `### Result` with exactly one `Review Result: Mergeable` line and a concise reason; `### Evidence`; `### Coverage`.
- `Not Mergeable`: `### Result` with exactly one `Review Result: Not Mergeable` line, one `Feedback Mode`, a `Blocking Issues: N` line, and a concise reason; `### Blocking Issues`; `### Coverage`.
- `Review Incomplete`: `### Result` with exactly one `Review Result: Review Incomplete` line and a concise reason; `### Confirmed Issues` when any are already proven; `### Verified Facts`; `### Required Evidence`; `### Coverage`.

For a qualified negative result, use the `Not Mergeable` structure with `Review Status: Not Converged` and `Confirmed Blocking Issues: N` in place of `Blocking Issues: N`.
State that the findings are a confirmed subset, and report the actual completed and unreviewed coverage, unresolved differences, failed or unavailable checks, and the reason confirmation could not finish.

For each reported issue, prefix its title with `[P0]`, `[P1]`, or `[P2]` according to the proven impact and urgency.

- P0 (urgent): Critical failures requiring immediate action, such as severe data loss, critical security exposure, or widespread core-service outage.
- P1 (high): Serious failures on supported paths that require prompt correction and do not meet P0.
- P2 (normal): All other warranted issues, including ordinary or minor defects and violations of applicable coding standards.

Priority expresses impact and urgency; determine proof, blocking status, feedback mode and overall result using the existing review gates.

For each blocking issue include:

- `Evidence`: current public or sanitized verification anchor.
- `Impact`: concrete failing behavior or contract.
- `Required Change` for Change Request, or `Discussion Needed` for Needs
  Discussion.

Do not add patch-level changes after selecting Needs Discussion.
Do not include placeholder headings.
In `### Coverage`, give a short verification summary, normally one to three lines, with the candidate type and baseline or head, reviewed scope, relevant check status, CI scope, and material gaps or failures.
Keep the complete requirements and project-commitment basis, behavior clusters, discovery lenses, independent assessments and stages, adjudication and resolution of differences, file inventory, verification commands and exit codes, and `Not Applicable` checks with their bases in private review material.
Report the failed-check evidence required by the Mandatory Style Verification Gate and every limitation needed to interpret the result; brevity never waives internal review or verification.
For a standalone local candidate, identify the task baseline and attributed local delta; for a PR-backed candidate, identify authorized local commits, index changes, or working-tree changes separately from the public PR state.
In Code Correctness Review, state that the result is code-scope only and CI was not reviewed while distinguishing the completed local style verification from CI.

### PR Discussion Reply

Return only the copy-ready reply.
State whether the finding is retained, withdrawn, or needs clarification, then give the public evidence and minimum next action.
Do not force a formal verdict.

### Correction

Begin with `### Correction`, then `Previous Finding or Conclusion`, `Current Status` (`Retained`, `Withdrawn`, or `Changed to Review Incomplete`), and `Reason`.
Follow with the applicable current result while keeping exactly one formal `Review Result` line when a formal result is requested.
