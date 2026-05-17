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

# Implementation Plan: MCP Registry Release Validation

**Branch**: `001-shardingsphere-mcp`  
**Spec**: `.specify/specs/022-mcp-registry-release-validation/spec.md`  
**Source Map**: `.specify/specs/022-mcp-registry-release-validation/source-map.md`  
**Input**: Add static Dockerfile metadata validation to the existing MCP Registry release command and use it in CI/release workflows.

## Technical Context

**Language/Version**: Java 21 MCP subchain  
**Primary Dependencies**: Jackson for `server.json`, Maven Exec Plugin for CI command invocation, existing GitHub Actions Docker build workflow  
**Storage**: `mcp/server.json`, `distribution/mcp/Dockerfile`  
**Testing**: JUnit 5, Hamcrest, module-scoped Maven test and style commands  
**Target Modules**: `mcp/registry`, `.github/workflows`, Speckit documentation  
**Constraints**: No branch switching; no dynamic image inspection in this slice; no generated directory edits; do not touch unrelated dirty MCP runtime files  

## Framework Basis

- MCP Registry documentation states Docker/OCI packages use `registryType: oci` and the Docker/OCI identifier format `registry/namespace/repository:tag`.
- MCP Registry ownership verification requires `io.modelcontextprotocol.server.name` to match the `server.json` server name.
- OCI image annotations define `org.opencontainers.image.version` as the packaged software version metadata key.
- Maven Exec Plugin `exec:java` runs the configured Java class inside the current Maven project with project dependencies on the classpath.
- Spec Kit is used here as a specification-driven documentation structure, maintained manually because branch switching is forbidden.

## Governance Check

- Use the existing branch `001-shardingsphere-mcp`; do not run branch-switching or branch-creation commands.
- Keep publication-only validation under `mcp/registry`.
- Keep runtime MCP modules untouched for this requirement.
- Add the smallest validator needed for static Dockerfile metadata checks.
- Keep messages actionable and mapped to the missing Dockerfile requirement.
- Do not add `docker inspect`, `docker buildx imagetools inspect`, registry pulls, or post-push checks.
- Run scoped tests and style checks for touched modules.
- Apply mcp-builder review because this change touches the MCP code path.

## Project Structure

```text
.specify/specs/022-mcp-registry-release-validation/
|-- spec.md
|-- plan.md
|-- source-map.md
|-- tasks.md
`-- checklists/
    `-- requirements.md

specs/013-mcp-registry-release-validation/
`-- requirements.md
```

## Source Areas

- `mcp/registry/src/main/java/org/apache/shardingsphere/mcp/registry/MCPRegistryMetadataCommand.java`
- `mcp/registry/src/main/java/org/apache/shardingsphere/mcp/registry/`
- `mcp/registry/src/test/java/org/apache/shardingsphere/mcp/registry/MCPRegistryMetadataCommandTest.java`
- `distribution/mcp/Dockerfile`
- `.github/workflows/jdk21-subchain-ci.yml`
- `.github/workflows/mcp-build.yml`

## Execution Strategy

### Phase 1 - Baseline And Sources

1. Confirm the active branch remains `001-shardingsphere-mcp`.
2. Re-scan current MCP Registry command, tests, Dockerfile, and workflow invocations.
3. Record official MCP Registry, OCI, Maven Exec Plugin, and Spec Kit source evidence.

### Phase 2 - Static Dockerfile Validator

1. Add an optional `--dockerfile-path` command argument.
2. Validate Dockerfile metadata only when the argument is supplied.
3. Require `ARG MCP_SERVER_NAME=<server.json name>`.
4. Require `io.modelcontextprotocol.server.name="${MCP_SERVER_NAME}"`.
5. Require `ARG MCP_IMAGE_VERSION=unknown`.
6. Require `org.opencontainers.image.version="${MCP_IMAGE_VERSION}"`.

### Phase 3 - Tests

1. Add a valid Dockerfile metadata scenario.
2. Add one failure scenario for missing option value.
3. Add one failure scenario for mismatched `MCP_SERVER_NAME`.
4. Add one failure scenario for missing MCP Registry ownership label.
5. Add one failure scenario for missing image version build argument.
6. Add one failure scenario for missing OCI image version label.
7. Add source-file validation covering `mcp/server.json` with `distribution/mcp/Dockerfile`.

### Phase 4 - Workflow Integration

1. Replace shell `grep` Dockerfile checks in JDK 21 subchain CI with the Java validator.
2. Add Dockerfile validation to the release metadata prepare/validate steps before Docker image push.
3. Keep release image build and registry publication flow unchanged.

### Phase 5 - Verification And Review

1. Run `mcp/registry` unit tests.
2. Run module-scoped Spotless and Checkstyle.
3. Run static source metadata validation with the real Dockerfile.
4. Run distribution package validation if the current dirty workspace permits it.
5. Run mcp-builder and doubt-driven self-review against the final artifact.

## Verification Bar

- Every new Dockerfile validation branch has exactly one dedicated test scenario.
- Workflow validation uses the Java command path rather than duplicated shell pattern checks.
- Checkstyle and Spotless pass for `mcp/registry`.
- The static validator reads only source files and does not depend on a built image.

## Risk Register

- **Static-only blind spot**: Static Dockerfile checks cannot prove the final pushed multi-arch image labels survived the build. Accepted for this slice because dynamic label inspection is explicitly out of scope.
- **Dockerfile formatting drift**: Validation uses exact line checks for build args and substring checks for label entries. This intentionally keeps the contract strict and reviewer-visible.
- **Workflow drift**: If future workflows bypass `MCPRegistryMetadataCommand`, Dockerfile metadata checks can drift. Mitigate by invoking the Java validator in both CI and release workflows.
