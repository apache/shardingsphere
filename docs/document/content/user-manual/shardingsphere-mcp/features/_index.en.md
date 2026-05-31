+++
title = "Feature Plugins"
weight = 8
chapter = true
+++

ShardingSphere-MCP extends domain capabilities through feature plugins.
When a feature plugin needs a multi-step governance change, it uses [Plugin Workflows](plugin-workflow/) for planning, preview, apply, and validation phases.

The packaged distribution includes these official MCP feature plugins:

- Data Encryption: plan, apply, and validate data encryption rules.
- Data Masking: plan, apply, and validate data masking rules.

Additional or third-party feature plugins can be added to the runtime classpath through the `plugins/` directory.
If a feature plugin is not packaged by default, prepare its required ShardingSphere modules and third-party jars before startup.
