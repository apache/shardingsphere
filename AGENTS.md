# ShardingSphere Codex Development Guide

Follow this guide; use judgment only where no rule applies or permits it. Paths are repository-relative.

## Instruction Sources and Routing

1. `CODE_OF_CONDUCT.md` governs contribution, Java, and unit-test style. Before changing code or tests, inspect its applicable section and record any controlling decision.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP run configurations, commands likely to exceed 100 output lines, or large structured analysis, read or reuse `.codex/context/token-efficiency.md` and follow its Mandatory Execution Contract.
3. Before the first code-affecting write, read `.codex/skills/code-implementation/SKILL.md` through EOF and follow it. This covers production, tests, scripts, build logic, generated source, and behavior-affecting configuration; its implementation reference defines the design style, and every in-scope candidate violation is required. Read-only analysis, review, diagnosis, and prose-only changes do not activate this workflow. A Skill grants no authority or scope.
4. Skill catalog discovery is diagnostic. If it omits `code-implementation`, load the readable repository file by exact path. A missing required canonical source keeps ordinary target writes read-only; only an exact user-authorized policy or harness repair may use `.codex/harness/agents/policy-maintenance.md`, never for production or test code.
5. For read-only code or test analysis, planning, design, or review, read `.codex/skills/code-implementation/references/rules/implementation.md` through EOF without activating the write workflow. Also read `testing.md` when deciding test requirements or analyzing tests, `artifact-removal-and-contract-impact.md` when its gate below applies, and `non-regression.md` when reviewing a current-task candidate or a credible functional or performance risk.
6. Before classifying an artifact as unused or removable, read `.codex/skills/code-implementation/references/rules/artifact-removal-and-contract-impact.md` through EOF and apply its complete evidence gate. Apply its single-model convergence rule only when a current-task edit removes the last production consumer and the audit proves no compatibility contract remains. This read-only route does not activate writing.
7. For runtime diagnosis, read `.codex/context/runtime-triage.md` through EOF. Diagnosis remains read-only; if the user authorizes a fix, activate `code-implementation` before the first code-affecting write.
8. Before selecting optional cross-cutting Skills for external-version decisions, public contracts, unexpected failures, performance, simplification, threat modeling, or high-risk decisions, read `.codex/context/cross-cutting-skills.md` through EOF and apply its triggers and limits.
9. Before choosing, running, or assessing repository verification in any task, read `.codex/skills/code-implementation/references/verification.md` through EOF. Before handoff, read `.codex/context/change-completion.md` through EOF and apply its applicable steps.
10. Before creating or revising documentation, Skills, prompts, comments, configuration descriptions, or other prose, read `.codex/context/documentation-wording.md` through EOF and apply it.
11. Use repository Skills for specialized workflows. Keep task-specific notes in the task, issue, or PR, not this file.

### Changing Repository Policy

Before changing this guide, a canonical policy source, or its harness, read `.codex/harness/agents/policy-maintenance.md` and `.codex/harness/agents/policy-sources.toml` through EOF. The manifest is the exact source list; follow its V0, capability-ledger, integrity, candidate, canary, performance, and repair requirements. Reject a critical regression or an oversized root guide.

## Response Style

- Use plain language and the shortest complete answer; include only necessary details.
- For needed details, put the concise answer first, then `---` alone and the details; otherwise omit it.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only. Inspect and report; do not edit the reviewed target.
- Change, build, implement, and fix requests authorize the smallest in-scope local production and test code edits plus non-destructive verification. Documentation, configuration, scripts, generated artifacts, file deletion, Docker cleanup, and system changes require explicit authorization in the current task. A request that names the exact non-code target is authorization for that target; do not ask again unless another gate below applies.
- Inspect `git status --short` before editing; the task-lifecycle gate below defines how to preserve unrelated or unattributed working-tree changes.

### Consolidated Authorization Requests

Before requesting authorization, identify every proven scope expansion, non-code or system write, Git or remote mutation, remote transmission, destructive action, and file-changing tool. Request them together by exact target, action, intent, and impact; list conditional needs without requesting them, and do not re-request granted or speculative authority. Request an unforeseen need only when evidence proves its delta and the effect of declining it. Keep task authority separate from platform approval and preserve danger warnings.

### Git Is Read-Only by Default

The user owns every Git state change. Read-only `status`, `diff`, `show`, `log`, `blame`, `grep`, and `ls-files` are allowed. Resolve a proposed Git write read-only first, then run it only when the current request authorizes that exact operation and target; do not extend authority to prerequisites, follow-ups, adjacent targets, or later operations.

Without it, do not change the index, working tree, refs, remotes, branches, tags, stashes, worktrees, or submodules. This includes `add`, `commit`, `push`, `fetch`, `pull`, `merge`, `rebase`, `reset`, `restore`, `checkout`, `switch`, `clean`, `cherry-pick`, `revert`, branch or tag, stash, or worktree mutation, and `submodule update`. Do not stage or use Git to roll back.

Complete separately authorized local work without an unauthorized Git mutation. After a code-writing task completes, provide a commit message without being asked; this never authorizes Git mutation.

For requested manual steps, use `git commit --dry-run --only`, then `git commit --only`, for exact task paths; prepend `git add --` only for new task files.

### Remote Writes and Sensitive Data

- A local coding request does not authorize updating an issue, PR, review, repository, deployment, production API, connector, cloud task, message, or other remote state. Write remotely only when the current request names the exact action and target.
- For GitHub, use `GH_TOKEN`, then `GITHUB_TOKEN`, without exposing it. Call the API directly when either exists; otherwise use `gh`.
- Do not send credentials, tokens, private keys, private logs, proprietary source, personal data, connection strings, or other sensitive repository data externally. Redact sensitive values from commands, summaries, and retained artifacts.
- Do not invoke an additional Codex task or external review service. The policy harness in `.codex/harness/agents/` is the only exception: it may run a separate isolated Codex task only with synthetic, non-sensitive policy cases in a read-only, ephemeral environment. Do not include source, logs, task data, or sensitive values in its prompt. Otherwise perform bounded self-review in the active user-authorized Codex task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker containers, changing global configuration, permissions, or packages, or performing another destructive local action:

1. Resolve and inspect the exact targets with read-only commands.
2. State the impact and whether recovery is reliable.
3. Obtain explicit confirmation unless the user already authorized the exact resolved targets and a reliable recovery path exists.

A reliable recovery path is a verified backup or source with restore steps, or a deterministic rebuild or re-download from a confirmed origin. Git counts only with separate authorization for the exact restore. If recovery is unverified, state that no reliable rollback exists and confirm again.

For Docker cleanup, distinguish reproducible images and containers from persistent volumes or local data. When cleanup is in scope, images proven unused and reproducible may be removed without another confirmation; this does not authorize removing containers, volumes, local data, or an image with uncertain origin or reproducibility.

Use this prompt when confirmation is required:

```text
Dangerous operation detected!
Operation type: [specific action]
Scope of impact: [exact targets]
Recovery: [verified steps, or "no reliable rollback"]
Risk assessment: [potential consequence]
Please confirm whether to continue.
```

## Evidence, Scope, and Planning

Evaluate user premises independently when they affect correctness, scope, compatibility, safety, or cost. Separate evidence from inference, assumption, and preference. When evidence contradicts or cannot support an action, report it, its impact, and the smallest alternative, check, or decision before acting; do not add generic caveats or expand scope.

Before editing, restate the goal, non-goals, forbidden tools, and output constraints. Inspect affected owners, code, tests, contracts, configuration, registrations, boundaries, precedent, and instructions; identify reuse, compatibility, prohibited paths, tests, and verification; then record a compact acceptance checklist and a 3–10 step plan for non-trivial work.

### Strict Scope and Task-Delta Gate

1. An active task is one independent, verifiable user objective, including corrections and later turns that still serve it. Start a new baseline only after completion and an explicit independent objective; when the transition is ambiguous, remain read-only and confirm. Findings, failures, reviews, and mentions of another module do not start a new task.
2. Derive acceptance criteria only from requested outcomes, proven direct prerequisites, and focused regression protection. Do not promote cleanup, refactoring, generalization, consistency work, adjacent fixes, or optional improvements into requirements. Accept a prerequisite only when omitting it prevents requested behavior, compilation, or scoped verification and no smaller in-boundary alternative exists.
3. Before any file-changing tool, record the original status and relevant diffs, infer the smallest owning path set, and freeze the exact `file -> allowed intent -> unmet criterion` allowlist. A user-named module or path is the maximum boundary, and an allowed file does not authorize unrelated hunks.
4. Keep the original baseline and boundary across later turns, corrections, reviews, failures, inspection, and verification. Expand them only after the user authorizes the exact additional path and intent; append the authorization without rebaselining and keep unapproved scope read-only.
5. Map every task hunk to an unmet criterion. Inspect every path and hunk produced by a file-changing action immediately, and stop before an outside-boundary edit to report its evidence, smallest scope, affected files or contracts, and consequence of declining it.
6. Preserve all pre-existing and unattributed changes. Do not overwrite, format, remove, roll back, or claim an unattributed change; absence from the original baseline does not establish task ownership.
7. After the last write, audit the task delta against the original baseline. Remove only proven task changes that are unnecessary and safely separable, then hand off the smallest correct delta as `file -> changed behavior -> acceptance criterion -> necessity`.

### Architecture Change Gate

Architecture changes include public contracts, SPIs, extension or loading contracts, `final`, visibility, inheritance, constructors, signatures, module dependencies, and shared-code ownership. Before editing, report the owner and behavior, reuse or delegation, compatibility, and minimum files and tests. Then make only the exact user-requested change; confirm materially different unresolved choices and always gate an SPI change.

If the user rejects a design, stop patching it. Remove only its proven current-task changes, preserve unrelated work, and redesign from the last confirmed boundary. Use precise edits unless the current task authorizes the exact Git restore.

## Task Code Size Limit

Do not add more than 10,000 physical production and test source lines in one active task without explicit authorization. Estimate before writing source and measure after each write against the original baseline; all tool, script, Skill, module, and later-turn output counts.

If the projected or measured total exceeds the limit, stop, report the current and projected totals and paths, and propose the smallest independently verifiable decomposition without expanding scope.

## Specialized Workflows

Use the matching repository Skill:

- Issue diagnosis and copy-ready maintainer replies: `$analyze-issue`.
- Unit-test generation or systematic coverage: `$gen-ut`, composed with `code-implementation` and its pre-write functional and performance non-regression assessment.
- PR correctness, side effects, mergeability, CI, and pre-handoff review: `$review-pr`; use Discussion Reply only when requested. Complete reviews or recommendations use Formal Review with `### Result`, one `Review Result`, `### Evidence`, and `### Coverage`; Coverage identifies local-only changes. Use `Review Incomplete` only when an outcome-sensitive decisive fact is proven unavailable. Standalone review is read-only and skips `code-implementation`.

If a matching repository Skill is unavailable, use and record an equivalent manual checklist; do not install or create a task-specific Skill.

`.codex/context/cross-cutting-skills.md` governs optional Skills; their use or absence never waives a gate, grants authority, or expands scope.

## Change Completion Gate

Every authorized change, build, implementation, or fix requires `.codex/context/change-completion.md`, except a governed standalone restoration or rollback. Use its bounded self-review only for a proven low-risk standalone change; use `$review-pr` Formal Review for every trigger listed there. Fix safe in-scope findings, rerun invalidated checks, and hand off only after its task-delta audit, verification, non-regression check, and review pass.

## Functional and Performance Non-Regression Gate

Apply `.codex/skills/code-implementation/references/rules/non-regression.md` when a change can affect supported product, build, or runtime behavior or has credible performance cost. Freeze required baselines before editing, reject task-introduced loss outside the authorized behavior, and treat missing or inconclusive evidence as blocking. Do not benchmark when inspection rules out credible cost growth.
