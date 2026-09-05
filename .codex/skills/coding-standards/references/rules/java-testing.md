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

# Java Test Code Rules

Apply these rules only to Java test code.

## Actual-Value Naming

- Name a local variable that stores the value produced by the operation under test with the `actual` prefix; use `actual` when no additional domain distinction is needed.
- Do not name that local variable `result` when it supplies the actual value to an assertion.
- When correcting the name, rename the declaration and every reference to the same variable.
- Keep a direct assertion when the tested expression is clear; do not introduce an `actual` local whose only purpose is to satisfy the naming rule.
- The returned-value rules in `java-naming.md` continue to govern a test helper's own return flow; the actual-value rule overrides them only for a local value asserted as the result of the operation under test.

## Reflective Field Access

- Do not call `AccessibleObject#setAccessible` in Java test code.
- When the `CODE_OF_CONDUCT.md` field-access exception applies, use `Plugins.getMemberAccessor().get` or `Plugins.getMemberAccessor().set` instead of changing a `Field` object's accessibility and calling `Field#get` or `Field#set` directly.
- Do not use core reflection or `MemberAccessor#invoke` or `MemberAccessor#newInstance` to test private methods or constructors; exercise that behavior through a public API.
- Before reporting a `setAccessible` violation, resolve the receiver as `Field`, `Method`, `Constructor`, or another `AccessibleObject`; a custom method with the same name is not a violation.

## Empty Collection and Map Assertions

- When a Java test verifies that a `Collection`, `Map`, or one of their subtypes is empty, use `assertTrue(expression.isEmpty())` instead of `assertThat(expression.size(), is(0))`.
- When the original assertion supplies a failure reason, preserve it as the second argument to `assertTrue`.
- Resolve the receiver's declared type or the invoked method's return contract before applying this rule; the presence of a `size()` method alone does not prove that the receiver is a `Collection` or `Map`.
- Do not report or rewrite an assertion on another type merely because that type provides `size()`; when the receiver type cannot be proved, do not report a confirmed violation.
