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

- Test behavior owned by the production class: computation, decisions, validation, transformation, state transitions, error handling, or external contracts.
- Each test must fail for a realistic regression that matters.
- Do not add tests that only prove behavior owned by Java, Lombok, Mockito, a third-party parser, a collection library, a framework, or another collaborator, or that lock in private implementation shape.
- Test a pass-through, constant, accessor, delegation, wiring path, or contract literal only when it expresses documented behavior owned by the production type or a public or externally visible contract that no broader behavior test protects.
- Never add a test solely to increase a coverage number or duplicate an existing scenario unless it covers a new branch, input class, edge case, contract, calculation path, or failure mode.
- Never add tests whose subject is another test case, a test class or method, test fixture, mock helper, test utility, or other test-only code. Test only the production behavior that the test-only code supports. Test-support code distributed as an independent artifact with an external contract is production code for this rule.
- Do not test a collaborator-owned rule through the current class. Isolate the collaborator result at the nearest stable boundary and test the rule in its owner. Mock external or heavy dependencies and collaborator-owned decisions; use real simple values when they are stable and do not introduce cross-layer behavior. Cross-layer behavior belongs in an explicitly scoped integration, contract, or E2E test.
- Give each behavior-owning public production method focused coverage. Each test method covers one scenario and invokes the target public method once by default. Invoke it more than once only when repeated invocation is itself the behavior under test, such as idempotency, accumulation, or a state transition.
- Test interface-owned `default` or `static` methods directly. Test abstract interface contracts through concrete implementations.
- Every new public production type that owns behavior or a public or externally visible contract requires direct focused tests, except exception types covered by `Exception Tests`. Broad workflow tests do not replace them unless they explicitly exercise that type's public behavior or external contract.
- When a coverage target is stated, list every branch or path before coding and map each required branch or path to at least one planned scenario. Each additional case must protect a distinct branch, input class, edge case, contract, calculation path, or failure mode; one scenario may protect multiple paths. Update the map when production code changes and verify the final target with JaCoCo. Obtain explicit user authorization before lowering the stated coverage target.
- If coverage exposes unreachable production code, apply the artifact-removal and contract-impact gate before classifying it as removable. If the required production change is outside the frozen boundary, report the exact blocker and request that scope instead of adding a redundant test.

## Exception Tests

- Do not add a dedicated test for an exception class when it only forwards arguments to a superclass, adds no externally observable contract or owned behavior, and the production error path already protects the forwarded contract. Add direct tests for an owned error code, category, message format, cause conversion, field, validation, branch, calculation, conversion, or known regression.
