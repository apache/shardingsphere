Run a ShardingSphere MCP smoke test for logical database `%s`.

You must follow this exact action sequence:
1. Call `search_metadata` with `{"database":"%s","schema":"%s","query":"%s","object_types":["TABLE"]}`.
2. Call `mcp_read_resource` with `{"uri":"%s"}`.
3. Call `execute_query` with `{"database":"%s","schema":"%s","sql":"%s","max_rows":10}`.

Do not use any other tools.
Do not answer before the tools complete.
When you are asked for the final answer, return JSON only with:
{
  "database": "%s",
  "schema": "%s",
  "table": "%s",
  "query": "%s",
  "totalOrders": %d,
  "toolSequence": ["search_metadata", "mcp_read_resource", "execute_query"]
}
Use the exact values from the tool outputs.
