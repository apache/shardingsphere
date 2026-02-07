---
name: analyze-issue
description: >-
  Used to analyze Apache ShardingSphere community issues. Emphasizes root-cause-first
  and evidence-first analysis with issue-type classification before conclusions, and
  outputs traceable results plus label recommendations in a fixed four- or five-section
  structure.
---

# Analyze Issue

## Objective

Provide a consistent, traceable, and reviewable issue analysis workflow.
Prioritize identifying the real root cause and aligning with official ShardingSphere conventions.

## Execution Boundary

Default mode is analysis-only:
- Do not modify repository files or submit code changes.
- Do not provide patch-ready implementation content unless the user explicitly asks for implementation.
- If implementation is requested, finish issue analysis first and explicitly state that execution has switched from analysis mode to implementation mode.

## Source Policy

Use only the following sources:
- Apache ShardingSphere official documentation.
- Apache ShardingSphere official repository code and tests.
- Target GitHub issue content (body, comments, and linked PRs in the same repository).

Do not use blogs, third-party tutorials, or forum posts as evidence.

## Intake Workflow

1. Identify the issue number from user input.
2. Use the canonical URL: `https://github.com/apache/shardingsphere/issues/${issueNO}`.
3. Try normal browsing first.
4. If normal access fails, use curl fallback:

```bash
curl -L -sS "https://github.com/apache/shardingsphere/issues/${issueNO}"
```

5. If structured data is needed, use the GitHub API:

```bash
curl -sS -H "Accept: application/vnd.github+json" \
  "https://api.github.com/repos/apache/shardingsphere/issues/${issueNO}"
```

## Analysis Method (Classify First)

1. Confirm the reported behavior from issue body and comments.
2. Confirm expected behavior from official docs.
3. Confirm actual behavior from repository code and tests.
4. Classify issue type first:
   - Question
   - Misunderstanding / Invalid Usage
   - Bug
   - Enhancement
5. If behavior changes are needed, explain scope and compatibility impact.

Always complete root-cause analysis before recommendations.

## Evidence Method

For every issue:
1. Distinguish Observation (directly observed) from Inference (reasoned).
2. Mark inferences explicitly.
3. Every conclusion must bind to at least one traceable source (see Source Policy).
4. If evidence conflicts, state the conflict explicitly and avoid forced certainty.

## Type and Label Recommendation

Before final conclusion, provide issue type and label recommendations:
- Question: recommend `type: question`
- Misunderstanding / Invalid Usage: recommend `type: question`, `status: invalid`
- Bug: recommend `type: bug`, optionally with module/database labels (for example `in: SQL parse`, `db: SQLServer`)
- Enhancement: recommend `type: enhancement`, and optionally `status: volunteer wanted` to invite community contribution

## Response Strategy by Type

Type-specific rules:
1. Question
- Answer directly and provide verifiable evidence.
- Use the four-section structure (see Mandatory Output Structure).

2. Misunderstanding / Invalid Usage
- Clearly identify the misunderstanding or misuse.
- Explain why (documentation constraints, code behavior, or configuration facts).
- Use the four-section structure (see Mandatory Output Structure).

3. Bug / Enhancement
- Provide code-level design suggestions (module boundaries, key classes, test scope, compatibility).
- Invite community contributors to submit PRs (including code and tests).
- Do not provide temporary workarounds.
- Use the five-section structure (see Mandatory Output Structure).

## Mandatory Output Structure

Respond in the same language as the user.

Four-section structure (Question, Misunderstanding / Invalid Usage):
1. Problem Understanding
2. Root Cause
3. Problem Analysis
4. Problem Conclusion

Five-section structure (Bug, Enhancement):
1. Problem Understanding
2. Root Cause
3. Problem Analysis
4. Code-Level Design Suggestions
5. Problem Conclusion

At the end of `Problem Conclusion`, append:
- `Issue Type: ...`
- `Recommended Labels: ...`
- `Next Action: ...`

## Missing Information Handling

If evidence is insufficient, do not guess. Explicitly list missing details and request them, for example:
- ShardingSphere version and deployment mode (JDBC / Proxy)
- Database type and version
- Minimal reproducible SQL and configuration
- Expected result vs actual result
- Error stack trace and full log snippets
- Related DistSQL / YAML configuration
- Stable reproduction or intermittent behavior

## Documentation and Code Citation Rules

- Documentation references must include concrete URLs.
- Code behavior references must include concrete repository paths or class names.
- All references must comply with Source Policy.

If Java examples are included, use fenced `java` code blocks.

## Extended Issue Types (Optional)

In addition to the four primary types, you may use:
- Duplicate
- Needs More Info
- Documentation Gap
- Out of Scope / Won't Fix
- Security (use responsible security disclosure workflow)

## Prohibited Content

- Do not recommend behavior that conflicts with official ShardingSphere conventions.
- Do not provide certainty when evidence is insufficient.
- Source and workaround restrictions are governed by Source Policy and Response Strategy by Type.
