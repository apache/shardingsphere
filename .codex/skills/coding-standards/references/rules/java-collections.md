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

# Java Collection Rules

## Declaration Types

Apply these rules to fields, local variables, method and constructor parameters, and method return types declared as a `java.util.Collection` interface or implementation.

- Declare a collection as `Collection<E>` when every required operation and applicable contract can be expressed by `Collection`.
- Use `List<E>` only when an applicable caller, callee, override, SPI, framework, or compatibility contract requires `List`, or when the implementation requires a `List`-specific operation such as positional access through `get(int)`.
- Use `Set<E>` only when an applicable caller, callee, override, SPI, framework, or compatibility contract requires `Set`, or when the declaration must express uniqueness or set semantics.
- Use `Queue<E>` only when an applicable caller, callee, override, SPI, framework, or compatibility contract requires `Queue`, or when the implementation requires queue operations such as `offer`, `poll`, or `peek`.
- Do not infer the declaration type from the concrete implementation that creates the value; a declaration may use `Collection<E>` while its value is created by `ArrayList`, `HashSet`, `ArrayDeque`, or another suitable implementation.
- Do not declare a concrete collection implementation unless the implementation-specific API is required.
- Before choosing or auditing a declaration type, inspect its assignments and uses together with applicable callers, callees, overrides, interfaces, SPIs, framework contracts, and compatibility contracts.
- Treat searches for `List`, `Set`, `Queue`, or concrete collection classes as candidate discovery only; report a violation only after proving that `Collection` provides every required operation and satisfies every applicable contract.
- When an outcome-sensitive contract cannot be determined, preserve the existing declaration in implementation mode and mark the affected check as blocked in standalone audit mode.

## Mutable Collection Construction

Treat the following expressions as candidates whose mutable-copy construction requires semantic inspection, not as violations by text match alone.

```java
new LinkedList<>(Collections.singleton(value))
new ArrayList<>(Collections.singleton(value))
new LinkedList<>(Arrays.asList(values))
new ArrayList<>(Arrays.asList(values))
new HashMap<>(Collections.singletonMap(key, value))
new LinkedHashMap<>(Collections.singletonMap(key, value))
```

- Before adding, retaining, or auditing a candidate, trace the constructed collection or map through its local operations, aliases, callers, callees, overrides, SPIs, framework behavior, and compatibility contracts.
- Keep the mutable-copy construction only when a supported operation mutates the constructed value or an explicit existing contract requires that value to be mutable.
- A mutable declaration type, hypothetical future mutation, consistency with nearby code, and test setup convenience do not prove that a mutable value is required.
- When no mutation requirement exists, remove the outer mutable-copy constructor and use the source value or the least complex non-mutable factory that satisfies the required collection or map contract.
- Preserve the required collection abstraction when removing a mutable copy; for example, use `Collections.singletonList(value)` when a `List` is required instead of retaining `Collections.singleton(value)`, which is a `Set`.
- Do not retain an `ArrayList` or `LinkedList` copy of `Arrays.asList` solely for element replacement because `Arrays.asList` supports `set`; size-changing operations such as `add` or `remove` require another mutable list implementation.
- A snapshot or alias-isolation requirement may justify a copy under an applicable written rule, but it does not by itself justify making the resulting value mutable.
- Before replacing a candidate, verify the required abstraction and preserve null handling, iteration order, duplicate behavior, alias visibility, mutation behavior, exception behavior, and supported caller and callee contracts.
- When an outcome-sensitive mutation or contract fact cannot be determined, preserve the existing construction in implementation mode and mark the affected check as blocked in standalone audit mode.
