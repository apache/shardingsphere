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

# Source Map: MCP CI E2E And Release Consolidation

## Official Sources

- MCP transport specification: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
  - Source-driven decision: MCP defines `stdio` and `Streamable HTTP` as standard transports, so complete MCP runtime E2E must cover both when validating transport behavior.
  - Source-driven decision: Streamable HTTP is a single endpoint that supports POST and GET, so HTTP E2E must validate runtime behavior rather than just process startup.
- GitHub Actions workflow syntax: `https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax`
  - Source-driven decision: workflow files live under `.github/workflows`, support path filters, job timeouts, service containers, and matrix controls; workflow consolidation must stay within those supported primitives.
- GitHub Container Registry documentation: `https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry`
  - Source-driven decision: GHCR supports image push and pull by tag or digest, so release validation can and should pull the published image after push.
- Docker pull reference: `https://docs.docker.com/reference/cli/docker/image/pull/`
  - Source-driven decision: pulling an image validates the selected platform image on the runner; multi-platform publication still needs manifest inspection for platforms not executed natively.
- Maven Toolchains Plugin documentation: `https://maven.apache.org/plugins/maven-toolchains-plugin/`
  - Source-driven decision: toolchains allow Maven plugins to use a selected JDK independent of the JRE running Maven, which justifies a dedicated Java 21 MCP subchain validation lane.
- Maven Compiler Plugin different-JDK guidance: `https://maven.apache.org/plugins/maven-compiler-plugin-4.x/examples/compile-using-different-jdk.html`
  - Source-driven decision: Java compilation and related build steps can depend on the selected JDK, so non-JDK21 CI is not enough to prove Java 21 MCP behavior.

## Repository Governance Sources

- `AGENTS.md` supplied in the thread
  - Branch, destructive operation, testing, Checkstyle/Spotless, MCP review, and no-generated-target constraints.
- `CODE_OF_CONDUCT.md`
  - Repository CI/workflow naming and quality expectations referenced by AGENTS instructions.
- `.specify/memory/constitution.md`
  - Specification drives plan and tasks; implementation must not precede reviewed requirements.

## Workflow Paths

- `.github/workflows/jdk21-subchain-ci.yml`
  - JDK 21 MCP lane.
  - Final target: MySQL HTTP/STDIO runtime E2E plus one complete distribution E2E job; no MCP smoke-only selector remains.
- `.github/workflows/mcp-build.yml`
  - Release build, GHCR push, and MCP Registry publish workflow.
  - Final target: local pre-push distribution E2E remains, then the pushed GHCR image digest is inspected, pulled, and runtime-validated on the native runner platform.
- `.github/workflows/mcp-llm-e2e.yml`
  - Removed after LLM usability became the complete MySQL HTTP/STDIO target.
- `.github/workflows/mcp-llm-usability-e2e.yml`
  - LLM usability workflow invoking `LLMUsabilitySuiteE2ETest` as the single complete LLM E2E target.
- `.github/workflows/required-check.yml`
  - Repo-wide Checkstyle, Spotless, and RAT owner.
  - Current evidence: the workflow runs `./mvnw checkstyle:check -Pcheck -T1C`, `./mvnw spotless:check -Pcheck -T1C`, and `./mvnw apache-rat:check -Pcheck -T1C` without workflow path filters.
- `pom.xml`
  - Current evidence: the root reactor includes `mcp`, `test`, and `distribution`.
- `test/e2e/pom.xml`
  - Current evidence: the E2E reactor includes `mcp`.
- `distribution/pom.xml`
  - Current evidence: the distribution reactor includes `mcp`.

## MCP E2E Test Paths

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/smoke/LLMSmokeE2ETest.java`
  - Removed after usability absorbed the topology.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/LLMUsabilitySuiteE2ETest.java`
  - Final target: parameterized MySQL HTTP and MySQL STDIO usability suite.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/scenario/LLMUsabilityScenarioCatalog.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ProductionMySQLRuntimeE2ETest.java`
  - Final target: MySQL HTTP and STDIO production runtime E2E.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionE2ETest.java`
  - Final target: packaged HTTP, packaged STDIO, packaged plugin discovery, container HTTP, and container STDIO from one distribution E2E suite.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ContainerStdioSmokeE2ETest.java`
  - Removed after container STDIO moved into `PackagedDistributionE2ETest`.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionSmokeE2ETest.java`
  - Removed after packaged HTTP/STDIO moved into `PackagedDistributionE2ETest`.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionPluginDiscoveryE2ETest.java`
  - Removed after plugin discovery moved into `PackagedDistributionE2ETest`.
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyEncryptWorkflowE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyMaskWorkflowE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/runtime/MySQLRuntimeTestSupport.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/runtime/H2RuntimeConfigurationTestSupport.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/MCPHttpInteractionClient.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/MCPStdioInteractionClient.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/DockerImageStdioInteractionClient.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/distribution/PackagedDistributionTestSupport.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/distribution/DockerImageHttpRuntime.java`
- `test/e2e/mcp/src/test/resources/env/e2e-env.properties`
  - Final target: `mcp.e2e.production.h2.enabled=false`, so H2 is not default production E2E evidence.

## MCP Distribution Paths

- `distribution/mcp/pom.xml`
- `distribution/mcp/Dockerfile`
- `distribution/mcp/src/main/bin/docker-entrypoint.sh`
- `distribution/mcp/src/main/bin/start.sh`
- `distribution/mcp/src/main/bin/start.bat`
- `distribution/mcp/src/main/resources/conf/mcp-http.yaml`
- `distribution/mcp/src/main/resources/conf/mcp-stdio.yaml`

## Search And Verification Targets

- `rg -n "Smoke|LLMSmoke|ProductionH2|ContainerStdioSmoke|PackagedDistributionSmoke" .github/workflows test/e2e/mcp/src/test/java`
- `rg -n "checkstyle:check|spotless:check|apache-rat:check" .github/workflows/required-check.yml .github/workflows/jdk21-subchain-ci.yml`
- `rg -n "ghcr.io/apache/shardingsphere-mcp|docker/build-push-action|mcp-publisher|manifest|digest" .github/workflows/mcp-build.yml`
- `rg -n "mcp.e2e.production.h2.enabled|mcp.e2e.production.mysql.enabled|mcp.e2e.production.stdio.enabled" .github/workflows test/e2e/mcp/src/test/resources`

## Review Gates

- Source-driven review: re-check official docs if workflow syntax, GHCR behavior, Maven Toolchains, or MCP transport versions are changed during implementation.
- Doubt-driven review: re-run the smoke-removal, H2-removal, distribution/release-boundary, and JDK21-style-gate claims before implementation handoff.
- Reanalysis review: re-check release platform scope, H2 default semantics, Required Check evidence, and LLM smoke workflow removal before implementation handoff.
- MCP builder review: required for implementation touching `mcp`, `test/e2e/mcp`, or `distribution/mcp`.
