# ShardingSphere Codex Development Guide

This repository guide is written for Codex. Keep only stable, repository-wide
rules here and rely on Codex for ordinary coding competence. Follow every
explicit rule literally; do not replace a repository rule with general judgment
unless that rule authorizes it. Paths are relative to the repository root.

## Instruction Sources

1. `CODE_OF_CONDUCT.md` is the authority for contribution, Java, and unit-test
   style. Inspect the applicable section before changing code or tests and
   record it when it controls a decision.
2. Before Maven, E2E, Proxy startup, database clients, IDE/MCP run
   configurations, commands likely to output more than 100 lines, or large
   structured analysis, read or reuse
   `.codex/context/token-efficiency.md` and follow its Mandatory Execution
   Contract.
3. Use repository Skills for specialized workflows instead of reproducing their
   detailed instructions here.
4. Keep task-specific notes in the task, issue, or PR. Do not add session notes
   to this file.

### Changing This Guide

For changes to this guide, use `.codex/harness/agents/`. `cases.toml` is the
source of the harness catalog: each case records its group, purpose, enforcement
phase, and whether the ordinary completion loop rechecks it. Run
`python3 .codex/harness/agents/run.py --list-cases` to render the table.

1. Treat every explicit requirement, prohibition, exception, authorization
   boundary, and verification step as an independent policy capability.
2. Before editing, build an old-to-new capability ledger and classify every
   capability as exactly preserved, changed with the user's explicit
   authorization in the current task, or restored before handoff.
3. Reconcile every deleted or weakened rule explicitly. A replacement is
   equivalent only when it preserves the same trigger, required or forbidden
   action, scope, exceptions, and verification obligation. A positive general
   rule does not replace a specific negative prohibition. Ordinary Codex competence,
   implication, nearby prose, a Skill, or a canary is not evidence that any
   explicit rule is preserved. Partial or implicit coverage is a regression.
4. Never remove, weaken, merge away, or broaden an exception to a capability
   unless the user explicitly authorizes that exact policy change. When
   equivalence is uncertain, keep the existing rule.
5. Before editing, capture a V0 policy baseline; then change one coherent
   instruction group.
6. Count a canary as passing only when its decision matches, every required
   action and reason is present, and every action outside its complete
   allowed-action set is absent. A partial forbidden-action list is
   insufficient.
7. Run all policy canaries and reject any critical regression. When the user
   explicitly authorizes a case-contract change, name only that case with
   `--authorized-contract-change`; every other changed or removed critical
   contract remains a regression.
8. Compare pass rate, duration, input tokens, and uncached input tokens with V0.
   Accept only candidates that preserve correctness; efficiency improvements
   count only after quality gates pass.
9. If the same failure appears twice, add a focused case instead of generic
   prose. Stop after five candidates or when no measurable improvement remains.
10. After the canaries pass, complete the applicable simplification, internal
    candidate review, finding-fix, and re-review steps in the Completion Loop
    before handoff.

## Response Style

- Use plain language and the shortest response that fully answers the request.
- Do not add details unless the user requests them or they are necessary.
- When details are necessary, put a self-contained concise answer above a line
  containing only `---` and the details below it. Omit the separator when no
  details follow.

## Authority and Safety

- Answer, explain, review, diagnose, audit, and plan requests are read-only.
  Inspect and report; do not edit the reviewed target.
- Change, build, implement, and fix requests authorize the smallest in-scope
  local production and test code edits plus non-destructive verification.
  Documentation, configuration, scripts, generated artifacts, file deletion,
  Docker cleanup, and system changes require explicit authorization in the
  current task. A request that names the exact non-code target is authorization
  for that target; do not ask again unless another gate below applies.
- Inspect `git status --short` before editing; the task-lifecycle gate below defines how to preserve unrelated or unattributed working-tree changes.

### Git Is Read-Only by Default

The user owns every Git state change. Codex may use read-only Git commands such
as `status`, `diff`, `show`, `log`, `blame`, `grep`, and `ls-files`.

Codex may run a Git state-changing command only when the current request
explicitly authorizes that exact operation and its exact target. The
authorization applies only to that operation in the current task; it does not
authorize prerequisite, follow-up, adjacent, or future Git writes. Resolve the
target with read-only inspection first and report the completed operation.

Without that exact authorization, never run a Git command that changes the
index, working tree, refs, remotes, branches, tags, stashes, worktrees, or
submodules. This prohibition includes `add`, `commit`, `push`, `fetch`, `pull`,
`merge`, `rebase`, `reset`, `restore`, `checkout`, `switch`, `clean`,
`cherry-pick`, `revert`, branch or tag mutation, stash mutation, worktree
mutation, and `submodule update`. Do not stage changes or use Git as a rollback
mechanism.

When a requested Git write lacks exact authorization, complete any separately
authorized local work, then provide a commit message or exact manual next step.
Proceed with the allowed parts of the request; omit the unauthorized Git
mutation rather than refusing the entire task. A request for only a commit
message is not authorization to commit.

### Remote Writes and Sensitive Data

- A local coding request never authorizes updating an issue, PR, review,
  repository, deployment, production API, connector, cloud task, message, or
  other remote state. Perform a remote write only when the current request
  explicitly names the action and exact target.
- For GitHub access, use the first configured token in this order: `GH_TOKEN`,
  then `GITHUB_TOKEN`; check it without printing, logging, persisting, or
  otherwise exposing its value. When a token is available, call the GitHub API
  directly and do not search for, inspect, or invoke `gh`; use `gh` only when
  neither token is configured.
- Do not transmit credentials, tokens, private keys, private logs, proprietary
  source, personal data, connection strings, or other sensitive repository data
  outside the active user-authorized Codex task, including to websites, search
  queries, connectors, plugins, MCP servers, review services, or external tools.
  Redact sensitive values from commands, summaries, and retained artifacts.
- Do not invoke an additional Codex task or external review service. The policy
  harness in `.codex/harness/agents/` is the only exception: it may run a
  separate isolated Codex task only with synthetic, non-sensitive policy cases
  in a read-only, ephemeral environment. Do not include source, logs, task data,
  or sensitive values in its prompt. Otherwise perform bounded self-review in
  the active user-authorized Codex task.

### Destructive and High-Risk Local Actions

Before deleting files or data, bulk-editing non-code artifacts, removing Docker
containers, changing global configuration, permissions, or packages, or
performing another destructive local action:

1. Resolve and inspect the exact targets with read-only commands.
2. State the impact and whether recovery is reliable.
3. Obtain explicit confirmation unless the user already authorized those exact
   resolved targets and a reliable recovery path exists.

A reliable recovery path is a verified backup or source plus concrete restore
steps, or a deterministic rebuild or re-download whose origin has been
confirmed. Git is a recovery path only when the current task separately
authorizes the exact restore operation. If the only copy would be lost, the
source is unknown, or recovery has not been verified, state that there is no
reliable rollback and confirm again before acting. Never imply that an
irreversible action is recoverable.

For Docker cleanup, distinguish reproducible images and containers from
persistent volumes or local data. When Docker cleanup is in scope, Codex may
remove images that inspection proves unused and reproducible without another
confirmation. This exception does not authorize removing containers, volumes,
or local data, or an image whose source or reproducibility is uncertain.

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

Maintain independent technical judgment. Do not agree with or adopt a
user-supplied premise, diagnosis, design, or conclusion merely because the user
proposed or prefers it; treat it as a claim to evaluate. When such a claim could
materially affect correctness, scope, compatibility, safety, or cost,
distinguish verified evidence from inference, assumption, and preference;
inspect contradictions, missing constraints, unsupported causal links, and
plausible alternatives.

If evidence contradicts the user's premise or is insufficient for the proposed
action, say so before acting and state the decisive evidence, likely impact,
and the minimum viable alternative, additional check, or decision needed. Do
not add generic caveats, expand scope or authority, or delay straightforward
authorized work merely to demonstrate skepticism.

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

### Strict Scope and Task-Delta Gate

Apply this gate to every file-changing task and action, including changes made
by formatters, generators, scripts, Skills, and review fixes, except a
standalone restoration or rollback governed by its existing gates.

For this gate, an active task is one independent, verifiable user objective,
not the permanent lifetime of a UI conversation. It includes every later turn
and correction for that objective, including after a handoff. Only after the
previous objective is complete and the user explicitly requests an independent
objective may Codex capture a new baseline and freeze a new boundary. When a
user request could reasonably be either a follow-up or an independent
objective, remain read-only and confirm the task boundary before writing. An
agent finding, review result, failure, or mention of another module is not an
independent objective and does not create that ambiguity.

Preserve pre-existing and unattributed working-tree changes throughout the task; the baseline and post-write audit below define how to identify them.

1. Derive the acceptance checklist only from user-requested outcomes, direct
   prerequisites proven by inspected evidence, and focused regression
   protection required by the Test Rules. Do not turn an agent-proposed
   cleanup, refactor, generalization, consistency improvement, or adjacent fix
   into an acceptance criterion.
2. Treat a prerequisite as direct only when omitting it would prevent the
   requested behavior, compilation, or scoped verification and no smaller
   in-boundary alternative exists. Relevance, repository evidence, a test
   failure, or a review finding may prove that expansion is required, but does
   not authorize that expansion.
3. Before the first write, record the pre-task working-tree baseline, including the existing status and relevant diffs, and derive the smallest owning module or repository-path set from the user request and inspected evidence, whether or not the user named it.
   Freeze the allowed files and the allowed change intent for each file as the hard write allowlist for the active Codex task; an allowed file does not authorize unrelated hunks in that file.
4. Later user turns, follow-up changes, reviews, failures, and further inspection in the same task do not reset or expand the baseline or boundary.
   Inspection and verification outside the boundary remain read-only.
   Never edit another module, including a sibling, shared, dependency, parent, root, or consuming module, unless the user explicitly authorizes the exact additional path and change intent before the edit.
   Treat that authorization as an append-only expansion: map it to an acceptance criterion, freeze only the smallest newly authorized path and intent, and retain the original task baseline.
   Never rebaseline an active task, infer whole-module authorization from an exact-file authorization, or treat an added path as blanket authorization.
   A direct prerequisite does not self-authorize expansion.
   The allowlist is a maximum boundary, not blanket authorization or a reason to repeat completed work; every later edit still needs an unsatisfied acceptance criterion.
   Do not repeat completed edits, checks, or reviews unless an authorized in-boundary edit invalidates them.
5. When the request makes the boundary clear, infer and freeze it without asking the user to repeat it.
   Every changed file and task-introduced hunk must map directly to one acceptance criterion and be necessary to satisfy it.
6. After each file-changing action, inspect the paths and hunks that action may
   have changed before continuing. Tool-produced changes are task changes and
   receive no scope exemption.
7. If evidence proves that work outside the frozen boundary is required, stop
   before that edit. Report the blocking evidence, smallest additional scope,
   exact files or contracts, and consequence of declining it, then request
   confirmation. Until expansion is authorized, limit outside-boundary work to
   the read-only evidence needed for that report; do not start its design or
   implementation workflow. Report other out-of-scope findings without fixing
   them.
8. After the last file-changing action, audit the task-introduced delta against
   the pre-task baseline, not merely the aggregate working-tree diff. Preserve
   every pre-existing user change and every later delta that cannot be proven
   to result from a current-task write. Absence from the baseline does not prove
   task ownership. Treat an unattributed delta as user-owned, do not overwrite,
   format, remove, or roll it back, and stop the affected write path to report
   the conflict. Remove only proven current-task changes that lack a direct
   acceptance-criterion mapping; if precise removal is unsafe or a tool keeps
   recreating them, stop and report the blocker.
9. Hand off only when the audited task delta is the smallest correct change.
   Summarize each remaining file as `file -> changed behavior -> acceptance
   criterion -> necessity` so the user can verify the scope directly.

### Unused and Removal Conclusions

Apply this gate to every analysis or change that classifies code, dependencies,
configuration, resources, test support, or another repository artifact as
unused or removable.

1. Distinguish absence of direct references from complete unused evidence. A
   single text or regex search, or a production-only search, proves at most that
   no direct reference was found and cannot justify a removal recommendation.
2. Inspect semantic and indirect consumers appropriate to the artifact,
   including reflection, generated code, registrations, SPI and `ServiceLoader`,
   JDBC driver discovery, Maven scopes, profiles, plugins and transitive
   dependencies, build/test/runtime classpaths, test JARs, packaging and
   distributions, tests, E2E, and external consumers.
3. Inspect Git history and linked issue, pull-request, CI, and failure evidence
   for prior additions, removals, and restorations. A prior removal failure
   makes the artifact indirectly required unless same-boundary evidence proves
   that the failure was unrelated or that the dependency is obsolete.
4. Classify each examined artifact as directly used, indirectly required,
   purpose unresolved, or a verified removal candidate. Use purpose unresolved
   when evidence is incomplete. Use verified removal candidate only after a
   controlled removal experiment, or equivalent existing evidence, covers the
   affected compilation, tests, packaging, and runtime paths.

### Architecture Change Gate

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
redesign from the last confirmed boundary. Restore through precise file edits
unless the current task separately authorizes the exact Git restore operation.

## Implementation Rules

### Task Code Size Limit

Do not add more than 10,000 physical lines across production and test source files in one active task unless the user explicitly authorizes an exact higher limit before the task exceeds it.

Before the first source-code write, estimate the total added source lines required; after every source-code write, measure task-introduced added lines against the active task's original baseline.

Count source output from formatters, generators, scripts, Skills, and later turns of the same task toward the limit; splitting code across files, modules, or turns does not reset or evade it.

If the projected or measured total exceeds the authorized limit, stop before further source-code writes, report the current and projected totals, name every affected path, and provide the smallest independently verifiable decomposition; do not split mechanically or expand the frozen scope without authorization.

### Codex Design Style

Apply this section to every production, test, script, or other implementation
created or changed within the authorized boundary. Treat an in-scope violation
in the effective candidate as a required finding and fix it before handoff; do
not expand scope or rewrite unrelated existing code.

- Within already-authorized designs, resolve trade-offs in this order: semantic
  and contract correctness; correct behavior and module ownership; verified
  compatibility and real boundaries; minimal conceptual surface and local
  readability; consistency with maintained nearby code and direct reuse;
  testability; then optional extensibility, formal symmetry, or structural
  completeness. A lower-priority concern must not compromise a higher-priority
  one. This order does not override authority, safety, scope, or architecture
  gates.
- Preserve existing architecture by default. Prefer direct reuse, then
  composition or delegation, then an existing extension point. Do not invent a
  boundary when no maintained precedent exists.
- Use the smallest clear implementation and minimize conceptual surface rather
  than line count. Prefer one readable local flow with the fewest independently
  meaningful types, states, representations, execution paths, and cross-file
  hops. Every added line, helper, abstraction, identifier, literal, guard, copy,
  wrapper, and configuration entry must serve production behavior, a public
  contract, regression protection, diagnostics, safety, readability, or removal
  of real duplication. Do not add code for formal symmetry, coverage appearance,
  structural completeness, hypothetical reuse, or test convenience. Avoid
  unnecessary locals, thin wrappers, helpers, comments, guards, and copies;
  inline single-use locals unless a name improves readability. Keep a name,
  local variable, or extraction when it materially clarifies intent; do not
  compress code mechanically.
- A new abstraction must own production behavior, state, or a contract that
  existing types cannot express and must represent a real, stable variation or
  boundary. Define the narrowest contract, keep common behavior in its owner,
  and isolate only true implementation or dialect differences. Forwarding,
  renaming, multiple callers, readability alone, direct test value, symmetry,
  type distinction, shorter call sites, test convenience, and anticipated reuse
  are insufficient. Reuse alone never authorizes changing `final`, visibility,
  inheritance, or shared-code ownership. Do not wrap a simple internal two-path
  flow in marker interfaces, result hierarchies, or DTO-style helpers unless
  they define a stable boundary, keep the owner readable, or remove meaningful
  duplication. This rule does not bypass the architecture-change gate.
- Do not introduce package-private top-level helper types by default. Keep a
  small single-owner helper private and nested; add a top-level helper only when
  the approved production behavior, state, or contract cannot reasonably be
  owned by an existing type.
- Do not add production types, widen visibility, remove `final`, or change
  constructors or signatures for test convenience. Avoid accumulating multiple
  nested collaborators inside one owner.
- Keep only constructors with distinct production semantics or framework,
  reflection, serialization, or SPI requirements. Update callers explicitly
  instead of adding convenience or compatibility overloads. Before adding or
  changing a constructor, inspect nearby production conventions for visibility,
  Lombok, validation, and tests. Before handoff, scan changed call sites for
  unused or compatibility-only constructors.
- Apply `Unused and Removal Conclusions` before classifying code as unused or removable.
  Make the change converge on one coherent model: correct or replace the existing owner before adding a parallel path, then remove superseded in-scope representations, paths, adapters, shims, tests, configuration, and other obsolete code after verifying usages and contracts.
  Keep coexistence only for a verified compatibility contract. If convergence
  exceeds the acceptance checklist or file-type authorization, stop at the
  existing gate. Do not leave placeholders, TODO implementations, speculative
  compatibility shims, or test-only production hooks.
- When adding a database, dialect, plugin, or module, reuse the applicable
  framework and extension mechanisms. Keep derived dialects aligned with shared
  behavior and isolate only real differences. If no maintained precedent
  exists, record the search evidence before using the architecture gate.
- Reuse or create an SPI only when its contract matches the required production
  behavior. One caller, one implementation, code sharing, type distinction,
  symmetry, anticipated reuse, or test convenience does not establish an SPI
  contract.

### Validation and Exceptions

- Add runtime validation only at real external, public, persisted, parsed, SPI,
  reflection, shared-state, or asynchronous boundaries, or for a concrete
  diagnostic benefit. Do not recheck invariants guaranteed by callers or
  upstream contracts.
- Prefer `ShardingSpherePreconditions` with lazy exception suppliers when the
  module can use it and the resulting control flow preserves the required
  exception type, message, timing, and cause.
- Keep a manual throw when the module cannot depend on `infra/exception`, the
  code is inside `ShardingSpherePreconditions`, a caller-facing exception
  contract requires it, or a precondition wrapper would obscure necessary
  control flow. Do not replace manual throws mechanically, and record the
  concrete reason for keeping one.
- When an edit removes the last checked-exception source from a private or
  internal method, remove the stale `throws` declaration and update callers.
  Keep checked exceptions on public or overridden methods only when a
  caller-facing, framework, external API, or compatibility contract requires
  them. Never widen to generic `Exception` or `Throwable`.

### Repository Style

- Apply the `CODE_OF_CONDUCT.md` Lombok preference to touched boilerplate when
  generated semantics are equivalent. Use narrow annotations; never use broad
  annotations such as `@Data` unless every generated behavior is required.
  Before replacing a public constructor or accessor, verify its generated
  signature, access, parameter order, annotations, and reflection or
  serialization behavior. Keep a manual member when Lombok would change or
  obscure logic, documentation, validation, defaults, side effects,
  compatibility, framework semantics, or a public contract.
- Keep public API and SPI Javadocs required by the code of conduct. Beyond that,
  document only caller or implementer obligations not expressed by code. Do not
  restate names, signatures, visible collection properties, or inherited
  contracts unless an override adds a new obligation.
- Keep declarations near first use. Do not mark local, loop, resource, or
  lambda variables `final`; use `final` only for method, constructor, and
  `catch` parameters when applicable.
- Declare collections by the least-specific required contract. Do not copy or
  wrap collections without an owned mutability, snapshot, isolation, or public
  contract reason. Record the concrete reason for every explicit copy or
  wrapper. Use `Collection` for common iteration, size, or emptiness operations;
  use `List` only for positional, ordered, duplicate-preserving, or required API
  semantics; use `Set` only for uniqueness or set semantics. Do not declare a
  concrete collection type unless its implementation-specific API is required.
  In tests, create a mutable copy only when that instance is mutated or
  mutability is the scenario.
- Add a YAML anchor only when aliases in the same file remove meaningful
  duplication.
- Use repository-relative paths, configuration, or temporary directories in
  code, tests, scripts, and Skills; never hard-code a local workspace path.
- Add the ASF license header to new source files and keep implementation intent
  and reviewer-relevant rationale transparent.
- Account for compatibility, security, time and space complexity, I/O, memory,
  resource lifecycle, concurrency, and boundary failures when the affected
  path makes them relevant.

### Documentation Wording

Apply this section whenever creating or revising documentation, Skills, prompts, comments, configuration descriptions, or other prose.

- Make each rule understandable to people and Codex on the first reading. State what to inspect, what action to take, what result identifies a problem, and how to verify it; labels such as `authority source`, `constraint strength`, `behavior value`, or `proper ownership` do not replace these instructions.
- State the core rule, fact, or action first. Follow it with necessary applicability conditions, exceptions, and verification methods in that order.
- Use established project technical terms. Do not coin uncommon umbrella terms; when an unavoidable technical term first appears, immediately explain what it means.
- Express one main rule per sentence and do not compress multiple decisions into a difficult long sentence. When one established term fully expresses a requirement, use it instead of enumerating synonymous actions or manifestations.
- Use professional, direct, specific, and concise language. Avoid bureaucratic or academic phrasing, excessive informality, and mechanical shortening that replaces precise technical language with casual wording.
- State each requirement once and merge adjacent statements when one repeats or fully includes another. For Skills, keep the core workflow and reference routing in `SKILL.md`, specific decisions in its referenced files, and concrete rules in `references/rules/`; do not duplicate the same content across files.
- Give every Skill and audit category a distinct, accurately named responsibility. Do not use vague names to hide overlapping responsibilities or duplicate work owned by another specialized Skill.
- Use lists only for genuinely parallel rules, categories, or steps, and use tables only when readers need a column-by-column comparison. Express content that fits in one sentence as prose instead of a field-style list.
- Make each heading name its specific subject and purpose without repeating its parent heading. A title such as `Validation` is insufficient when the section only covers YAML keys. In a short document, omit overview tables, key-point checklists, tables of contents, and repeated summaries that have no independent purpose.
- Keep an exception only when a real allowed case has been verified. State why it is allowed and its exact boundary; do not add speculative or overly broad exceptions.
- Keep an example only when it resolves ambiguity, adds an independent decision condition, or materially helps execution. Otherwise remove it or separate it from the rule.
- Simplification must preserve the original scope, obligation strength, valid exceptions, and verification requirements. Do not shorten mechanically or make readers infer a rule that the original text stated explicitly.
- Keep each new or changed complete sentence on one physical line, and break a line only after punctuation ends the complete sentence. Do not reflow untouched text merely to make formatting consistent. Never modify ASF license text or its internal formatting.
- Before handoff, review every changed prose sentence and rewrite any sentence that does not let a reader directly identify what to inspect, what result is a problem, which exceptions apply, and how to verify it.

## Test Rules

- Test behavior owned by the production class: computation, decisions, validation, transformation, state transitions, error handling, or external contracts.
  Each test must fail for a realistic regression that matters.
  Do not add tests that only prove pass-throughs, constants, accessors, delegation, wiring, Java, Lombok, Mockito, parsers, collection libraries, framework behavior, or private implementation shape.
  A documented public or externally visible contract may justify testing pass-through behavior; contract literals are exceptions only when no broader behavior can protect them.
  Never add a test solely to increase a coverage number or duplicate an existing scenario unless it covers a new branch, input class, edge case, contract, calculation path, or failure mode.
- Never add tests whose subject is another test case, a test class or method,
  test fixture, mock helper, test utility, or other test-only code. Test only
  the production behavior that the test-only code supports. Test-support code
  distributed as an independent artifact with an external contract is
  production code for this rule.
- Do not test collaborator rules through the current class. Mock the nearest
  stable boundary and test the collaborator rule in its owner. Cross-layer
  behavior belongs in an explicitly scoped integration, contract, or E2E test.
- Give each behavior-owning public production method focused coverage. Each
  test method covers one scenario and invokes the target public method at most
  once; repeat only when the same scenario requires additional assertions. Do
  not create interface-only tests; exercise concrete implementations.
- Every new public production type requires direct focused tests, except
  exception types covered by `Exception Tests`. Broad workflow tests do not
  replace them unless they explicitly exercise that type's public behavior. If
  a pure pass-through public type has no meaningful owned or external contract
  to test, do not add the type merely for structural completeness.
- Default to direct Mockito mocks. Use a private helper only for repeated local
  setup and a standalone fixture only for a stable external or packaged test
  boundary. Give fixtures the narrowest practical visibility, keep them in the
  nearest owning test package or module, and do not create cross-module test
  APIs for convenience. Delete or inline thin mock wrappers.
- Obtain SPI implementations through the project loader by default. Use
  `Plugins.getMemberAccessor()` for permitted field access; direct reflection
  APIs and reflective invocation of private methods are forbidden. If the class
  under test implements `TypedSPI` or `DatabaseTypedSPI`, instantiate it through
  `TypedSPILoader` or `DatabaseTypedSPILoader`, not with `new`.
- Prefer `AutoMockExtension` and its static or construction mocking support.
  Use direct `mockStatic` or `mockConstruction` only when the extension cannot
  apply and the reason is recorded; scope it with try-with-resources. When a
  class is listed in `@StaticMockSettings`, do not call `mockStatic` or
  `mockConstruction` for it; stub it through `when(...)`.
- Do not mix Mockito matchers with raw arguments in one invocation. Mock
  databases, caches, registries, network calls, time, and other heavy external
  dependencies instead of constructing deep unrelated object graphs.
- Every unit-test class must be named `<ProductionClassName>Test`, using the
  exact simple name of the production class it directly tests. This class-name
  rule is mandatory and is independent of scenario-focused test-method naming.
- Parameterized tests must set `name = "{0}"`. Keep test names concise and
  scenario-focused, following `CODE_OF_CONDUCT.md`; avoid `ReturnsXXX` and
  wording that restates the expected result instead of naming the scenario.
- Use JUnit 5 and Mockito. Keep setup, action, and assertions distinct; reset
  static state between scenarios and reuse existing swappers or helpers for
  complex configuration.
- When a coverage target is stated, list every branch or path before coding and
  map each one to exactly one planned test. Add cases until every declared
  branch is covered or explicitly waived, update the map when code changes, and
  verify with JaCoCo when coverage is uncertain. Document unreachable code
  instead of adding redundant tests.

### Exception Tests

- Do not add dedicated tests for exception classes that only declare
  constructors or format and forward their arguments to a tested superclass.
  Add direct tests only for owned validation, branching, calculation,
  conversion, or a known regression.

## Specialized Workflows

### Repository Workflows

Use the matching repository Skill when its trigger applies:

- Issue diagnosis and copy-ready maintainer replies: `$analyze-issue`.
- Unit-test generation or systematic coverage work: `$gen-ut`.
- PR correctness, side effects, mergeability, GitHub review replies, or the
  pre-handoff review of an authorized implementation targeting an existing PR:
  `$review-pr`. For pre-handoff review, use its Local Candidate Preflight Mode.

If a matching repository Skill is unavailable, apply an equivalent manual
checklist, record the fallback in the plan or final response, and continue
without installing it. Do not create a new Skill merely to hold task-specific
instructions.

### Optional Cross-Cutting Skills

The Skills in this subsection are optional workflow accelerators, not
prerequisites. Availability alone is not a trigger. When an optional Skill is
available and its trigger below applies, prefer it and use only the smallest
matching Skill set. If it is unavailable, skip the Skill itself and continue
with the applicable repository gates and ordinary workflow. Do not install,
create, reconstruct, or treat the absence of an optional Skill as a blocker.
Mention the absence only when the user explicitly requested the Skill or it
leaves a material residual risk.

Skipping a Skill never waives compatibility, correctness, security, scope,
verification, or completion requirements. Using a Skill never expands the
frozen task scope, allowed modules or files, acceptance checklist, write or
remote authority, Git authority, or tool permissions. This guide prevails when
a Skill conflicts with it.

- Implementation or review whose correctness depends on the current version of
  an external framework or library: use `$source-driven-development` when
  available. Identify the applicable version from repository dependency
  metadata, verify the relevant decision against authoritative primary
  documentation, record the source, and mark anything that cannot be verified
  as unverified. This verification remains required without the Skill. Do not
  invoke it for version-independent local logic.
- Designing, adding, or changing a public API, SPI, extension, loading or
  registration contract, module boundary, externally visible interface, or
  cross-module type contract: use `$api-and-interface-design` when available
  before implementation. Apply the Architecture change and Contract and Impact
  gates whether or not the Skill is available; using it does not authorize the
  change or expand the confirmed scope.
- An unexpected test, build, runtime, or behavior failure: use
  `$debugging-and-error-recovery` when available. Stop unrelated implementation,
  preserve evidence, reproduce, localize, reduce, identify the root cause, and
  verify the result. Add focused regression protection only when the root cause
  is an in-scope production defect and the test protects meaningful owned
  behavior; do not add a test for an environment, infrastructure, or unrelated
  failure. A failure never authorizes an out-of-boundary fix or a Git write.
- An explicit performance requirement, reproducible reported slowness or
  suspected regression, profiling evidence, or a requested change that can
  materially alter the cost of a confirmed performance-sensitive path: use
  `$performance-optimization` when available to measure first. A path being
  high-volume by itself is not a trigger. Freeze the workload, metric,
  environment, and baseline; identify the owning bottleneck before editing;
  test one hypothesis at a time; and repeat the same measurement with
  correctness checks. Keep a change only when the improvement exceeds
  run-to-run variance and correctness remains intact. Remove neutral, worse,
  or incorrect current-task experiments. Record attempts in the task unless an
  exact file or remote target is separately authorized. Do not optimize by
  intuition or follow adjacent hotspots outside the frozen boundary.
- After scoped behavior and checks pass, when the effective task delta contains
  concrete unnecessary complexity or a review identifies it: use
  `$code-simplification` when available on that delta only. Preserve behavior
  exactly and never perform a drive-by refactor or gain Git authority from the
  Skill. Do not invoke it merely because the Completion Loop is running or when
  the candidate is already clear.
- An explicit user request to threat model a repository or path, enumerate
  threats or abuse paths, or perform AppSec threat modeling: use
  `$security-threat-model` when available. Do not invoke it for an ordinary
  architecture summary, code review, security check, or non-security design.
  The request remains read-only unless the user also authorizes the exact
  output file; the Skill's default artifact creation grants no write authority.
- A non-trivial decision involving module boundaries, public contracts,
  unfamiliar code, or high-risk behavior, or a branching invariant that is not
  directly established by types, existing tests, or an explicit contract and
  whose failure would materially affect correctness, compatibility, security,
  or another high-risk behavior: use `$doubt-driven-development` when available
  only within the active task. An ordinary branch is not a trigger. The
  prohibition on additional Codex tasks and external review overrides its
  fresh-context and cross-model steps. Perform bounded adversarial self-review
  instead and do not report the full Skill as completed.

Do not include sensitive repository data in external searches.

## Contract and Impact Gates

- For a public or externally visible identifier, search all affected reference
  surfaces: APIs, SQL, configuration and YAML keys, SPIs, errors, CLI commands,
  resources, documentation, examples, distributions, tests, E2E, and
  baselines. Exclude `.git` and `target`. If a compatibility alias remains,
  state whether it is discoverable; if it must stay hidden, protect that
  contract with a focused test or check.
- Name the affected database engines and dialects and preserve backward
  compatibility for their supported versions unless an exact compatibility
  break is authorized.
- For errors, logs, HTTP or JSON payloads, CLI output, and exception conversion,
  test the complete external output when it could expose credentials, tokens,
  connection strings, SQL, paths, or user data.
- Regenerate or verify affected snapshots, golden files, fingerprints, SQL
  cases, descriptors, schemas, and agent-visible metadata with the existing
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
  metadata freshness, parser dialect, and the owning data-flow step. For routing
  failures, also inspect feature-rule configuration and report the SQL, relevant
  configuration, owning module, and focused test. Do not edit generated output.
- For a sandbox or network denial, report the command, failure, and safe
  alternative or required authorization.
- When an E2E, integration, client smoke, or Docker smoke fails, hangs, or times
  out, stop rerunning. Classify it as environment, classpath, stale snapshot,
  dependency, test design, protocol implementation, data setup, assertion
  logic, or external-service behavior, then record evidence and minimum fix
  scope before changing code or configuration. Complete deterministic
  prerequisites such as classpath consistency, stale-bytecode checks, dependency
  alignment, or required evidence capture before running one matching sentinel;
  if it fails unexpectedly, return to analysis.

## Verification and Commands

Run the narrowest meaningful checks first. Derive explicit Maven modules from
changed owners, affected tests, and consuming runtime modules.

- Focused test:
  `./mvnw -pl <module> -DskipITs -Dspotless.skip=true
  -Dtest=<FullyQualifiedTestClassName>
  -Dsurefire.failIfNoSpecifiedTests=false test`
- Scoped tests: `./mvnw test -pl <explicit-module-set>`
- Scoped package:
  `./mvnw -pl <explicit-module-set> -DskipTests package`
- Coverage:
  `./mvnw -pl <explicit-module-set> -Djacoco.skip=false test jacoco:report`
- Full build: `./mvnw clean install -B -T1C -Pcheck`

Prefer current-source IDE/MCP runs or explicit `-pl` module sets. Use `-am`
only when dependency freshness, missing reactor artifacts, or CI equivalence
cannot otherwise be established, normally once per unchanged task state. For
multi-module checks, verify lower-level changed owners before their higher-level
consumers.

Keep background unit tests under 60 seconds. Capture high-volume output
according to `.codex/context/token-efficiency.md`; report commands, exit codes,
and decisive log excerpts instead of dumping raw logs.

For every user-forbidden tool, API, assertion, or pattern, run a scoped final
search and report the command and result. Do not rely only on plan compliance.

After the last file-changing action:

1. Run `./mvnw spotless:apply -Pcheck -T1C` for code or documentation changes.
2. Run `./mvnw checkstyle:check -Pcheck -T1C` when production, test, or
   project-rule files changed.
3. Do not manually reformat afterward. Any later edit invalidates formatting
   and requires the applicable checks again.

## Completion Loop

For an authorized change, build, implement, or fix request, excluding a
standalone restoration or rollback:

1. Perform the post-write task-delta audit required by `Strict Scope and Task-Delta Gate`, using the read-only `git diff`, surrounding context, and acceptance checklist as evidence.
2. Confirm every task-introduced file and hunk is necessary, expected changed
   files contain only their frozen change intent, prohibited paths have zero
   task delta, direct reuse was considered, architecture changes were
   authorized, and every test protects owned behavior. Inspect Java changes for
   newly added local-variable `final`, scan changed code and Skills for local
   absolute paths, and remove only current-task violations.
3. Treat scripts, searches, formatting, compilation, and passing tests as
   evidence, not proof of semantic compliance. Judge the final behavior,
   contracts, architecture, and user request directly; fix every safe in-scope
   violation instead of reporting it as an accepted risk.
4. Apply the `$code-simplification` trigger and limits defined under `Optional Cross-Cutting Skills`.
5. Review the effective local candidate against the same code-correctness gates
   used by `$review-pr`: root cause and fix mapping, affected behavior, side
   effects and regressions, contracts and architecture, test validity, and
   adversarial cases. Apply `$code-review-and-quality` when available, or its
   equivalent review, for the general quality axes; it does not replace these
   shared correctness gates.
6. When the implementation targets an existing PR, apply the `$review-pr` pre-handoff workflow defined under `Repository Workflows`.
   PR-specific public-head and remote-evidence checks remain part of formal PR review.
7. Fix every safe in-scope required finding, rerun invalidated checks, and
   repeat the applicable reviews. If a finding requires scope expansion, an
   unresolved architecture choice, or a high-risk action, stop at its existing
   authorization gate instead of fixing it automatically.
8. Hand off and propose a commit message only after one complete applicable
   review pass finds zero new required issues. Before that review passes, do not
   hand off or propose a commit message. Do not defer a locally discoverable
   required finding to a later formal PR review.
9. Stop when no required in-scope finding remains. Do not iterate for optional
   polish, broad cleanup, or risky refactoring.

If a report or verdict is disproved, fix the highest-leverage rule, schema,
validator, prompt, or regression case before correcting the artifact, unless
the user explicitly requests a one-off correction.

The final response must lead with the outcome and include changed files and
rationale, commands with exit codes, verification status, remaining risks, and
the next action only when one is still required. When Git commit authorization
is absent, provide a proposed commit message without staging or committing.
