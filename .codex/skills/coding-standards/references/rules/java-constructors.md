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

# Java Constructor Rules

Apply these rules to explicit and generated constructors and to instance-field changes that may alter a generated constructor's signature or behavior.

- Use `lombok.RequiredArgsConstructor` instead of `lombok.AllArgsConstructor` when it generates the required constructor without changing its signature, visibility, constructor annotations, static factory behavior, or null-check behavior.
- Do not use `lombok.AllArgsConstructor` when it exposes a field that is not required to create a valid instance as a constructor parameter.
- Use `lombok.AllArgsConstructor` only when every parameter it generates belongs to a verified construction, compatibility, serialization, reflection, framework, or builder contract that `lombok.RequiredArgsConstructor` cannot satisfy.
- Write a manual constructor when neither Lombok constructor annotation expresses the exact required parameter set or behavior.
- Before choosing or replacing a Lombok constructor annotation, inspect field initialization, `final` and `lombok.NonNull` fields, explicit constructors, `access`, `staticName`, `onConstructor`, builders, constructor callers, and applicable framework or compatibility contracts.
- Treat annotation searches as candidate discovery only; report a violation only after proving equivalent `lombok.RequiredArgsConstructor` behavior or an unnecessary generated parameter.
- If the required constructor contract cannot be determined, preserve the existing annotation in implementation mode and mark the affected check as blocked in standalone audit mode.
