# Implementation Plan: ShardingSphere MCP V1 Unified Database Contract

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-03-21 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md)
**Input**: Feature specification from `/specs/001-shardingsphere-mcp/spec.md`
with technical inputs from `docs/mcp/ShardingSphere-MCP-Technical-Design.md`
and `docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

## Summary

在主仓库内引入一个独立的 ShardingSphere MCP 子链路：代码位于 `mcp/core` 与 `mcp/bootstrap`，
发布位于 `distribution/mcp`，端到端验证位于 `test/e2e/mcp`。实现采用仓库内自管的
领域模型与状态型会话/事务模型，并在 `mcp/bootstrap` 局部引入 MCP Java SDK、
embedded Tomcat、Streamable HTTP + STDIO 双 transport；Jackson 继续跟随根工程
`2.16.1` 版本，不向主仓库其他模块外溢。

## Technical Context

**Language/Version**: Java 17 for the MCP subchain with module-local JDK 17 build settings inside the default reactor  
**Primary Dependencies**: MCP Java SDK and embedded Tomcat scoped to
`mcp/bootstrap`, Maven Toolchains, repository-managed Jackson 2.16.1,
ShardingSphere `infra` / `database` / `parser` / `mode` / `kernel` modules,
targeted `features` modules only when required  
**Storage**: ShardingSphere metadata sources, Java in-memory session store for HTTP session state, packaged configuration files under `distribution/mcp/conf`  
**Testing**: JUnit 5, Mockito, module unit tests, bootstrap integration tests, protocol smoke tests, `test/e2e/mcp`, module-scoped Maven verification  
**Target Platform**: Standalone JDK 17 server on Linux or macOS,
Streamable HTTP for remote transport, STDIO for local debugging, interoperating
with ShardingSphere deployments that use Proxy cluster + registry, JDBC embedded,
or hybrid governance topologies; MCP itself does not embed into Proxy/JDBC runtime  
**Project Type**: Java monorepo subproject with runtime modules, distribution packaging, and dedicated E2E module  
**Performance Goals**: Preserve the PRD requirement that metadata and DCL
changes become visible within 1 minute, keep result truncation consistent with
request or capability defaults, and keep MCP concerns isolated to its own
modules, runtime, and release assets  
**Constraints**: protocol version is fixed at `2025-11-25`, Spring AI is out of scope,
Proxy/JDBC embedding is forbidden, the built-in runtime stays limited to
session / protocol validation plus runtime-boundary checks, HTTP sessions use
sticky-session plus local memory only, and distributed session storage plus
transaction failover are out of scope  
**Scale/Scope**: 12 V1 database types, 11 public tools, 16 public resources,
3 new build subchains (`mcp`, `distribution/mcp`, `test/e2e/mcp`), and
4 test layers (unit, module integration, protocol integration, E2E)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Note**: [constitution.md](/Users/zhangliang/IdeaProjects/shardingsphere/.specify/memory/constitution.md)
now defines the feature-level non-negotiables for Spec Kit analysis and planning.
Repository-level
[AGENTS.md](/Users/zhangliang/IdeaProjects/shardingsphere/AGENTS.md)
and
[CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md)
remain the detailed implementation law for this repository and do not conflict
with the constitution.

- **Gate 1 - Smallest safe change**: PASS
  The design keeps Java 17 dependencies and compiler settings localized inside the MCP subchain while using the same default reactor module model as JDBC and Proxy.
- **Gate 2 - Quality and verification path exists**: PASS
  The implementation sequence includes unit, integration, protocol, and E2E layers, with scoped Maven verification for touched modules.
- **Gate 3 - Runtime boundaries and audit are explicit**: PASS
  Capability, audit, local-mode boundary checks, sticky-session boundaries, and unsupported semantics are all explicitly modeled.
- **Gate 4 - Traceability is preserved**: PASS
  Design decisions are captured in `research.md`, implementation structure is fixed in this plan, and contracts are written as first-class artifacts.
- **Gate 5 - Governance layers are aligned**: PASS
  The constitution defines feature-level non-negotiables, while AGENTS and
  CODE_OF_CONDUCT keep repository-wide requirements such as Checkstyle,
  Spotless, scoped verification, and explicit rollback notes in force.

**Post-Design Re-check**: PASS
Phase 1 artifacts keep all critical decisions explicit and introduce no new governance violations requiring justification.

## Project Structure

### Documentation (this feature)

```text
specs/001-shardingsphere-mcp/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── mcp-domain-contract.md
│   └── streamable-http-contract.md
└── tasks.md
```

### Source Code (repository root)

```text
pom.xml
distribution/
├── pom.xml
└── mcp/
    ├── pom.xml
    ├── Dockerfile
    └── src/
        └── main/
            ├── bin/
            │   └── start.sh
            └── resources/
                └── conf/
                    └── mcp.yaml
mcp/
├── pom.xml
├── core/
│   ├── pom.xml
│   └── src/
│       ├── main/java/org/apache/shardingsphere/mcp/
│       │   ├── audit/
│       │   ├── capability/
│       │   ├── execute/
│       │   ├── metadata/
│       │   ├── protocol/
│       │   ├── resource/
│       │   ├── session/
│       │   └── tool/
│       └── test/java/org/apache/shardingsphere/mcp/
└── bootstrap/
    ├── pom.xml
    └── src/
        ├── main/java/org/apache/shardingsphere/mcp/bootstrap/
        │   ├── config/
        │   ├── lifecycle/
        │   ├── transport/http/
        │   ├── transport/stdio/
        │   └── wiring/
        └── test/java/org/apache/shardingsphere/mcp/bootstrap/
test/
└── e2e/
    ├── pom.xml
    └── mcp/
        ├── pom.xml
        └── src/test/java/org/apache/shardingsphere/test/e2e/mcp/
```

**Structure Decision**: Follow the documented three-chain design from the
technical documents: one MCP code subtree, one distribution subtree,
and one E2E subtree, all participating in the default reactor modules while
keeping Java 17 compilation and runtime dependencies localized to the MCP subchain.

## Phase 0 Research Summary

- Use the main repository as the hosting location and keep MCP in the default reactor module graph like JDBC, Proxy, and agent.
- Fix the runtime stack to repository-owned core semantics plus an MCP Java SDK transport adapter localized to `mcp/bootstrap`.
- Keep HTTP stateful because transactions and savepoints are part of the product contract.
- Treat capability, session, audit, refresh visibility, and transaction matrix as first-class domain concepts in `mcp/core`.
- Model transport and runtime wiring in `mcp/bootstrap`, not in `mcp/core`.
- Publish a standalone distribution with its own scripts, Dockerfile, configuration, and release assets.

## Phase 1 Design Outputs

- [research.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/research.md)
  Captures all major implementation decisions, rationale, and rejected alternatives.
- [data-model.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/data-model.md)
  Defines the capability, session, metadata, result, audit, and refresh-visibility domain models.
- [streamable-http-contract.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/streamable-http-contract.md)
  Fixes the transport-level contract for `/mcp`, HTTP methods, headers, and session lifecycle semantics.
- [mcp-domain-contract.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md)
  Fixes the resources, tools, result models, and error surface exposed to MCP clients.
- [quickstart.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md)
  Defines the expected build, packaging, runtime configuration, and smoke verification flow.

## Phase 2 Implementation Strategy

1. **Foundation and build isolation**
   Add `mcp`, `distribution/mcp`, and `test/e2e/mcp` to the default Maven chains; wire JDK 17 Toolchains and keep MCP-specific compilation and runtime dependencies localized.
2. **Core domain and matrix**
   Implement the transaction matrix registry, capability assembler, error model, session model, audit, refresh coordination, and core protocol DTOs inside `mcp/core`.
3. **Bootstrap and transport**
   Implement HTTP and STDIO bootstrapping, `/mcp` Streamable HTTP endpoint handling, session-header and protocol validation, local-mode `Origin` checks, and lifecycle management in `mcp/bootstrap`.
4. **Metadata and execution integration**
   Wire metadata discovery, `execute_query`, audit output, DDL / DCL refresh visibility, transaction/savepoint validation, and result mapping to ShardingSphere internals.
5. **Distribution and verification**
   Package `distribution/mcp`, provide scripts/configs/Dockerfile, then land integration tests and `test/e2e/mcp` coverage across transaction, optional object, audit, and refresh-visibility scenarios.

## Validation Strategy

- **Build isolation check**
  `./mvnw -pl mcp -am test -DskipITs -Dspotless.skip=true`
- **Distribution packaging**
  `./mvnw -pl distribution/mcp -am -DskipTests package`
- **E2E verification**
  `./mvnw -pl test/e2e/mcp -am test -Dsurefire.failIfNoSpecifiedTests=false`
- **Supported database baseline verification**
  `./mvnw -pl mcp/core -am -Dtest=MCPDatabaseCapabilityProviderTest test -Dsurefire.failIfNoSpecifiedTests=false`
- **Quality gates for touched modules**
  `./mvnw -pl <touched-module> -am checkstyle:check -Pcheck`
  and
  `./mvnw -pl <touched-module> -am spotless:apply -Pcheck`

## Complexity Tracking

No constitution violations require justification for this plan. The design
deliberately chooses the smallest safe structure that still satisfies the PRD,
the fixed technical design, and the repository governance rules.
