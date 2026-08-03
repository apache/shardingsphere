---
name: audit-shardingsphere-design
description: >-
  Audit an Apache ShardingSphere module or the entire repository for evidence-backed
  design problems without changing code; default to the entire repository when no scope is
  provided. Use for module responsibility and dependency direction, every kind of SPI/plugin
  implementation including database, feature and algorithm plugins, coupling and extensibility,
  over-design and duplication, repository coding standards and style, or production and test
  design. Do not use for PR/diff review or implementation.
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

# Audit ShardingSphere Design

## Objective

Find current, material design problems that an experienced ShardingSphere engineer can
verify and act on. Review a named Maven module or the complete repository. Include production
and test code by default. When the invocation provides no scope, review the complete repository
without asking for one. Explain the minimum correction direction, but never modify code or produce
a patch.

## Boundaries

- Review the current source tree, not a PR or diff.
- Do not edit files, install Skills, or require another Skill at runtime.
- Do not audit historical problems. Use history only when the repository instructions require
  it to verify a removal claim or when current evidence cannot establish a contract.
- Do not report compatibility and evolution as a standalone category. Discuss compatibility
  only as the impact of a current public API, SPI, loading, registration, or configuration issue.
- Treat every production SPI implementation as a plugin. Do not assume all plugin categories
  have the same ownership or placement rule.
- Do not call code safely removable without the complete evidence required by repository rules.

## Required References

Read these files directly from this Skill before classifying findings:

- Always read [finding-model.md](references/finding-model.md) for proof, priority,
  confidence, and output rules.
- Always read [design-smells.md](references/design-smells.md) for responsibility,
  coupling, complexity, duplication, and style criteria.
- Read [spi-plugin-design.md](references/spi-plugin-design.md) whenever the scope contains
  an SPI declaration, implementation, loader, registration, or plugin consumer.
- Read [test-design.md](references/test-design.md) when tests exist in the selected scope.

## Workflow

1. Resolve the scope as one or more Maven modules or the entire repository. If the user provides
   no scope after invoking the Skill, select the entire repository immediately and do not ask a
   clarifying question. If the user gives a file or package, map it to its owning module and include
   the minimum consuming modules needed to judge ownership. For an aggregator or the whole
   repository, enumerate leaf Maven modules and review them in bounded partitions; account for
   every leaf module rather than substituting a sample. Do not switch to PR scope.
2. Read the applicable `AGENTS.md`, `CODE_OF_CONDUCT.md`, module POMs, registrations, and the
   closest maintained production and test precedents. Treat written rules as authority and
   nearby code as evidence of project idiom, not as automatic proof that a pattern is good.
3. Map each examined behavior to its owner, callers, dependencies, extension axis, SPI contract,
   implementations, registrations, packaging path, and tests. Trace cross-module behavior end
   to end before judging placement or coupling.
4. Discover candidates across all applicable capability groups:
   - module responsibility and dependency direction;
   - SPI/plugin contract, placement, isolation, loading, and category-specific design;
   - coupling, knowledge leakage, and extension cost;
   - unnecessary conceptual surface, indirection, duplication, and misplaced reuse;
   - explicit coding rules plus the project's readability, simplicity, consistency, and
     abstraction principles;
   - production-owned test behavior, boundaries, mocks, and SPI loading.
5. Apply the proof gate in `finding-model.md`. Inspect the strongest counter-evidence. Reject
   checklist matches that do not demonstrate a real consequence in this codebase.
6. Consolidate findings that share one root cause and correction boundary. Rank findings by
   impact, scope, and confidence; do not rank by how easy they are to describe.
7. Return the report defined below. If no candidate passes the proof gate, say that no confirmed
   design problem was found and list any material evidence limitation without inventing findings.
   If an aggregate scope cannot be completed, return `Review incomplete`, preserve confirmed
   findings as partial facts, and identify the unreviewed modules; never present partial coverage
   as a complete repository or module conclusion.

## Judgment Rules

- Prefer repository contracts and actual dependency paths over package-name intuition.
- Separate an explicit standards violation from a semantic style problem. Prove semantic style
  problems through reduced readability, inconsistent abstraction, needless concepts, or a
  maintained precedent that better expresses the same responsibility.
- Treat one implementation, one caller, a wrapper, a factory, or an interface as a signal only.
  Report over-design only when the concept lacks a real stable boundary and adds demonstrated
  navigation, state, dependency, testing, or extension cost.
- Distinguish useful reuse from forced unification. Share stable semantics owned by one module;
  keep real database, feature, or algorithm differences inside their plugins.
- Treat unverified indirect use as unresolved. Inspect registrations, `ServiceLoader`, project
  loaders, reflection, generated resources, Maven scopes, tests, packaging, and runtime consumers
  before making unused or removal claims.
- Keep optional external Skills outside the decision path. Their availability must not change
  the scope, proof standard, or output.

## Output Contract

Start with the reviewed scope and a one-sentence conclusion. Then list confirmed findings in
priority order. Use this shape for every finding:

```markdown
### [P1|P2|P3] Concise problem title

- Location: repository-relative file and line, or the smallest responsible module
- Capability: one capability group from the workflow
- Evidence: observed code path, dependency, registration, rule, and relevant counter-evidence
- Problem: the violated responsibility, contract, or project principle
- Impact: current maintenance, extension, correctness, packaging, or test consequence
- Direction: the smallest correction direction without patch content
- Confidence: High or Medium, with the reason
```

Use `P1` for a confirmed wrong ownership or contract that creates broad or immediate risk, `P2`
for a material extension or maintenance cost, and `P3` for a localized but non-trivial clarity,
consistency, or test-design problem. Do not publish low-confidence candidates as findings.

End with:

- `Coverage`: capability groups and modules actually examined.
- `Not findings`: important candidates rejected by counter-evidence, only when this helps prevent
  the same false positive from recurring.
- `Evidence limits`: facts that could materially change the conclusion.

Keep the report concise. Do not add a generic checklist, praise unaffected code, or recommend a
large redesign when a smaller ownership correction addresses the evidence.

## Completion Gate

Before returning the report, verify that:

- every finding traces a complete relevant path and cites current repository evidence;
- every finding identifies a concrete consequence and minimum correction boundary;
- every leaf module in an aggregate scope is accounted for, or the result is explicitly incomplete;
- SPI findings apply the correct category profile rather than a universal placement rule;
- style findings distinguish written rules from engineering judgment;
- test findings protect production-owned behavior rather than coverage appearance;
- no finding depends on another Skill, a single regex match, or an unverified deletion premise;
- the report covers every applicable capability group and contains no patch or source edit.
