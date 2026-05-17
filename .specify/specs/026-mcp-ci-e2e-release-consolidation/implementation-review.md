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

# Implementation Review: MCP CI E2E And Release Consolidation

## API And Boundary Review

- Workflow boundary: JDK 21 CI owns Java 21 MCP build/runtime/distribution behavior; Required Check remains the owner for Checkstyle, Spotless, and RAT.
- Runtime test boundary: `ProductionMySQLRuntimeE2ETest` owns MySQL HTTP/STDIO production runtime behavior; `PackagedDistributionE2ETest` owns packaged and container distribution behavior.
- Distribution boundary: `distribution/mcp` packages runtime drivers needed by the default H2 demo config and the MySQL-backed E2E config.
- Release boundary: `.github/workflows/mcp-build.yml` validates local build before push and validates the pushed GHCR image by digest after push.

## MCP Builder Review

- Tool and resource usefulness: the merged distribution E2E calls real MCP tools and resources, including metadata search, SQL query, runtime diagnostics, capabilities, plugin tool, and plugin resource.
- Transport coverage: packaged HTTP, packaged STDIO, container HTTP, and container STDIO are all covered in one suite.
- Configuration clarity: container tests mount an explicit generated runtime YAML and use `host.docker.internal` only for the Dockerized MCP runtime to reach the MySQL Testcontainers host port.
- Operational evidence: release workflow now records version, tags, digest, and MCP server identifier, and separates native runtime validation from arm64 manifest validation.
- Security posture: runtime diagnostics assertions continue to reject raw JDBC URLs, bearer tokens, and stack traces in public runtime output.

## Simplification Review

- Removed standalone smoke workflow and smoke-only workflow selectors.
- Removed smoke-only LLM and distribution classes after their topology moved into complete suites.
- Renamed the remaining H2 runtime base class and LLM success message so disabled/non-production helpers no longer carry smoke wording.
- Moved H2 access-mode mapping out of the generic `RuntimeTransport` enum and into `H2RuntimeTestSupport`, keeping transport as a transport-only boundary.
- Scoped optional H2 STDIO enablement behind both H2 and STDIO flags, so the MySQL STDIO lane cannot accidentally activate H2 tests.
- Collapsed distribution validation into `PackagedDistributionE2ETest` instead of keeping separate package and container smoke classes.
- Reused one MySQL container across the read-only distribution suite to reduce runtime without changing assertions.
- Kept H2 demo config and disabled H2 production E2E defaults; deeper deletion of optional H2-specific local tests is not part of this safe cleanup because those tests cover H2-specific sequence and metadata semantics not replaced by MySQL.

## Remaining Coverage Boundaries

- linux/arm64 release runtime is not executed locally or in CI; it is validated by manifest inspection only, matching the accepted design.
- LLM usability full model execution is workflow-owned and was not run locally in this implementation review because it requires the LLM runtime image/model lane.
- Required Check coverage is not duplicated in JDK 21 CI; local scoped Checkstyle/Spotless was run for touched modules.

## Final Self-Question Loop

1. Is there more cleanup that is safe without reducing coverage? No; remaining H2-specific tests are disabled as production CI evidence but still cover local H2 semantics.
2. Is there a simpler abstraction boundary available now? No; the current split keeps workflow, runtime fixture, distribution, and release concerns separate.
3. Is any smoke-only workflow selector left? No; static search found no MCP smoke-only selector.
4. Is any H2 default still presenting itself as production E2E evidence? No; the production H2 flag defaults to false and workflows no longer invoke it.
5. Is the implementation acceptable under MCP builder review? Yes; it validates real MCP tool/resource behavior over both transports and both artifact forms.
6. Is the implementation acceptable under code-review-and-quality review? Yes; scoped checks, package build, Docker build, and MySQL-backed E2E passed.
