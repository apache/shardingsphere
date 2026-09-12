# ShardingSphere Codex Development Guide

Follow this guide; repository paths are relative.

## Instruction Sources and Routing

1. `CODE_OF_CONDUCT.md` governs contribution, Java, and unit-test style. Before changing code or tests, inspect the applicable section and record controlling decisions.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP run configurations, large structured analysis, or a command likely to exceed 100 output lines, read or reuse `.codex/context/token-efficiency.md` and follow its Mandatory Execution Contract.
3. Before the first code-affecting write, read `.codex/skills/code-implementation/SKILL.md` through EOF. It covers code, tests, scripts, build logic, generated source, and behavior configuration; read-only and prose-only work does not activate it. Its implementation reference is required, as is every in-scope candidate violation. A Skill grants no authority or scope.
4. Skill discovery is diagnostic. If it omits `code-implementation`, load that repository file directly. A missing required source keeps ordinary writes read-only; only an exact user-authorized policy or harness repair may use `.codex/harness/agents/policy-maintenance.md`, never for production or tests.
5. For read-only code or test analysis, planning, design, or review, read `.codex/skills/code-implementation/references/rules/implementation.md` through EOF without activating the write workflow. Also read `testing.md` when deciding test requirements or analyzing tests, `artifact-removal-and-contract-impact.md` when its gate below applies, and `non-regression.md` when reviewing a current-task candidate or a credible functional or performance risk.
6. Before classifying an artifact as unused or removable, read that removal reference through EOF and apply its complete evidence gate. Use single-model convergence only when the task removes the last production consumer and proves no compatibility contract remains. This route is read-only.
7. For runtime diagnosis, read `.codex/context/runtime-triage.md` through EOF. Diagnosis remains read-only; if the user authorizes a fix, activate `code-implementation` before the first code-affecting write.
8. Before selecting optional cross-cutting Skills, read `.codex/context/cross-cutting-skills.md` through EOF and apply its triggers and limits.
9. Before choosing, running, or assessing repository verification in any task, read `.codex/skills/code-implementation/references/verification.md` through EOF. For an authorized change, build, implementation, or fix, read `.codex/context/change-completion.md` before its completion loop or handoff.
10. Before creating or revising documentation, Skills, prompts, comments, configuration descriptions, or other prose, read `.codex/context/documentation-wording.md` through EOF and apply it.
11. Use repository Skills for specialized workflows; keep task-specific notes outside this file.

### Changing Repository Policy

Before changing this guide, a canonical policy source, or its harness, read `.codex/harness/agents/policy-maintenance.md` and `policy-sources.toml` through EOF. The manifest is the exact source list. Follow its V0, capability-ledger, integrity, candidate, canary, performance, and repair requirements; reject critical regressions and oversized root guides.

## Response Style

- Use plain language and the shortest complete answer.
- For details, lead with the answer, then `---`; otherwise omit it.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only: inspect and report without editing the reviewed target.
- Change, build, implement, and fix requests authorize the smallest in-scope local production and test edits plus non-destructive verification. Documentation, configuration, scripts, generated artifacts, deletion, Docker cleanup, and system changes require current-task authorization. Naming the exact non-code target authorizes it unless another gate applies.
- Inspect `git status --short` before editing and preserve unrelated or unattributed changes under the task-lifecycle gate.

### Consolidated Authorization Requests

Before requesting authorization, identify every proven scope expansion, non-code or system write, Git or remote mutation, remote transmission, destructive action, and file-changing tool. Consolidate proven needs by exact target, action, intent, and impact; list conditional needs separately. Do not re-request granted or speculative authority. Request an unforeseen need only when evidence proves its delta and declining impact. Keep task authority separate from platform approval and preserve danger warnings.

### Git Is Read-Only by Default

The user owns every Git state change. Read-only `status`, `diff`, `show`, `log`, `blame`, `grep`, and `ls-files` are allowed. Resolve a Git write read-only and run it only with current authorization for its exact operation and target; do not extend authority.

Without exact authority, do not change the index, working tree, refs, remotes, branches, tags, stashes, worktrees, or submodules. This includes `add`, `commit`, `push`, `fetch`, `pull`, `merge`, `rebase`, `reset`, `restore`, `checkout`, `switch`, `clean`, `cherry-pick`, `revert`, branch, tag, stash, worktree, and `submodule update`; do not stage or use Git to roll back.

Complete authorized local work without an unauthorized Git mutation.

For requested manual steps, use `git commit --dry-run --only`, then `git commit --only`, for exact task paths; prepend `git add --` only for new task files.

### Remote Writes and Sensitive Data

- A local coding request does not authorize updating an issue, PR, review, repository, deployment, production API, connector, cloud task, message, or other remote state. Write remotely only when the current request authorizes the exact action and target.
- For GitHub, prefer `GH_TOKEN`, then `GITHUB_TOKEN`, without exposing either; call the API directly when available, otherwise use `gh`.
- Do not send credentials, tokens, private keys, private logs, proprietary source, personal data, connection strings, or other sensitive repository data externally. Redact sensitive values from commands, summaries, and retained artifacts.
- Do not invoke another Codex task or external review service. Only the policy harness may run an isolated task, with synthetic non-sensitive cases in a read-only ephemeral environment and no source, logs, task data, or sensitive values. Otherwise use bounded self-review in the active task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker containers, changing global configuration, permissions, or packages, or taking another destructive local action:

1. Resolve and inspect the exact targets read-only.
2. State the impact and whether recovery is reliable.
3. Confirm unless the user already authorized the exact targets and a reliable recovery path exists.

A reliable recovery path is a verified backup or source with restore steps, or a deterministic rebuild or re-download from a confirmed origin. Git requires separate authority for the exact restore. If recovery is unverified, state that no reliable rollback exists and confirm again.

For Docker cleanup, distinguish reproducible images from containers, volumes, and local data. In-scope images proven unused and reproducible need no further confirmation; containers, volumes, local data, and uncertain images remain unauthorized.

The confirmation must identify the operation, exact targets, recovery or lack of reliable rollback, and potential consequence.

## Evidence, Scope, and Planning

Evaluate user premises affecting correctness, scope, compatibility, safety, or cost. Separate evidence from inference, assumption, and preference. If evidence contradicts or cannot support an action, report its impact and the smallest alternative or decision before acting; do not add generic caveats or expand scope.

Before editing, restate the goal, non-goals, forbidden tools, and output constraints. Inspect affected ownership, code, tests, contracts, configuration, registrations, boundaries, precedent, and instructions. Identify reuse, compatibility, prohibited paths, and verification, then record a compact acceptance checklist and 3–10 steps for non-trivial work.

### Strict Scope and Task-Delta Gate

1. An active task is one independent, verifiable objective, including its corrections and follow-ups. Start a new baseline only after completion and an explicit independent objective; if ambiguous, remain read-only and confirm. Findings, failures, reviews, or module mentions do not start a task.
2. Derive acceptance criteria only from requested outcomes, proven direct prerequisites, and focused regression protection. Cleanup, refactoring, generalization, consistency, adjacent fixes, and optional improvements are not requirements. A prerequisite qualifies only when omitting it prevents requested behavior, compilation, or scoped verification and no smaller in-boundary option exists.
3. Before a file-changing tool, record original status and relevant diffs, infer the smallest owning paths, and freeze `file -> allowed intent -> unmet criterion`. A user-named module or path is the maximum boundary; an allowed file does not authorize unrelated hunks.
4. Keep that baseline and boundary across follow-ups, reviews, failures, and verification. Expand only after exact additional path and intent authorization; append without rebaselining and keep other scope read-only.
5. Map every task hunk to an unmet criterion and inspect it immediately. Before an outside-boundary edit, report its evidence, minimum scope, affected contracts, and consequence of declining it.
6. Preserve all pre-existing and unattributed changes. Do not overwrite, format, remove, roll back, or claim them; absence from the baseline does not prove task ownership.
7. After the last write, audit the delta against the original baseline. Remove only unnecessary task changes that are safely separable, then hand off `file -> changed behavior -> acceptance criterion -> necessity`.

### Architecture Change Gate

Architecture changes include public contracts, SPIs, extension or loading contracts, `final`, visibility, inheritance, constructors, signatures, module dependencies, and shared ownership. Before editing, report ownership and behavior, reuse or delegation, compatibility, and minimum files and tests. Make only the requested change; confirm materially different unresolved choices and always gate an SPI change.

If the user rejects a design, stop patching it. Remove only its proven task changes, preserve unrelated work, and redesign from the last confirmed boundary. Use precise edits unless the task authorizes an exact Git restore.

## Task Code Size Limit

Do not add over 10,000 physical production and test source lines in one active task without explicit authorization. Estimate before writing and measure after each write against the original baseline; all tool, script, Skill, module, and later-turn output counts.

If the total would exceed the limit, stop, report current and projected totals and paths, and propose the smallest independently verifiable decomposition without expanding scope.

## Specialized Workflows

Use matching Skills:

- Issue diagnosis and maintainer replies: `$analyze-issue`.
- Unit-test generation or coverage: `$gen-ut` with `code-implementation` and its pre-write non-regression assessment.
- PR correctness, mergeability, CI, or pre-handoff review: `$review-pr`; Discussion Reply only when requested. Complete reviews or recommendations use Formal Review with `### Result`, one `Review Result`, `### Evidence`, and `### Coverage`; Coverage identifies local-only changes. Use `Review Incomplete` only when an outcome-sensitive decisive fact is proven unavailable. Standalone review is read-only and skips `code-implementation`.

If unavailable, record and use an equivalent manual checklist; do not install or create one for the task.

`cross-cutting-skills.md` governs optional Skills; they never waive gates or expand authority or scope.

## Change Completion Gate

Every authorized change, build, implementation, or fix requires `.codex/context/change-completion.md`, except a governed standalone restoration or rollback. Use its bounded self-review only for a proven low-risk standalone change; use `$review-pr` Formal Review for every trigger listed there. Fix safe in-scope findings and hand off only after the required delta audit, checks, and review pass.

## Functional and Performance Non-Regression Gate

Apply `.codex/skills/code-implementation/references/rules/non-regression.md` when a change can affect supported product, build, or runtime behavior or has credible performance cost. Freeze required baselines before editing, reject unauthorized loss, and treat missing or inconclusive required evidence as blocking. Do not benchmark when inspection rules out credible cost growth.
