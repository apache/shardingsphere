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

# Performance Budget

This checkpoint uses lightweight budgets to catch obvious regressions without
turning unit tests into benchmarks. The budget tests are smoke guards, not
absolute performance certification.

## Production Budgets

`MCPPerformanceBudgetSmokeTest` enforces these local budgets:

- Descriptor generation: 100 capability payload generations must finish within
  5000 ms.
- Request scope creation: 200 request scopes must finish within 5000 ms.
- Metadata lookup: 100 `search_metadata` calls over the in-memory test metadata
  fixture must finish within 5000 ms.
- SQL classification: 1000 read-only SQL classifications must finish within
  5000 ms.

Required command:

```bash
./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true \
  -Dtest=MCPPerformanceBudgetSmokeTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

## E2E Lane Budgets

Budgets are measured at the Maven command level because these lanes include
transport startup, Docker readiness, packaged runtime setup, or live model
interaction.

- Default MCP plus MCP E2E lane:
  budget 5 minutes; current evidence `EV-009` finished in 3:22.
- STDIO production lane:
  budget 10 minutes; current evidence `EV-015` finished in 8:25.
- MySQL HTTP plus STDIO lane:
  budget 5 minutes; current evidence `EV-016` finished in 2:55.
- Distribution assembly:
  budget 2 minutes; current evidence `EV-017` finished in 15.463 s.
- Packaged HTTP, STDIO, and plugin smoke:
  budget 1 minute; current evidence `EV-018` finished in 6.124 s.
- Live LLM smoke plus usability lane:
  budget 45 minutes; current evidence `EV-019` finished in 32:12.

## Resource Boundaries

- Live LLM runs are opt-in and use Docker-backed Ollama by default.
- MySQL runs are opt-in and use Docker-backed `mysql:8.0.36`.
- Packaged distribution runs are opt-in and reuse the assembled local package.
- The MCP runtime itself does not call an external model provider; model calls
  are performed only by the E2E client harness.
