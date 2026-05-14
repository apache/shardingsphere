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

# Data Model: MCP Scorecard Perfect 100

## Score Dimension

Represents one independently scored capability area.

- `id`: Stable identifier such as `production.code-readability` or `e2e.stability`.
- `module_group`: `production` or `e2e`.
- `name`: Human-readable dimension name.
- `current_score`: Latest baseline score.
- `target_score`: Always `100`.
- `status`: `open`, `evidence-needed`, or `met`.
- `gap_refs`: One or more score gaps.
- `exit_gate_refs`: One or more exit gates.

Validation rules:

- `target_score` MUST be `100`.
- `status` MUST NOT be `met` while any linked gap is open.
- Open risks keep the score status open.

## Score Gap

Represents why a dimension is below 100.

- `id`: Stable gap identifier.
- `dimension_id`: Owning score dimension.
- `description`: Specific reason from the latest review.
- `source_evidence`: File path, review note, or observed command result.
- `severity`: `P0`, `P1`, or `P2`.
- `closure_task_refs`: Tasks that close the gap.

Validation rules:

- Every baseline score below 100 MUST have at least one score gap.
- A gap cannot be closed by explanation only.

## Exit Gate

Represents the proof needed before a dimension reaches 100.

- `id`: Stable gate identifier.
- `dimension_id`: Owning score dimension.
- `gate_type`: `command`, `artifact`, `contract`, or `review`.
- `required_evidence`: Exact command, artifact path, snapshot, or review record.
- `pass_condition`: Objective condition for passing.

Validation rules:

- Every dimension MUST have at least one exit gate.
- A command gate MUST record exit code.
- An artifact gate MUST record path and content-level pass condition.

## Evidence Record

Represents a completed verification event.

- `gate_id`: Linked exit gate.
- `timestamp`: Verification time.
- `command_or_artifact`: Command string, artifact path, or contract snapshot.
- `exit_code`: Required for command evidence.
- `result`: `pass` or `fail`.
- `notes`: Concise context and residual risks.

Validation rules:

- A dimension can reach `met` only if every required gate has passing evidence.
- Failed evidence keeps the dimension open.

## Standard Source Mapping

Represents an official MCP source requirement that controls one or more score dimensions.

- `source_url`: Official MCP specification, security guide, or verified SDK documentation URL.
- `protocol_area`: Lifecycle, transport, authorization, tool, resource, prompt, completion, pagination, error handling, or security.
- `implementation_scope`: Production module, E2E module, distribution runtime, or documentation surface that must obey this source.
- `evidence_records`: Evidence IDs proving that the implementation follows the source.
- `sdk_version_constraint`: Detected SDK version when the mapping depends on Java SDK APIs.

Validation rules:

- A standard source mapping MUST use official MCP sources or verified local SDK `1.1.2` source.
- A mapping MUST NOT be satisfied by ShardingSphere-only convention.
- Missing mapping evidence keeps related active standard-first dimensions below `100/100`.

## Open Risk

Represents a missing or deferred proof point.

- `dimension_id`: Linked dimension.
- `reason`: Why evidence is missing.
- `owner`: Person or role responsible for resolving the risk.
- `resolution_condition`: Evidence needed to close the risk.

Validation rules:

- Open risks MUST be dimension-specific.
- A dimension with any open risk MUST remain below 100.
- Waivers and exception records are not allowed.
- If the user wants to remove a dimension, that requires an explicit spec update.
