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

# MCP Configuration Input Validation Alignment Scope Map

This map records MCP YAML configuration and direct counterpart configuration input validation candidates only.
Line numbers are based on the current working tree at the time this scope was drafted and may shift during implementation.

## Source Boundary

In scope:

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncher.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/metadata/jdbc/RuntimeDatabaseConfiguration.java`

Out of scope:

- MCP descriptor catalog and descriptor YAML under `mcp/support/descriptor`.
- Registry metadata, including `server.json` publication.
- Tool, resource, prompt, completion, and catalog semantic validation.
- HTTP request headers, sessions, request authorization decisions, and transport lifecycle state.

## Directly Movable

These checks can move to existing YAML configuration validation entry points with built-in constraints or already-present DTO validation.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPLaunchConfigurationSwapper.java`
  - Current checks: root launch configuration null check and `transport` null check.
  - Target: validate `YamlMCPLaunchConfiguration` before swapping; keep `transport` covered by `@NotNull` and nested `@Valid`.
  - Implementation note: remove swapper duplication only after the launcher/swapper path invokes validation consistently.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlMCPTransportConfiguration.java`
  - Current checks: `http` and `stdio` sections are required by DTO annotations, with duplicate null checks in downstream swappers.
  - Target: keep section presence as YAML DTO validation through `@NotNull` and nested `@Valid`.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`
  - Current checks: null config, required host, required port, non-negative port, endpoint path presence, and endpoint path prefix.
  - Target: `@NotNull`, `@NotBlank`, `@PositiveOrZero`, and `@Pattern` on `YamlHttpTransportConfiguration` or equivalent validated DTO properties.
  - Implementation note: preserve any post-normalization defaults that are not raw validation errors.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlOAuthIntrospectionConfiguration.java`
  - Current checks: `cacheTtlMillis` must be zero or positive when present.
  - Target: keep `@PositiveOrZero` as YAML DTO validation and ensure nested validation is invoked from HTTP configuration.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlStdioTransportConfigurationSwapper.java`
  - Current check: root STDIO configuration null check.
  - Target: call the same YAML configuration validator as the HTTP path or validate the STDIO DTO before swapping.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlRuntimeDatabaseConfigurationSwapper.java`
  - Current checks: null config, database type, JDBC URL, username, password, and driver class presence.
  - Target: `@NotNull`, `@NotBlank`, and nested DTO validation on the YAML runtime database DTO.
  - Keep outside raw DTO validation: post-environment-resolution blank checks for placeholder-backed values.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncher.java`
  - Current check: at least one runtime database is required.
  - Target: `@NotEmpty` on the YAML launch DTO collection and an equivalent counterpart configuration input guard when programmatic paths bypass YAML validation.

## Movable With Custom Constraints Or DTO Adjustments

These checks are still in scope, but they need class-level constraints, type-use constraints, DTO typing changes, validation groups, or resolved configuration validators.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlMCPLaunchConfiguration.java`
  - Current concern: `runtimeDatabases` is `Map<String, Map<String, Object>>`, so nested runtime database DTO validation is indirect.
  - Target: use a typed map, map-value validation, or a focused custom validator so runtime database entries are validated as configuration input.
  - Compatibility note: preserve the external YAML map shape and unsupported-property behavior.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPLaunchConfiguration.java`
  - Current checks: HTTP and STDIO cannot both be enabled, and exactly one transport must be enabled.
  - Target: class-level configuration input validation on `MCPLaunchConfiguration` or an equivalent resolved launch configuration validator.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
  - Current checks: remote HTTP access requires explicit opt-in, non-loopback bindings require allowed origins, and remote access requires authorization.
  - Target: class-level configuration input validation on resolved `HttpTransportConfiguration`.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
  - Current checks: allowed origins must be valid HTTP or HTTPS origins.
  - Target: collection element validation or class-level validation on resolved HTTP configuration because values may contain YAML placeholders before resolution.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
  - Current checks: `accessToken` and OAuth introspection endpoint cannot both be configured.
  - Target: class-level configuration input validation on resolved HTTP configuration.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
  - Current checks: authorization servers are required when authorization is enabled and must be valid HTTPS URLs without fragments.
  - Target: collection element validation or class-level validation on resolved HTTP configuration.

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
  - Current checks: OAuth introspection requires a valid endpoint, client ID, client secret, non-negative cache TTL, and valid expected issuer.
  - Target: class-level validation on `OAuthIntrospectionConfiguration` and/or the resolved HTTP configuration.

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/metadata/jdbc/RuntimeDatabaseConfiguration.java`
  - Current check: `databaseType` must be non-null and non-blank for programmatic configuration input.
  - Target: retain or centralize as counterpart configuration input validation so non-YAML construction remains protected.

## Intentionally Retained In Domain Or Runtime Validation

These are reviewed but are not part of the Bean Validation migration unless a later design proves equivalent context is available.

- Descriptor catalog validation, descriptor YAML validation, registry metadata validation, and catalog semantics.
- Tool, resource, prompt, and completion schema validation.
- Runtime tool argument validation against JSON schema.
- HTTP request headers, security decisions, session state, and transport lifecycle checks.
- Registry connectivity, filesystem publication side effects, and environment-specific runtime state.
- Post-environment-resolution checks where raw YAML values may legally contain placeholders.

## Migration Decision Rules

- Move a check directly when it is deterministic from a single DTO field and can be expressed by a built-in constraint.
- Use a custom or class-level constraint when the configuration invariant spans multiple fields, collection elements, or map keys.
- Use DTO typing or validation groups when the current YAML configuration model prevents nested validation.
- Retain the check outside this scope when it needs descriptor/catalog context, SPI loading, registry state, request payload data, or transport runtime state.
