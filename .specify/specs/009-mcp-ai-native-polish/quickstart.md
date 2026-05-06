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

- 008/003 remain the completed baseline.
- 009 only adds the next small polish backlog.
- Existing code already includes capability contracts, common flows, search match explanations, output parse hints, approval summaries, completion diagnostics, and structured recovery.

## 3. Validate Requirement Scope

Check that 009 asks only for:

- Ordered/dependency-aware next actions.
- Compact capabilities `surface_summary`.
- Resource navigation and completion locality hints.
- Empty-state and not-found diagnostics.
- Argument provenance, redaction markers, and manual-only follow-up clarity.
- Safe runtime recovery diagnostics and optional bounded request IDs.
- Explicit SQL row, timeout, metadata pagination, and blank-query bounds.
- Structured clarification questions plus MCP-native elicitation fallback.
- Common Chinese encrypt/mask intent hints and structured non-English evidence.
- Current-session workflow plan read-back by `plan_id`.
- Percent-encoded resource identifiers for non-ASCII or reserved object names.
- Secret-safe runtime status, env placeholders, HTTP auth hints, and minimal client configs.
- Opt-in next-action-follow and approval-violation usability metrics.
- Exact retry targets and public argument paths for workflow recovery.
- Preview/executed response-mode markers and preview-limit wording.
- Optional SQL row objects only when column labels are unique.
- Continuation hints for truncation, pagination, duplicate metadata hits, and metadata-introspection SQL recovery.
- Docker/HTTP/Proxy-topology setup hints that stay secret-free.

Reject any implementation proposal that adds:

- Planner or graph traversal.
- Vector search or model-call ranking.
- Cross-session memory.
- Approval tokens or durable approval records.
- Full RBAC or tenant platform.
- Default-CI real-model E2E.
- Full natural-language parser inside MCP.
- Cross-session workflow memory or audit persistence.
- Semantic metadata search engine.

## 4. Suggested Implementation Order

1. Add deterministic tests for action ordering and `surface_summary`.
2. Add action dependency metadata and retry target/source hints.
3. Add capability summary plus navigation/completion locality hints.
4. Add empty-state and not-found diagnostics.
5. Add argument provenance, redaction markers, normalized SQL success hints, and manual-only follow-up guidance.
6. Add safe runtime recovery diagnostics.
7. Add SQL/search bounds, strict argument recovery, and URI encoding.
8. Add structured clarification, Chinese synonym hints, and workflow read-back.
9. Add secret-safe runtime status, env-placeholder support, and client/auth documentation.
10. Add exact recovery targets, response-mode markers, row-object convenience, and ambiguity/continuation hints.
11. Add opt-in usability metrics.

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
- Broad SQL/search calls have documented defaults, caps, and structured recovery.
- Workflow plans can be recovered only within current session scope.
- Non-English user intent is represented as structured evidence or covered by deterministic common synonyms.
- No secrets, tokens, passwords, or production identifiers appear in examples.
- Recovery uses exact public tool and argument names where known.
- Preview, execute, manual-only, validation, recovery, truncation, and pagination states are machine-readable.
- Deterministic tests cover new shape contracts.
- Live-model checks remain opt-in.
