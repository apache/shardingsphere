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

After the last code-affecting action and before the applicable pre-handoff review, run read-only Spotless and Checkstyle checks for every applicable task-changed code file; run Spotless for a documentation-only change only when the configured formatter governs that file.

After the last file-changing action and before the applicable pre-handoff review, run Apache RAT when applicable.

1. Run `./mvnw apache-rat:check -Pcheck -T1C` from the repository root only when the task adds a file, changes a license header, changes `src/resources/rat.txt` or RAT/POM configuration, or explicitly requires CI-equivalent verification. Otherwise record why RAT is not applicable. A nonzero or inconclusive required result blocks review and handoff; fix only in-scope violations.
2. Resolve the exact task-changed files governed by Spotless and their smallest owning Maven project set, then run `./mvnw -pl <explicit-owner-set> -DspotlessFiles='<comma-separated exact absolute-path regular expressions>' spotless:check -Pcheck -T1C`. Construct each expression from the current workspace and task path without hard-coding a local workspace path, and ensure it selects only allowlisted files.
3. Run `spotless:apply` only after `spotless:check` reports an in-scope formatting failure and the user authorizes every file it can modify. Inspect every resulting hunk and rerun the same `spotless:check`; do not use a modifying formatter as the default verification path.
4. For production or test Java changes, run `./mvnw -pl <explicit-owner> -Dcheckstyle.includes='<comma-separated source-root-relative task files>' -Dcheckstyle.includeResources=false -Dcheckstyle.includeTestResources=false checkstyle:check -Pcheck -T1C` once per owning Maven project. For a changed resource or project-rule file governed by Checkstyle, use its exact supported resource filter; if the configured plugin cannot isolate that file, stop and report the minimum unavoidable scope instead of silently widening the check.
5. Treat a filtered command as passing only after its output and the post-command task-delta inspection prove that every applicable task file was selected, no non-allowlisted file was written, and the command exited successfully. A successful command that matched no intended file is not evidence.
6. Any later edit invalidates only the Spotless, Checkstyle, or RAT evidence whose governed files or configuration changed; rerun those checks before the applicable review.

Record every required command, exit code, decisive result, and remaining unverified path. A required check with missing, incomparable, unstable, or inconclusive evidence is not a pass.
