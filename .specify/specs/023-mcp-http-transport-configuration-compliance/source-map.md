<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Source Map: MCP HTTP Transport Configuration Compliance

## Primary Configuration Paths

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/OAuthIntrospectionConfiguration.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlOAuthIntrospectionConfiguration.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidator.java`

## Primary Runtime Paths

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncher.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServer.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandler.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServlet.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidator.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpOAuthTokenIntrospector.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactory.java`

## Configuration And Documentation Paths

- `distribution/mcp/src/main/resources/conf/mcp-http.yaml`
- `distribution/mcp/src/main/resources/conf/mcp-stdio.yaml`
- `mcp/README.md`
- `mcp/README_ZH.md`
- `docs/mcp/ShardingSphere-MCP-Detailed-Design.md`
- `docs/mcp/ShardingSphere-MCP-Technical-Design.md`

## Test And E2E Paths

- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/MCPDocumentationContractTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPLaunchConfigurationTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServerTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandlerTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServletTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidatorTest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/runtime/AbstractConfigBackedRuntimeE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/distribution/PackagedDistributionTestSupport.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/distribution/PackagedDistributionTestSupportTest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportAccessTokenE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportOAuthIntrospectionE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSessionLifecycleE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionSmokeE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/MCPBuilderEvaluationArtifactTest.java`
- `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/client/LLMChatModelClient.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/artifact/LLME2EArtifactWriterTest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/ExecuteQueryTransactionE2ETest.java`

## Candidate New Test Paths

- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidatorTest.java`

## Field To Source Mapping

- `transport.http.enabled`: remove; replaced by `transport.type`.
- `transport.stdio.enabled`: remove; replaced by `transport.type`.
- `transport.type`: add as the only transport selector; allowed ShardingSphere YAML enum values are `STREAMABLE_HTTP` and `STDIO`.
- `transport.http`: keep only for `transport.type: STREAMABLE_HTTP`; reject it when `transport.type: STDIO`.
- `bindHost`: keep as optional; Tomcat connector address, startup log, and runtime HTTP safety input. Default is `127.0.0.1`. Validate it as a local bind host/address, not a URL, Origin, path, or remote mode.
- `allowRemoteAccess`: remove without replacement; `remote` and `exposure` are not approved design targets.
- `accessToken`: remove or reject as unsupported; no static shared-secret authorization is retained in this slice.
- `port`: keep as optional; Tomcat connector port and startup log. Default is `18088`. Allow `0` only as explicit ephemeral-port test or embedded usage where the actual runtime port is read after startup.
- `endpointPath`: keep as optional; local MCP endpoint mapping and servlet delegate endpoint. Default is `/mcp`. Validate it as one absolute path with exactly one leading slash and no scheme, host, query, or fragment.
- `allowedOrigins`: remove from YAML; MCP-required Origin handling remains internal runtime behavior.
- `remote`: remove; not an approved design target.
- `exposure`: remove; not an approved design target.
- `authorization`: out of current scope; do not add an authorization group in this slice.
- `oauth`: out of current scope; do not add an OAuth group in this slice.
- `authorizationServers`: out of current scope; future MCP OAuth package only.
- `scopesSupported`: out of current scope; future MCP OAuth package only.
- `protectedResource`: out of current scope; future MCP OAuth package only.
- `oauthIntrospection`: out of current scope; future MCP OAuth package only.
- `tokenValidation`: out of current scope; future MCP OAuth package only.
- `bearerMethodsSupported`: out of current scope; future MCP OAuth package only.
- `expectedIssuer`: out of current scope; future MCP OAuth package only.
- Unknown fields under `transport` or `transport.http`: reject rather than silently ignore.

## Listener Validation Boundary

- `bindHost` examples:
  - `127.0.0.1`: default local loopback binding.
  - `0.0.0.0`: all IPv4 interfaces; explicit remote exposure risk.
  - A concrete local interface IP: one interface only.
- `bindHost` non-examples:
  - `http://127.0.0.1:18088`: URL, not a bind host.
  - `https://gateway.example.test`: Origin/URL, not a bind host.
  - `/mcp`: path, not a bind host.
- `endpointPath` examples:
  - `/mcp`
  - `/api/mcp`
- `endpointPath` non-examples:
  - `mcp`: missing leading slash.
  - `//mcp`: ambiguous double leading slash.
  - `/mcp?debug=true`: query is not part of the endpoint path.
  - `/mcp#fragment`: fragment is not part of the endpoint path.
  - `https://example.test/mcp`: URL, not a local endpoint path.

## E2E Cleanup Classification

- Internal Origin matrix for this slice:
  - Missing Origin is accepted for non-browser MCP clients.
  - Malformed, `null`, or non-loopback present Origin returns HTTP 403.
  - Loopback Origin is accepted only for loopback-bound local HTTP.
  - Browser-origin remote allowlists remain future work outside this package.
- Delete current MCP HTTP authorization success-path coverage:
  - `HttpTransportAccessTokenE2ETest`
  - `HttpTransportOAuthIntrospectionE2ETest`
- Simplify current MCP HTTP security coverage:
  - `HttpTransportSecurityE2ETest` keeps internal Origin and binding coverage, but removes access token, OAuth, `allowedOrigins`, and authorization server fixtures.
  - `HttpTransportContractE2ETest` and `HttpTransportSessionLifecycleE2ETest` keep session/protocol behavior and add or confirm valid GET coverage for the same MCP endpoint.
- Update shared E2E fixture and distribution configuration coverage:
  - `AbstractConfigBackedRuntimeE2ETest`
  - `PackagedDistributionTestSupport`
  - `PackagedDistributionTestSupportTest`
  - `PackagedDistributionSmokeE2ETest`
- Update documentation and evaluation contract coverage:
  - `MCPDocumentationContractTest` must stop requiring old auth fields and must reject stale current-behavior examples.
  - `mcp-builder-evaluation.xml` and `MCPBuilderEvaluationArtifactTest` must replace the OAuth authorization category/evidence with current transport-security/configuration evidence.
- Preserve unrelated tests:
  - `LLMChatModelClient` and `LLME2EArtifactWriterTest` use external LLM provider authorization and secret redaction, not MCP HTTP authorization.
  - `ExecuteQueryTransactionE2ETest` uses SQL metadata introspection, not OAuth token introspection.

## Official Sources

- MCP Streamable HTTP transport: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- MCP Authorization: `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`

## Future Review Gates

- `mcp-builder` skill is a design sanity check for MCP server quality and must be used again when implementation touches MCP runtime, distribution, or MCP E2E paths.
- If a later package adds MCP OAuth Authorization, it must cite MCP Authorization, OAuth Protected Resource Metadata, OAuth Bearer Token Usage, and its chosen token validation mechanism as separate sources.
