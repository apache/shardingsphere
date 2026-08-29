# ShardingSphere Codex Development Guide

This repository guide is written for Codex. Keep only stable, repository-wide rules here and rely on Codex for ordinary coding competence. Follow every explicit rule literally; do not replace a repository rule with general judgment unless that rule authorizes it. Paths are relative to the repository root.

## Instruction Sources and Routing

1. `CODE_OF_CONDUCT.md` is the authority for contribution, Java, and unit-test style. Inspect the applicable section before changing code or tests and record it when it controls a decision.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP run configurations, commands likely to output more than 100 lines, or large structured analysis, read or reuse `.codex/context/token-efficiency.md` and follow its Mandatory Execution Contract.
3. Before the first code-affecting write in a task, read `.codex/skills/code-implementation/SKILL.md` through EOF and follow it. Its implementation reference defines the repository Codex design style, and every in-scope violation in the effective candidate is a required finding. Code-affecting writes include every production, test, script, or other implementation artifact, including build logic, generated source, and behavior-affecting configuration. Read-only analysis or review, diagnosis-only work, and prose-only changes do not activate this write workflow. Reading a Skill never grants authority or expands scope.
4. Automatic Skill catalog discovery is diagnostic only. If the catalog does not expose `code-implementation` but the exact repository file is readable, load it by exact path and continue. If a canonical source required for the current task is missing or unreadable, keep ordinary target writes read-only and report the blocker. Only an exact user-authorized policy or harness repair may use the constrained recovery procedure in `.codex/harness/agents/policy-maintenance.md`; that exception never applies to production or test code.
5. For read-only code or test analysis, planning, design, or review, read `.codex/skills/code-implementation/references/rules/implementation.md`, `.codex/skills/code-implementation/references/rules/testing.md`, and `.codex/skills/code-implementation/references/rules/contracts-and-removal.md` through EOF without activating the write workflow. Apply each contract, impact, or removal rule only when its own trigger matches, and read `.codex/skills/code-implementation/references/rules/non-regression.md` through EOF when reviewing a current-task candidate.
6. Before every analysis or change that classifies an artifact as unused or removable, read `.codex/skills/code-implementation/references/rules/contracts-and-removal.md` through EOF and apply its complete evidence gate. When a current-task edit removes the last production consumer and the audit proves that no compatibility contract remains, apply its single-model convergence rule. This read-only route does not activate the code-writing workflow.
7. For runtime diagnosis, read `.codex/context/runtime-triage.md` through EOF. Diagnosis remains read-only; if the user authorizes a fix, activate `code-implementation` before the first code-affecting write.
8. Before selecting optional cross-cutting Skills for external-version decisions, public-contract design, unexpected failures, performance work, simplification, threat modeling, or high-risk decisions, read `.codex/context/cross-cutting-skills.md` through EOF and apply its exact triggers and limits.
9. Before choosing, running, or assessing repository verification in any task, read `.codex/skills/code-implementation/references/verification.md` through EOF. Before handoff, read `.codex/context/change-completion.md` through EOF and apply every applicable completion step.
10. Use repository Skills for specialized workflows instead of reproducing their detailed instructions here. Keep task-specific notes in the task, issue, or PR; do not add session notes to this file.

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

Before any file-changing task or tool, including formatters, generators, scripts, Skills, and review fixes, apply the Strict Scope and Task-Delta Gate.
Remain read-only until recording the original baseline and exact `file -> intent -> unmet criterion` write allowlist.
Only exact user authorization expands it; no later workflow, failure, finding, prerequisite, Skill, or tool does.
Only a governed standalone restoration or rollback is exempt.

### Consolidated Authorization Requests

Before requesting authorization, inspect the workflow for all proven exact needs: scope expansion; non-code or system writes; Git mutation; remote transmission or mutation; destructive actions; and file-changing tools.
Request all once by target, action, intent, and impact. List unresolved conditions without requesting them; never request speculative or granted authority.
Request again only when new evidence proves an unforeseeable need. State the evidence, exact delta, why it was unknowable, and the effect of declining it; preserve the original baseline and boundary.
Separate task authority from command-bound platform approval. A preannouncement needs no task confirmation; invoke approval only with its command, and let neither expand the other. Preserve required dangerous-operation warnings.

### Git Is Read-Only by Default

The user owns every Git state change. Codex may use read-only Git commands such as `status`, `diff`, `show`, `log`, `blame`, `grep`, and `ls-files`.

Codex may run a Git state-changing command only when the current request explicitly authorizes that exact operation and its exact target. The authorization applies only to that operation in the current task; it does not authorize prerequisite, follow-up, adjacent, or future Git writes. Resolve the target with read-only inspection first and report the completed operation.

Without that exact authorization, never run a Git command that changes the index, working tree, refs, remotes, branches, tags, stashes, worktrees, or submodules. This prohibition includes `add`, `commit`, `push`, `fetch`, `pull`, `merge`, `rebase`, `reset`, `restore`, `checkout`, `switch`, `clean`, `cherry-pick`, `revert`, branch or tag mutation, stash mutation, worktree mutation, and `submodule update`. Do not stage changes or use Git as a rollback mechanism.

When a requested Git write lacks exact authorization, complete any separately authorized local work, then provide a commit message or exact manual next step. Proceed with the allowed parts of the request; omit the unauthorized Git mutation rather than refusing the entire task. A request for only a commit message is not authorization to commit.

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

Maintain independent technical judgment. Do not agree with or adopt a user-supplied premise, diagnosis, design, or conclusion merely because the user proposed or prefers it; treat it as a claim to evaluate. When such a claim could materially affect correctness, scope, compatibility, safety, or cost, distinguish verified evidence from inference, assumption, and preference; inspect contradictions, missing constraints, unsupported causal links, and plausible alternatives.

If evidence contradicts the user's premise or is insufficient for the proposed action, say so before acting and state the decisive evidence, likely impact, and the minimum viable alternative, additional check, or decision needed. Do not add generic caveats, expand scope or authority, or delay straightforward authorized work merely to demonstrate skepticism.

Before editing:

1. Restate the verifiable goal, non-goals, user-forbidden tools or APIs, and coverage or output constraints.
2. Inspect the affected code, tests, contracts, configuration, registrations, module boundaries, closest maintained precedent, and applicable instructions.
3. Identify behavior owners, reuse opportunities, compatibility impact, expected changed files, prohibited paths, required tests, and scoped verification.
4. Convert these items into a compact acceptance checklist and a 3–10 step plan for non-trivial work.

### Strict Scope and Task-Delta Gate

For this gate, an active task is one independent, verifiable user objective, not the permanent lifetime of a UI conversation. It includes every later turn and correction for that objective, including after a handoff. Only after the previous objective is complete and the user explicitly requests an independent objective may Codex capture a new baseline and freeze a new boundary. When a user request could reasonably be either a follow-up or an independent objective, remain read-only and confirm the task boundary before writing. An agent finding, review result, failure, or mention of another module is not an independent objective and does not create that ambiguity.

Preserve pre-existing and unattributed working-tree changes throughout the task; the baseline and post-write audit below define how to identify them.

1. Derive the acceptance checklist only from user-requested outcomes, direct prerequisites proven by inspected evidence, and focused regression protection required by `.codex/skills/code-implementation/references/rules/testing.md`. Do not turn an agent-proposed cleanup, refactor, generalization, consistency improvement, or adjacent fix into an acceptance criterion.
2. Add a prerequisite to the acceptance checklist only when omitting it prevents the requested behavior, compilation, or scoped verification, no smaller in-boundary alternative exists, and every required write is already allowlisted.
   Otherwise it requires scope expansion; relevance, evidence, failures, and review findings never authorize it.
3. Before the first write, record the pre-task working-tree baseline, including the existing status and relevant diffs, and derive the smallest owning module or repository-path set from the user request and inspected evidence, whether or not the user named it. Freeze the allowed files and the allowed change intent for each file as the hard write allowlist for the active Codex task; an allowed file does not authorize unrelated hunks in that file.
4. Later turns, corrections, reviews, failures, inspection, and verification never reset or expand the original baseline or boundary; keep outside work read-only. Edit another path only after its exact path and intent are authorized; append its smallest delta to an unmet criterion without rebaselining. Exact-file authority excludes siblings and its module; prerequisites grant nothing. The allowlist is a maximum; every edit needs an unmet criterion. Repeat work only when an authorized edit invalidates it.
5. When the request makes the boundary clear, infer and freeze it without asking the user to repeat it. Every changed file and task-introduced hunk must map directly to one acceptance criterion and be necessary to satisfy it.
6. Before a tool may modify tracked, user-authored, or task-introduced repository files, verify that every possible write path is allowlisted; otherwise do not run it.
   After each file-changing action, inspect the actual paths and hunks before continuing.
   Tool-produced changes are task changes and receive no scope exemption.
7. If evidence proves that work outside the frozen boundary is required, stop before that edit. Report the blocking evidence, smallest additional scope, exact files or contracts, and consequence of declining it, then request confirmation. Until expansion is authorized, limit outside-boundary work to the read-only evidence needed for that report; do not start its design or implementation workflow. Report other out-of-scope findings without fixing them.
8. After the last file-changing action, audit the task-introduced delta against the pre-task baseline, not merely the aggregate working-tree diff. Preserve every pre-existing user change and every later delta that cannot be proven to result from a current-task write. Absence from the baseline does not prove task ownership. Treat an unattributed delta as user-owned, do not overwrite, format, remove, or roll it back, and stop the affected write path to report the conflict. Remove only proven current-task changes that lack a direct acceptance-criterion mapping; if precise removal is unsafe or a tool keeps recreating them, stop and report the blocker.
9. Hand off only when the audited task delta is the smallest correct change. Summarize each remaining file as `file -> changed behavior -> acceptance criterion -> necessity` so the user can verify the scope directly.

### Architecture Change Gate

Changes to public contracts, SPIs, extension or loading contracts, class or method `final`, visibility, inheritance, constructors or signatures, module dependencies, or shared-code ownership are architecture changes. Before such a change, report:

- current behavior and owner;
- direct-reuse and delegation analysis;
- compatibility impact;
- minimum affected files and tests.

An exact architecture change explicitly requested by the user is authorized after that report. If materially different ownership, architecture, or compatibility choices remain unresolved, obtain confirmation. Adding, changing, or removing an SPI always follows this gate.

If the user rejects a design, stop patching it. Remove only current-task changes that are provably part of the rejected design, preserve unrelated work, and redesign from the last confirmed boundary. Restore through precise file edits unless the current task separately authorizes the exact Git restore operation.

## Task Code Size Limit

Do not add more than 10,000 physical lines across production and test source files in one active task unless the user explicitly authorizes an exact higher limit before the task exceeds it.

Before the first source-code write, estimate the total added source lines required; after every source-code write, measure task-introduced added lines against the active task's original baseline.

Count source output from formatters, generators, scripts, Skills, and later turns of the same task toward the limit; splitting code across files, modules, or turns does not reset or evade it.

If the projected or measured total exceeds the authorized limit, stop before further source-code writes, report the current and projected totals, name every affected path, and provide the smallest independently verifiable decomposition; do not split mechanically or expand the frozen scope without authorization.

## Documentation Wording

Apply this section whenever creating or revising documentation, Skills, prompts, comments, configuration descriptions, or other prose.

- Make each rule understandable to people and Codex on the first reading. State what to inspect, what action to take, what result identifies a problem, and how to verify it; labels such as `authority source`, `constraint strength`, `behavior value`, or `proper ownership` do not replace these instructions.
- State the core rule, fact, or action first. Follow it with necessary applicability conditions, exceptions, and verification methods in that order.
- Use established project technical terms. Do not coin uncommon umbrella terms; when an unavoidable technical term first appears, immediately explain what it means.
- Express one main rule per sentence and do not compress multiple decisions into a difficult long sentence. When one established term fully expresses a requirement, use it instead of enumerating synonymous actions or manifestations.
- Use professional, direct, specific, and concise language. Avoid bureaucratic or academic phrasing, excessive informality, and mechanical shortening that replaces precise technical language with casual wording.
- State each requirement once and merge adjacent statements when one repeats or fully includes another. For Skills, keep the core workflow and reference routing in `SKILL.md`, specific decisions in its referenced files, and concrete rules in `references/rules/`; do not duplicate the same content across files.
- Give every Skill and audit category a distinct, accurately named responsibility. Do not use vague names to hide overlapping responsibilities or duplicate work owned by another specialized Skill.
- Use lists only for genuinely parallel rules, categories, or steps, and use tables only when readers need a column-by-column comparison. Express content that fits in one sentence as prose instead of a field-style list.
- Make each heading name its specific subject and purpose without repeating its parent heading. A title such as `Validation` is insufficient when the section only covers YAML keys. In a short document, omit overview tables, key-point checklists, tables of contents, and repeated summaries that have no independent purpose.
- Keep an exception only when a real allowed case has been verified. State why it is allowed and its exact boundary; do not add speculative or overly broad exceptions.
- Keep an example only when it resolves ambiguity, adds an independent decision condition, or materially helps execution. Otherwise remove it or separate it from the rule.
- Simplification must preserve the original scope, obligation strength, valid exceptions, and verification requirements. Do not shorten mechanically or make readers infer a rule that the original text stated explicitly.
- Keep each new or changed complete sentence on one physical line, and break a line only after punctuation ends the complete sentence. Do not reflow untouched text merely to make formatting consistent. Never modify ASF license text or its internal formatting.
- Before handoff, review every changed prose sentence and rewrite any sentence that does not let a reader directly identify what to inspect, what result is a problem, which exceptions apply, and how to verify it.

## Specialized Workflows

Use the matching repository Skill when its trigger applies:

- Issue diagnosis and copy-ready maintainer replies: `$analyze-issue`.
- Unit-test generation or systematic coverage work: `$gen-ut`. This code-changing workflow composes with `code-implementation`, including its pre-write functional and performance non-regression assessment.
- Use `$review-pr` for PR correctness, side effects, mergeability, CI, discussion replies, and pre-handoff review targeting a PR. Every complete review or recommendation uses Formal Review: `### Result`, one `Review Result: ...` line, `### Evidence`, and `### Coverage`. This includes local candidates; identify local-only changes in `### Coverage`. Use PR Discussion Reply only when explicitly requested. Standalone review is read-only and does not activate `code-implementation`.

If a matching specialized repository Skill is unavailable, apply an equivalent manual checklist, record the fallback in the plan or final response, and continue without installing it. Do not create a new Skill merely to hold task-specific instructions.

Optional cross-cutting Skills remain governed by `.codex/context/cross-cutting-skills.md`. Their use or absence never waives a repository gate, grants authority, or expands the frozen boundary.

## Change Completion Gate

A pre-handoff review is required for every authorized change, build, implement, or fix request, excluding a standalone restoration or rollback governed by its existing gates. After the last write, audit the task delta, complete the applicable verification, and review the effective local candidate against the same code-correctness gates used by `$review-pr`: root-cause and fix mapping, affected behavior, side effects and regressions, contracts and architecture, test validity, and adversarial cases.

Fix every safe in-scope required finding, rerun every invalidated check, and repeat the applicable review. If a finding requires scope expansion, an unresolved architecture choice, or a high-risk action, stop at its existing authorization gate instead of fixing it automatically. Hand off or propose a commit message only after one complete applicable review pass finds zero new required issues.

## Functional and Performance Non-Regression Gate

Before the first relevant code write, assess functional and performance regression risk under `.codex/skills/code-implementation/references/rules/non-regression.md`. When inspection cannot rule out a credible cost increase on an affected existing supported path, measure and freeze a comparable pre-change performance baseline before editing. When inspection rules out that risk, record the concrete reason and do not run a benchmark solely because code changed.

After the last relevant write, apply the same reference before handoff. Any task-introduced loss or weakening of supported functionality outside the exact behavior or contract change required by the acceptance checklist and explicitly authorized for the task blocks handoff. Any task-introduced performance regression on an affected existing supported path also blocks handoff. Verified performance neutrality satisfies this gate for an ordinary feature or correctness change; only an optimization experiment must demonstrate an improvement beyond run-to-run variation.

An improvement in one workload, metric, engine, dialect, or behavior cannot offset a regression in another. Missing, incomparable, unstable, or inconclusive evidence required by the recorded risk classification is not a pass. Repair every safe in-scope regression and rerun invalidated evidence; otherwise stop at the applicable scope or decision gate and report the blocker.
