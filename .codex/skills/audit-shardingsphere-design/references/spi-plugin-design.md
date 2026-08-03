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

# SPI Plugin Design

- [Model](#model)
- [Common Plugin Invariants](#common-plugin-invariants)
- [Category Profiles](#category-profiles)
- [High-Value Smells](#high-value-smells)

## Model

Treat every production implementation of an SPI as a plugin, regardless of whether it represents
a database, feature, algorithm, provider, protocol, loader, or another extension category. Treat
the SPI contract, neutral loader, and registration infrastructure as the extension mechanism, not
as plugin implementations.

This model does not require every implementation to have its own Maven module. Judge module
granularity from the real variation axis, dependency direction, independent packaging needs, and
maintained project structure.

## Common Plugin Invariants

Check every SPI category for these invariants:

1. The contract expresses a stable variation needed by production behavior, not type distinction,
   test convenience, formal symmetry, or anticipated reuse alone.
2. The contract and loader remain neutral to concrete plugin classes and plugin-only data shapes.
3. Core callers depend on the contract. Plugins depend on the contract or shared owned behavior;
   peer plugins do not depend on each other for convenience.
4. Registration, type identity, loader selection, Maven dependencies, and packaged resources
   agree. A source-level implementation that cannot be discovered or packaged is incomplete.
5. Configuration validation, lifecycle, state, and failure handling belong at the boundary that
   owns them and do not leak one plugin's assumptions into all callers.
6. Shared behavior stays with its real owner, while variant behavior stays inside the plugin.

Trace `contract -> loader/registry -> registration -> implementation -> caller -> package` before publishing a placement or coupling finding.

## Category Profiles

### Database and Dialect Plugins

Use this profile when behavior varies by database type, dialect, supported version, protocol, or
database capability. Keep database-neutral policy in its owning core and isolate real dialect
differences in maintained database or dialect modules. Check derived databases for intentional
reuse of shared semantics without erasing their plugin identities.

A database-specific implementation in `infra`, `kernel`, or another shared module is a candidate,
not an automatic finding. Prove that the shared module now knows a database variant, that a
maintained dialect module can own it without reversing dependencies, and that the move preserves
registration and packaging. Conversely, keep an infrastructure-owned extension in infrastructure
when the variation itself is infrastructure behavior rather than a database plugin.

### Feature or Function Plugins

Use this profile when selection varies by a product feature, rule, transaction mode, authority
provider, governance behavior, function, protocol capability, or another functional type. Derive
ownership from the feature's API/core/type structure. Keep feature-neutral orchestration in the
feature owner and concrete behavior in the corresponding type or provider module.

Do not force a feature plugin into a database module merely because it consumes database metadata.
When a plugin varies by both feature and database, place the implementation at the narrowest
maintained boundary that owns both axes without making core depend on the implementation.

### Algorithm Plugins

Use this profile when behavior is selected by algorithm type and properties. Keep algorithm
contracts, shared property semantics, and common validation in their owners. Keep concrete
strategies in algorithm type modules and prevent neutral callers from branching on their classes.

Small implementations are valid plugins; do not report them as over-designed merely because the
algorithm is simple. Report abstraction overhead only when extra contracts or wrappers do not
serve loading, configuration, behavior, or a stable algorithm family.

### Other SPI Plugins

Do not squeeze an unknown SPI into the three profiles above. Identify its actual selection key,
variation reason, lifecycle, packaging boundary, and consumer. Apply the common invariants and
derive category-specific ownership from maintained peers with the same runtime role.

## High-Value Smells

- Core code imports, constructs, casts to, or branches on concrete plugin classes.
- A plugin implementation sits in a shared module and makes that module depend on variant-only
  configuration, libraries, resources, or release behavior.
- The SPI returns an implementation-specific DTO or exposes lifecycle methods needed by only one
  plugin.
- Adding one plugin requires edits to unrelated plugin modules or repeated central registries.
- Registration points to the wrong implementation, duplicates an identity, or is absent from the
  artifact that must provide it.
- Common code copied across plugins is stable owner behavior, or shared code contains variant
  branches that should remain inside plugins.

For every smell, inspect counterexamples such as framework discovery constraints, bootstrap-owned
registries, deliberately closed sets, default implementations, and distribution assembly. Report
the issue only when those facts do not justify the design.
