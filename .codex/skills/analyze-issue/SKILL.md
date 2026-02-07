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

## Document Hygiene

Keep section titles production-ready:
- Do not keep editorial markers such as "Add New Section" or "Add Before ...".
- Section names must describe stable workflow behavior, not editing intent.

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

## Fast Triage Gate

Run this 3-question triage first and record a provisional type:
1. Can the behavior be reproduced with version + mode + SQL + config + log evidence?
2. Is the expected behavior explicitly documented in official ShardingSphere docs?
3. Do repository code/tests confirm a mismatch with the documented expectation?

Triage decision:
- Mostly Q&A -> Question
- Misconfigured or unsupported usage -> Misunderstanding / Invalid Usage
- Reproducible mismatch between expected and actual behavior -> Bug
- Intended new capability or behavior evolution -> Enhancement

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

## Minimum Evidence Package

Before root-cause conclusion, verify:
- ShardingSphere version and deployment mode (JDBC / Proxy)
- Database type and version
- Minimal reproducible SQL
- Related YAML / DistSQL config
- Expected result vs actual result
- Error stack trace and key log snippet

If any required item is missing, classify as `Needs More Info` and stop short of definitive root-cause claims.

## Topology Check

Always record topology before root-cause analysis:
- Access mode: JDBC / Proxy
- Governance mode: Standalone / Cluster
- Registry/config center: ZooKeeper / Etcd / Consul / N/A

If topology is unknown, lower confidence and request missing info first.

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
5. Use stable evidence IDs for key statements:
   - `OBS-<n>` for directly observed facts.
   - `INF-<n>` for inferences.
6. Every `INF` must reference one or more `OBS`.
7. Every conclusion in `Problem Conclusion` must reference at least one evidence ID.
8. Include source URL/path near each `OBS`.
9. For each key conclusion, output Confidence: High / Medium / Low.
10. If confidence is Low, do not give a hard conclusion; switch to missing-info request flow.

## Conflict Resolution Rule

When evidence conflicts, apply this order:
1. Official docs define expected behavior boundaries.
2. Repository code/tests define actual current behavior.
3. Issue statements/comments describe reported symptoms.

If docs and code conflict:
- Infer Bug when code violates documented behavior.
- Infer Documentation Gap when code is intentional but docs are outdated/unclear.
Always mark this as Inference and cite both sources.

## Type and Label Recommendation

Before final conclusion, provide issue type and label recommendations:
- Question: recommend `type: question`
- Misunderstanding / Invalid Usage: recommend `type: question`, `status: invalid`
- Bug: recommend `type: bug`, optionally with module/database labels (for example `in: SQL parse`, `db: SQLServer`)
- Enhancement: recommend `type: enhancement`, and optionally `status: volunteer wanted` to invite community contribution

When type is Bug/Enhancement, add module/database labels when evidence is sufficient:
- Parser-related -> `in: SQL parse`
- Routing/rewrite/execution core -> `in: Kernel`
- Proxy runtime/protocol -> `in: Proxy`
- JDBC driver behavior -> `in: JDBC`
- Database specific behavior -> `db: <engine>`

If module ownership is unclear, use only type/status labels first.

For Bug/Enhancement, provide severity and impact scope:
- Severity:
  - `S0`: critical outage or severe data risk
  - `S1`: major functionality blocked
  - `S2`: partial impact with workaround
  - `S3`: minor impact or low-frequency edge case
- Impact scope:
  - single SQL / single module / single database / cross-module / cross-database

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
- Add a mandatory regression scope subsection covering:
  - Affected modules
  - Compatibility impact (API/config/behavior)
  - Required test scope (unit/integration/e2e boundaries)
  - Backward-compatibility notes and rollback hint
- Add a mandatory compatibility checklist:
  - Behavior compatibility
  - Configuration compatibility
  - API/SPI compatibility
  - SQL compatibility (dialect/version scope)
- If incompatibility exists, document migration note and rollback hint.

## Mandatory Output Structure

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
- `Evidence Confidence: High/Medium/Low`
- `Severity: S0/S1/S2/S3` (Bug/Enhancement required)
- `Impact Scope: ...`
- `Topology: JDBC/Proxy + Standalone/Cluster`
- `Issue Type: ...`
- `Recommended Labels: ...`
- `Next Action: ...`

For Bug/Enhancement, also append:
- `Compatibility: Behavior/Config/API-SPI/SQL`
- `Regression Scope: ...`

## Missing Information Handling

If evidence is insufficient, do not guess. Explicitly list missing details and request them, for example:
- ShardingSphere version and deployment mode (JDBC / Proxy)
- Database type and version
- Minimal reproducible SQL and configuration
- Expected result vs actual result
- Error stack trace and full log snippets
- Related DistSQL / YAML configuration
- Stable reproduction or intermittent behavior

When classified as `Needs More Info`:
- Ask for the minimum missing evidence in one consolidated list.
- Set a follow-up window: 7-14 days.
- If no response after the window, recommend close with `status: invalid` (or project-default stale policy).

## Documentation and Code Citation Rules

- Documentation references must include concrete URLs.
- Code behavior references must include concrete repository paths or class names.
- All references must comply with Source Policy.

If Java examples are included, use fenced `java` code blocks.

## Extended Issue Types (First-Class Outcomes)

Extended types are valid final classifications when evidence supports them:
- Duplicate
- Needs More Info
- Documentation Gap
- Out of Scope / Won't Fix
- Security (use responsible security disclosure workflow)

Each must still follow Mandatory Output Structure and include labels and next action.

## Lightweight Lint Recommendation

Add a local checker (script or CI step) for analysis output quality:
- Verify required sections exist.
- Verify required conclusion fields exist.
- Verify evidence IDs are present and referenced.
- Verify label format and type-label consistency.

If lint fails, mark analysis as incomplete.

## Prohibited Content

- Do not recommend behavior that conflicts with official ShardingSphere conventions.
- Do not provide certainty when evidence is insufficient.
- Source and workaround restrictions are governed by Source Policy and Response Strategy by Type.
