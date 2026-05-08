<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Quickstart: Judge MCP AI-Native Perfect 100

## Purpose

Use this guide when deciding whether `shardingsphere-mcp` has reached the strict `100/100` state for LLM-native usability.

## Step 1: Confirm Branch and Scope

```bash
git branch --show-current
```

Expected branch:

```text
001-shardingsphere-mcp
```

Do not switch branches for this feature unless the user explicitly changes the constraint.

## Step 2: Read the Requirement Package

Read these files in order:

1. `specs/005-mcp-ai-native-perfect-100/spec.md`
2. `specs/005-mcp-ai-native-perfect-100/requirements.md`
3. `specs/005-mcp-ai-native-perfect-100/checklists/requirements.md`
4. `specs/005-mcp-ai-native-perfect-100/tasks.md`

The `100` decision must use these requirements, not intuition.

## Step 3: Inspect Evidence

Required evidence after implementation:

- Contract tests for P0 response mode, next actions, continuation, completion recovery, and empty-state recovery.
- P1 tests or smoke evidence for readiness, runtime visibility, provenance, redaction, lexicon, terminology, and local MCP client flow.
- P2 lint or artifact evidence for correlation id, packaging metadata hints, descriptor linting, and scorecard maintenance.

## Step 4: Run Narrow Verification

Minimum documentation check for this package:

```bash
git diff --check
find specs/005-mcp-ai-native-perfect-100 -type f | sort
```

Recommended implementation checks:

```bash
./mvnw -pl mcp -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp -am -Pcheck checkstyle:check
```

If the MCP module path is different in the repository, use the actual module path that owns the MCP bootstrap and contract tests.

## Step 5: Apply the Score Gate

Answer `100/100` only when all are true:

- Every P0 requirement is passing.
- Every P1 requirement is passing.
- Every P2 requirement is passing.
- The maintained scorecard lists evidence for every passing gate.
- No new in-scope optimization is visible after repeating the review question against the current code and docs.

If any required item is missing, answer below `100` and name the missing gates.

## Canonical Final Answer After Completion

When every gate passes and no in-scope gap remains, the repeated review answer must be:

```text
100/100. 在当前定义的 MCP 原生易用性范围内，没有已知可优化空间。
```
