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

Default completion level:
- Unless the user explicitly waives or lowers the target, requests such as "add tests" remain bound to `R10-A` completion criteria (including default coverage and quality gates).

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
- `<ResolvedTargetClasses>`: one fully-qualified production class or a comma-separated list of target classes from user input.
- `Target-class coverage scope`: for each target class, aggregate coverage for the target binary class and all binary classes whose names start with `<targetBinaryName>$` (including member/anonymous/local classes).
- `Related test classes`: existing `TargetClassName + Test` classes resolvable within the same module's test scope.
- `Assertion differences`: distinguishable assertions in externally observable results or side effects.
- `Necessity reason tag`: fixed-format tag for retention reasons, using `KEEP:<id>:<reason>`, recorded in the "Implementation and Optimization" section of the delivery report.
- `Baseline quality summary`: one pre-edit diagnostic run that combines rule scanning, candidate summary, and coverage evidence for the current scope.
- `Verification snapshot digest`: content hash over `<ResolvedTestFileSet>` used to decide whether a previous green verification result is still reusable.
- `Gate reuse state`: persisted mapping from logical gate names (for example `target-test`, `coverage`, `rule-scan`) to the latest green digest for that gate.
- `Latest green target-test digest`: compatibility alias for the `target-test` entry in `Gate reuse state`.
- `Consolidated hard-gate scan`: one script execution that enforces `R8`, `R14`, and all file-content-based `R15` rules while still reporting results per rule.

Module resolution order:
1. If the user explicitly provides modules, use them first.
2. Otherwise, resolve by searching upward for the nearest parent `pom.xml` from `<ResolvedTestFileSet>` paths.
3. Otherwise, resolve by searching upward for the nearest parent `pom.xml` from target class source paths.

## Mandatory Constraints

- Norm levels: `MUST` (required), `SHOULD` (preferred), `MAY` (optional).
- Definition source principle: mandatory constraints are defined only in this `R1-R15` section; other sections only provide term/workflow/command descriptions and must not add, override, or relax `R1-R15`.

- `R1`: `MUST` comply with `AGENTS.md` and `CODE_OF_CONDUCT.md`; rule interpretation should prioritize corresponding clauses and line-number evidence in `CODE_OF_CONDUCT.md`.

- `R2`: test types and naming
  - Non-parameterized scenarios `MUST` use JUnit `@Test`.
  - Data-driven scenarios `MUST` use JUnit `@ParameterizedTest(name = "{0}")` with `@MethodSource` + `Arguments`.
  - Parameterized test method signatures `MUST` use `final String name` as the first parameter.
  - Parameterized tests `MUST NOT` use `Consumer` (including `java.util.function.Consumer` and its generic forms) in method signatures or scenario-transport arguments.
  - Each parameterized test `MUST` provide at least 3 `Arguments` rows; fewer than 3 is a violation and `MUST` be converted to non-parameterized `@Test`.
  - Parameterized tests `MUST NOT` introduce new nested type declarations (member/local helper `class` / `interface` / `enum` / `record`) for scenario transport; use `Arguments` rows plus existing or JDK types instead.
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
  - For parameterized tests, `Arguments` row count `MUST` be greater than or equal to 3.
  - Tests `MUST` exercise behavior through public methods only.
  - Coverage-relevant invocations of target public methods `MUST` appear in test method bodies together with assertions for the externally observable result of that invocation.
  - Helper methods and `@MethodSource` providers `MUST NOT` invoke target public methods merely to warm caches, precompute coverage, or otherwise execute target behavior without assertions in the same test method body.
  - Public production methods with business logic `MUST` be covered with dedicated test methods.
  - For interface targets, only `default` public methods are required test targets by default, and non-`default` public methods `MUST NOT` be tested unless the user explicitly requests them in the current turn.
  - Dedicated test targets `MUST` follow the `R4` branch-mapping exclusion scope.

- `R6`: SPI, Mock, and reflection
  - For interface `default` methods, this rule overrides SPI instantiation requirements in `R6`: tests `MUST` use Mockito `CALLS_REAL_METHODS` to invoke real default implementations, and this path does not require SPI-bypass justification.
  - If the class under test can be obtained via SPI, `MUST` instantiate by default with `TypedSPILoader`/`OrderedSPILoader` (or database-specific loaders), and `MUST` keep the resolved instance as a test-class-level field (global variable) by default.
  - SPI metadata accessor methods `TypedSPI#getType`, `OrderedSPI#getOrder`, and `getTypeClass` are default no-test-required targets.
  - For these accessors, tests `MUST NOT` be added by default; they are allowed only when the user explicitly requests tests for them in the current turn.
  - If such tests are added without explicit request, they `MUST` be removed before completion.
  - If not instantiated via SPI, `MUST` record the reason before implementation.
  - Test dependencies `SHOULD` use Mockito mocks by default.
  - Reflection access `MUST` use `Plugins.getMemberAccessor()`, and field access only.

- `R7`: related test class strategy
  - If related test classes already exist, `MUST` update in place and fill missing coverage first.
  - If no related test class exists, `MUST` create `<TargetClassName>Test`.
  - If the user explicitly provides a test class list, it is only used as execution filtering input and `MUST NOT` replace the "in-place update of related test classes" strategy.
  - Deletion/merge of coverage-equivalent tests is determined by `R13`.

- `R8`: parameterized optimization (enabled by default)
  - `MUST` run pre-implementation candidate analysis and output an `R8-CANDIDATES` record (target public method, candidate count, decision, and evidence).
  - `MUST` report the mergeable method set and merge candidate count.
  - Candidates meeting all conditions below are considered "high-fit for parameterization":
    - A. target public method and branch skeleton are consistent;
    - B. scenario differences mainly come from input data;
    - C. assertion skeleton is consistent, or only declared assertion differences exist;
    - D. parameter sample count is at least 3;
    - E. parameterized test body does not require dispatch logic via `switch`.
  - "Declared assertion differences" means differences explicitly recorded in the delivery report.
  - If a candidate requires `switch` in a `@ParameterizedTest` body to distinguish argument rows, it is not high-fit and `MUST NOT` be refactored to parameterized form.
  - High-fit candidates `MUST` be refactored directly to parameterized form.
  - For high-fit candidates, a "do not recommend refactor" conclusion is allowed only when refactoring causes significant readability/diagnosability regression, and the exception `MUST` include a `Necessity reason tag` with concrete evidence.
  - Parameter construction `MUST` use `Arguments + @MethodSource`.
  - `MUST` provide either a "recommend refactor" or "do not recommend refactor" conclusion with reasons for each candidate; when no candidates exist, `MUST` output "no candidates + decision reason".
  - If high-fit candidates exist but neither parameterized refactor nor valid `KEEP` evidence is present, status `MUST NOT` be concluded as `R10-A`.

- `R9`: dead code and coverage blockers
  - When dead code blocks progress, `MUST` report class name, file path, exact line number, and unreachable reason.
  - Within this skill scope, `MUST NOT` bypass dead code by modifying production code.

- `R10`: state machine and completion criteria
  - `R10-INPUT_BLOCKED`: missing target classes, or unable to determine `<ResolvedTestModules>`.
  - `R10-A` (done): all of the following must be satisfied:
    - scope satisfies `R3`;
    - target test command succeeds, and surefire report has `Tests run > 0` (recommended to also satisfy `Tests run - Skipped > 0`);
    - coverage evidence satisfies the target (default class/line/branch 100%, unless explicitly lowered by the user);
    - each class in `<ResolvedTargetClasses>` has explicit aggregated class-level coverage evidence (CLASS/LINE/BRANCH counters with covered/missed/ratio) over the `Target-class coverage scope`, and all ratios satisfy the declared target;
    - Checkstyle, Spotless, two `R14` scans, and all required `R15` scans all pass;
    - `R8` analysis and compliance evidence are complete, including `R8-CANDIDATES` and candidate-level decisions (refactor or valid `KEEP` evidence).
  - `R10-B` (blocked): under the "production code cannot be changed" constraint, dead code blocks coverage targets, and evidence satisfies `R9`.
  - `R10-C` (blocked): failure occurs outside `R3` scope, and evidence satisfies `R11`.
  - `R10-D` (in-progress): none of `R10-INPUT_BLOCKED/R10-B/R10-C/R10-A` is satisfied yet.
  - Decision priority: `R10-INPUT_BLOCKED` > `R10-B` > `R10-C` > `R10-A` > `R10-D`.
  - `MUST NOT` conclude the task as completed while in `R10-D`; continue implementation and verification loops until reaching a terminal state.

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
  - Local variable declarations in test code `MUST NOT` use `final`; this rule applies only to local variables and does not change `R15-E` for parameterized-test method parameters.
  - Each retained item `MUST` carry a `KEEP:<id>:<reason>` tag and be recorded in the delivery report; items without tags are treated as redundant.
  - Each test method `MUST` provide unique value: cover a new branch/path, or add assertion differences.
  - If deleting a test method does not change line/branch coverage and has no assertion differences, `MUST` delete it.
  - Unless scenario requires otherwise, `SHOULD` use Mockito default return values instead of extra stubs.

- `R14`: boolean assertion hard gate
  - Boolean literal/boolean constant assertions `MUST` use `assertTrue`/`assertFalse`.
  - For boolean assertions where expected value is variable-driven (for example: parameter/local variable/field), `MUST` use `assertThat(actual, is(expected))`.
  - `MUST NOT` dispatch boolean assertions through control flow (for example `if/else`, `switch`, or ternary) only to choose between `assertTrue` and `assertFalse`.
  - `MUST NOT` use:
    - `assertThat(<boolean expression>, is(true|false|Boolean.TRUE|Boolean.FALSE))`
    - `assertEquals(true|false|Boolean.TRUE|Boolean.FALSE, ...)`
    - `assertEquals(..., true|false|Boolean.TRUE|Boolean.FALSE)`
  - `MUST` run one hard-gate scan after implementation and another before delivery; any hit means incomplete.

- `R15`: pre-delivery hard gates
  - `R15-A` (parameterization enforcement): if an `R8` high-fit candidate exists, the corresponding tests `MUST` be parameterized with `@ParameterizedTest(name = "{0}")`, unless a valid `KEEP` exception is recorded.
  - `R15-B` (metadata accessor test ban): unless the user explicitly requests it in the current turn, tests targeting `getType` / `getOrder` / `getTypeClass` `MUST NOT` be added.
  - `R15-C` (scope mutation guard): test-generation tasks `MUST NOT` introduce new diffs under any `src/main/` path.
  - `R15-D` (parameterized argument floor): each `@ParameterizedTest` `MUST` bind to `@MethodSource` providers that together contain at least 3 `Arguments` rows; otherwise it is a violation.
  - `R15-E` (parameterized name parameter): each `@ParameterizedTest` method `MUST` declare the first parameter exactly as `final String name`.
  - `R15-F` (parameterized switch ban): `@ParameterizedTest` method bodies `MUST NOT` contain `switch` statements.
  - `R15-G` (parameterized nested-type ban): when a file contains `@ParameterizedTest`, newly introduced diff lines `MUST NOT` add nested helper type declarations (`class` / `interface` / `enum` / `record`) inside the test class.
  - `R15-H` (boolean variable assertion style): for variable-driven boolean expectations, tests `MUST` assert with `assertThat(actual, is(expected))`, and `MUST NOT` use control-flow dispatch only to choose `assertTrue`/`assertFalse`.
  - `R15-I` (parameterized Consumer ban): files containing `@ParameterizedTest` `MUST NOT` introduce or retain `Consumer`-based scenario transport in parameterized method signatures or `@MethodSource` argument rows.
  - `R15-J` (assertion-backed target invocation): non-test helper methods and `@MethodSource` providers `MUST NOT` invoke target public methods; target public method invocations that contribute to coverage `MUST` be asserted in the same test method body.

## Workflow

1. Read `AGENTS.md` and `CODE_OF_CONDUCT.md`, and record hard constraints for this round (`R1`).
   - Capture scope baseline once: `git status --porcelain > /tmp/gen-ut-status-before.txt`.
2. Parse target classes, related test classes, and input-blocked state (`R10-INPUT_BLOCKED`).
3. Resolve `<ResolvedTestClass>`, `<ResolvedTestFileSet>`, `<ResolvedTestModules>`, and record `pom.xml` evidence (`R3`).
4. Run a `Baseline quality summary` using the bundled baseline script unless equivalent evidence was just produced in the same turn.
    - Use the baseline summary to identify current branch-miss lines, existing `R15` risks, and likely `R8-CANDIDATES` before editing.
    - `SHOULD` fix deterministic precheck warnings from the baseline summary before the first standalone target-test run; these warnings are advisory only and do not replace final `checkstyle` / `spotless` / hard-gate verification.
5. Decide whether `R12` is triggered; if not, output `R4` branch mapping.
   - For parser / utility classes that return context or value objects, `SHOULD` align planned assertions with the returned object's public API before the first target-test run, to avoid internal-branch coverage assertions that do not match externally observable behavior.
6. Execute `R8` parameterized optimization analysis, output `R8-CANDIDATES`, and apply required refactoring.
7. Execute `R9` dead-code checks and record evidence.
8. Complete test implementation or extension according to `R2-R7`.
9. Perform necessity trimming and coverage re-verification according to `R13`.
10. After each edit batch, `SHOULD` run one lightweight precheck pass before expensive verification when signatures or parameterized-test structure changed.
    - Recommended command: `python3 scripts/scan_quality_rules.py --precheck-only <ResolvedTestFileSet>`.
    - This pass is advisory and deterministic; it may fail fast on early style issues such as missing `final` on test-method parameters, missing `@MethodSource`, too-few `Arguments` rows, or an invalid first parameter for parameterized tests, but it does not replace formal `R14` / `R15` / `checkstyle` verification.
11. After each edit batch, recompute the `Verification snapshot digest`; during in-scope repair loops, prefer `target test + one consolidated hard-gate scan` as the minimal verification required by `R11`.
    - After any standalone target-test command succeeds, `SHOULD` persist the digest through `scripts/verification_gate_state.py mark-gate-green --gate target-test`.
12. Run final verification commands and handle failures by `R11`.
    - Independent final gates (`coverage`, `checkstyle`, `spotless`, `consolidated hard-gate scan`) `SHOULD` run in parallel when the environment allows; otherwise serialize them.
    - Prefer the bundled `scripts/run_quality_gates.py` runner so independent gates share one orchestration entry and can reuse gate-level green results from `Gate reuse state`.
    - If `scripts/verification_gate_state.py match-gate-green --gate target-test` reports a match for the current `<ResolvedTestFileSet>`, and the final coverage command re-executes tests on that same digest, `MAY` skip an extra standalone target-test rerun before delivery.
    - A previously green `coverage` gate `MAY` be reused for the same digest; `checkstyle` and `spotless` `SHOULD` still execute for the current module scope.
    - The consolidated hard-gate scan `MUST` be executed twice to satisfy `R14`: once after implementation stabilizes and once immediately before delivery. Only the earlier scan may be reused for diagnostics; the delivery scan must execute again.
13. Decide status by `R10` after verification; if status is `R10-D`, return to Step 5 and continue.
14. Before final response, run a second `R10` status decision and output `R10=<state>` with rule-to-evidence mapping.

## Verification and Commands

Flag presets:
- Module input provided:
  - `<TestModuleFlags>` = `-pl <module>`
  - `<GateModuleFlags>` = `-pl <module>`
- Module input not provided:
  - `<TestModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<GateModuleFlags>` = `-pl <ResolvedTestModules>`
  - `<FallbackGateModuleFlags>` = `<GateModuleFlags> -am` (for troubleshooting missing cross-module dependencies only; does not change `R3` and `R10`).

0. Baseline quality summary (recommended before editing):
```bash
python3 scripts/collect_quality_baseline.py --workdir <RepoRoot> \
  --coverage-command "./mvnw <GateModuleFlags> -DskipITs -Dsurefire.useManifestOnlyJar=false -Dtest=<ResolvedTestClass> -DfailIfNoTests=true -Dsurefire.failIfNoSpecifiedTests=false -Djacoco.skip=false -Djacoco.append=false -Djacoco.destFile=/tmp/gen-ut-baseline.exec test jacoco:report -Djacoco.dataFile=/tmp/gen-ut-baseline.exec" \
  --jacoco-xml-path <JacocoXmlPath> \
  --target-classes <ResolvedTargetClasses> \
  --baseline-before /tmp/gen-ut-status-before.txt \
  <ResolvedTestFileSet>
```
The baseline script reuses `scan_quality_rules.py` diagnostics and prints current coverage plus branch-miss lines for each target class.
It also prints deterministic non-blocking precheck warnings for high-frequency style failures such as missing `final` on test-method parameters or obvious parameterized-test structure issues; these warnings are for early repair only and do not replace formal gates.

0.1 Lightweight precheck pass (recommended after structural edits and before the next standalone target-test):
```bash
python3 scripts/scan_quality_rules.py --precheck-only <ResolvedTestFileSet>
```
This mode is intentionally narrower than the consolidated hard-gate scan. It exists only to catch deterministic early-fix issues cheaply and `MUST NOT` be used as a replacement for final `R14`, `R15`, `checkstyle`, or `spotless` verification.

1. Target tests:
```bash
./mvnw <TestModuleFlags> -DskipITs -Dspotless.skip=true -Dtest=<ResolvedTestClass> -DfailIfNoTests=true -Dsurefire.failIfNoSpecifiedTests=false test
```
After a green standalone target-test command, record the digest:
```bash
python3 scripts/verification_gate_state.py mark-gate-green --state-file /tmp/gen-ut-gate-state.json --gate target-test <ResolvedTestFileSet>
```

1.1 Verification snapshot digest:
```bash
python3 scripts/verification_gate_state.py digest <ResolvedTestFileSet>
```

1.2 Latest green target-test digest reuse check:
```bash
python3 scripts/verification_gate_state.py match-gate-green --state-file /tmp/gen-ut-gate-state.json --gate target-test <ResolvedTestFileSet>
```

2. Coverage:
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report jacoco:check@jacoco-check -Pcoverage-check
```
If the module does not define `jacoco-check@jacoco-check`:
```bash
./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report
```
After a green standalone coverage command, the digest may be recorded for reuse:
```bash
python3 scripts/verification_gate_state.py mark-gate-green --state-file /tmp/gen-ut-gate-state.json --gate coverage <ResolvedTestFileSet>
```

2.1 Target-class coverage hard gate (default target 100 unless explicitly lowered, aggregated over `Target-class coverage scope`):
```bash
bash -lc '
python3 - <JacocoXmlPath> <TargetRatioPercent> <ResolvedTargetClasses> <<'"'"'PY'"'"'
import sys
import xml.etree.ElementTree as ET
xml_path, target = sys.argv[1], float(sys.argv[2])
target_classes = [each.strip() for each in sys.argv[3].split(",") if each.strip()]
if not target_classes:
    print("[R10] empty target class list")
    sys.exit(1)
all_classes = list(ET.parse(xml_path).getroot().iter("class"))
all_ok = True
for fqcn in target_classes:
    class_name = fqcn.replace(".", "/")
    matched_nodes = [each for each in all_classes if each.get("name") == class_name or each.get("name", "").startswith(class_name + "$")]
    if not matched_nodes:
        print(f"[R10] class not found in jacoco.xml: {fqcn}")
        all_ok = False
        continue
    for counter_type in ("CLASS", "LINE", "BRANCH"):
        covered = 0
        missed = 0
        found_counter = False
        for each in matched_nodes:
            counter = next((c for c in each.findall("counter") if c.get("type") == counter_type), None)
            if counter is None:
                continue
            found_counter = True
            covered += int(counter.get("covered"))
            missed += int(counter.get("missed"))
        if not found_counter:
            print(f"[R10] missing {counter_type} counter for {fqcn}")
            all_ok = False
            continue
        total = covered + missed
        ratio = 100.0 if total == 0 else covered * 100.0 / total
        print(f"[R10] {fqcn} (+inner) {counter_type} covered={covered} missed={missed} ratio={ratio:.2f}%")
        if ratio + 1e-9 < target:
            print(f"[R10] {fqcn} (+inner) {counter_type} ratio {ratio:.2f}% < target {target:.2f}%")
            all_ok = False
if not all_ok:
    sys.exit(1)
PY
'
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

4.1 Unified final-gate runner (recommended):
```bash
python3 scripts/run_quality_gates.py --workdir <RepoRoot> \
  --state-file /tmp/gen-ut-gate-state.json \
  --tracked-path <ResolvedTestFileSet> \
  --reuse-gate coverage \
  --record-gate coverage \
  --record-gate hard-gate=rule-scan \
  --gate coverage="./mvnw <GateModuleFlags> -DskipITs -Djacoco.skip=false test jacoco:report" \
  --gate checkstyle="./mvnw <GateModuleFlags> -Pcheck checkstyle:check -DskipTests" \
  --gate spotless="./mvnw <GateModuleFlags> -Pcheck spotless:check -DskipTests" \
  --gate hard-gate="python3 scripts/scan_quality_rules.py --baseline-before /tmp/gen-ut-status-before.txt <ResolvedTestFileSet>"
```
If the environment cannot or should not parallelize, rerun the same command with `--serial`.
Coverage still remains the authoritative source for target-class counters, and the runner does not relax any gate.

5. Consolidated hard-gate scan (`R8`, `R14`, `R15-A/B/C/D/E/F/G/H/I/J`):
```bash
python3 scripts/scan_quality_rules.py --baseline-before /tmp/gen-ut-status-before.txt <ResolvedTestFileSet>
```
If the user explicitly requested metadata accessor tests in the current turn:
```bash
python3 scripts/scan_quality_rules.py --allow-metadata-accessor-tests --baseline-before /tmp/gen-ut-status-before.txt <ResolvedTestFileSet>
```
The script consolidates repeated file parsing and git-diff inspection without changing rule accuracy. It also evaluates `R15-C` by comparing the current git status against `/tmp/gen-ut-status-before.txt`.
For machine-readable automation or quick summaries, the script also supports:
```bash
python3 scripts/scan_quality_rules.py --json --baseline-before /tmp/gen-ut-status-before.txt <ResolvedTestFileSet>
python3 scripts/scan_quality_rules.py --summary-only --baseline-before /tmp/gen-ut-status-before.txt <ResolvedTestFileSet>
```

6. Scope validation:
```bash
git diff --name-only
```

## Final Output Requirements

- `MUST` include a status line `R10=<state>`.
- `MUST` include aggregated class-level coverage evidence for each class in `<ResolvedTargetClasses>` over the `Target-class coverage scope` (CLASS/LINE/BRANCH counters and ratios).
- `MUST` include `R8-CANDIDATES` output (candidate set, counts, and per-candidate decision evidence).
- `MUST` include executed commands and exit codes.
- If `R10` is not `R10-A`, `MUST` explicitly mark the task as not completed and provide blocking reason plus next action.
- `MUST NOT` use completion wording when `R10` is `R10-D`.
