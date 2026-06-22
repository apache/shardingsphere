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

# SQL Parser Review Reference

Read this reference before reviewing PRs that touch SQL grammar, SQL visitor classes, parser tests, SQL syntax docs, dialect parser behavior, or parser-generated baseline resources.

## Official Evidence

- Prefer the target database's official SQL reference/manual as first-class evidence.
- When the PR or review comment mentions a concrete SQL syntax form, cite the exact official documentation page that supports that form.
- If official docs do not clearly support the syntax, do not infer support from another dialect, secondary article, parser implementation, or AI-reposted content alone.
- Reject syntax support that cannot be proven from official documentation unless the PR explicitly scopes it as ShardingSphere-specific behavior and maintainers accept that scope.

## Dialect Family

- Build the dialect-family map from repository conventions before judging parser scope.
- If the touched dialect is a trunk parser, inspect affected branch dialect parser files, tests, and docs.
- If the touched dialect is a branch parser, inspect the trunk parser and sibling branch dialects that may share or copy the same logic.
- For each related dialect, decide whether the same root cause exists, whether the PR fixes it, and whether validation or non-applicability evidence is present.
- Do not silently treat unreviewed related dialects as safe.

## Review Questions

- Does the PR preserve precedence, name resolution, and shadowing semantics for adjacent valid cases?
- Would same-name or shadowing cases now take a different path?
- If shared parser code is touched, would another dialect now diverge from its documented semantics?
- Are accepted, rejected, and unsupported syntax boundaries explicit?
- Do parser tests cover each affected dialect or explain why the dialect is not affected?
- Do ShardingSphere docs, examples, release notes, and generated baselines remain consistent with the parser behavior?

## Output Requirements

- In `Reviewed Scope`, name the target dialect, related trunk or branch dialects checked, official documentation pages used, and repo docs or examples checked.
- If syntax evidence is missing or contradicted, classify the result using the main skill's evidence sufficiency rules instead of guessing.
