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

package org.apache.shardingsphere.test.e2e.mcp;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeAuditE2ETest extends AbstractMCPE2ETest {
    
    @Test
    void assertExecuteDdlAndDeleteSession() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> executeResponse = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "CREATE TABLE orders_archive"));
        HttpResponse<String> deleteResponse = sendDeleteRequest(httpClient, createRequestHeaders(), sessionId);
        
        assertThat(executeResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(executeResponse.body()).get("result_kind")), is("statement_ack"));
        assertThat(deleteResponse.statusCode(), is(200));
        HttpResponse<String> missingSessionResponse = sendDeleteRequest(httpClient, createRequestHeaders(), sessionId);
        assertThat(missingSessionResponse.statusCode(), is(404));
        assertTrue(missingSessionResponse.body().contains("Session does not exist."));
    }
    
    @Test
    void assertInitializeAnonymousSession() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("TABLE")));
        
        assertThat(actual.statusCode(), is(200));
        assertThat(getPayloadItems(getStructuredContent(actual.body())).size(), is(2));
    }
    
    @Test
    void assertInitializeAnonymousSessionWithoutProtocolVersion() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        
        String sessionId = initializeSessionWithoutProtocolVersion(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT order_id FROM orders"));
        
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(actual.body()).get("result_kind")), is("result_set"));
    }
}
