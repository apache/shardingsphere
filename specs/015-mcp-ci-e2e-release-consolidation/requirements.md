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

# MCP CI E2E And Release Consolidation Requirements

Canonical Speckit package: `.specify/specs/026-mcp-ci-e2e-release-consolidation/`
Current branch: `001-shardingsphere-mcp`
Branch constraint: do not switch or create branches for this work.

## Goal

Create a clean MCP CI target state:

- Replace workflow-invoked smoke tests with complete MySQL-backed E2E suites.
- Remove H2-backed production E2E from real MCP E2E evidence.
- Collapse distribution validation into one complete distribution E2E.
- Keep JDK 21 CI focused on Java 21 MCP build/runtime behavior.
- Strengthen release workflow by validating the published GHCR image and MCP Registry publication.

## User Constraints

- Do not modify the previous `024` Spec Kit package.
- Do not switch branches.
- Do not implement code in this design-only round.
- Preserve unrelated worktree changes.
- Use MySQL for real E2E.
- Do not keep smoke as the final E2E target.

## Functional Requirements

- Future implementation must first map current smoke topology coverage before deleting or de-targeting smoke tests.
- MySQL HTTP and MySQL STDIO full E2E must cover all workflow-protected smoke topology.
- H2-backed production E2E must leave real E2E workflows or be reclassified as lightweight non-production validation.
- LLM usability E2E must cover LLM smoke topology before `LLMSmokeE2ETest` is removed from workflow invocation.
- Final LLM E2E must not keep a standalone smoke workflow entry after usability becomes a topology superset.
- Distribution E2E must package `distribution/mcp` once and validate packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO.
- JDK 21 CI must validate Java 21 MCP build/runtime/distribution behavior.
- JDK 21 CI must not duplicate Checkstyle, Spotless, or RAT while Required Check already covers MCP files.
- Release workflow must pull and validate the published `ghcr.io/apache/shardingsphere-mcp:<version>` image after push.
- Release workflow must verify linux/amd64 and linux/arm64 manifest entries when both are pushed.
- Release runtime validation proves the native pulled runner platform; non-native platforms are manifest-validated unless a future implementation explicitly accepts emulated runtime validation.
- Release workflow must pin or integrity-check `mcp-publisher`.
- H2 defaults or documentation must be changed, renamed, or reclassified so H2 is not presented as production E2E evidence.
- Implementation touching `mcp`, `test/e2e/mcp`, or `distribution/mcp` must receive MCP builder review.
- Verification commands and exit codes must be reported after implementation.

## Verification Requirements

- `rg -n "Smoke|LLMSmoke|ProductionH2|ContainerStdioSmoke|PackagedDistributionSmoke" .github/workflows test/e2e/mcp/src/test/java` maps all smoke/H2 targets.
- `rg -n "checkstyle:check|spotless:check|apache-rat:check" .github/workflows/required-check.yml .github/workflows/jdk21-subchain-ci.yml` confirms style/license responsibility.
- Workflow selectors no longer invoke smoke-only E2E classes in the final target state.
- MySQL HTTP and MySQL STDIO tests pass.
- Complete distribution E2E validates packaged and container runtime paths.
- Release workflow records published image tag, digest or manifest, runtime validation, and MCP Registry server identifier.
