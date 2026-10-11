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

# Java Line-Wrapping Rules

Apply these rules to Java declaration headers, statements, and expressions.

## Line-Length Decision

- Count every character in the physical line, including its leading indentation.
- Reconstruct a multi-line declaration header, statement, or expression in normal one-line Java form by removing only layout line breaks and indentation and preserving the tokens and required spaces.
- Keep the reconstructed form on one physical line when it contains no more than 200 characters.
- Wrap the reconstructed form only when it would contain more than 200 characters.
- Do not treat braces and the bodies of classes, methods, control-flow statements, switch rules, or multi-statement lambdas as removable layout line breaks.
- Do not reconstruct comments, Javadocs, text blocks, or string contents in a way that changes their tokens, content, or semantics.
- In Implementation Guidance Mode, apply these rules to every declaration header, statement, or expression added or modified by the task, but do not reformat untouched surrounding code.
- In Standalone Compliance Audit Mode, apply these rules to every Java declaration header, statement, and expression in the exact audit scope.

## Wrapping Forms

- Use the fewest line breaks that keep every physical line within 200 characters, except for the list, builder, and resource forms defined below.
- Keep as much complete content as possible on the preceding line and break only at the applicable syntactic boundary.
- Indent expression continuations by eight additional spaces unless a declaration rule below requires alignment with the first parameter.
- Keep a closing parenthesis, bracket, or statement terminator on the same line as the last item or expression that it closes.
- Wrap method and constructor declaration parameters after a comma, align every continuation line with the first parameter, and keep the closing parenthesis and `throws` clause with the last parameter when that line remains within 200 characters.
- Wrap method and constructor invocation arguments after a comma; break immediately after the opening parenthesis only when the invocation prefix and first complete argument cannot share a line within 200 characters.
- Do not place every invocation argument on a separate line unless the invocation is a list form covered below or an individual argument otherwise cannot fit within 200 characters.
- For a wrapped assignment, keep the left-hand side and `=` on the first line and indent the right-hand side by eight additional spaces.
- For a wrapped fluent invocation, break before `.`, indent the continuation by eight additional spaces, and keep as many complete ordinary chain operations together as the limit permits.
- When a builder chain requires wrapping, one builder operation per continuation line is allowed even when adjacent operations would fit on the same line.
- For a wrapped logical expression, break before `&&` or `||` and place the operator at the start of the continuation line.
- For a wrapped conditional expression, break before `?` and `:` and place each operator at the start of its continuation line.
- For wrapped string concatenation, break before `+`, preserve each string fragment, and place `+` at the start of the continuation line.
- When an outer list expression such as `Stream.of` requires wrapping, keep its opening invocation on the first line and place each top-level list element on its own continuation line.
- If one top-level list element exceeds 200 characters, wrap that element again at its applicable internal syntactic boundary.
- When a try-with-resources declaration requires wrapping, place each resource on its own continuation line.
- Keep an expression lambda on one line when the containing expression remains within 200 characters; use a block lambda only when its body contains statements that require a block.

## Verification

- Use Checkstyle `LineLength` to detect physical Java lines longer than 200 characters.
- Do not treat passing Checkstyle as proof that a multi-line construct needed wrapping or used the required wrapping form.
- Do not treat passing Spotless as proof of compliance because the repository formatter preserves existing wrapped lines.
- For every multi-line candidate in scope, reconstruct and measure its normal one-line form before deciding whether the line breaks are allowed.
- Report a construct whose reconstructed form contains no more than 200 characters as a violation and use the reconstructed single line as the minimum correction.
- When the reconstructed form exceeds 200 characters, verify every physical line, wrapping boundary, continuation indentation, operator position, and closing delimiter against the applicable wrapping form.
