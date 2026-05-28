+++
title = "Feature Plugins"
weight = 7
chapter = true
+++

ShardingSphere-MCP extends domain capabilities through feature plugins.
The MCP runtime provides transport, sessions, descriptor discovery, metadata, and workflow infrastructure. Feature plugins provide concrete tools, resources, and business semantics.

The packaged distribution includes these official MCP feature plugins:

- Encrypt: plan, apply, and validate data encryption rules.
- Mask: plan, apply, and validate data masking rules.

Additional or third-party feature plugins can be added to the runtime classpath through the `plugins/` directory.
If a feature plugin is not packaged by default, prepare its required ShardingSphere modules and third-party jars before startup.
