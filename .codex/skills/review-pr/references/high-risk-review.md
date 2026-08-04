<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# High-Risk Review

Read the sections selected by the main Skill's mandatory risk triage. More than
one section may apply to a behavior cluster; do not stop after the first
recognized risk. Complete the main Skill's Completion Gate after applying the
triggered guidance.

## Shared Ownership and Implicit State

Apply when shared modules, public APIs or SPIs, session or executor state,
connector state, registries, caches, lifecycle handles, constructors, or
metadata owners change.

- Keep dialect, protocol, and feature-specific behavior in its owning module.
  Shared code must not acquire target names, protocol identifiers, type-name
  string checks, or target lifecycle comments without a verified shared
  contract.
- Inspect nullable construction, partial initialization, hidden modes, boolean
  switches, magic values, empty sentinels, no-op implementations, and temporal
  side effects.
- Check create, repeated-use, cleanup, release, free, error, cancellation,
  disabled-feature, and stale-state paths.
- Verify generic shared behavior separately from target activation.
- Treat ownership or implicit-state concerns as blockers only when a reachable
  contract violation is proven; prefer explicit state only when it is the
  minimum necessary fix.

## Authoritative Validation Boundary

Apply when a finding proposes new validation.

- Identify where untrusted input first becomes trusted: YAML validator, CLI
  parser, request binder, SPI loader, protocol decoder, SQL parser, config
  center loader, or another external boundary.
- Do not require duplicate validation downstream when every production path
  already passes through an authoritative boundary.
- Require downstream validation only for a verified bypass path, public or
  shared API exposure, untrusted deserialization, asynchronous ownership,
  persisted state, or an invariant owned by the downstream type.
- Prefer propagation tests from the boundary to runtime behavior over repeated
  defensive checks in internal holders or DTOs.

## Test and Generated Artifact Claims

Before claiming missing coverage:

- Trace the complete test entry, fixture setup, helper calls, target invocation,
  and assertions.
- Distinguish production-path validation from fixture-injected, reflected,
  mocked, or collaborator-owned behavior.
- Check existing focused, integration, E2E, native, and client coverage that
  actually reaches the changed path.
- Require a new test only for a realistic regression owned by this PR.

For native-image or generated metadata, first classify reflection,
`ServiceLoader`, resource include, proxy, JNI, serialization, generator output,
or tracing-agent noise. Check the production access path, source of truth,
automatic features, disabled flags, and focus-required native verification.
Block only when a reachable current-PR path is otherwise uncovered.

## Performance and Concurrency

Apply when the change affects a high-frequency path, shared cache, lock,
allocation, I/O, or concurrent state.

- Establish the runtime and supported version before relying on implementation
  details of the JDK, driver, database, or library.
- Check call frequency, contention, allocation, blocking behavior, cleanup,
  memory growth, and worst-case input size.
- Treat `ConcurrentHashMap#computeIfAbsent` and similar APIs as evidence prompts,
  not unconditional blockers. Require a preceding `get` fast path only when
  version-aligned measurements or source evidence show that the changed hot path
  needs it and the extra lookup preserves semantics.
- Prefer benchmarks, profiles, source evidence, or an existing verified
  repository rule over performance intuition.

## User and Operational Impact

Require documentation or release notes only when users need information for
upgrade, migration, configuration, compatibility, troubleshooting, rollback,
or release awareness. Internal refactors, test-only changes, and implementation
details do not need low-signal release entries.

When behavior, configuration, diagnostics, or persisted state changes, inspect:

- Backward compatibility and supported database or dialect versions.
- Upgrade, downgrade, migration, and rollback behavior.
- Errors and logs for accuracy, actionability, and sensitive-data exposure.
- Standalone and cluster configuration consistency where applicable.
- Staged rollout or operational guardrails when runtime behavior changes.

## Dependencies and Distribution

Apply when dependency, plugin, packaging, native-image, distribution, LICENSE,
NOTICE, generated resource, or release artifact files change.

- Verify source, version alignment, compatibility, security, license, packaging,
  and runtime inclusion.
- Inspect the consuming distribution, not only the declaring module.
- Confirm generated artifacts and sources of truth remain synchronized.
- Treat passing compilation as supporting evidence, not proof of packaging or
  runtime availability.

## Style and Non-Behavioral Churn

Use repository-declared formatters and style gates as authority. For
ShardingSphere, Spotless and Checkstyle decide ordinary formatting.

- Include GitHub-listed import-only, whitespace-only, and formatter-only files
  in reviewed scope.
- Do not report formatter-stable whitespace as a blocker.
- Do not run generic whitespace diagnostics routinely.
- Report non-behavioral churn only when it hides behavior, fails repository
  gates, touches broad unrelated areas, or violates an explicit scope contract.
