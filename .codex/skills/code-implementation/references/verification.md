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

After the last file-changing action:

1. Run `./mvnw spotless:apply -Pcheck -T1C` for code or documentation changes.
2. Run `./mvnw checkstyle:check -Pcheck -T1C` when production, test, or project-rule files changed.
3. Do not manually reformat afterward. Any later edit invalidates formatting and requires the applicable checks again.

Record every required command, exit code, decisive result, and remaining unverified path. A required check with missing, incomparable, unstable, or inconclusive evidence is not a pass.
