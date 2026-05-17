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

# Source Map: MCP Registry Release Validation

## Official Sources

- MCP Registry package types: https://modelcontextprotocol.io/registry/package-types
  - Docker/OCI packages use `registryType: "oci"`.
  - Docker/OCI identifiers use `registry/namespace/repository:tag`.
  - Docker/OCI ownership verification checks `io.modelcontextprotocol.server.name`.
  - The ownership value must match the `server.json` server name.
- OCI Image Specification annotations: https://oci-playground.github.io/specs-latest/specs/image/v1.0.0/oci-image-spec.html#annotations
  - Annotation keys are string key-value metadata.
  - The `org.opencontainers.image` namespace is reserved for image-spec keys.
  - `org.opencontainers.image.version` describes the packaged software version.
- Maven Exec Plugin `exec:java`: https://www.mojohaus.org/exec-maven-plugin/java-mojo.html
  - `exec:java` executes the configured Java class in the current Maven project.
  - The command supports class arguments via the `exec.args` user property.
- GitHub Spec Kit documentation: https://github.github.com/spec-kit/index.html
  - Spec Kit is a specification-driven development toolkit.
  - This repository records the requirement manually because branch switching is forbidden.

## Requirement Traceability

- **FR-002 / runtime boundary**: `mcp/registry` owns publication validation; MCP bootstrap runtime modules are not changed by this requirement.
- **FR-003 / server metadata**: `MCPRegistryMetadataCommand` continues validating schema URL, name, versions, OCI identifiers, package transports, HTTP URL, and required environment variables.
- **FR-005 and FR-006 / MCP Registry ownership**: static Dockerfile validation enforces the server-name build argument and ownership label required by MCP Registry documentation.
- **FR-007 and FR-008 / OCI version metadata**: static Dockerfile validation enforces the image-version build argument and OCI version label.
- **FR-010 / static-only scope**: dynamic image inspection is documented as out of scope and must not be introduced in this implementation slice.
- **FR-011 / workflow consistency**: both JDK 21 subchain CI and release build workflows call the Java validator.

## Local Evidence

- `mcp/server.json` declares `name: io.github.apache/shardingsphere-mcp`, `registryType: oci`, and `ghcr.io/apache/shardingsphere-mcp:<tag>` identifiers.
- `distribution/mcp/Dockerfile` defines `ARG MCP_SERVER_NAME`, `ARG MCP_IMAGE_VERSION`, `io.modelcontextprotocol.server.name`, and `org.opencontainers.image.version`.
- `.github/workflows/jdk21-subchain-ci.yml` validates release metadata during the distribution lane.
- `.github/workflows/mcp-build.yml` prepares and validates registry metadata before Docker image push and registry publication.
