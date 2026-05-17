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

- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServerTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandlerTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServletTest.java`
- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidatorTest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportAccessTokenE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportOAuthIntrospectionE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java`

## Candidate New Test Paths

- `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidatorTest.java`

## Field To Source Mapping

- `enabled`: launcher and launch configuration validator.
- `bindHost`: Tomcat connector address, loopback validation, startup log, Origin validation.
- `allowRemoteAccess`: YAML validator only; remote exposure confirmation.
- `accessToken`: static bearer authorization and startup authorization status.
- `port`: Tomcat connector port and startup log.
- `endpointPath`: MCP endpoint mapping, servlet delegate endpoint, metadata endpoint path, default resource URI.
- `allowedOrigins`: remote Origin validation.
- `authorizationServers`: protected resource metadata and default expected issuer list.
- `scopesSupported`: protected resource metadata, challenge scope, and required scope validation.
- `protectedResource`: metadata `resource` and OAuth resource/audience validation.
- `oauthIntrospection`: OAuth token introspection endpoint, client credentials, issuer override, and validation cache TTL.

## Official Sources

- MCP Streamable HTTP transport: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- MCP Authorization: `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`
- OAuth Protected Resource Metadata: `https://www.rfc-editor.org/rfc/rfc9728.html`
- OAuth Token Introspection: `https://www.rfc-editor.org/rfc/rfc7662.html`
- OAuth Bearer Token Usage: `https://www.rfc-editor.org/rfc/rfc6750.html`
- OAuth Resource Indicators: `https://www.rfc-editor.org/rfc/rfc8707.html`
- MCP builder best practices reference: local installed `mcp-builder` skill loaded during this session; relevant points are copied into `research.md`.
