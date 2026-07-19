# MCP Standalone rule update follow-up

## Decision

Do not fix this issue as part of the current MCP and MCP E2E task. It is a pre-existing `mode/core` defect exposed by the new MCP Shadow lifecycle E2E, not a regression introduced by the MCP changes. Revisit it only when the user explicitly asks after the current task is complete.

## Reproduction

1. Run ShardingSphere-Proxy in Standalone mode.
2. Create a Shadow rule whose generated algorithm name is `shadow_rule_orders_value_match_0` and whose `value` property is `1`.
3. Execute `ALTER SHADOW RULE` with the same rule, table and algorithm type, changing only `value` to `2`.
4. The DistSQL execution reports success, but `SHOW SHADOW RULES` still returns `value=1`.

The MCP validation evidence was:

- expected algorithm properties: `{operation=insert, value=2, column=order_id}`
- actual algorithm properties: `{column=order_id, operation=insert, value=1}`

## Root cause

`VersionPersistService.persist` writes and activates the new metadata version. `DatabaseRulePersistService.persistTuples` then subtracts one from that version before returning `MetaDataVersion`. `StandaloneMetaDataManagerPersistService` compares the returned version with the active version and skips the in-memory rule-item refresh when they differ.

Initial creation is not affected because version zero remains zero after the existing `Math.max` expression. Updating an existing named item is affected because the active version advances while the returned version remains the previous one. Cluster mode does not consume this returned collection in the same way.

## Future minimum fix boundary

- `mode/core/src/main/java/org/apache/shardingsphere/mode/metadata/persist/config/database/DatabaseRulePersistService.java`
- `mode/core/src/test/java/org/apache/shardingsphere/mode/metadata/persist/config/database/DatabaseRulePersistServiceTest.java`
- A focused Standalone regression sentinel proving that an update to an existing named rule item refreshes the active configuration.

Before implementing, revalidate the version contract and all consumers of `DatabaseRulePersistService.persist`; do not change MCP code to work around the defect.
