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

# Java Exception Handling Rules

Apply these rules when adding, changing, or auditing a Java method annotated with `lombok.SneakyThrows`.

- Do not require `lombok.SneakyThrows`; code that handles or declares its checked exceptions without the annotation is compliant with these rules.
- When using `lombok.SneakyThrows`, specify the narrowest stable checked exception type or types that cover the checked exceptions intentionally propagated without a `throws` clause.
- Use `Exception.class` only when the method body explicitly throws `Exception` or calls an API whose applicable checked-exception contract is `Exception`, and the current method intentionally omits `throws Exception` from its signature.
- Do not use `Exception.class` when the method's checked exceptions can be expressed with more specific stable types.
- An existing `throws Exception` clause does not justify `@SneakyThrows(Exception.class)` because the annotation is unnecessary for that declared exception.
- Use `Throwable.class` only when the method body actually rethrows a value statically typed as `Throwable` and preserving that propagation is required; do not use it as a catch-all for unresolved exceptions.
- Before choosing or auditing the annotation values, inspect the complete method body, invoked method signatures, caught and rethrown values, overrides, framework contracts, and compatibility requirements.
- Treat annotation searches as candidate discovery only; report a semantic violation only after resolving the checked exceptions that the method can propagate.
- When an outcome-sensitive exception contract cannot be determined, preserve the existing annotation values in implementation mode and mark the affected check as blocked in standalone audit mode.
