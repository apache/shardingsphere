---
name: code-implementation
description: "Implement, fix, refactor, or remove repository code under required scope, non-regression, verification, and review gates. Use whenever a task may change a production, test, script, or other implementation artifact, including build logic, generated source, and behavior-affecting configuration. Also use it alongside a specialized code-changing Skill. Do not use for read-only analysis or review, diagnosis-only work, or prose-only changes."
---

# Code Implementation

`AGENTS.md` remains authoritative for scope, permission, safety, architecture, and task lifecycle. Reading this Skill does not grant write, Git, remote, file-type, or scope authority.

## Required References

Read every selected reference through EOF before relying on it. Do not substitute catalog metadata, a summary, another Skill, or ordinary coding judgment for a required repository source.

- For every code-affecting write, read `.codex/skills/code-implementation/references/rules/implementation.md`.
- Before loading the testing rules, use these routing facts: for Maven source trees only `src/main/**` may be the subject of added or updated tests; a change confined to `src/test/**` requires no additional coverage; and a script, configuration, policy, or harness change needs tests only when an existing focused test mechanism protects its behavior. Read `.codex/skills/code-implementation/references/rules/testing.md` before assessing test scenarios for changed `src/main/**` behavior, satisfying requested test or coverage work, or creating or changing a test.
- Read `.codex/skills/code-implementation/references/rules/artifact-removal-and-contract-impact.md` before removing or replacing an owner, classifying an artifact as unused or removable, changing a public or externally visible contract, or modifying an affected snapshot, golden file, registration, loading path, or workflow filter.
- Read `.codex/skills/code-implementation/references/rules/non-regression.md` when the task can affect supported product, build, or runtime behavior or when inspection cannot rule out a credible performance cost increase. For an isolated documentation, policy, or harness change that cannot affect those paths, record that evidence and use its applicable policy or artifact baseline instead.
- Before choosing or running verification and before handoff, read `.codex/skills/code-implementation/references/verification.md`.
- For production, test, script, build-logic, generated-source, or Maven POM changes, read `.codex/skills/coding-standards/SKILL.md` through EOF and use its Implementation Guidance Mode. Do not run its standalone inventory or physical-line audit unless the user explicitly requests that audit.

Specialized repository Skills compose with this Skill when they write code. Their narrower rules add to these rules and do not replace them.

## Workflow

1. Apply the pre-write evidence, acceptance, frozen-boundary, baseline, architecture, and source-line gates in `AGENTS.md`.
   Before the first write, derive the exact `file -> allowed change intent -> unmet acceptance criterion` write allowlist required by the frozen-boundary gate.
   When the current request clearly authorizes the inferred allowlist, record and freeze it without asking the user to repeat the request; otherwise obtain confirmation of the exact allowlist, then record and freeze it before writing.
   Expand the frozen allowlist only after the user explicitly authorizes the exact additional path and change intent.
2. Map every planned production and test change to one unsatisfied acceptance criterion and identify the supported existing paths that the change can affect.
3. Classify functional and performance regression risk before the first relevant write. When the non-regression reference is triggered, capture every required behavioral or performance baseline before editing.
4. Implement the smallest clear change under the implementation rules, and apply the testing rules when their trigger above matches.
5. After each coherent precise edit, inspect every path and hunk the action may have changed. Inspect immediately after a formatter, generator, bulk replacement, or other action whose write set is not exact. Stop at the existing scope or authority gate when a required change lies outside the frozen boundary.
6. Complete this step when Coding Standards applies. After the last implementation write, reapply the Coding Standards Implementation Guidance Mode to affected files and complete its final manual checklist recheck before final verification. Any later write returns the affected files and rules to this step.
7. Run the narrowest meaningful checks under `.codex/skills/code-implementation/references/verification.md`, then verify functional and performance non-regression with evidence proportionate to the classified risk.
8. Complete the repository completion loop, fix every safe in-scope required finding, rerun invalidated checks, and hand off only after a complete review finds no new required issue.

Do not treat a passing build, test, formatter, benchmark, or review tool as proof by itself. Judge the requested behavior, supported existing behavior, contracts, architecture, and affected performance paths directly.
