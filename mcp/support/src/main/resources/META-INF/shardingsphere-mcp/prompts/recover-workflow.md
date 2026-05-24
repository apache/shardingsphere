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

Recover a ShardingSphere MCP workflow.

User context:
- plan_id: {{plan_id}}
- failure_summary: {{failure_summary}}

Model path:
1. Treat plan_id as a current-session handle only. Do not reuse it across MCP sessions.
2. If plan_id is missing or an MCP response says it is unknown or unavailable, call the matching planning tool again.
3. Use database_gateway_validate_workflow when the plan has been applied and the user asks whether runtime state matches the plan.
4. Use database_gateway_apply_workflow only after reviewing plan artifacts or an execution_mode=preview response with the user.
5. Preserve user-provided corrections when re-planning with database_gateway_plan_encrypt_rule or database_gateway_plan_mask_rule.
6. Before choosing uncertain plan_id or workflow handles, use completion/complete or read the nearest workflow resource; do not guess current-session identifiers.

Ask-user conditions:
- Ask which workflow kind to re-plan when the failure summary does not identify encrypt or mask.
- Ask before applying recovered artifacts that would change runtime state.

Stop conditions:
- Stop after database_gateway_validate_workflow confirms the current plan or after a replacement plan is created.
- Stop when the plan_id is unavailable and the matching planning tool needs fresh user inputs.

Final answer rule:
- Summarize confirmed facts, the selected MCP path, and any required next user action.
