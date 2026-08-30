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

# Code Implementation Rules

Apply this file to every production, test, script, or other implementation artifact changed within the authorized boundary, including build logic, generated source, and behavior-affecting configuration. Treat an in-scope violation in the effective candidate as a required finding and fix it before handoff; do not expand scope or rewrite unrelated existing code.

## Design and Ownership

- Treat semantic and contract correctness, correct behavior and module ownership, verified compatibility and real boundaries, and every applicable authority, safety, scope, and architecture gate as prerequisites for each design choice. No design preference may compromise them.
- Among designs that satisfy these prerequisites, first preserve the maintained architecture already present in the repository, including its behavior owners, module boundaries, and design patterns, unless the user explicitly requires an authorized architecture change. Prefer direct reuse, then composition or delegation, then an existing extension point. Do not invent a boundary when no maintained precedent exists. If the existing architecture cannot express the required behavior, apply the architecture-change gate before changing it.
- Second, within the preserved architecture or an explicitly authorized architecture change, use the smallest clear implementation, prioritize local readability over line count, and minimize conceptual surface. Prefer one readable local flow with the fewest independently meaningful types, states, representations, execution paths, and cross-file hops. Consider testability next, then optional extensibility, formal symmetry, or structural completeness. Lower design priorities must not compromise higher ones. Every added line, helper, abstraction, identifier, literal, guard, copy, wrapper, and configuration entry must serve production behavior, a public contract, regression protection, diagnostics, a verified safety requirement, readability, or removal of real duplication. Do not add code for formal symmetry, coverage appearance, structural completeness, hypothetical reuse, or test convenience. Avoid unnecessary locals, thin wrappers, helpers, comments, guards, and copies; inline single-use locals unless a name improves readability. Keep a name, local variable, or extraction when it materially clarifies intent; do not compress code mechanically.
- A new abstraction must own production behavior, state, or a contract that existing types cannot express and must represent a real, stable variation or boundary. Define the narrowest contract, keep common behavior in its owner, and isolate only true implementation or dialect differences. Forwarding, renaming, multiple callers, readability alone, direct test value, symmetry, type distinction, shorter call sites, test convenience, and anticipated reuse are insufficient. Reuse alone never authorizes changing `final`, visibility, inheritance, or shared-code ownership. Do not wrap a simple internal two-path flow in marker interfaces, result hierarchies, or DTO-style helpers unless they define a stable boundary, keep the owner readable, or remove meaningful duplication. This rule does not bypass the architecture-change gate.
- Do not introduce package-private top-level helper types by default. Keep a small single-owner helper private and nested; add a top-level helper only when the approved production behavior, state, or contract cannot reasonably be owned by an existing type.
- Do not add production types, widen visibility, remove `final`, or change constructors or signatures for test convenience. Avoid accumulating multiple nested collaborators inside one owner.
- Keep only constructors with distinct production semantics or framework, reflection, serialization, or SPI requirements. Update callers explicitly instead of adding convenience or compatibility overloads. Before adding or changing a constructor, inspect nearby production conventions for visibility, Lombok, validation, and tests. Before handoff, scan changed call sites for unused or compatibility-only constructors.
- Before handoff, inspect every method or constructor parameter added or made unused by the current task. Remove a parameter that the effective candidate does not use and update every in-scope caller unless the signature is required by a public API, override, interface, SPI, reflection, serialization, callback, framework, or compatibility contract. Keep a contract-required parameter, record the exact contract, and apply the architecture and scope gates before changing that signature.
- Apply `.codex/skills/code-implementation/references/rules/artifact-removal-and-contract-impact.md` before classifying code as unused or removable. Make the change converge on one coherent model: correct or replace the existing owner before adding a parallel path, then remove superseded in-scope representations, paths, adapters, shims, tests, configuration, and other obsolete code after verifying usages and contracts. Keep coexistence only for a verified compatibility contract. If convergence exceeds the acceptance checklist or file-type authorization, stop at the existing gate. Do not leave placeholders, TODO implementations, speculative compatibility shims, or test-only production hooks.
- When adding a database, dialect, plugin, or module, reuse the applicable framework and extension mechanisms. Keep derived dialects aligned with shared behavior and isolate only real differences. If no maintained precedent exists, record the search evidence before using the architecture gate.
- Reuse or create an SPI only when its contract matches the required production behavior. One caller, one implementation, code sharing, type distinction, symmetry, anticipated reuse, or test convenience does not establish an SPI contract.

## Evidence Required for Defensive Code

Defensive code is a guard, repeated validation, copy, wrapper, fallback, default value, exception catch, retry, or mutation restriction added to prevent misuse or failure rather than implement requested production behavior.

- Apply this gate to every defensive construct that the current task adds, extends, or strengthens; touching nearby code does not authorize changing a pre-existing defensive construct that the task does not alter.
- Do not add, extend, or strengthen defensive code unless qualifying evidence proves that the defended condition occurs on a supported production path or an explicit existing contract requires the defense.
- Qualifying evidence is limited to user-required behavior, an explicit public, internal, SPI, framework, serialization, concurrency, or lifecycle contract, a maintained production consumer, or an observed failure on a supported path.
- Before adding or keeping defensive code, record in the task analysis or review the exact defended condition, the supported producer, consumer, or boundary that can trigger it, the behavior or contract being protected, and the qualifying evidence source.
- A mutable type, theoretical misuse, a possible future caller, general safety, best practice, consistency with nearby code, hypothetical reuse, and test convenience are not qualifying evidence.
- A test cannot establish the production condition or contract used to justify defensive code by itself; it may only corroborate qualifying production or contract evidence.
- When qualifying evidence does not exist, omit or remove defensive code added by the current task, undo only the task-introduced extension or strengthening of a pre-existing defensive construct, preserve its pre-task behavior, and remove every task-introduced test whose only purpose is to preserve the unsupported defense.

### Collection Copies and Mutation Restrictions

- Before adding a collection copy or unmodifiable wrapper, inspect current production producers and consumers, alias ownership, asynchronous or concurrent lifetime, and applicable public, SPI, framework, serialization, immutability, snapshot, and isolation contracts.
- `List.copyOf`, `Map.copyOf`, `Set.copyOf`, `Collections.unmodifiable*`, collection constructors that copy another collection, and equivalent copies or wrappers are allowed only when the inspected evidence proves at least one of the following allowed cases.
  - An explicit existing contract requires immutable or snapshot semantics.
  - A supported producer or consumer currently mutates an aliased collection after handoff, and that mutation would violate behavior owned by the receiving object.
  - A supported path exposes owned mutable state, and an existing contract forbids the consumer from mutating it.
  - A supported asynchronous or concurrent path requires a stable snapshot.
- A mutable collection implementation, or the theoretical ability of a caller to mutate it, is not evidence.
- When inspection finds no supported mutation path and no explicit contract, pass, store, or return the original collection directly; remove every task-introduced copy or wrapper and every task-introduced test that only asserts immutability or rejected mutation.
- When a copy or wrapper is allowed, verify that its null handling, iteration order, duplicate semantics, aliasing, mutation visibility, exception behavior, and time and space cost preserve the required behavior.

## Validation and Exceptions

- Add runtime validation only when the defensive-code evidence gate proves that it is required at a real external, public, persisted, parsed, SPI, reflection, shared-state, or asynchronous boundary to preserve an existing contract or provide a concrete diagnostic for a condition that can occur there. Do not recheck invariants guaranteed by callers or upstream contracts.
- Prefer `ShardingSpherePreconditions` with lazy exception suppliers when the module can use it and the resulting control flow preserves the required exception type, message, timing, and cause.
- Keep a manual throw when the module cannot depend on `infra/exception`, the code is inside `ShardingSpherePreconditions`, a caller-facing exception contract requires it, or a precondition wrapper would obscure necessary control flow. Do not replace manual throws mechanically, and record the concrete reason for keeping one.
- When an edit removes the last checked-exception source from a private or internal method, remove the stale `throws` declaration and update callers. Keep checked exceptions on public or overridden methods only when a caller-facing, framework, external API, or compatibility contract requires them. Never widen to generic `Exception` or `Throwable`.

## Repository Style

- Apply the `CODE_OF_CONDUCT.md` Lombok preference to touched boilerplate when generated semantics are equivalent. Use narrow annotations; never use broad annotations such as `@Data` unless every generated behavior is required. Before replacing a public constructor or accessor, verify its generated signature, access, parameter order, annotations, and reflection or serialization behavior. Keep a manual member when Lombok would change or obscure logic, documentation, validation, defaults, side effects, compatibility, framework semantics, or a public contract.
- Keep public API and SPI Javadocs required by the code of conduct. Beyond that, document only caller or implementer obligations not expressed by code. Do not restate names, signatures, visible collection properties, or inherited contracts unless an override adds a new obligation.
- Keep declarations near first use. Do not mark local, loop, resource, or lambda variables `final`; use `final` only for method, constructor, and `catch` parameters when applicable.
- Declare collections by the least-specific required contract. Use `Collection` for common iteration, size, or emptiness operations; use `List` only for positional, ordered, duplicate-preserving, or required API semantics; use `Set` only for uniqueness or set semantics. Do not declare a concrete collection type unless its implementation-specific API is required. In tests, create a mutable copy only when that instance is mutated or mutability is the scenario.
- Add a YAML anchor only when aliases in the same file remove meaningful duplication.
- Use repository-relative paths, configuration, or temporary directories in code, tests, scripts, and Skills; never hard-code a local workspace path.
- Add the ASF license header to new source files and keep implementation intent and reviewer-relevant rationale transparent.
- Account for compatibility, security, time and space complexity, I/O, memory, resource lifecycle, concurrency, and boundary failures when the affected path makes them relevant.
