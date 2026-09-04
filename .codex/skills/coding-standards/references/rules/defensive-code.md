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

# Defensive-Code Rules

Defensive code is a guard, repeated validation, copy, wrapper, fallback, default value, exception catch, retry, concurrency mechanism, lifecycle restriction, or mutation restriction added to prevent misuse or failure rather than implement requested production behavior.

## Required Evidence

- A defensive construct is compliant only when qualifying evidence proves that the defended condition occurs on a supported production path or an explicit existing contract requires the defense.
- Apply this gate to every defensive construct that the current task adds, extends, or strengthens; touching nearby code does not authorize changing a pre-existing defensive construct that the task does not alter.
- Do not add, extend, or strengthen defensive code unless qualifying evidence proves that the defended condition occurs on a supported production path or an explicit existing contract requires the defense.
- Qualifying evidence is limited to user-required behavior, an explicit public, internal, SPI, framework, serialization, concurrency, or lifecycle contract, a maintained production consumer, or an observed failure on a supported path.
- A mutable type, theoretical misuse, a possible future caller, general safety, best practice, consistency with nearby code, hypothetical reuse, and test convenience are not qualifying evidence.
- A test cannot establish the production condition or contract used to justify defensive code by itself; it may only corroborate qualifying production or contract evidence.
- Before adding or keeping defensive code, record in the task analysis or review the exact defended condition, the supported producer, consumer, or boundary that can trigger it, the behavior or contract being protected, and the qualifying evidence source.
- When qualifying evidence does not exist, omit or remove defensive code added by the current task, undo only the task-introduced extension or strengthening of a pre-existing defensive construct, preserve its pre-task behavior, and remove every task-introduced test whose only purpose is to preserve the unsupported defense.
- In Standalone Compliance Audit Mode, inspect all accessible contracts and supported producers, consumers, and paths before reporting an existing defensive construct as non-compliant.
- Failure to find qualifying evidence does not prove that an existing defensive construct is unnecessary when an outcome-sensitive external contract or supported path remains unavailable.
- Mark the affected standalone check as blocked when unavailable evidence could change its conclusion; report a violation only when the completed evidence proves that no qualifying condition applies.

## Null, Empty, Range, Type, and State Validation

- Do not add or retain a null guard, `Objects.requireNonNull`, `Optional.ofNullable`, null fallback, or null-triggered early return when supported callers and upstream contracts already guarantee a non-null value and no explicit existing boundary or diagnostic contract requires local validation.
- Do not repeat empty collection, empty string, range, type, capability, or state checks whose invariants are guaranteed by supported callers, parsers, constructors, frameworks, or state machines unless an explicit existing boundary or diagnostic contract requires local validation.
- Add runtime validation only when the defensive-code evidence gate proves that it is required at a real external, public, persisted, parsed, SPI, reflection, shared-state, or asynchronous boundary to preserve an existing contract or provide a concrete diagnostic for a condition that can occur there. Do not recheck invariants guaranteed by callers or upstream contracts.
- Prefer `ShardingSpherePreconditions` with lazy exception suppliers when the module can use it and the resulting control flow preserves the required exception type, message, timing, and cause.
- Keep a manual throw when the module cannot depend on `infra/exception`, the code is inside `ShardingSpherePreconditions`, a caller-facing exception contract requires it, or a precondition wrapper would obscure necessary control flow. Do not replace manual throws mechanically, and record the concrete reason for keeping one.

## Collection Copies and Mutation Restrictions

- Before adding a collection copy or unmodifiable wrapper, inspect current production producers and consumers, alias ownership, asynchronous or concurrent lifetime, and applicable public, SPI, framework, serialization, immutability, snapshot, and isolation contracts.
- `List.copyOf`, `Map.copyOf`, `Set.copyOf`, `Collections.unmodifiable*`, collection constructors that copy another collection, and equivalent copies or wrappers are allowed only when the inspected evidence proves at least one of the following allowed cases.
  - An explicit existing contract requires immutable or snapshot semantics.
  - A supported producer or consumer currently mutates an aliased collection after handoff, and that mutation would violate behavior owned by the receiving object.
  - A supported path exposes owned mutable state, and an existing contract forbids the consumer from mutating it.
  - A supported asynchronous or concurrent path requires a stable snapshot.
- A mutable collection implementation, or the theoretical ability of a caller to mutate it, is not evidence.
- When inspection finds no supported mutation path and no explicit contract, pass, store, or return the original collection directly; remove every task-introduced copy or wrapper and every task-introduced test that only asserts immutability or rejected mutation.
- When a copy or wrapper is allowed, verify that its null handling, iteration order, duplicate semantics, aliasing, mutation visibility, exception behavior, and time and space cost preserve the required behavior.

## Fallbacks and Default Values

- Do not convert a contract-violating null, empty value, unknown state, or impossible branch into an empty collection, empty object, zero, false, default enum, or other fallback merely to keep execution running.
- Do not add a fallback that hides an upstream contract violation or changes a defined failure into apparent success.
- Keep a fallback only when qualifying evidence proves that the triggering condition can occur on a supported path and an existing contract defines the fallback result.

## Exception Handling and Retries

- Do not catch and ignore an exception, log and continue, or return a fallback from a catch block unless an existing recovery contract defines that behavior for the caught condition.
- Do not broaden a catch to `Exception` or `Throwable` merely to prevent an unexpected failure from escaping.
- Add a retry only when qualifying evidence identifies a supported transient failure and the operation's idempotency or duplicate-effect contract permits another attempt.
- A retry must have an existing bound, termination condition, and timing policy; the theoretical possibility of a network or storage failure is not sufficient evidence.
- When an edit removes the last checked-exception source from a private or internal method, remove the stale `throws` declaration and update callers. Keep checked exceptions on public or overridden methods only when a caller-facing, framework, external API, or compatibility contract requires them. Never widen to generic `Exception` or `Throwable`.

## Concurrency and Lifecycle Protection

- Do not add locks, `volatile`, concurrent collections, snapshots, atomic state, or synchronization unless qualifying evidence proves that the protected state is accessed across supported concurrent paths.
- Do not add initialized, closed, executed, or similar state flags to suppress repeated operations when a supported lifecycle contract already guarantees the operation occurs once.
- Keep concurrency or lifecycle protection only when the protected transition, competing path, and required behavior are identified by qualifying evidence.

## Semantic Verification

- Treat text searches and regular expressions only as candidate discovery; they cannot establish whether defensive code has qualifying evidence.
- Trace the defended condition through the relevant contract, producer, consumer, caller, alias, failure, concurrency, and lifecycle paths before deciding compliance.
- Verify allowed defensive code against its null behavior, exceptions, ordering, timing, alias visibility, duplicate effects, and time and space cost whenever those properties can change supported behavior.
- Do not report architecture, ownership, lifecycle, concurrency, or runtime design as an independent coding-standards violation; use those facts only to decide whether a defensive-code rule is satisfied.
