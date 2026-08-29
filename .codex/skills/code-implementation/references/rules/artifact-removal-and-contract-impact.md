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

# Artifact Removal and Contract Impact Rules

## Unused and Removal Conclusions

Apply this gate to every analysis or change that classifies code, dependencies, configuration, resources, test support, or another repository artifact as unused or removable.

1. After a current-task edit removes or replaces a production call, behavior owner, registration, loading path, or package path, inspect the affected symbols for remaining production consumers. If only test consumers remain or no production consumer is found, apply every requirement in this gate. Compilation, passing tests, coverage, and a wrapper's dedicated test do not establish production use.
2. Distinguish absence of direct references from complete unused evidence. A single text or regex search, or a production-only search, proves at most that no direct reference was found and cannot justify a removal recommendation.
3. Inspect semantic and indirect consumers appropriate to the artifact, including reflection, generated code, registrations, SPI and `ServiceLoader`, JDBC driver discovery, Maven scopes, profiles, plugins and transitive dependencies, build/test/runtime classpaths, test JARs, packaging and distributions, tests, E2E, and external consumers.
4. Inspect Git history and linked issue, pull-request, CI, and failure evidence for prior additions, removals, and restorations. A prior removal failure makes the artifact indirectly required unless same-boundary evidence proves that the failure was unrelated or that the dependency is obsolete.
5. Classify each examined artifact as directly used, indirectly required, purpose unresolved, or a verified removal candidate. Use purpose unresolved when evidence is incomplete. Use verified removal candidate only after a controlled removal experiment, or equivalent existing evidence, covers the affected compilation, tests, packaging, and runtime paths.
6. When the audit verifies that the current task removed the last production consumer and no compatibility contract remains, apply the single-model convergence rule in `.codex/skills/code-implementation/references/rules/implementation.md`. Remove superseded production code and dedicated tests only when the frozen boundary and required file-deletion and public-contract authority permit it. Otherwise, stop, report the exact removal scope and compatibility impact, and request authorization. Keep a candidate that pre-existed the task read-only and report it instead of performing drive-by cleanup or mechanically relocating it as active code.

## Contract and Impact Gates

- For a public or externally visible identifier, search all affected reference surfaces: APIs, SQL, configuration and YAML keys, SPIs, errors, CLI commands, resources, documentation, examples, distributions, tests, E2E, and baselines. Exclude `.git` and `target`. If a compatibility alias remains, state whether it is discoverable; if it must stay hidden, protect that contract with a focused test or check.
- Name the affected database engines and dialects and preserve backward compatibility for their supported versions unless an exact compatibility break is authorized.
- For errors, logs, HTTP or JSON payloads, CLI output, and exception conversion, test the complete external output when it could expose credentials, tokens, connection strings, SQL, paths, or user data.
- Regenerate or verify affected snapshots, golden files, fingerprints, SQL cases, descriptors, schemas, and agent-visible metadata with the existing project tool.
- Determine affected GitHub Actions from changed-file path filters and job commands. Run the local equivalent when practical; otherwise record the narrower check and residual risk. Do not update remote workflow state.
- When runtime paths change, record relevant engine or dialect compatibility and a performance baseline or guardrail. An absolute guardrail supplements but does not replace the comparable pre-change baseline when `.codex/skills/code-implementation/references/rules/non-regression.md` requires one. When governance, registry, observability, or agent integrations are touched, state their impact.
