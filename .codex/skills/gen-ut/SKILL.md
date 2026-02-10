---
name: gen-ut
description: >-
  Generate standard unit tests for one or more target classes in Apache ShardingSphere;
  by default, target 100% class/line/branch coverage and pass quality gates;
  perform explicit merge analysis, suitability filtering, and refactor optimization for parameterized tests.
---

# Generate Unit Tests

## Input Conventions

Required inputs:
- Target class list (fully-qualified class names are recommended).

Optional inputs:
- Module name (limits Maven command scope).
- Test class list (for targeted execution only; does not limit in-place updates for related test classes).

Missing input handling:
- Note: this section only describes entry handling; final decisions follow `R7`/`R10`.
- Missing target classes: enter `R10-INPUT_BLOCKED`.
- Missing test classes: auto-discover with the `TargetClassName + Test` convention.
- No related test classes: create `<TargetClassName>Test` in the resolved module test source set.
- Cannot resolve `<ResolvedTestModules>`: enter `R10-INPUT_BLOCKED` and request additional module scope.

## Terms

- `<ResolvedTestClass>`: one fully-qualified test class or a comma-separated list of test classes.
- `<ResolvedTestFileSet>`: editable file set (space-separated in shell commands), containing only related test files and required test resources.
- `<ResolvedTestModules>`: comma-separated Maven module list used by scoped verification commands.
- `Related test classes`: existing `TargetClassName + Test` classes resolvable within the same module's test scope.
- `Assertion differences`: distinguishable assertions in externally observable results or side effects.
- `Necessity reason tag`: fixed-format tag for retention reasons, using `KEEP:<id>:<reason>`, recorded in the "Implementation and Optimization" section of the delivery report.

Module resolution order:
1. If the user explicitly provides modules, use them first.
2. Otherwise, resolve by searching upward for the nearest parent `pom.xml` from `<ResolvedTestFileSet>` paths.
3. Otherwise, resolve by searching upward for the nearest parent `pom.xml` from target class source paths.

## Mandatory Constraints

- Norm levels: `MUST` (required), `SHOULD` (preferred), `MAY` (optional).
- Definition source principle: mandatory constraints are defined only in this `R1-R14` section; other sections only provide term/workflow/command descriptions and must not add, override, or relax `R1-R14`.

- `R1`: `MUST` comply with `AGENTS.md` and `CODE_OF_CONDUCT.md`; rule interpretation should prioritize corresponding clauses and line-number evidence in `CODE_OF_CONDUCT.md`.

- `R2`: test types and naming
  - Non-parameterized scenarios `MUST` use JUnit `@Test`.
  - Data-driven scenarios `MUST` use JUnit `@ParameterizedTest`, and display names `MUST` use `@ParameterizedTest(name = "{0}")`.
  - `MUST NOT` use `@RepeatedTest`.
  - Test method naming `MUST` follow `CODE_OF_CONDUCT.md`: use the `assert` prefix; when a single test uniquely covers a production method, use `assert<MethodName>`.

- `R3`: change and execution scope
  - Edit scope `MUST` be limited to `<ResolvedTestFileSet>`.
  - Path scope `MUST` be limited to `src/test/java` and `src/test/resources`.
  - `MUST NOT` modify production code or generated directories (such as `target/`).
  - `MUST NOT` modify other test files to fix failures outside scope; if scope expansion is needed, `MUST` be explicitly approved by the user in the current turn.
  - `MUST NOT` use destructive git operations (for example, `git reset --hard`, `git checkout --`).

- `R4`: branch list and mapping
  - Before coding, `MUST` enumerate branches/paths of target public methods and build branch-to-test mappings.
  - Branch mapping scope `MUST` exclude Lombok-generated methods without custom logic.
  - By default, one branch/path maps to one test method.
  - Whether to keep additional tests on the same branch is determined by `R13`.

- `R5`: test granularity
  - Each test method `MUST` cover only one scenario.
  - Each test method `MUST` call the target public method at most once; additional assertions are allowed in the same scenario.
  - For parameterized tests, each `Arguments` row `MUST` represent one independent scenario and one branch/path mapping unit for `R4`.
  - Tests `MUST` exercise behavior through public methods only.
  - Public production methods with business logic `MUST` be covered with dedicated test methods.
  - Dedicated test targets `MUST` follow the `R4` branch-mapping exclusion scope.

- `R6`: SPI, Mock, and reflection
  - If the class under test can be obtained via SPI, `MUST` instantiate by default with `TypedSPILoader`/`OrderedSPILoader` (or database-specific loaders).
  - If not instantiated via SPI, `MUST` record the reason before implementation.
  - Test dependencies `SHOULD` use Mockito mocks by default.
  - Reflection access `MUST` use `Plugins.getMemberAccessor()`, and field access only.

- `R7`: related test class strategy
  - If related test classes already exist, `MUST` update in place and fill missing coverage first.
  - If no related test class exists, `MUST` create `<TargetClassName>Test`.
  - If the user explicitly provides a test class list, it is only used as execution filtering input and `MUST NOT` replace the "in-place update of related test classes" strategy.
  - Deletion/merge of coverage-equivalent tests is determined by `R13`.

- `R8`: parameterized optimization (enabled by default)
  - `MUST` report the mergeable method set and merge candidate count.
  - Candidates meeting all conditions below are considered "high-fit for parameterization":
    - A. target public method and branch skeleton are consistent;
    - B. scenario differences mainly come from input data;
    - C. assertion skeleton is consistent, or only declared assertion differences exist;
    - D. parameter sample count is at least 3.
  - "Declared assertion differences" means differences explicitly recorded in the delivery report.
  - High-fit candidates `MUST` be refactored directly to parameterized form.
  - For high-fit candidates, a "do not recommend refactor" conclusion is allowed only when refactoring causes significant readability/diagnosability regression, and the exception `MUST` include a `Necessity reason tag` with concrete evidence.
  - Parameter construction `SHOULD` prefer `Arguments + @MethodSource`; `MAY` use clearer options such as `@CsvSource`/`@EnumSource`.
  - `MUST` provide either a "recommend refactor" or "do not recommend refactor" conclusion with reasons for each candidate; when no candidates exist, `MUST` output "no candidates + decision reason".

- `R9`: dead code and coverage blockers
  - When dead code blocks progress, `MUST` report class name, file path, exact line number, and unreachable reason.
  - Within this skill scope, `MUST NOT` bypass dead code by modifying production code.

- `R10`: state machine and completion criteria
  - `R10-INPUT_BLOCKED`: missing target classes, or unable to determine `<ResolvedTestModules>`.
  - `R10-A` (done): all of the following must be satisfied:
    - scope satisfies `R3`;
    - target test command succeeds, and surefire report has `Tests run > 0` (recommended to also satisfy `Tests run - Skipped > 0`);
    - coverage evidence satisfies the target (default class/line/branch 100%, unless explicitly lowered by the user);
    - Checkstyle, Spotless, and two `R14` scans all pass;
    - `R8` analysis and compliance evidence are complete.
  - `R10-B` (blocked): under the "production code cannot be changed" constraint, dead code blocks coverage targets, and evidence satisfies `R9`.
  - `R10-C` (blocked): failure occurs outside `R3` scope, and evidence satisfies `R11`.
  - Decision priority: `R10-INPUT_BLOCKED` > `R10-B` > `R10-C` > `R10-A`.

- `R11`: failure handling
  - If failure is within `R3` scope: `MUST` fix within `<ResolvedTestFileSet>` and rerun minimal verification.
  - If failure is outside `R3` scope: `MUST` record blocking evidence (failed command, exit code, key error lines, blocking file/line) and request user decision.
  - Minimal verification is defined as "target test command + one `R14` hard-gate scan command".
  - Minimal verification is only for in-scope repair and `MUST NOT` replace final gating of `R10-A`.
  - Retryable errors (temporary plugin resolution failure, mirror timeout, transient network jitter) `MAY` retry up to 2 times.

- `R12`: 100% coverage optimization mode
  - If target class coverage evidence is already 100%, `MUST` skip coverage completion and execute only `R8` parameterized optimization.
  - Coverage judgment `MUST` be reproducible (command + report path).
  - In this mode, `MAY` omit `R4` branch mapping output, but `MUST` mark `R4=N/A (triggered by R12)` in rule mapping and attach coverage evidence.

- `R13`: test necessity trimming
  - Trimming order `MUST` be fixed as "objective trimming -> exception retention review".
  - In objective trimming stage, `MUST` first remove coverage-equivalent tests and re-verify coverage uniformly, then remove redundant mock/stub/assertion and single-use local variables that do not affect branch selection/collaborator interaction behavior (call count, parameters)/observable assertions; if retention significantly improves readability, `MAY` keep and mark `Necessity reason tag`.
  - Each retained item `MUST` carry a `KEEP:<id>:<reason>` tag and be recorded in the delivery report; items without tags are treated as redundant.
  - Each test method `MUST` provide unique value: cover a new branch/path, or add assertion differences.
  - If deleting a test method does not change line/branch coverage and has no assertion differences, `MUST` delete it.
  - Unless scenario requires otherwise, `SHOULD` use Mockito default return values instead of extra stubs.

- `R14`: boolean assertion hard gate
  - Boolean literal/boolean constant assertions `MUST` use `assertTrue`/`assertFalse`.
  - `MUST NOT` use:
    - `assertThat(<boolean expression>, is(true|false|Boolean.TRUE|Boolean.FALSE))`
    - `assertEquals(true|false|Boolean.TRUE|Boolean.FALSE, ...)`
    - `assertEquals(..., true|false|Boolean.TRUE|Boolean.FALSE)`
  - `MUST` run one hard-gate scan after implementation and another before delivery; any hit means incomplete.

## Workflow

1. Read `AGENTS.md` and `CODE_OF_CONDUCT.md`, and record hard constraints for this round (`R1`).
2. Parse target classes, related test classes, and input-blocked state (`R10-INPUT_BLOCKED`).
3. Resolve `<ResolvedTestClass>`, `<ResolvedTestFileSet>`, `<ResolvedTestModules>`, and record `pom.xml` evidence (`R3`).
4. Decide whether `R12` is triggered; if not, output `R4` branch mapping.
5. Execute `R8` parameterized optimization analysis and apply required refactoring.
6. Execute `R9` dead-code checks and record evidence.
7. Complete test implementation or extension according to `R2-R7`.
8. Perform necessity trimming and coverage re-verification according to `R13`.
9. Run verification commands and handle failures by `R11`; execute two `R14` scans.
10. Decide status by `R10` and output rule-to-evidence mapping.

## Verification and Commands

Flag presets:
- Module input provided:
  - `<TestModuleFlags>` = `-pl <module>`
  - `<GateModuleFlags>` = `-pl <module>`
- Module input not provided:
  - `<TestModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<GateModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<FallbackGateModuleFlags>` = `<GateModuleFlags> -am` (for troubleshooting missing cross-module dependencies only; does not change `R3` and `R10`).

1. Target tests:
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -DfailIfNoTests=true -Dsurefire.failIfNoSpecifiedTests=false test
```

2. Coverage:
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report jacoco:check@jacoco-check -Pcoverage-check
```
If the module does not define `jacoco-check@jacoco-check`:
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report
```

3. Checkstyle:
```bash
./mvnw <GateModuleFlags> -Pcheck checkstyle:check -DskipTests
```

4. Spotless:
```bash
./mvnw <GateModuleFlags> -Pcheck spotless:check -DskipTests
```
If missing cross-module dependencies occur, rerun the gate command above once with `<FallbackGateModuleFlags>` and record the trigger reason and result.

5. `R8` parameterized compliance scan (annotation block parsing):
```bash
bash -lc '
python3 - <ResolvedTestFileSet> <<'"'"'PY'"'"'
import re
import sys
from pathlib import Path

name_pattern = re.compile(r'name\s*=\s*"\{0\}"')
token = "@ParameterizedTest"

def collect_violations(path):
    source = Path(path).read_text(encoding="utf-8")
    violations = []
    pos = 0
    while True:
        token_pos = source.find(token, pos)
        if token_pos < 0:
            return violations
        line = source.count("\n", 0, token_pos) + 1
        cursor = token_pos + len(token)
        while cursor < len(source) and source[cursor].isspace():
            cursor += 1
        if cursor >= len(source) or source[cursor] != "(":
            violations.append(f"{path}:{line}")
            pos = token_pos + len(token)
            continue
        depth = 1
        end = cursor + 1
        while end < len(source) and depth:
            if source[end] == "(":
                depth += 1
            elif source[end] == ")":
                depth -= 1
            end += 1
        if depth:
            violations.append(f"{path}:{line}")
            return violations
        if not name_pattern.search(source[cursor + 1:end - 1]):
            violations.append(f"{path}:{line}")
        pos = end

violations = []
for each in sys.argv[1:]:
    if each.endswith(".java"):
        violations.extend(collect_violations(each))

if violations:
    print("[R8] @ParameterizedTest must use name = \"{0}\"")
    for each in violations:
        print(each)
    sys.exit(1)
PY
'
```

6. `R14` hard-gate scan:
```bash
bash -lc '
BOOLEAN_ASSERTION_BAN_REGEX="assertThat\s*\((?s:.*?)is\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)\s*\)|assertEquals\s*\(\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*,"
BOOLEAN_ASSERTION_BAN_REGEX+="|assertEquals\s*\((?s:.*?),\s*(?:true|false|Boolean\.TRUE|Boolean\.FALSE)\s*\)"
if rg -n -U --pcre2 "$BOOLEAN_ASSERTION_BAN_REGEX" <ResolvedTestFileSet>; then
  echo "[R14] forbidden boolean assertion found"
  exit 1
fi'
```
7. Scope validation:
```bash
git diff --name-only
```
