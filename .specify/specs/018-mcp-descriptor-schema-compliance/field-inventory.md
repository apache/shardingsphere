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

# Field Inventory: MCP Descriptor Schema Compliance

**Protocol baseline**: MCP Specification `2025-11-25`
**Local SDK baseline**: MCP Java SDK `1.1.2`

## Resource Fields

- `Resource.size`
  - MCP schema: supported optional official field.
  - SDK `1.1.2`: supported by `McpSchema.Resource.size()`.
  - ShardingSphere status: implemented for fixed resources only.
  - Descriptor policy: must be an integer and non-negative when present.
- `Resource.icons`
  - MCP schema: supported optional official field.
  - SDK `1.1.2`: not exposed by `McpSchema.Resource`.
  - ShardingSphere status: deferred until the SDK boundary supports it.

## Resource Template Fields

- `ResourceTemplate.icons`
  - MCP schema: supported optional official field.
  - SDK `1.1.2`: not exposed by `McpSchema.ResourceTemplate`.
  - ShardingSphere status: deferred until the SDK boundary supports it.
- `ResourceTemplate.size`
  - MCP schema: not supported.
  - SDK `1.1.2`: not exposed.
  - ShardingSphere status: rejected by raw descriptor validation.

## Tool Fields

- `Tool.icons`
  - MCP schema: supported optional official field.
  - SDK `1.1.2`: not exposed by `McpSchema.Tool`.
  - ShardingSphere status: deferred until the SDK boundary supports it.
- `Tool.execution.taskSupport`
  - MCP schema: supported optional official field with values `forbidden`, `optional`, and `required`.
  - SDK `1.1.2`: `ToolExecution` is not exposed.
  - ShardingSphere status: deferred until the SDK boundary supports it.

## Metadata Boundary

- Descriptor YAML uses `meta` for developer-authored MCP metadata.
- MCP Java SDK `meta()` methods serialize as official `_meta`.
- ShardingSphere descriptor-only extension and runtime fields must remain separate from official descriptor fields.
