---
name: gen-ut
description: >-
  Generate standard unit tests for one or more target classes in Apache ShardingSphere;
  use unified rules to complete test coverage and pass quality gates.
---

# Generate Unit Tests

## Input Contract

Required input:
- Target class list: one or more classes, preferably fully qualified names.

Optional input:
- Module name (to scope Maven commands).
- Test class list (for targeted test execution).
- Extra constraints (for example forbidden APIs, command allowlist, coverage threshold).

Handling missing input:
- If target classes are missing, request the class list before any coding work.
- If test classes are missing, discover existing related test classes by the `TargetClassName + Test` convention first; if resolution fails, mark as blocked and ask the user to provide test classes.

Test class placeholder convention:
- `<ResolvedTestClass>` can be one fully qualified test class or a comma-separated list.
- `<ResolvedTestFileSet>` is the concrete test file path list resolved from `<ResolvedTestClass>` (space-separated in shell commands).

## Mandatory Constraints (Single Source of Rules)

- `R1`: Follow `CODE_OF_CONDUCT.md`.
- `R2`: Use standard unit tests with `@Test` only; forbid `@RepeatedTest` and `@ParameterizedTest`.
- `R3`: Do not modify production code; only modify `src/test/java` and `src/test/resources`.
- `R4`: Enumerate all branch paths of target public methods before coding.
- `R5`: Apply minimal branch coverage: one branch maps to one test method;
  do not cover the same branch in multiple test methods.
- `R6`: Each test method covers one scenario and invokes the target public method at most once (extra assertions are allowed for the same scenario).
- `R7`: Prefer `TypedSPILoader` and `OrderedSPILoader` to build subjects under test; subjects under test must be class-level fields.
- `R8`: If dead code exists, report class name, file path, exact line number, and unreachable reason.
- `R9`: Completion criteria must satisfy one of the following:
  - `R9-A`: Target class coverage reaches 100% for class/line/branch, target test classes are executed successfully by Surefire, Checkstyle passes,
    and all commands with exit codes are recorded.
  - `R9-B`: If dead code blocks 100% branch coverage under the "no production code changes" rule,
    report fully as required by `R8` and wait for user decision.
- `R10`: If a related test class already exists for a target class, extend that class to add only missing-coverage tests; create a new test class only when no related test class exists.
- `R11`: Do not claim completion if target tests were not actually executed due compile/runtime blockers. First remove blockers with minimal test-scope fixes and rerun verification;
  only when blockers are outside scope and cannot be resolved safely in-turn, report exact blocker files/lines/commands and request user decision.
- `R12`: Boolean assertion policy in tests:
  - Must use `assertTrue` / `assertFalse` for boolean checks.
  - Forbidden assertion patterns:
    - `assertThat(<boolean expression>, is(true))`
    - `assertThat(<boolean expression>, is(false))`
    - `assertEquals(true, ...)`
    - `assertEquals(false, ...)`
- `R13`: Hard gate for `R12`:
  - Use the unified scan regex:
    - `assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*,`
  - Run hard-gate scan twice:
    - after test implementation (early fail-fast gate);
    - before final delivery (final release gate).
  - If any match is found, task state is "not complete" until all violations are fixed and scan is rerun clean.
- `R14`: Change scope must be strictly limited to tests resolved from input target classes.
  - Allowed edit files: only `<ResolvedTestClass>` mapped source files under `src/test/java` or `src/test/resources`.
  - Forbidden: modifying any other test file to fix unrelated build/lint/gate failures.
  - Exception: only if the user explicitly approves scope expansion in the current turn.
- `R15`: No meaningless test code; each line must have direct necessity for the scenario.
  - Do not add redundant mocks/stubs/assertions that do not change branch selection, object behavior, or verification result.
  - Prefer Mockito default return values unless explicit stubbing is required by the scenario.
  - If a line is only cosmetic and removing it does not affect scenario setup or assertions, remove it.
- `R16`: Inline single-use local variables in tests.
  - If a local variable is used exactly once, inline it at the call site.
  - Exception: keep the variable only when it is required for additional stubbing/verification or shared assertions.
- `R17`: Test-method necessity hard gate; no coverage-equivalent duplicates.
  - Each test method must add at least one unique value: either an uncovered branch/path from `R4/R5`, or a distinct externally observable behavior assertion not already covered.
  - If removing a test method keeps target-class line/branch coverage unchanged and does not lose unique behavior verification, remove that method.
  - For factory/route fallback scenarios, keep only one representative method per branch outcome unless the user explicitly requests an extra regression guard.

## Execution Boundary

- Only handle unit-test-scope tasks; do not perform production feature refactoring.
- Do not edit generated directories (such as `target/`).
- Do not use destructive git operations (such as `git reset --hard`, `git checkout --`).
- If module name is missing, module-less command templates are allowed; if module is specified, module-scoped commands are mandatory.
- If failures come from out-of-scope test files under `R14`, report blockers and wait for user decision instead of editing those files.

## Workflow

1. Re-check `AGENTS.md` and `CODE_OF_CONDUCT.md`.
2. Locate target classes and existing test classes.
3. Output branch-path inventory according to `R4`.
4. Output branch-to-test mapping according to `R5`.
5. Perform dead-code analysis according to `R8` and record findings.
6. Implement or extend tests according to `R2/R3/R6/R7/R10/R12/R13/R14/R15/R16/R17`.
7. Run necessity self-check according to `R15/R16/R17` and remove redundant mocks/stubs/assertions/single-use locals/coverage-equivalent test methods.
8. Run the first `R13` hard-gate scan (early fail-fast) and fix all hits.
9. Run verification commands and iterate.
10. Run the second `R13` hard-gate scan (final release gate) and ensure clean.
11. Deliver results using the output structure.

## Verification and Commands

With module input:

1. Targeted unit tests:
```bash
./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```

2. Coverage:
```bash
./mvnw -pl <module> -am -DskipITs -Djacoco.skip=false test jacoco:report
```

3. Checkstyle:
```bash
./mvnw -pl <module> -am -Pcheck checkstyle:check -DskipTests
```

4. `R13` hard-gate scan (must be clean, run in step 8 and step 10):
```bash
bash -lc 'if rg -n "assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*," <ResolvedTestFileSet>; then echo "[R13] forbidden boolean assertion found"; exit 1; fi'
```

Without module input:

1. Targeted unit tests:
```bash
./mvnw -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```

2. Coverage:
```bash
./mvnw -DskipITs -Djacoco.skip=false test jacoco:report
```

3. Checkstyle:
```bash
./mvnw -Pcheck checkstyle:check -DskipTests
```

4. `R13` hard-gate scan (must be clean, run in step 8 and step 10):
```bash
bash -lc 'if rg -n "assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*," <ResolvedTestFileSet>; then echo "[R13] forbidden boolean assertion found"; exit 1; fi'
```

Command execution rules:
- Record every command and exit code.
- If a command fails, record the failure reason and execute at least one remediation attempt; if still blocked, continue clearing blockers within test scope before escalating.
- Escalate to user only when blockers are outside safe scope or require non-test changes; include exact failing commands, error lines, and attempted remediations.

## Output Structure

Follow this order:

1. Goal and constraints (mapped to `R1-R17`)
2. Plan and implementation (including branch mapping result)
3. Dead-code and coverage results (according to `R8/R9`)
4. Verification commands and exit codes
5. `R13` hard-gate evidence (both scan commands and exit codes)
6. Risks and next actions

## Quality Self-Check

- Rule definitions must exist only in "Mandatory Constraints"; other sections should reference rule IDs only.
- Final state must satisfy `R9`, and all applicable rules (including `R10`, `R11`, `R12`, `R13`, `R14`, `R15`, `R16`, and `R17`) must be met, with complete command and exit-code records.
- `R13` command is mandatory evidence; missing `R13` command record or non-clean scan means not complete.

## Maintenance Rules

- After changing any `R` numbering, run `rg -n "R[0-9]+" .codex/skills/gen-ut/SKILL.md` to ensure there are no dangling references.
- After changing skill rules, verify trigger semantics are consistent between `SKILL.md` and `agents/openai.yaml`.
- Fixed final-review order:
  1. Numbering consistency check
  2. Duplicate phrase scan
  3. Semantic alignment check between `SKILL.md` and `agents/openai.yaml`
