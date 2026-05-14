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

# Requirements Checklist: MCP Annotations Protocol Compliance

**Spec**: `.specify/specs/017-mcp-annotations-protocol-compliance/spec.md`
**Plan**: `.specify/specs/017-mcp-annotations-protocol-compliance/plan.md`
**Tasks**: `.specify/specs/017-mcp-annotations-protocol-compliance/tasks.md`
**Status**: Completed checklist

## Intake

- [x] User requested Speckit requirement management.
- [x] User explicitly forbade branch switching.
- [x] Current branch recorded as `001-shardingsphere-mcp`.
- [x] No branch-changing command or Speckit branch script was used during requirement intake.
- [x] Requirement intake started as documentation-only.
- [x] Implementation and verification are completed and recorded in `tasks.md`.

## Source Alignment

- [x] MCP Specification `2025-11-25` selected as protocol source baseline.
- [x] Source references recorded for `Annotations`, `Role`, `Resource`, `ResourceTemplate`, `Tool`, and `ToolAnnotations`.
- [x] Tool boolean defaults recorded from MCP schema.
- [x] Resource priority optionality recorded from MCP schema.
- [x] Descriptor policy is separated from MCP schema optionality.

## Requirement Quality

- [x] Resource and tool annotation models are explicitly non-mergeable.
- [x] Resource priority presence is called out as a non-negotiable invariant.
- [x] Primitive tool boolean defaults are specified.
- [x] Tool annotation-object presence is required separately from primitive boolean values.
- [x] Raw YAML explicit-key validation is required before primitive defaults are applied.
- [x] Empty annotation maps are required to fail descriptor validation.
- [x] Production public tool explicit hint policy is documented as ShardingSphere policy.
- [x] Test fixture exception for `MCPToolAnnotations.EMPTY` is documented.
- [x] Output-layer omission rules are documented.

## Acceptance Coverage

- [x] Acceptance scenarios cover absent resource annotations.
- [x] Acceptance scenarios cover explicit `priority: 0.0`.
- [x] Acceptance scenarios cover tool default values.
- [x] Acceptance scenarios cover missing production tool hints.
- [x] Acceptance scenarios cover invalid audience, invalid priority, invalid lastModified, and contradictory tool hints.
- [x] Acceptance scenarios cover annotation output field isolation.
- [x] Current production descriptor annotation inventory is recorded.

## Implementation Readiness

- [x] API model tasks identify exact package paths.
- [x] YAML DTO and raw validation tasks identify exact package paths.
- [x] Payload and SDK mapping tasks identify exact package paths.
- [x] Descriptor cleanup tasks identify production descriptor paths.
- [x] Verification tasks include scoped tests, Checkstyle, Spotless, static searches, and diff check.

## Completion State

- [x] API annotation model implementation is complete.
- [x] YAML DTO, raw validation, and semantic catalog validation implementation is complete.
- [x] Payload and SDK mapping implementation is complete.
- [x] Descriptor cleanup and README guidance are complete.
- [x] Scoped verification evidence is recorded in `tasks.md`.
- [x] Broader non-annotation MCP descriptor schema alignment is excluded from this package and tracked by `.specify/specs/018-mcp-descriptor-schema-compliance/`.
