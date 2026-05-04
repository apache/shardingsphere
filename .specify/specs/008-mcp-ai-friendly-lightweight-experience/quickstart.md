# Quickstart: MCP AI-Friendly Lightweight Experience

This quickstart describes how to validate the requirements without switching branches.

## 1. Confirm branch and working tree

```bash
git branch --show-current
git status --short
```

Expected result:

- Branch remains the current working branch.
- No branch switch or branch creation is required.

For documentation-only requirement updates, also run:

```bash
git diff --check -- .specify/specs/008-mcp-ai-friendly-lightweight-experience docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md
```

Expected result:

- No whitespace errors.
- Only the intended Spec Kit and MCP requirement documents are changed.

## 2. Confirm Phase 0 analysis notes

Before implementation starts for a P0 slice, confirm the analysis note records:

- Current behavior evidence.
- Inspected descriptor, handler, resource, workflow, completion, test, or README paths.
- Minimal affected paths.
- Verification map.
- Explicit non-goals.
- Rollback boundary.

Expected result:

- The implementation slice does not start from requirement text alone.
- Code facts that conflict with the requirement are resolved in docs or design before coding.

## 3. Check current public surface

Build or run an MCP test fixture, then read capabilities:

```bash
./mvnw -pl test/e2e/mcp -am install -DskipTests -DskipITs -Dspotless.skip=true -B -ntp
```

Expected model-facing sections in `shardingsphere://capabilities`:

- `resources`
- `resourceTemplates`
- `tools`
- `prompts`
- `completionTargets`
- `resourceNavigation`
- `protocolAvailability`
- `fingerprints`

## 4. Validate documentation consistency

Compare the public tool list in:

- `mcp/README.md`
- `mcp/README_ZH.md`
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`
- `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`
- `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`
- `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`

Expected result:

- README lists only tools exposed by descriptors and capabilities.
- Old PRD-only tool names are not presented as the current implementation contract.

## 5. Validate side-effect SQL guidance

Call `execute_update` with `execution_mode=preview` for one supported side-effecting statement.

Expected result:

- Response contains `would_execute=false`.
- Response contains `side_effect_scope`.
- Response contains `requires_user_approval=true`.
- Response contains reusable `suggested_arguments`.
- Response tells the model to ask the user before executing.

## 6. Validate metadata URI hints

Call `search_metadata` for a known logical table, column, index, view, or sequence.

Expected result:

- Each safely derivable hit includes `resource_uri`.
- Parent or next-hop URIs are included only when they can be derived from descriptor-backed patterns.
- The response does not invent physical object names, secrets, or guessed paths.

## 7. Validate output schema alignment

Compare descriptor-visible schemas with real responses for:

- `search_metadata`
- `execute_query`
- `execute_update`
- `plan_encrypt_rule`
- `plan_mask_rule`
- `apply_workflow`
- `validate_workflow`

Expected result:

- Field names, enum casing, required fields, nested objects, and common states match the real payloads.
- Complex states are explained by schema fields or compact examples.

## 8. Validate common recovery paths

Trigger the following errors:

- Missing `database`.
- Missing `execution_mode`.
- Mutating SQL sent to `execute_query`.
- Unknown resource or tool.
- Unknown or unavailable `plan_id`.

Expected result:

- Each response includes a structured `recovery` object when safe.
- Recovery never invents hidden values or secrets.
- Side-effect recovery recommends preview and preserves approval semantics.

## 9. Validate descriptor quality

Run the descriptor lint tests after implementation.

Expected checks:

- No empty descriptions.
- No placeholder descriptions.
- Side-effecting tools expose side-effect and approval metadata.
- Enum fields list values.
- Core output schemas keep key fields.
- Navigation entries resolve to public identifiers.

## 10. Validate lightweight contract and optional LLM scenarios

Run deterministic contract tests first:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

Run opt-in LLM usability scenarios only when the local model environment is ready:

```bash
./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilitySuiteE2ETest -Dsurefire.failIfNoSpecifiedTests=true -Dmcp.e2e.llm.enabled=true -B -ntp
```

Expected result:

- Deterministic tests do not require model credentials.
- Real-model tests remain opt-in.

## 11. Validate P1/P2 only after P0

P1 validation should be added only when each P1 item is implemented:

- Current-session workflow plan lookup returns only current-session plans.
- Metadata resources expose lightweight navigation without a graph engine.
- Complex tool examples are static and secret-free.
- Completion ordering improves with supplied context without model calls or vector search.
- Algorithm resources expose property templates without requiring a workflow plan first.
- Metadata freshness hints are present without adding active refresh.

P2 validation should be added only when each P2 item is implemented:

- Startup output clarifies HTTP, STDIO, token, config, log, and database-count expectations.
- Environment variable references resolve or fail with clear messages.
- Troubleshooting docs cover first-run failures.
- LLM usability scenarios remain opt-in.
