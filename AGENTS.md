# ShardingSphere Codex Development Guide

Follow this repository-wide guide literally. Use ordinary competence only where no rule applies; replace an explicit rule with judgment only when it permits that choice. Paths are relative to repository root.

## Instruction Sources and Routing

1. `CODE_OF_CONDUCT.md` is the authority for contribution, Java, and unit-test style. Inspect the applicable section before changing code or tests and record it when it controls a decision.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP run configurations, commands likely to output more than 100 lines, or large structured analysis, read or reuse `.codex/context/token-efficiency.md` and follow its Mandatory Execution Contract.
3. Before the first code-affecting write in a task, read `.codex/skills/code-implementation/SKILL.md` through EOF and follow it. Its implementation reference defines the repository Codex design style, and every in-scope violation in the effective candidate is a required finding. Code-affecting writes include every production, test, script, or other implementation artifact, including build logic, generated source, and behavior-affecting configuration. Read-only analysis or review, diagnosis-only work, and prose-only changes do not activate this write workflow. Reading a Skill never grants authority or expands scope.
4. Automatic Skill catalog discovery is diagnostic only. If the catalog does not expose `code-implementation` but the exact repository file is readable, load it by exact path and continue. If a canonical source required for the current task is missing or unreadable, keep ordinary target writes read-only and report the blocker. Only an exact user-authorized policy or harness repair may use the constrained recovery procedure in `.codex/harness/agents/policy-maintenance.md`; that exception never applies to production or test code.
5. For read-only code or test analysis, planning, design, or review, read `.codex/skills/code-implementation/references/rules/implementation.md` through EOF without activating the write workflow. Also read `testing.md` when deciding test requirements or analyzing tests, `artifact-removal-and-contract-impact.md` when its gate below applies, and `non-regression.md` when reviewing a current-task candidate or a credible functional or performance risk.
6. Before every analysis or change that classifies an artifact as unused or removable, read `.codex/skills/code-implementation/references/rules/artifact-removal-and-contract-impact.md` through EOF and apply its complete evidence gate. When a current-task edit removes the last production consumer and the audit proves that no compatibility contract remains, apply its single-model convergence rule. This read-only route does not activate the code-writing workflow.
7. For runtime diagnosis, read `.codex/context/runtime-triage.md` through EOF. Diagnosis remains read-only; if the user authorizes a fix, activate `code-implementation` before the first code-affecting write.
8. Before selecting optional cross-cutting Skills for external-version decisions, public-contract design, unexpected failures, performance work, simplification, threat modeling, or high-risk decisions, read `.codex/context/cross-cutting-skills.md` through EOF and apply its exact triggers and limits.
9. Before choosing, running, or assessing repository verification in any task, read `.codex/skills/code-implementation/references/verification.md` through EOF. Before handoff, read `.codex/context/change-completion.md` through EOF and apply every applicable completion step.
10. Before creating or revising documentation, Skills, prompts, comments, configuration descriptions, or other prose, read `.codex/context/documentation-wording.md` through EOF and apply it to the changed prose.
11. Use repository Skills for specialized workflows. Keep task-specific notes in the task, issue, or PR, not this file.

### Changing Repository Policy

Before changing this guide, a canonical policy source, or its harness, read `.codex/harness/agents/policy-maintenance.md` and `.codex/harness/agents/policy-sources.toml` through EOF. Treat the manifest as the exact canonical source list and follow its V0 baseline, capability-ledger, source-integrity, candidate, canary, performance-comparison, and repair requirements. Do not accept a critical regression or a root guide larger than the manifest limit.

## Response Style

- Use plain language and the shortest complete answer; add only requested or necessary details.
- When details are needed, put the complete concise answer first, then `---` alone and the details; otherwise omit it.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only. Inspect and report; do not edit the reviewed target.
- Change, build, implement, and fix requests authorize the smallest in-scope local production and test code edits plus non-destructive verification. Documentation, configuration, scripts, generated artifacts, file deletion, Docker cleanup, and system changes require explicit authorization in the current task. A request that names the exact non-code target is authorization for that target; do not ask again unless another gate below applies.
- Inspect `git status --short` before editing; the task-lifecycle gate below defines how to preserve unrelated or unattributed working-tree changes.

### File-Change Entry Gate

Before any file-changing task or tool, apply the Strict Scope and Task-Delta Gate and remain read-only until recording the original baseline and exact `file -> intent -> unmet criterion` allowlist. Only exact user authorization expands it; workflows, failures, findings, prerequisites, Skills, and tools grant no scope. Only a governed standalone restoration or rollback is exempt.

### Consolidated Authorization Requests

Before requesting authorization, inspect the workflow for every proven scope expansion, non-code or system write, Git or remote mutation, remote transmission, destructive action, and file-changing tool. Request them once by target, action, intent, and impact; list conditional needs without requesting them, and never re-request granted or speculative authority. Ask again only for an unforeseeable need proved by new evidence, stating its exact delta and the effect of declining it without resetting the baseline or boundary. Keep task authority separate from command-bound platform approval and preserve dangerous-operation warnings.

### Git Is Read-Only by Default

The user owns every Git state change. Codex may use read-only Git commands such as `status`, `diff`, `show`, `log`, `blame`, `grep`, and `ls-files`.

Run a Git state-changing command only when the current request explicitly authorizes that exact operation and target. Resolve it read-only first, report the result, and do not extend that authority to prerequisites, follow-ups, adjacent targets, or later operations.

Without that exact authorization, never run a Git command that changes the index, working tree, refs, remotes, branches, tags, stashes, worktrees, or submodules. This prohibition includes `add`, `commit`, `push`, `fetch`, `pull`, `merge`, `rebase`, `reset`, `restore`, `checkout`, `switch`, `clean`, `cherry-pick`, `revert`, branch or tag mutation, stash mutation, worktree mutation, and `submodule update`. Do not stage changes or use Git as a rollback mechanism.

When a Git write lacks exact authorization, complete separately authorized local work and omit the mutation; provide a commit message or manual next step only when requested. A commit-message request is not commit authorization.

When requested, propose `git commit --dry-run --only` followed by `git commit --only` for exact task paths, with `git add --` first only for new task files.

### Remote Writes and Sensitive Data

- A local coding request never authorizes updating an issue, PR, review, repository, deployment, production API, connector, cloud task, message, or other remote state. Perform a remote write only when the current request explicitly names the action and exact target.
- For GitHub access, use the first configured token in this order: `GH_TOKEN`, then `GITHUB_TOKEN`; check it without printing, logging, persisting, or otherwise exposing its value. When a token is available, call the GitHub API directly and do not search for, inspect, or invoke `gh`; use `gh` only when neither token is configured.
- Do not transmit credentials, tokens, private keys, private logs, proprietary source, personal data, connection strings, or other sensitive repository data outside the active user-authorized Codex task, including to websites, search queries, connectors, plugins, MCP servers, review services, or external tools. Redact sensitive values from commands, summaries, and retained artifacts.
- Do not invoke an additional Codex task or external review service. The policy harness in `.codex/harness/agents/` is the only exception: it may run a separate isolated Codex task only with synthetic, non-sensitive policy cases in a read-only, ephemeral environment. Do not include source, logs, task data, or sensitive values in its prompt. Otherwise perform bounded self-review in the active user-authorized Codex task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker containers, changing global configuration, permissions, or packages, or performing another destructive local action:

1. Resolve and inspect the exact targets with read-only commands.
2. State the impact and whether recovery is reliable.
3. Obtain explicit confirmation unless the user already authorized those exact resolved targets and a reliable recovery path exists.

A reliable recovery path is a verified backup or source plus concrete restore steps, or a deterministic rebuild or re-download whose origin has been confirmed. Git is a recovery path only when the current task separately authorizes the exact restore operation. If the only copy would be lost, the source is unknown, or recovery has not been verified, state that there is no reliable rollback and confirm again before acting. Never imply that an irreversible action is recoverable.

For Docker cleanup, distinguish reproducible images and containers from persistent volumes or local data. When Docker cleanup is in scope, Codex may remove images that inspection proves unused and reproducible without another confirmation. This exception does not authorize removing containers, volumes, or local data, or an image whose source or reproducibility is uncertain.

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

Evaluate user-supplied premises independently when they can affect correctness, scope, compatibility, safety, or cost. Distinguish evidence from inference, assumption, and preference; inspect contradictions, missing constraints, unsupported causal links, and alternatives. When evidence contradicts or cannot support the proposed action, report the decisive evidence, impact, and minimum alternative, check, or decision before acting without adding generic caveats or expanding scope.

Before editing, restate the verifiable goal, non-goals, forbidden tools, and output constraints; inspect affected owners, code, tests, contracts, configuration, registrations, boundaries, precedent, and instructions; identify reuse, compatibility, expected and prohibited paths, tests, and verification; then record a compact acceptance checklist and a 3–10 step plan for non-trivial work.

### Strict Scope and Task-Delta Gate

An active task is one independent, verifiable user objective and includes every correction and later turn serving it. Capture a new baseline only after that objective is complete and the user explicitly starts another; if the transition is ambiguous, remain read-only and confirm it. A finding, failure, review result, or mention of another module never starts a new task.

Preserve pre-existing and unattributed working-tree changes throughout the task; the baseline and post-write audit below define how to identify them.

1. Derive acceptance criteria only from requested outcomes, proven direct prerequisites, and focused regression protection. Do not promote cleanup, refactoring, generalization, consistency work, or adjacent fixes into requirements.
2. Accept a prerequisite only when omitting it prevents the requested behavior, compilation, or scoped verification, no smaller in-boundary alternative exists, and every write is allowlisted; otherwise request scope expansion.
3. Before the first write, record status and relevant diffs, derive the smallest owning path set, and freeze each allowed file and change intent. An allowed file never authorizes unrelated hunks.
4. Never reset or expand the original baseline or boundary for a later turn, correction, review, failure, inspection, or verification. Edit another exact path and intent only after user authorization, append it without rebaselining, and treat the allowlist as a maximum rather than a target.
5. Infer a clear boundary without asking the user to repeat it, and map every task hunk to an unmet acceptance criterion.
6. Before a file-changing tool runs, ensure every possible write path is allowlisted. Treat tool output as task changes and inspect its actual paths and hunks immediately.
7. When an outside-boundary edit becomes necessary, stop before it and report the evidence, smallest additional scope, exact files or contracts, and consequence of declining it. Keep that scope read-only until authorized and report unrelated findings without fixing them.
8. After the last write, audit the task delta against the original baseline. Preserve all pre-existing and unattributed work; absence from the baseline does not prove task ownership. Do not overwrite, format, remove, or roll back an unattributed delta, and remove only proven task changes that are unnecessary and safely separable.
9. Hand off only the smallest correct delta and summarize each file as `file -> changed behavior -> acceptance criterion -> necessity`.

### Architecture Change Gate

Architecture changes include public contracts, SPIs, extension or loading contracts, `final`, visibility, inheritance, constructors, signatures, module dependencies, and shared-code ownership. Before editing, report the owner and behavior, reuse or delegation, compatibility, and minimum files and tests. Then make only the exact user-requested change; confirm materially different unresolved choices and always gate an SPI change.

If the user rejects a design, stop patching it. Remove only current-task changes that are provably part of the rejected design, preserve unrelated work, and redesign from the last confirmed boundary. Restore through precise file edits unless the current task separately authorizes the exact Git restore operation.

## Task Code Size Limit

Do not add more than 10,000 physical lines across production and test source files in one active task without explicit authorization for a higher limit. Estimate additions before the first source write and measure task-introduced additions after each source write against the original baseline; formatter, generator, script, Skill, module, and later-turn output all counts.

If the projected or measured total exceeds the limit, stop before writing more source, report the current and projected totals and affected paths, and propose the smallest independently verifiable decomposition without expanding scope.

## Specialized Workflows

Use the matching repository Skill when its trigger applies:

- Issue diagnosis and copy-ready maintainer replies: `$analyze-issue`.
- Unit-test generation or systematic coverage work: `$gen-ut`. This code-changing workflow composes with `code-implementation`, including its pre-write functional and performance non-regression assessment.
- Use `$review-pr` for PR correctness, side effects, mergeability, CI, and PR pre-handoff review; use its Discussion Reply only when explicitly requested. Complete reviews or recommendations use Formal Review with `### Result`, one `Review Result`, `### Evidence`, and `### Coverage`; Coverage identifies local-only changes. Use `Review Incomplete` only when an outcome-sensitive decisive fact is proven unavailable. Standalone review is read-only; skip `code-implementation`.

If a matching repository Skill is unavailable, use an equivalent manual checklist, record the fallback, and continue without installing or creating a task-specific Skill.

Optional cross-cutting Skills remain governed by `.codex/context/cross-cutting-skills.md`. Their use or absence never waives a repository gate, grants authority, or expands the frozen boundary.

## Change Completion Gate

Every authorized change, build, implement, or fix request requires the pre-handoff task-delta audit, verification, non-regression check, and risk-based review in `.codex/context/change-completion.md`, except a governed standalone restoration or rollback. Use its bounded self-review only for a proven low-risk standalone change; use `$review-pr` Formal Review for every trigger listed there. Fix safe in-scope findings, rerun invalidated checks, and hand off only after the applicable review passes.

## Functional and Performance Non-Regression Gate

Apply `.codex/skills/code-implementation/references/rules/non-regression.md` when a change can affect supported product, build, or runtime behavior or has a credible performance cost. Freeze any required pre-change baseline before editing, reject every task-introduced loss outside the exact authorized behavior, and treat missing or inconclusive required evidence as a blocker; do not benchmark a path when inspection rules out credible cost growth.
