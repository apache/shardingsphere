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

# E2E, LLM, and Operations Evidence

## Scope

- Evidence date: 2026-05-16.
- Branch: `001-shardingsphere-mcp`.
- Branch switching: no `git switch`, `git checkout`, branch creation script, or branch-changing Speckit command was used.
- Functional scope: encrypt and mask workflows only.
- Tool-result format: structured JSON plus serialized JSON text fallback; Markdown remains optional report readability, not a tool-result requirement.

## Commands

- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=MCPBuilderEvaluationArtifactTest -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; tests run `1`; duration `8.562s`.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; tests run `270`; skipped `15`; duration `3:00`.
- `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests -DskipITs checkstyle:check -B -ntp`
  Exit code `0`; duration `1.921s`.
- `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests -DskipITs spotless:check -B -ntp`
  Exit code `0`; duration `2.477s`.
- `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; duration `35.860s`.
- `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipTests -DskipITs checkstyle:check -B -ntp`
  Exit code `0`; final rerun duration `8.963s`.
- `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp -Pcheck -DskipTests -DskipITs spotless:check -B -ntp`
  Exit code `0`; final rerun duration `2.520s`.
- `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests -DskipITs spotless:apply -B -ntp`
  Exit code `0`; formatted the three touched Java test files to the repository Spotless profile.
- `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests -DskipITs checkstyle:check -B -ntp`
  Final rerun exit code `0`; duration `2.755s`.
- `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests -DskipITs spotless:check -B -ntp`
  Final rerun exit code `0`; duration `2.594s`.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=ProductionMySQLRuntimeSmokeE2ETest -Dmcp.e2e.production.mysql.enabled=true -Dmcp.e2e.production.stdio.enabled=true -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; tests run `22`; duration `2:18`.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=PackagedDistributionSmokeE2ETest -Dmcp.e2e.distribution.enabled=true -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; tests run `2`; duration `6.098s`.
- `./mvnw -pl test/e2e/mcp -Pllm-e2e -DskipITs -Dspotless.skip=true -Dtest=LLMSmokeE2ETest -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; tests run `4`; duration `12:10`.
- `./mvnw -pl test/e2e/mcp -Pllm-e2e -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilitySuiteE2ETest -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Initial run exit code `1`; root cause was `extended-database-disambiguation-h2` invalid metadata-search retry arguments copied from response context.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilityScenarioCatalogTest -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Exit code `0`; tests run `2`; duration `4.852s`.
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=MCPBuilderEvaluationArtifactTest,LLMUsabilityScenarioCatalogTest -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Final rerun exit code `0`; tests run `3`; duration `4.595s`.
- `./mvnw -pl test/e2e/mcp -Pllm-e2e -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilitySuiteE2ETest -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp`
  Rerun exit code `0`; tests run `1`; duration `14:52`.

## LLM Scorecards

- Run id: `20260516015738-b8a8d5be`.
- Core scorecard: `overallScore=100.0`, `fullScore=true`, `invalidCallRate=0.0`, `nativeToolCallRate=1.0`, `harnessRecoveryRate=0.0`.
- Extended scorecard: `overallScore=100.0`, `fullScore=true`, `invalidCallRate=0.0`, `nativeToolCallRate=1.0`, `harnessRecoveryRate=0.0`.
- Extended multi-database scenario evidence: read `shardingsphere://databases`, called `database_gateway_search_metadata`
  with `database`, `schema`, `query`, and `object_types=["table"]`, then executed the read-only count query.

## Infrastructure

- `docker info --format '{{.ServerVersion}}'`: exit code `0`, server version `27.3.1`.
- `docker image inspect ollama/ollama:latest >/dev/null 2>&1 && echo present || echo missing`: exit code `0`, image present.
- Local `ollama` CLI was not installed, so LLM lanes used the available Docker/Testcontainers path.
