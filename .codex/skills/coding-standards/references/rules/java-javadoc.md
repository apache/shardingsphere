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

# Java Javadoc Rules

Apply these writing rules to Java declarations and Javadocs added or changed by the current task and to review of that delta.
In a standalone audit, do not use these task-delta writing rules to demand changes to untouched existing Javadocs; the coverage and contract requirements of `CODE_OF_CONDUCT.md` still apply according to their own scope.

- Keep the Javadoc coverage, complete user-facing API and SPI contracts, explicit nullable-contract documentation, and constructor restrictions required by `CODE_OF_CONDUCT.md`.
- Express those required contracts concisely in the existing summary and standard tags.
  These specific obligations do not justify explanatory paragraphs for ordinary declarations.
- Describe a class or interface with a short responsibility phrase and a method with a short action phrase, using the existing project Javadoc structure.
- A simple description may closely match the declaration name, such as `License check context.`; do not require extra information merely to avoid restating it.
- Keep `@param`, `@return`, and `@throws` descriptions short and accurate, using the existing project format and tag order.
- Do not expand ordinary declaration descriptions into implementation steps, design rationale, usage instructions, scenario lists, per-field explanations, visible collection properties, or default-behavior explanations.
  Do not add generalized claims such as unused fields being nullable merely because the implementation permits them.
- A non-obvious implementation fact does not justify extra Javadoc for an ordinary declaration.
- Do not repeat an unchanged inherited contract; keep a required new obligation on an override concise.
- Do not add paragraph or layout tags such as `<p>` to a simple description.
  Keep useful standard Javadoc tags and `{@code ...}`; this restriction does not prohibit all Javadoc markup.
