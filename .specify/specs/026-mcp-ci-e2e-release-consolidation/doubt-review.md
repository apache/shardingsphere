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

# Doubt Review: MCP CI E2E And Release Consolidation

This review records the adversarial checks used before freezing this design package. It is a design review only; no implementation has been written.

## Claim 1 - Smoke Can Be Removed From E2E

**Claim**: Smoke-only E2E targets can be removed if complete MySQL HTTP/STDIO suites absorb the topology coverage first.

**Why it matters**: Removing smoke too early would reduce coverage even if the final design is cleaner.

**Adversarial findings**:

- Valid and actionable: the design must require a topology map before removing smoke. Added as inventory tasks T006-T008.
- Valid and actionable: LLM smoke cannot disappear until usability covers MySQL HTTP and MySQL STDIO. Added as T016-T018.
- Valid trade-off: smoke-named classes may remain temporarily during migration, but they must not remain final workflow targets.

## Claim 2 - H2 Should Leave Real E2E

**Claim**: H2-backed production E2E should not be used as real MCP E2E evidence.

**Why it matters**: H2 can pass while MySQL protocol, driver, container, and dialect behavior fail.

**Adversarial findings**:

- Valid and actionable: avoid wording that deletes all H2 assets. H2 may stay as unit/lightweight integration or demo config if not used as production E2E evidence.
- Valid and actionable: distribution E2E must inject or use MySQL-backed runtime data instead of relying on default H2 demo config.

## Claim 3 - Distribution E2E And Release Workflow Are Related But Different

**Claim**: Distribution E2E validates local artifacts; release workflow validates published artifacts.

**Why it matters**: Treating local distribution E2E as release validation would miss broken GHCR tags, digests, manifests, or registry publication.

**Adversarial findings**:

- Valid and actionable: any shared helper must take an explicit artifact reference so a local package cannot accidentally stand in for a published image.
- Valid and actionable: release validation must pull the pushed image by tag or digest and inspect multi-platform manifest contents.
- Valid and actionable: MCP Publisher download must be pinned or integrity-checked.

## Claim 4 - JDK 21 CI Should Not Add Duplicate Style Gates

**Claim**: JDK 21 CI should focus on Java 21 build/runtime behavior and not duplicate Required Check style/license gates.

**Why it matters**: Duplicate gates increase CI cost without adding coverage when Required Check is repo-wide.

**Adversarial findings**:

- Valid and actionable: re-verify Required Check coverage before implementation, because the decision depends on the current workflow and reactor shape.
- Valid trade-off: local scoped Checkstyle/Spotless remains useful for developer verification, even if JDK 21 CI does not add duplicate workflow jobs.

## Claim 5 - MCP Builder Perspective

**Claim**: The design is reasonable for MCP because it validates both standard transports and validates the packaged server as users will run it.

**Why it matters**: MCP server quality depends on tool/resource behavior being reachable through real client transports, not just class-level tests.

**MCP-builder findings**:

- Valid and actionable: transport coverage must include stdio and Streamable HTTP because they serve different deployment models.
- Valid and actionable: packaged distribution tests must exercise plugin discovery, not only startup.
- Valid and actionable: published image validation must run an MCP interaction, not only `docker pull`.
- Valid trade-off: this spec does not design new MCP tools/resources; it validates existing server packaging and transport delivery.

## Open Questions

No open confirmation questions remain for the design package after the user confirmed that a new non-conflicting Spec Kit package should be created.

## Reanalysis Loop

The follow-up reanalysis found three refinements worth recording:

- Published image runtime validation should be scoped to the native runner platform; arm64 should be verified by manifest inspection unless QEMU runtime validation is explicitly accepted later.
- H2 defaults such as `mcp.e2e.production.h2.enabled=true` must be handled during implementation so H2 is not still presented as production E2E evidence.
- The final LLM target must remove any standalone smoke workflow entry after usability becomes a topology superset; a second workflow is acceptable only if it runs a complete non-smoke target for a documented scheduling or resource reason.
