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
  - Each parameterized test `MUST` provide at least 3 `Arguments` rows; fewer than 3 is a violation and `MUST` be converted to non-parameterized `@Test`.
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
  - Public production methods with business logic `MUST` be covered with dedicated test methods.
  - Dedicated test targets `MUST` follow the `R4` branch-mapping exclusion scope.

- `R6`: SPI, Mock, and reflection
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
    - D. parameter sample count is at least 3.
  - "Declared assertion differences" means differences explicitly recorded in the delivery report.
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

- `R15`: pre-delivery hard gates
  - `R15-A` (parameterization enforcement): if an `R8` high-fit candidate exists, the corresponding tests `MUST` be parameterized with `@ParameterizedTest(name = "{0}")`, unless a valid `KEEP` exception is recorded.
  - `R15-B` (metadata accessor test ban): unless the user explicitly requests it in the current turn, tests targeting `getType` / `getOrder` / `getTypeClass` `MUST NOT` be added.
  - `R15-C` (scope mutation guard): test-generation tasks `MUST NOT` introduce new diffs under any `src/main/` path.
  - `R15-D` (parameterized argument floor): each `@ParameterizedTest` `MUST` bind to `@MethodSource` providers that together contain at least 3 `Arguments` rows; otherwise it is a violation.

## Workflow

1. Read `AGENTS.md` and `CODE_OF_CONDUCT.md`, and record hard constraints for this round (`R1`).
   - Capture scope baseline once: `git status --porcelain > /tmp/gen-ut-status-before.txt`.
2. Parse target classes, related test classes, and input-blocked state (`R10-INPUT_BLOCKED`).
3. Resolve `<ResolvedTestClass>`, `<ResolvedTestFileSet>`, `<ResolvedTestModules>`, and record `pom.xml` evidence (`R3`).
4. Decide whether `R12` is triggered; if not, output `R4` branch mapping.
5. Execute `R8` parameterized optimization analysis, output `R8-CANDIDATES`, and apply required refactoring.
6. Execute `R9` dead-code checks and record evidence.
7. Complete test implementation or extension according to `R2-R7`.
8. Perform necessity trimming and coverage re-verification according to `R13`.
9. Run verification commands and handle failures by `R11`; execute two `R14` scans and all required `R15` scans.
10. Decide status by `R10` after verification; if status is `R10-D`, return to Step 5 and continue.
11. Before final response, run a second `R10` status decision and output `R10=<state>` with rule-to-evidence mapping.

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

5. `R8` parameterized compliance scan (annotation block parsing):
```bash
bash -lc '
python3 - <ResolvedTestFileSet> <<'"'"'PY'"'"'
import re
import sys
from pathlib import Path

name_pattern = re.compile(r'name\s*=\s*"\{0\}"')
token = "@ParameterizedTest"
violations = []
for path in (each for each in sys.argv[1:] if each.endswith(".java")):
    source = Path(path).read_text(encoding="utf-8")
    pos = 0
    while True:
        token_pos = source.find(token, pos)
        if token_pos < 0:
            break
        line = source.count("\n", 0, token_pos) + 1
        cursor = token_pos + len(token)
        while cursor < len(source) and source[cursor].isspace():
            cursor += 1
        if cursor >= len(source) or "(" != source[cursor]:
            violations.append(f"{path}:{line}")
            pos = token_pos + len(token)
            continue
        depth = 1
        end = cursor + 1
        while end < len(source) and depth:
            if "(" == source[end]:
                depth += 1
            elif ")" == source[end]:
                depth -= 1
            end += 1
        if depth or not name_pattern.search(source[cursor + 1:end - 1]):
            violations.append(f"{path}:{line}")
        pos = end
if violations:
    print("[R8] @ParameterizedTest must use name = \"{0}\"")
    for each in violations:
        print(each)
    sys.exit(1)
PY
'
```

5.1 `R15-A` high-fit candidate enforcement scan (shape-based):
```bash
bash -lc '
python3 - <ResolvedTestFileSet> <<'"'"'PY'"'"'
import re
import sys
from pathlib import Path
from collections import defaultdict

IGNORE = {"assertThat", "assertTrue", "assertFalse", "mock", "when", "verify", "is", "not"}

def extract_block(text, brace_index):
    depth = 0
    i = brace_index
    while i < len(text):
        if "{" == text[i]:
            depth += 1
        elif "}" == text[i]:
            depth -= 1
            if 0 == depth:
                return text[brace_index + 1:i]
        i += 1
    return ""

decl = re.compile(r"(?:@Test|@ParameterizedTest(?:\\([^)]*\\))?)\\s+void\\s+(assert\\w+)\\s*\\([^)]*\\)\\s*\\{", re.S)
call = re.compile(r"\\b\\w+\\.(\\w+)\\s*\\(")
param_targets = defaultdict(set)
plain_target_count = defaultdict(lambda: defaultdict(int))
for path in (each for each in sys.argv[1:] if each.endswith(".java")):
    source = Path(path).read_text(encoding="utf-8")
    for match in decl.finditer(source):
        brace_index = source.find("{", match.start())
        body = extract_block(source, brace_index)
        methods = [each for each in call.findall(body) if each not in IGNORE]
        if not methods:
            continue
        target = methods[0]
        header = source[max(0, match.start() - 160):match.start()]
        if "@ParameterizedTest" in header:
            param_targets[path].add(target)
        else:
            plain_target_count[path][target] += 1
violations = []
for path, each_counter in plain_target_count.items():
    for method_name, count in each_counter.items():
        if count >= 3 and method_name not in param_targets[path]:
            violations.append(f"{path}: method={method_name} nonParameterizedCount={count}")
if violations:
    print("[R15-A] high-fit candidate likely exists but no parameterized test found:")
    for each in violations:
        print(each)
    sys.exit(1)
PY
'
```

5.2 `R15-D` parameterized minimum arguments scan:
```bash
bash -lc '
python3 - <ResolvedTestFileSet> <<'"'"'PY'"'"'
import re
import sys
from pathlib import Path

PARAM_METHOD_PATTERN = re.compile(r"@ParameterizedTest(?:\\s*\\([^)]*\\))?\\s*((?:@\\w+(?:\\s*\\([^)]*\\))?\\s*)*)void\\s+(assert\\w+)\\s*\\(", re.S)
METHOD_SOURCE_PATTERN = re.compile(r"@MethodSource(?:\\s*\\(([^)]*)\\))?")
METHOD_DECL_PATTERN = re.compile(r"(?:private|protected|public)?\\s*(?:static\\s+)?[\\w$<>\\[\\], ?]+\\s+(\\w+)\\s*\\([^)]*\\)\\s*\\{", re.S)
ARGUMENT_ROW_PATTERN = re.compile(r"\\b(?:Arguments\\.of|arguments)\\s*\\(")

def extract_block(text, brace_index):
    depth = 0
    index = brace_index
    while index < len(text):
        if "{" == text[index]:
            depth += 1
        elif "}" == text[index]:
            depth -= 1
            if 0 == depth:
                return text[brace_index + 1:index]
        index += 1
    return ""

def parse_method_sources(method_name, annotation_block):
    resolved = []
    matches = list(METHOD_SOURCE_PATTERN.finditer(annotation_block))
    if not matches:
        return resolved
    for each in matches:
        raw = each.group(1)
        if raw is None or not raw.strip():
            resolved.append(method_name)
            continue
        raw = raw.strip()
        normalized = re.sub(r"\\bvalue\\s*=\\s*", "", raw)
        names = re.findall(r'"([^"]+)"', normalized)
        for name in names:
            # Ignore external references such as "pkg.Class#method"; they are unresolved in this scan.
            resolved.append(name.split("#", 1)[-1])
    return resolved

violations = []
for path in (each for each in sys.argv[1:] if each.endswith(".java")):
    source = Path(path).read_text(encoding="utf-8")
    method_bodies = {}
    for match in METHOD_DECL_PATTERN.finditer(source):
        method_name = match.group(1)
        brace_index = source.find("{", match.start())
        if brace_index < 0:
            continue
        method_bodies[method_name] = extract_block(source, brace_index)
    for match in PARAM_METHOD_PATTERN.finditer(source):
        annotation_block = match.group(1)
        method_name = match.group(2)
        line = source.count("\\n", 0, match.start()) + 1
        source_methods = parse_method_sources(method_name, annotation_block)
        if not source_methods:
            violations.append(f"{path}:{line} method={method_name} missing @MethodSource")
            continue
        total_rows = 0
        unresolved = []
        for provider in source_methods:
            body = method_bodies.get(provider)
            if body is None:
                unresolved.append(provider)
                continue
            total_rows += len(ARGUMENT_ROW_PATTERN.findall(body))
        if unresolved:
            violations.append(f"{path}:{line} method={method_name} unresolvedProviders={','.join(unresolved)}")
            continue
        if total_rows < 3:
            violations.append(f"{path}:{line} method={method_name} argumentsRows={total_rows}")
if violations:
    print("[R15-D] each @ParameterizedTest must have >= 3 Arguments rows from @MethodSource")
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

7. `R15-B` metadata accessor test ban scan (skip only when explicitly requested by user):
```bash
bash -lc '
if rg -n -U "@Test(?s:.*?)void\\s+assert\\w*(GetType|GetOrder|GetTypeClass)\\b|assertThat\\((?s:.*?)\\.getType\\(\\)|assertThat\\((?s:.*?)\\.getOrder\\(\\)|assertThat\\((?s:.*?)\\.getTypeClass\\(\\)" <ResolvedTestFileSet>; then
  echo "[R15-B] metadata accessor test detected without explicit user request"
  exit 1
fi'
```

8. Scope validation:
```bash
git diff --name-only
```

9. `R15-C` production-path mutation guard (baseline-based):
```bash
bash -lc '
# capture once at task start:
# git status --porcelain > /tmp/gen-ut-status-before.txt
git status --porcelain > /tmp/gen-ut-status-after.txt
python3 - <<'"'"'PY'"'"'
from pathlib import Path

before_path = Path("/tmp/gen-ut-status-before.txt")
after_path = Path("/tmp/gen-ut-status-after.txt")
before = set(before_path.read_text(encoding="utf-8").splitlines()) if before_path.exists() else set()
after = set(after_path.read_text(encoding="utf-8").splitlines())
introduced = sorted(after - before)
violations = []
for each in introduced:
    path = each[3:].strip()
    if "/src/main/" in path or path.startswith("src/main/"):
        violations.append(path)
if violations:
    print("[R15-C] out-of-scope production path modified:")
    for each in violations:
        print(each)
    raise SystemExit(1)
PY
'
```

## Final Output Requirements

- `MUST` include a status line `R10=<state>`.
- `MUST` include aggregated class-level coverage evidence for each class in `<ResolvedTargetClasses>` over the `Target-class coverage scope` (CLASS/LINE/BRANCH counters and ratios).
- `MUST` include `R8-CANDIDATES` output (candidate set, counts, and per-candidate decision evidence).
- `MUST` include executed commands and exit codes.
- If `R10` is not `R10-A`, `MUST` explicitly mark the task as not completed and provide blocking reason plus next action.
- `MUST NOT` use completion wording when `R10` is `R10-D`.
