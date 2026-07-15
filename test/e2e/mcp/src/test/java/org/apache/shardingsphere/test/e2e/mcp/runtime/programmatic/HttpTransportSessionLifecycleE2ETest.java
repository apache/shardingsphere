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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HttpTransportSessionLifecycleE2ETest extends AbstractHttpProtocolOnlyE2ETest {
    
    @Test
    void assertRejectOpenStreamAfterDelete() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        sendDeleteRequest(httpClient, createSessionHeaders(sessionId));
        HttpResponse<String> actual = openEventStream(httpClient, Map.of(
                "Accept", "text/event-stream",
                "MCP-Session-Id", sessionId,
                "MCP-Protocol-Version", getProtocolVersion()));
        assertThat(actual.statusCode(), is(405));
    }
    
    @Test
    void assertRejectFollowUpRequestWithoutSessionHeader() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        initializeSession(httpClient);
        HttpResponse<String> actual = sendCapabilitiesRequest(httpClient, Map.of("MCP-Protocol-Version", getProtocolVersion()));
        assertThat(actual.statusCode(), is(400));
    }
    
    @Test
    void assertRejectFollowUpRequestWithMalformedRequestBody() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendRawPostRequest(httpClient, createSessionHeaders(sessionId), "not-json");
        assertThat(actual.statusCode(), is(400));
    }
    
    @Test
    void assertRejectDeleteForClosedSession() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> firstDelete = sendDeleteRequest(httpClient, createSessionHeaders(sessionId));
        HttpResponse<String> secondDelete = sendDeleteRequest(httpClient, createSessionHeaders(sessionId));
        assertThat(firstDelete.statusCode(), is(200));
        assertThat(secondDelete.statusCode(), is(404));
    }
    
    @Test
    void assertDeleteKeepsOtherSession() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String firstSessionId = initializeSession(httpClient);
        String secondSessionId = initializeSession(httpClient);
        HttpResponse<String> deleteResponse = sendDeleteRequest(httpClient, createSessionHeaders(firstSessionId));
        HttpResponse<String> firstSessionResponse = sendCapabilitiesRequest(httpClient, createSessionHeaders(firstSessionId));
        HttpResponse<String> secondSessionResponse = sendCapabilitiesRequest(httpClient, createSessionHeaders(secondSessionId));
        assertThat(deleteResponse.statusCode(), is(200));
        assertThat(firstSessionResponse.statusCode(), is(404));
        assertThat(secondSessionResponse.statusCode(), is(200));
    }
}
