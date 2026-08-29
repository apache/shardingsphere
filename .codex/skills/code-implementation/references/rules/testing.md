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

# Testing Rules

- Test behavior owned by the production class: computation, decisions, validation, transformation, state transitions, error handling, or external contracts. Each test must fail for a realistic regression that matters. Do not add tests that only prove pass-throughs, constants, accessors, delegation, wiring, Java, Lombok, Mockito, parsers, collection libraries, framework behavior, or private implementation shape. A documented public or externally visible contract may justify testing pass-through behavior; contract literals are exceptions only when no broader behavior can protect them. Never add a test solely to increase a coverage number or duplicate an existing scenario unless it covers a new branch, input class, edge case, contract, calculation path, or failure mode.
- Never add tests whose subject is another test case, a test class or method, test fixture, mock helper, test utility, or other test-only code. Test only the production behavior that the test-only code supports. Test-support code distributed as an independent artifact with an external contract is production code for this rule.
- Do not test collaborator rules through the current class. Mock the nearest stable boundary and test the collaborator rule in its owner. Cross-layer behavior belongs in an explicitly scoped integration, contract, or E2E test.
- Give each behavior-owning public production method focused coverage. Each test method covers one scenario and invokes the target public method at most once; repeat only when the same scenario requires additional assertions. Do not create interface-only tests; exercise concrete implementations.
- Every new public production type requires direct focused tests, except exception types covered by `Exception Tests`. Broad workflow tests do not replace them unless they explicitly exercise that type's public behavior. If a pure pass-through public type has no meaningful owned or external contract to test, do not add the type merely for structural completeness.
- When a coverage target is stated, list every branch or path before coding and map each one to exactly one planned test. Add cases until every declared branch is covered or explicitly waived, update the map when code changes, and verify with JaCoCo when coverage is uncertain. Document unreachable code instead of adding redundant tests.

## Exception Tests

- Do not add dedicated tests for exception classes that only declare constructors or format and forward their arguments to a tested superclass. Add direct tests only for owned validation, branching, calculation, conversion, or a known regression.
