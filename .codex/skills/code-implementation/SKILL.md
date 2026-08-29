---
name: code-implementation
description: "Implement, fix, refactor, or remove repository code under required scope, non-regression, verification, and review gates. Use whenever a task may change a production, test, script, or other implementation artifact, including build logic, generated source, and behavior-affecting configuration. Also use it alongside a specialized code-changing Skill. Do not use for read-only analysis or review, diagnosis-only work, or prose-only changes."
---

# Code Implementation

`AGENTS.md` remains authoritative for scope, permission, safety, architecture, and task lifecycle. Reading this Skill does not grant write, Git, remote, file-type, or scope authority.

## Required References

Read every selected reference through EOF before relying on it. Do not substitute catalog metadata, a summary, another Skill, or ordinary coding judgment for a required repository source.

- For every code-affecting write, read `.codex/skills/code-implementation/references/rules/implementation.md`, `.codex/skills/code-implementation/references/rules/contracts-and-removal.md`, and `.codex/skills/code-implementation/references/rules/non-regression.md`. Apply each contract, impact, or removal rule only when its own trigger matches.
- Before deciding whether tests or test support are required, and before creating or changing them, read `.codex/skills/code-implementation/references/rules/testing.md`.
- Before choosing or running verification and before handoff, read `.codex/skills/code-implementation/references/verification.md`.
- For every implementation, fix, or refactor, read `.codex/skills/coding-standards/SKILL.md` through EOF and use its Implementation Guidance Mode. Do not run its standalone inventory or physical-line audit unless the user explicitly requests that audit.

Specialized repository Skills compose with this Skill when they write code. Their narrower rules add to these rules and do not replace them.

## Workflow

1. Apply the pre-write evidence, acceptance, frozen-boundary, baseline, architecture, and source-line gates in `AGENTS.md`.
2. Map every planned production and test change to one unsatisfied acceptance criterion and identify the supported existing paths that the change can affect.
3. Classify functional and performance regression risk before the first relevant write. Capture any required behavioral or performance baseline using the protocol in `.codex/skills/code-implementation/references/rules/non-regression.md`.
4. Implement the smallest clear change under `.codex/skills/code-implementation/references/rules/implementation.md`, and apply `.codex/skills/code-implementation/references/rules/testing.md` to every affected test decision.
5. After each write, inspect every path and hunk the action may have changed. Stop at the existing scope or authority gate when a required change lies outside the frozen boundary.
6. Run the narrowest meaningful checks under `.codex/skills/code-implementation/references/verification.md`, then verify functional and performance non-regression with evidence proportionate to the classified risk.
7. Complete the repository completion loop, fix every safe in-scope required finding, rerun invalidated checks, and hand off only after a complete review finds no new required issue.

Do not treat a passing build, test, formatter, benchmark, or review tool as proof by itself. Judge the requested behavior, supported existing behavior, contracts, architecture, and affected performance paths directly.
