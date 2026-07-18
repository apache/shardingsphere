/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.e2e.mcp.runtime.programmatic;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.SessionAttributionSourceConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionProtocolSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HttpTransportSessionAttributionE2ETest extends AbstractHttpProtocolOnlyE2ETest {
    
    private static final String SUBJECT_HEADER = "X-Test-Subject";
    
    private static final String SOURCE_HEADER = "X-Test-Source";
    
    private static final String ATTRIBUTE_HEADER = "X-Test-Attr-Region";
    
    @Test
    void assertAcceptMatchedSessionAttribution() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeAttributedSession(httpClient);
        assertThat(sendCapabilitiesRequest(httpClient, createAttributedSessionHeaders(sessionId, "subject")).statusCode(), is(200));
    }
    
    @Test
    void assertRejectChangedSessionAttribution() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeAttributedSession(httpClient);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, createAttributedSessionHeaders(sessionId, "other"));
        assertThat(actual.statusCode(), is(400));
        Map<String, Object> error = MCPInteractionPayloads.getRequiredObject(parseJsonBody(actual.body()), "error");
        Map<String, Object> data = MCPInteractionPayloads.getRequiredObject(error, "data");
        assertThat(MCPInteractionPayloads.getRequiredObject(data, "recovery").get("category"), is("session_attribution_mismatch"));
    }
    
    private String initializeAttributedSession(final HttpClient httpClient) throws IOException, InterruptedException {
        Map<String, String> headers = createAttributionHeaders("subject");
        HttpResponse<String> initializeResponse = sendInitializeRequest(
                httpClient, headers, MCPInteractionProtocolSupport.createInitializeRequestParams("mcp-e2e-programmatic"));
        assertThat(initializeResponse.statusCode(), is(200));
        String result = initializeResponse.headers().firstValue("MCP-Session-Id").orElseThrow();
        assertThat(sendRawPostRequest(httpClient, createAttributedSessionHeaders(result, "subject"),
                MCPInteractionProtocolSupport.createJsonRpcNotificationBody("notifications/initialized", Map.of())).statusCode(), is(202));
        return result;
    }
    
    private Map<String, String> createAttributedSessionHeaders(final String sessionId, final String subject) {
        Map<String, String> result = new LinkedHashMap<>(createSessionHeaders(sessionId));
        result.putAll(createAttributionHeaders(subject));
        return result;
    }
    
    private Map<String, String> createAttributionHeaders(final String subject) {
        return Map.of(SUBJECT_HEADER, subject, SOURCE_HEADER, "gateway", ATTRIBUTE_HEADER, "ap-south");
    }
    
    @Override
    protected HttpTransportConfiguration createHttpTransportConfiguration() {
        return new HttpTransportConfiguration("127.0.0.1", 0, getHttpEndpointPath(),
                new SessionAttributionSourceConfiguration(SUBJECT_HEADER, SOURCE_HEADER, "X-Test-Attr-"));
    }
}
