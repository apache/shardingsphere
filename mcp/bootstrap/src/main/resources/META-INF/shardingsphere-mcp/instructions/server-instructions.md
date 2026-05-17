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

Apache ShardingSphere MCP.

Use official MCP list discovery methods (`tools/list`, `resources/list`, `resources/templates/list`, `prompts/list`) for the public surface.

Use `completion/complete` for supported argument values, and read `shardingsphere://capabilities` only as optional ShardingSphere domain catalog guidance.

Use `database_gateway_execute_query` only for read-only `SELECT` or `EXPLAIN ANALYZE`.
Use `database_gateway_execute_update` with `execution_mode=preview` before side effects.

Continue from `next_actions` or `recovery.next_actions` instead of guessing hidden tools or arguments.
