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

Read this reference when public review feedback already exists, new commits
address previous findings, or an author, maintainer, or user challenges a
finding.

## Multi-Round Review

Use public previous-round feedback only. Do not expose private review notes,
local chat iterations, raw inventory, or internal accountability.

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

## Challenged Findings

Treat the prior finding as a hypothesis to disprove:

1. Rebuild the evidence chain from current public facts.
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

When correcting a previously published conclusion, begin the single fenced
Markdown result with:

- `Previous Finding`
- `Current Status`: `Retained`, `Withdrawn`, or `Changed to Review Incomplete`
- `Reason`

Then provide the current mode's ordinary result. Keep exactly one formal
`Review Result` line when a formal result is requested.

## Discussion Replies

- Draft a copy-ready committer reply rather than a formal verdict unless the
  user asks for a formal review.
- State whether the finding is retained, withdrawn, or needs clarification.
- Explain the public evidence and minimum next action.
- Use English for GitHub-facing replies unless the user requests another
  language.
- Do not make a strong public claim, assign blame, or request a merge-blocking
  change until the evidence chain is closed.
