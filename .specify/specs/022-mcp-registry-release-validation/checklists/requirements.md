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

# Requirements Checklist: MCP Registry Release Validation

## Completeness

- [x] Branch switching is explicitly forbidden.
- [x] Static Dockerfile validation scope is defined.
- [x] Dynamic image inspection is explicitly out of scope.
- [x] MCP Registry ownership metadata requirements are mapped.
- [x] OCI image version metadata requirements are mapped.
- [x] Workflow integration requirements are mapped.
- [x] Test coverage requirements are mapped to concrete failure paths.

## Quality Gates

- [x] `MCPRegistryMetadataCommandTest` passes.
- [x] `mcp/registry` Spotless passes.
- [x] `mcp/registry` Checkstyle passes.
- [x] Real `mcp/server.json` and `distribution/mcp/Dockerfile` static validation passes.
- [x] mcp-builder review is completed.
- [x] Doubt-driven review is completed.
