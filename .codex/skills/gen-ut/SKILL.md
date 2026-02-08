---
name: gen-ut
description: >-
  Generate standard unit tests for one or more target classes in Apache ShardingSphere;
  use unified rules to make target classes reach 100% class/line/branch coverage and pass quality gates.
---

# Generate Unit Tests

## Input Conventions

Required input:
- Target class list: one or more classes, preferably fully qualified class names.

Optional input:
- Module name (used to scope Maven command execution).
- Test class list (used for targeted test execution).

Missing-input handling:
- If target classes are missing, request the class list before any coding work.
- If test classes are missing, first discover existing related test classes by the `TargetClassName + Test` convention.
- If related test classes do not exist, create `<TargetClassName>Test` in the resolved module test source set and continue.
- If `<ResolvedTestModules>` cannot be inferred from related test files or target class source files, mark as blocked and require the user to explicitly provide module scope.

Input-blocking status mapping:
- Input blocked (awaiting supplemental input): target classes are missing, or `<ResolvedTestModules>` cannot be determined and the user has not provided module scope.
- After input is complete and the task enters execution, determine completion/blocking by `R9`, `R10`, and `R3`.
- Execution-phase status mapping: dead-code blocking follows `R9-B`; out-of-scope failures follow `R9-C`.

Test-class placeholder conventions:
- `<ResolvedTestClass>` can be one fully qualified test class, or a comma-separated list.
- `<ResolvedTestFileSet>` is the concrete editable file list (space-separated in shell commands), including:
  - test source files resolved from `<ResolvedTestClass>`;
  - new test files and test resources strictly required for these target classes.
  - Must be resolved to concrete paths in workflow step 3.
- `<ResolvedTestModules>` is a comma-separated Maven module list for scoped verification commands.
  - Resolution order:
    1. If explicit module input is provided, use it first;
    2. Otherwise infer from related test files (`<ResolvedTestFileSet>`) via the nearest parent Maven module (`pom.xml`);
    3. Otherwise infer from target class source files via the nearest parent Maven module (`pom.xml`).

Terminology:
- `Related test class`: existing `TargetClassName + Test` classes resolved within the same module test scope.
- `Distinct observable assertion`: assertions for different public outcomes or side effects.

## Mandatory Constraints (Single Source of Rules)

- Definition-source principle: rule definitions exist only in the "Mandatory Constraints" section; other sections only reference rule IDs.
- Layered index:
  - `L1 (Base Constraints Layer)`: `R1`, `R2`, `R3`
  - `L2 (Test Design and Implementation Layer)`: `R4`, `R5`, `R6`, `R7`, `R8`
  - `L3 (Status Determination and Blocking Handling Layer)`: `R9`, `R10`
  - `L4 (Hard Quality Gate Layer)`: `R11`, `R12`
- `R1 (L1-Base Constraints Layer)`: Follow `CODE_OF_CONDUCT.md`.
- `R2 (L1-Base Constraints Layer)`: Use JUnit `@Test`; `@RepeatedTest` is forbidden.
- `R3 (L1-Base Constraints Layer)`: Change scope is strictly limited to the test scope resolved from input target classes.
  - Editable files are only `<ResolvedTestFileSet>`.
  - Modifying other test files to fix unrelated build/check/gate failures is forbidden.
  - Scope can be expanded only when explicitly approved by the user in the current turn.
- `R4 (L2-Test Design and Implementation Layer)`: Branch-path rule: before coding, enumerate all branch paths of target public methods; by default one branch/path maps to only one test method; necessity of additional tests on the same branch is judged by `R11`.
- `R5 (L2-Test Design and Implementation Layer)`: Each test covers one scenario, and invokes the target public method at most once; additional assertions are allowed in the same scenario.
- `R6 (L2-Test Design and Implementation Layer)`: When the class under test can be obtained via SPI, default to `TypedSPILoader` and `OrderedSPILoader`.
  - "Obtainable via SPI": the class implements `TypedSPI`/`DatabaseTypedSPI`, or its implementation can be discovered by an SPI loader.
  - If SPI is not used for instantiation, the reason must be recorded in the plan before implementation.
- `R7 (L2-Test Design and Implementation Layer)`: If the target class already has related tests, update in place: first fill missing coverage, then delete or merge coverage-equivalent tests by `R11`; create new tests only when no related tests exist.
- `R8 (L2-Test Design and Implementation Layer)`: If dead code exists, report class name, file path, exact line number, and unreachable reason.
- `R9 (L3-Status Determination and Blocking Handling Layer)`: Completion is determined by one of the following:
  - `R9-A`: target class coverage (class/line/branch) is 100%, target test classes run successfully in Surefire, and required quality gates (`R12` hard gate, Checkstyle) pass, with commands and exit codes attached.
  - `R9-B`: if dead code blocks 100% branch coverage under the "do not modify production code" rule, report by `R8` and mark blocked.
  - `R9-C`: if failure occurs outside `R3` scope, report blocking evidence by `R10` and mark blocked.
  - Priority: evaluate `R9-B` first, then `R9-C`; only when neither applies can `R9-A` be marked complete.
- `R10 (L3-Status Determination and Blocking Handling Layer)`: Blocking handling: first remove blocking within the smallest test scope and re-verify.
  - If blocking is outside `R3` scope and cannot be safely solved in the current task, report exact blocking file/line/command and request user decision.
- `R11 (L4-Hard Quality Gate Layer)`: Hard gate for test necessity:
  - Decision order is fixed as "objective pruning -> exception-retention review"; reversing the order and causing repeated rework is forbidden.
  - Objective-pruning phase: first identify and batch-delete coverage-equivalent tests based on `R4` branch mapping and coverage results, then uniformly re-verify coverage.
  - Objective-pruning phase: then delete redundant mocks/stubs/assertions and single-use local variables that do not affect branch selection/collaborator behavior/observable assertion results.
  - Each retained item must have a one-line necessity-reason label; retained items without labels are treated as redundant.
  - Meaningless test code is not allowed; every line must have direct necessity for the scenario.
  - If deleting a line leaves the above three dimensions unchanged, that line is redundant (including redundant mocks/stubs/assertions) and must be deleted.
  - Unless a scenario explicitly requires stubbing, use Mockito default return values.
  - Single-use local variables should be inlined at the call site; retain only when the variable is used for extra stubbing/verification across two or more statements or for shared assertions.
  - Coverage-equivalent duplicate test methods are forbidden.
  - Each test method must add unique value: cover an uncovered branch/path, or add an uncovered distinct observable assertion.
  - Coverage-equivalent duplicate: same target public method, same branch/path, and no assertion difference; changing only literals/mock names/fixture values with unchanged assertion results is also duplicate.
  - If deleting a test method does not change line/branch coverage of the target class, the method must be deleted.
  - For factory/routing fallback scenarios, by default keep only one representative method for each branch result; add extra methods only when there is assertion difference or the user explicitly requests extra regression protection.
- `R12 (L4-Hard Quality Gate Layer)`: Boolean assertions and hard gate:
  - Boolean checks must use `assertTrue` / `assertFalse`; the following patterns are forbidden:
  - `assertThat(<boolean expression>, is(true))`
  - `assertThat(<boolean expression>, is(false))`
  - `assertEquals(true, ...)`
  - `assertEquals(false, ...)`
  - The single source of regex is "Validation and Commands / Item 4".
  - The hard-gate scan must run twice: after test implementation (early fail-fast gate) and before final delivery (final release gate).
  - Any hit is considered "incomplete" until all issues are fixed and a clean rescan is obtained.

## Execution Boundaries

- Only handle unit test tasks.
- Do not modify production code.
- Only `src/test/java` and `src/test/resources` may be modified.
- Do not edit generated directories (for example, `target/`).
- Do not use destructive git operations (for example, `git reset --hard`, `git checkout --`).
- If module name is not provided, infer `<ResolvedTestModules>` and keep commands module-scoped; repository-scoped commands are forbidden unless approved by the user.

## Workflow

1. Re-check `AGENTS.md` and `CODE_OF_CONDUCT.md`.
2. Locate target classes and related test classes.
3. Resolve `<ResolvedTestClass>`, `<ResolvedTestFileSet>`, and `<ResolvedTestModules>`, and record module-resolution evidence (`pom.xml` paths).
4. Output branch list and test mapping (`R4`); if extra tests exist on the same branch, record their necessity basis (`R11`).
5. Execute dead-code analysis by `R8` and record the result.
6. Implement or extend tests by implementation rules (`R2`, `R4`, `R5`, `R6`, `R7`, `R3`, `R11`, `R12`, execution boundaries).
7. Execute necessity self-check by `R11` (objective pruning -> exception-retention review), removing redundant mocks/stubs/assertions, single-use local variables, and coverage-equivalent test methods.
8. Execute the first hard-gate scan by `R12` (early fail-fast) and fix all hits.
9. Run verification commands in layered order (target tests -> target module gate -> fallback gate when necessary) and iterate.
10. Execute the second hard-gate scan by `R12` (final release gate) and ensure clean results.
11. Determine final status by `R9`, then deliver with the rule-evidence mapping.

## Validation and Commands

Execution decision tree (rule references only):
0. Input blocking (awaiting supplemental input) first: process by "Input-blocking status mapping" and do not enter `R9` determination.
1. Completion determination follows `R9` only.
2. Failure within `R3` scope: fix within `<ResolvedTestFileSet>` by `R10` and rerun.
3. Failure outside `R3` scope: record evidence by `R10` and mark blocked by `R9-C`.
4. `<FallbackGateModuleFlags>` is only for troubleshooting, does not expand edit scope (`R3`), and does not change completion determination (`R9-A`).

Flag presets:
- When module input is provided:
  - `<TestModuleFlags>` = `-pl <module>`
  - `<GateModuleFlags>` = `-pl <module>`
- When module input is not provided:
  - `<TestModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<GateModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<FallbackGateModuleFlags>` = `<GateModuleFlags> -am` (used only when gate fails because of cross-module dependency gaps).

1. Targeted unit test:
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```

2. Coverage:
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report jacoco:check@jacoco-check -Pcoverage-check
```
If `jacoco-check@jacoco-check` Maven execution is not defined in the target module's `pom.xml`, use:
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report
```
Then record manual coverage evidence with the following template (must include the generation command and an accessible report path):
- `Target classes`: `<ClassA>,<ClassB>,...`
- `Coverage`: `class=<...>, line=<...>, branch=<...>`
- `Report generation command`: `<report generation command>`
- `Report path`: `<report path>`

3. Checkstyle:
```bash
./mvnw <GateModuleFlags> -Pcheck checkstyle:check -DskipTests
```

4. `R12` hard-gate scan (must be clean; execute in workflow step 8 and step 10):
```bash
bash -lc '
BOOLEAN_ASSERTION_BAN_REGEX="assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*,"
if rg -n "$BOOLEAN_ASSERTION_BAN_REGEX" <ResolvedTestFileSet>; then
  echo "[R12] forbidden boolean assertion found"
  exit 1
fi'
```

Command-execution rules:
- Record every command and its exit code.
- Retry only once for retryable errors (for example, temporary plugin-resolution failure, repository mirror timeout, transient network jitter); handle non-retryable errors directly by `R10`.
- If a gate command fails because of cross-module dependency gaps, remediate once with `<FallbackGateModuleFlags>` and record the fallback reason.
- If a command fails, record the failed command and key error lines by the execution decision tree (`R10`, `R3`, `R9-C`).

Blocking report template:
- `Failed command`: `<failed command>`
- `Exit code`: `<code>`
- `Key error line`: `<key error line>`
- `Within R3 scope`: `<yes/no>`
- `Status`: `<R9-B or R9-C>`

5. Minimal pre-delivery machine-check list (recommended order):
```bash
git diff --name-only
```
- Check against `<ResolvedTestFileSet>` item by item to confirm modified files do not exceed `R3` scope.
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```
- Execute item 4 `R12` hard-gate scan command and record the result (final release gate).

## Output Structure

Use the following order:

1. Goal and constraints (mapped to `R1-R12`)
2. Status (determined by `R9`) + one-line reason
3. Plan and implementation (including module-resolution evidence, branch-mapping results, and any `R11` exception reasons and labels)
4. Dead code and coverage results (at minimum include: target-class coverage values, dead-code location, and reason)
5. Verification and quality-gate evidence (at minimum include: key commands, exit codes, and `R12` scan results)
6. Rule-evidence mapping (`R#->evidence`, at least covering `R4`, `R7`, `R8`, `R9`, `R10`, `R3`, `R11`, `R12`)
7. Risks and next actions

Minimal template for rule-evidence mapping:
- `R4`: branch list and branch-test mapping.
- `R7`: evidence of in-place update or creation decision for related test classes.
- `R8`: dead-code location (class, path, line number, reason).
- `R9`: final status and determination reason.
- `R10/R3`: blocking-scope determination and blocking-report template.
- `R11`: necessity self-check result (deleted items, retained-reason labels, coverage re-verification result).
- `R12`: boolean-assertion policy and results of two hard-gate scans.

## Quality Self-Check

- Rule definitions may appear only in the "Mandatory Constraints" section; other sections may only reference rule IDs.
- Final status must satisfy `R9`; command-based rules provide command and exit-code evidence, and non-command rules provide mapping/code evidence.
- The `R12` command is mandatory evidence; missing `R12` records or a non-clean scan is considered incomplete.
- Blocking-state evidence must follow `R10`.
- Output must include "Rule-evidence mapping" and must be consistent with the final status in `R9`.

## Maintenance Rules

- After modifying any `R` index, run `rg -n "R[0-9]+" .codex/skills/gen-ut/SKILL.md` to ensure no dangling references.
- After modifying skill rules, verify semantic consistency between `SKILL.md` and `agents/openai.yaml` trigger semantics.
- Fixed final review order:
  1. Index consistency check
  2. Repeated-phrase scan
  3. Semantic consistency check between `SKILL.md` and `agents/openai.yaml`
