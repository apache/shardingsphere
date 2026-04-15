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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeAuditE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    @Test
    void assertExecuteDdlAndDeleteSession() throws IOException, InterruptedException {
        launchHttpProgrammaticRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> executeResponse = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "CREATE TABLE orders_archive"));
        HttpResponse<String> deleteResponse = sendDeleteRequest(httpClient, sessionId);
        
        assertThat(executeResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(executeResponse.body()).get("result_kind")), is("statement_ack"));
        assertThat(deleteResponse.statusCode(), is(200));
        HttpResponse<String> missingSessionResponse = sendDeleteRequest(httpClient, sessionId);
        assertThat(missingSessionResponse.statusCode(), is(404));
        assertTrue(missingSessionResponse.body().contains("Session does not exist."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertInitializeAnonymousSessionCases")
    void assertInitializeAnonymousSession(final String name, final boolean protocolVersionIncluded,
                                          final String toolName, final Map<String, Object> arguments,
                                          final String expectedKey, final String expectedValue) throws IOException, InterruptedException {
        launchHttpProgrammaticRuntime();
        HttpClient httpClient = createHttpClient();
        
        String sessionId = protocolVersionIncluded ? initializeSession(httpClient) : initializeSessionWithoutProtocolVersion(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, toolName, arguments);
        
        assertThat(actual.statusCode(), is(200));
        if ("item_count".equals(expectedKey)) {
            assertThat(String.valueOf(getPayloadItems(getStructuredContent(actual.body())).size()), is(expectedValue));
            return;
        }
        assertThat(String.valueOf(getStructuredContent(actual.body()).get(expectedKey)), is(expectedValue));
    }
    
    static Stream<Arguments> assertInitializeAnonymousSessionCases() {
        return Stream.of(
                Arguments.of("with protocol version", true, "search_metadata",
                        Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("TABLE")),
                        "item_count", "2"),
                Arguments.of("without protocol version", false, "execute_query",
                        Map.of("database", "logic_db", "schema", "public", "sql", "SELECT order_id FROM orders"),
                        "result_kind", "result_set"),
                Arguments.of("without protocol version metadata search", false, "search_metadata",
                        Map.of("database", "logic_db", "schema", "public", "query", "active", "object_types", List.of("VIEW")),
                        "item_count", "1"));
    }
}
