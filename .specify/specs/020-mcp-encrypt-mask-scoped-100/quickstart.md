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

# Quickstart: Encrypt and Mask MCP Workflows

## Preconditions

- Use MCP protocol `2025-11-25` and MCP Java SDK `1.1.2`.
- Connect the runtime to ShardingSphere Proxy logical metadata.
- Keep the score scope to encrypt and mask workflows only.
- Treat `plan_id` as a current-session handle. Do not reuse it across MCP sessions.

## Discover

1. Call `tools/list` and read the input/output schemas before choosing a tool.
2. Call `resources/templates/list` before constructing resource URIs.
3. Read `shardingsphere://databases` and then the concrete database/table resources needed for the workflow.
4. Read `shardingsphere://features/encrypt/algorithms` or `shardingsphere://features/mask/algorithms` before choosing an algorithm.

## Plan Encrypt

Call `database_gateway_plan_encrypt_rule` with the logical database, schema, table, column, algorithm choice, and any user-provided options.

Minimum review payload to expect:

- `plan_id`
- `resources_to_read`
- `next_actions`
- `derived_column_plan`
- `masked_property_preview`
- DDL, DistSQL, index, and validation artifacts when applicable

Stop after planning unless the user has reviewed the returned artifacts.

## Plan Mask

Call `database_gateway_plan_mask_rule` with the logical database, schema, table, column, algorithm choice, and field semantics.

Minimum review payload to expect:

- `plan_id`
- `resources_to_read`
- `next_actions`
- `masked_property_preview`
- DistSQL and validation artifacts

Stop after planning unless the user has reviewed the returned artifacts.

## Preview

Call `database_gateway_apply_workflow` with:

```json
{
  "plan_id": "plan-...",
  "execution_mode": "preview"
}
```

Preview must not apply DDL, DistSQL, or runtime metadata changes. Use it to explain side effects and gather approval.

## Approve and Apply

Only after explicit user approval, call:

```json
{
  "plan_id": "plan-...",
  "execution_mode": "review-then-execute",
  "approved_by_user": true
}
```

Use `approved_steps` only when intentionally recovering from a partial or skipped artifact path.

## Validate

After approved execution or manual artifact execution, call `database_gateway_validate_workflow` with the current-session `plan_id`.

Report success only when validation layers match current runtime metadata. If validation fails, report the failed layer and follow `next_actions`.

## Recover

Use the `recover_workflow` prompt or the structured recovery payload when:

- `plan_id` is missing or stale.
- Metadata changed after planning.
- Required algorithm properties or field semantics were missing.
- Manual-only artifacts were exported but not yet executed.

Recovery should re-plan or validate through MCP; it must not invent algorithms, forge approval, or skip preview.
