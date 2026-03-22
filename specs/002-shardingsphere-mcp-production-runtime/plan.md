# Implementation Plan: ShardingSphere MCP Production Runtime Integration

**Branch**: `002-shardingsphere-mcp-production-runtime` | **Date**: 2026-03-22 | **Spec**:
[spec.md](/Users/zhangliang/IdeaProjects/shardingsphere/specs/002-shardingsphere-mcp-production-runtime/spec.md)
**Input**: Feature specification from `/specs/002-shardingsphere-mcp-production-runtime/spec.md`

## Summary

补齐当前 MCP 子链路最后一段真实 runtime integration：为 `distribution/mcp` 引入真实 metadata provider、真实执行适配层、fail-fast 启动校验与非 fixture 的 E2E 验收，
使默认发行包兑现 PRD 中“统一 metadata 发现 + 统一 SQL 执行 + 统一事务边界”的产品承诺，而不再停留在协议与契约骨架阶段。

## Technical Context

**Language/Version**: Java 17 in the MCP subchain  
**Primary Dependencies**: MCP Java SDK, embedded Tomcat, repository-managed Jackson 2.16.1, ShardingSphere `infra` / `database` / `parser` / `mode` / `kernel`, deployment-specific mode repositories and JDBC drivers when required  
**Storage**: Shared ShardingSphere metadata sources, real execution adapters over ShardingSphere runtime, in-memory MCP HTTP session state, packaged configuration under `distribution/mcp/conf`  
**Testing**: JUnit 5, Mockito, MCP module unit tests, bootstrap integration tests, non-fixture E2E tests in `test/e2e/mcp`, scoped Maven verification, packaged quickstart smoke validation  
**Target Platform**: Standalone JDK 17 MCP server on Linux or macOS, Streamable HTTP for production access, optional STDIO only for local debugging, interoperating with supported ShardingSphere deployment topologies without embedding into Proxy or JDBC processes  
**Project Type**: Java monorepo subproject with standalone distribution and dedicated E2E module  
**Performance Goals**: Preserve the PRD requirement that DDL / DCL metadata visibility reaches current session immediately and global visibility within 60 seconds, while keeping MCP startup fail-fast and avoiding empty-runtime false positives  
**Constraints**: MCP remains a standalone runtime, protocol version stays fixed at `2025-11-25`, empty metadata/runtime defaults cannot remain the production success path, runtime provider wiring must stay explicit and testable, and fixture-only success paths are insufficient for acceptance  
**Scale/Scope**: Default distribution path, 11 public tools, 16 public resources, 12 V1 database capability entries, 1 provider abstraction layer, and at least 2 real-database E2E acceptance slices

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Note**: [constitution.md](/Users/zhangliang/IdeaProjects/shardingsphere/.specify/memory/constitution.md),
[AGENTS.md](/Users/zhangliang/IdeaProjects/shardingsphere/AGENTS.md), and
[CODE_OF_CONDUCT.md](/Users/zhangliang/IdeaProjects/shardingsphere/CODE_OF_CONDUCT.md)
remain jointly binding.

- **Gate 1 - Product value over protocol shell**: PASS
  This feature exists specifically to close the gap between the current transport shell and the PRD product promise.
- **Gate 2 - Smallest safe change**: PASS
  The plan keeps MCP standalone and introduces runtime providers rather than embedding into Proxy or JDBC.
- **Gate 3 - Quality and verification path exists**: PASS
  The plan requires non-fixture E2E acceptance, packaged quickstart validation, and scoped module verification.
- **Gate 4 - Runtime boundary clarity**: PASS
  The plan keeps external exposure behind network boundaries and adds fail-fast startup diagnostics instead of silent empty-runtime success.
- **Gate 5 - Traceability**: PASS
  Runtime-provider behavior, capability assembly, and acceptance deltas are explicitly tracked in this follow-up spec.

## Project Structure

### Documentation (this feature)

```text
specs/002-shardingsphere-mcp-production-runtime/
├── spec.md
├── plan.md
└── tasks.md
```

### Source Code (repository root)

```text
mcp/
├── core/
│   └── src/
│       ├── main/java/org/apache/shardingsphere/mcp/
│       │   ├── capability/
│       │   ├── execute/
│       │   ├── metadata/
│       │   └── session/
│       └── test/java/org/apache/shardingsphere/mcp/
└── bootstrap/
    └── src/
        ├── main/java/org/apache/shardingsphere/mcp/bootstrap/
        │   ├── config/
        │   ├── lifecycle/
        │   ├── runtime/
        │   └── transport/http/
        └── test/java/org/apache/shardingsphere/mcp/bootstrap/
distribution/
└── mcp/
    └── src/
        └── main/
            ├── bin/
            └── resources/conf/
test/
└── e2e/
    └── mcp/
        └── src/test/java/org/apache/shardingsphere/test/e2e/mcp/
```

**Structure Decision**: Keep the existing three-chain MCP structure, introduce a dedicated `mcp/bootstrap/runtime` adapter layer for production runtime providers,
and limit follow-up changes to MCP, distribution, and MCP E2E modules.

## Phase 0 Research Focus

- Pick the first production topology to support end-to-end with the standalone MCP runtime.
- Define the provider boundary that converts shared ShardingSphere metadata and execution entrypoints into `MetadataCatalog` and real `execute_query` behavior.
- Decide how fail-fast startup validation reports missing metadata sources, drivers, or runtime dependencies.
- Fix the acceptance boundary between fixture-based tests and production-runtime E2E validation.

## Phase 1 Design Outputs

- Provider interfaces and launch-mode boundaries for metadata and execution integration.
- Capability assembly order for static matrix, runtime metadata, and deployment overrides.
- Packaged runtime configuration model for production integration.
- Real-runtime quickstart and host-registration guidance.

## Phase 2 Implementation Strategy

1. **Runtime provider foundation**
   Introduce provider abstractions, production launch mode, configuration validation, and fail-fast startup behavior in `mcp/bootstrap`.
2. **Metadata integration**
   Adapt real ShardingSphere metadata sources into `MetadataCatalog`, wire real capability assembly inputs, and replace empty-runtime success paths for discovery.
3. **Execution integration**
   Replace the in-memory execution default with a real execution adapter that routes `execute_query` through ShardingSphere internals while preserving MCP error and result contracts.
4. **Refresh, audit, and packaging**
   Connect DDL / DCL visibility to real metadata refresh behavior, upgrade packaged configuration and startup notes, and keep audit evidence operator-visible.
5. **Acceptance and rollout**
   Add non-fixture E2E coverage, packaged quickstart verification, and README updates for deployment plus MCP host registration.

## Validation Strategy

- **Provider and bootstrap checks**
  `./mvnw -pl mcp/bootstrap -am test -DskipITs -Dspotless.skip=true`
- **Core contract checks**
  `./mvnw -pl mcp/core -am test -DskipITs -Dspotless.skip=true`
- **Production-runtime E2E verification**
  `./mvnw -pl test/e2e/mcp -am test -Dsurefire.failIfNoSpecifiedTests=false`
- **Distribution packaging**
  `./mvnw -pl distribution/mcp -am -DskipTests package`
- **Quality gates for touched modules**
  `./mvnw -pl <touched-module> -am checkstyle:check -Pcheck`
  and
  `./mvnw -pl <touched-module> -am spotless:apply -Pcheck`

## Complexity Tracking

No constitution violations are expected. The follow-up change increases product completeness, not architectural sprawl, and keeps MCP standalone as previously designed.
