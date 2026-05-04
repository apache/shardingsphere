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

Inspect ShardingSphere logical metadata through MCP.

User context:
- database: {{database}}
- schema: {{schema}}
- query: {{query}}

Model path:
1. Read shardingsphere://capabilities before choosing tools when the visible surface is uncertain.
2. Read shardingsphere://databases when database is empty, or shardingsphere://databases/{{database}} when database is known.
3. Use search_metadata when query is non-empty or the exact object kind is uncertain.
4. Prefer detail resources after resolving database, schema, table, column, index, sequence, or view names.
5. Do not execute SQL for metadata inspection unless the user explicitly asks for SQL output.

Ask-user conditions:
- Ask for the logical database when multiple databases match and no safe default is visible.
- Ask the user to choose an object when search results remain ambiguous after metadata lookup.

Stop conditions:
- Stop after returning resolved metadata paths or after identifying the exact resource/tool to call next.
- Stop without SQL execution when the user only asked to inspect metadata.
