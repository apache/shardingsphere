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

# Annotation Inventory: MCP Annotations Protocol Compliance

**Source**: Production descriptor YAML under `mcp/*/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/`
**Captured**: 2026-05-14 on branch `001-shardingsphere-mcp`

## Resource Annotations

- `server-capability-catalog`
  - Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - Current annotations: `audience=[assistant]`, `priority=1.0`
  - Target policy: explicit annotations required because this is the high-priority ShardingSphere catalog entry point.
- `runtime-status`
  - Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - Current annotations: `audience=[assistant]`, `priority=0.9`
  - Target policy: explicit annotations allowed and recommended because this is model-facing runtime guidance.
- `logical-databases`
  - Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - Current annotations: empty
  - Target policy: empty allowed because it is an ordinary list resource with no special audience, priority, or last-modified signal.
- `encrypt-algorithms`
  - Path: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-encrypt.yaml`
  - Current annotations: empty
  - Target policy: empty allowed because it is ordinary feature metadata.
- `mask-algorithms`
  - Path: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-mask.yaml`
  - Current annotations: empty
  - Target policy: empty allowed because it is ordinary feature metadata.

## Tool Annotations

- `database_gateway_search_metadata`
  - Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - Current annotations: `readOnlyHint=true`, `destructiveHint=false`, `idempotentHint=true`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required.
- `database_gateway_execute_query`
  - Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - Current annotations: `readOnlyHint=true`, `destructiveHint=false`, `idempotentHint=true`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required.
- `database_gateway_execute_update`
  - Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
  - Current annotations: `readOnlyHint=false`, `destructiveHint=true`, `idempotentHint=false`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required because the tool can perform side effects after preview and approval.
- `database_gateway_apply_workflow`
  - Path: `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-support.yaml`
  - Current annotations: `readOnlyHint=false`, `destructiveHint=true`, `idempotentHint=false`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required because the tool can apply generated artifacts after preview and approval.
- `database_gateway_validate_workflow`
  - Path: `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-support.yaml`
  - Current annotations: `readOnlyHint=true`, `destructiveHint=false`, `idempotentHint=true`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required.
- `database_gateway_plan_encrypt_rule`
  - Path: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-encrypt.yaml`
  - Current annotations: `readOnlyHint=false`, `destructiveHint=false`, `idempotentHint=false`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required. Current values are acceptable if planning stores workflow state but does not apply runtime side effects.
- `database_gateway_plan_mask_rule`
  - Path: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-mask.yaml`
  - Current annotations: `readOnlyHint=false`, `destructiveHint=false`, `idempotentHint=false`, `openWorldHint=true`
  - Target policy: explicit production tool annotations required. Current values are acceptable if planning stores workflow state but does not apply runtime side effects.

## Required Follow-Up Checks

- Verify planner tools still mutate workflow/session state before keeping `readOnlyHint=false`.
- Verify every public production tool has all four boolean hint keys explicitly present in YAML.
- Verify no resource uses an empty `annotations: {}` map.
- Verify no tool uses empty `annotations: {}` in production descriptors.
