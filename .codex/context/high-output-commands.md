# High-Output Command Rules

This file defines high-output handling rules for Apache ShardingSphere build, test, service startup, E2E, database query, and IDE/MCP commands.

## Trigger

Before running Maven, E2E, Proxy startup, database client, IDE/MCP run configuration, or any command that may output more than 100 lines, read or reuse this file.

If this exact file from the current repository has already been read in this session and there is no evidence it changed, reuse the loaded content.

## Mandatory Execution Contract

Reading or reusing this file is not sufficient.

Before emitting any command, classify it first:

- `Must Wrap`: use the Canonical Shell Wrapper before execution.
- `May Run Raw`: run directly only when output is known to be small and bounded.
- `Unsure`: use the Canonical Shell Wrapper.
- Do not run a raw `Must Wrap` shell command directly.
- If a prepared command is raw but should be `Must Wrap`, rewrite it into the wrapper before execution.
- The visible output for `Must Wrap` commands must contain only a filtered summary, log path, and exit code.
- IDE/MCP tool calls do not need shell redirection. For run or execution tools that support `fullOutputPath`, prefer a mode that returns it for potentially high-output runs.

## Core Rules

Do not print full high-output command logs directly into the conversation context.

Use a log file or the output file returned by a tool to hold command output. In the final report, include only the command, exit code, log path, and a small filtered summary.

Before sharing a log summary, avoid exposing secrets, passwords, tokens, private addresses, or undisclosed vulnerability details.

## Output Risk Classification

Use output risk as the primary classification. Command categories are hints, not absolute rules.

### Must Wrap

Use the Canonical Shell Wrapper when output is likely large, streaming, repeated, failure-prone, or more than 100 lines.

Typical `Must Wrap` commands include:

- Maven build, test, package, install, verification, and plugin goals.
- E2E runs.
- Proxy or service startup from shell.
- Database queries that return rows, metadata, diagnostics, or `SHOW` output.
- Log inspection commands that may return large or repeated matches.
- Commands whose failure may print long stack traces or dependency/build logs.

### May Run Raw

Small and bounded commands may run without the wrapper.

Typical `May Run Raw` commands include:

- Version or help checks such as `./mvnw -version`, `java -version`, `mysql --version`, or `psql --version`.
- Short status commands such as `git status --short`.
- Targeted searches expected to return only a few lines.
- Commands that are intentionally limited by object name, row count, file path, or explicit filters.

### Unsure

If output size or failure output is unclear, use the Canonical Shell Wrapper.

## Canonical Shell Wrapper

All `Must Wrap` shell commands MUST use this wrapper. Do not duplicate category-specific wrapper scripts elsewhere in this file.

- Redirect stdout and stderr to a temporary log file for the command classified as `Must Wrap`.
- Save the exit code with `rc=$?`. Do not use `status=$?`, because `status` is a read-only special parameter in zsh and may make the wrapper fail before preserving the real command result.
- Do not use `tee` to copy full Maven, Proxy, or E2E logs to the terminal.
- If a command has already started producing large output without log capture, stop waiting and rerun it with a log-file wrapper.
- Decide success or failure from the exit code. Read log content only when extracting summaries or diagnosing failures.
- Prefer whichever filtering tool is available in the current environment, such as `rg`, `grep`, `awk`, or `sed`.

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

## Maven

Maven build, test, package, install, verification, and plugin goals are usually `Must Wrap`. Small and bounded version or help checks may run raw.

- Run scoped commands when possible instead of defaulting to whole-repository builds.
- Add `-am` only when there is a clear reason, such as missing reactor dependencies, stale dependent module artifacts, CI-equivalent builds, or commands that require reactor participation.
- When a Maven command uses `-am`, record why the smaller command was insufficient.
- On Maven success, extract one summary line such as `BUILD SUCCESS`, `Tests run:`, or the runner summary from the log.
- On Maven failure, inspect `tail -n 30 "$log_file"` first, then use an available filtering tool to find `ERROR`, `FAILURE`, `Caused by`, or the failed test name.

## IDE/MCP Tool Runs

- IDE/MCP runs are tool-managed. Classify them by expected output and runtime behavior.
- Do not force IDE/MCP tool calls through the Canonical Shell Wrapper solely because the tool does not provide a log path.
- For run or execution tools that support a log-path field such as `fullOutputPath`, treat that path as the log file for the run.
- For long-running Proxy, E2E, service configurations, or other potentially high-output runs, prefer a mode that returns `fullOutputPath`, such as `waitForExit=false` when supported.
- If the IDE or MCP tool already returned a log path, do not analyze a large output snapshot directly.
- Small and bounded tool outputs, or tools intentionally used to read content such as `read_file`, do not need a separate log path.
- When a log path is available, use an available filtering tool to find startup markers, port readiness, `BUILD SUCCESS`, `Process finished`, `testFailed`, `Caused by`, or feature-specific keywords.

## Proxy, E2E, and Service Startup

- Treat Proxy startup, E2E, and debug logs as `Must Wrap` when launched from shell, especially when SQL show or debug logging is enabled.
- Record the startup command or run configuration, exit code when available, `fullOutputPath` or log path, and a small filtered readiness or failure summary.
- If behavior is verified through a running Proxy or service, first confirm the process uses the current branch code or artifacts rebuilt from this change before using the result as evidence.
- Stop temporary long-running processes after verification to avoid occupying ports or debug sessions.

## Database Queries

- Database clients are not automatically `Must Wrap`; classify them by expected output.
- Small and bounded version or help checks may run raw.
- Queries, metadata inspection, diagnostics, and commands with unbounded rows are `Must Wrap`.
- Prefer read-only queries for verification.
- Limit row and column counts. Do not use `SELECT *` unless there is a clear reason.
- Add filters, object names, or result limits to `SHOW`, `information_schema`, or database dictionary queries.
- Write query output to a file or intentionally keep terminal output very small.
- If verification requires a write operation, explain the purpose, impact scope, and rollback plan, then wait for explicit user confirmation.

## Final Report

For each high-output command, report only:

- Command or IDE run configuration name.
- Exit code or tool-returned status.
- Log path or `fullOutputPath`.
- One success summary line or a focused failure snippet.
- Skipped verification items and the exact command that can rerun them.
