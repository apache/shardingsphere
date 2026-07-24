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
    - **Code economy**: every added line, identifier, literal, helper, abstraction, and configuration entry must serve production behavior, a public contract,
      regression protection, diagnostics, safety, readability, or removal of real duplication.
      Do not add code for formal symmetry, coverage appearance, hypothetical reuse, structural completeness, or test convenience.
      Avoid unnecessary locals, thin wrappers, helpers, collection copies, comments, guards, and abstractions; inline single-use locals unless reuse or readability justifies them.
      Use the smallest clear implementation, and remove obsolete code within scope only after verifying its usages.
    - **YAML anchor rule**: do not add YAML anchors unless they are reused in the same file and reduce meaningful duplication.
      Prefer repeating a small block when an anchor has no aliases, anticipates future reuse, or obscures nearby YAML.
    - Do not introduce package-private top-level helper types by default.
      Keep very small, single-owner state or continuation helpers as private nested types, but avoid accumulating multiple nested collaborators inside one class.
      When a helper has cohesive behavior, multiple callers, direct test value, or enough logic to distract from the owner class, split it into a public top-level type with a clear contract and direct tests.
      If neither private nor public fits, do not add a helper; keep the implementation in the approved owner or simplify the design within the declared boundary.
    - Every new public production type must have direct, focused tests.
      Broad workflow tests do not replace public contract tests unless they explicitly exercise that public type's behavior.
    - Do not add production types or abstractions, widen visibility, or change constructors or signatures solely for test convenience.
      Require an independent production reason and prefer test-local fixtures, mocks, existing public construction paths, builders, or SPI loaders.
    - New internal abstractions must reduce cognitive complexity instead of merely wrapping branches in more types.
      For simple internal two-path flows, avoid marker interfaces, multi-type result hierarchies, or extra DTO-style helpers.
      Add them only when they define a stable boundary, keep owner classes readable, or remove meaningful duplicated logic.
    - Delete unused code; when changing functionality, remove legacy compatibility shims.
    - When adding or touching boilerplate constructors, getters, setters, or logger fields, apply the `CODE_OF_CONDUCT.md` Lombok preference first when Lombok is already available in the module and the replacement is semantically equivalent.
      Use the narrowest suitable Lombok annotation, such as `@NoArgsConstructor(access = AccessLevel.PRIVATE)` for utility-class private constructors, `@RequiredArgsConstructor` or `@AllArgsConstructor` for plain constructors, and `@Getter` or `@Setter` for accessor methods.
      For public constructors and accessors, verify the generated signature, access level, parameter order, annotations, and reflection or serialization behavior before replacing manual code.
      Keep manual members only when they contain real logic, annotations, documentation, validation, defaulting, side effects, compatibility requirements, framework or reflection semantics, or public-contract details that Lombok would change or obscure.
      Do not use broad Lombok annotations such as `@Data` unless every generated behavior is intentionally required.
    - **Validation discipline**: add null, size, state, or other runtime guards only at real contract boundaries, such as external input, public APIs,
      persisted or parsed configuration, SPI or reflection input, and shared or asynchronous state, or when they provide concrete diagnostic value.
      Do not recheck invariants guaranteed by callers or upstream contracts; every added guard must map to a specific failure scenario or diagnostic benefit.
      Prefer `ShardingSpherePreconditions` with lazy exception suppliers for production guards.
      Use manual throws only when the module cannot depend on `infra/exception`, the check is inside `ShardingSpherePreconditions`,
      or required control flow would otherwise be obscured; record the reason.
      Remove stale checked `throws` from private and internal methods when their last exception source disappears.
      Keep them on public or overridden methods only when required by a caller-facing, framework, external API, or compatibility contract; do not widen them to generic `Exception` or `Throwable`.
    - **Javadoc signal-to-noise gate**:
      - Keep the concise summaries and required tags mandated by `CODE_OF_CONDUCT.md` for public APIs and SPIs.
      - Beyond that baseline, document only contracts not expressed by code that change caller or implementer behavior,
        such as concurrency, lifecycle, state, protocol, compatibility, or failure handling.
      - Do not restate names, signatures, visible collection properties, or parent contracts unless an override adds implementation-specific obligations.
      - Review only Javadocs within scope, and remove any sentence that does not affect caller action, implementer obligation, compatibility, or failure handling.
    - Keep variable declarations adjacent to first use to satisfy Checkstyle VariableDeclarationUsageDistance; do not mark local variables as `final`,
      including ordinary local declarations, loop variables, enhanced `for` variables, and try-with-resources resources.
      For parameters, use `final` only on method parameters, constructor parameters and `catch` parameters; leave lambda parameters without `final` unless surrounding code style or tooling requires it.
    - For collection declarations in production and test code, use the least-specific type that expresses the required contract.
      Prefer `Collection` for parameters, fields, local variables, and internal return values when the code only iterates, checks emptiness or size,
      or uses common collection operations.
      Use `List` only when list-specific semantics or APIs are required, such as positional access, stable ordered contract, duplicate-preserving list contract,
      or an external API or public contract that requires `List`.
      Use `Set` only when uniqueness, set semantics, set-specific APIs, or an external API or public contract requires `Set`.
      Do not declare implementation types such as `LinkedList`, `ArrayList`, or `HashSet` unless implementation-specific APIs are required.
      Choose concrete implementations according to `CODE_OF_CONDUCT.md`.
    - Do not copy, wrap, or re-materialize collections merely for defensive programming, possible mutability or ordering, concrete-type symmetry, or convenience.
      A copy or wrapper requires a documented public contract, a snapshot of shared, asynchronous, or cached state, isolation for later mutation, or an external API requirement.
      Ordinary collection literals and transformation results need no mutable replacement; in tests, use a mutable copy only when that instance is mutated or mutability is the scenario.
      Record the concrete reason for any explicit copy or wrapper.
- **Complete Implementation**: no MVPs/placeholders/TODOs—deliver fully runnable solutions.

### Dead Code Verification
- Before declaring code unused, use semantic Find Usages; otherwise search the entire repository, covering direct calls, method references, generated accessors, overrides, reflection, SPI/framework registrations, tests, E2E, and external consumers.
- Inspect every match; one regex or production-only search is insufficient evidence.

### Constructor Design Rules
- Before adding or changing a constructor, inspect nearby production code and follow the module's existing conventions for visibility, Lombok, validation, and tests.
- Keep only constructors with distinct production semantics or real framework, SPI, reflection, or serialization requirements.
- Do not add or keep constructor overloads only for fewer arguments, test convenience, new-code backward compatibility, or future flexibility.
- When a new semantic parameter is required, update callers to pass it explicitly instead of adding a default-forwarding overload.
- Before handoff, scan changed files and call sites for unused or compatibility constructors and delete them.

### Performance Standards
- **Algorithm Awareness**: account for time and space complexity.
- **Resource Management**: optimize memory usage and I/O behavior.
- **Boundary Handling**: cover exceptional situations and edge conditions.

### Testing Requirements
- **Test-Driven**: design for testability, ensure unit-test coverage, and keep background unit tests under 60s to avoid job stalls.
- **Meaningful Unit Tests**:
    - Meaningless unit tests are tests that do not protect behavior owned by the class under test.
      A unit test must cover computation, decision-making, validation, transformation, state transition, error handling,
      protocol or payload contract generation, or another non-trivial behavior owned by the target class.
      If a method only passes data through, delegates directly, returns a constant, exposes a getter or setter, or wires an object without adding logic,
      do not add a dedicated unit test for that method unless the pass-through itself is a documented public contract or part of an externally visible protocol boundary.
    - A unit test is meaningful only if it would fail for a realistic bug that matters to users, callers, protocol clients, or maintainers of the class contract.
      If a test only proves that implementation structure remains the same, rather than proving behavior is correct, remove it.
    - Do not add unit tests that only assert constant literal values, enum names, field names, method names, YAML anchor names,
      or configuration structure created only for internal reuse.
      Literal-value tests are allowed only when the literal is an external protocol value, documented compatibility contract, SQL keyword, configuration key, YAML key,
      error code, resource URI, or model-visible schema value, and no broader behavior or contract test already protects it.
      Prefer protecting such literals through the behavior that emits or consumes them; add a dedicated literal-only test only when no behavior path can cover the contract.
    - Do not add tests that only verify Java, Lombok, Mockito, YAML parser, collection library, or framework behavior.
      Do not add tests that duplicate an existing scenario without covering a new branch, input class, edge case, contract, calculation path, or failure mode.
      Do not add tests that only increase coverage numbers but would not catch a real behavioral regression.
    - Do not add tests that mirror private implementation details, helper call order, local variable structure,
      or current refactoring shape unless that interaction is the explicit public contract of the class under test.
- **Quality Assurance**: run static checks, formatting, and code reviews.
- **Checkstyle Gate**: do not hand off code with Checkstyle/Spotless failures—run the relevant module check locally and fix before completion.
- **Formatting Gate**: after code or documentation changes, format only with `./mvnw spotless:apply -Pcheck -T1C`, then check style with `./mvnw checkstyle:check -Pcheck -T1C`; do not use any other formatting method.
  Spotless must run after the last file-changing action and before Checkstyle/tests in the final handoff sequence.
  If any file is edited, generated, moved, or manually whitespace-cleaned after Spotless, rerun Spotless before replying.
- **Continuous Verification**: rely on automated tests and integration validation.
- **Test Naming Simplicity**: keep test names concise and scenario-focused (avoid “ReturnsXXX”/overly wordy or AI-like phrasing); describe the scenario directly.
- **Coverage Discipline**: follow the dedicated coverage & branch checklist before coding when coverage targets are stated.
- **Dedicated and scoped tests**: each behavior-owning public production method must be covered by dedicated test methods.
  Pure pass-through, constant-returning, accessor, or wiring-only methods follow the Meaningful Unit Tests gate instead of forcing dedicated tests.
  Each test method covers only one scenario and invokes the target public method at most once (repeat only when the same scenario needs extra assertions),
  and different branches/inputs belong in separate test methods.
- **No testing through layers**: a unit test must not appear to test the current class under test while its inputs, branch triggers, or assertions encode collaborator implementation rules such as SPI, registry, factory, parser, loader, metadata option, driver option, dialect defaults, exception classification, or message/SQLState parsing.
  Unit tests must distinguish behavior owned by the class under test from behavior owned by collaborators.
  If a collaborator implementation can change while the class under test contract stays unchanged, the current class test must not fail; mock the nearest stable collaborator boundary and cover the collaborator rule in that collaborator's own focused tests.
  Cross-layer verification belongs only in explicitly scoped integration, contract, or E2E tests.
- **No interface-only tests**: do not create unit tests for interfaces themselves; cover behavior through concrete implementations instead, and avoid dedicated test classes for pure contracts or SPI interfaces.
- **Parameterized tests naming**: all parameterized tests must set an explicit `name` and use the `"{0}"` template for display names.
- **Mocking Rule**: default to mocks; see Mocking & SPI Guidance for static/constructor mocking and spy avoidance details.

#### Mock Helper and Test Fixture Boundaries

##### Prefer Direct Mocks for Scenario Behavior
- Do not create standalone mock utility classes only to hide Mockito setup, reduce imports, or share a few `mock`, `when`, `mockStatic`, or `mockConstruction` calls.
- Prefer direct mocks in the test method when the mocked behavior is part of the scenario being asserted.

##### Use Private Helpers Only for Local Repetition
- Use a private helper inside the same test class only when the setup is repeated in that class and the helper name makes the tested scenario easier to read.

##### Allow Standalone Fixtures Only for Stable Boundaries
- A standalone test fixture or helper is allowed only when it represents a stable test fixture boundary, such as a reusable fake external runtime, packaged plugin fixture, JDBC metadata fixture, test distribution fixture, or multi-collaborator context that would otherwise obscure the test intent.
- Standalone test helpers must have the narrowest practical visibility, live in the nearest owning test package or module, and must not become cross-module test APIs for convenience.

##### Delete or Inline Thin Mock Wrappers
- Delete or inline mock helpers when they merely wrap simple Mockito behavior, hide database-specific or SPI behavior from the test scenario, have only one or two incidental callers, or make it harder to see which collaborator behavior the test depends on.
- Test-only reuse, shorter call sites, easier static mocking, or avoiding duplicated imports are not sufficient reasons to add or keep a standalone mock utility class.
- **Reflection Rule**: when tests must touch fields or methods via reflection, use `Plugins.getMemberAccessor()`—direct reflection APIs are forbidden.

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
- **Reference-first implementation**: before coding, inspect the affected code, tests, contracts, configuration, registration paths, and module boundaries.
  For each affected subsystem, follow the closest applicable, compliant, and maintained pattern for architecture, extension points, dependencies, naming, and tests.
  When adding a database, dialect, plugin, or module, reuse applicable framework and extension mechanisms;
  a database or dialect may be derived or entirely new, and derived dialects must keep shared behavior aligned and isolate only real differences.
  If no applicable precedent exists, design the missing part and record the search scope, selected references or absence of precedent, and rationale in the plan and final report.
  Do not use this analysis to expand the task or refactor unrelated code.
- **Simple first**: solve the verified goal with the smallest clear implementation that preserves existing behavior.
- **Precise modification**: change only the files and code paths required by the task; avoid drive-by refactors and unrelated cleanup.
- **Path portability**: when writing code, tests, scripts, or skills, do not hard-code local machine paths or workspace-specific absolute paths.
  Use repository-relative paths, configurable parameters, temporary directories, or documented environment variables instead.
- **Execution mode discipline**: in confirmation-only tasks, surface unclear boundaries, design conflicts, and rule conflicts as questions or recommendations before editing.
  In authorized implementation tasks, produce a compliant result directly when the issue can be resolved within the declared boundary; do not stop merely to ask permission or explain alternatives.
  Stop only for missing scope, required scope expansion, dangerous operations, external approvals, or true impasses that cannot be resolved from local evidence.
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
  `code-simplification` after implementation, and `code-review-and-quality` before handoff.
- If a named third-party skill is unavailable, do not install it automatically or block the task.
  Apply an equivalent manual checklist for the same intent, record the fallback in the plan or final response, and continue.

### Cross-Model Review Prohibition
- Do not use `doubt-driven-development` in this repository when it requires an external or cross-model review offer.
- Unless the user explicitly requests a specific external model in the current task, never ask whether to run a cross-model review, never pause work for that choice,
  and never invoke Gemini CLI, Codex CLI, Claude CLI, or any other external model or model-review service.
- When an independent challenge is useful, use a fresh-context sub-agent within the current session or perform a bounded adversarial self-review, then continue to completion without asking the user to choose a reviewer.

## Workflow
- Use Sequential Thinking when tasks need decomposition: 6-10 steps (fallback 3-5), one sentence each, actionable.
- Intake: choose the strategy for the task, confirm tool availability/fallbacks, capture constraints (forbidden APIs, output format, coverage/test expectations),
  and use `source-driven-development` when available, or equivalent source-checking, to verify facts that depend on authoritative sources.
- Plan: complete the required reference analysis and plan before coding, use a bounded fresh-context or adversarial self-review for non-trivial decisions,
  and set the quality/verification bar without invoking or offering cross-model review.
- Implement: keep scope minimal, follow quality standards, record decisions, and handle edge cases; honor instruction precedence from Core Principle #7.
- Validate: run the narrowest meaningful checks (see Verification & Commands) and prefer scoped runs; note any sandbox or limit blocks and alternatives.
- Report & self-check: share intent, edits, verification results, and next steps; ensure all required instructions, coverage, and mocking rules are satisfied, with remaining risks called out.

### Database Protocol / Client Compatibility Evidence Gate
- Apply this gate only to database wire-protocol, proxy frontend protocol, native/client compatibility, protocol E2E, Docker/native client smoke,
  packet/trace/log/client behavior evidence, and database client-visible protocol behavior work.
  It does not apply to unrelated feature work, ordinary unit-test failures, formatting failures, documentation-only changes, or non-protocol implementation tasks.
- Treat database protocol compatibility, native-client smoke, driver compatibility, packet/trace/log evidence, and externally visible client behavior failures
  as evidence-first failures until proven otherwise.
- Evidence-first, capture-first, trace-first, or packet-first is an execution method, not a user-confirmation stop gate.
  When the next action remains inside the approved scope, continue with evidence collection, behavior correlation, root-cause classification,
  the smallest deterministic fix, focused checks, and one matching sentinel instead of stopping only to report progress.
- Before changing protocol bytes, packet order, handshake/authentication/session lifecycle, status/error semantics, cursor or fetch lifecycle,
  row-transfer payloads, metadata payloads, or client-specific completion logic, create a reference-versus-current behavior record for the same scenario.
- The behavior record must use the same client or driver version, server or database version, schema/configuration, input SQL or scripts, and execution markers.
- The behavior record must identify the first actionable mismatch, such as a missing message, extra message, wrong order, wrong status or error semantics,
  wrong lifecycle transition, wrong field value, wrong metadata shape, or wrong bounded payload shape.
- Capture, trace, log, or packet collection complete is not analysis complete. A record is code-ready only when it includes scenario identity,
  reference/current inventories, chronological request/response correlation, critical response ownership, the first actionable mismatch,
  non-mismatch exclusions, minimum fix scope, and a deterministic guard plan.
- If any correlation field is missing, continue capture, source analysis, or behavior analysis. Do not change protocol implementation code,
  rewrite test expectations, rerun the same or adjacent sentinel, or send a progress-only final response unless the user explicitly asks for status or stop.
- A deterministic guard is not an exploratory probe. Add or run it only after the behavior record states the expected branch.
  If the guard failure matches the recorded mismatch, implement the smallest in-scope fix. If it exposes a wrong guard expectation or route assumption,
  return to behavior correlation before changing protocol code or test expectations.
- After one fix, run only one sentinel that matches the recorded mismatch. If the sentinel fails, update the behavior record before any further code change or rerun.
- Having no current question for the user is not a reason to continue rerunning the failed scenario, but it is also not a reason to stop work.
  Continue analysis until the behavior record is complete, then proceed with an in-scope fix when available.
- If a database-specific spec, task state, evidence matrix, or machine gate exists, follow it as the specialization of this general rule.
  Database-specific specializations may be stricter than this root rule.
- Stop and ask the user only when the next action requires scope expansion, third-party license or login approval, deleting files, adding dependencies,
  changing project goals, or when analysis reaches an actual impasse that cannot be resolved from local evidence.

### E2E / Integration Failure Gate
- When an E2E, integration, client-smoke, Docker-smoke, or protocol scenario fails, hangs, times out, or requires guess-and-retry debugging, stop further reruns immediately. This means stop the rerun loop, not stop the task.
- Before rerunning the scenario, classify the failure as environment, classpath, stale snapshot, dependency, test design, protocol implementation, data setup, assertion logic, or external-service behavior.
- Record the root cause, the evidence inspected, and the minimum fix scope before changing code or configuration.
- Complete deterministic gates first, such as classpath consistency, stale bytecode scan, dependency alignment, packet-trace completeness, or direct/proxy evidence review.
- After the gate passes, rerun only one sentinel scenario. If that sentinel fails unexpectedly, stop rerunning and return to analysis instead of applying small speculative patches.
- Do not loop through repeated E2E attempts, incremental guesses, or patch-and-rerun cycles unless the user explicitly approves that debugging mode for the current task.
- Slow-loop fuse: after one matching sentinel fails, or when 30-45 minutes of work on the same failing scenario produces no new mismatch,
  guard expectation, state transition, or closed task, stop reruns and blind patches immediately and continue analysis, state repair, or handoff.
  This is not a user stop gate unless the next action needs approval under the dangerous-operation or scope-expansion rules.

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
- **Execution discipline:** keep changes minimal; default to mocks and SPI loaders; keep variable declarations near first use without marking local variables `final`; delete dead code and avoid placeholders/TODOs.
  Before handoff, inspect the Java diff for newly added `final` declarations and remove any new meaningless local-variable `final`.
  Verify code and skills do not contain local machine paths before handoff.
- **Final semantic fix gate:** after finishing code, test, documentation, configuration, or generated-artifact changes and before considering the task complete,
  inspect the final diff and surrounding changed context against every `AGENTS.md` and `CODE_OF_CONDUCT.md` rule that applies to the requested scope,
  touched files, and changed behavior.
  Do not rely on scripts, search output, Checkstyle, Spotless, tests, or compilation alone as proof that the change satisfies semantic rules.
  Use tools such as `git diff`, `rg`, tests, Checkstyle, and Spotless to collect evidence and candidate violations,
  then make an explicit semantic judgment from the code, contracts, existing patterns, and user request.
  Verify that every actual change is simple, necessary, and directly tied to the user request or an explicitly applicable rule requirement.
  Keep necessary formatting changes inside touched hunks, but remove unrelated file or hunk changes introduced by the current task.
  If an applicable rule is violated, or if a change is meaningless, speculative, cosmetic, convenience-only, or unnecessary, fix or remove the current-task edit before completion.
  Do not treat reporting a violation, waiver, or residual risk as a substitute for fixing an in-scope issue that can be fixed safely.
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
- Use a bounded fresh-context or adversarial self-review to keep raising and resolving valuable in-scope questions until the stop condition is met.
  Stop when no actionable findings remain, the same findings repeat, 3 doubt cycles complete, or the user explicitly overrides.
- Before finishing any implementation task, compare the final diff against the declared or confirmed change boundary.
  Use `git diff` to verify the final changed file set and changed hunks against the declared or confirmed boundary.
  If any edit is outside that boundary and has no documented reason required by the task or an applicable rule,
  revert only the out-of-scope edits made in the current task, then rework the solution within the allowed scope.
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

## Governance Basics
- Follow the instruction order from Core Principle #7 and surface conflicts with rationale when they arise.
- Technical choices must satisfy ASF transparency: include license headers, document intent, and keep rationales visible to reviewers.
- Default to the smallest safe change: monthly feature trains plus weekly patch windows reward incremental fixes unless the product requires deeper refactors.
- Secure approvals for structural changes (new modules, configs, knobs); localized doc or code tweaks may land after self-review when you surface the evidence reviewers expect (tests, configs, reproduction steps).
- Maintain deterministic builds, measurable coverage, and clear rollback notes; avoid speculative work without a benefit statement.

## Verification & Commands
- Core commands: `./mvnw clean install -B -T1C -Pcheck` (full build), `./mvnw test -pl <module>` (scoped unit tests),
  `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=<TestClassName> test` (fast verification),
  `./mvnw -pl <explicit-module-set> -DskipTests package` (scoped packaging or smoke preparation).
- Reactor freshness strategy: prefer IDE/MCP current-source runs or precise `-pl <moduleA>,<moduleB>` Maven scopes.
  Use `-am` only when dependency freshness, missing local reactor artifacts, or CI-equivalent behavior cannot be proven otherwise,
  and normally run that freshness gate at most once per unchanged PR head before final handoff or mergeability judgment.
- Module selection: derive the explicit module set from changed modules, affected tests, and runtime entry modules.
  For multi-module checks, validate lower-level changed modules first and then higher-level adapter or runtime modules that consume them.
- Coverage: when tests change or targets demand it, run `./mvnw test jacoco:check@jacoco-check -Pcoverage-check`
  or scoped `-pl <explicit-module-set> -Djacoco.skip=false test jacoco:report`; pair with the Coverage & Branch Checklist.
- Format: after code or documentation changes, run `./mvnw spotless:apply -Pcheck -T1C`; do not use any other formatting method.
  This must be repeated after the last file-changing action before handoff.
- Style: after formatting, run `./mvnw checkstyle:check -Pcheck -T1C` when production, test, or project-rule files are touched.
- Scoped defaults: prefer module-scoped runs over whole-repo builds; include `-Dsurefire.failIfNoSpecifiedTests=false` when targeting specific tests.
- Testing ground rules: JUnit 5 + Mockito, `<ProductionClassName>Test` naming, Arrange–Act–Assert, mock external systems/time/network, reset static caches, and reuse swappers/helpers for complex configs.
- API bans: if a user forbids a tool/assertion, add it to the plan, avoid it during implementation, and cite verification searches (e.g., `rg assertEquals`) in the final report.

## Run & Triage Quick Sheet
- **Proxy quick start:** prefer an IDE/MCP `Bootstrap` run configuration or `./mvnw -pl proxy,<required-upstream-modules> package`
  when the module set is known; use `./mvnw -pl proxy -am package` only when dependency freshness cannot otherwise be proven.
  Report command, exit code, config path, and protocol.
- **JDBC smoke:** prefer IDE/MCP current-source test runs or `./mvnw -pl jdbc,<required-upstream-modules> test -Dtest=<TestClassName>`
  when the module set is known; use `-am` only as the freshness fallback. Note test name, datasource setup, and failure logs.
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

## Mocking & SPI Guidance
- Favor Mockito over bespoke fixtures; only add new fixture classes when mocks cannot express the scenario.
- Use marker interfaces when distinct rule/attribute types are needed; reuse SPI types such as `ShardingSphereRule` where possible.
- Name tests after the production method under test; never probe private helpers directly—document unreachable branches instead.
- Mock heavy dependencies (database/cache/registry/network) and prefer mocking over building deep object graphs.
- When static methods or constructors need mocking, prefer `@ExtendWith(AutoMockExtension.class)` with `@StaticMockSettings` (or the extension's constructor-mocking support); when a class is listed in `@StaticMockSettings`, do not call `mockStatic`/`mockConstruction` directly—stub via `when(...)` instead. Only if `AutoMockExtension` cannot be used and the reason is documented in the plan may you fall back to `mockStatic`/`mockConstruction`, wrapped in try-with-resources.
- Before coding tests, follow the Coverage & Branch Checklist to map inputs/branches to planned assertions.
- When a component is available via SPI (e.g., `TypedSPILoader`, `DatabaseTypedSPILoader`, `PushDownMetaDataRefresher`), obtain the instance through SPI by default; note any exceptions in the plan.
- If the class under test implements `TypedSPI` or `DatabaseTypedSPI`, instantiate it via `TypedSPILoader` or `DatabaseTypedSPILoader` instead of calling `new` directly.
- Do not mix Mockito matchers with raw arguments; choose a single style per invocation, and ensure the Mockito extension aligns with the mocking approach.
- Compliance is mandatory: before any coding, re-read AGENTS.md and convert all hard requirements (SPI usage, no FQCN, mocking rules, coverage targets, planning steps) into a checklist in the plan; do not proceed or report completion until every item is satisfied or explicitly waived by the user.
