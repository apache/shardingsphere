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

# MCP Configuration Input Validation Alignment Requirements

Canonical Speckit package: `.specify/specs/021-mcp-validation-framework-alignment/`  
Current branch: `001-shardingsphere-mcp`  
Branch constraint: do not switch or create branches for this work.

## Goal

Move MCP YAML configuration input validation and direct counterpart configuration input validation into validation-driven constraints where practical.
The scope is limited to bootstrap configuration DTOs, configuration swappers, launch/transport configuration objects, and the direct runtime database configuration object created from YAML.

## User Constraints

- Do not switch branches.
- Do not implement production code in this Speckit organization pass.
- Include both directly movable and custom/DTO-adjustment configuration candidates in scope.
- Keep descriptor catalog, registry metadata, tool/resource/prompt schema, request-state, and runtime lifecycle checks outside this migration.

## Direct Migration Scope

- Launch YAML configuration: root config, required transport, and non-empty runtime database collection.
- Transport YAML configuration: required HTTP and STDIO sections with nested validation.
- HTTP transport YAML configuration: required host, required port, non-negative or valid port, endpoint path pattern, and OAuth cache TTL.
- STDIO transport YAML configuration: validate before swapping instead of relying on swapper null checks.
- Runtime database YAML configuration: required DTO, database type, JDBC URL, username, password, and driver class name.
- Post-environment-resolution blank checks stay on resolved configuration input, not raw YAML fields.

## Custom Constraint Or DTO Adjustment Scope

- Runtime database map entries: typed map-value validation or a focused custom validator for `runtimeDatabases`.
- Launch transport exclusivity: exactly one of HTTP and STDIO can be enabled.
- HTTP remote access safety: non-loopback binding requires explicit remote access opt-in, allowed origins, and authorization.
- HTTP origin validation: allowed origins must be valid HTTP or HTTPS origins after placeholder resolution.
- HTTP authorization consistency: bearer token and OAuth introspection cannot both be configured.
- Authorization servers: required when authorization is enabled and must be HTTPS URLs without fragments.
- OAuth introspection: endpoint, client ID, client secret, cache TTL, and expected issuer must be consistent after placeholder resolution.
- Runtime database counterpart validation: non-YAML construction must still protect required configuration input such as `databaseType`.

## Out Of Scope

- Branch switching, branch creation, commits, pushes, dependency upgrades, or broad refactors.
- Descriptor catalog validation and descriptor YAML validation.
- Registry metadata validation, including `server.json`.
- Tool, resource, prompt, completion, and catalog semantic validation.
- Dynamic runtime tool argument JSON-schema validation.
- HTTP request headers, request security decisions, session state, lifecycle state, registry connectivity, and filesystem side effects.

## Completion Rule

The implementation is complete only when every configuration candidate in `.specify/specs/021-mcp-validation-framework-alignment/scope-map.md` has a recorded disposition.
Migrated checks must have targeted tests, valid launch configuration examples must still pass, and scoped tests and style checks must pass for touched modules.
The final handoff must confirm the branch remained unchanged.
