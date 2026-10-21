---
name: gen-ut
description: >-
  Generate standard unit tests for one or more target classes in Apache ShardingSphere;
  cover requested behavior and every affected SUT-owned branch, enforce an explicitly requested numeric coverage target, and pass quality gates;
  perform explicit merge analysis, suitability filtering, and refactor optimization for parameterized tests.
---

# Generate Unit Tests

## Inputs and Scope

Require target production classes, preferably as fully-qualified names. Accept an optional module and optional test-class execution filter.
Discover related tests by the exact `<TargetClassName>Test` convention and update them in place; create that class only when none exists.

Resolve:

- `<ResolvedTargetClasses>`: requested production classes.
- `<ResolvedTestFileSet>`: only related test Java files and required test resources that may be edited.
- `<ResolvedTestModules>`: explicit Maven modules owning those tests.
- `<ResolvedTestClass>`: focused test-class filter for execution.

Use an explicitly supplied module first. Otherwise resolve the nearest owning `pom.xml` from test files, then target sources.
If target classes or modules cannot be resolved, return `R10-INPUT_BLOCKED`.

## Ownership Terms

- `SUT-owned behavior`: decisions, branches, state changes, calls, results, or error handling owned by the target class.
- `Collaborator-owned behavior`: behavior computed by an SPI, registry, factory, parser, loader, driver, dialect, metadata option, or another dependency.
- `Testing through layers`: driving or asserting collaborator-owned rules instead of mocking the result consumed by the target.
- `KEEP:<id>:<reason>`: evidence for retaining an otherwise redundant candidate because removal materially harms readability or diagnosis.
- `Task scope baseline`: structured pre-edit snapshot of the allowed test files and every dirty path outside them.

## Mandatory Rules

`MUST`, `SHOULD`, and `MAY` are normative. This section is the source of `R1-R15`; workflow and command examples do not override it.

### R1: Repository authority

Before any test write, read `AGENTS.md` and [code-implementation/SKILL.md](../code-implementation/SKILL.md) through EOF, then read every reference selected by that base Skill for this task through EOF, including its implementation, testing, contract, impact, removal, non-regression, and verification rules.
Follow the applicable `CODE_OF_CONDUCT.md` sections.
The base Skill owns universal implementation, testing, non-regression, verification, and completion requirements; this Skill adds target resolution, coverage, branch-map, parameterization, and scanner requirements for systematic unit-test generation.
Reading either Skill does not expand the user-authorized scope, file types, Git authority, or other permissions.

### R2: Test form and naming

Apply the Unit Testing Standards in [CODE_OF_CONDUCT.md](../../../CODE_OF_CONDUCT.md#unit-testing-standards) to test forms, naming, parameter sources, scenario dispatch, and nested types.

### R3: Change boundary

- Edit only `<ResolvedTestFileSet>` under `src/test/java` or `src/test/resources`.
- Do not edit production, generated output, unrelated tests, or another file type.
- Capture `Task scope baseline` after resolving the file set and before editing. Any candidate-relevant changed path outside that set is a failure,
  including further mutation of a path already dirty at task start. Exclude only untracked or ignored reproducible verification outputs such as Maven `target/`
  and Python `__pycache__/`; they are not editable scope and must not be modified intentionally.
- Obtain explicit approval before expanding scope. Never use destructive Git operations.

### R4: Branch map

- Before coding, enumerate target public-method branches and map each planned scenario to one branch or path.
- Classify every trigger and assertion as SUT-owned or collaborator-owned. Isolate collaborator-owned results at the nearest stable boundary and apply `R6` to decide whether to mock them.
- Exclude Lombok-generated behavior without custom logic. Default to one test per branch or path; use `R13` for additional cases.

### R5: Scenario granularity

- Keep one scenario per test and invoke the target public method once by default. Invoke it more than once only when repeated invocation is itself the scenario, such as idempotency, accumulation, or a state transition.
- Keep the coverage-relevant invocation and its externally observable assertions in the test body. Helpers and providers must not execute target behavior.
- For interface targets, test owned `default` or `static` methods directly. Exercise abstract contracts through concrete implementations, including when the user explicitly requests an interface-contract test.

### R6: SPI, mocks, and reflection

- Exercise interface default methods with Mockito `CALLS_REAL_METHODS`.
- Obtain SPI targets through the applicable project loader and keep the resolved instance as a test-class field by default. Record a concrete reason before bypassing the loader.
- Mock heavy dependencies and collaborator-owned decisions. Allow real simple values and explicitly scoped integration, contract, or E2E behavior.

### R7: Related tests

Update existing related tests in place and fill missing coverage first. An explicit test-class input filters execution only; it does not replace related-test discovery.
Create a new exact-name test class only when none exists.

### R8: Parameterization analysis

For each target public method, record `R8-CANDIDATES` with the method, candidate count, decision, and evidence. A candidate is high-fit only when all are true:

1. target method is the same;
2. differences are mainly input data;
3. the same form of assertion is used;
4. the group meets the minimum scenario count in `CODE_OF_CONDUCT.md`.

Collaborator-owned differences are not high-fit. Refactor high-fit candidates; otherwise record the concrete reason.
Use `KEEP` only when a genuinely high-fit refactor would materially reduce readability or diagnosis.

The scanner discovers possible groups but never decides semantic high-fit. Treat its candidate count as evidence to review, not an instruction to refactor.

### R9: Coverage blockers

When unreachable production code blocks coverage, report the class, path, exact line, and reason. Do not change production code within this Skill.

### R10: Completion state

Use exactly one state:

- `R10-INPUT_BLOCKED`: target classes or test modules cannot be resolved.
- `R10-A`: scope is clean; focused tests execute at least one test for the effective candidate; declared target coverage is met for every target and inner class; repository formatting/checkstyle gates pass; the full mechanical scan has no blocking violations under `R11`; semantic reviews and `R8` decisions are complete.
- `R10-B`: production dead code blocks the target and `R9` evidence is complete.
- `R10-C`: a blocking out-of-scope failure is evidenced under `R11`.
- `R10-D`: work remains; continue instead of claiming completion.

Priority is `INPUT_BLOCKED > B > C > A > D`. By default, cover the requested behavior and every affected SUT-owned branch. Apply a numeric CLASS, LINE, or BRANCH target only when the user states it; an explicit 100% target remains mandatory.

### R11: Failures

Fix task-introduced or currently relevant in-scope failures and rerun the checks they invalidate.
Treat a mechanical diagnostic as nonblocking only when the pre-edit baseline proves that it already existed, remains unchanged, and is unrelated to the requested behavior, contracts, coverage target, and verification validity.
Retain its command, nonzero exit code, diagnostic, and baseline evidence; do not describe the full scan as passing.
Missing baseline evidence, new or strengthened violations, and relevant historical diagnostics remain blocking.
For an out-of-scope failure that blocks a required check, record command, exit code, decisive lines, and blocking file/line, then request direction.
Retry transient dependency or network failures at most twice.
Minimal repair checks never replace final gates.

### R12: Existing full coverage

If reproducible target-class evidence is already 100%, skip coverage completion and perform only required optimization and quality work.
Mark `R4=N/A (triggered by R12)` and retain the coverage command and report path.

### R13: Necessity trimming

- Treat equal line/branch coverage only as a duplicate candidate, never as removal proof.
- Remove a test only when it adds no unique branch, input class, boundary, calculation path, failure mode, contract, collaborator interaction, or externally observable regression protection.
- Remove collaborator-only tests from the target unit test and report the separate owner when coverage is needed there.
- Remove redundant stubs, assertions, and locals that affect neither behavior nor diagnosis. Prefer Mockito defaults when the scenario permits.
- Apply `KEEP` only to an otherwise redundant item retained for a concrete readability or diagnostic reason; meaningful tests need no tag.

### R14: Boolean assertions

Apply the Boolean assertion standard in `CODE_OF_CONDUCT.md` to literal, constant, and variable expectations.

### R15: Delivery gates

- `R15-A`: complete the semantic `R8` decision; the scanner cannot infer high-fit.
- `R15-B`: review every test against the test-value rule in `CODE_OF_CONDUCT.md`; method names do not decide whether a test is permitted.
- `R15-C`: compare the final worktree with `Task scope baseline`; any candidate-relevant out-of-scope mutation fails. Ignore only reproducible,
  untracked or Git-ignored verification outputs.
- `R15-D`: verify each parameterized test's argument source and actual case count against `CODE_OF_CONDUCT.md`; inspect external, unresolved, or dynamic sources manually.
- `R15-F`: inspect each `switch` candidate in a parameterized test against the grouping rule in `CODE_OF_CONDUCT.md`.
- `R15-G`: inspect each new nested-type candidate used in argument sets against the grouping rule in `CODE_OF_CONDUCT.md`.
  A nested type is not a violation by itself.
  Unresolved grouping keeps the task at `R10-D`.
- `R15-H`: review Boolean assertions under `R14`; for assertion-dispatch candidates in parameterized tests, compare the assertion forms against the grouping rule in `CODE_OF_CONDUCT.md`.
- `R15-I`: inspect callback candidates used in argument sets against the grouping rule in `CODE_OF_CONDUCT.md`.
  Inspect external, unresolved, or dynamic argument sources manually.
- `R15-J`: helpers and providers do not invoke target public methods. Treat scanner results as partial evidence and inspect unresolved ownership manually.

## Workflow

1. Complete the `R1` source loading, then read this Skill's [verification commands](references/verification.md) through EOF.
2. Resolve targets, related tests, editable files, modules, and input-blocked state.
3. Create a task-specific temporary directory and capture `Task scope baseline` for the resolved file set.
4. Run focused baseline coverage and the full scanner through the repository command wrapper, retaining native JaCoCo reports and scanner diagnostics under the verification commands.
5. Decide `R12`; otherwise record the SUT/collaborator ownership boundary and `R4` branch map.
6. Review scanner candidates against every `R8` condition and record candidate-level decisions.
7. Check `R9`, then implement the smallest tests allowed by `R2-R7`.
8. After each coherent edit, run invalidated focused tests and relevant repository Checkstyle checks.
9. Apply `R13` and inspect semantic `R15-A/B/D/G/I/J` obligations.
10. Complete missing or invalidated final coverage, repository formatting/checkstyle, and full mechanical checks under the [verification commands](references/verification.md).
11. Confirm final scope against the baseline and decide `R10` once from current evidence before delivery.

Do not reuse a prior task's test or coverage result.
Final coverage must include executed tests for the effective candidate; use the verification commands to determine whether current evidence remains valid.

## Output

Include:

- `R10=<state>`;
- native JaCoCo report paths and raw CLASS/LINE/BRANCH counters for every target and inner class, with per-class ratios for each requested numeric metric;
- `R8-CANDIDATES` with candidate decisions and evidence;
- branch/ownership mapping, or the `R12` exemption;
- executed commands, exit codes, and bounded log paths/summaries;
- rule-to-evidence mapping for `R1-R15`;
- blocker and next action when state is not `R10-A`.

Never use completion wording for `R10-D`.
