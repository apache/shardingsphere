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

# Architecture Evidence: MCP Encrypt/Mask Scoped Scorecard 100

## Boundary Decision

- Public MCP surface remains descriptor-driven: tools, resources, prompts, completions, and output schemas are declared in descriptor YAML and exposed by bootstrap factories.
- Feature ownership stays local: encrypt-specific descriptors, prompts, handlers, planning, and validation live under `mcp/features/encrypt`; mask-specific equivalents live under `mcp/features/mask`.
- Generic modules stay feature-agnostic: `mcp/api`, `mcp/support`, `mcp/core`, and `mcp/bootstrap` must not import `org.apache.shardingsphere.mcp.feature.encrypt` or `org.apache.shardingsphere.mcp.feature.mask`.
- Runtime packaging may depend on feature modules through Maven so the MCP runtime can discover encrypt/mask handlers. That dependency is a packaging boundary, not a Java source import boundary.

## API and Interface Design Check

- Planning tools expose reviewable workflow plans only; apply and validate remain shared workflow tools keyed by `plan_id`.
- Side effects are still explicit through `execution_mode` plus `approved_by_user`; no new implicit execution path was added.
- Model-facing response fields use canonical names: `resources_to_read`, `next_actions`, and `arguments`. Legacy alias-style `required_arguments` remains rejected.
- MCP SDK `1.1.2` and protocol `2025-11-25` stay fixed; no version negotiation or optional icon work is introduced for this scoped scorecard.

## Code Simplification Check

- No production extraction was made in `WorkflowToolResponseBuilder`: encrypt and mask payload builders share a shape, but the property roles and derived-column semantics differ enough that a generic abstraction would hide feature intent.
- `WorkflowGuidancePayloadBuilder` keeps the two scoped workflow-kind strings because it is the shared model-guidance bridge for resource hints. Extracting a new feature registry for only encrypt and mask would be broader than the current need.
- Descriptor validators remain the right place to reject stale aliases and non-goal descriptor keys because they protect the public descriptor boundary before runtime wiring.

## Guard Test

- `MCPArchitectureBoundaryTest#assertGenericModulesDoNotImportEncryptOrMaskFeatures` scans generic MCP production Java sources and fails if encrypt/mask feature packages leak upward.

## Closure Mapping

- T050: reviewed workflow payload construction; no production extraction was justified under the readability-first rule.
- T051: feature-to-core dependency direction is documented above and protected by `MCPArchitectureBoundaryTest`.
- T052: lightweight source boundary test added without a new architecture framework.
- T053: no dead compatibility shim was removed; stale public aliases are intentionally rejected by descriptor validation.
- T054: broader framework extraction rejected because local handler/service boundaries already express the scoped encrypt/mask responsibilities.
