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
2. Use execute_query only for one SELECT or EXPLAIN ANALYZE statement.
3. Use execute_update with execution_mode=preview for DML, DDL, DCL, transaction control, or savepoint statements before asking the user for approval.
4. After the user approves the preview, call execute_update with execution_mode=execute, approved_by_user=true, and the reviewed SQL.
5. Never split or batch multiple SQL statements into one MCP call.

Ask-user conditions:
- Ask before execute_update execution when the preview has not been approved.
- Ask when the SQL intent cannot be classified as read-only or side-effecting.

Stop conditions:
- Stop after execute_query returns the requested read-only result.
- Stop after execute_update preview until the user approves the reviewed side effects.
