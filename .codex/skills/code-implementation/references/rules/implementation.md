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

- Within already-authorized designs, resolve trade-offs in this order: semantic and contract correctness; correct behavior and module ownership; verified compatibility and real boundaries; minimal conceptual surface and local readability; consistency with maintained nearby code and direct reuse; testability; then optional extensibility, formal symmetry, or structural completeness. A lower-priority concern must not compromise a higher-priority one. This order does not override authority, safety, scope, or architecture gates.
- Preserve existing architecture by default. Prefer direct reuse, then composition or delegation, then an existing extension point. Do not invent a boundary when no maintained precedent exists.
- Use the smallest clear implementation and minimize conceptual surface rather than line count. Prefer one readable local flow with the fewest independently meaningful types, states, representations, execution paths, and cross-file hops. Every added line, helper, abstraction, identifier, literal, guard, copy, wrapper, and configuration entry must serve production behavior, a public contract, regression protection, diagnostics, safety, readability, or removal of real duplication. Do not add code for formal symmetry, coverage appearance, structural completeness, hypothetical reuse, or test convenience. Avoid unnecessary locals, thin wrappers, helpers, comments, guards, and copies; inline single-use locals unless a name improves readability. Keep a name, local variable, or extraction when it materially clarifies intent; do not compress code mechanically.
- A new abstraction must own production behavior, state, or a contract that existing types cannot express and must represent a real, stable variation or boundary. Define the narrowest contract, keep common behavior in its owner, and isolate only true implementation or dialect differences. Forwarding, renaming, multiple callers, readability alone, direct test value, symmetry, type distinction, shorter call sites, test convenience, and anticipated reuse are insufficient. Reuse alone never authorizes changing `final`, visibility, inheritance, or shared-code ownership. Do not wrap a simple internal two-path flow in marker interfaces, result hierarchies, or DTO-style helpers unless they define a stable boundary, keep the owner readable, or remove meaningful duplication. This rule does not bypass the architecture-change gate.
- Do not introduce package-private top-level helper types by default. Keep a small single-owner helper private and nested; add a top-level helper only when the approved production behavior, state, or contract cannot reasonably be owned by an existing type.
- Do not add production types, widen visibility, remove `final`, or change constructors or signatures for test convenience. Avoid accumulating multiple nested collaborators inside one owner.
- Keep only constructors with distinct production semantics or framework, reflection, serialization, or SPI requirements. Update callers explicitly instead of adding convenience or compatibility overloads. Before adding or changing a constructor, inspect nearby production conventions for visibility, Lombok, validation, and tests. Before handoff, scan changed call sites for unused or compatibility-only constructors.
- Apply `.codex/skills/code-implementation/references/rules/contracts-and-removal.md` before classifying code as unused or removable. Make the change converge on one coherent model: correct or replace the existing owner before adding a parallel path, then remove superseded in-scope representations, paths, adapters, shims, tests, configuration, and other obsolete code after verifying usages and contracts. Keep coexistence only for a verified compatibility contract. If convergence exceeds the acceptance checklist or file-type authorization, stop at the existing gate. Do not leave placeholders, TODO implementations, speculative compatibility shims, or test-only production hooks.
- When adding a database, dialect, plugin, or module, reuse the applicable framework and extension mechanisms. Keep derived dialects aligned with shared behavior and isolate only real differences. If no maintained precedent exists, record the search evidence before using the architecture gate.
- Reuse or create an SPI only when its contract matches the required production behavior. One caller, one implementation, code sharing, type distinction, symmetry, anticipated reuse, or test convenience does not establish an SPI contract.

## Validation and Exceptions

- Add runtime validation only at real external, public, persisted, parsed, SPI, reflection, shared-state, or asynchronous boundaries, or for a concrete diagnostic benefit. Do not recheck invariants guaranteed by callers or upstream contracts.
- Prefer `ShardingSpherePreconditions` with lazy exception suppliers when the module can use it and the resulting control flow preserves the required exception type, message, timing, and cause.
- Keep a manual throw when the module cannot depend on `infra/exception`, the code is inside `ShardingSpherePreconditions`, a caller-facing exception contract requires it, or a precondition wrapper would obscure necessary control flow. Do not replace manual throws mechanically, and record the concrete reason for keeping one.
- When an edit removes the last checked-exception source from a private or internal method, remove the stale `throws` declaration and update callers. Keep checked exceptions on public or overridden methods only when a caller-facing, framework, external API, or compatibility contract requires them. Never widen to generic `Exception` or `Throwable`.

## Repository Style

- Apply the `CODE_OF_CONDUCT.md` Lombok preference to touched boilerplate when generated semantics are equivalent. Use narrow annotations; never use broad annotations such as `@Data` unless every generated behavior is required. Before replacing a public constructor or accessor, verify its generated signature, access, parameter order, annotations, and reflection or serialization behavior. Keep a manual member when Lombok would change or obscure logic, documentation, validation, defaults, side effects, compatibility, framework semantics, or a public contract.
- Keep public API and SPI Javadocs required by the code of conduct. Beyond that, document only caller or implementer obligations not expressed by code. Do not restate names, signatures, visible collection properties, or inherited contracts unless an override adds a new obligation.
- Keep declarations near first use. Do not mark local, loop, resource, or lambda variables `final`; use `final` only for method, constructor, and `catch` parameters when applicable.
- Declare collections by the least-specific required contract. Do not copy or wrap collections without an owned mutability, snapshot, isolation, or public contract reason. Record the concrete reason for every explicit copy or wrapper. Use `Collection` for common iteration, size, or emptiness operations; use `List` only for positional, ordered, duplicate-preserving, or required API semantics; use `Set` only for uniqueness or set semantics. Do not declare a concrete collection type unless its implementation-specific API is required. In tests, create a mutable copy only when that instance is mutated or mutability is the scenario.
- Add a YAML anchor only when aliases in the same file remove meaningful duplication.
- Use repository-relative paths, configuration, or temporary directories in code, tests, scripts, and Skills; never hard-code a local workspace path.
- Add the ASF license header to new source files and keep implementation intent and reviewer-relevant rationale transparent.
- Account for compatibility, security, time and space complexity, I/O, memory, resource lifecycle, concurrency, and boundary failures when the affected path makes them relevant.
