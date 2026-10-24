<!--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements. See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

# Verification Commands

Run commands from the repository root. Before every Maven command, apply the Mandatory Execution Contract in
`.codex/context/token-efficiency.md`; place the Maven command inside its canonical wrapper and report only the bounded summary,
log path, and exit code.

## Task setup

Create one private temporary directory per task and use it for all transient reports:

```bash
task_dir="$(mktemp -d -t shardingsphere-gen-ut.XXXXXX)"
python3 .codex/skills/gen-ut/scripts/scan_quality_rules.py \
  --capture-scope-baseline "$task_dir/scope-baseline.json" \
  <ResolvedTestFileSet>
```

Do not use shared fixed files under `/tmp`. Keep the temporary directory through delivery so logs and evidence remain inspectable.
The scope guard excludes untracked or ignored Maven `target/` files and Python `__pycache__/` files because the required verification commands create them.
It still rejects tracked files in those directories and other ignored files outside the resolved test file set.

## Baseline

Run focused tests separately for each resolved test module so one module cannot overwrite another module's execution data.
Create a module-specific task directory before running Maven through the command wrapper:

```bash
module_dir="$task_dir/<ResolvedTestModule>"
mkdir -p "$module_dir"
```

Use one task-specific JaCoCo data file for both the test agent and report:

```bash
./mvnw -pl <ResolvedTestModule> -DskipITs -Dspotless.skip=true \
  -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false \
  -Djacoco.skip=false -Djacoco.append=false \
  -Djacoco.destFile="$module_dir/baseline.exec" test jacoco:report \
  -Djacoco.dataFile="$module_dir/baseline.exec"
```

Retain each module's baseline XML report and command log in its task directory before final reports overwrite the report path.
Run the full scanner and retain its JSON output, exit code, diagnostics, parameterization candidates, and semantic review inputs:

```bash
python3 .codex/skills/gen-ut/scripts/scan_quality_rules.py --json \
  --scope-baseline "$task_dir/scope-baseline.json" \
  <ResolvedTestFileSet>
```

Retain the applicable repository Checkstyle report alongside the scanner baseline; Checkstyle owns Boolean-literal assertion syntax.
Apply `R11` to baseline diagnostics rather than treating every nonzero scanner exit as a new blocking failure.
New or task-relevant Checkstyle warnings block completion; only baseline-proven unchanged, unrelated diagnostics may remain non-blocking under `R11`.
Read coverage counters and missed branch locations from the native JaCoCo XML and HTML reports.

## Edit-loop checks

When signatures or parameterized-test structure change, run the owning module's repository Checkstyle check through the command wrapper:

```bash
./mvnw -pl <ResolvedTestModule> checkstyle:check -Pcheck
```

Run focused tests after each coherent edit:

```bash
./mvnw -pl <ResolvedTestModules> -DskipITs -Dspotless.skip=true \
  -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```

Then run the mechanical rules and scope guard:

```bash
python3 .codex/skills/gen-ut/scripts/scan_quality_rules.py --json \
  --scope-baseline "$task_dir/scope-baseline.json" \
  <ResolvedTestFileSet>
```

Complete the semantic reviews required by `R14` and `R15` from source and record evidence; zero mechanical violations alone is not `R10-A`.
In JSON output, inspect `rules` for mechanical results, `candidates` for parameterization groups, and `reviews` for source locations and files without resolved target public-method information.
Inspect actual argument sets for every standard JUnit source, including external or dynamic providers; source-code call counts do not prove executed row counts.
Review every test against the test-value rule in `CODE_OF_CONDUCT.md`, independently of method names.
Review `switch`, `Consumer`, nested-type, and assertion-control-flow candidates in parameterized tests against the rule in `CODE_OF_CONDUCT.md` for combining cases that invoke the same method under test and use the same form of assertion.
These partial signals do not establish a violation.
Review Boolean assertions with variable expectations from source because syntax-only checks cannot infer every expression's type.

## Final coverage

Use current focused coverage evidence for the effective candidate.
When that evidence is missing or invalid, repeat the baseline coverage command for each module with `final.exec` instead of `baseline.exec`.
Confirm that the data file is nonempty and was produced by the current accepted test execution, and that the agent, report, and check all use that same file.
For an explicit numeric coverage target, run the existing `coverage-check` profile against that data:

```bash
./mvnw -pl <ResolvedTestModule> -Pcoverage-check \
  -Djacoco.skip=false -Djacoco.dataFile="$module_dir/final.exec" \
  '-Djacoco.check.class.pattern=org.example.Target:org.example.Target.*' \
  -Djacoco.check.counter=<CLASS|LINE|BRANCH> \
  -Djacoco.minimum.coverage=<TargetRatio> jacoco:check@jacoco-check
```

Replace the example filter with the exact resolved target families using JaCoCo's dot-separated class names, including dots for inner-class separators, and separate families with `:`.
Do not use a target-name prefix wildcard that also matches sibling classes.
Set `<TargetRatio>` between zero and one and use the accepted current data path if coverage evidence was reused instead of regenerated as `final.exec`.
Inspect the check log for actual execution; a skipped check or unmatched class filter is not a pass.
Verify every resolved target and compiled inner class in the current XML report and retain their raw per-class counters; aggregate-only evidence is insufficient.
For an explicit numeric CLASS, LINE, or BRANCH target, compare only that metric against the requested threshold using the raw JaCoCo counters for every resolved target and inner class.
Use covered and missed counts rather than rounded displayed percentages; a target's aggregate must not compensate for an unmet class, inner class, or another target.
When several numeric metrics are requested, run the configured check separately for each requested metric and threshold against the same valid execution data.
Without an explicit numeric target, decide completion from the requested-behavior and affected-SUT-owned-branch map while still reporting the available counters.
Do not run the profile's default 95% check when no numeric target was requested.
A missing target or mandatory CLASS or LINE counter blocks completion even without a numeric target; a zero Maven exit code alone is not coverage proof.
A valid branchless class satisfies BRANCH coverage with zero missed branches, including a zero/zero counter.

## Repository completion gates

After the last edit, run the applicable Spotless and Checkstyle commands required by the [code-implementation verification rules](../../code-implementation/references/verification.md), through the canonical output wrapper.
Use those rules to select read-only checks first and determine whether a needed formatter write is authorized.

Reuse a complete scan or focused coverage result only within the current task while its relevant sources, providers, configuration, tool version, execution filters, scope baseline, and required semantic evidence remain unchanged.
Rerun checks invalidated by a relevant write, scope change, failure, or new evidence.
A lightweight precheck is not a full scan and cannot establish scope compliance or `R10-A`.
