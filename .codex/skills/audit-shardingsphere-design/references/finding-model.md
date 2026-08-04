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

## Candidate Diagnosis and Proof Gate

Treat a naming pattern, directory placement, dependency, abstraction, duplicate block, or rule
match as a candidate. Complete the diagnosis below internally before confirming it. Do not turn
these reasoning steps into report fields or expose the candidate analysis in the final report.

1. **Current owner:** Identify the concrete class, resource, POM, registration, module, or test that
   owns the observed behavior.
2. **Owned responsibility:** Identify the decision, knowledge, state, rule, variation, or production
   behavior that changes independently.
3. **Boundary failure:** Prove why the current owner should not own that responsibility, or why an
   abstraction, duplicate owner, style choice, or test design violates its concrete responsibility,
   contract, dependency direction, written rule, or maintained project principle.
4. **Change propagation:** Trace the caller, dependency, loading, registration, packaging, or test
   path far enough to demonstrate current correctness risk, knowledge leakage, packaging risk,
   extension cost, duplicated change points, navigation cost, or misleading test protection.
5. **Correct boundary:** Determine what stays, what moves or changes, and which owner should carry
   the responsibility. Prove current ownership wrong independently from whether the correct owner
   already exists. Do not invent a target module name or hierarchy. When no existing module can own
   the behavior without violating responsibility or dependency direction, require current
   variation and packaging evidence that a new boundary is necessary; describe its responsibility
   and variation axis, and cite maintained precedents when available.
6. **Counter-evidence:** Check the strongest framework, bootstrap, compatibility, packaging,
   closed-set, default-implementation, or nearby-precedent explanation that could justify the
   current design.

Use the category-specific reasoning in the other references rather than forcing every problem into
an ownership smell. For an explicit written-rule violation, prove the exact rule and violation. For
a test finding, prove the realistic production regression that can escape. If any fact could
reverse the diagnosis, keep the item as an internal candidate and do not publish it.

Confirm the finding only when all six steps are supported by current repository evidence. Reject
preferences, theoretical future risks, pattern names without a demonstrated consequence, and any
candidate whose counter-evidence justifies the current design.

## Rule-Applicability Gate

Apply this gate before using a written rule to classify existing code as non-compliant:

1. Identify the exact requirement, prohibition, permission, exception, and verification obligation;
   do not infer a prohibition from a preference.
2. Identify the rule's explicit subject and scope, such as all repository code, contributions,
   newly created code, changed or touched code, an effective candidate, or Codex's own actions.
3. Confirm that the reviewed artifact and its current state are inside that scope. A rule that tells
   Codex how to implement or review a future change does not by itself establish that untouched
   existing code is defective.
4. Check higher-authority and repository-wide sources for an explicit permission or alternative
   compliant form. A stricter workflow preference for future changes does not override an allowed
   existing form outside that preference's stated scope.
5. If the rule does not govern the current artifact, retain it only as a constraint on the proposed
   correction. Publish a finding only when an independently proven semantic consequence passes the
   ordinary proof gate.

Treat `AGENTS.md` as authority for how the audit is performed. Treat an individual `AGENTS.md` rule
as evidence against existing source only when that rule's wording applies to existing source, not
merely to code Codex creates, changes, or hands off. When source applicability or precedence remains
ambiguous, reject the standards candidate rather than broadening the rule.

## Evidence Quality

Prefer evidence in this order:

1. Written rules that pass the rule-applicability gate, public API/SPI contracts, and module POMs.
2. Complete current production paths, registrations, resources, packaging, and tests.
3. Closest maintained precedents with the same variation axis and runtime role.
4. Engineering inference explicitly connected to the observations above.

Do not use one regex match, one direct-reference search, package names alone, or generic design
maxims as sufficient proof. Treat a decisive source conflict as unresolved and do not confirm the
finding until the conflict is resolved.

## Priority

- **P1:** Confirmed wrong ownership, dependency direction, SPI contract, registration, or plugin
  leakage with broad or immediate correctness, packaging, or extension impact.
- **P2:** Material maintenance or extension cost across multiple types, modules, or change paths.
- **P3:** Localized but non-trivial clarity, consistency, abstraction, or test-design problem.

Priority combines consequence, affected scope, and likelihood. It does not measure effort or
personal preference. Consolidate symptoms that require the same correction into one finding.

## Confidence

- Use confidence only for the internal publication decision; never include it in the report.
- **High:** The full relevant path, violated contract, consequence, and counter-evidence are all
  directly supported by current repository facts.
- **Medium:** The path and consequence are supported, but one non-decisive ownership or runtime
  detail remains inferred.
- **Low:** A missing consumer, runtime, packaging, contract, or intent fact could reverse the
  conclusion. Keep it internal and do not publish it as a finding.

## Remediation Assessment

Assess remediation only after consolidating symptoms with the same root cause and correction
boundary. The assessment does not affect priority or whether the finding passes the proof gate.

Derive the minimum change scope from the confirmed correction boundary. Name only work required by
current evidence across production owners and callers, Maven dependencies, SPI contracts and
registrations, configuration and resources, distribution packaging, compatibility work, and tests.
Do not inflate the scope with adjacent cleanup or count every indirect consumer as a required edit.

Estimate development effort as a numeric person-day range for one engineer familiar with
ShardingSphere. Include implementation, dependency and registration changes, required tests, and
local verification. Exclude review wait time, CI queues, release work, external-team coordination,
and redesign not required by the finding. Estimate work packages rather than converting file or
module counts directly into days. Use the lower bound for confirmed work and the upper bound for
evidence-backed uncertainty. State the assumption that materially controls the range, widen the
range when necessary, and never replace it with false single-point precision.

Estimate each consolidated finding once. Do not sum findings with overlapping correction boundaries
unless the user explicitly asks for a deduplicated total estimate.

## Removal Boundary

Do not equate absence of direct references with unused code. Before calling an artifact safely
removable, inspect semantic and indirect consumers, including overrides, reflection, generated
code, SPI and `ServiceLoader` registrations, Maven scopes and profiles, test JARs, packaging,
distributions, tests, E2E, and external contracts. Follow the repository's required history and
controlled-removal gates.

Without that evidence, classify the candidate internally as `suspected unnecessary design` or
`purpose unresolved` and do not publish a removal finding. Ordinary audits do not investigate
history merely to find old problems.

## Reporting Discipline

- Publish only the final diagnosis and remediation conclusions defined by `SKILL.md`.
- Make the title state the root problem and make the diagnosis paragraph state the current concrete
  consequence. Do not narrate how the conclusion was reached.
- Cite tight repository-relative locations that let an engineer verify the conclusion without
  reproducing the internal evidence chain.
- Give a concrete correction boundary, not a patch, new class hierarchy, or speculative target
  architecture.
- Do not publish capability tags, evidence chains, path walkthroughs, counter-evidence checks,
  confidence, candidate logs, rejected candidates, or generic compliments.
