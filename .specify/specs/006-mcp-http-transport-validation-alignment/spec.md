# Feature Specification: MCP HTTP Transport Validation Alignment

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-28
**Status**: Draft
**Input**: User description: "Align ShardingSphere MCP HTTP transport validation with MCP's native validation lifecycle, stop maintaining a parallel servlet-side validation chain, and adapt ShardingSphere-specific rules through MCP-compatible best practices without preserving the current custom error envelope."

## Clarifications

### Session 2026-04-28

- This feature is about HTTP transport validation boundaries, error ownership, and validation extensibility. It is not a redesign of MCP business workflows or feature semantics.
- The desired end state is MCP-native request validation: MCP framework code must invoke the registered `ServerTransportSecurityValidator` automatically, and `StreamableHttpMCPServlet` must stop manually calling `validateHeaders(...)`.
- Only one class in this transport feature should implement `ServerTransportSecurityValidator`: the MCP-facing composite adapter registered with the upstream builder.
- Leaf validation components for access token, loopback origin, and protocol-version consistency should become local transport header constraints rather than direct MCP validator implementations.
- ShardingSphere-specific protocol-version validation should be adapted into the MCP header-validation chain rather than preserved as a separate servlet-side request validator.
- Session existence, Accept-header requirements, JSON-RPC parsing, and other request/session lifecycle errors should fall back to MCP's own request handling rather than being duplicated in ShardingSphere servlet code.
- The current custom JSON error body contract such as `{"message":"Unauthorized."}` is not required to remain compatible in this feature. MCP-native error rendering is acceptable.
- Success-path behavior for initialize, follow-up requests, stream opening, and session deletion must remain functionally available after the refactor.
- `normalizeInitializeRequest(...)`, default Accept-header supplementation, and negotiated protocol-response header behavior may remain in `StreamableHttpMCPServlet` if they are still needed for successful protocol interoperability.
- This feature should remove parallel validation structures instead of leaving mixed-mode compatibility logic in place.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Use MCP's native validation hook as the only header-validation entry point (Priority: P1)

As an MCP transport maintainer, I want all pre-request header validation to run through MCP's registered `ServerTransportSecurityValidator` so that ShardingSphere no longer maintains a parallel servlet-side validation chain.

**Why this priority**: This is the core architectural correction. Without it, the transport remains split between an upstream validation mechanism and a custom front-running validation path.

**Independent Test**: Inspect the HTTP transport wiring and run targeted HTTP transport tests to verify that the servlet no longer calls `validateHeaders(...)` manually and that MCP still rejects invalid header-driven requests through the registered validator.

**Acceptance Scenarios**:

1. **Given** the HTTP transport is created, **When** the MCP provider builder is configured, **Then** it receives a real `ServerTransportSecurityValidator` implementation instead of `NOOP`.
2. **Given** an incoming HTTP request reaches MCP through the ShardingSphere servlet wrapper, **When** header validation is needed, **Then** validation is performed by MCP's native validator invocation path rather than by manual servlet-side header extraction and invocation.
3. **Given** a reviewer inspects the transport code, **When** they trace request validation entry points, **Then** they find one MCP-native header-validation chain instead of parallel ShardingSphere and MCP validation chains.

---

### User Story 2 - Adapt ShardingSphere-specific header constraints without coupling leaf constraints to MCP interfaces (Priority: P1)

As a transport maintainer, I want ShardingSphere-specific access-token, loopback-origin, and protocol-version constraints to be expressed as local header constraints so that MCP coupling exists only at the composite adapter boundary.

**Why this priority**: The architecture should clearly separate upstream MCP transport extension points from ShardingSphere-owned validation rules.

**Independent Test**: Unit-test local header constraints independently from MCP, then verify that the composite MCP adapter executes them in order and maps local failures to `ServerTransportSecurityException`.

**Acceptance Scenarios**:

1. **Given** a request is missing or has an invalid bearer token while access-token protection is enabled, **When** validation runs, **Then** the access-token constraint rejects it and the MCP-facing adapter exposes that as HTTP `401`.
2. **Given** the bind host is loopback-only and the request has a non-loopback `Origin`, **When** validation runs, **Then** the loopback-origin constraint rejects it and the MCP-facing adapter exposes that as HTTP `403`.
3. **Given** a follow-up request references an existing session but omits or mismatches the negotiated protocol header, **When** validation runs, **Then** the protocol-version constraint rejects it and the MCP-facing adapter exposes that as HTTP `400`.

---

### User Story 3 - Keep MCP responsible for request/session lifecycle errors (Priority: P1)

As a transport maintainer, I want session existence checks, Accept-header checks, malformed JSON-RPC handling, and other request/session lifecycle errors to remain in MCP's own request processing so that ShardingSphere does not duplicate upstream behavior.

**Why this priority**: The main reason the current design became tangled is that ShardingSphere duplicated parts of MCP's request-validation responsibility while only partially delegating the rest.

**Independent Test**: Run GET/POST/DELETE transport flows that trigger missing session IDs, missing sessions, bad Accept headers, and malformed request bodies, and verify that MCP's own request handling remains the owner of those responses.

**Acceptance Scenarios**:

1. **Given** a follow-up request references a missing session ID header, **When** the request reaches MCP, **Then** the resulting `400` comes from MCP request handling rather than from a ShardingSphere-specific pre-validator.
2. **Given** a follow-up request references a session ID that does not exist, **When** the request reaches MCP, **Then** the resulting `404` comes from MCP session lookup rather than from a ShardingSphere-specific pre-validator.
3. **Given** a request body is malformed JSON-RPC, **When** the request reaches MCP, **Then** MCP returns its own invalid-request error behavior without a ShardingSphere wrapper rewriting that branch.

---

### User Story 4 - Preserve successful MCP HTTP workflows while accepting MCP-native error rendering (Priority: P2)

As a ShardingSphere MCP client integrator, I want successful initialize, follow-up, stream, and delete flows to continue working even though header-validation failures now use MCP-native error rendering instead of the current custom JSON message envelope.

**Why this priority**: The refactor is acceptable only if the architecture improves without breaking the supported successful transport workflows.

**Independent Test**: Run focused HTTP transport integration scenarios for initialize, authorized follow-up requests, invalid protocol follow-up requests, stream opening, and delete operations, and verify that success flows still work while header-validation error rendering follows MCP-native behavior.

**Acceptance Scenarios**:

1. **Given** a valid initialize request without a supported protocol version in the body, **When** the request succeeds, **Then** ShardingSphere still normalizes the protocol version and returns the negotiated version in the response.
2. **Given** an authorized follow-up request for an existing session with the correct protocol header, **When** the request is processed, **Then** the follow-up flow still succeeds.
3. **Given** a header-validation failure such as invalid token, invalid origin, or protocol mismatch, **When** MCP rejects the request, **Then** callers can rely on the HTTP status code but are no longer promised the current custom ShardingSphere JSON message body.

---

### Edge Cases

- What happens when a request is an initialize request and therefore has no session ID yet?
- What happens when a request includes a session ID for a session that does not exist?
- What happens when a request has a session ID but the protocol header is absent?
- What happens when a request has a session ID but the protocol header does not match the negotiated session protocol?
- What happens when the bind host is not loopback and an arbitrary `Origin` is supplied?
- What happens when a request is rejected for header reasons but also has other request-format problems?
- What happens when the caller still tries to parse every header-validation failure body as the old `{"message": ...}` format?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST register a non-`NOOP` `ServerTransportSecurityValidator` with `HttpServletStreamableServerTransportProvider.builder()` for MCP HTTP transport.
- **FR-002**: `StreamableHttpMCPServlet` MUST NOT manually call `validateHeaders(...)` on transport validators.
- **FR-003**: The system MUST expose exactly one MCP-facing HTTP transport validator implementation per servlet instance, and that implementation MUST be the composite adapter registered with the MCP builder.
- **FR-004**: Leaf header-validation components owned by ShardingSphere MUST NOT implement `ServerTransportSecurityValidator` directly.
- **FR-005**: The system MUST define a local transport-header constraint abstraction for ShardingSphere-owned header validation logic.
- **FR-006**: The system MUST define a local exception type for transport-header constraint failures and MUST map that exception to `ServerTransportSecurityException` only at the MCP-facing adapter boundary.
- **FR-007**: The access-token constraint MUST reject missing or invalid bearer tokens with HTTP `401` when access-token protection is enabled.
- **FR-008**: The loopback-origin constraint MUST reject non-loopback `Origin` values with HTTP `403` when the bind host is loopback-only.
- **FR-009**: The protocol-version constraint MUST validate `MCP-Protocol-Version` only for follow-up requests that reference an existing session.
- **FR-010**: The protocol-version constraint MUST reject missing protocol headers for existing-session follow-up requests with HTTP `400`.
- **FR-011**: The protocol-version constraint MUST reject mismatched protocol headers for existing-session follow-up requests with HTTP `400`.
- **FR-012**: The protocol-version constraint MUST NOT claim ownership of missing-session or unknown-session outcomes; requests without a known session MUST fall through to MCP's own session handling.
- **FR-013**: The system MUST remove the ShardingSphere-specific `StreamableHttpMCPRequestValidator` layer instead of preserving it in parallel with MCP-native validation.
- **FR-014**: The system MUST let MCP retain ownership of request/session lifecycle errors including missing session IDs, unknown sessions, invalid Accept headers, malformed JSON-RPC payloads, and generic request-processing errors.
- **FR-015**: The system MUST accept MCP-native error rendering for header-validation failures and MUST NOT preserve the current custom JSON message envelope as a compatibility requirement in this feature.
- **FR-016**: The system MUST preserve successful initialize, authorized follow-up, stream, and delete transport workflows after the refactor.
- **FR-017**: The system MUST preserve initialize-request protocol normalization and negotiated response-header publication if they remain necessary for successful MCP interoperability.
- **FR-018**: The system MUST preserve default Accept-header supplementation if that behavior is still required for successful MCP interoperability.
- **FR-019**: The composite MCP-facing validator MUST execute ShardingSphere-owned local header constraints in deterministic order.
- **FR-020**: The composite MCP-facing validator MUST short-circuit on the first failing local header constraint.
- **FR-021**: The implementation MUST remove obsolete servlet-side helper code that exists only to support the previous manual header-validation path.
- **FR-022**: Unit and integration tests MUST be updated to assert MCP-native error ownership boundaries rather than the previous ShardingSphere-specific JSON error body contract.
- **FR-023**: Transport tests MUST continue to verify status-code correctness for token, origin, protocol-version, missing-session, and malformed-request failure scenarios after the refactor.
- **FR-024**: Reviewer-facing design artifacts MUST make it clear which failures are header-rule failures owned by the MCP validator adapter and which are request/session lifecycle failures owned by MCP request handling.

### Key Entities *(include if feature involves data)*

- **MCP Validator Adapter**: The sole `ServerTransportSecurityValidator` implementation registered with the MCP builder for HTTP transport.
- **Transport Header Constraint**: A ShardingSphere-owned header-validation component that checks one transport precondition without directly implementing MCP interfaces.
- **Transport Header Constraint Failure**: A local validation failure with an HTTP status code and message that is later mapped to `ServerTransportSecurityException`.
- **Existing Session Follow-Up Request**: An HTTP MCP request that carries `MCP-Session-Id` and refers to a session already known to the runtime.
- **MCP-Owned Request/Session Error**: A failure produced by MCP's own GET/POST/DELETE request handling after header validation, such as missing session ID, unknown session, invalid Accept header, malformed body, or internal processing failure.
- **Negotiated Protocol Response Header**: The `MCP-Protocol-Version` header ShardingSphere adds to initialize responses after protocol normalization succeeds.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `StreamableHttpMCPServlet` no longer contains a manual `validateHeaders(...)` invocation path.
- **SC-002**: The HTTP transport registers exactly one non-`NOOP` MCP validator adapter through `HttpServletStreamableServerTransportProvider.builder()`.
- **SC-003**: Access-token, loopback-origin, and protocol-version validation each have dedicated local constraint tests plus composite adapter tests.
- **SC-004**: HTTP transport integration tests continue to pass for successful initialize/follow-up/delete flows after the validation refactor.
- **SC-005**: HTTP transport tests verify correct status codes for token, origin, protocol-version, missing-session, and malformed-request failures without depending on the old custom JSON message envelope.
- **SC-006**: Reviewers can explain from code and tests alone which error branches are owned by MCP validator adaptation and which are owned by MCP request/session processing.

## Assumptions

- The MCP upstream transport provider's `ServerTransportSecurityValidator` hook is the only intended extension point for header-level pre-request validation in this transport.
- The MCP product remains pre-release enough that moving from a ShardingSphere-specific JSON error envelope to MCP-native error rendering is acceptable.
- Protocol-version consistency for existing sessions is best modeled as a transport-header precondition rather than as a parallel servlet-side request validator.
- Session existence itself remains an MCP request/session lifecycle concern rather than a ShardingSphere-owned header-rule concern.

## Out of Scope

- Redesigning MCP business workflows, tool semantics, or resource semantics.
- Preserving the current custom JSON error body as a backward-compatibility requirement.
- Introducing a second request-validation SPI parallel to MCP's `ServerTransportSecurityValidator`.
- Rewriting MCP upstream request/session lifecycle behavior.
- Changing non-HTTP MCP transports.
