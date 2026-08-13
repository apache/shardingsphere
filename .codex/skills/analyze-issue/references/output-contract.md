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

# Analyze Issue Output Contract

Read this reference when the user explicitly requests Reference Analysis, a
two-part response, a reusable reply template, or multi-line code whose Markdown
fences require special handling. Complete the main Skill's evidence workflow
before applying these output shapes.

## Maintainer Reply Templates

Use these as compact shape guides, not rigid text.

### Question

```markdown
Hi @user, thanks for the question.
<Direct answer from current ShardingSphere behavior.>
<Brief reason from docs/code.>
Community members are also welcome to share related experience, examples, or documentation improvements.
I suggest labeling this as `type: question` and closing it once the answer is clear.
```

### Misunderstanding / Invalid Usage

```markdown
Hi @user, thanks for the question.
This configuration is not supported by the current <feature> rule model.
<Explain the two or three project-level reasons, using `we` / `current ShardingSphere` language.>
Please <correct usage>. I suggest closing this as invalid usage / question.
```

### Bug

```markdown
Hi @user, thanks for reporting this.
This looks like a bug in <module/path> because <documented expected behavior> does not match <current code behavior>.
The fix should cover <key classes/paths> and include <test scope>. Contributors are welcome to submit a PR with code and tests.
```

### Duplicate

```markdown
Hi @user, thanks for reporting this.
This is a duplicate of #<issue-or-pr>, which already fixed or tracks the same root cause in <module/class>.
Please verify with a version that includes #<fix-pr>. I suggest labeling this as `type: duplicate` and closing it as duplicate.
```

### Enhancement

```markdown
Hi @user, thanks for the suggestion.
This is not supported today and should be handled as an enhancement rather than a bug.
Before implementation, we need to define <semantic contract>, <compatibility impact>, and <test scope>. I suggest labeling this as `type: enhancement`.
```

### Needs More Info

```markdown
Hi @user, thanks for reporting this.
We need a bit more information before we can classify this issue:
- <blocking fact 1>
- <blocking fact 2>
Please provide these details within <7-14 days>. If there is no update, we may close this as inactive / invalid due to insufficient information.
```

## Reference Analysis Requirements

- In two-part mode, put the maintainer reply first, then add this bridge sentence:
  `The reply above is based on the analysis below; the detailed reasoning is kept here for reference and follow-up contributors.`
- Use the same natural language as the user request unless the user asks for
  another language.
- Keep stable headings and fields in English: `Problem Understanding`, `Root
  Cause`, `Problem Analysis`, `Code-Level Design Suggestions`, `Problem
  Conclusion`, `Evidence Confidence`, `Issue Type`, `Recommended Labels`, and
  `Next Action`.
- Use short bullets, evidence IDs, and repo-relative paths with line numbers.
  Use bold inline labels such as `Observation:`, `Inference:`, `Confidence:`,
  and `Action:`. Do not expose local absolute paths.
- Prefer bullets over tables and concise evidence over raw JSON, full logs, or
  terminal transcripts. Keep command evidence in inline code or short fenced
  blocks.

Use this four-section shape for Question, Misunderstanding / Invalid Usage, and
Duplicate:

```markdown
### Problem Understanding

- **Issue:** ...
- **Topology:** ...
- **Observed Evidence:** `OBS-1`, `OBS-2`

### Root Cause

- **Observation:** ...
- **Inference:** ...
- **Confidence:** High/Medium/Low

### Problem Analysis

- **Issue Type:** Question / Misunderstanding / Invalid Usage / Duplicate
- **Evidence:** ...
- **Label Recommendation:** ...

### Problem Conclusion

- **Evidence Confidence:** High/Medium/Low
- **Impact Scope:** ...
- **Topology:** JDBC/Proxy + Standalone/Cluster
- **Issue Type:** ...
- **Duplicate Of:** #issue-or-pr (Duplicate only)
- **Fix PR:** #pr (Duplicate only)
- **Merged In:** commit/milestone/version if known (Duplicate only)
- **Recommended Labels:** ...
- **Next Action:** ...
```

Use this five-section shape for Bug and Enhancement:

```markdown
### Problem Understanding

- **Issue:** ...
- **Topology:** ...
- **Observed Evidence:** `OBS-1`, `OBS-2`

### Root Cause

- **Observation:** ...
- **Inference:** ...
- **Confidence:** High/Medium/Low

### Problem Analysis

- **Issue Type:** Bug / Enhancement
- **Evidence:** ...
- **Compatibility Checklist:** Behavior / Config / API-SPI / SQL

### Code-Level Design Suggestions

- **Affected Modules:** ...
- **Key Classes:** ...
- **Required Test Scope:** ...
- **Rollback Hint:** ...

### Problem Conclusion

- **Evidence Confidence:** High/Medium/Low
- **Severity:** S0/S1/S2/S3
- **Impact Scope:** ...
- **Topology:** JDBC/Proxy + Standalone/Cluster
- **Issue Type:** ...
- **Recommended Labels:** ...
- **Next Action:** ...
- **Compatibility:** Behavior/Config/API-SPI/SQL
- **Regression Scope:** ...
```

## Codex Chat Delivery

- Wrap the requested GitHub-facing body in exactly one outer fenced `markdown`
  block and keep any copy instruction outside it.
- The outer fence is a chat delivery wrapper, not part of the GitHub comment.
  When posting through an API or tool, submit only the inner body.
- Default to inline code inside the body. Use an inner fence only for multi-line
  content that materially benefits from it.
- If the body contains an inner fence, use more backticks for the outer fence
  than for every inner fence. Verify that all fences close and that the copyable
  body begins and ends inside the outer fence.
- If fence safety is uncertain, replace the inner fence with inline code or a
  plain bullet.

## Output Self-Check

- The opening reply addresses the author when known and states the decision in
  its first paragraph.
- A two-part response contains the exact bridge sentence and the required four-
  or five-section Reference Analysis.
- Every conclusion cites at least one `OBS-*` or `INF-*` evidence ID, and every
  `INF-*` derives from one or more observations.
- `Problem Conclusion` includes all fields required by the selected shape.
- Documentation evidence has concrete URLs; code evidence has repository paths
  or class names.
- Labels, severity, topology, commands, class names, method names, SQL, YAML,
  and Java snippets retain their original English or code form.
- The inner GitHub-facing body is not itself wrapped in a blockquote, HTML/XML
  container, or transcript.
- A local checker or CI step may enforce the required sections, conclusion
  fields, evidence references, and type-label consistency. Treat a failed check
  as incomplete output.
