# ShardingSphere Codex Development Guide

This repository guide is written for Codex using GPT-5.6 Sol. Keep only stable,
repository-wide rules here and rely on Codex for ordinary coding competence.
Paths are relative to the repository root.

## Instruction Sources

1. `CODE_OF_CONDUCT.md` is the authority for contribution, Java, and unit-test
   style. Inspect the applicable section before changing code or tests.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP run
   configurations, commands likely to output more than 100 lines, or large
   structured analysis, read or reuse
   `.codex/context/token-efficiency.md` and follow its Mandatory Execution
   Contract.
3. Use repository Skills for specialized workflows instead of reproducing their
   detailed instructions here.
4. Keep task-specific notes in the task, issue, or PR. Do not add session notes
   to this file.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only.
  Inspect and report; do not edit the reviewed target.
- Change, build, implement, and fix requests authorize the smallest in-scope
  local production and test code edits plus non-destructive verification.
  Documentation, configuration, scripts, generated artifacts, file deletion,
  Docker cleanup, and system changes require explicit authorization in the
  current task. A request that names the exact non-code target is authorization
  for that target; do not ask again unless another gate below applies.
- Preserve unrelated working-tree changes. Inspect `git status --short` before
  editing and never discard work whose ownership is uncertain.

### Git Is Read-Only

The user owns every Git state change. Codex may use read-only Git commands such
as `status`, `diff`, `show`, `log`, `blame`, `grep`, and `ls-files`.

Never run a Git command that changes the index, working tree, refs, remotes,
branches, tags, stashes, worktrees, or submodules. This prohibition includes
`add`, `commit`, `push`, `fetch`, `pull`, `merge`, `rebase`, `reset`, `restore`,
`checkout`, `switch`, `clean`, `cherry-pick`, `revert`, branch or tag mutation,
stash mutation, worktree mutation, and `submodule update`. Do not stage changes
or use Git as a rollback mechanism.

When asked to commit, push, restore, or otherwise mutate Git, do not perform the
Git action. Complete any separately authorized local edits, then provide the
user with a commit message or exact manual next step.

### Remote Writes and Sensitive Data

- A local coding request never authorizes updating an issue, PR, review,
  repository, deployment, production API, connector, cloud task, message, or
  other remote state. Perform a remote write only when the current request
  explicitly names the action and exact target.
- Do not send credentials, tokens, private keys, private logs, proprietary
  source, personal data, connection strings, or other sensitive repository data
  to any model, website, search query, connector, plugin, MCP server, review
  service, or external tool beyond the active user-authorized Codex/Sol task.
  Redact sensitive values from commands, summaries, and retained artifacts.
- Do not invoke an additional model or cross-model review service. Perform
  bounded self-review in the active Codex/Sol task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker
images or containers, changing global configuration, permissions, or packages,
or performing another destructive local action:

1. Resolve and inspect the exact targets with read-only commands.
2. State the impact and whether recovery is reliable.
3. Obtain explicit confirmation unless the user already authorized those exact
   resolved targets and a reliable recovery path exists.

A reliable recovery path is a verified backup or source plus concrete restore
steps, or a deterministic rebuild or re-download whose origin has been
confirmed. Git is not a recovery path for Codex because Git writes are
forbidden. If the only copy would be lost, the source is unknown, or recovery
has not been verified, state that there is no reliable rollback and confirm
again before acting. Never imply that an irreversible action is recoverable.

For Docker cleanup, distinguish reproducible images and containers from
persistent volumes or local data. Broad requests such as “clean whatever is
unused” authorize inspection only, not deletion.

Use this prompt when confirmation is required:

```text
Dangerous operation detected!
Operation type: [specific action]
Scope of impact: [exact targets]
Recovery: [verified steps, or "no reliable rollback"]
Risk assessment: [potential consequence]
Please confirm whether to continue.
```

## Evidence, Scope, and Planning

Before editing:

1. Restate the verifiable goal, non-goals, user-forbidden tools or APIs, and
   coverage or output constraints.
2. Inspect the affected code, tests, contracts, configuration, registrations,
   module boundaries, closest maintained precedent, and applicable
   instructions.
3. Identify behavior owners, reuse opportunities, compatibility impact,
   expected changed files, prohibited paths, required tests, and scoped
   verification.
4. Convert these items into a compact acceptance checklist and a 3–10 step plan
   for non-trivial work.

When the request makes the boundary clear, infer and record it without asking
the user to repeat it. The checklist is the maximum change boundary. Every
changed file and hunk must satisfy it; nearby cleanup and unrelated failures do
not expand it. If evidence proves that work outside the boundary is required,
stop before that edit, report the required expansion, and request confirmation.

Changes to public contracts, SPIs, extension or loading contracts, class or
method `final`, visibility, inheritance, constructors or signatures, module
dependencies, or shared-code ownership are architecture changes. Before such a
change, report:

- current behavior and owner;
- direct-reuse and delegation analysis;
- compatibility impact;
- minimum affected files and tests.

An exact architecture change explicitly requested by the user is authorized
after that report. If materially different ownership, architecture, or
compatibility choices remain unresolved, obtain confirmation. Adding,
changing, or removing an SPI always follows this gate.

If the user rejects a design, stop patching it. Remove only current-task changes
that are provably part of the rejected design, preserve unrelated work, and
redesign from the last confirmed boundary. Because Git writes are forbidden,
perform any authorized restoration through precise file edits.

## Implementation Rules

- Preserve existing architecture by default. Prefer direct reuse, then
  composition or delegation, then an existing extension point. Do not invent a
  boundary when no maintained precedent exists.
- Use the smallest clear implementation. Every added line, helper, abstraction,
  identifier, literal, guard, copy, wrapper, and configuration entry must serve
  production behavior, a public contract, regression protection, diagnostics,
  safety, readability, or removal of real duplication.
- A new abstraction must own production behavior, state, or a contract that
  existing types cannot express. Symmetry, type distinction, shorter call
  sites, test convenience, and anticipated reuse are insufficient.
- Do not add production types, widen visibility, remove `final`, or change
  constructors or signatures for test convenience. Keep tiny single-owner
  helpers private and nested; avoid accumulating nested collaborators.
- Keep only constructors with distinct production semantics or framework,
  reflection, serialization, or SPI requirements. Update callers explicitly
  instead of adding convenience or compatibility overloads.
- Before declaring code unused, inspect semantic usages and repository-wide
  references, including method references, generated accessors, overrides,
  reflection, registrations, tests, E2E, and external consumers.
- Remove obsolete in-scope code after verifying usages. Do not leave
  placeholders, TODO implementations, speculative compatibility shims, or
  test-only production hooks.

### Validation and Exceptions

- Add runtime validation only at real external, public, persisted, parsed, SPI,
  reflection, shared-state, or asynchronous boundaries, or for a concrete
  diagnostic benefit.
- Prefer `ShardingSpherePreconditions` with lazy exception suppliers when the
  module can use it and the resulting control flow preserves the required
  exception type, message, timing, and cause.
- Keep a manual throw when the module cannot depend on `infra/exception`, the
  code is inside `ShardingSpherePreconditions`, a caller-facing exception
  contract requires it, or a precondition wrapper would obscure necessary
  control flow. Do not replace manual throws mechanically.
- When an edit removes the last checked-exception source from a private or
  internal method, remove the stale `throws` declaration and update callers.
  Keep checked exceptions on public or overridden methods only when a
  caller-facing, framework, external API, or compatibility contract requires
  them. Never widen to generic `Exception` or `Throwable`.

### Repository Style

- Apply the `CODE_OF_CONDUCT.md` Lombok preference to touched boilerplate when
  generated semantics are equivalent. Use narrow annotations; never use broad
  annotations such as `@Data` unless every generated behavior is required.
- Keep public API and SPI Javadocs required by the code of conduct. Beyond that,
  document only caller or implementer obligations not expressed by code.
- Keep declarations near first use. Do not mark local, loop, resource, or
  lambda variables `final`; use `final` only for method, constructor, and
  `catch` parameters when applicable.
- Declare collections by the least-specific required contract. Do not copy or
  wrap collections without an owned mutability, snapshot, isolation, or public
  contract reason.
- Add a YAML anchor only when aliases in the same file remove meaningful
  duplication.
- Use repository-relative paths, configuration, or temporary directories in
  code, tests, scripts, and Skills; never hard-code a local workspace path.
- Add the ASF license header to new source files and keep implementation intent
  and reviewer-relevant rationale transparent.
- Account for compatibility, security, time and space complexity, I/O, memory,
  resource lifecycle, concurrency, and boundary failures when the affected
  path makes them relevant.

## Test Rules

- Test behavior owned by the production class: computation, decisions,
  validation, transformation, state transitions, error handling, or external
  contracts. Each test must fail for a realistic regression that matters.
- Do not add tests that only prove constants, accessors, delegation, wiring,
  Java, Lombok, Mockito, parsers, collection libraries, framework behavior, or
  private implementation shape. Contract literals are exceptions only when no
  broader behavior can protect them. Never add a test solely to increase a
  coverage number.
- Do not test collaborator rules through the current class. Mock the nearest
  stable boundary and test the collaborator rule in its owner. Cross-layer
  behavior belongs in an explicitly scoped integration, contract, or E2E test.
- Give each behavior-owning public production method focused coverage. Each
  test method covers one scenario and normally invokes the target public method
  once. Do not create interface-only tests; exercise concrete implementations.
- Every new public production type requires direct focused tests unless it is a
  pure pass-through excluded by the meaningful-test gate.
- Default to direct Mockito mocks. Use a private helper only for repeated local
  setup and a standalone fixture only for a stable external or packaged test
  boundary. Do not create thin mock wrappers.
- Obtain SPI implementations through the project loader by default. Use
  `Plugins.getMemberAccessor()` for permitted field access; direct reflection
  APIs and reflective invocation of private methods are forbidden.
- Prefer `AutoMockExtension` and its static or construction mocking support.
  Use direct `mockStatic` or `mockConstruction` only when the extension cannot
  apply and the reason is recorded; scope it with try-with-resources.
- Parameterized tests must set `name = "{0}"`. Keep test names concise and
  scenario-focused, following `CODE_OF_CONDUCT.md`.
- When a coverage target is stated, map every branch to a planned test before
  coding and verify with JaCoCo when coverage is uncertain.

## Specialized Workflows

Use the matching repository Skill when its trigger applies:

- Issue diagnosis and copy-ready maintainer replies: `$analyze-issue`.
- Unit-test generation or systematic coverage work: `$gen-ut`.
- PR correctness, side effects, mergeability, or GitHub review replies:
  `$review-pr`.

If a named Skill is unavailable, apply an equivalent manual checklist, record
the fallback in the plan or final response, and continue without installing it.
Do not create a new Skill merely to hold task-specific instructions.

When implementation decisions depend on a current external framework or
library, use authoritative documentation and record the relevant source. Do
not include sensitive repository data in external searches.

## Contract and Impact Gates

- For a public or externally visible identifier, search all affected reference
  surfaces: APIs, SQL, configuration and YAML keys, SPIs, errors, CLI commands,
  resources, documentation, examples, distributions, tests, E2E, and
  baselines. Exclude `.git` and `target`.
- For errors, logs, HTTP or JSON payloads, CLI output, and exception conversion,
  test the complete external output when it could expose credentials, tokens,
  connection strings, SQL, paths, or user data.
- Regenerate or verify affected snapshots, golden files, fingerprints, SQL
  cases, descriptors, schemas, and model-visible metadata with the existing
  project tool.
- Determine affected GitHub Actions from changed-file path filters and job
  commands. Run the local equivalent when practical; otherwise record the
  narrower check and residual risk. Do not update remote workflow state.
- When runtime paths change, record relevant engine or dialect compatibility
  and a performance baseline or guardrail. When governance, registry,
  observability, or agent integrations are touched, state their impact.

## Runtime Triage

- For Proxy startup, prefer the existing IDE/MCP `Bootstrap` configuration or a
  scoped `proxy` package with explicit upstream modules. Record the
  configuration path, mode, ports, command, and exit code.
- For JDBC smoke tests, use a current-source IDE/MCP run or a focused `jdbc`
  module test with explicit upstream modules and datasource setup.
- Keep standalone `server.yaml` and affected cluster `mode/` configuration
  behavior aligned; call out default changes.
- For startup, routing, or runtime failures, inspect `proxy/logs/` and relevant
  `target/surefire-reports`; correlate decisive lines with configuration,
  metadata freshness, parser dialect, and the owning data-flow step. Do not
  edit generated output.
- When an E2E, integration, client smoke, or Docker smoke fails, hangs, or times
  out, stop rerunning. Classify the failure and record evidence and minimum fix
  scope before changing code or configuration. After deterministic
  prerequisites pass, run one matching sentinel; if it fails unexpectedly,
  return to analysis.

## Verification and Commands

Run the narrowest meaningful checks first. Derive explicit Maven modules from
changed owners, affected tests, and consuming runtime modules.

- Focused test:
  `./mvnw -pl <module> -DskipITs -Dspotless.skip=true
  -Dtest=<TestClassName> -Dsurefire.failIfNoSpecifiedTests=false test`
- Scoped tests: `./mvnw test -pl <explicit-module-set>`
- Scoped package:
  `./mvnw -pl <explicit-module-set> -DskipTests package`
- Coverage:
  `./mvnw -pl <explicit-module-set> -Djacoco.skip=false test jacoco:report`
- Full build: `./mvnw clean install -B -T1C -Pcheck`

Prefer current-source IDE/MCP runs or explicit `-pl` module sets. Use `-am`
only when dependency freshness, missing reactor artifacts, or CI equivalence
cannot otherwise be established, normally once per unchanged task state.

Keep background unit tests under 60 seconds. Capture high-volume output
according to `.codex/context/token-efficiency.md`; report commands, exit codes,
and decisive log excerpts instead of dumping raw logs.

After the last file-changing action:

1. Run `./mvnw spotless:apply -Pcheck -T1C` for code or documentation changes.
2. Run `./mvnw checkstyle:check -Pcheck -T1C` when production, test, or
   project-rule files changed.
3. Do not manually reformat afterward. Any later edit invalidates formatting
   and requires the applicable checks again.

## Completion Loop

For an authorized implementation:

1. Compare the read-only `git diff` and surrounding context with the acceptance
   checklist.
2. Confirm every hunk is necessary, prohibited paths have zero diff, direct
   reuse was considered, architecture changes were authorized, and every test
   protects owned behavior.
3. Apply `$code-simplification` when available, or its equivalent checklist, to
   remove unnecessary complexity without changing behavior.
4. Apply `$code-review-and-quality` when available, or an equivalent review,
   before handoff and fix every safe in-scope required finding.
5. Rerun checks invalidated by a fix, then repeat the semantic diff review.
6. Stop when no required in-scope finding remains. Do not iterate for optional
   polish, broad cleanup, or risky refactoring.

For changes to this guide, use `.codex/harness/agents/`:

1. Capture a V0 policy baseline before editing.
2. Change one coherent instruction group.
3. Run all policy canaries and reject any critical regression.
4. Compare pass rate, duration, input tokens, and uncached input tokens with V0.
5. Accept only candidates that preserve correctness; efficiency improvements
   count only after quality gates pass.
6. If the same failure appears twice, add a focused case instead of generic
   prose. Stop after five candidates or when no measurable improvement remains.

If a report or verdict is disproved, fix the highest-leverage rule, schema,
validator, prompt, or regression case before correcting the artifact, unless
the user explicitly requests a one-off correction.

The final response must lead with the outcome and include changed files and
rationale, commands with exit codes, verification status, remaining risks, and
the next action only when one is still required. When changes are ready, provide
a proposed Git commit message; never stage or commit them.
