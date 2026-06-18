# ShardingSphere AI Development Guide

## TOKEN EFFICIENCY ACTION — READ OR REUSE .codex/context/token-efficiency.md

Before running Maven, E2E, Proxy startup, database clients, IDE/MCP run configurations, any command likely to output more than 100 lines,
or any task likely to produce large analysis or review output, I MUST ensure `.codex/context/token-efficiency.md` is available in the active context.
If this exact file from this repository has already been read in the current session and there is no evidence it changed, reuse the loaded content.
Otherwise, read it before running the high-output command or producing the large structured output.
Execute the command according to the Mandatory Execution Contract in that file.
This file is the repository-local source of truth for token-efficient command classification, log capture, filtered summaries, final reporting, and structured output.
Paths in this section are relative to the Apache ShardingSphere repository root.

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
    - Do not introduce package-private top-level helper types by default.
      Keep very small, single-owner state or continuation helpers as private nested types, but avoid accumulating multiple nested collaborators inside one class.
      When a helper has cohesive behavior, multiple callers, direct test value, or enough logic to distract from the owner class, split it into a public top-level type with a clear contract and direct tests.
      If neither private nor public fits, pause before coding and explain why.
    - Every new public production type must have direct, focused tests.
      Broad workflow tests do not replace public contract tests unless they explicitly exercise that public type's behavior.
    - New internal abstractions must reduce cognitive complexity instead of merely wrapping branches in more types.
      For simple internal two-path flows, avoid marker interfaces, multi-type result hierarchies, or extra DTO-style helpers.
      Add them only when they define a stable boundary, keep owner classes readable, or remove meaningful duplicated logic.
    - Delete unused code; when changing functionality, remove legacy compatibility shims.
    - Do not add or keep Javadocs on methods that only override or implement a documented parent method.
      Keep the public contract on the declaring API, SPI, or interface.
      An overriding method should add Javadocs only when it documents implementation-specific behavior, stricter preconditions, side effects, exceptions, compatibility notes,
      or semantics not already covered by the parent declaration.
      When cleaning redundant override Javadocs, change comments only and verify no public contract information is lost.
    - Keep variable declarations adjacent to first use to satisfy Checkstyle VariableDeclarationUsageDistance; do not mark local variables as `final`.
    - Single-use local variables must be inlined by default; keep a local variable only when it is reused (for stubbing/verification/assertions) or materially improves readability.
    - Do not add explicit defensive immutable collection copies in constructors or method return values by default.
      Avoid `List.copyOf`, `Set.copyOf`, `Map.copyOf`, `Collections.unmodifiableList`, `Collections.unmodifiableSet`, `Collections.unmodifiableMap`,
      `Collectors.toUnmodifiableList`, `Collectors.toUnmodifiableSet`, `Collectors.toUnmodifiableMap`,
      Guava `ImmutableList` / `ImmutableSet` / `ImmutableMap`, or similar explicit immutable copy/wrapper APIs
      when the only reason is defensive programming.
    - Ordinary collection literals or stream collection results are allowed when they express direct data construction or transformation.
      Do not flag `List.of`, `Set.of`, `Map.of`, or `Stream.toList()` by default, and do not replace `Stream.toList()` with a mutable collector
      unless the code has a concrete mutability requirement.
    - Explicit immutable collection copies or wrappers are allowed only with a concrete semantic reason, such as enforcing a documented public API contract,
      preserving a snapshot across shared ownership or asynchronous execution, protecting cached/global state from mutation, or satisfying an external API requirement.
      Record the reason in the plan, review note, or nearby code rationale.
- **Complete Implementation**: no MVPs/placeholders/TODOs—deliver fully runnable solutions.

### Performance Standards
- **Algorithm Awareness**: account for time and space complexity.
- **Resource Management**: optimize memory usage and I/O behavior.
- **Boundary Handling**: cover exceptional situations and edge conditions.

### Testing Requirements
- **Test-Driven**: design for testability, ensure unit-test coverage, and keep background unit tests under 60s to avoid job stalls.
- **Quality Assurance**: run static checks, formatting, and code reviews.
- **Checkstyle Gate**: do not hand off code with Checkstyle/Spotless failures—run the relevant module check locally and fix before completion.
- **Formatting Gate**: after code or documentation changes, format only with `./mvnw spotless:apply -Pcheck -T1C`, then check style with `./mvnw checkstyle:check -Pcheck -T1C`; do not use any other formatting method.
  Spotless must run after the last file-changing action and before Checkstyle/tests in the final handoff sequence.
  If any file is edited, generated, moved, or manually whitespace-cleaned after Spotless, rerun Spotless before replying.
- **Continuous Verification**: rely on automated tests and integration validation.
- **Test Naming Simplicity**: keep test names concise and scenario-focused (avoid “ReturnsXXX”/overly wordy or AI-like phrasing); describe the scenario directly.
- **Coverage Discipline**: follow the dedicated coverage & branch checklist before coding when coverage targets are stated.
- **Dedicated and scoped tests**: each public production method must be covered by dedicated test methods; each test method covers only one scenario and invokes the target public method at most once (repeat only when the same scenario needs extra assertions), and different branches/inputs belong in separate test methods.
- **No interface-only tests**: do not create unit tests for interfaces themselves; cover behavior through concrete implementations instead, and avoid dedicated test classes for pure contracts such as `MCPHandlerProvider`.
- **Parameterized tests naming**: all parameterized tests must set an explicit `name` and use the `"{0}"` template for display names.
- **Mocking Rule**: default to mocks; see Mocking & SPI Guidance for static/constructor mocking and spy avoidance details.
- **Reflection Rule**: when tests must touch fields or methods via reflection, use `Plugins.getMemberAccessor()`—direct reflection APIs are forbidden.

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

## Dangerous Operation Confirmation Mechanism

### High-Risk Operation Checklist—obtain explicit confirmation **before** doing any of the following:
- **File System**: deleting files/directories, bulk edits, or moving system files.
- **Code Submission**: `git commit`, `git push`, `git reset --hard`.
- **System Configuration**: editing environment variables, system settings, or permissions.
- **Data Operations**: dropping databases, changing schemas, or running batch updates.
- **Network Requests**: sending sensitive data or calling production APIs.
- **Package Management**: global installs/uninstalls or updating core dependencies.

### Confirmation Template

Dangerous operation detected! Operation type: [specific action] Scope of impact: [affected area] Risk assessment: [potential consequence] Please confirm whether to continue. [Requires explicit “yes”, “confirm”, or “proceed”]

## Coding Execution Principles
- **Think before coding**: inspect existing code, contracts, tests, and relevant standards before editing; do not guess, hide uncertainty, or invent unsupported facts.
- **Simple first**: solve the verified goal with the smallest clear implementation that preserves existing behavior.
- **Precise modification**: change only the files and code paths required by the task; avoid drive-by refactors and unrelated cleanup.
- **Path portability**: when writing code, tests, scripts, or skills, do not hard-code local machine paths or workspace-specific absolute paths.
  Use repository-relative paths, configurable parameters, temporary directories, or documented environment variables instead.
- **Scope declaration gate**: before planning or editing, determine and declare the requested change boundary.
  If the boundary is clear from the user request, state the inferred scope explicitly before making changes.
  If the boundary is missing or ambiguous, pause and ask the developer to confirm it before making changes.
  The boundary may be a module name, package name, class name, file path, public API, issue/PR scope, or specific behavior/test scenario.
  Do not determine the scope after editing, and do not infer a broader scope from nearby code, unrelated failures, or cleanup opportunities.
  The declared or confirmed boundary is the maximum allowed scope for production, test, documentation, and configuration changes.
  If implementation evidence shows the declared or confirmed scope is insufficient, pause again and explain the required expansion before editing outside it.
- **Goal-driven execution**: convert the request into verifiable outcomes before implementation, then validate those outcomes with scoped checks.

## Coding Skill Guidance
- When the named third-party skills are available in the current environment, use them for the matching work:
  `source-driven-development` for official-source checks, `api-and-interface-design` for public contracts,
  `doubt-driven-development` for non-trivial decisions, `code-simplification` after implementation, and `code-review-and-quality` before handoff.
- If a named third-party skill is unavailable, do not install it automatically or block the task.
  Apply an equivalent manual checklist for the same intent, record the fallback in the plan or final response, and continue.

## Workflow
- Use Sequential Thinking when tasks need decomposition: 6-10 steps (fallback 3-5), one sentence each, actionable.
- Intake: choose the strategy for the task, confirm tool availability/fallbacks, capture constraints (forbidden APIs, output format, coverage/test expectations),
  and use `source-driven-development` when available, or equivalent source-checking, to verify facts that depend on authoritative sources.
- Plan: inspect existing code with tools before edits, finish the plan before coding, use `doubt-driven-development` when available for non-trivial decisions,
  and set the quality/verification bar.
- Implement: keep scope minimal, follow quality standards, record decisions, and handle edge cases; honor instruction precedence from Core Principle #7.
- Validate: run the narrowest meaningful checks (see Verification & Commands) and prefer scoped runs; note any sandbox or limit blocks and alternatives.
- Report & self-check: share intent, edits, verification results, and next steps; ensure all required instructions, coverage, and mocking rules are satisfied, with remaining risks called out.

### Protocol / Client Compatibility Evidence Gate
- Treat protocol compatibility, native-client smoke, driver compatibility, packet-trace, and externally visible client behavior failures as evidence-first failures until proven otherwise.
- Before changing protocol bytes, packet order, handshake/authentication/session lifecycle, status/error semantics, cursor or fetch lifecycle, row-transfer payloads, or client-specific completion logic,
  create a reference-versus-current behavior record for the same scenario.
- The behavior record must use the same client or driver version, server or database version, schema/configuration, input SQL or scripts, and execution markers.
- The behavior record must identify the first actionable mismatch, such as a missing message, extra message, wrong order, wrong status or error semantics, wrong lifecycle transition, wrong field value, or wrong bounded payload shape.
- Capture complete is not analysis complete. A record is code-ready only when it includes scenario identity, reference/current inventories, chronological request/response correlation,
  critical response ownership, the first actionable mismatch, non-mismatch exclusions, minimum fix scope, and a deterministic guard plan.
- If any correlation field is missing, continue capture, source analysis, or behavior analysis. Do not change production code, rewrite test expectations, rerun the same or adjacent sentinel,
  or send a progress-only final response unless the user explicitly asks for status or stop.
- A deterministic guard is not an exploratory probe. Add or run it only after the behavior record states the expected branch.
  If the guard failure matches the recorded mismatch, implement the smallest in-scope fix. If it exposes a wrong guard expectation or route assumption, return to behavior correlation before changing code or test expectations.
- After one fix, run only one sentinel that matches the recorded mismatch. If the sentinel fails, update the behavior record before any further code change.
- Having no current question for the user is not a reason to continue rerunning the failed scenario, but it is also not a reason to stop work. Continue analysis until the behavior record is complete, then proceed with an in-scope fix when available.
- Stop and ask the user only when the next action requires scope expansion, third-party license or login approval, deleting files, adding dependencies, changing project goals, or when analysis reaches an actual impasse that cannot be resolved from local evidence.

### E2E / Integration Failure Gate
- When an E2E, integration, client-smoke, Docker-smoke, or protocol scenario fails, hangs, times out, or requires guess-and-retry debugging, stop further reruns immediately. This means stop the rerun loop, not stop the task.
- Before rerunning the scenario, classify the failure as environment, classpath, stale snapshot, dependency, test design, protocol implementation, data setup, assertion logic, or external-service behavior.
- Record the root cause, the evidence inspected, and the minimum fix scope before changing code or configuration.
- Complete deterministic gates first, such as classpath consistency, stale bytecode scan, dependency alignment, packet-trace completeness, or direct/proxy evidence review.
- After the gate passes, rerun only one sentinel scenario. If that sentinel fails unexpectedly, stop rerunning and return to analysis instead of applying small speculative patches.
- Do not loop through repeated E2E attempts, incremental guesses, or patch-and-rerun cycles unless the user explicitly approves that debugging mode for the current task.

## Compliance Guardrails & Checklists
- **Pre-task checklist (do before planning/coding):** re-read AGENTS.md and `CODE_OF_CONDUCT.md`; restate user goal, constraints, forbidden tools/APIs, coverage expectations, sandbox/network/approval limits; prefer `rg`/`./mvnw`/`apply_patch`; avoid destructive commands (`git reset --hard`, `git checkout --`, bulk deletes) and generated paths like `target/`.
- **Change boundary checklist:** determine and declare the change boundary before planning or editing.
  If the boundary is clear from the request, state the inferred module, package, class, file path, public API, issue/PR scope, or behavior/test scenario.
  If the boundary is missing or ambiguous, ask the developer to confirm it before making changes.
- **Risk gate:** if any action fits the Dangerous Operation Checklist, pause and use the confirmation template before proceeding.
- **Planning rules:** use Sequential Thinking with 3-10 actionable steps (no single-step plans) via the plan tool for non-trivial tasks; convert all hard requirements (SPI usage, mocking rules, coverage/test naming, forbidden APIs) into a checklist inside the plan and do not code until each item is addressed or explicitly waived.
- **Report root-cause rule:** for report, audit, review, or analysis tasks, if a reported finding, verdict, or conclusion is challenged, disproved, or shown to be inaccurate,
  do not stop at patching the output artifact. First fix the highest-leverage root cause in rules, workflow, schema, validators, prompts, regression cases,
  or tests so the same class of error is less likely to recur. Update the report artifact afterward as a consequence of that root-cause fix, unless the user explicitly requests a one-off result correction only.
- **Execution discipline:** inspect existing code before edits; keep changes minimal; default to mocks and SPI loaders; keep variable declarations near first use without marking local variables `final`; inline single-use locals by default unless reuse/readability justifies retention; delete dead code and avoid placeholders/TODOs.
  Verify code and skills do not contain local machine paths before handoff.
- **CI impact gate:** before handoff, determine affected GitHub Actions from changed files.
  Use `rg` or small `sed` ranges to inspect only matching workflow `paths`, job names, and execution commands instead of reading every workflow file.
  For production code, tests, E2E, build configuration, or project-rule changes, run the local equivalent of the affected workflow command when practical.
  For docs-only or clearly unrelated changes, or when a workflow-equivalent run is not practical, state the rationale, residual risk, and narrower verification that was run.
- **Public contract propagation gate:** when changing any public contract or externally visible identifier, search the affected reference surfaces with `rg` before handoff.
  This includes API names, SQL syntax, configuration keys, YAML keys, SPI types, error codes, protocol fields, CLI commands, resource identifiers, canonical documentation names, descriptors, examples, distributions, tests, E2E fixtures, and baseline resources.
  Exclude generated directories such as `.git` and `target`.
  If a compatibility alias remains, document whether it is discoverable; if it must stay hidden, cover that with a focused test or contract check.
- **External output safety gate:** when changing externally visible errors, diagnostics, log summaries, HTTP/JSON payloads, CLI output, or exception conversion, test the complete external output when inputs may contain connection strings, credentials, tokens, SQL text, filesystem paths, or user data.
  Do not rely only on nested-field assertions when the full payload could still expose sensitive or misleading text.
- **Generated contract and baseline gate:** when a change can affect snapshots, baselines, golden files, fingerprints, expected SQL cases, protocol descriptors, generated schemas, or client/model-visible metadata, regenerate or verify those artifacts with the existing project tool or test before handoff.
  Run the corresponding contract, golden, baseline, or E2E check when practical; otherwise report why it was not practical and what narrower verification was used.
- **AGENTS.md maintenance:** do not add or update a `Session Notes` section in `AGENTS.md`. Keep task-specific notes in the active conversation, issue, or PR; only stable project-level rules may be generalized into this file.
- **Post-task self-check (before replying):** confirm all instructions were honored; verify no placeholders/unused code; ensure Checkstyle/Spotless gates for touched modules are satisfied or explain why not run and what to run; list commands with exit codes; call out risks and follow-ups; complete all applicable checks before replying and do not rely on users to find missed rule violations.
- **End-of-task format/style gate:** for any task that edits files, run `./mvnw spotless:apply -Pcheck -T1C` after the final edit, then run `./mvnw checkstyle:check -Pcheck -T1C` when production, test, or project-rule files are touched.
  Do not perform manual formatting or whitespace cleanup after the final Spotless run; if a later cleanup is required, repeat Spotless and then Checkstyle before the final response.
- **Final response template:** include intent/why, changed files with paths, rationale per file/section, commands run (with exit codes), verification status, and remaining risks/next actions (if tests skipped, state reason and the exact command to run); include a concise self-check result statement confirming final clean status after fixes.

## Final Self-Iteration Gate
- Apply this gate only to implementation tasks where the user has requested or authorized code or documentation edits.
  For review-only, analysis-only, triage-only, or plan-only tasks, report findings and suggested follow-ups without modifying the reviewed target.
- Before finishing an authorized implementation task, ask whether this task created in-scope legacy or dead code that can be safely removed,
  whether the changed implementation can be simpler without changing behavior,
  whether existing public behavior and contracts are preserved, and whether `code-review-and-quality` or equivalent review still has in-scope required findings.
- Use `doubt-driven-development` when available, or equivalent adversarial self-review, to keep raising and resolving valuable in-scope questions until the stop condition is met.
  Stop when no actionable findings remain, the same findings repeat, 3 doubt cycles complete, or the user explicitly overrides.
- Before finishing any implementation task, compare the final diff against the declared or confirmed change boundary.
  If any edit is outside that boundary, revert only the out-of-scope edits made in the current task, then rework the solution within the allowed scope.
  Do not keep out-of-scope changes for convenience, and do not use destructive git commands or revert unrelated user changes.
  If the task cannot be completed after removing the out-of-scope edits, stop and ask the developer to confirm the required scope expansion.
- If any answer reveals an in-scope, behavior-preserving, low-risk required fix, make the fix and rerun relevant checks.
- Repeat the review-fix-verify loop until no in-scope required findings remain.
- Do not continue iterating for optional polish, broad cleanup, risky refactors, or unrelated code. Record those as follow-up suggestions instead of expanding the task.

## Coverage & Branch Checklist
- When coverage targets are declared (including 100%), list every branch/path with its planned test before coding.
- Map each branch to exactly one test; add cases until all declared branches are covered or explicitly waived.
- For utilities with multiple return paths, record the branch list and update it if the code changes.
- Use Jacoco to confirm expectations when coverage is in question; document any unreachable code instead of adding redundant tests.

## Response Style

### Language and Tone
- **Language Consistency**: respond in the same language as the user; if the user explicitly specifies a language, prioritize that language.
- **Friendly and Natural**: interact like a professional peer; avoid stiff formal language.
- **No Emojis or Symbols**: do not use emojis or decorative graphic symbols in any reply.
- **Hit the Point Fast**: start with a sentence that captures the core idea, especially for complex problems.

### Content Organization
- **Hierarchical**: separate sections with headings/subheadings; split long content into sections.
- **Focused Bullets**: break long paragraphs into short sentences or bullets, each covering a single idea.
- **Logical Flow**: use ordered lists for multi-step work (1. 2. 3.) and unordered lists for peers (- or *).
- **Proper Spacing**: keep blank lines or `---` between blocks to boost readability.
> Avoid complex tables in the terminal (especially for long, code-heavy, or narrative content).

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

### Ending Suggestions
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
- **Patterns to lean on:** builders/factories from `infra`, SPI-driven extensions, DTOs with explicit ownership contracts, explicit strategy enums.
- **Anti-patterns:** duplicating parsing logic, bypassing metadata caches, silently accepting invalid configs, static singletons in shared modules, or overbuilt helpers.
- **Known pitfalls:** routing regressions when shadow rules are skipped, timezone drift from poor time-mocking, forgetting standalone vs cluster (`mode`) validation, missing ASF headers, Mockito inline mocks breaking on JDKs that block self-attach.
- **Success recipe:** explain why the change exists, cite the affected data-flow step, keep public APIs backward compatible, and record defaults/knobs alongside code changes.

## Verification & Commands
- Core commands: `./mvnw clean install -B -T1C -Pcheck` (full build), `./mvnw test -pl <module>[-am]` (scoped unit tests), `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=ClassName test` (fast verification), `./mvnw -pl proxy -am -DskipTests package` (proxy packaging/perf smoke).
- Coverage: when tests change or targets demand it, run `./mvnw test jacoco:check@jacoco-check -Pcoverage-check` or scoped `-pl <module> -am -Djacoco.skip=false test jacoco:report`; pair with the Coverage & Branch Checklist.
- Format: after code or documentation changes, run `./mvnw spotless:apply -Pcheck -T1C`; do not use any other formatting method.
  This must be repeated after the last file-changing action before handoff.
- Style: after formatting, run `./mvnw checkstyle:check -Pcheck -T1C` when production, test, or project-rule files are touched.
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
- When static methods or constructors need mocking, prefer `@ExtendWith(AutoMockExtension.class)` with `@StaticMockSettings` (or the extension’s constructor-mocking support); when a class is listed in `@StaticMockSettings`, do not call `mockStatic`/`mockConstruction` directly—stub via `when(...)` instead. Only if AutoMockExtension cannot be used and the reason is documented in the plan may you fall back to `mockStatic`/`mockConstruction`, wrapped in try-with-resources.
- Before coding tests, follow the Coverage & Branch Checklist to map inputs/branches to planned assertions.
- When a component is available via SPI (e.g., `TypedSPILoader`, `DatabaseTypedSPILoader`, `PushDownMetaDataRefresher`), obtain the instance through SPI by default; note any exceptions in the plan.
- If the class under test implements `TypedSPI` or `DatabaseTypedSPI`, instantiate it via `TypedSPILoader` or `DatabaseTypedSPILoader` instead of calling `new` directly.
- Do not mix Mockito matchers with raw arguments; choose a single style per invocation, and ensure the Mockito extension aligns with the mocking approach.
- Compliance is mandatory: before any coding, re-read AGENTS.md and convert all hard requirements (SPI usage, no FQCN, mocking rules, coverage targets, planning steps) into a checklist in the plan; do not proceed or report completion until every item is satisfied or explicitly waived by the user.
