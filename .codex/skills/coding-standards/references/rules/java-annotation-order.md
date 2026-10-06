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

# Java Declaration Annotation Order

Apply these rules when the same Java declaration has at least two annotations from the canonical list below.

## Canonical Relative Order

Resolve annotation types before comparing them.
The canonical relative order is `org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation`, `lombok.AllArgsConstructor`, `lombok.RequiredArgsConstructor`, `lombok.NoArgsConstructor`, `lombok.Builder`, `lombok.Getter`, `lombok.Setter`, `lombok.EqualsAndHashCode`, `lombok.ToString`, and `lombok.extern.slf4j.Slf4j`.

The order is relative, so a declaration does not need to contain every listed annotation.
Filter the declaration's annotations to the recognized fully qualified types and report a violation only when that filtered sequence is not in canonical order.
Do not match an annotation only by its simple name, and do not treat nested annotations such as `lombok.Builder.Default` as the listed `lombok.Builder` annotation.

Annotations outside the canonical list may remain before, between, or after recognized annotations and do not need to be contiguous with them.
When correcting a violation, reorder only recognized annotations and preserve each unrecognized annotation's position unless another applicable written rule requires a change.
Test-framework, dependency-injection, framework, and serialization annotations are outside this order unless their fully qualified type appears in the canonical list.

## Declaration Applicability

Use the resolved annotation definition and annotation-processor contract to verify that each annotation supports the concrete declaration before applying the relative order.
The following declaration groups are the maximum scope of this rule:

- A Java type declaration may use the recognized type-targeted annotations, subject to the concrete Lombok annotation's supported type kinds.
- A field may participate only through `HighFrequencyInvocation`, `Getter`, and `Setter`.
- A method may participate only through `HighFrequencyInvocation` and `Builder`.
- A constructor may participate only through `HighFrequencyInvocation` and `Builder`.
- A parameter, local variable, catch parameter, lambda parameter, and record component does not participate in this rule.

Do not use this order to permit an annotation on a declaration kind that Java or its annotation processor does not support.

## Syntax and Semantic Boundaries

A declaration annotation annotates the declaration itself, while a `TYPE_USE` annotation annotates a use of a type and does not participate in this rule.
Java modifiers are not annotations and do not participate in the canonical sequence.
Use Checkstyle `ModifierOrder` for the syntax-level order between declaration annotations and Java modifiers, and preserve valid `TYPE_USE` placement that Checkstyle intentionally skips.
This rule governs only the position of `HighFrequencyInvocation`; do not infer, add, remove, or validate high-frequency scope, cacheability, or performance behavior from this file.
