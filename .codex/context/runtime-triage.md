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

# Runtime Triage

- For Proxy startup, prefer the existing IDE/MCP `Bootstrap` configuration or a scoped `proxy` package with explicit upstream modules. Record the configuration path, mode, ports, command, and exit code.
- For JDBC smoke tests, use a current-source IDE/MCP run or a focused `jdbc` module test with explicit upstream modules and datasource setup.
- Keep standalone `server.yaml` and affected cluster `mode/` configuration behavior aligned; call out default changes.
- For startup, routing, or runtime failures, inspect `proxy/logs/` and relevant `target/surefire-reports`; correlate decisive lines with configuration, metadata freshness, parser dialect, and the owning data-flow step. For routing failures, also inspect feature-rule configuration and report the SQL, relevant configuration, owning module, and focused test. Do not edit generated output.
- For a sandbox or network denial, report the command, failure, and safe alternative or required authorization.
- When an E2E, integration, client smoke, or Docker smoke fails, hangs, or times out, stop rerunning. Classify it as environment, classpath, stale snapshot, dependency, test design, protocol implementation, data setup, assertion logic, or external-service behavior, then record evidence and minimum fix scope before changing code or configuration. Complete deterministic prerequisites such as classpath consistency, stale-bytecode checks, dependency alignment, or required evidence capture before running one matching sentinel; if it fails unexpectedly, return to analysis.
