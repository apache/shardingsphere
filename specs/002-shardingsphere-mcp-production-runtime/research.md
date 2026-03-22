# Research: ShardingSphere MCP Production Runtime Integration

## Decision 1: Keep MCP as a standalone runtime and close the gap through provider integration

- **Decision**: Continue to keep MCP as an independent runtime and add a production-runtime provider boundary instead of embedding MCP into Proxy or JDBC.
- **Rationale**: The approved architecture already treats MCP as a standalone access surface. The remaining gap is real metadata and execution integration, not a missing embedded entrypoint.
- **Alternatives considered**:
  - Embed MCP into Proxy: rejected because it changes the runtime shape and deployment boundary approved by the PRD and technical design.
  - Embed MCP into JDBC: rejected because it would couple agent-facing HTTP lifecycle to application processes and make deployment topology ambiguous.

## Decision 2: Support one explicit first production topology before expanding compatibility

- **Decision**: Define the first production topology as a standalone MCP runtime that reads shared ShardingSphere metadata sources and invokes real ShardingSphere execution entrypoints through a provider.
- **Rationale**: The current gap is best closed by one end-to-end deployable topology with clear ownership and acceptance criteria, rather than by attempting multiple runtime shapes at once.
- **Alternatives considered**:
  - Support multiple topologies in the first follow-up: rejected because it dilutes verification and makes fail-fast diagnostics harder to define.
  - Keep only fixture-backed topology in this phase: rejected because it does not close the PRD gap.

## Decision 3: Remove empty metadata/runtime as a successful production path

- **Decision**: Production startup must fail fast when provider configuration, runtime dependencies, or metadata sources are missing, rather than silently falling back to empty `MetadataCatalog` or empty `DatabaseRuntime`.
- **Rationale**: Silent empty startup is the main reason the current distribution can pass transport smoke checks while still missing the product value promised by the PRD.
- **Alternatives considered**:
  - Keep empty fallback for operator convenience: rejected because it misrepresents system readiness and hides deployment errors.
  - Allow a warning-only fallback mode: rejected because it still permits false-positive startup success in production.

## Decision 4: Preserve existing MCP core contracts and change only the runtime input path

- **Decision**: Keep the current MCP domain surface, result models, session semantics, and transport contracts from `001-shardingsphere-mcp`, while replacing the runtime input path with real metadata and execution providers.
- **Rationale**: The protocol shell, resource/tool contracts, and session semantics are already defined. The missing work is wiring those contracts to real ShardingSphere internals.
- **Alternatives considered**:
  - Redesign tools and resources for production runtime: rejected because it would expand scope and invalidate already completed protocol work.
  - Expose provider-specific tool variants: rejected because the PRD requires one unified MCP surface.

## Decision 5: Keep capability assembly deterministic

- **Decision**: Database capability continues to be assembled in a fixed order: transaction matrix defaults, runtime metadata facts, and deployment-specific overrides.
- **Rationale**: This preserves traceability and prevents runtime integration from turning capability responses into ad hoc behavior.
- **Alternatives considered**:
  - Derive all capability from runtime metadata only: rejected because some transaction semantics are published product guarantees, not just discovered facts.
  - Let deployment overrides replace the full capability model: rejected because it weakens contract consistency and reviewability.

## Decision 6: Make non-fixture E2E the acceptance gate for this follow-up

- **Decision**: Treat fixture-backed tests as useful regression coverage, but require at least one real production-runtime E2E path as the acceptance gate for this feature.
- **Rationale**: The current code already proves the transport shell with fixtures. This follow-up exists specifically to prove that the default packaged runtime reaches real metadata and real execution behavior.
- **Alternatives considered**:
  - Keep fixture E2E as the only acceptance path: rejected because it does not validate the production-runtime gap.
  - Rely on unit and integration tests only: rejected because packaging, startup diagnostics, and host registration expectations need end-to-end confirmation.

## Decision 7: Separate operator deployment guidance from local protocol debugging

- **Decision**: Keep local protocol debugging guidance lightweight, but move production-runtime validation into a dedicated quickstart that starts from packaged configuration and ends with host registration plus real-tool smoke checks.
- **Rationale**: Operators need to know whether the packaged runtime is truly ready for model use, not just whether `/mcp` responds to `initialize`.
- **Alternatives considered**:
  - Reuse the local debug flow as the production quickstart: rejected because it hides provider configuration and runtime readiness requirements.
  - Document only implementation details in plan/tasks: rejected because deployment reproducibility is part of the approved follow-up scope.

## Decision 8: Inherit `001` contracts and add only production-runtime-specific supplemental contracts

- **Decision**: Reuse the domain and transport contracts from `001-shardingsphere-mcp` as the baseline, and add only two new contract layers in this follow-up: runtime-provider behavior and production acceptance behavior.
- **Rationale**: The follow-up should clarify the new runtime obligations without duplicating already accepted protocol definitions.
- **Alternatives considered**:
  - Copy all `001` contracts into `002`: rejected because it creates drift risk and duplicated maintenance.
  - Keep `002` without contracts: rejected because provider behavior and production acceptance would remain implicit.
