# Research: ShardingSphere MCP Official Registry Publication and OCI Distribution

## Decision Summary

### Decision 1: Use `oci` on GHCR as the first official package type

- **Decision**: publish `ghcr.io/apache/shardingsphere-mcp` as the first public artifact for the official MCP Registry.
- **Rationale**:
  - the repository already has `distribution/mcp/Dockerfile`
  - official MCP Registry supports `oci` packages hosted on GHCR
  - this avoids introducing a second packaging ecosystem for a Java runtime
- **Alternatives considered**:
  - `mcpb`: viable later, but would require an extra artifact format and checksum workflow
  - `npm` / `pypi`: mismatched with the current Java distribution shape
  - `remotes`: not ready because the repository does not operate a public remote MCP endpoint

### Decision 2: Use GitHub namespace and GitHub OIDC

- **Decision**: use `io.github.apache/shardingsphere-mcp` and publish with GitHub OIDC.
- **Rationale**:
  - official Authentication guide allows `io.github.orgname/*` for GitHub auth
  - no DNS TXT record or `/.well-known/mcp-registry-auth` endpoint is needed
  - the repository already lives under the `apache` GitHub organization
- **Alternatives considered**:
  - reverse-DNS domain namespace: stronger brand ownership, but needs extra cross-system setup not present in this repository
  - GitHub PAT auth: works, but is less desirable than OIDC for release automation

### Decision 3: Keep HTTP as the container default and add a stdio switch

- **Decision**: keep `docker run ghcr.io/apache/shardingsphere-mcp:<version>` on HTTP by default, and add `SHARDINGSPHERE_MCP_TRANSPORT=stdio` for Registry package launches.
- **Rationale**:
  - preserves current README quick start
  - makes the Registry-declared `stdio` transport truthful
  - avoids requiring clients to author a new config file at runtime
- **Alternatives considered**:
  - flip the image default to stdio: would break the current HTTP-first quick start
  - publish a second stdio-only image: unnecessary duplication for the first release path

### Decision 4: Publish from a dedicated workflow after GitHub release or manual dispatch

- **Decision**: add a dedicated workflow that builds the MCP distribution, pushes GHCR images, then publishes `mcp/server.json`.
- **Rationale**:
  - official GitHub Actions guidance recommends a workflow-driven publish
  - ShardingSphere tags are plain versions like `5.5.3`, so a repository-specific workflow is safer than copying the official `v*` example verbatim
  - workflow_dispatch provides a release-manager fallback without forcing a new branch strategy
- **Alternatives considered**:
  - manual local `mcp-publisher publish`: too easy to drift from the image version
  - reusing `nightly-build.yml`: wrong lifecycle and would mix nightly artifacts with official release publication

## Source Notes

- Official Registry overview confirms the registry hosts metadata, not binaries, and requires publicly accessible installation methods.
- Official Package Types guide confirms GHCR-backed `oci` packages are supported and OCI verification relies on `io.modelcontextprotocol.server.name`.
- Official Authentication guide confirms GitHub-based auth requires `io.github.orgname/*`.
- Official GitHub Actions guide confirms OIDC-based `mcp-publisher login github-oidc` is the recommended automation path.
- Official Versioning guide confirms each publication needs a unique version and recommends aligning package and server versions.
