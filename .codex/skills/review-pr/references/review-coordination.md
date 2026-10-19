<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Review Coordination

This procedure is the shared review-confirmation route for read-only reviews and the Formal Review invoked by [change-completion.md](../../../context/change-completion.md).
That outer loop alone owns code changes, their verification and coding-task completion.

## Independent Reviewer Roles

For every top-level Formal Review and every discussion reply that makes or changes an overall readiness conclusion, a coordinator must obtain two independent complete assessments of the same effective candidate, original baseline, authoritative scope, requirements and rule versions, and review focus.
A reply limited to one finding remains scoped to that finding and does not require a full-candidate pair unless it makes an overall readiness conclusion.
Reviewers may discover different relevant raw evidence, but shared evidence must refer to the same content version and all outcome-sensitive evidence must be reconciled before completion.
Use isolated reviewer contexts within the active task; do not create another user-owned task or send repository material to an external review service.
Give every fresh pair the current requirements and their provenance, complete candidate inventory and relevant raw evidence without the author's full session history, earlier assessments, candidate lists or dispositions, a target conclusion or an expected finding count.
Apply the [main Skill](../SKILL.md)'s evidence-reuse rules when sharing current raw facts and verification evidence.
Never share reviewed-file statuses, candidate findings, proof conclusions or convergence results between independent reviewers.
If isolated contexts are unavailable, report that the required independent reviews could not be completed and do not claim a completed Formal Review.

Each independent reviewer runs the complete Review Workflow and Completion Gate once, records a separate ledger and original assessment, and returns them privately to the coordinator.
The independent-review role must not start another pair of reviews or publish a final Formal Review result.
Existing previous-miss, challenged-finding and high-risk triggers remain applicable within each assessment; the required pair supplies their independent review rather than adding another reviewer.
Preserve every original assessment and ledger without rewriting them to make the pair agree.

## Review of the Reports

The coordinator must substantively review both assessments and their supporting evidence even when their verdicts and issue lists already match.
Recheck each claim's full path, strongest counter-evidence, necessity, scope and decision classification, and check the pair for duplicated issues, omitted historical candidates and unsupported acceptance conclusions.
Normalize issues by root cause, trigger and independent fix boundary; compare the complete issue sets, evidence gaps, discussion conditions, verdicts and feedback modes rather than counts, order or wording.
Record an evidence-based disposition for every candidate raised in any assessment, including retained, withdrawn or merged candidates, with its supporting evidence, strongest counter-evidence and explanation.
A later reviewer failing to rediscover a candidate does not withdraw it, and neither majority voting nor repeated matching subsets can discard a historical candidate.
The coordinator's result review is focused verification of the reports and their evidence, not another complete independent assessment, and mechanical session validation never replaces it.

## Cross-Round Confirmation

Before changing an overall conclusion, compare the prior and current effective candidates, requirement sources and versions, applicable exceptions, review focus, evidence identities and finding sets; candidate-type or Git revision labels and a different previous verdict do not establish a changed basis.
A changed effective candidate, requirement, rule version or outcome-sensitive external fact invalidates the affected prior assessments; never combine results from different bases to pass this gate.
Start fresh confirmation on the changed basis while retaining the original task baseline, attributed scope, historical candidates and original assessments.
Only consecutive rounds on the current basis count toward its confirmation; earlier basis phases remain historical records.

1. Obtain a fresh independent pair under Independent Reviewer Roles and complete Review of the Reports.
2. Append the original reports and the coordinator's complete adjudication for this round, including every historical candidate's current disposition.
3. If no previous adjudication exists on this basis, retain this first adjudication as the comparison baseline and start a fresh round; the first round cannot complete confirmation.
4. Compare the previous and current complete adjudications, including every canonical issue's identity, status and classification, evidence gaps, discussion conditions, result and feedback mode.
   Compare unchanged decisions semantically; reviewer identities, member mappings, file hashes, evidence wording and report ordering do not establish a changed conclusion.
5. Confirm only when the fresh pair's normalized complete assessments agree with each other, the previous adjudication and the current adjudication, the supporting evidence and counter-evidence are sufficient, and no unresolved issue can change the conclusion.
   An evidenced need for a maintainer decision may be a confirmed `Needs Discussion` condition; it does not establish a code defect.
6. Otherwise verify the differences, record their evidence-based dispositions without changing the original assessments, retain the updated current adjudication as the next comparison baseline and start a fresh round.

Any substantive issue, classification, disposition, evidence-gap or readiness change requires a subsequent fresh pair.
The coordinator must retain and compare the prior adjudication even though the fresh reviewers must not receive it.
Do not select an arbitrary matching pair across rounds or impose an unagreed round or time limit.
If a concrete failure prevents continuing confirmation, report `Review Status: Not Converged`, that reason, the confirmed facts and unresolved differences without a formal `Review Result` line or a mergeability conclusion.
Disagreement or an unavailable isolation mechanism does not satisfy the Review Incomplete Proof Gate.

When authoritative scope cannot be established, preserve the Review Incomplete Proof Gate's missing-scope evidence route and state explicitly that complete independent code reviews could not start.
That proven scope failure may return the existing `Review Incomplete` report without a session, but must not claim completed dual coverage or a mergeability conclusion.
A confirmed `Mergeable` or `Not Mergeable` completes a read-only review; the caller determines any further authorized action.

## Session Records and Validation

Each reviewer writes one private original result JSON containing `reviewer`, the exact `ledger` path, `ledger_sha256`, `result` and `feedback_mode` after completing its ledger.
Use a distinct isolated reviewer identity for every assessment, compute hashes from the frozen file bytes, and keep the original result and ledger unchanged through session validation.
Use `Mergeable`, `Not Mergeable` or `Review Incomplete` for `result`, and an empty string, `Change Request` or `Needs Discussion` for `feedback_mode` as required by the Formal Decision Contract.

The coordinator's private session JSON keeps `reviews` in consecutive pairs and one `rounds` entry per pair, using the following record shape:

```json
{"reviews": [{"path": "<original-result JSON>", "sha256": "<original-result SHA256>"}], "rounds": [{"result": "<this round's adjudicated result>", "feedback_mode": "<this round's adjudicated feedback mode>", "report_reviews": [{"reviewer": "<identity>", "reason": "<result and evidence recheck>", "counter_evidence": "<strongest contrary evidence checked>", "evidence": [{"reviewer": "<identity>", "anchor": {}}]}], "findings": [{"id": "<stable canonical issue ID>", "members": [{"reviewer": "<identity>", "finding": "<ledger finding ID>"}], "status": "<finding status>", "classification": "<issue type, discussion condition or evidence-gap classification>", "reason": "<disposition and supporting evidence>", "counter_evidence": "<strongest contrary evidence checked>", "evidence": [{"reviewer": "<identity>", "anchor": {}}]}]}], "result": "<confirmed final result>", "feedback_mode": "<confirmed final feedback mode>"}
```

Include a `report_reviews` entry for both reviewers in every pair, even for matching empty issue lists, and substantiate the result classification and acceptance premises as well as individual findings.
Replace every example `anchor` with a complete captured real-source anchor from that reviewer's ledger, including its object and range hashes; an empty object or a free-form citation does not establish evidence.
Each round's `findings` must cover every original candidate raised up to that round, map each reviewer and finding exactly once, and retain earlier canonical IDs with their updated evidence-based disposition.
Use the existing finding statuses; a duplicate may map to the same canonical issue, while a merged earlier canonical ID remains recorded as withdrawn with the merge target explained in `reason`.
Use `classification` to describe the issue type, discussion condition or evidence gap in stable text; it is not a new status enumeration.
An issue discovered during result review may have empty `members` but still requires real evidence, a disposition and counter-evidence; an unresolved candidate never permits a completed result.
After a basis change, retain the earlier rounds and original reports for historical completeness and verify the current dispositions from evidence valid on the current basis.
Do not treat an earlier basis's frozen evidence as current or require it to pass current-candidate freshness checks.
The final pair's original result, feedback mode, normalized confirmed issues and evidence gaps must agree with each other and with both final consecutive adjudications and the proposed session result.
Complete historical dispositions, including their classifications, must also be stable between those adjudications.

Before publishing a completed Formal Review result or an overall readiness conclusion in a discussion reply, run `scripts/review_ledger.py validate-session --session <JSON>` after the coordinator's substantive result review.
The command is read-only and validates records, references and evidence freshness; it neither launches reviewers nor proves their isolation or semantic correctness.
If session validation fails, inspect the exact failure and correct only an unsupported record or missing evidence; never rewrite an original assessment or change issue mappings merely to obtain a pass.
Only the coordinator emits the final report under the Output Contract after validation succeeds.

## Review-Material Lifecycle

Keep ledger data private and let the coordinator retain the owned ledgers, original results, session records and input materials throughout confirmation.
Independent reviewers return their recorded materials without cleaning them.
For a read-only review, form a self-contained report and clean the owned materials after result review and session validation finish.
For an enclosing coding task, retain them across `Not Mergeable` findings and candidate changes until that task completes or its failure or cancellation is handled.
At cleanup, automatically run `scripts/review_ledger.py cleanup --ledger <recorded-ledger>` for each exact recorded review instance, including Mergeable, Not Mergeable, Review Incomplete and unconverged sessions.
Before cleaning a ledger, remove only temporary input materials created and explicitly recorded for this review, preserving declared sources owned by the repository, the user or another review.
Apply the same exact-path ownership rule to the recorded original-result and session files.
On a handled failure or cancellation, attempt the same cleanup for resources whose ownership is established, preserve the original failure and report any cleanup failure or remaining resources.
Keep the report independent of temporary files and report cleanup failures without changing the review assessment.
