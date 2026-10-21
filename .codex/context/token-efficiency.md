# Token Efficiency Rules

Use this file before Maven, E2E, Proxy startup, database clients, IDE or MCP run configurations, commands that may exceed 100 output lines, or large structured analysis. Reuse the exact file during the same session unless it changed.

## Command Contract

Before execution, determine the command's writes, network use, authority, platform approval, output destination, timeout, success evidence, and output risk.

- `Must Wrap`: output may be large, streaming, repeated, failure-prone, or over 100 lines; use the wrapper below.
- `May Run Raw`: output and failure output are known to be small and bounded.
- `Unsure`: treat as `Must Wrap`.

Maven build, test, package, install, verification, and plugin goals are usually `Must Wrap`. E2E, shell-launched services, unbounded database or metadata queries, broad log searches, and commands that may emit dependency logs or stack traces are also `Must Wrap`. Version and help commands, `git status --short`, exact-path searches, and explicitly limited queries may run raw.

For `Must Wrap`, keep full output out of the conversation and expose only the command, exit code, log path, and one filtered success line or focused failure excerpt. Redact secrets, credentials, private addresses, personal data, and undisclosed vulnerability details.

When a required command needs network access that the current sandbox is known to deny, invoke its command-bound platform approval with the first execution attempt. Do not make a knowingly blocked trial run, and do not repeat already granted task authority when requesting platform approval.

After a nonzero exit, timeout, or missing expected result, inspect the exit code and the smallest relevant stdout, stderr, event, result, and summary evidence before deciding the next action. If an expected output directory is empty, inspect the parent command result and launcher stderr. Classify the cause as syntax, permission or sandbox, network or external service, timeout, runner or tool, or candidate behavior. Until evidence supports that classification, do not rerun the command unchanged or in parallel and do not edit the candidate, canary, or runner to hide the failure. Retry only after correcting the cause, and report it when the same cause repeats.

## Canonical Shell Wrapper

Redirect both streams to a temporary log, preserve the exit code in `rc` because zsh reserves `status`, and never use `tee` to copy the full log. Decide the result from the exit code and read log content only for bounded summaries or diagnosis. If a command starts producing large uncaptured output, stop waiting and rerun it through this wrapper.

```sh
log_file="$(mktemp -t shardingsphere-verify.XXXXXX.log)"
<command> >"$log_file" 2>&1
rc=$?
if [ "$rc" -eq 0 ]; then
  grep -E 'BUILD SUCCESS|Tests run:|Ran [0-9]+ test|tests passed|OK$' "$log_file" | tail -1 || printf 'PASS (see %s)\n' "$log_file"
else
  tail -n 30 "$log_file"
fi
printf 'log=%s exit=%s\n' "$log_file" "$rc"
exit "$rc"
```

## Execution-Specific Rules

### Maven

- Prefer explicit `-pl` modules derived from changed owners, affected tests, and consumers. Add `-am` only for dependency freshness, missing reactor artifacts, CI equivalence, or required reactor participation, and record why the explicit set was insufficient.
- For PR readiness, normally run `-am` at most once per unchanged head; rerun only after a relevant failure, code change, or widened scope.
- Verify multi-module changes bottom-up. On success extract `BUILD SUCCESS`, `Tests run:`, or the runner summary. On failure inspect the last 30 log lines, then search for `ERROR`, `FAILURE`, `Caused by`, or the failed test.

### IDE, Proxy, E2E, and Services

- Tool-managed runs do not need shell redirection. Prefer a mode that returns `fullOutputPath` for long or high-output runs and analyze that file instead of a large snapshot.
- Filter logs for startup, readiness, `BUILD SUCCESS`, `Process finished`, `testFailed`, `Caused by`, or feature-specific markers.
- Before using a running service as evidence, prove that it uses current-source or rebuilt task artifacts. Stop temporary processes after verification.

#### Local Docker Test Images

- For serial local tests, use the repository's existing standard image names with the `latest` tag for locally built test artifacts instead of inventing names or per-run tags.
  Preserve existing external-dependency and CI image tags unless they conflict with the established target version for the same dependency and major-version line.
- Use one pinned image version across all test entry points for the same external test dependency and supported major-version line.
  Follow the target version explicitly required by the current task or established repository tests; resolve an unclear or conflicting target before selecting an image.
  Do not use a different version, a floating tag, or an omitted tag to satisfy that dependency and major-version line.
- Before execution, inspect the final effective test image selection.
  Correct a conflicting selection only within the current task's authorized scope, before starting the test.
  If the conflicting selection is outside that scope, report the difference without changing other entry points or starting the test.
- Do not relabel or substitute an image to present another dependency version as the required version.
  During actual runtime verification, verify both the selected image and the dependency's actual version; an image tag alone does not prove the version.
- Unifying an external test dependency image within one major-version line does not authorize changes to other dependencies or major-version lines.
- Reuse an existing image only after proving that it contains the current artifacts being verified; the presence of `latest` alone is not proof.
  When artifacts change or freshness cannot be proved, rebuild current artifacts and update the same `latest` tag through the repository's standard build flow.
- Record the old image ID, if present, before rebuilding.
  Verify the new image ID after a successful build, and do not delete it when it equals the old ID.
  Retain the original usable image if the build fails.
- After a successful replacement, remove the recorded old image only after confirming that it has no other tags, no container dependencies, no other use, and a verified rebuild source.
  Check all containers, including running and stopped containers, when verifying image dependencies.
  An untagged `<none>:<none>` listing does not prove that an image is unused, and overwriting `latest` does not remove the old image.
- Apply the same tag, dependency, other-use, and rebuild checks to task-created temporary images, and handle them after build or test success or failure.
  Keep the standard `latest` image intended for reuse.
  For any image that cannot be safely removed, report its ID, retention reason, and conditions for later cleanup.
- When parallel local tests require independent tags, explain the isolation need and plan cleanup before using them so that tasks do not contend for `latest`.
- Limit deletion to image IDs confirmed for this task, using `docker image rm --no-prune` without force to avoid removing unselected parent images.
  Do not run global prune or extend image cleanup to other tasks' images, containers, volumes, or data.

### Database Queries

- Prefer read-only queries and bound rows, columns, object names, and metadata filters. Avoid `SELECT *` without a specific reason.
- Wrap unbounded rows, metadata, diagnostics, and broad `SHOW` output. A database write requires its purpose, exact impact, rollback plan, and explicit confirmation.

## Structured Output Constraints

Apply this section only to large analysis, review, handoff, or repeated evidence output.

- Prefer exact edits, patches, or compact structured data over repeated prose when supported.
- Use a table or list for genuine comparisons or repeated records, define repeated structure once, and include only conclusion, action, evidence, and remaining risk.
- Do not restate the request or loaded context. Do not impose structure on a short answer or when it harms readability.
