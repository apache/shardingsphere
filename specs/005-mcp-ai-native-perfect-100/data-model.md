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

# Data Model: MCP AI-Native Perfect 100

## Response Envelope

Represents a public MCP response contract.

Fields:

- `response_mode`: one of the allowed modes in `MCP100-P0-01`.
- `summary`: concise human-readable summary.
- `data`: mode-specific payload.
- `next_actions`: ordered list of follow-up actions.
- `recovery`: optional recovery detail for validation, not-found, ambiguous, or empty states.
- `redaction_summary`: optional secret-safe list of redacted categories.
- `request_id`: optional correlation id.

Rules:

- `response_mode` is always present or explicitly documented for static descriptors.
- `next_actions` is empty only when the response is terminal.
- Sensitive values never appear in `summary`, `data`, `next_actions`, or `recovery`.

## Next Action

Represents one safe follow-up.

Fields:

- `order`: integer ordering key.
- `type`: `tool_call`, `resource_read`, `ask_user`, `completion`, `terminal`, `manual_step`, or `documentation`.
- `title`: short action name.
- `requires_user_approval`: boolean.
- `depends_on`: optional prior action order or id.
- `tool_name`: optional MCP tool name.
- `arguments`: optional prefilled tool arguments.
- `resource_uri`: optional MCP resource URI.
- `question`: optional user-facing question.

Rules:

- Read-only tool and resource actions set `requires_user_approval=false`.
- Mutating or external-effect actions set `requires_user_approval=true`.
- `ask_user` actions include a clear question and no guessed value.
- `completion` actions include the reference type, reference, argument name, and context arguments.
- Legacy aliases such as `action_kind`, `target_tool`, `target_resource`, and `required_arguments` may remain for compatibility.

## Continuation State

Represents how a capped list can be continued.

Fields:

- `is_capped`: boolean.
- `has_more`: boolean.
- `continuation_mode`: `none`, `pagination`, or `search_metadata`.
- `next_page_arguments`: optional arguments for real pagination.
- `search_arguments`: optional arguments for narrowing or searching.
- `large_result_guidance`: optional concise guidance.

Rules:

- `pagination` requires `next_page_arguments`.
- `search_metadata` requires `search_arguments`.
- `has_more=true` cannot appear with `continuation_mode=none`.

## Recovery Detail

Represents why the current call did not produce the requested result.

Fields:

- `recovery_category`: `not_found`, `ambiguous`, `empty_scope`, `missing_context`, `validation`, or `terminal`.
- `category`: optional stable detailed category for backwards-compatible clients.
- `requested_token`: optional token from the user request.
- `parent_resource_uri`: optional nearest safe resource.
- `retry_arguments`: optional retry-ready arguments.
- `message`: concise explanation.

Rules:

- The server must not emit a child URI for an unresolved object.
- If `requested_token` exists and search is possible, `retry_arguments.query` uses it.
- `parent_resource_uri` is included only when enough context is known.

## Argument Provenance

Represents the origin of a planned workflow argument.

Fields:

- `name`: argument name.
- `value`: value or redacted marker.
- `provenance`: `user_provided`, `inferred_from_intent`, `server_defaulted`, `server_generated`, or `redacted`.
- `confidence`: optional `high`, `medium`, or `low`.
- `reason`: optional concise explanation.

Rules:

- Ambiguous inferred values are not emitted as selected values.
- Redacted values keep enough type context for the model to continue safely.

## Runtime Visibility

Represents secret-free runtime status for MCP use.

Fields:

- `server_status`: `ready`, `degraded`, or `unavailable`.
- `transport`: `stdio`, `streamable_http`, or `unknown`.
- `database`: optional logical database name.
- `metadata_visibility`: `ready`, `unsupported`, `unconfigured`, or `unavailable`.
- `capability_visibility`: `ready`, `unsupported`, `unconfigured`, or `unavailable`.
- `feature_visibility`: `ready`, `unsupported`, `unconfigured`, or `unavailable`.
- `driver_category`: optional database driver family.
- `redaction_summary`: optional redacted categories.

Rules:

- Runtime visibility is diagnostic only.
- It never exposes raw credentials, registry secrets, or connection strings with secrets.

## Scorecard Entry

Represents evidence for one `100` gate.

Fields:

- `requirement_id`: requirement id from `requirements.md`.
- `status`: `passing`, `blocked`, or `not_implemented`.
- `evidence`: command, test, lint, or documented inspection.
- `notes`: concise context.

Rules:

- Any non-`passing` required entry makes the total score lower than `100`.
- Non-goals are listed separately and do not affect the score.
