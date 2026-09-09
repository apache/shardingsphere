---
name: coding-standards
description: >-
  Apply Apache ShardingSphere's written coding standards during implementation,
  fixes, refactoring, and code review, or perform a standalone read-only
  compliance audit of a user-specified scope. Uses CODE_OF_CONDUCT.md,
  applicable AGENTS.md rules, Checkstyle, Spotless, and other repository-defined
  standards, including naming and evidence-based defensive-code rules. Does not
  independently audit architecture, runtime ownership, caches, or lifecycle.
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

Use this mode when implementing, fixing, refactoring, or reviewing code unless the user explicitly requests a standalone coding-standards compliance audit.

- Before starting a read-only code review or making the first write in an implementation task, read the portions of `CODE_OF_CONDUCT.md`, repository-level and path-level `AGENTS.md`, Checkstyle, Spotless, and other written repository standards that actually apply to every affected file.
- Before the first write, record a compact applicable-rule checklist; group files governed by the same rules instead of repeating the checklist.
- For every affected Java file, read the [Java naming rules](references/rules/java-naming.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java type declaration or its type-level annotations, read the [Java type rules](references/rules/java-types.md) through EOF and add each applicable rule to the checklist.
- Before adding, changing, or reordering a covered declaration annotation, or when a Java declaration added or modified by the task contains at least two covered annotations, read the [Java declaration annotation order rules](references/rules/java-annotation-order.md) through EOF and add each applicable rule to the checklist.
- Before adding, changing, or removing an explicit or generated constructor, or changing an instance field that may alter a generated constructor's signature or behavior, read the [Java constructor rules](references/rules/java-constructors.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java `throw` statement, a conditional branch that throws, a validation or precondition call, or a call to `ShardingSpherePreconditions`, read the [Java precondition rules](references/rules/java-preconditions.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java method annotated with `lombok.SneakyThrows` or changing the checked-exception behavior of such a method, read the [Java exception handling rules](references/rules/java-exceptions.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java declaration whose type is a `java.util.Collection` interface or implementation, read the [Java collection declaration rules](references/rules/java-collections.md) through EOF and add each applicable rule to the checklist.
- Before adding or changing a Java expression that copies `Collections.singleton`, `Collections.singletonMap`, or `Arrays.asList` into `ArrayList`, `LinkedList`, `HashMap`, or `LinkedHashMap`, read the [Java collection rules](references/rules/java-collections.md) through EOF and add each applicable rule to the checklist.
- For every affected Java file, read the [Java expression rules](references/rules/java-expressions.md) through EOF and add each applicable rule to the checklist.
- For every affected Java file, read the [Java line-wrapping rules](references/rules/java-line-wrapping.md) through EOF and add each applicable rule to the checklist.
- For every affected Java test file, read the [Java test code rules](references/rules/java-testing.md) through EOF and add each applicable rule to the checklist.
- For every affected implementation artifact, read the [defensive-code rules](references/rules/defensive-code.md) through EOF and add each applicable rule to the checklist.
- Determine whether each rule governs all existing code or only code that is new, modified, touched, submitted, or generated by Codex.
- Apply every applicable rule to each modified file within the task's authorized scope.
- After the last write, manually recheck the effective task delta and only the surrounding declarations needed to evaluate every applicable checklist rule.
- For Java, explicitly verify every applicable `CODE_OF_CONDUCT.md` coding rule, including declaration order; when a method uses private helpers, verify that those helpers immediately follow the caller and appear in the caller's call order, regardless of the caller's visibility.
- For Java, explicitly verify every applicable Java naming rule against the effective task delta and the surrounding declarations needed to resolve semantic applicability.
- For Java, explicitly verify every applicable Java type rule against each type declaration and type-level annotation added or modified by the task.
- For Java, explicitly verify every applicable Java declaration annotation order rule against each declaration added or modified by the task.
- For Java, explicitly verify every applicable Java constructor rule against each explicit or generated constructor added, modified, removed, or affected by an instance-field change in the task.
- For Java, explicitly verify every applicable Java precondition rule against each added or modified exception-throwing condition and precondition call, including whether a specialized `ShardingSpherePreconditions` method can express the condition without changing exception or control-flow semantics.
- For Java, explicitly verify every applicable Java exception handling rule against each added or modified `lombok.SneakyThrows` annotation and the method bodies, invoked signatures, rethrown values, and contracts needed to resolve its exception types.
- For Java, explicitly verify every applicable Java collection declaration rule against the effective task delta and the operations, callers, callees, overrides, and contracts needed to determine the least-specific required declaration type.
- For Java, explicitly verify every applicable mutable-collection construction rule against the effective task delta and the operations, aliases, callers, callees, and contracts needed to determine whether mutability is required.
- For Java, explicitly verify every applicable Java expression rule against the effective task delta and the declarations needed to resolve named constants.
- For Java, explicitly verify every applicable Java line-wrapping rule against each declaration header, statement, and expression added or modified by the task.
- For Java tests, explicitly verify every applicable Java test code rule against the effective task delta and the declarations and member types needed to resolve semantic applicability.
- Explicitly verify every applicable defensive-code rule against the effective task delta and the contracts, producers, consumers, and supported paths needed to determine whether each defensive construct has qualifying evidence.
- Treat passing Checkstyle and Spotless as evidence only for the rules those tools enforce; neither result replaces the manual recheck.
- A later write invalidates final-check evidence only for affected files and rules; recheck those portions before verification or handoff.
- Do not create rules from personal preference, general clean-code principles, or nearby code.
- Do not scan for or report pre-existing violations outside the current task scope.
- Run the applicable formatting and standards checks required by the repository workflow.
- Do not run `apply`, `format`, or another file-modifying command unless the current task authority and repository rules authorize every possible write.
- Do not build a complete file inventory or perform physical-line accounting in this mode.

This mode constrains the current code task and must not expand it into a repository-wide audit.

## Standalone Compliance Audit Mode

Use this mode only when the user explicitly asks to audit, check, or report coding-standards compliance.

- Require an exact file, package, Maven module, directory, or whole-repository scope before starting.
- Audit the current working tree rather than a pull request or diff.
- Remain read-only throughout the audit. Do not edit code, generate patches, run formatting, or change Git or external state.
- Read the [standalone compliance audit workflow](references/standalone-audit.md) through EOF.
- When the scope contains Java, read the [Java naming rules](references/rules/java-naming.md) through EOF.
- When the scope contains Java, read the [Java type rules](references/rules/java-types.md) through EOF.
- When the scope contains a Java declaration with at least two covered declaration annotations, read the [Java declaration annotation order rules](references/rules/java-annotation-order.md) through EOF.
- When the scope contains Java, read the [Java constructor rules](references/rules/java-constructors.md) through EOF.
- When the scope contains Java `throw` statements, conditional exception paths, validation or precondition calls, or calls to `ShardingSpherePreconditions`, read the [Java precondition rules](references/rules/java-preconditions.md) through EOF.
- When the scope contains Java methods annotated with `lombok.SneakyThrows`, read the [Java exception handling rules](references/rules/java-exceptions.md) through EOF.
- When the scope contains Java collection declarations, read the [Java collection declaration rules](references/rules/java-collections.md) through EOF.
- When the scope contains Java expressions that copy `Collections.singleton`, `Collections.singletonMap`, or `Arrays.asList` into `ArrayList`, `LinkedList`, `HashMap`, or `LinkedHashMap`, read the [Java collection rules](references/rules/java-collections.md) through EOF.
- When the scope contains Java, read the [Java expression rules](references/rules/java-expressions.md) through EOF.
- When the scope contains Java, read the [Java line-wrapping rules](references/rules/java-line-wrapping.md) through EOF.
- When the scope contains Java tests, read the [Java test code rules](references/rules/java-testing.md) through EOF.
- When the scope contains implementation artifacts, read the [defensive-code rules](references/rules/defensive-code.md) through EOF.
- Before auditing, run `scripts/build_audit_inventory.py` with the repository root and every user-specified repository-relative scope.
- Check every constrained file and every physical line in scope without sampling.
- Record applicable rules, checked files, checked physical lines, excluded files, and blocked checks.
- Use exactly one of these conclusions: `Strictly compliant`, `Non-compliant`, `Partial audit`, or `Not applicable`.
- Report only confirmed violations of applicable written standards.
- Do not assign severity labels or independently report architecture, caching, lifecycle, ownership, or other semantic-design issues.

This Skill may inspect architecture, ownership, lifecycle, concurrency, and runtime paths only when those facts determine whether an applicable written coding standard is satisfied.
