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

Choose the safest MCP SQL path.

User context:
- database: {{database}}
- schema: {{schema}}
- sql_intent: {{sql_intent}}

Model path:
1. Read shardingsphere://databases/{{database}}/capabilities before execution when statement support is uncertain.
2. Use database_gateway_execute_query only for one classifier-approved SELECT or EXPLAIN ANALYZE statement.
3. Use database_gateway_execute_update with execution_mode=preview for DML, DDL, DCL, transaction control, savepoint, or side-effecting EXPLAIN ANALYZE statements before execution.
   Treat preview as classification-only, not as a database dry run.
4. After reviewing the preview, call database_gateway_execute_update with execution_mode=execute and the reviewed SQL only when execution is still intended.
5. Never split or batch multiple SQL statements into one MCP call.
6. Before choosing uncertain database, schema, table, or column names, use completion/complete or read the nearest MCP resource; do not guess identifiers.

Ask-user conditions:
- Ask before database_gateway_execute_update execution when the previewed side effects are ambiguous.
- Ask when the SQL intent cannot be classified as classifier-approved query SQL or side-effecting SQL.

Stop conditions:
- Stop after database_gateway_execute_query returns the requested query result.
- Stop after database_gateway_execute_update preview unless execution is still intended.

Final answer rule:
- Summarize confirmed facts, the selected MCP path, and any required next user action.
