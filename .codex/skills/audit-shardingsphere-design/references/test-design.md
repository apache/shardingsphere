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

# Test Design

## Purpose

Judge whether tests protect meaningful production-owned behavior and its boundaries. Do not turn
coverage percentage, test count, or conformance to a preferred test shape into a design finding.
Apply the rule-applicability gate in `finding-model.md` before using `CODE_OF_CONDUCT.md` or an
applicable `AGENTS.md` test rule against existing tests.

## Capability Areas

### Behavioral Value

Confirm that each test can fail for a realistic regression in computation, decisions, validation,
transformation, state transitions, error handling, or an external contract owned by the production
subject. Look for tests that only prove constants, accessors, delegation, wiring, Java, Lombok,
Mockito, a parser library, or private implementation shape.

Do not report a simple test merely because it is small. Demonstrate that it protects no owned or
externally visible behavior, duplicates another scenario, or asserts the collaborator instead of
the subject.

### Ownership and Boundaries

Check that tests exercise behavior through the nearest owning public API. Collaborator rules
belong in the collaborator's tests; cross-module behavior belongs in an explicitly scoped contract,
integration, or E2E test. Flag production visibility, constructors, hooks, or abstractions added
only to make tests convenient.

Require precise scenarios and assertions at important correct, boundary, and error paths. Do not
recommend testing every branch when a branch is only framework wiring or cannot represent a
meaningful regression.

### Isolation and Mocks

Check whether tests remain automatic, independent, and repeatable. Mock heavy external boundaries
and unrelated deep object graphs, but do not mock away the production decision being tested. Look
for fragile call-order assertions, deep setup for unrelated collaborators, shared mutable state,
environment dependencies, and static resources that are not released.

Prefer direct mocks and the nearest stable boundary. Treat a helper or fixture as a smell only
when it creates a test-only API, hides the scenario, duplicates thin delegation, or crosses module
ownership for convenience.

Do not report an allowed, correctly closed direct static or construction mock merely because
`AutoMockExtension` is preferred for newly changed tests. If the repository-wide test standard
permits direct mocking with try-with-resources or explicit cleanup and the test satisfies that
lifecycle, require an independent leak, isolation failure, behavioral distortion, or material
maintenance consequence before publishing a finding. A possible mechanical migration or a few
local setup lines are not such a consequence.

### SPI and Plugin Tests

Obtain `TypedSPI` and `DatabaseTypedSPI` implementations through the project loader by default.
Check registration, type selection, properties, failure contracts, and the concrete behavior owned
by each plugin. Do not accept interface-only tests or direct construction when it bypasses the
loading contract the production path relies on.

Do not require a dedicated test for a pass-through plugin type with no owned behavior merely for
structural symmetry. If the type has no meaningful contract to test, question the production type
before adding a coverage-only test.

## Reporting

Tie every test finding to the production regression that can currently escape or to the production
design distorted for testing. Complete the test entry, setup, invocation, assertion, owner, and
counter-evidence analysis internally. Publish only the final diagnosis and remediation conclusions
required by `SKILL.md`; do not output the test-path walkthrough or candidate analysis. Keep explicit
naming, assertion, Mockito, and JUnit rule violations separate from semantic test-design findings.
