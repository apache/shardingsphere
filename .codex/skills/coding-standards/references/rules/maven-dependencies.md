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

# Maven Dependency Version Rules

## Project Dependency Scope

Apply these rules to project dependencies under a project or profile `<dependencies>` element and outside `<dependencyManagement>` when modifying or reviewing a Maven POM.

- Dependency declarations inside `<dependencyManagement>` are not violations of these rules.
- Maven plugin `<version>` elements are outside these rules.
- Dependencies declared under `<plugin><dependencies>` are outside these rules.

## Managed Dependency Matching

- Match a project dependency to the effective `<dependencyManagement>` by `groupId:artifactId:type:classifier` after model interpolation.
- Treat an omitted `type` as `jar` and an omitted `classifier` as empty when matching.
- Resolve effective dependency management from the current POM, its parent POMs, and imported BOMs for the build context being checked.
- For a dependency declared in a profile, evaluate only the supported activation paths in which that dependency is active.
- Do not use dependency management from a mutually exclusive or otherwise inactive profile to classify a version as redundant.

## Version Declarations

- Omit an explicit project dependency version when the matching effective dependency management supplies the same model-interpolated version value.
- Different property expressions do not justify an explicit version when they resolve to the same value in every supported build context being checked.
- Keep an explicit version when no matching effective dependency management entry supplies it.
- An explicit version that resolves to a different value on a supported build path is not redundant.
- Keep that override only after verifying the compatibility or release contract that requires it.
- A possible future version change does not justify repeating the currently managed version.

## Verification and Inspection Boundaries

- Before removing a redundant version, compare the effective dependency before and after the change for every supported activation path that can affect it.
- Verify that the effective `version`, `scope`, `type`, `classifier`, `exclusions`, and profile behavior are unchanged.
- When an outcome-sensitive management source, property value, or activation path cannot be resolved, preserve the explicit version in Implementation Guidance Mode and mark the affected check as blocked in Standalone Compliance Audit Mode.
- In Implementation Guidance Mode, inspect only project dependency declarations added or modified by the current task and do not scan, clean up, or report unrequested existing violations.
- In Standalone Compliance Audit Mode, inspect every target POM only when the user explicitly requested a Maven standards audit and specified its exact scope.
