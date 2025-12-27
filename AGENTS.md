# ShardingSphere AI Development Guide

This guide is written **for AI coding agents only**. Follow it literally; improvise only when the rules explicitly authorize it.

## Core Immutable Principles

1. **Quality First**: code quality and system security are non-negotiable.
2. **Think Before Action**: perform deep analysis and planning before coding.
3. **Tools First**: prioritize the proven best toolchain.
4. **Transparent Records**: keep every key decision and change traceable.
5. **Continuous Improvement**: learn from each execution and keep optimizing.
6. **Results Oriented**: judge success solely by whether the target is achieved.
7. **Coding Standards**: `CODE_OF_CONDUCT.md` is the binding “law” for any generated artifact. Review it once per session and refuse to keep code that conflicts with it (copyright, inclusivity, licensing, etc.). Whenever you need to interpret any rule, inspect `CODE_OF_CONDUCT.md` first, cite the relevant section/line, and only fall back to this guide when the code of conduct is silent.

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
- **Public-Only Tests**: unit tests must exercise behavior via public APIs only; never use reflection to access private members.
- **Single-Test Naming**: when a production method is covered by only one test case, name that test method `assert<MethodName>` without extra suffixes.
- **Public Method Isolation**: aim for one public production method per dedicated test method rather than combining multiple public behaviors in a single test.
- **Test Method Order**: keep unit test method ordering consistent with corresponding production methods when practical to improve traceability.
- **Test Naming Simplicity**: keep test names concise and scenario-focused (avoid “ReturnsXXX”/overly wordy or AI-like phrasing); describe the scenario directly.
- **Boolean Assertions**: use `assertTrue` / `assertFalse` for boolean checks; do not use `assertThat(..., is(true/false))`.
- **Parameterized Test Names**: provide display names through parameters and prefix each name with `{index}:` to include the sequence number.
- **Coverage Pledge**: when 100% coverage is required, enumerate every branch/path and its planned test before coding, then implement once to reach 100% without post-hoc fixes.
- **Mock/Spy Specification**: Use mock by default; consider spy only when the scenario cannot be adequately represented using a mock. Avoid spy entirely when standard `mock + when` can express behavior, and do not introduce inner classes for testing purposes—prefer plain test classes with mocks.
- **Strictness and Stub Control**: Enable @MockitoSettings(strictness = Strictness.LENIENT) in the Mockito scenario or apply lenient() to specific stubs to ensure there are no unmatched or redundant stubs; clean up any unused stubs, imports, or local variables before committing.



## Tool Usage Guide

### Sequential Thinking - Structured Reasoning
**Purpose**: break down complex problems, build multi-step plans, and evaluate options.
**When to Trigger**
- When the task needs to be decomposed into multiple steps.
- When generating an execution plan or decision tree, or comparing multiple approaches.
**Rules**
- 6-10 steps.
- Each step: one sentence plus optional dependencies.
- Output an executable plan without exposing intermediate reasoning.
**Fallback**: optionally simplify to a 3-5 step core flow locally.

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

## ✅ Key Checkpoints

### Task Start
- Choose the strategy that fits the task characteristics.
- Confirm tool availability plus fallback approaches.

### Before Coding
- Finish the `Sequential-Thinking` analysis.
- Use tools to understand the existing code.
- Define an execution plan and quality bar.

### During Implementation
- Follow the selected quality standards.
- Record major decisions and reasons for changes.
- Handle exceptions and edge cases promptly.

### After Completion
- Validate functional correctness and code quality.
- Update related tests and documentation.

## Terminal Output Style Guide

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

## Governance Basics
- `CODE_OF_CONDUCT.md` remains the binding law—review it once per session and reject any instruction or artifact that conflicts with ASF requirements on licensing, inclusivity, and attribution.
- Instruction order is `CODE_OF_CONDUCT.md` → user direction → this guide → other repository materials; raise conflicts immediately. When explaining whether an action is allowed, first cite the exact `CODE_OF_CONDUCT.md` clause (file + line) you are relying on, then describe any supplemental rules from this guide.
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

## Execution Loop
1. **Intake & Clarify** – restate the request, map affected modules, confirm sandbox/network/approval constraints, and capture a constraint checklist (forbidden APIs, output formats, ordering rules, coverage targets). As part of intake, reopen `CODE_OF_CONDUCT.md` sections relevant to the task (e.g., Unit Testing Standards before discussing assertions) so you never rely on memory or AGENTS-only guidance when the code of conduct already rules on the topic.
2. **Plan & Reason** – craft a multi-step plan (analysis, edits, tests). When a user asks for specific coverage/branch lists, pause coding until you have responded with an explicit bullet list of every path (file + line/branch) you will exercise, as well as the single test that will cover it; this list is a blocking prerequisite for any edits. Add rule-specific constraints (e.g., “no `assertEquals`”) to the plan and re-check them before edits. Before altering tests or mocks, inspect how `AutoMockExtension`, `@StaticMockSettings`, or other helpers already handle static/construction mocks and list every static dependency you will touch so you can confirm whether it is already covered or needs an explicit override. If a user request is scoped (e.g., “replace `anyCollection` with concrete matchers”), confirm that no broader refactor is expected and keep the change surface constrained unless they explicitly expand it. (No production/test code until the branch checklist and constraint review are complete.)
3. **Implement** – touch only the required files, reuse abstractions, preserve ASF headers, and document major decisions. If you must replace a file wholesale (e.g., rewrite a test), delete the old file first and then add the new version so `apply_patch` does not fight stale context.
4. **Validate** – run the narrowest meaningful command (e.g., `./mvnw -pl <module> -am test`, `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=ClassName test`). Announce intent beforehand and summarize exit codes afterward; when blocked, state the command you intended to run and why it matters.
5. **Report** – lead with intent, list edited files plus rationale/line refs, cite verification commands + results, and propose next steps.

**Self-check before finishing**
- Confirm instruction precedence and constraint checklist items are satisfied.
- Ensure edits are minimal, ASF headers intact, Spotless-ready, and any semantic change has a corresponding test (or explicit rationale).
- Record exact commands, exit codes, and relevant log snippets.
- Highlight remaining risks or follow-ups and keep ASCII-only output unless non-ASCII already existed.
- Run a quick scan to ensure no inline fully-qualified class names remain in code or tests (e.g., `rg "\\b[A-Za-z_]+\\.[A-Za-z_]+\\.[A-Za-z_]+" src test`); replace any hits with imports.

## Tooling & Testing Essentials
- **Go-to commands:** `./mvnw clean install -B -T1C -Pcheck` (full build), `./mvnw test -pl <module>[-am]` (scoped unit tests), `./mvnw spotless:apply -Pcheck [-pl <module>]` (format), `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=ClassName test` (fast verification), and `./mvnw -pl proxy -am -DskipTests package` (proxy packaging/perf smoke).
- **Coverage verification:** before finishing any task that adds or changes tests, run the coverage check (e.g., `./mvnw test jacoco:check@jacoco-check -Pcoverage-check` or scoped `-pl <module> -am`) and explicitly confirm whether the targeted code reaches 100% when required.
- **Checkstyle command:** run `./mvnw checkstyle:check -Pcheck` (or with `-pl <module> -am -Pcheck` for scoped runs) unless explicitly instructed otherwise.
- **Default verification commands:** prefer scoped runs `./mvnw -pl <module> -am -DskipITs -Djacoco.skip=false -Dsurefire.failIfNoSpecifiedTests=false test jacoco:report` for coverage and `./mvnw -pl <module> checkstyle:check -Pcheck` for style; avoid whole-repo builds unless the user requests them.
- **Testing ground rules:** JUnit 5 + Mockito, `ClassNameTest` naming, Arrange–Act–Assert structure, mocks for databases/time/network, reset static caches between cases, and prefer existing swapper/helpers for complex configs.
- **Coverage discipline:** run Jacoco (`./mvnw -pl <module> -am -Djacoco.skip=false test jacoco:report`) when coverage is in question; describe any uncovered branches with file/line reasoning.
- **Branch-focused work:** when asked for “minimal branch coverage” or similar, list every branch upfront, map each to a single test, and document unreachable code explicitly instead of adding redundant cases.
- **API bans:** if a user disallows a tool or assertion, add it to your plan, avoid it during implementation, and cite any verification searches (e.g., `rg assertEquals`) in the final report.

## Run & Triage Quick Sheet
- **Proxy quick start:** `./mvnw -pl proxy -am package` then `shardingsphere-proxy/bin/start.sh -c conf/server.yaml`; report command, exit code, config path, and protocol.
- **JDBC smoke:** `./mvnw -pl jdbc -am test -Dtest=YourTest` with datasource configs from `examples`; note test name, datasource setup, and failure logs.
- **Config validation:** update standalone `server.yaml` and cluster `mode/` configs together; call out defaults and any edits that affect both.
- **Failure triage:** collect `proxy/logs/` plus `target/surefire-reports`, quote the relevant log lines, map them to the data-flow step, and propose the next diagnostic.
- **Routing mistakes:** check feature-rule configs, metadata freshness, and parser dialect; include SQL + config snippet plus impacted module (`features` or `kernel`), and add/plan targeted tests.
- **Proxy won’t start:** verify configs/mode/ports and reuse known-good example configs; share the log snippet and files inspected without editing generated artifacts.
- **Spotless/checkstyle:** run `./mvnw spotless:apply -Pcheck [-pl <module>]` (or `spotless:check`) and confirm ASF headers/import ordering.
- **Sandbox/network block:** if a command is denied, state what you ran, why it failed, and the approval or alternative plan required.
- **Single-module tests:** prefer scoped commands over repo-wide runs; avoid `-Dtest=Pattern` from repo root unless you know the target exists, otherwise use the module’s suite or `-Dsurefire.failIfNoSpecifiedTests=false`. When you must target a single test class, pass the fully-qualified class name (e.g., `-Dtest=org.example.FooTest`) so Surefire can locate it deterministically. To run multiple tests at once, join multiple FQCNs with commas (example: `-Dtest=a.b.FooTest,a.b.BarTest`) and always append `-Dsurefire.failIfNoSpecifiedTests=false` to avoid premature build failure.

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
- For deep chained interactions, you may use Mockito’s `RETURNS_DEEP_STUBS` to reduce intermediate mocks; this is independent of the static-mock rule above.
- Before changing how mocks are created, scan the repository for similar tests (e.g., other rule decorators or executor tests) and reuse their proven mocking pattern instead of inventing a new structure.
- When constructors hide collaborators, use `Plugins.getMemberAccessor()` to inject mocks and document why SPI creation is bypassed.
- When static methods or constructors need mocking, prefer `@ExtendWith(AutoMockExtension.class)` with `@StaticMockSettings` (or the extension’s constructor-mocking support); when a class is listed in `@StaticMockSettings`, do not call `mockStatic`/`mockConstruction` directly—stub via `when(...)` instead. Only if AutoMockExtension cannot be used and the reason is documented in the plan may you fall back to `mockStatic`/`mockConstruction`, wrapped in try-with-resources.
- Before adding coverage to a utility with multiple return paths, list every branch (no rule, non-Single config, wildcard blocks, missing data node, positive path, collection overload) and map each to a test; update the plan whenever this checklist changes.
- Ban inline fully-qualified class names in production and test code—always add imports instead; before wrapping up, run a search (e.g., `rg "\\b[A-Za-z_]+\\.[A-Za-z_]+\\.[A-Za-z_]+" <paths>`) and fix any occurrences rather than waiving them.
- Before coding tests, prepare a concise branch-and-data checklist (all branches, inputs, expected outputs) and keep the plan in sync when the checklist changes.
- When a component is available via SPI (e.g., `TypedSPILoader`, `DatabaseTypedSPILoader`, `PushDownMetaDataRefresher`), obtain the instance through SPI by default; note any exceptions in the plan.
- Do not mix Mockito matchers with raw arguments; choose a single style per invocation, and ensure the Mockito extension aligns with the mocking approach.
- When the user requires full branch/line coverage, treat 100% coverage as a blocking condition: enumerate branches, map tests, and keep adding cases until all branches are covered or explicitly waived; record the coverage requirement in the plan and self-check before concluding.
- Compliance is mandatory: before any coding, re-read AGENTS.md and convert all hard requirements (SPI usage, no FQCN, mocking rules, coverage targets, planning steps) into a checklist in the plan; do not proceed or report completion until every item is satisfied or explicitly waived by the user.

## Brevity & Signal
- Prefer tables/bullets over prose walls; cite file paths (`kernel/src/...`) directly.
- Eliminate repeated wording; reference prior sections instead of restating.
- Default to ASCII; only mirror existing non-ASCII content when necessary.

## Session Notes
- MySQLSchemataQueryExecutorFactoryTest：public 方法分别测试，`accept` 与 `newInstance` 各自使用独立测试方法。
