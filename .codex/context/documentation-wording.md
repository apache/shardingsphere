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

# Documentation Wording

Apply this file whenever creating or revising documentation, Skills, prompts, comments, configuration descriptions, or other prose.

- Make each rule understandable to people and Codex on the first reading. State what to inspect, what action to take, what result identifies a problem, and how to verify it; labels such as `authority source`, `constraint strength`, `behavior value`, or `proper ownership` do not replace these instructions.
- State the core rule, fact, or action first. Follow it with necessary applicability conditions, exceptions, and verification methods in that order.
- Use established project technical terms. Do not coin uncommon umbrella terms; when an unavoidable technical term first appears, immediately explain what it means.
- When a supplied established term subsumes several operations, use that term consistently in every rule, exception, and verification statement instead of naming or enumerating only a subset.
- Express one main rule per sentence and do not compress multiple decisions into a difficult long sentence. When one established term fully expresses a requirement, use it instead of enumerating synonymous actions or manifestations.
- Use professional, direct, specific, and concise language. Avoid bureaucratic or academic phrasing, excessive informality, and mechanical shortening that replaces precise technical language with casual wording.
- State each requirement once and merge adjacent statements when one repeats or fully includes another. For Skills, keep the core workflow and reference routing in `SKILL.md`, specific decisions in its referenced files, and concrete rules in `references/rules/`; do not duplicate the same content across files.
- Give every Skill and audit category a distinct, accurately named responsibility. Do not use vague names to hide overlapping responsibilities or duplicate work owned by another specialized Skill.
- Use lists only for genuinely parallel rules, categories, or steps, and use tables only when readers need a column-by-column comparison. Express content that fits in one sentence as prose instead of a field-style list.
- Make each heading name its specific subject and purpose without repeating its parent heading. A title such as `Validation` is insufficient when the section only covers YAML keys. In a short document, omit overview tables, key-point checklists, tables of contents, and repeated summaries that have no independent purpose.
- Keep an exception only when a real allowed case has been verified. State why it is allowed and its exact boundary; do not add speculative or overly broad exceptions.
- Keep an example only when it resolves ambiguity, adds an independent decision condition, or materially helps execution. Otherwise remove it or separate it from the rule.
- Simplification must preserve the original scope, obligation strength, valid exceptions, and verification requirements. Do not shorten mechanically or make readers infer a rule that the original text stated explicitly.
- Keep each new or changed complete sentence on one physical line, and break a line only after punctuation ends the complete sentence. Do not reflow untouched text merely to make formatting consistent. Never modify ASF license text or its internal formatting.
- Before handoff, review every changed prose sentence and rewrite any sentence that does not let a reader directly identify what to inspect, what result is a problem, which exceptions apply, and how to verify it.
