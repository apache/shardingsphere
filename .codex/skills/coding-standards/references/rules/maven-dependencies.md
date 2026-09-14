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

## Shared Version Override Ownership

- In Implementation Guidance Mode, perform this ownership check only when the current task adds or modifies the same explicit dependency version override in multiple child POMs; do not scan historical duplicate overrides.
- Treat overrides as the same only when their model-interpolated `groupId`, `artifactId`, `type`, `classifier`, and version match in every supported activation path.
- Consider a common POM as the ownership candidate only when it is the actual Maven parent of every affected child, not merely an aggregator, and repository structure or existing dependency-management entries prove that it owns dependency version management for those modules.
- Move the shared version to that parent's `<dependencyManagement>` only when the parent POM and every child declaration changed by the consolidation are within the task's authorized scope.
- Before moving the version, compare the effective POM before and after for every affected supported activation path and prove that the dependency's version, scope, type, classifier, exclusions, and profile behavior do not change.
- Inspect every other supported consumer that inherits the parent and prove that the consolidation causes no unintended effective dependency change.
- Keep the overrides in their child POMs when their complete coordinates or versions differ, when they implement different compatibility or profile contracts, when ownership is not proven, when the effective model cannot be verified completely, or when a required parent or child change is outside the authorized scope.
- Do not expand the current task to a parent POM or another child module only to centralize a version.
- Report a possible consolidation as out of scope when it cannot be completed within the authorized boundary, and do not classify the unchanged out-of-scope declarations as a current-task violation.
- In Standalone Compliance Audit Mode, report a shared-version-ownership violation only when the user-specified audit scope includes the common parent POM and the affected child POMs and complete evidence proves every consolidation condition above.

## Task-Caused Unused Version Properties

- When the current task modifies or removes a project dependency version declaration, inspect only a version property that may have lost its last consumer because that task removed a reference.
- Delete such a property only when repository search and the Maven effective model both prove that no child-module override, profile, plugin, resource-filtering path, or other consumer remains.
- Do not use this check to scan, report, or remove unrelated existing version properties.
- When evidence about any possible consumer is incomplete, preserve the property in Implementation Guidance Mode and mark the affected check as blocked in Standalone Compliance Audit Mode.

## Verification and Inspection Boundaries

- Before removing a redundant version, compare the effective POM before and after the change for every supported activation path that can affect it.
- Use the effective POM to confirm each affected dependency's complete `groupId:artifactId:type:classifier` coordinate and final dependency management.
- Verify that the effective `version`, `scope`, `type`, `classifier`, `exclusions`, and profile behavior are unchanged.
- When a change may affect a resolved dependency version or the transitive dependency graph, compare the dependency tree before and after the change.
- Do not require a dependency-tree comparison for every POM change; use it only when the result depends on the actual resolved dependency graph.
- Do not use the dependency tree as a substitute for the effective POM, supported profile activation paths, or complete `groupId:artifactId:type:classifier` matching.
- When an outcome-sensitive management source, property value, or activation path cannot be resolved, preserve the explicit version in Implementation Guidance Mode and mark the affected check as blocked in Standalone Compliance Audit Mode.
- In Implementation Guidance Mode, inspect only project dependency declarations added or modified by the current task and do not scan, clean up, or report unrequested existing violations.
- In Standalone Compliance Audit Mode, inspect every target POM only when the user explicitly requested a Maven standards audit and specified its exact scope.
