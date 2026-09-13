# ShardingSphere Codex Development Guide

Follow this guide; paths are repository-relative.

## Instruction Sources and Routing

1. `CODE_OF_CONDUCT.md` governs contribution, Java, and test style; inspect its applicable section before changing code or tests.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP runs, large structured analysis, or commands likely to exceed 100 lines, read or reuse `.codex/context/token-efficiency.md` and follow its Command Contract.
3. Before a code-affecting write, read `.codex/skills/code-implementation/SKILL.md` through EOF and follow its implementation reference and every applicable in-scope rule. This covers production, tests, scripts, build logic, generated source, and behavior configuration; read-only and prose-only work does not activate it, and a Skill grants no authority or scope.
4. Skill discovery is diagnostic; load the exact repository file when it omits `code-implementation`. A missing required canonical source keeps ordinary writes read-only; only an exact user-authorized policy or harness repair may use `.codex/harness/agents/policy-maintenance.md`, never for production or tests.
5. For read-only code or test analysis, planning, design, or review, read `.codex/skills/code-implementation/references/rules/implementation.md` through EOF without activating the write workflow. Also read `testing.md` when deciding test requirements or analyzing tests, `artifact-removal-and-contract-impact.md` when its gate below applies, and `non-regression.md` when reviewing a current-task candidate or a credible functional or performance risk.
6. Before classifying an artifact as unused or removable, read the removal reference through EOF and apply its complete evidence gate. Use single-model convergence only when the task removes the last production consumer and proves no compatibility contract remains.
7. For runtime diagnosis, read `.codex/context/runtime-triage.md` through EOF; diagnosis remains read-only until an authorized fix activates `code-implementation`.
8. Before selecting optional cross-cutting Skills, read `.codex/context/cross-cutting-skills.md` through EOF and apply its triggers and limits.
9. Before choosing, running, or assessing repository verification in any task, read `.codex/skills/code-implementation/references/verification.md` through EOF. For an authorized change, read `.codex/context/change-completion.md` through EOF before completion review or handoff.
10. Before revising documentation, Skills, prompts, comments, or configuration prose, read `.codex/context/documentation-wording.md` through EOF.
11. Use repository Skills for specialized workflows and keep task-specific notes outside this file.

### Changing Repository Policy

Before changing this guide, a canonical policy source, or its harness, read `.codex/harness/agents/policy-maintenance.md` through EOF and inspect the affected entries and profiles in `policy-sources.toml`. The runner validates the complete manifest. Follow the applicable V0, capability-ledger, integrity, candidate, canary, performance, and repair requirements; reject critical regressions and oversized root guides.

## Response Style

- Use plain language and the shortest complete answer.
- For details, lead with the answer, then `---`; otherwise omit it.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only; inspect and report without editing the target.
- Change, build, implement, and fix requests authorize the smallest in-scope local production and test edits plus non-destructive verification. Documentation, configuration, scripts, generated artifacts, deletion, Docker cleanup, and system changes require current-task authorization; naming the exact non-code target supplies it unless another gate applies.
- Inspect `git status --short` before editing and preserve unrelated or unattributed changes.

### Consolidated Authorization Requests

Before requesting authorization, resolve and consolidate every proven scope expansion, non-code or system write, Git or remote mutation, remote transmission, destructive action, and file-changing tool by exact target, action, intent, and impact. List conditional needs separately, do not re-request granted or speculative authority, and request unforeseen authority only when evidence proves the delta and consequence of declining it. Keep task authority separate from platform approval and preserve danger warnings.

### Git Is Read-Only by Default

The user owns every Git state change. Read-only inspection is allowed, but without current authorization for an exact operation and target, do not mutate the index, working tree, refs, remotes, branches, tags, stashes, worktrees, or submodules; this includes `add`, `commit`, `push`, `fetch`, `pull`, `merge`, `rebase`, `reset`, `restore`, `checkout`, `switch`, `clean`, `cherry-pick`, `revert`, branch, tag, stash, worktree, and `submodule update`. Do not stage files or use Git to roll back. Resolve a requested mutation read-only first, do not extend its authority, and complete authorized local work without it when it remains unauthorized.

For requested manual steps, use `git commit --dry-run --only`, then `git commit --only`, for exact task paths; prepend `git add --` only for new task files.

### Remote Writes and Sensitive Data

- A local request does not authorize a remote update; write only when the current request names the exact remote action and target.
- For GitHub, use `GH_TOKEN`, then `GITHUB_TOKEN`, without exposing either; prefer the API when available, otherwise use `gh`.
- Never send sensitive data externally, including credentials, private logs or source, personal data, and connection strings; redact it from commands, summaries, and retained artifacts.
- Do not invoke another Codex task or external review service. Only the policy harness may run isolated, read-only, ephemeral Codex evaluations with synthetic non-sensitive cases and no repository source, logs, or task data; otherwise review in the active task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker containers, changing global configuration, permissions, or packages, or another destructive or high-risk local action, resolve the exact targets read-only, state impact and recovery, and confirm unless the user already authorized those targets and recovery is reliable. Reliable recovery requires a verified source with restore steps or deterministic rebuild or download; Git restore needs separate authority. The confirmation must name the operation, targets, recovery, and consequence.

For Docker cleanup, distinguish reproducible images from containers, volumes, and data. An in-scope image proven unused and reproducible needs no further confirmation; the other categories remain unauthorized unless named.

## Evidence, Scope, and Planning

Evaluate premises that affect correctness, scope, compatibility, safety, or cost. Separate evidence from inference and preference; when evidence contradicts or cannot support an action, report the impact and smallest alternative before acting.

Before editing, restate the goal, non-goals, forbidden tools, and output constraints. Inspect affected owners, code, tests, contracts, configuration, registrations, boundaries, precedent, and instructions; then record a compact acceptance checklist and a 3–10 step plan for non-trivial work.

### Strict Scope and Task-Delta Gate

1. An active task is one independent, verifiable objective including its corrections and follow-ups. Start a new baseline only after completion and an explicit independent objective; when ambiguous, stay read-only and confirm. Findings, failures, reviews, and module mentions do not start a task.
2. Derive acceptance criteria only from requested outcomes, proven direct prerequisites, and focused regression protection. Do not promote cleanup, refactoring, generalization, consistency, adjacent fixes, or optional improvements; a prerequisite qualifies only when no smaller in-boundary option can preserve behavior, compilation, or verification.
3. Before a file-changing tool, record original status and relevant diffs, infer the smallest owning paths, and freeze `file -> allowed intent -> unmet criterion`. A named module or path is the maximum boundary, and an allowed file does not authorize unrelated hunks.
4. Keep the baseline and boundary through follow-ups, reviews, failures, and verification. Expand only with exact additional path and intent authorization, append without rebaselining, and keep other scope read-only.
5. Map every task hunk and inspect all paths and hunks after each coherent precise edit. Inspect immediately after a formatter, generator, bulk replacement, or other action whose write set is not exact. Before an outside-boundary edit, report its evidence, minimum scope, affected contracts, and consequence of declining it.
6. Preserve pre-existing and unattributed changes; do not overwrite, format, remove, roll back, or claim them, and do not infer ownership from baseline absence.
7. After the last write, audit the delta against the original baseline, remove only safely separable unnecessary task changes, and hand off `file -> behavior -> criterion -> necessity`.

### Architecture Change Gate

Architecture changes include public contracts, SPIs, extension or loading contracts, `final`, visibility, inheritance, constructors, signatures, module dependencies, and shared ownership. Before editing, report ownership, reuse or delegation, compatibility, and minimum files and tests. Make only the requested change; confirm materially different choices and always gate an SPI change.

If the user rejects a design, stop patching it. Remove only its proven task changes, preserve unrelated work, and redesign from the last confirmed boundary. Use precise edits unless the task authorizes an exact Git restore.

## Task Code Size Limit

Do not add over 10,000 physical production and test source lines in one active task without explicit authorization. Estimate before writing against the original baseline across tools, scripts, Skills, modules, and later turns; recount after a generator or bulk write, when the estimate reaches 8,000 lines, and exactly after the last write. If projected or measured output exceeds the limit, stop, report totals and paths, and propose the smallest independently verifiable decomposition.

## Specialized Workflows

Use `$analyze-issue` for issue diagnosis and maintainer replies, `$gen-ut` with `code-implementation` for unit-test generation or coverage, and `$review-pr` for PR correctness, mergeability, CI, or triggered pre-handoff Formal Review. Discussion Reply requires a request. Complete reviews or recommendations use Formal Review with `### Result`, one `Review Result`, `### Evidence`, and `### Coverage`; Coverage identifies local-only changes. Use `Review Incomplete` only when an outcome-sensitive decisive fact is proven unavailable. Standalone review is read-only and skips `code-implementation`.

If a required specialized Skill is unavailable, record and use an equivalent manual checklist without installing or creating one.

`cross-cutting-skills.md` governs optional Skills; they never waive gates or expand authority or scope.

## Change Completion Gate

Every authorized change, build, implementation, or fix requires `.codex/context/change-completion.md`, except a governed standalone restoration or rollback. Use its bounded self-review unless a listed Formal Review trigger applies; fix safe in-scope findings and hand off only after the required delta audit, checks, and review pass.

## Functional and Performance Non-Regression Gate

Apply `.codex/skills/code-implementation/references/rules/non-regression.md` when a change can affect supported product, build, or runtime behavior or has credible performance cost. Freeze required baselines before editing, reject unauthorized loss, and treat missing or inconclusive required evidence as blocking. Do not benchmark when inspection rules out credible cost growth.
