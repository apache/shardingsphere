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

# Docker Resources

Apply these rules before direct Docker commands and any build, Maven test, E2E, IDE run, Compose or Testcontainers operation that may pull images or create Docker resources.

## Image Selection and Reuse

- Inspect existing image versions, platforms, actual dependencies and artifact freshness before pulling or building, and reuse an image that satisfies the current verification requirements.
- Record the concrete verification need for each additional image before acquiring it; convenience, standby use and unverified guesses do not justify another image.
- Apply these checks to explicit pulls and automatic acquisition through `FROM`, `run`, Compose, Testcontainers and other build or test entry points.
- Pull another architecture only for a concrete test requirement, and record its release version, platform, digest and retention purpose.
- Distinguish multi-platform index digests, platform manifest digests and local image IDs, and associate each recorded digest with its type and selected platform.
- For a locally built image without a registry digest, record its local image ID and verified build source; do not pull an image solely to obtain a digest.
- For serial local tests, use the repository's existing standard image names with the `latest` tag for locally built test artifacts instead of inventing names or per-run tags.
- Preserve existing external-dependency and CI image tags unless they conflict with the established target version for the same dependency and major-version line.
- Use one pinned image version across all test entry points for the same external test dependency and supported major-version line.
- Follow the target version explicitly required by the current task or established repository tests; resolve an unclear or conflicting target before selecting an image.
- Do not use a different version, a floating tag, or an omitted tag to satisfy that dependency and major-version line.
- Select only the platforms required by the current test while keeping the pinned release version.
- A shared major version or release version does not prove that full and slim variants are interchangeable.
- Before execution, inspect the final effective test image selection.
- Correct a conflicting selection only within the current task's authorized scope, before starting the test.
- If the conflicting selection is outside that scope, report the difference without changing other entry points or starting the test.
- Do not relabel or substitute an image to present another dependency version as the required version.
- During actual runtime verification, verify both the selected image and the dependency's actual version; an image tag alone does not prove the version.
- Unifying an external test dependency image within one major-version line does not authorize changes to other dependencies or major-version lines.
- Reuse an existing image only after proving that it contains the current artifacts being verified; the presence of `latest` alone is not proof.
- When artifacts change or freshness cannot be proved, rebuild current artifacts and update the same `latest` tag through the repository's standard build flow.

## Resource Records and Parallel Tasks

- Before operations, record existing image IDs and tags and relevant containers so task-added, pre-existing and unowned resources remain distinguishable.
- Record each task-added image, tag and temporary container with its image or container ID.
- For every recorded resource, include its purpose, reproducible source and retention reason, and mark information that cannot be verified as unknown.
- For pulled images, also record the release version, platform and digest.
- Each parallel subtask must report its resource delta to the main task, which must check combined ownership and use and complete the final resource checks and cleanup.
- A subtask finishing does not establish that its resources have been cleaned.
- When parallel local tests require independent tags, explain the isolation need and plan cleanup before using them so that tasks do not contend for `latest`.

## Replacement, Cleanup, and Recovery

- Record the old image ID, if present, before rebuilding.
- Verify the new image ID after a successful build, and do not delete it when it equals the old ID.
- Retain the original usable image if the build fails.
- After a successful replacement, remove the recorded old image only after confirming that it has no other tags, no container dependencies, no other use, and a verified rebuild source.
- Check all containers, including running and stopped containers, when verifying image dependencies.
- An untagged `<none>:<none>` listing does not prove that an image is unused, and overwriting `latest` does not remove the old image.
- Apply the same tag, dependency, other-use, and rebuild checks to task-created temporary images, and handle them after build or test success or failure.
- Keep the standard `latest` image intended for reuse.
- For any image that cannot be safely removed, report its ID, retention reason, and conditions for later cleanup.
- For task-added tags or temporary containers left in place, also report their associated image or container IDs, retention reasons and conditions for later cleanup.
- Inspect recorded resources after success, failure or an explicit stop and before delivery.
- Before delivery, remove task-added temporary images only when they are no longer needed, cleanup is within current task authority and their pull or build source has been verified.
- When resuming a task after an unexpected interruption, inspect leftover Docker resources before continuing.
- Before deletion, trace configured build and runtime references, derived images and confirmed parallel-task or reuse needs using recorded digests and local image IDs.
- Image age or the absence of containers also does not prove that an image is unused.
- Do not remove resources that existed before the task or whose ownership is unknown, except a recorded old image successfully replaced by this task that passes all replacement cleanup checks above.
- Do not request repeated confirmation for necessary Docker operations already authorized by the current task.
- Image cleanup does not authorize deleting containers, networks, volumes or data.
- Limit deletion to image IDs confirmed for this task, using `docker image rm --no-prune` without force to avoid removing unselected parent images.
- Do not run global prune or extend image cleanup to other tasks' images, containers, volumes, or data.
