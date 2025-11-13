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

## Deployment & Topology Snapshot
- **Proxy cluster + registry** (ZooKeeper/Etcd): clients speak MySQL/PostgreSQL to `proxy`; governance data lives in `mode` configs.
- **JDBC embedded**: applications embed `jdbc` driver with sharding/encryption rules shipped via YAML/Spring configs.
- **Hybrid**: traffic governance and observability via `mode` while compute happens inside applications.
Mention which topology you target, the registry used, and any compatibility constraints (e.g., MySQL 5.7 vs 8.0) when proposing changes.

## Design Playbook
- **Patterns to lean on:** builder/factory helpers in `infra`, SPI-based extension points, immutable DTOs for plan descriptions, explicit strategy enums for behavior toggles.
- **Anti-patterns:** duplicating SQL parsing logic, bypassing metadata caches, silent fallbacks when configuration is invalid, adding static singletons in shared modules.
- **Known pitfalls:** routing regressions when skipping shadow rules, timezone drift when mocking time poorly, forgetting to validate both standalone and cluster (`mode`) settings, missing ASF headers in new files.
- **Success recipe:** describe why a change is needed, point to affected data flow step, keep public APIs backwards compatible, and document defaults in `docs`.
- **Case in point:** a prior shadow-rule regression was fixed by (1) reproducing via `proxy` + sample config, (2) adding a `kernel` unit test covering the skipped branch, (3) updating docs with the exact YAML flag—mirror that discipline for new features.

## AI Execution Workflow
1. **Intake & Clarify** — restate the ask, map affected modules, confirm sandbox/approval/network constraints.
2. **Plan & Reason** — write a multi-step plan with checkpoints (analysis, edits, tests). Align scope with release tempo (prefer incremental fixes unless told otherwise).
3. **Implement** — touch only necessary files, reuse abstractions, keep ASF headers.
4. **Validate** — choose the smallest meaningful command, announce the intent before execution, summarize exit codes afterward; if blocked (sandbox, missing deps), explain what would have run and why it matters.
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
| `./mvnw -pl {module} -DskipTests spotbugs:check checkstyle:check` | Static analysis without running tests | Before landing riskier refactors or when reviewing user patches |
| `./mvnw -pl proxy -am -DskipTests package && shardingsphere-proxy/bin/start.sh -c conf/perf.yaml` | Proxy packaging + lightweight perf smoke | When change may influence runtime characteristics; capture QPS/latency deltas |

## Testing Expectations
- Use JUnit 5 + Mockito; tests mirror package paths and follow the `ClassNameTest` convention.
- Method names read `assertXxxCondition`; structure tests as Arrange–Act–Assert sections with explicit separators/comments when clarity drops.
- Mock databases, time, and network boundaries; build POJOs directly.
- When Jacoco fails, open `{module}/target/site/jacoco/index.html`, note uncovered branches, and explain how new tests address them.
- Need a quick coverage view? Run `./mvnw -pl {module} -am -Djacoco.skip=false test jacoco:report` and open `{module}/target/site/jacoco/index.html`.

### Unit Test Style Recap
- Mirror production package paths, keep tests named `ClassNameTest`, and express assertions through `assertXxxCondition` methods.
- Follow a strict Arrange–Act–Assert narrative; add inline separators/comments only when complex flows would otherwise be unclear.
- Prefer Mockito mocks over heavyweight fixtures for external systems (DB, time, network) while directly instantiating simple POJOs.
- Validate with module-scoped Maven commands such as `./mvnw test -pl {module} -am`, then inspect Jacoco reports to plug remaining coverage gaps.

### Test Request Auto-Directives
- Whenever the AI detects that a task involves authoring or updating unit tests, it must automatically:
  1. Apply the style and dependency rules above (JUnit5 + Mockito, `ClassNameTest`, `assertXxxCondition`, AAA structure, external dependencies mocked).
  2. Design tests that pass on the first execution and reason about them using `./mvnw test -pl {module} -am` (or an equivalent command); when fixtures grow heavy, prefer mocks and document the trade-off.
  3. Target 100% statement and branch coverage for the relevant classes and paths, running `./mvnw test jacoco:check@jacoco-check -Pcoverage-check` when needed and summarizing coverage in the report.
  4. Leave truly unreachable dead code uncovered only if the report lists the file and line numbers, explains why it is unreachable, and states whether cleanup is recommended.

### How To Ask Me (Tests)
- "Implement or update unit tests for {class|scenario} following the AGENTS.md testing guidelines."
- "Ensure the tests pass in one shot; reason against `./mvnw test -pl {module} -am` (or an equivalent command) and describe the mock/fixture strategy."
- "Cover every branch of {module}/{class}, run `./mvnw test jacoco:check@jacoco-check -Pcoverage-check`, and report the coverage results."
- "If dead code remains uncovered, explain the reason and cite the exact location."

## Run, Debug & Triage

| Scenario | How to run / inspect | AI response pattern |
| --- | --- | --- |
| Proxy quick start | `./mvnw -pl proxy -am package`; run `shardingsphere-proxy/bin/start.sh -c conf/server.yaml` using configs from `examples/src/resources/conf` | Record command + exit code, cite config path, include protocol info if issues arise |
| JDBC smoke | `./mvnw -pl jdbc -am test -Dtest=YourTest` with datasource configs copied from `examples` | Note which test ran, describe datasource setup, mention log excerpts on failure |
| Config change validation | Update both standalone `server.yaml` and cluster `mode/` configs; document defaults under `docs/content` | Explain affected deployment mode(s), show sample snippet, list docs touched |
| Failure triage | Gather `proxy/logs/` and `target/surefire-reports`; capture error codes/messages | Quote relevant log lines, map them to data-flow steps, propose next diagnostic |
| SQL routes wrong/missing shards | Check feature rule configs, metadata freshness, parser dialect | Provide reproduction SQL + config snippet, point to impacted module (`features`/`kernel`), add/plan targeted tests |
| `jacoco:check` fails | Review `{module}/target/site/jacoco` for uncovered branches | Describe uncovered branch, add focused unit tests, rerun module tests |
| Proxy won’t start | Validate `conf/server.yaml`, mode settings, port conflicts; reuse configs from `examples` | Share exact log snippet, list configs inspected, suggest fix without editing generated files |
| Spotless/checkstyle errors | Run `./mvnw spotless:apply -Pcheck [-pl module]` (or `spotless:check` in read-only) | Mention command result, confirm ASF header/import order adjustments |
| Sandbox/network block | Command denied due to sandbox or dependency fetch | State attempted command + purpose, ask for approval or alternative artifact |

## Compatibility, Performance & External Systems
- **Database/protocol support:** note targeted engines (MySQL 5.7/8.0, PostgreSQL 13+, openGauss, etc.) and ensure new behavior stays backward compatible; link to affected dialect files.
- **Registry & config centers:** `mode` integrates with ZooKeeper, Etcd, Consul; describe tested registry and highlight compatibility risks when editing governance logic.
- **Metrics/observability:** mention if changes touch agent plugins, Prometheus exporters, or tracing hooks; reference the module (`agent`, `infra/metrics`, `docs/content/dev-guide/observability`).
- **Performance guardrails:** when runtime impact is possible, capture baseline vs new latency/QPS from perf smoke and include CPU/memory observations.

## AI Collaboration Patterns

| Scenario | Include in the response |
| --- | --- |
| Change request | Goal, constraints, suspected files/modules, planned validation command |
| Code review | Observed issue, impact/risk, suggested fix or follow-up test |
| Status update | Work done, verification status (commands + exit codes), remaining risks/TODOs |
| Failure/blocker | Command attempted, why it failed (sandbox/policy/log), approval or artifact required next |

- **Anti-patterns:** decline vague orders (e.g., “optimize stuff”) and ask for module + expected outcome instead of guessing.
- **Hand-off checklist:** intent, edited files + rationale, executed commands/results, open risks, related issues/PRs.

## Release & Rollback Rituals
1. **Pre-change:** restate why current release train needs the change, cite relevant issue/PR/decision doc, note affected modules and configs.
2. **Post-implementation:** record tests + perf smokes, confirm Spotless/Jacoco/static analysis status, update docs/configs, flag translation/backport tasks if docs span multiple locales.
3. **Rollback-ready:** describe simple revert steps (e.g., disable knob, revert YAML, roll back module), list files to touch, and mention how to confirm rollback success.
4. **Communication:** include release note snippets or doc anchor updates so reviewers can copy/paste.

## Collaboration & Escalation
- Commit messages use `module: intent` (e.g., `kernel: refine route planner`) and cite why the change exists.
- Reviews focus on risks first (regressions, coverage gaps, configuration impact) before polish.
- Approval request template: “Command → Purpose → Sandbox limitation/side effects → Expected output”; include why escalation is justified.
- If repo state or sandbox limits conflict with `CODE_OF_CONDUCT.md`, stop immediately and request direction—do not attempt workarounds.

## Prompt & Reference Quick Sheet

| Area | Include when prompting/reporting | Key references |
| --- | --- | --- |
| Parser / dialect | Target database version, sample SQL, expected AST deltas, downstream module depending on result | `parser/src/**`, `docs/content/dev-guide/sql-parser` |
| Kernel routing & features | Metadata shape (tables, binding, sharding/encryption rules), knob values, impacted `features` hook | `kernel/src/**`, `features/**`, configs under `examples` |
| Proxy runtime & governance | Startup command, `conf/server.yaml`, registry/mode config, log excerpts, client protocol | `proxy/**`, `mode/**`, `examples/src/resources/conf`, `docs/content/dev-guide/proxy` |
| Observability/agent | Metrics/tracing plugin touched, expected signal format, dashboards affected | `agent/**`, `infra/metrics/**`, `docs/content/dev-guide/observability` |
| Docs/config updates | Audience (user/admin), file path, translation needs, screenshots/assets | `docs/content/**`, `README*.md`, translation directories |
| Process & releases | Commit/PR intent, release-train notes, maturity level, rollback strategy | `CONTRIBUTING.md`, `MATURITY.md`, `README.md` |
| Compliance & conduct | Any risk of license header, third-party code, inclusive language issues | `CODE_OF_CONDUCT.md`, `LICENSE`, `NOTICE` |

## Mocking Guidelines
- Prefer Mockito mocks over bespoke test fixtures; avoid introducing new fixture classes unless a scenario cannot be expressed with mocks.
- When different rule/attribute types must be distinguished, declare marker interfaces and mock them with Mockito instead of writing concrete fixture implementations.
- If the project already exposes a suitable interface (e.g., `ShardingSphereRule`), create distinct Mockito mocks directly (`mock(MyRule.class)`), and only introduce marker interfaces when no existing type can represent the scenario.
- Test method names must reference the production method under test (e.g., `assertGetInUsedStorageUnitNameAndRulesMapWhen...`) so intent is unambiguous to reviewers.
- Never use reflection to test private helpers; unit tests must target public APIs. If a private branch is unreachable via public methods, document it (file + reason) instead of adding a reflective test.
- Mock dependencies that need heavy external environments (database, cache, registry, network) instead of provisioning real instances.
- When constructing an object requires more than two nested builders/factories, prefer mocking the object.
- If an SPI returns an object that is not the subject under test, use `mockStatic` (or equivalent) to provide canned behavior.
- When only a few properties of a complex object are used, mock it rather than assembling the full graph.
- Do not mock simple objects that can be instantiated directly with `new`.
- Do not enable Mockito’s `RETURNS_DEEP_STUBS` unless unavoidable chained interactions make explicit stubs impractical; if you must enable it, mention the justification in the test description.

## AI Self-Check Checklist (Pre-Submission Must-Do)
1. Instruction precedence: `CODE_OF_CONDUCT.md` → user request → this guide → other docs. Are any conflicts unresolved?
2. Are edited files minimal, include ASF headers, and pass Spotless?
3. Do all semantic changes have corresponding tests (or a rationale if not) with commands/exit codes recorded?
4. Does run/triage information cite real file paths plus log/config snippets?
5. Does the report list touched files, verification results, known risks, and recommended next steps?
6. For new or updated tests, did you inspect the target production code paths, enumerate the branches being covered, and explain that in your answer?
7. Before finishing, did you re-check the latest verification command succeeded (rerun if needed) so the final state is green?

## Brevity & Signal
- Prefer tables/bullets over prose walls; cite file paths (`kernel/src/...`) directly.
- Eliminate repeated wording; reference prior sections instead of restating.
- Default to ASCII; only mirror existing non-ASCII content when necessary.
