---
name: coding-standards
description: >-
  Apply Apache ShardingSphere's written coding standards when explicitly requested, or when code-implementation routes task-changed production, test, script, build, generated, or Maven POM artifacts through repository standards.
  Also perform a standalone read-only compliance audit of a user-specified scope.
  Use CODE_OF_CONDUCT.md, applicable AGENTS.md rules, Checkstyle, Spotless, and other repository-defined standards, including naming and evidence-based defensive-code rules.
  Do not invoke independently for an ordinary implementation already governed by code-implementation, and do not independently audit architecture, runtime ownership, caches, or lifecycle.
---

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

# Coding Standards

Use this Skill in exactly one of the following modes. Both modes derive compliance only from written standards that actually apply to the target files.
For routing purposes, covered declaration annotations are `HighFrequencyInvocation`, `AllArgsConstructor`, `RequiredArgsConstructor`, `NoArgsConstructor`, `Builder`, `Getter`, `Setter`, `EqualsAndHashCode`, `ToString`, and `Slf4j`.

## Implementation Guidance Mode

Use this mode when implementing, fixing, refactoring, or reviewing code, or when modifying or reviewing a Maven POM, unless the user explicitly requests a standalone coding-standards compliance audit.
Read only the references whose conditions below match the affected files and task delta.

- Before starting a read-only code review or making the first write in an implementation task, read the portions of `CODE_OF_CONDUCT.md`, repository-level and path-level `AGENTS.md`, Checkstyle, Spotless, and other written repository standards that actually apply to every affected file.
- Before the first write, record a compact applicable-rule checklist; group files governed by the same rules instead of repeating the checklist.
- Before modifying or reviewing a `pom.xml`, read the [Maven dependency version rules](references/rules/maven-dependencies.md) through EOF and add each applicable rule to the checklist.
- In this mode, apply the Maven dependency version rules only to project dependency declarations added or modified by the current task.
- For every affected Java file, read the [Java naming rules](references/rules/java-naming.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java type declaration or its type-level annotations, read the [Java type rules](references/rules/java-types.md) through EOF and add each applicable rule to the checklist.
- Before adding, changing, or reordering a covered declaration annotation, or when a Java declaration added or modified by the task contains at least two covered annotations, read the [Java declaration annotation order rules](references/rules/java-annotation-order.md) through EOF and add each applicable rule to the checklist.
- Before adding, changing, or removing an explicit or generated constructor, or changing an instance field that may alter a generated constructor's signature or behavior, read the [Java constructor rules](references/rules/java-constructors.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java `throw` statement, a conditional branch that throws, a validation or precondition call, or a call to `ShardingSpherePreconditions`, read the [Java precondition rules](references/rules/java-preconditions.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java method annotated with `lombok.SneakyThrows` or changing the checked-exception behavior of such a method, read the [Java exception handling rules](references/rules/java-exceptions.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java declaration whose type is a `java.util.Collection` interface or implementation, read the [Java collection declaration rules](references/rules/java-collections.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java expression that copies `Collections.singleton`, `Collections.singletonMap`, or `Arrays.asList` into `ArrayList`, `LinkedList`, `HashMap`, or `LinkedHashMap`, read the [Java collection rules](references/rules/java-collections.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java `==` or `!=` comparison whose operand may resolve to a named constant, read the [Java expression rules](references/rules/java-expressions.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a multi-line Java declaration header, statement, or expression, or a Java physical line that may exceed 200 characters, read the [Java line-wrapping rules](references/rules/java-line-wrapping.md) through EOF and add each applicable rule to the checklist.
- For every affected Java test file, read the [Java test code rules](references/rules/java-testing.md) through EOF and add each applicable rule to the checklist.
- Before adding, extending, strengthening, removing, or reviewing a defensive construct, read the [defensive-code rules](references/rules/defensive-code.md) through EOF and add each applicable rule to the checklist.
- Determine whether each rule governs all existing code or only code that is new, modified, touched, submitted, or generated by Codex.
- Apply every applicable rule to each modified file within the task's authorized scope.
- After the last write, manually recheck the effective task delta and only the surrounding declarations needed to evaluate every applicable checklist rule.
- For Java, explicitly verify every applicable `CODE_OF_CONDUCT.md` coding rule, including declaration order; when a method uses private helpers, verify that those helpers immediately follow the caller and appear in the caller's call order, regardless of the caller's visibility.
- For Java, explicitly verify every applicable Java declaration annotation order rule against each declaration added or modified by the task.
- For Java, explicitly verify every applicable Java exception handling rule against each added or modified `lombok.SneakyThrows` annotation and the method bodies, invoked signatures, rethrown values, and contracts needed to resolve its exception types.
- For Java, explicitly verify every applicable Java collection declaration rule against the effective task delta and the operations, callers, callees, overrides, and contracts needed to determine the least-specific required declaration type.
- For Java, explicitly verify every applicable mutable-collection construction rule against the effective task delta and the operations, aliases, callers, callees, and contracts needed to determine whether mutability is required.
- For Java, when line-wrapping rules were selected, explicitly verify them against each applicable declaration header, statement, and expression added or modified by the task.
- For Java tests, explicitly verify every applicable Java test code rule against the effective task delta and the declarations and member types needed to resolve semantic applicability.
- Explicitly recheck every other selected rule against the effective task delta and only the declarations, contracts, producers, consumers, and supported paths needed to resolve its applicability.
- Treat passing Checkstyle and Spotless as evidence only for the rules those tools enforce; neither result replaces the manual recheck.
- A later write invalidates final-check evidence only for affected files and rules; recheck those portions before verification or handoff.
- Do not create rules from personal preference, general clean-code principles, or nearby code.
- Do not scan for or report pre-existing violations outside the current task scope.
- Run the applicable formatting and standards checks required by the repository workflow.
- Do not run `apply`, `format`, or another file-modifying command unless the current task authority and repository rules authorize every possible write.
- Do not build a complete file inventory or perform physical-line accounting in this mode.

This mode constrains the current code task and must not expand it into a repository-wide audit.

## Standalone Compliance Audit Mode

Use this mode only when the user explicitly asks for a coding-standards compliance audit of an exact file, package, Maven module, directory, or the whole repository. Keep the audit read-only and read the [standalone compliance audit workflow](references/standalone-audit.md) through EOF; it defines reference selection, complete inventory, inspection, and reporting.

This Skill may inspect architecture, ownership, lifecycle, concurrency, and runtime paths only when those facts determine whether an applicable written coding standard is satisfied.
