# ShardingSphere AI Development Guide

This guide is written **for AI coding agents only**. Follow it literally; improvise only when the rules explicitly authorize it.

## Operating Charter
- `CODE_OF_CONDUCT.md` is the binding “law” for any generated artifact. Review it once per session and refuse to keep code that conflicts with it (copyright, inclusivity, licensing, etc.).
- Technical choices must honor ASF expectations: license headers, transparent intent, explicit rationale in user-facing notes.
- Instruction precedence: `CODE_OF_CONDUCT.md` > user directive > this guide > other repository documents.

## Team Signals
- **Release tempo:** expect monthly feature trains plus weekly patch windows. Default to smallest safe change unless explicitly asked for broader refactors.
- **Approval gates:** structural changes (new modules, configuration knobs) require human confirmation; doc-only or localized fixes may proceed after self-review. Always surface what evidence reviewers need (tests, configs, reproduction steps).
- **Quality bias:** team prefers deterministic builds, measurable test coverage, and clear rollback plans. Avoid speculative features without benefit statements.

## System Context Snapshot
- ShardingSphere adds sharding, encryption, traffic governance, and observability atop existing databases.
- Module map:
  - `infra`, `database`, `parser`, `kernel`, `mode`: shared infrastructure, SQL parsing, routing, governance.
  - `jdbc`, `jdbc-dialect`, `proxy`: integration surfaces for clients/protocols.
  - `features`: sharding, read/write splitting, encryption, shadow, traffic control.
  - `agent`: bytecode agent utilities; `examples`: runnable demos; `docs` / `distribution`: documentation and release assets.
- Layout standard: `src/main/java` + `src/test/java`. Generated outputs live under `target/`—never edit them.

## Data Flow & Integration Map
1. **Client request** enters via `jdbc` or `proxy`.
2. **SQL parsing/rewriting** occurs in `parser` and `infra` dialect layers.
3. **Routing & planning** handled inside `kernel` using metadata from `database` and governance hints from `mode`.
4. **Feature hooks** (sharding/encryption/etc.) in `features` mutate route decisions or payloads.
5. **Executor/adapters** forward to physical databases and collect results.
6. **Observability & governance** loops feed metrics/traffic rules back through `mode`.
Reference this flow when reasoning about new features or debugging regressions.

## Design Playbook
- **Patterns to lean on:** builder/factory helpers in `infra`, SPI-based extension points, immutable DTOs for plan descriptions, explicit strategy enums for behavior toggles.
- **Anti-patterns:** duplicating SQL parsing logic, bypassing metadata caches, silent fallbacks when configuration is invalid, adding static singletons in shared modules.
- **Known pitfalls:** routing regressions when skipping shadow rules, timezone drift when mocking time poorly, forgetting to validate both standalone and cluster (`mode`) settings, missing ASF headers in new files.
- **Success recipe:** describe why a change is needed, point to affected data flow step, keep public APIs backwards compatible, and document defaults in `docs`.

## AI Execution Workflow
1. **Intake & Clarify** — restate the ask, map affected modules, confirm sandbox/approval/network constraints.
2. **Plan & Reason** — write a multi-step plan with checkpoints (analysis, edits, tests). Align scope with release tempo (prefer incremental fixes unless told otherwise).
3. **Implement** — touch only necessary files, reuse abstractions, keep ASF headers.
4. **Validate** — choose the smallest meaningful command; if blocked (sandbox, missing deps), explain what would have run and why it matters.
5. **Report** — lead with intent, list edited files with rationale and line references, state verification results, propose next actions.

## Tooling & Verification Matrix

| Command | Purpose | When to run |
| --- | --- | --- |
| `./mvnw clean install -B -T1C -Pcheck` | Full build with Spotless, license, checkstyle gates | Before releasing or when cross-module impact is likely |
| `./mvnw test -pl {module}[-am]` | Unit tests for targeted modules (+ rebuild deps with `-am`) | After touching code in a module |
| `./mvnw spotless:apply -Pcheck [-pl module]` | Auto-format + import ordering | After edits that may violate style |
| `./mvnw spotless:check -Pcheck` | Format check only (fast lint) | When sandbox forbids writes or before pushing |
| `./mvnw test jacoco:check@jacoco-check -Pcoverage-check` | Enforce Jacoco thresholds | When coverage requirements are mentioned or when adding new features |
| `./mvnw -pl {module} -DskipITs -Dspotless.skip=true test` | Quick lint-free smoke (unit tests only) | To shorten feedback loops during iteration |

Always describe command intent before execution and summarize exit codes / key output afterwards.

## Testing Expectations
- Use JUnit 5 + Mockito; tests mirror package paths and follow the `ClassNameTest` convention.
- Method names read `assertXxxCondition`; structure tests as Arrange–Act–Assert sections with explicit separators/comments when clarity drops.
- Mock databases, time, and network boundaries; build POJOs directly.
- When Jacoco fails, open `{module}/target/site/jacoco/index.html`, note uncovered branches, and explain how new tests address them.

## Run & Debug Cookbook
- **Proxy quick start:** `./mvnw -pl proxy -am package` then run `shardingsphere-proxy/bin/start.sh -c conf/server.yaml`. Point configs to samples under `examples/src/resources/conf`.
- **JDBC smoke:** run `./mvnw -pl jdbc -am test -Dtest=YourTest` after wiring datasource configs from `examples`.
- **Config changes:** document defaults in `docs/content` and ensure both standalone (`server.yaml`) and cluster (`mode/`) configs include the new knob.
- **Failure triage:** collect logs under `proxy/logs/`, inspect `target/surefire-reports` for unit tests, and mention relevant error codes/messages in the report.

## Troubleshooting Playbook

| Symptom | Likely cause | AI response pattern |
| --- | --- | --- |
| SQL routed incorrectly or misses shards | Feature rule (shadow/readwrite) skipped, metadata stale, parser mis-dialect | Identify data-flow step impacted (usually `features`/`kernel`), cite configs under `examples`, add reproduction SQL, propose targeted test in `kernel` or `features` |
| `jacoco:check` fails | New code paths lack tests or mocks bypass coverage | Describe uncovered branches from `target/site/jacoco`, add focused unit tests, rerun module-level tests |
| Proxy fails to start | Missing configs, port conflicts, server.yaml mismatch with mode config | Quote exact log snippet, point to `examples` config used, suggest verifying `conf/server.yaml` + `mode` sections, avoid editing generated files |
| Spotless/checkstyle errors | Imports/order mismatched, header missing | Run `./mvnw spotless:apply -Pcheck [-pl module]`, ensure ASF header present, mention command result |
| Integration blocked by sandbox/network | Restricted command or dependency fetch | State attempted command, why it matters, what approval or artifact is needed; wait for explicit user go-ahead |

## AI Collaboration Patterns
- **Prompt templates:**  
  - Change request: “Goal → Constraints → Files suspected → Desired validation.”  
  - Code review: “Observed issue → Impact → Suggested fix.”  
  - Status update: “What changed → Verification → Pending risks.”
- **Anti-pattern prompts:** Avoid vague asks like “optimize stuff” or instructions lacking module names; request clarification instead of guessing.
- **Hand-off checklist:** intent, touched files with reasons, commands run + results, open risks/TODOs, references to issues/PRs if mentioned.
- **Failure responses:** when blocked by sandbox/policy, state the attempted action, why it matters, and what approval or artifact is needed next.

### Module-oriented prompt hints
- **Parser adjustments:** specify dialect, target SQL sample, expected AST changes, and downstream modules consuming the parser output.
- **Kernel routing strategy:** describe metadata shape (table count, binding rules), existing rule config, and which `features` hook participates.
- **Proxy runtime fixes:** include startup command, config file path, observed log lines, and client protocol (MySQL/PostgreSQL).
- **Docs/config updates:** mention audience (user/admin), file paths under `docs/content`, and whether translation or screenshots exist.

## Collaboration & Escalation
- Commit messages use `module: intent` (e.g., `kernel: refine route planner`) and cite why the change exists.
- Reviews focus on risks first (regressions, coverage gaps, configuration impact) before polish.
- If repo state or sandbox limits conflict with `CODE_OF_CONDUCT.md`, stop immediately and request direction—do not attempt workarounds.

## Brevity & Signal
- Prefer tables/bullets over prose walls; cite file paths (`kernel/src/...`) directly.
- Eliminate repeated wording; reference prior sections instead of restating.
- Default to ASCII; only mirror existing non-ASCII content when necessary.

## Reference Pointers
- `CODE_OF_CONDUCT.md` — legal baseline; cite line numbers when flagging violations.
- `CONTRIBUTING.md` — human contributor workflow; reference when mirroring commit/PR styles.
- `docs/content` — user-facing docs; add/update pages when introducing config knobs or behavior changes.
- `examples` configs — canonical samples for proxy/JDBC; always mention which sample you reused.
- `MATURITY.md` / `README.md` — high-level positioning; useful when summarizing project context for reports.
