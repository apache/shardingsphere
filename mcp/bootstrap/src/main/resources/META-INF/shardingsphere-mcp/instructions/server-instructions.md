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

Use MCP list methods (`tools/list`, `resources/list`, `resources/templates/list`, `prompts/list`) to discover the protocol surface.

Use `completion/complete` for supported argument values. Read `shardingsphere://guidance` when ShardingSphere domain capability guidance, workflow guidance, or side-effect notes are needed.

Use `database_gateway_execute_query` only for parser-approved `SELECT`.
Use `database_gateway_execute_explain_query` for execution-plan diagnostics.
Use `database_gateway_execute_update` with `execution_mode=preview` before side effects.
Treat `database_gateway_execute_update` preview as database-aware validation and classification, not as a database dry run.

Continue from top-level `next_actions` instead of guessing hidden tools or arguments.
