<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Verification and Commands

Run the narrowest meaningful checks first. Derive explicit Maven modules from changed owners, affected tests, and consuming runtime modules.

- Focused test: `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=<FullyQualifiedTestClassName> -Dsurefire.failIfNoSpecifiedTests=false test`
- Scoped tests: `./mvnw test -pl <explicit-module-set>`
- Scoped package: `./mvnw -pl <explicit-module-set> -DskipTests package`
- Coverage: `./mvnw -pl <explicit-module-set> -Djacoco.skip=false test jacoco:report`
- Full build: `./mvnw clean install -B -T1C -Pcheck`

Prefer current-source IDE/MCP runs or explicit `-pl` module sets. Use `-am` only when dependency freshness, missing reactor artifacts, or CI equivalence cannot otherwise be established, normally once per unchanged task state. For multi-module checks, verify lower-level changed owners before their higher-level consumers.

Keep background unit tests under 60 seconds. Capture high-volume output according to `.codex/context/token-efficiency.md`; report commands, exit codes, and decisive log excerpts instead of dumping raw logs.

For every user-forbidden tool, API, assertion, or pattern, run a scoped final search and report the command and result. Do not rely only on plan compliance.

After the last code-affecting action and before the pre-handoff Formal Review, run both Spotless and Checkstyle for every applicable task-changed code file; run Spotless for a documentation-only change when the configured formatter governs that file.

After the last file-changing action and before the pre-handoff Formal Review, run Apache RAT when applicable.

1. Skip Apache RAT only when `src/resources/rat.txt` is unchanged and excludes every task-changed file; this includes a Markdown-only task. Otherwise, run `./mvnw apache-rat:check -Pcheck -T1C` from the repository root after the last write. A nonzero or inconclusive RAT result blocks Formal Review and handoff; fix only in-scope violations and stop at the scope gate for an unrelated violation.
2. Resolve the exact task-changed files governed by Spotless and their owning Maven projects, then run `./mvnw -pl <explicit-owner> -DspotlessFiles='<comma-separated exact absolute-path regular expressions>' spotless:apply -Pcheck -T1C` for each smallest owner set. Construct each regular expression from the current workspace and repository-relative task path without hard-coding a local workspace path, and ensure it can match only an allowlisted task file before running this file-modifying goal.
3. For production or test Java changes, run `./mvnw -pl <explicit-owner> -Dcheckstyle.includes='<comma-separated source-root-relative task files>' -Dcheckstyle.includeResources=false -Dcheckstyle.includeTestResources=false checkstyle:check -Pcheck -T1C` once per owning Maven project. For a changed resource or project-rule file governed by Checkstyle, use its exact supported resource filter; if the configured plugin cannot isolate that file, stop and report the minimum unavoidable scope instead of silently widening the check.
4. Treat a filtered command as passing only after its output and the post-command task-delta inspection prove that every applicable task file was selected, no non-allowlisted file was written, and the command exited successfully. A successful command that matched no intended file is not evidence.
5. Do not manually reformat afterward. Any later edit invalidates the affected Spotless and Checkstyle evidence and requires both applicable checks to run again before another Formal Review. It also invalidates Apache RAT evidence when the later edit changes `src/resources/rat.txt` or a RAT-covered file.

Record every required command, exit code, decisive result, and remaining unverified path. A required check with missing, incomparable, unstable, or inconclusive evidence is not a pass.
