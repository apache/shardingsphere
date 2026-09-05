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

# Java Expression Rules

This file governs only named-constant comparisons that require semantic resolution beyond Checkstyle's syntax checks.

## Named Constant Comparison Order

- Place a resolved named constant on the left and a variable or other non-constant expression on the right when they are operands of `==` or `!=`.
- Apply this rule only when a declaration, enum membership, static import, or qualified-member resolution proves that the operand is a constant.
- Do not classify an identifier as a constant from uppercase spelling alone.
- When both operands are constants, both operands are non-constant expressions, or accessible evidence cannot resolve the roles, do not report a confirmed standalone-audit violation.
- In implementation mode, inspect the accessible declaration before changing comparison order; if the roles remain unresolved, preserve the expression instead of guessing.

Use the resolved constant as the left operand:

```java
MAX_SIZE == size
Status.ACTIVE != status
Integer.MAX_VALUE == limit
```

Do not place the variable or other non-constant expression first:

```java
size == MAX_SIZE
status != Status.ACTIVE
limit == Integer.MAX_VALUE
```
