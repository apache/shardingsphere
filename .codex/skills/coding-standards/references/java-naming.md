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

# Java Naming and Lombok Rules

Apply these rules to fields, local variables, method and constructor parameters, and lambda parameters in Java source governed by this Skill.

## Identifier Components

- Spell the MySQL component of a variable identifier `mysql`; `mySQL` is prohibited.
- Spell the PostgreSQL component of a variable identifier `postgresql`; `postgreSQL` is prohibited.
- In ordinary variable identifiers, abbreviate `arguments` as `args`, `parameters` as `params`, `environment` as `env`, `configuration` as `config`, `configurations` as `configs`, and `properties` as `props`.
- Apply the abbreviation rule to a whole identifier and to the corresponding lower-camel-case component of a compound identifier.
- An ordinary variable identifier must not contain `optional`; the direct lambda parameter of `java.util.Optional#map` is the only naming exception.
- Method parameters must not be named `result`, `each`, or `entry` unless a verified contract fixes the parameter name.

## Lombok-Generated Accessors

- Do not use `lombok.Data` or `lombok.Value`, whether referenced through an import or by a fully qualified annotation name.
- Do not report an annotation named `Value` from another package as a violation of the Lombok prohibition.
- Use only the narrow Lombok annotations whose generated members are required.
- When Lombok generates a getter or setter from a field identifier, spell the listed words in that field identifier as `arguments`, `parameters`, `environment`, `configuration`, `configurations`, and `properties` instead of `args`, `params`, `env`, `config`, `configs`, and `props`.
- An abbreviated field name is allowed when neither a Lombok getter nor a Lombok setter is generated for that field; account for class-level annotations and field-level `AccessLevel.NONE` overrides before deciding.
- If either a Lombok getter or a Lombok setter is generated for a field, the full-word field-name rule applies even when generation of the other accessor is disabled.

## Returned Values

- Return an expression directly when a local variable would have no use other than its declaration and immediate return.
- Do not introduce a local variable named `result` solely to satisfy the returned-value naming rule.
- A local variable that must remain because it participates in intermediate accumulation, mutation, validation, or another operation before being returned must be named `result`.
- A method parameter may be returned directly without being renamed or copied to `result`.

Use a direct return when no intermediate operation exists:

```java
return createRule();
```

Do not retain an unnecessary local solely for the return:

```java
Rule result = createRule();
return result;
```

## Loop Variables

- Name the element variable of an enhanced `for` loop `each`.
- Name the element variable `entry` instead when its declared type is `Map.Entry`.
- Do not apply the `each` or `entry` rule to the initializer, condition, update expression, index, or counter of a traditional `for` loop.

## Lambda Parameters

- Name the direct lambda parameter of `java.util.Optional#map` exactly `optional`, regardless of the contained value's domain type.
- The exact `optional` identifier is mandatory for this parameter; do not use `value`, `each`, a domain-specific name, or a compound name such as `optionalRule`.
- This specific rule overrides the ordinary prohibition on `optional` and any general preference for domain-specific lambda parameter names.
- Given `Optional<Rule> maybeRule`, the compliant lambda form is `maybeRule.map(optional -> normalize(optional))`.
- Do not apply the `optional` name to `Stream#map`, `flatMap`, `filter`, `ifPresent`, or any other operation.
- A method reference passed to `Optional#map` has no lambda parameter to name and is not constrained by the `optional` rule.
- Do not use generic two-parameter lambda pairs such as `(a, b)` or `(k, v)`.
- Name a map key-and-value pair `(key, value)` or use more specific domain names that express both roles.
- When the two lambda parameters semantically represent the old value and the current value, name them exactly `(oldValue, currentValue)` in that order.
- Do not apply the old-and-current pair merely because a lambda has two parameters; first verify the parameter roles from the invoked API and surrounding code.

## Semantic Verification and Contract Exceptions

- Inspect parsed declarations, resolved receiver and annotation types, generated Lombok accessors, and lambda roles when those facts determine whether a rule applies.
- Treat text searches and regular expressions only as candidate discovery; they cannot prove compliance with semantic rules.
- Ignore matching text in comments, string literals, type names, method names, and unrelated annotation types.
- Apply the rules regardless of whitespace, line breaks, punctuation, declaration layout, or whether an annotation is imported or fully qualified.
- When a public API, override, interface, SPI, framework, serialization, reflection, or compatibility contract fixes an identifier that would otherwise violate a naming rule, record the exact contract and keep the identifier unless the authorized task includes changing that contract.
- Treat a verified contract-fixed identifier as an allowed naming exception in a standalone audit, and report the contract instead of proposing a mechanical rename.
- The contract exception does not permit `lombok.Data` or `lombok.Value`.
