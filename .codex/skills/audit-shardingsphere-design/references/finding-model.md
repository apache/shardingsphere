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

# Finding Model

## Candidate and Finding

Treat a naming pattern, directory placement, dependency, abstraction, duplicate block, or rule
match as a candidate. Publish it as a finding only when all of these conditions hold:

1. **Observation:** Identify the exact current code, registration, dependency, contract, test,
   or written project rule.
2. **Path:** Trace the relevant caller-to-owner or contract-to-loader-to-implementation path far
   enough to establish responsibility and consequence.
3. **Principle:** Name the concrete module responsibility, SPI contract, dependency direction,
   repository rule, or design principle that the code violates.
4. **Consequence:** Demonstrate current correctness risk, knowledge leakage, packaging risk,
   extension cost, duplicated change points, navigation cost, or misleading test protection.
5. **Counter-evidence:** Inspect the strongest fact that could make the design intentional, such
   as framework requirements, public contracts, another variation, packaging, or nearby owners.
6. **Correction boundary:** Identify the smallest responsibility or dependency that must change.

Reject the candidate when it is only a preference, a theoretical future risk, or a pattern name
without a demonstrated consequence.

## Self-Contained Finding Gate

A confirmed finding must be understandable and actionable without a follow-up question. Explain
the issue in current ShardingSphere terms rather than using a smell label as the conclusion. Every
finding must answer:

1. **Where:** Which concrete class, resource, POM, registration, module, or line owns the problem.
2. **How it is reached:** Which caller, dependency, loader, registration, packaging, or test path
   establishes the behavior.
3. **Why it is wrong:** Which specific responsibility, dependency direction, SPI contract, written
   rule, or maintained project principle is violated.
4. **What it causes:** Which current correctness, maintenance, extension, packaging, navigation, or
   test-protection consequence follows from that path.
5. **What must change:** Which responsibility stays, which concrete artifacts move or change, and
   which behavior must remain verified. Do not invent a target module or hierarchy without
   maintained ownership evidence.
6. **Why the alternative explanation fails:** Which framework, bootstrap, compatibility,
   packaging, closed-set, default-implementation, or nearby-precedent justification was checked.

Define contextual phrases such as `shared module`, `core policy`, `plugin-specific behavior`, or
`wrong owner` through the actual consumers and dependencies. If any answer that could reverse the
conclusion is missing, keep the item as a candidate or evidence limitation rather than a finding.

## Evidence Quality

Prefer evidence in this order:

1. Applicable `AGENTS.md`, `CODE_OF_CONDUCT.md`, public API/SPI contracts, and module POMs.
2. Complete current production paths, registrations, resources, packaging, and tests.
3. Closest maintained precedents with the same variation axis and runtime role.
4. Engineering inference explicitly connected to the observations above.

Do not use one regex match, one direct-reference search, package names alone, or generic design
maxims as sufficient proof. When sources conflict, report the conflict and lower confidence.

## Priority

- **P1:** Confirmed wrong ownership, dependency direction, SPI contract, registration, or plugin
  leakage with broad or immediate correctness, packaging, or extension impact.
- **P2:** Material maintenance or extension cost across multiple types, modules, or change paths.
- **P3:** Localized but non-trivial clarity, consistency, abstraction, or test-design problem.

Priority combines consequence, affected scope, and likelihood. It does not measure effort or
personal preference. Consolidate symptoms that require the same correction into one finding.

## Confidence

- **High:** The full relevant path, violated contract, consequence, and counter-evidence are all
  directly supported by current repository facts.
- **Medium:** The path and consequence are supported, but one non-decisive ownership or runtime
  detail remains inferred.
- **Low:** A missing consumer, runtime, packaging, contract, or intent fact could reverse the
  conclusion. Keep it out of findings and list it only as an evidence limitation when material.

## Removal Boundary

Do not equate absence of direct references with unused code. Before calling an artifact safely
removable, inspect semantic and indirect consumers, including overrides, reflection, generated
code, SPI and `ServiceLoader` registrations, Maven scopes and profiles, test JARs, packaging,
distributions, tests, E2E, and external contracts. Follow the repository's required history and
controlled-removal gates.

Without that evidence, use `suspected unnecessary design` or `purpose unresolved`. Recommend
simplifying the responsibility, not deleting the artifact. Ordinary audits do not investigate
history merely to find old problems.

## Reporting Discipline

- Cite repository-relative paths and tight line locations when available.
- Separate observed facts from inference in the evidence text.
- Explain why the issue matters to the next engineer who changes or extends the code.
- Give a concrete correction boundary, not a patch, new class hierarchy, or speculative target
  architecture. Name affected classes, dependencies, registrations, or resources when the evidence
  requires them.
- Omit generic compliments, exhaustive candidate logs, and findings that failed the proof gate.
