# ShardingSphere Planning Notes

Use this file to keep planning context, progress checkpoints, and decision history for work in this repository.

## Purpose / Big Picture
- Keep quality-first changes aligned with AGENTS.md and CODE_OF_CONDUCT.md while minimizing blast radius.
- Anchor work to ShardingSphere’s layers (infra/database/parser/kernel/mode/jdbc/proxy/features/agent) and prefer scoped module changes.
- Make every plan actionable: commands, expected outputs, and verification steps must be explicit and copy-pasteable.

## Progress
1. Confirm scope, constraints, and sandbox/approval settings; restate the user goal in plain terms.
2. Inspect relevant modules and tests before coding; list branches/edges that need coverage.
3. Implement the smallest safe change, deleting dead code and keeping variables near first use.
4. Verify with targeted Maven commands (e.g., `./mvnw test -pl <module>[-am]` or `./mvnw checkstyle:check -Pcheck`) and capture exit codes.
5. Summarize intent, edits, verification, and risks; propose next actions only when they are natural follow-ups.

## Decision Log
- Established PLANS.md as the canonical planning reference linked from AGENTS.md for Codex readiness checks.
- Default workflow: run codex-readiness in read-only first; switch to execute mode with a reviewed plan.json when command execution is required.
- Keep planning notes in this file rather than scattering across threads to satisfy traceability expectations.

## Outcomes & Retrospective
- Record each session’s outputs (commands + exit codes, test scopes, notable fixes) and any remaining risks or TODOs.
- Note when coverage, Spotless, or Checkstyle were skipped and which commands to run later.
- Capture rollback considerations (e.g., config toggles or revert paths) when changes touch runtime behavior.

## Surprises & Discoveries
- Codex readiness requires a plan-named markdown or planning headings; keep this file updated to avoid failing deterministic checks.
- Read-only mode leaves execution checks NOT_RUN; execute mode needs a confirmed plan.json and should be limited to scoped commands.
- AGENTS.md line limit (<=300) and formatting expectations apply here too; avoid placeholders and keep entries concise.
