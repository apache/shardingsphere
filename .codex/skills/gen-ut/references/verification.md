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

Run focused tests with a task-specific JaCoCo data file:

```bash
./mvnw -pl <ResolvedTestModules> -DskipITs -Dspotless.skip=true \
  -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false \
  -Djacoco.skip=false -Djacoco.append=false \
  -Djacoco.destFile="$task_dir/baseline.exec" test jacoco:report \
  -Djacoco.dataFile="$task_dir/baseline.exec"
```

Then print the baseline without executing another shell command:

```bash
python3 .codex/skills/gen-ut/scripts/collect_quality_baseline.py \
  --jacoco-xml-path <JacocoXmlPath> \
  --target-classes <ResolvedTargetClasses> \
  --scope-baseline "$task_dir/scope-baseline.json" \
  <ResolvedTestFileSet>
```

`coverageStatus=missing` means required target, CLASS, or LINE evidence does not exist; it never means 100%.
For a present target with no BRANCH counter, report `covered=0 missed=0 ratio=100%` because JaCoCo omits zero-total branch counters.

## Edit-loop checks

Run the structural precheck before an expensive build when signatures or parameterized-test structure changed:

```bash
python3 .codex/skills/gen-ut/scripts/scan_quality_rules.py \
  --precheck-only <ResolvedTestFileSet>
```

Run focused tests after each coherent edit:

```bash
./mvnw -pl <ResolvedTestModules> -DskipITs -Dspotless.skip=true \
  -Dtest=<ResolvedTestClass> -Dsurefire.failIfNoSpecifiedTests=false test
```

Then run the mechanical rules and scope guard:

```bash
python3 .codex/skills/gen-ut/scripts/scan_quality_rules.py \
  --scope-baseline "$task_dir/scope-baseline.json" \
  <ResolvedTestFileSet>
```

The scanner labels semantic or partly semantic rules as `semanticReviewRequired=true`. Resolve them through source inspection and record evidence; zero mechanical violations alone is not `R10-A`.

## Final coverage

Use current focused coverage evidence for the effective candidate.
When that evidence is missing or invalid, use the module's configured JaCoCo check when present; otherwise run `test jacoco:report` and collect target ratios with the bundled reporter:

```bash
python3 .codex/skills/gen-ut/scripts/collect_quality_baseline.py \
  --jacoco-xml-path <JacocoXmlPath> \
  --target-classes <ResolvedTargetClasses> \
  --scope-baseline "$task_dir/scope-baseline.json" \
  <ResolvedTestFileSet>
```

For an explicit numeric CLASS, LINE, or BRANCH target, compare only that metric against the requested threshold using the raw JaCoCo counters for every resolved target and inner class.
Use covered and missed counts rather than rounded displayed percentages; a target's aggregate must not compensate for an unmet class, inner class, or another target.
Add `--minimum-ratio <TargetRatioPercent>` only when the user requests the same threshold for all three metrics; retain the reporter's existing aggregate checks, which do not replace per-class acceptance.
Without an explicit numeric target, decide completion from the requested-behavior and affected-SUT-owned-branch map while still reporting the available counters.
A missing target or mandatory CLASS or LINE counter blocks completion even without `--minimum-ratio`; the reporter's zero exit code alone is not coverage proof.
A valid branchless class satisfies BRANCH coverage with zero missed branches, including a zero/zero counter.

## Repository completion gates

After the last edit, run the applicable Spotless and Checkstyle commands required by the [code-implementation verification rules](../../code-implementation/references/verification.md), through the canonical output wrapper.
Use those rules to select read-only checks first and determine whether a needed formatter write is authorized.

Reuse a complete scan or focused coverage result only within the current task while its relevant sources, providers, configuration, tool version, execution filters, scope baseline, and required semantic evidence remain unchanged.
Rerun checks invalidated by a relevant write, scope change, failure, or new evidence.
A lightweight precheck is not a full scan and cannot establish scope compliance or `R10-A`.
