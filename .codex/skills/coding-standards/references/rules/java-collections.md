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

# Java Collection Declaration Rules

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
