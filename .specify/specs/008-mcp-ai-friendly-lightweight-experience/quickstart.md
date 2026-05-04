# Quickstart: MCP AI-Friendly Lightweight Experience

This quickstart describes how to validate the requirements without switching branches.

## 1. Confirm branch and working tree

```bash
git branch --show-current
git status --short
```

Expected result:

- Branch remains `001-shardingsphere-mcp`.
- No branch switch or branch creation is required.

## 2. Check current public surface

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

## 3. Validate documentation consistency

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

## 4. Validate side-effect SQL guidance

Call `execute_update` with `execution_mode=preview` for one supported side-effecting statement.

Expected result:

- Response contains `would_execute=false`.
- Response contains `side_effect_scope`.
- Response contains `requires_user_approval=true`.
- Response contains reusable `suggested_arguments`.
- Response tells the model to ask the user before executing.

## 5. Validate common recovery paths

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

## 6. Validate descriptor quality

Run the descriptor lint tests after implementation.

Expected checks:

- No empty descriptions.
- No placeholder descriptions.
- Side-effecting tools expose side-effect and approval metadata.
- Enum fields list values.
- Core output schemas keep key fields.
- Navigation entries resolve to public identifiers.

## 7. Validate lightweight contract and optional LLM scenarios

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
