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

# Java Precondition Rules

Apply these rules to Java `throw` statements, conditional exception paths, validation or precondition calls, and calls to `ShardingSpherePreconditions`.

## Preconditions and Direct Throws

- When a conditional branch does nothing except construct and throw an exception because a required condition is false, replace the branch with the equivalent `ShardingSpherePreconditions` call if the module can use `infra/exception` and the replacement preserves behavior.
- Prefer `ShardingSpherePreconditions` with lazy exception suppliers when the module can use it and the resulting control flow preserves the required exception type, message, timing, and cause.
- Supply the exception lazily with a constructor reference or a lambda such as `ExceptionType::new` or `() -> new ExceptionType(arguments)`.
- Use `checkState` for a required boolean condition only when no specialized `ShardingSpherePreconditions` method expresses the complete condition.

## Specialized Methods

- Replace `checkState(null != value, supplier)` with `checkNotNull(value, supplier)`.
- Replace `checkState(!Strings.isNullOrEmpty(value), supplier)` with `checkNotEmpty(value, supplier)`; this rejects null and empty strings but does not reject a non-empty whitespace-only string.
- Replace `checkState(!values.isEmpty(), supplier)` and `checkState(!map.isEmpty(), supplier)` with the applicable `checkNotEmpty` overload.
- Replace `checkState(values.isEmpty(), supplier)` and `checkState(map.isEmpty(), supplier)` with the applicable `checkMustEmpty` overload.
- Replace `checkState(values.contains(element), supplier)` with `checkContains(values, element, supplier)`.
- Replace `checkState(!values.contains(element), supplier)` with `checkNotContains(values, element, supplier)`.
- Replace `checkState(map.containsKey(key), supplier)` with `checkContainsKey(map, key, supplier)`.
- Apply a specialized-method replacement only when the matched predicate is the complete expected expression; inspect a compound expression instead of mechanically splitting or rewriting it.

## Semantic Preservation and Exceptions

- Preserve the exception type, message, cause, construction timing, condition evaluation order, short-circuit behavior, and surrounding control flow.
- Keep a manual throw when the module cannot depend on `infra/exception`, the code is inside `ShardingSpherePreconditions`, a caller-facing exception contract requires it, or a precondition wrapper would obscure necessary control flow.
- Keep a direct throw for an unconditional failure, exception translation, exception wrapping, rethrowing, or a test double or fixture that deliberately simulates a failure.
- Do not add or widen a module dependency solely to replace a manual throw with `ShardingSpherePreconditions`.
- Do not replace manual throws mechanically, and record the concrete reason for keeping one.

## Semantic Verification

- Before changing or reporting a candidate, inspect the module dependencies, complete condition, branch contents, surrounding control flow, exception contract, supplier evaluation, receiver types, overload resolution, and test purpose.
- Treat searches for `throw new`, `ShardingSpherePreconditions.checkState`, `null !=`, `Strings.isNullOrEmpty`, `isEmpty`, `contains`, and `containsKey` as candidate discovery only.
- When outcome-sensitive behavior or a required contract cannot be determined, preserve the existing code in implementation mode and mark the affected check as blocked in standalone audit mode.
