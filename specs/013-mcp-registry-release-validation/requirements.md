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

# MCP Registry Release Validation Requirements

Canonical Speckit package: `.specify/specs/022-mcp-registry-release-validation/`  
Current branch: `001-shardingsphere-mcp`  
Branch constraint: do not switch or create branches for this work.

## Goal

Keep MCP Registry publication checks out of production runtime code and strengthen CI/release validation by statically checking the Dockerfile metadata that MCP Registry and ShardingSphere OCI publication depend on.

## User Constraints

- Do not switch branches.
- Use Speckit to manage the requirement.
- Do not add dynamic post-build real image label validation in this slice.
- If MCP code changes, review the design and implementation with mcp-builder.
- Before handoff, confirm whether any open question still requires user confirmation.

## Functional Requirements

- `mcp/registry` remains the owner of `server.json` preparation and validation.
- `MCPRegistryMetadataCommand` continues validating schema URL, official server name, concrete versions, OCI identifiers, package transports, HTTP URL, and required environment variables.
- The command accepts an optional Dockerfile path for static Dockerfile metadata validation.
- Dockerfile validation requires `ARG MCP_SERVER_NAME=io.github.apache/shardingsphere-mcp`.
- Dockerfile validation requires `io.modelcontextprotocol.server.name="${MCP_SERVER_NAME}"`.
- Dockerfile validation requires `ARG MCP_IMAGE_VERSION=unknown`.
- Dockerfile validation requires `org.opencontainers.image.version="${MCP_IMAGE_VERSION}"`.
- CI and release workflows use the Java validator instead of shell-only duplicated Dockerfile checks.
- Dynamic image inspection with `docker inspect`, `docker buildx imagetools inspect`, image pull, or post-push label gates is out of scope.

## Verification Requirements

- Add focused unit tests for valid Dockerfile metadata and each failure path.
- Run scoped `mcp/registry` tests.
- Run scoped Spotless and Checkstyle for `mcp/registry`.
- Run the command against real `mcp/server.json` and `distribution/mcp/Dockerfile`.
- Report all command exit codes and any residual risk.
