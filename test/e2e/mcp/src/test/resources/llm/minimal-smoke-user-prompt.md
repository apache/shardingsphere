Run a ShardingSphere MCP smoke test for logical database `%s`.

You must follow this exact tool sequence:
1. Call `list_tables` with `{"database":"%s","schema":"%s"}`.
2. Call `describe_table` with `{"database":"%s","schema":"%s","table":"%s"}`.
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
  "toolSequence": ["list_tables", "describe_table", "execute_query"]
}
Use the exact values from the tool outputs.
