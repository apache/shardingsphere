# ShardingSphere MCP Constitution

## Core Principles

### I. Smallest Safe Change
All MCP work MUST remain behind the non-default `mcp` profile and MUST NOT alter the default Java 8 build path.

### II. Explicit Governance and Security
Authentication, access policy, audit, unsupported semantics, and transaction boundaries MUST be explicit in the specification, plan, contracts, and tasks.

### III. Testable Delivery
Each user story MUST define scoped unit, integration or protocol, and end-to-end verification paths before implementation is considered complete.

### IV. Traceable Contracts
Public resources, tools, result models, capability models, and error codes MUST remain traceable across `spec.md`, `plan.md`, contracts, and `tasks.md`.

### V. Quality Gates
Touched modules MUST have a scoped Maven verification path, including tests and repository-required style gates, before delivery is considered complete.

## Constraints

MCP V1 uses a Java 17 isolated subchain, Streamable HTTP for remote access, and local-memory sticky sessions for HTTP state. Proxy or JDBC embedding, distributed session recovery, and transaction failover remain out of scope for V1.

## Delivery Workflow

The feature MUST keep build isolation explicit, preserve repository traceability, and record validation steps in planning and task artifacts before implementation begins.

## Governance

This constitution is authoritative for Spec Kit analysis and planning. Repository-level `AGENTS.md` and `CODE_OF_CONDUCT.md` provide detailed implementation rules and MUST NOT conflict with this constitution.

**Version**: 1.0.0 | **Ratified**: 2026-03-21 | **Last Amended**: 2026-03-21
