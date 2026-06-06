+++
title = "Feature Plugins"
weight = 8
chapter = true
+++

ShardingSphere-MCP extends domain capabilities through feature plugins.
When a feature plugin needs a multi-step governance change, it uses the [Rule Change Flow](plugin-workflow/) for requirement confirmation, preview, apply, and validation.

The packaged distribution includes these official MCP feature plugins:

- Data Encryption: plan, review, apply, and validate data encryption rules.
- Data Masking: plan, review, apply, and validate data masking rules.
- Broadcast: plan, review, apply, and validate broadcast table rules.
- Readwrite-Splitting: plan, review, apply, and validate readwrite-splitting rules and status changes.
- Shadow: plan, review, apply, and validate shadow rules, default shadow algorithms, and unused algorithm cleanup.
- Sharding: plan, review, apply, and validate sharding table rules, reference rules, default strategies, key generation, and unused component cleanup.

When using additional feature plugins, follow the plugin provider's instructions, prepare the required jars and dependencies, and place them under the distribution `plugins/` directory before startup.
