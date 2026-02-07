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

## Execution Boundary

- Only handle unit-test-scope tasks; do not perform production feature refactoring.
- Do not edit generated directories (such as `target/`).
- Do not use destructive git operations (such as `git reset --hard`, `git checkout --`).
- If module name is missing, module-less command templates are allowed; if module is specified, module-scoped commands are mandatory.

## Workflow

1. Re-check `AGENTS.md` and `CODE_OF_CONDUCT.md`.
2. Locate target classes and existing test classes.
3. Output branch-path inventory according to `R4`.
4. Output branch-to-test mapping according to `R5`.
5. Perform dead-code analysis according to `R8` and record findings.
6. Implement or extend tests according to `R2/R3/R6/R7/R10/R12/R13`.
7. Run the first `R13` hard-gate scan (early fail-fast) and fix all hits.
8. Run verification commands and iterate.
9. Run the second `R13` hard-gate scan (final release gate) and ensure clean.
10. Deliver results using the output structure.

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

4. `R13` hard-gate scan (must be clean, run in step 7 and step 9):
```bash
bash -lc 'if rg -n "assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*," <module>/src/test/java; then echo "[R13] forbidden boolean assertion found"; exit 1; fi'
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

4. `R13` hard-gate scan (must be clean, run in step 7 and step 9):
```bash
bash -lc 'if rg -n "assertThat\\s*\\(.*is\\s*\\(\\s*(true|false)\\s*\\)\\s*\\)|assertEquals\\s*\\(\\s*(true|false)\\s*," . --glob "**/src/test/java/**"; then echo "[R13] forbidden boolean assertion found"; exit 1; fi'
```

Command execution rules:
- Record every command and exit code.
- If a command fails, record the failure reason and execute at least one remediation attempt; if still blocked, continue clearing blockers within test scope before escalating.
- Escalate to user only when blockers are outside safe scope or require non-test changes; include exact failing commands, error lines, and attempted remediations.

## Output Structure

Follow this order:

1. Goal and constraints (mapped to `R1-R13`)
2. Plan and implementation (including branch mapping result)
3. Dead-code and coverage results (according to `R8/R9`)
4. Verification commands and exit codes
5. `R13` hard-gate evidence (both scan commands and exit codes)
6. Risks and next actions

## Quality Self-Check

- Rule definitions must exist only in "Mandatory Constraints"; other sections should reference rule IDs only.
- Final state must satisfy `R9`, and all applicable rules (including `R10`, `R11`, `R12`, and `R13`) must be met, with complete command and exit-code records.
- `R13` command is mandatory evidence; missing `R13` command record or non-clean scan means not complete.

## Maintenance Rules

- After changing any `R` numbering, run `rg -n "R[0-9]+" .codex/skills/gen-ut/SKILL.md` to ensure there are no dangling references.
- After changing skill rules, verify trigger semantics are consistent between `SKILL.md` and `agents/openai.yaml`.
- Fixed final-review order:
  1. Numbering consistency check
  2. Duplicate phrase scan
  3. Semantic alignment check between `SKILL.md` and `agents/openai.yaml`
