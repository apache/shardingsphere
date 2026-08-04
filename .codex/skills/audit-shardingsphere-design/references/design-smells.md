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

# Design Smells

## Capability Map

| Capability | Examine | Report only when |
|---|---|---|
| Module responsibility | Owners, boundaries and dependency direction | Behavior sits outside its owner or defeats a module boundary |
| Coupling and extensibility | Concrete-type, state, configuration and lifecycle knowledge | One variant forces unrelated core or peer-plugin changes |
| Complexity and reuse | Concepts, indirection, representations and duplicate change points | A concept owns no needed behavior, state or contract and creates real cost |
| Standards and style | Written rules, intent, consistency and abstraction level | Code breaks a rule or materially obscures its responsibility |

## Module Responsibility

Derive a module's purpose from its POM, public contracts, package structure, consumers, and
maintained peers. Trace behavior rather than judging directory names. Look for:

- core or shared modules that know concrete variant classes, configuration keys, or lifecycle;
- implementation modules that redefine policy owned by a shared contract;
- dependencies flowing from an abstraction owner to a concrete implementation without a
  packaging or bootstrap reason;
- API modules containing runtime orchestration, mutable state, or implementation details;
- callers reaching across the intended public boundary because the owner exposes the wrong
  contract or data shape.

Do not report a class merely because another directory looks more attractive. Demonstrate the
responsibility mismatch and the dependency or change consequence.

## Coupling and Extensibility

Use a real supported variation as the extension test. Ask which files must change to add another
database, feature, algorithm, provider, or protocol implementation. Strong smells include:

- central `switch`, `if`, type check, or concrete-class lookup repeated for plugin identities;
- plugin-specific properties or result types leaking into neutral callers;
- one plugin importing another plugin instead of depending on a shared owned contract;
- global mutable registries or lifecycle assumptions coupling otherwise independent plugins;
- a supposedly optional plugin required to compile or package the core path.

Branching is not automatically wrong. Keep branches that implement policy genuinely owned by the
central component, especially when the set is closed by contract rather than extensible by SPI.

## Over-Design and Conceptual Surface

For every questioned abstraction, identify the production behavior, state, invariant, or public
contract it owns. Then identify the stable variation it represents. A candidate becomes a finding
when neither exists and the abstraction adds cross-file navigation, construction paths, states,
representations, tests of wiring, or dependency edges.

Treat these as signals, not verdicts: one implementation, one caller, an interface, a factory, a
wrapper, a DTO, a private helper, or a strategy name. Framework contracts, SPI loading, public API
stability, security boundaries, and genuinely clearer ownership can justify them.

Prefer the smallest conceptual surface, not mechanically fewer lines. Do not recommend inlining a
name or extraction that materially explains intent.

## Duplication and Reuse

Distinguish repeated text from repeated responsibility. Report duplication when the same stable
rule must change in several places and one clear owner can express it without importing plugin
differences. Do not unify code that only looks similar but varies by database semantics, feature
contract, algorithm behavior, lifecycle, exception contract, or release cadence.

Prefer direct reuse, then composition or delegation, then an existing extension point. A new
shared abstraction must own a stable concept; reducing line count alone does not justify it.

## Standards and Style Essence

First identify explicit violations of `CODE_OF_CONDUCT.md`, applicable `AGENTS.md`, Checkstyle, or
established public API/SPI documentation requirements. State the exact rule.

Review semantic style separately. Apply the project's development philosophy:

- **Readability:** Intent is visible from names and a direct control flow.
- **Simplicity:** Every concept and line has a current purpose; minimal does not mean compressed.
- **Consistency:** Similar responsibilities use the same maintained idiom unless semantics differ.
- **Abstraction:** Methods, classes, packages, and modules stay at one coherent level.
- **Cleanliness:** Remove parallel representations and accidental complexity, not useful domain
  language.

Do not turn personal taste or generic clean-code slogans into findings. Cite the concrete reading,
change, ownership, or consistency cost and the closest applicable project evidence.
