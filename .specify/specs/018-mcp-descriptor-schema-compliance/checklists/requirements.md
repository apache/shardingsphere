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

# Requirements Checklist: MCP Descriptor Schema Compliance

**Spec**: `.specify/specs/018-mcp-descriptor-schema-compliance/spec.md`
**Plan**: `.specify/specs/018-mcp-descriptor-schema-compliance/plan.md`
**Tasks**: `.specify/specs/018-mcp-descriptor-schema-compliance/tasks.md`
**Status**: Draft checklist

## Intake

- [x] Follow-up scope is separated from package 017.
- [x] User explicitly forbade branch switching in the MCP Speckit workflow.
- [x] Current branch remains `001-shardingsphere-mcp`.
- [x] No branch-changing command or Speckit branch script was used during requirement intake.

## Source Alignment

- [x] MCP Specification `2025-11-25` selected as protocol source baseline.
- [x] Source references recorded for `Resource`, `ResourceTemplate`, `Tool`, `ToolExecution`, `Icon`, and `_meta`.
- [x] Package 017 annotation source facts are treated as completed and out of scope.
- [x] MCP Java SDK support is called out as an implementation gate.

## Requirement Quality

- [x] Official MCP descriptor fields and ShardingSphere descriptor-only metadata are separated.
- [x] API/support/bootstrap module boundaries are explicit.
- [x] Optional-field omission behavior is required.
- [x] Stricter descriptor validation is documented as ShardingSphere policy.
- [x] Existing descriptors that omit new fields must keep behavior unchanged.

## Acceptance Coverage

- [x] Acceptance scenarios cover valid resource official fields.
- [x] Acceptance scenarios cover valid tool official fields.
- [x] Acceptance scenarios cover omitted-field behavior.
- [x] Acceptance scenarios cover invalid `taskSupport`.
- [x] Acceptance scenarios cover metadata boundary isolation.

## Implementation Readiness

- [x] API model tasks identify exact package paths.
- [x] YAML DTO and raw validation tasks identify exact package paths.
- [x] Payload and SDK mapping tasks identify exact package paths.
- [x] Verification tasks include scoped tests, Checkstyle, Spotless, static searches, and diff check.

## Open Items

- [ ] Implementation has not started for package 018.
- [ ] MCP Java SDK `1.1.2` field support must be verified before API changes.
- [ ] Current raw YAML validator gaps must be inventoried before edits.
