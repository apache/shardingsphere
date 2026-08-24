---
name: gen-ut
description: >-
  Generate standard unit tests for one or more target classes in Apache ShardingSphere;
  by default, target 100% class/line/branch coverage and pass quality gates;
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

Before any test write, read `AGENTS.md` and [code-implementation/SKILL.md](../code-implementation/SKILL.md) through EOF, then read every reference selected by that base Skill for this task through EOF, including its implementation, testing, non-regression, and verification references.
Follow the applicable `CODE_OF_CONDUCT.md` sections.
The base Skill owns universal implementation, testing, non-regression, verification, and completion requirements; this Skill adds target resolution, coverage, branch-map, parameterization, and scanner requirements for systematic unit-test generation.
Reading either Skill does not expand the user-authorized scope, file types, Git authority, or other permissions.

### R2: Test form and naming

- Use JUnit 5 `@Test` for standalone scenarios.
- Use `@ParameterizedTest(name = "{0}")`, `@MethodSource`, and `Arguments` for data-driven scenarios. Declare `final String name` first and provide at least three rows.
- Do not use `@RepeatedTest`, `Consumer` as scenario transport, `switch` dispatch inside parameterized tests, or new nested transport types.
- Follow repository test naming. A single test uniquely covering one public method is `assert<MethodName>`.

### R3: Change boundary

- Edit only `<ResolvedTestFileSet>` under `src/test/java` or `src/test/resources`.
- Do not edit production, generated output, unrelated tests, or another file type.
- Capture `Task scope baseline` after resolving the file set and before editing. Any candidate-relevant changed path outside that set is a failure,
  including further mutation of a path already dirty at task start. Exclude only untracked or ignored reproducible verification outputs such as Maven `target/`
  and Python `__pycache__/`; they are not editable scope and must not be modified intentionally.
- Obtain explicit approval before expanding scope. Never use destructive Git operations.

### R4: Branch map

- Before coding, enumerate target public-method branches and map each planned scenario to one branch or path.
- Classify every trigger and assertion as SUT-owned or collaborator-owned. Mock the nearest stable boundary for collaborator results.
- Exclude Lombok-generated behavior without custom logic. Default to one test per branch or path; use `R13` for additional cases.

### R5: Scenario granularity

- Keep one scenario per test and invoke the target public method at most once per scenario.
- Keep the coverage-relevant invocation and its externally observable assertions in the test body. Helpers and providers must not execute target behavior.
- For interface targets, test only `default` methods unless the user explicitly requests abstract methods.

### R6: SPI, mocks, and reflection

- Exercise interface default methods with Mockito `CALLS_REAL_METHODS`.
- Obtain SPI targets through the applicable project loader and keep the resolved instance as a test-class field by default. Record a concrete reason before bypassing the loader.
- Do not add tests for `getType`, `getOrder`, or `getTypeClass` unless explicitly requested.
- Mock heavy dependencies and collaborator-owned decisions. Allow real simple values and explicitly scoped integration, contract, or E2E behavior.

### R7: Related tests

Update existing related tests in place and fill missing coverage first. An explicit test-class input filters execution only; it does not replace related-test discovery.
Create a new exact-name test class only when none exists.

### R8: Parameterization analysis

For each target public method, record `R8-CANDIDATES` with the method, candidate count, decision, and evidence. A candidate is high-fit only when all are true:

1. target method and branch skeleton are consistent;
2. differences are mainly input data;
3. assertion skeleton is consistent or differences are explicitly declared;
4. at least three scenarios exist;
5. no `switch` dispatch is required.

Collaborator-owned differences are not high-fit. Refactor high-fit candidates; otherwise record the concrete reason.
Use `KEEP` only when a genuinely high-fit refactor would materially reduce readability or diagnosis.

The scanner discovers possible groups but never decides semantic high-fit. Treat its candidate count as evidence to review, not an instruction to refactor.

### R9: Coverage blockers

When unreachable production code blocks coverage, report the class, path, exact line, and reason. Do not change production code within this Skill.

### R10: Completion state

Use exactly one state:

- `R10-INPUT_BLOCKED`: target classes or test modules cannot be resolved.
- `R10-A`: scope is clean; focused tests execute at least one test; declared target coverage is met for every target and inner class;
  repository formatting/checkstyle gates pass; both final rule scans pass; semantic reviews and `R8` decisions are complete.
- `R10-B`: production dead code blocks the target and `R9` evidence is complete.
- `R10-C`: an out-of-scope failure is evidenced under `R11`.
- `R10-D`: work remains; continue instead of claiming completion.

Priority is `INPUT_BLOCKED > B > C > A > D`. Default coverage is 100% CLASS, LINE, and BRANCH unless the user lowers it.

### R11: Failures

Fix in-scope failures and rerun the focused test plus mechanical rule scan. For out-of-scope failures, record command, exit code,
decisive lines, and blocking file/line, then request direction. Retry transient dependency or network failures at most twice.
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

Use `assertTrue`/`assertFalse` for literal or constant expectations and `assertThat(actual, is(expected))` for variable expectations.
Do not use boolean `assertEquals`, boolean literals inside Hamcrest `is`, or control flow whose only purpose is selecting
`assertTrue` versus `assertFalse`. Run the mechanical scan after implementation stabilizes and again immediately before delivery.

### R15: Delivery gates

- `R15-A`: complete the semantic `R8` decision; the scanner cannot infer high-fit.
- `R15-B`: confirm any metadata-accessor candidate was explicitly requested; the scanner only reports likely candidates.
- `R15-C`: compare the final worktree with `Task scope baseline`; any candidate-relevant out-of-scope mutation fails. Ignore only reproducible,
  untracked or Git-ignored verification outputs.
- `R15-D`: every parameterized test uses `@MethodSource` with at least three `Arguments` rows. Inspect external providers manually.
- `R15-E`: the first parameter is exactly `final String name`.
- `R15-F`: parameterized bodies contain no `switch`.
- `R15-G`: parameterized test changes add no nested transport type.
- `R15-H`: boolean assertions obey `R14` without assertion-dispatch control flow.
- `R15-I`: parameterized signatures and provider rows contain no `Consumer`; inspect external providers manually.
- `R15-J`: helpers and providers do not invoke target public methods. Treat scanner results as partial evidence and inspect unresolved ownership manually.

## Workflow

1. Complete the `R1` source loading, then read this Skill's [verification commands](references/verification.md) through EOF.
2. Resolve targets, related tests, editable files, modules, and input-blocked state.
3. Create a task-specific temporary directory and capture `Task scope baseline` for the resolved file set.
4. Run focused baseline coverage through the repository command wrapper, then use `collect_quality_baseline.py` to report coverage and mechanical risks.
5. Decide `R12`; otherwise record the SUT/collaborator ownership boundary and `R4` branch map.
6. Review scanner candidates against every `R8` condition and record candidate-level decisions.
7. Check `R9`, then implement the smallest tests allowed by `R2-R7`.
8. After each coherent edit, run the lightweight precheck, focused test, and mechanical scan.
9. Apply `R13`, re-run coverage, and inspect semantic `R15-A/B/D/I/J` obligations.
10. Run final coverage, repository formatting/checkstyle gates, and the two required mechanical scans.
11. Recheck scope against the baseline and decide `R10` twice before delivery.

Do not reuse a prior task's test or coverage result. Final coverage must execute tests for the effective candidate.

## Output

Include:

- `R10=<state>`;
- aggregated CLASS/LINE/BRANCH counters and ratios for every target plus inner classes;
- `R8-CANDIDATES` with candidate decisions and evidence;
- branch/ownership mapping, or the `R12` exemption;
- executed commands, exit codes, and bounded log paths/summaries;
- rule-to-evidence mapping for `R1-R15`;
- blocker and next action when state is not `R10-A`.

Never use completion wording for `R10-D`.
