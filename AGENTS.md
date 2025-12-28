# ShardingSphere AI Development Guide

This guide is written **for AI coding agents only**. Follow it literally; improvise only when the rules explicitly authorize it.

## Core Immutable Principles

1. **Quality First**: code quality and system security are non-negotiable.
2. **Think Before Action**: perform deep analysis and planning before coding.
3. **Tools First**: prioritize the proven best toolchain.
4. **Transparent Records**: keep every key decision and change traceable.
5. **Continuous Improvement**: learn from each execution and keep optimizing.
6. **Results Oriented**: judge success solely by whether the target is achieved.
7. **Coding Standards**: `CODE_OF_CONDUCT.md` is the binding “law”; use it as the first reference for rule interpretation and cite relevant lines. See Governance Basics for precedence and session review expectations.

## Quality Standards

### Engineering Principles
- **Architecture**: follow SOLID, DRY, separation of concerns, and YAGNI (build only what you need).
- **Code Quality**:
    - Use clear naming and reasonable abstractions.
    - Delete unused code; when changing functionality, remove legacy compatibility shims.
    - Keep variable declarations adjacent to first use; if a value must be retained, declare it `final` to satisfy Checkstyle VariableDeclarationUsageDistance.
- **Complete Implementation**: no MVPs/placeholders/TODOs—deliver fully runnable solutions.

### Performance Standards
- **Algorithm Awareness**: account for time and space complexity.
- **Resource Management**: optimize memory usage and I/O behavior.
- **Boundary Handling**: cover exceptional situations and edge conditions.

### Testing Requirements
- **Test-Driven**: design for testability, ensure unit-test coverage, and keep background unit tests under 60s to avoid job stalls.
- **Quality Assurance**: run static checks, formatting, and code reviews.
- **Checkstyle Gate**: do not hand off code with Checkstyle/Spotless failures—run the relevant module check locally and fix before completion.
- **Continuous Verification**: rely on automated tests and integration validation.
- **Test Naming Simplicity**: keep test names concise and scenario-focused (avoid “ReturnsXXX”/overly wordy or AI-like phrasing); describe the scenario directly.
- **Coverage Discipline**: follow the dedicated coverage & branch checklist before coding when coverage targets are stated.
- **Mocking Rule**: default to mocks; see Mocking & SPI Guidance for static/constructor mocking and spy avoidance details.
## Tool Usage Guide

### Exa - Web Search
**Purpose**: fetch the latest web information, official links, or announcements.
**When to Trigger**
- Needing current events, announcements, or security advisories.
- Looking up official website entry points.
- Verifying external information sources.
**Key Parameters**
- Keywords: ≤ 12.
- `web_search_exa`: moderate.

### mcp-deepwiki
- Deep knowledge aggregation.
  **Purpose**: deep semantic document retrieval, knowledge aggregation, and multi-source summarization.
  **When to Trigger**
    - Explaining technical concepts or contrasting standards.
    - Describing algorithm principles.
    - Integrating multiple official sources.
      **Key Parameters**
    - `topic`: technical topic or concept (e.g., "adaptive servo control").
    - `depth`: 1-3 to control semantic layers.

## ⚠️ Dangerous Operation Confirmation Mechanism

### High-Risk Operation Checklist—obtain explicit confirmation **before** doing any of the following:
- **File System**: deleting files/directories, bulk edits, or moving system files.
- **Code Submission**: `git commit`, `git push`, `git reset --hard`.
- **System Configuration**: editing environment variables, system settings, or permissions.
- **Data Operations**: dropping databases, changing schemas, or running batch updates.
- **Network Requests**: sending sensitive data or calling production APIs.
- **Package Management**: global installs/uninstalls or updating core dependencies.

### Confirmation Template

⚠️ Dangerous operation detected! Operation type: [specific action] Scope of impact: [affected area] Risk assessment: [potential consequence] Please confirm whether to continue. [Requires explicit “yes”, “confirm”, or “proceed”]

## Workflow
- Use Sequential Thinking when tasks need decomposition: 6-10 steps (fallback 3-5), one sentence each, actionable.
- Intake: choose the strategy for the task, confirm tool availability/fallbacks, and capture constraints (forbidden APIs, output format, coverage/test expectations).
- Plan: inspect existing code with tools before edits, finish the plan before coding, and set the quality/verification bar.
- Implement: keep scope minimal, follow quality standards, record decisions, and handle edge cases; honor instruction precedence from Core Principle #7.
- Validate: run the narrowest meaningful checks (see Verification & Commands) and prefer scoped runs; note any sandbox or limit blocks and alternatives.
- Report & self-check: share intent, edits, verification results, and next steps; ensure all required instructions, coverage, and mocking rules are satisfied, with remaining risks called out.

## Coverage & Branch Checklist
- When coverage targets are declared (including 100%), list every branch/path with its planned test before coding.
- Map each branch to exactly one test; add cases until all declared branches are covered or explicitly waived.
- For utilities with multiple return paths, record the branch list and update it if the code changes.
- Use Jacoco to confirm expectations when coverage is in question; document any unreachable code instead of adding redundant tests.

## Response Style

### Language and Tone
- **Friendly and Natural**: interact like a professional peer; avoid stiff formal language.
- **Use Light Accents**: prepend headings or bullets with emojis such as ✨⚠️ to highlight key points.
- **Hit the Point Fast**: start with a sentence that captures the core idea, especially for complex problems.

### Content Organization
- **Hierarchical**: separate sections with headings/subheadings; split long content into sections.
- **Focused Bullets**: break long paragraphs into short sentences or bullets, each covering a single idea.
- **Logical Flow**: use ordered lists for multi-step work (1. 2. 3.) and unordered lists for peers (- or *).
- **Proper Spacing**: keep blank lines or `---` between blocks to boost readability.
> ❌ Avoid complex tables in the terminal (especially for long, code-heavy, or narrative content).

### Visual & Layout Optimization
- **Keep It Simple**: limit each line length to ≤200 characters.
- **Leave White Space**: use blank lines wisely to avoid cramped output.
- **Align Consistently**: stick to one indentation and bullet style (use `-` instead of mixing symbols).
- **Emphasize Key Points**: highlight critical items with **bold** or *italics*.

### Technical Content Guidelines

#### Code & Data Presentation
- **Code Blocks**: wrap multi-line code/config/logs inside Markdown fences with language hints (e.g., a fenced Java block).
- **Focus on the Core**: trim unrelated snippets (like imports) to spotlight essential logic.
- **Diff Markers**: show changes with `+` / `-` for quick scanning.
- **Line Numbers**: add them when needed (e.g., debugging scenarios).

#### Structured Data
- **Prefer Lists**: use lists over tables in most cases.
- **Tables Sparingly**: only use Markdown tables when strict alignment is required (e.g., parameter comparisons).

### Interaction & User Experience
- **Immediate Feedback**: respond quickly; avoid long silent periods.
- **Visible Status**: surface progress for important actions (e.g., “Processing...”).
- **Friendly Errors**: clearly explain failures and suggest actionable fixes.

### ✅ Ending Suggestions
- Append a **short summary** after complex content to reiterate the core points.
- **Guide the Next Step**: close with actionable advice, instructions, or an invitation for follow-up questions.

### Brevity & Signal
- Prefer tables/bullets over prose walls; cite file paths (`kernel/src/...`) directly.
- Eliminate repeated wording; reference prior sections instead of restating.
- Default to ASCII; only mirror existing non-ASCII content when necessary.

## Governance Basics
- Follow the instruction order from Core Principle #7 and surface conflicts with rationale when they arise.
- Technical choices must satisfy ASF transparency: include license headers, document intent, and keep rationales visible to reviewers.
- Default to the smallest safe change: monthly feature trains plus weekly patch windows reward incremental fixes unless the product requires deeper refactors.
- Secure approvals for structural changes (new modules, configs, knobs); localized doc or code tweaks may land after self-review when you surface the evidence reviewers expect (tests, configs, reproduction steps).
- Maintain deterministic builds, measurable coverage, and clear rollback notes; avoid speculative work without a benefit statement.

## Platform Snapshot
- ShardingSphere layers sharding, encryption, traffic governance, and observability on top of existing databases.
- Module map summary:
  - `infra`/`database`/`parser`/`kernel`/`mode`: shared infrastructure for SQL parsing, routing, and governance metadata.
  - `jdbc`/`jdbc-dialect`/`proxy`: integration points for clients and protocols.
  - `features`: sharding, read/write splitting, encryption, shadow, traffic control.
  - `agent`: bytecode helpers; `examples`, `docs`, `distribution`: runnable demos and release assets.
- Layout standard: `src/main/java` + `src/test/java`; anything under `target/` is generated and must not be edited.

### Data Flow & Integration Map
1. **Client request** enters via `jdbc` or `proxy`.
2. **SQL parsing/rewriting** happens in `parser` plus `infra` dialect layers.
3. **Routing & planning** runs inside `kernel` using metadata from `database` and hints from `mode`.
4. **Feature hooks** (sharding/encryption/etc.) in `features` adjust routes or payloads.
5. **Executor/adapters** send work to physical databases and gather results.
6. **Observability & governance** loops feed metrics/traffic rules back through `mode`.
Use this flow to justify design changes and to debug regressions.

### Deployment Modes
- **Proxy cluster + registry** (ZooKeeper/Etcd): clients speak MySQL/PostgreSQL to `proxy`; governance state resides in `mode`.
- **JDBC embedded**: applications embed the `jdbc` driver with YAML/Spring configs describing sharding and encryption.
- **Hybrid**: compute happens in applications while governance/observability leverage `mode`.
Always state which topology, registry, and engine versions (e.g., MySQL 5.7 vs 8.0) your change targets.

## Design Playbook
- **Preferred styles:** elegant, minimal solutions that keep methods/tests lean, use guard clauses, and delete dead code immediately.
- **Patterns to lean on:** builders/factories from `infra`, SPI-driven extensions, immutable DTOs for plan descriptions, explicit strategy enums.
- **Anti-patterns:** duplicating parsing logic, bypassing metadata caches, silently accepting invalid configs, static singletons in shared modules, or overbuilt helpers.
- **Known pitfalls:** routing regressions when shadow rules are skipped, timezone drift from poor time-mocking, forgetting standalone vs cluster (`mode`) validation, missing ASF headers, Mockito inline mocks breaking on JDKs that block self-attach.
- **Success recipe:** explain why the change exists, cite the affected data-flow step, keep public APIs backward compatible, and record defaults/knobs alongside code changes.

## Verification & Commands
- Core commands: `./mvnw clean install -B -T1C -Pcheck` (full build), `./mvnw test -pl <module>[-am]` (scoped unit tests), `./mvnw spotless:apply -Pcheck [-pl <module>]` (format), `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=ClassName test` (fast verification), `./mvnw -pl proxy -am -DskipTests package` (proxy packaging/perf smoke).
- Coverage: when tests change or targets demand it, run `./mvnw test jacoco:check@jacoco-check -Pcoverage-check` or scoped `-pl <module> -am -Djacoco.skip=false test jacoco:report`; pair with the Coverage & Branch Checklist.
- Style: `./mvnw checkstyle:check -Pcheck` (scoped with `-pl <module> -am -Pcheck` when possible) unless told otherwise.
- Scoped defaults: prefer module-scoped runs over whole-repo builds; include `-Dsurefire.failIfNoSpecifiedTests=false` when targeting specific tests.
- Testing ground rules: JUnit 5 + Mockito, `ClassNameTest` naming, Arrange–Act–Assert, mock external systems/time/network, reset static caches, and reuse swappers/helpers for complex configs.
- API bans: if a user forbids a tool/assertion, add it to the plan, avoid it during implementation, and cite verification searches (e.g., `rg assertEquals`) in the final report.

## Run & Triage Quick Sheet
- **Proxy quick start:** `./mvnw -pl proxy -am package` then `shardingsphere-proxy/bin/start.sh -c conf/server.yaml`; report command, exit code, config path, and protocol.
- **JDBC smoke:** `./mvnw -pl jdbc -am test -Dtest=YourTest` with datasource configs from `examples`; note test name, datasource setup, and failure logs.
- **Config validation:** update standalone `server.yaml` and cluster `mode/` configs together; call out defaults and any edits that affect both.
- **Failure triage:** collect `proxy/logs/` plus `target/surefire-reports`, quote the relevant log lines, map them to the data-flow step, and propose the next diagnostic.
- **Routing mistakes:** check feature-rule configs, metadata freshness, and parser dialect; include SQL + config snippet plus impacted module (`features` or `kernel`), and add/plan targeted tests.
- **Proxy won’t start:** verify configs/mode/ports and reuse known-good example configs; share the log snippet and files inspected without editing generated artifacts.
- **Sandbox/network block:** if a command is denied, state what you ran, why it failed, and the approval or alternative plan required.
- **Single-module tests:** prefer scoped commands over repo-wide runs; use fully-qualified test class names and append `-Dsurefire.failIfNoSpecifiedTests=false` when targeting specific tests (see Verification & Commands for flags).

## Compatibility, Performance & External Systems
- Specify targeted engines and dialect files (MySQL 5.7/8.0, PostgreSQL 13+, openGauss, etc.) and guarantee backward-compatible behavior.
- Call out registry/config-center expectations (ZooKeeper, Etcd, Consul) and note risks when governance logic changes.
- Mention observability or agent impacts (metrics exporters, tracing hooks) whenever touched.
- Capture performance guardrails—baseline vs new latency/QPS, CPU, memory observations—when runtime paths change.

## Collaboration Patterns & Rituals
- **Response focus:** for change requests list goal/constraints/suspected files/validation plan; for code reviews highlight issues + risks + suggested fixes; for status updates summarize completed work + verification + risks/TODOs; for failures/blockers share attempted command, reason for failure, and what you need next.
- **Anti-patterns:** do not accept vague orders (“optimize stuff”); always clarify module + expected result.
- **Hand-off checklist:** intent, edited files, rationale, executed commands (with exit codes), open risks, related issues/PRs.
- **Release & rollback:** (1) restate why the release needs the change and cite affected modules/configs, (2) after implementation capture tests/perf smokes and document Spotless/Jacoco/static results plus translation/backport tasks, (3) outline rollback steps (disable knob, revert YAML/module) and how to confirm success, (4) prep release-note snippets or anchor updates for reviewers.
- **Escalation etiquette:** commit messages use `module: intent`, reviews prioritize regression risks, approval requests follow “Command → Purpose → Sandbox limitation → Expected output,” and halt for guidance whenever sandbox limits conflict with `CODE_OF_CONDUCT.md`.

## Prompt & Reference Snapshot
- **Parser / dialect:** include target database version, sample SQL, expected AST deltas, and downstream modules relying on the result.
- **Kernel routing & features:** describe metadata shape (tables, binding, sharding/encryption rules), knob values, and which `features` hook processes the change.
- **Proxy runtime & governance:** state startup command, `conf/server.yaml`, registry/mode config, relevant logs, and client protocol.
- **Observability / agent:** mention metrics or tracing plugins touched plus expected signal format and dashboards affected.
- **Docs / config updates:** specify audience (user/admin), file path, translation implications, and any screenshots/assets added.
- **Process & releases:** cite commit/PR intent, release-train context, maturity level, and rollback plan.
- **Compliance & conduct:** flag license-header risk, third-party code usage, and inclusive language considerations.

## Mocking & SPI Guidance
- Favor Mockito over bespoke fixtures; only add new fixture classes when mocks cannot express the scenario.
- Use marker interfaces when distinct rule/attribute types are needed; reuse SPI types such as `ShardingSphereRule` where possible.
- Name tests after the production method under test; never probe private helpers directly—document unreachable branches instead.
- Mock heavy dependencies (database/cache/registry/network) and prefer mocking over building deep object graphs.
- For static/constructor mocking, use `@ExtendWith(AutoMockExtension.class)` with `@StaticMockSettings`; avoid hand-written `mockStatic`/`mockConstruction` unless you documented why the extension cannot be used.
- When constructors hide collaborators, use `Plugins.getMemberAccessor()` to inject mocks and document why SPI creation is bypassed.
- When static methods or constructors need mocking, prefer `@ExtendWith(AutoMockExtension.class)` with `@StaticMockSettings` (or the extension’s constructor-mocking support); when a class is listed in `@StaticMockSettings`, do not call `mockStatic`/`mockConstruction` directly—stub via `when(...)` instead. Only if AutoMockExtension cannot be used and the reason is documented in the plan may you fall back to `mockStatic`/`mockConstruction`, wrapped in try-with-resources.
- Before coding tests, follow the Coverage & Branch Checklist to map inputs/branches to planned assertions.
- When a component is available via SPI (e.g., `TypedSPILoader`, `DatabaseTypedSPILoader`, `PushDownMetaDataRefresher`), obtain the instance through SPI by default; note any exceptions in the plan.
- If the class under test implements `TypedSPI` or `DatabaseTypedSPI`, instantiate it via `TypedSPILoader` or `DatabaseTypedSPILoader` instead of calling `new` directly.
- Do not mix Mockito matchers with raw arguments; choose a single style per invocation, and ensure the Mockito extension aligns with the mocking approach.
- Compliance is mandatory: before any coding, re-read AGENTS.md and convert all hard requirements (SPI usage, no FQCN, mocking rules, coverage targets, planning steps) into a checklist in the plan; do not proceed or report completion until every item is satisfied or explicitly waived by the user.

## Session Notes
- MySQLSchemataQueryExecutorFactoryTest：public 方法分别测试，`accept` 与 `newInstance` 各自使用独立测试方法。
