# CLAUDE.md - Strict Mode Code of Conduct

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Apache ShardingSphere is a distributed SQL transaction & query engine that allows for data sharding, scaling, encryption, and more - on any database.
Our community's guiding development concept is Database Plus for creating a complete ecosystem that allows you to transform any database into a distributed database system.

It focuses on repurposing existing databases, by placing a standardized upper layer above existing and fragmented databases, rather than creating a new database.

The goal is to provide unified database services and minimize or eliminate the challenges caused by underlying databases' fragmentation.
This results in applications only needing to communicate with a single standardized service.

The concepts at the core of the project are `Connect`, `Enhance` and `Pluggable`.

- `Connect:` Flexible adaptation of database protocol, SQL dialect and database storage. It can quickly connect applications and heterogeneous databases.
- `Enhance:` Capture database access entry to provide additional features transparently, such as: redirect (sharding, readwrite-splitting and shadow), transform (data encrypt and mask), authentication (security, audit and authority), governance (circuit breaker and access limitation and analyze, QoS and observability).
- `Pluggable:` Leveraging the micro kernel and 3 layers pluggable mode, features and database ecosystem can be embedded flexibly. Developers can customize their ShardingSphere just like building with LEGO blocks.

## Build System

The project uses Maven as its primary build system with the following key commands:

```bash
# Build the entire project with tests
./mvnw install -T1C

# Skip IT tests but build
./mvnw install -T1C -Dremoteresources.skip -DskipTests

# Format codes with Spotless
./mvnw spotless:apply -Pcheck

# Check codes with CheckStyle
./mvnw checkstyle:check -Pcheck

# Project dependencies check
./mvnw dependency:tree -Dverbose

# Use docker to run E2E tests
./mvnw -B clean install -Pe2e.env.docker -DskipTests
./mvnw -nsu -B install -Dspotless.apply.skip=true -De2e.run.type=DOCKER -De2e.artifact.modes=${{ matrix.mode }} -De2e.artifact.adapters=${{ matrix.adapter }} -De2e.run.additional.cases=false -De2e.scenarios=${{ matrix.scenario }} -De2e.artifact.databases=${{ matrix.database }}
```

## Project Structure

Key directories and their purposes:

- `infra/`: Infrastructure components including SPI (Service Provider Interface) implementations and basic components including binder, route, rewrite etc
- `parser/`: SQL parser for various database dialects and ShardingSphere's DistSQL
- `kernel/`: Core kernel functionality including metadata, transaction, and authority management
- `feature/`: Pluggable functionality including sharding, encryption, mask, and shadow databases
- `mode/`: Configuration persistence, management and coordination with standalone mode and cluster mode
- `proxy/`: ShardingSphere-Proxy implementation, including MySQL, PostgreSQL, and Firebird database protocols
- `jdbc/`: ShardingSphere-JDBC driver implementation
- `test/`: E2E and IT test engine and cases

## Development Guidelines

Please follow these guidelines when developing: https://shardingsphere.apache.org/community/en/involved/contribute/dev-env/

## Code Standards and Conventions

Please follow these guidelines when developing: https://shardingsphere.apache.org/community/en/involved/conduct/code/

## Absolute Prohibitions (Zero-Tolerance Violations)

1. **Strictly prohibit modifying unrelated code**: Unless explicitly instructed in the current conversation, absolutely prohibit modifying any existing code, comments, configurations, or files. This includes but is not limited to:
  - Fixing spelling errors or formatting issues that you "think" need correction.
  - Refactoring or optimizing code structures not mentioned in the instructions.
  - Performing any "code cleanup" outside the code lines/blocks specified by the current task.
  - Adding or deleting imports, dependencies not explicitly requested.

2. **Prohibit autonomous decision-making**: Prohibit any additional operations beyond the instruction scope based on "common sense" or "best practices". All actions must be strictly confined to the scope of the user's latest instruction.

3. **Prohibit creating unrelated files**: Unless explicitly instructed, prohibit creating any new files (including documents, tests, configuration files, etc.).


## Permission Framework Based on Whitelist

1. Automatic approval scope, automated processing is only permitted for:
  - Editing files explicitly mentioned in the current user instruction.
  - Creating new files in directories explicitly specified in the request.

2. Change scope locking
  - **Whitelist mechanism**: Your code modification scope must be strictly limited to the files, functions, code blocks, or line numbers explicitly mentioned in the instruction.
  - **Context association**: If the instruction description is ambiguous (e.g., "modify this function"), you must first confirm the full name and location of the target function with me before proceeding.

3. Code style and formatting
  - **Prohibit automatic formatting**: Strictly prohibit running any code formatting tools to format the entire file or project, unless explicitly instructed.
  - **Local consistency**: If modifying code, the style of the new code should be consistent with the immediately adjacent contextual code, rather than the "ideal style" of the entire project.

## Operational Procedures

1. Pre-execution checklist, before making any code changes, you must:
  - Verify the current task exactly matches the wording of the user's immediate request.
  - Confirm each target file/line is explicitly referenced.
  - Declare the planned changes in a bullet-point summary format.
  - Wait for the user's "PROCEED" confirmation instruction if any uncertainty exists.

2. Change implementation rules
  - Isolate edits to the smallest possible code blocks.
  - Maintain existing style, even if suboptimal; prohibit changes solely for formatting.
  - Preserve all comments unless directly contradictory to the specific edit.
  - If using Git, record the rationale for each change in the commit message.

## Cognitive Constraints

1. Scope adherence
  - Ignore potential improvements, optimizations, or technical debt outside the current task.
  - Suppress all creative suggestions unless explicitly requested via the "suggest:" prefix.
  - Defer all unrelated ideas to the post-task discussion phase.

2. Decision matrix when uncertainty arises:
  - If scope is ambiguous → Request clarification
  - If impact is uncertain → Propose a minimal safe experiment
  - If conflicts are detected → Pause and report constraints
  - If rules are contradictory → Follow the most restrictive interpretation

## Safety Overrides

1. Emergency stops: Immediately terminate the current operation if:
  - Code deletion exceeds 10 lines without explicit instruction.
  - Tests begin failing after changes.

2. Conservative principle: When any doubt exists:
  - Preserve existing functionality takes precedence over adding new features.
  - Maintain current behavior takes precedence over ideal implementations.
  - Favor minimal changes takes precedence over comprehensive solutions.

## Compliance Verification

1. Post-change verification: After each code modification:
  - If a test suite exists, run relevant tests.
  - Confirm no regression in basic functionality.
  - Verify the change scope conforms to the original request.
  - Report deviations immediately upon discovery.

2. Continuous execution
  - Re-read this protocol before starting each new task.
  - Verify rule compliance after each user interaction.
  - Report violations through clear error messages.
