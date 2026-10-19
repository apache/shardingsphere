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

# Review Corrections and Multi-Round Review

Read this reference when review feedback already exists, new commits address previous findings, an author, maintainer, or user challenges a finding, or a previous overall readiness conclusion is challenged or disproved.

## Multi-Round Review

Use public previous-round feedback only for public PR conclusions.
For standalone local corrections, inherit the main Skill's task baseline, attributed local delta, authorized evidence boundary and selected mode's output contract.
Do not expose private review notes, local chat iterations, raw inventory, or internal scratch state.

Before changing an overall conclusion, apply the basis comparison and historical-finding procedures in [review-coordination.md](review-coordination.md).
Correct both unsupported findings and an unsupported overall readiness conclusion, including a previous claim that no issues remain.
Do not preserve an unsupported previous verdict for consistency.

For every prior public finding, classify the latest head as:

- Fixed.
- Partially fixed.
- Not fixed.
- Out of scope.
- Superseded by a reopened design discussion.

Review all latest deltas and the complete affected behavior; fixing earlier
comments does not establish readiness. Keep only currently confirmed blockers
in the GitHub-facing result. For partial fixes, cite current evidence and state
the minimum remaining work.

If the latest evidence shows that the direction itself must be reconsidered,
switch to `Needs Discussion` and stop presenting previous patch-level requests
as the main path.

## Findings First Reported Later

Classify every finding first reported after an earlier formal review:

- `Introduced by latest commits`: the reviewed delta created the problem.
- `Exposed by the previous fix`: the earlier fix made a different path newly
  reachable or materially changed the evidence.
- `Missed in the previous review`: the problem was already present in the
  previously reviewed head.
- `Established by a changed review basis`: a proven applicable requirement, selected focus or decisive fact changed and newly supports a finding that the prior basis did not support.

Prove the classification from the previous and current effective candidates and their applicable requirements, focus and decisive facts.
Identify a changed requirement or review scope separately and cite the decision establishing it; a later rejection of an earlier approved design does not by itself prove an earlier code defect.
Do not describe an earlier-candidate problem as newly introduced.
For a previous-review miss, acknowledge it directly and inspect the complete effective candidate for the same omitted structure, then follow [review-coordination.md](review-coordination.md) before returning the complete remaining blocker set.

## Challenged Findings

Treat the prior finding as a hypothesis to disprove:

1. Rebuild the evidence chain from current admissible facts under the main Skill's evidence boundary.
2. List every assumption required for the finding to remain true.
3. Inspect challenger-provided source, version, reproduction, CI, specification,
   and implementation evidence first.
4. Recheck the complete production or test path and strongest counter-evidence.
5. Retain the finding only if every required assumption remains supported.

If an assumption is disproved or unsupported, withdraw the blocker or change
the result to incomplete. Do not preserve the same unsupported request by
narrowing its wording. If the finding remains valid, address the counter-evidence
directly and cite the current-head proof.

Run the final finding audit again before publishing another blocking result.

## Correction Output

When correcting a previously reported conclusion, use the selected mode's output contract and begin with:

- `Previous Finding or Conclusion`
- `Current Status`: `Retained`, `Withdrawn`, or `Changed to Review Incomplete`
- `Reason`

Then provide the current mode's ordinary result.
Keep exactly one formal `Review Result` line when a formal result is requested.

## Discussion Replies

- Draft a copy-ready committer reply rather than a formal verdict unless the
  user asks for a formal review.
- State whether the finding is retained, withdrawn, or needs clarification.
- Explain the public evidence and minimum next action.
- Use English for GitHub-facing replies unless the user requests another
  language.
- Do not make a strong public claim, assign blame, or request a merge-blocking
  change until the evidence chain is closed.
