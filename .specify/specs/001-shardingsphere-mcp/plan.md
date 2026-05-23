# Implementation Plan: ShardingSphere MCP Resource Metadata

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `.specify/specs/001-shardingsphere-mcp/spec.md`
**Scope Note**: This plan is design-only. It does not authorize implementation changes, branch switches, commits, pushes, or destructive cleanup.

## Summary

Evolve ShardingSphere MCP resource metadata as one coherent contract change. Internally, replace the generic resource extension model with
`ShardingSphereMCPResourceMetadata`. In descriptor YAML, replace `extension` with `shardingSphereMetadata` without compatibility aliases. Externally, expose the
same metadata through MCP `_meta` for `resources/list`, `resources/templates/list`, and ShardingSphere capability catalog resource and template entries.

## Technical Context

**Language/Version**: Java, current repository Maven configuration
**Primary Dependencies**: ShardingSphere MCP modules, MCP Java SDK 1.1.2, JUnit 5, Mockito
**Storage**: Not applicable
**Testing**: Module-scoped Maven unit tests, MCP bootstrap tests, `test/e2e/mcp` contract discovery checks with contract mode enabled
**Target Platform**: ShardingSphere MCP runtime over Proxy-oriented logical metadata
**Project Type**: Java backend protocol and descriptor contract change
**Performance Goals**: No runtime regression for resource dispatch; metadata projection happens once during descriptor loading
**Constraints**: Do not preserve old `extension` compatibility; do not change resource URIs, resource template URI templates, tool names, prompt names, or transport behavior
**Scale/Scope**: All MCP descriptor YAML files, support descriptor loading, core metadata handlers, bootstrap resource specifications, MCP e2e discovery

## Source-Driven Decisions

- MCP `_meta` is the official extensibility location for implementation metadata.
  Source: https://modelcontextprotocol.io/specification/2025-11-25/basic#_meta
- MCP `Resource` and `ResourceTemplate` both support `_meta`.
  Source: https://modelcontextprotocol.io/specification/2025-11-25/schema
- Official discovery paths are `resources/list` and `resources/templates/list`.
  Source: https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- Java code should keep `meta` naming because the MCP Java SDK accessor is `meta()` while serialization emits `_meta`.

## MCP Builder Check

- Resource metadata must be discoverable through official MCP discovery, not only through ShardingSphere's aggregate capability resource.
- Tool and resource discoverability must improve without adding new tools or changing existing resource URI surfaces.
- Error behavior must be actionable: duplicate ShardingSphere metadata ownership should fail during descriptor loading with a precise message.
- Context management should stay concise: keep typed metadata focused on resource semantics, not arbitrary workflow payloads.
- `test/e2e/mcp` should prove that real MCP discovery and capability baselines expose ShardingSphere `_meta`.

## Constitution Check

*GATE: Must pass before implementation.*

- Quality first: keep typed metadata to avoid string-map runtime parsing.
- Think before action: use this spec and plan before code changes.
- Tools first: use `rg`, `apply_patch`, scoped `./mvnw`, and official MCP docs.
- Transparent records: preserve decisions in Spec Kit docs and final reports.
- Coding standards: follow `CODE_OF_CONDUCT.md`, including readable naming and consistent style.
- No dangerous operation: no branch switch, commit, push, reset, or bulk cleanup without explicit confirmation.
- Test design: cover public APIs only, keep scenarios dedicated, and use concise assertion-style test names.
- Mocking: prefer Mockito through existing extensions; migrate static mocking to `AutoMockExtension` unless a documented exception is necessary.
- Assertions: follow repository Hamcrest/JUnit conventions and avoid manual output inspection.
- Verification: run scoped Maven checks plus Checkstyle/Spotless gates for touched modules before handoff.

## Project Structure

### Documentation (this feature)

```text
.specify/specs/001-shardingsphere-mcp/
|-- spec.md
`-- plan.md
```

### Source Code (repository root)

```text
mcp/api/
mcp/support/
mcp/core/
mcp/bootstrap/
mcp/features/encrypt/
mcp/features/mask/
test/e2e/mcp/
mcp/README.md
mcp/README_ZH.md
```

**Structure Decision**: Keep the typed metadata model in `mcp/support` with the descriptor catalog infrastructure. Descriptor loading is the single protocol
projection point; bootstrap and catalog builders consume merged descriptor `meta`. Keep runtime behavior checks in `mcp/core`.

## Large-Batch Evolution

### Batch 1 - Descriptor Contract And Naming

- Rename `MCPResourceExtensionDescriptor` to `ShardingSphereMCPResourceMetadata`.
- Rename catalog fields and index methods from resource extension terminology to ShardingSphere resource metadata terminology.
- Rename YAML model from `YamlMCPResourceExtensionDescriptor` to a ShardingSphere metadata-specific type.
- Replace descriptor YAML key `extension` with `shardingSphereMetadata`.
- Update production YAML, test resource YAML, and embedded e2e descriptor text blocks to use `shardingSphereMetadata`.
- Update descriptor YAML key validator to reject the old `extension` key in every descriptor authoring surface.

### Batch 2 - Metadata Projection To MCP `_meta`

- Add one conversion path from `ShardingSphereMCPResourceMetadata` to `Map<String, Object>` using `MCPShardingSphereMetadataKeys`.
- Define a fixed typed-owned ShardingSphere resource metadata key set in `MCPShardingSphereMetadataKeys`.
- Reject any raw `meta` key that intersects with the fixed typed-owned key set before merging typed metadata.
- Allow raw `org.apache.shardingsphere/` keys that are not in the typed-owned resource metadata key set.
- Merge typed metadata into `MCPResourceDescriptor.meta` during descriptor loading; the merged `meta` map is the protocol projection source of truth.
- Keep Java variables and methods named `meta`, while protocol payloads and JSON content use `_meta` where MCP requires it.

### Batch 3 - Capability Catalog Alignment

- Read merged `MCPResourceDescriptor.meta` in the capability catalog builder instead of re-converting typed metadata.
- Change fixed-resource and resource-template entries in the capability catalog from `meta` to `_meta`.
- Update e2e baseline projection code and baseline YAML to assert `_meta` for capability catalog resource metadata.
- Verify the capability catalog and official MCP discovery expose equivalent ShardingSphere metadata for shared resources.

### Batch 4 - Runtime Behavior Preservation

- Keep metadata handlers reading typed `ShardingSphereMCPResourceMetadata` from the catalog index.
- Preserve current list/detail response behavior, empty-state recovery, parent resource hints, and large-result search guidance.
- Avoid parsing ShardingSphere semantic fields back out of generic `_meta` maps in runtime handlers.

### Batch 5 - Unit And Integration Tests

- `mcp/support`: catalog loader, YAML swapper, validator, payload builder, duplicate key rejection, old key rejection.
- `mcp/bootstrap`: resource and resource-template specifications include metadata that the SDK serializes as `_meta`.
- `mcp/core`: metadata handler behavior remains unchanged after the rename.
- `test/e2e/mcp`: official discovery and capability baseline projections include ShardingSphere `_meta` for fixed resources and templates.
- Cleanup verification: old extension type names and `extension:` authoring keys return no matches in `mcp` or `test/e2e/mcp` descriptors.

### Batch 6 - Documentation And Verification

- Update `mcp/README.md` and `mcp/README_ZH.md` for official discovery `_meta`.
- Run scoped tests before broader checks.
- Run Checkstyle/Spotless gates for touched modules.
- Record exact commands and exit codes.

## Verification Plan

- `./mvnw -pl mcp/support -DskipITs -Dspotless.skip=true -Dtest=MCPDescriptorCatalogLoaderTest,MCPDescriptorCatalogYamlSwapperTest test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/support -DskipITs -Dspotless.skip=true -Dtest=MCPDescriptorCatalogValidatorTest,MCPDescriptorCatalogPayloadBuilderTest test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest=MCPResourceSpecificationFactoryTest test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true -Dtest=MetadataResourceHandlerTest,ServerCapabilitiesHandlerTest test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dmcp.e2e.contract.enabled=true test`
- `./mvnw -pl mcp/support,mcp/core,mcp/bootstrap,test/e2e/mcp -am checkstyle:check -Pcheck`
- `rg "MCPResourceExtensionDescriptor|YamlMCPResourceExtensionDescriptor" mcp test/e2e/mcp`
- `rg "extension:" mcp test/e2e/mcp -g "*.yaml" -g "*.yml" -g "*.java"`

## Doubt-Driven Review Plan

CLAIM: The planned design keeps ShardingSphere metadata typed internally while exposing it through MCP-compliant `_meta` externally.
WHY THIS MATTERS: A wrong boundary would either break MCP discovery clients or force brittle runtime map parsing.

Review artifact: `spec.md` plus this `plan.md`.
Review contract: the feature must satisfy MCP `_meta` protocol shape, ShardingSphere naming clarity, no compatibility aliases, fail-fast metadata ownership, and
mcp plus mcp-e2e test coverage.

Cross-model reviewer: Codex CLI in read-only sandbox, after explicit command confirmation.

Confirmed user decisions:

- Rename consistently without preserving old `extension` compatibility.
- Keep Java names as `meta`; serialize protocol payloads as `_meta`.
- Use fail-fast duplicate ownership semantics.
- Include `test/e2e/mcp`.
- Use external cross-model review with Codex CLI.

Resolved cross-model review findings:

- Add resource template URI template stability to plan constraints.
- Expand old-key cleanup to production YAML, test YAML, and embedded e2e descriptor text blocks.
- Cover capability catalog `_meta` for both fixed resources and resource templates.
- Make descriptor loading the single projection source of truth and detect duplicates before merging.
- Use a fixed typed-owned key set so raw ShardingSphere metadata can coexist when it is not owned by the typed model.
- Enable MCP contract e2e verification through `-Dmcp.e2e.contract.enabled=true`.
- Add test and mocking checklist items from repository governance.

## Open Questions

- None.

## Complexity Tracking

No constitution violations are planned. This is a broad but coherent contract migration across MCP descriptor loading, discovery, runtime behavior, tests, and docs.
