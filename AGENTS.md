# ShardingSphere Codex Development Guide

Follow this guide; paths are repository-relative.

## Instruction Sources and Routing

1. Inspect the applicable `CODE_OF_CONDUCT.md` section before changing code or tests.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP runs, large structured analysis, or commands likely to exceed 100 lines, read or reuse `.codex/context/token-efficiency.md` and follow its Command Contract.
3. Before changing production, tests, scripts, build logic, generated source, or behavior configuration, read `.codex/skills/code-implementation/SKILL.md` through EOF. Read-only and prose-only work does not activate it, and no Skill grants authority or scope.
4. If Skill discovery omits required `code-implementation`, load its repository file directly. A missing required canonical source keeps ordinary writes read-only; only an exact user-authorized policy or harness repair may use `.codex/harness/agents/policy-maintenance.md`.
5. For read-only code or test analysis, planning, design, or review, read the implementation rules without activating the write workflow. Load testing, removal, contract, or non-regression rules only when that topic can affect the requested conclusion.
6. Before classifying an artifact as unused or removable, read and apply the complete removal and contract-impact rules.
7. For runtime diagnosis, read `.codex/context/runtime-triage.md`; an authorized fix activates `code-implementation` before its first write.
8. Select optional Skills from their catalog triggers. Read `.codex/context/cross-cutting-skills.md` only when a matching trigger may apply, and use the smallest matching Skill set.
9. Before running or assessing a repository verification command, read the verification rules. For an authorized change, read `.codex/context/change-completion.md` before completion review or handoff.
10. Before revising documentation, Skills, prompts, comments, or configuration prose, read `.codex/context/documentation-wording.md`.
11. Keep task-specific notes outside repository Skills.

### Changing Repository Policy

Before changing this guide, a canonical policy source, or its harness, read `.codex/harness/agents/policy-maintenance.md` and the applicable semantic-verification rules. Inspect only affected manifest entries and profiles before editing; let `run.py --mode validate` check the complete manifest and reference graph.

## Response Style

- Use plain language and the shortest complete answer.
- For details, lead with the answer, then `---`; otherwise omit it.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only; inspect and report without editing the target.
- Change, build, implement, and fix requests authorize the smallest in-scope local production and test edits plus non-destructive verification. Documentation, configuration, scripts, generated artifacts, deletion, Docker cleanup, and system changes require current-task authorization; naming the exact non-code target supplies it.
- Inspect `git status --short` before editing and preserve unrelated or unattributed changes.

### Consolidated Authorization Requests

Before requesting authorization, consolidate every proven need by exact target, action, intent, and impact. Separate conditional needs, do not re-request granted or speculative authority, and request an unforeseen need only when evidence proves its delta and consequence. Keep task authority separate from platform approval.

### Git Is Read-Only by Default

The user owns every Git state change. Without current authorization for an exact operation and target, do not change the index, working tree, refs, remotes, branches, tags, stashes, worktrees, or submodules; do not stage files or use Git to roll back. Resolve a requested mutation read-only first, do not extend its authority, and complete authorized local work without it when it remains unauthorized.

### Remote Writes and Sensitive Data

- A local request does not authorize a remote update; require the exact remote action and target.
- For GitHub, use `GH_TOKEN`, then `GITHUB_TOKEN`, without exposing either; prefer the API when available, otherwise use `gh`.
- Never send sensitive data externally, including credentials, private logs or source, personal data, and connection strings; redact it from commands, summaries, and retained artifacts.
- Do not invoke another Codex task or external review service. Only the policy harness may run isolated, read-only, ephemeral Codex evaluations with synthetic non-sensitive cases and no repository source, logs, or task data; otherwise review in the active task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker containers, or changing global configuration, permissions, or packages, resolve exact targets, impact, and recovery read-only. Confirm unless the user already authorized those targets and recovery is reliable. Reliable recovery requires a verified source with restore steps or deterministic rebuild or download; Git restore needs separate authority.

For Docker cleanup, distinguish reproducible images from containers, volumes, and data. An in-scope image proven unused and reproducible needs no further confirmation; the other categories remain unauthorized unless named.

## Evidence, Scope, and Planning

Evaluate premises that affect correctness, scope, compatibility, safety, or cost. Separate evidence from inference; report contradictory or insufficient evidence and the smallest alternative before acting.

Before editing, record the goal, boundary, acceptance criteria, and required verification. Inspect only the owners, consumers, contracts, tests, configuration, registrations, precedent, and instructions needed to make the change. For non-trivial work, plan only the ordered steps needed to control dependencies and verification.

### Strict Scope and Task-Delta Gate

1. An active task is one independent, verifiable objective including its corrections and follow-ups. Start a new baseline only after completion and an explicit independent objective; when ambiguous, stay read-only and confirm.
2. Derive acceptance criteria only from requested outcomes, proven direct prerequisites, and focused regression protection. Do not add cleanup, generalization, consistency work, adjacent fixes, or optional improvements.
3. Before a file-changing tool, record original status and relevant diffs, infer the smallest owning paths, and freeze `file -> allowed intent -> unmet criterion`. A named module or path is the maximum boundary, and an allowed file does not authorize unrelated hunks.
4. Keep the baseline and boundary through follow-ups, reviews, failures, and verification. Expand only with exact additional path and intent authorization, append without rebaselining, and keep other scope read-only.
5. Inspect every path and hunk after each coherent edit batch. Inspect immediately after a formatter, generator, bulk replacement, or any action whose write set is not exact. Before an outside-boundary edit, report its evidence, minimum scope, affected contracts, and consequence of declining it.
6. Preserve pre-existing and unattributed changes; do not overwrite, format, remove, roll back, or claim them, and do not infer ownership from baseline absence.
7. After the last write, audit the delta against the original baseline and remove only safely separable unnecessary task changes.

### Architecture Change Gate

Architecture changes include public contracts, SPIs, extension or loading contracts, module dependencies, shared ownership, and changes to `final`, visibility, inheritance, constructors, or signatures at a public or shared boundary. A private internal signature or constructor change is not architectural by itself. Before an architecture edit, report ownership, reuse or delegation, compatibility, and minimum files and tests; confirm materially different choices and always gate an SPI change.

If the user rejects a design, stop patching it. Remove only its proven task changes, preserve unrelated work, and redesign from the last confirmed boundary. Use precise edits unless the task authorizes an exact Git restore.

## Task Code Size Limit

Do not add over 10,000 physical production and test source lines in one active task without explicit authorization. Estimate before writing; recount after generated or bulk output, at 8,000 lines, and after the last write. If the task would exceed the limit, stop and propose the smallest independently verifiable decomposition.

## Specialized Workflows

Use `$analyze-issue` for issue diagnosis and maintainer replies, `$gen-ut` with `code-implementation` for unit-test generation or coverage, and `$review-pr` for PR correctness, mergeability, CI, or a triggered Formal Review. Complete reviews or recommendations use Formal Review with `### Result`, one `Review Result`, `### Evidence`, and `### Coverage`; Coverage identifies local-only changes. Standalone review is read-only. A missing required specialized Skill uses an equivalent manual checklist; do not install or create one for the task. Optional Skills never waive gates or expand authority or scope.

## Change Completion Gate

Every authorized change, build, implementation, or fix follows `.codex/context/change-completion.md`, except a governed standalone restoration or rollback.

## Functional and Performance Non-Regression Gate

Preserve supported behavior outside the exact authorized change. Load the full non-regression rules when a change affects a public or shared contract, crosses owners, or has credible functional or performance risk. Do not benchmark when inspection rules out credible cost growth.
