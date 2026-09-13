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
- Read `.codex/skills/code-implementation/references/rules/non-regression.md` when a change affects a public or shared contract, crosses behavior owners, or has credible functional or performance risk. Otherwise preserve supported behavior through the implementation rules and focused owner checks without loading the full reference.
- Read `.codex/skills/code-implementation/references/verification.md` before the first repository check and reuse it while the task state is unchanged.
- For production or test Java and Maven POM changes, read `.codex/skills/coding-standards/SKILL.md` through EOF and use its Implementation Guidance Mode. For other artifacts, inspect only written standards that govern the changed files. Do not run the standalone inventory or physical-line audit unless the user requests that audit.

Specialized repository Skills compose with this Skill when they write code. Their narrower rules add to these rules and do not replace them.

## Workflow

1. Apply the pre-write gates in `AGENTS.md` and freeze `file -> allowed change intent -> unmet acceptance criterion`. Infer clearly authorized paths without asking the user to repeat the request; expand only with exact additional path and intent authorization.
2. Map every planned production and test change to one unsatisfied acceptance criterion and identify the supported existing paths that the change can affect.
3. Classify functional and performance risk before the first relevant write. Preserve supported behavior outside the exact requested change; when the full non-regression reference is triggered, capture its required baseline before editing.
4. Implement the smallest clear change under the implementation rules, and apply the testing rules when their trigger above matches.
5. After each coherent edit batch, inspect every changed path and hunk. Inspect immediately after a formatter, generator, bulk replacement, or another action whose write set is not exact. Stop at the existing scope or authority gate when a required change lies outside the frozen boundary.
6. Complete this step when Coding Standards applies. After the last implementation write, reapply the Coding Standards Implementation Guidance Mode to affected files and complete its final manual checklist recheck before final verification. Any later write returns the affected files and rules to this step.
7. Run the narrowest meaningful checks under the verification rules. Reuse a passing result while its task state and evidence remain unchanged; rerun only checks invalidated by a relevant write, failure, scope change, or new evidence.
8. After verification, follow `.codex/context/change-completion.md` as the sole completion-loop orchestration owner.

Do not treat a passing build, test, formatter, benchmark, or review tool as proof by itself. Judge the requested behavior, supported existing behavior, contracts, architecture, and affected performance paths directly.
