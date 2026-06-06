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

Plan a ShardingSphere encrypt rule workflow.

User context:
- database: {{database}}
- schema: {{schema}}
- table: {{table}}
- column: {{column}}
- algorithm_type: {{algorithm_type}}
- plan_id: {{plan_id}}

Model path:
1. Collect database, table, and column values from user context or explicit user input before calling database_gateway_plan_encrypt_rule; schema is optional.
2. Read shardingsphere://features/encrypt/algorithms before choosing algorithm_type.
3. Read existing encrypt rules for the database or table when database and table are known.
4. Call database_gateway_plan_encrypt_rule with gathered logical names, explicit rule column names, and reviewed algorithm choices.
5. Use database_gateway_apply_workflow with execution_mode=preview before applying generated encrypt rule DistSQL.
6. Before choosing uncertain database, schema, table, column, algorithm, or plan_id values, ask the user or read feature algorithm/rule resources whose URI can be built from already-known identifiers; do not guess identifiers.

Ask-user conditions:
- Ask when decrypt, equality, LIKE, or sensitive-field semantics are unclear.
- Ask before applying generated encrypt rule DistSQL that changes runtime state.

Stop conditions:
- Stop after database_gateway_plan_encrypt_rule returns a planned workflow with plan_id and reviewable artifacts.
- Stop after a clarifying response lists missing inputs instead of guessing algorithm or column choices.

Final answer rule:
- Summarize confirmed facts, the selected MCP path, and any required next user action.
