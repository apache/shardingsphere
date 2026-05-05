# Quickstart: MCP AI-Native Polish

Use this quickstart to review or implement the requirements without switching branches.

## 1. Confirm Branch Constraint

Run:

```bash
git branch --show-current
```

Expected:

- Current branch remains `001-shardingsphere-mcp`.
- Do not run `git switch`, `git checkout`, or branch-creating Speckit scripts.

## 2. Read the Baseline

Review:

- `.specify/specs/008-mcp-ai-friendly-lightweight-experience/spec.md`
- `specs/003-mcp-ai-friendly-guided-interaction/requirements.md`
- `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md`
- `mcp/README.md`
- `mcp/README_ZH.md`

Expected:

- 008 remains the completed baseline.
- 009 only adds the next small polish backlog.

## 3. Validate Requirement Scope

Check that 009 asks only for:

- Capability-level next-action contract.
- Static common flows.
- Search context and match explanations.
- Output parse hints for SQL and workflow payloads.
- Server-owned approval summaries.
- Focused deterministic guards.

Reject any implementation proposal that adds:

- Planner or graph traversal.
- Vector search or model-call ranking.
- Cross-session memory.
- Approval tokens or durable approval records.
- Full RBAC or tenant platform.
- Default-CI real-model E2E.

## 4. Suggested Implementation Order

1. Add or update tests for capabilities shape.
2. Add `next_action_contract` and `common_flows`.
3. Add `search_metadata` search context and match explanations.
4. Add SQL and workflow output parse hints and schema updates.
5. Add approval summaries to preview responses.
6. Add optional parity checks and opt-in LLM usability scenarios.

## 5. Suggested Verification Commands

For documentation-only changes:

```bash
git diff --check
```

For implementation changes touching MCP modules:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

For descriptor or capabilities changes:

```bash
./mvnw -pl mcp/support,mcp/core -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

For Checkstyle after Java changes:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -Pcheck -DskipTests checkstyle:check
```

## 6. Completion Checklist

- The branch was not switched.
- New model-facing fields are descriptor-backed or response-backed.
- Side effects still require preview and user approval.
- No secrets, tokens, passwords, or production identifiers appear in examples.
- Deterministic tests cover new shape contracts.
- Live-model checks remain opt-in.
