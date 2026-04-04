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
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ExecuteQueryTransactionE2ETest extends AbstractMCPE2ETest {
    
    @Test
    void assertExecuteSelectOverHttpSession() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "max_rows", 10));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("result_set"));
    }
    
    @Test
    void assertExecuteBeginAndCommit() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> beginActual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        HttpResponse<String> commitActual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "COMMIT"));
        
        assertThat(beginActual.statusCode(), is(200));
        assertThat(commitActual.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(commitActual.body()).get("message")), is("Transaction committed."));
    }
    
    @Test
    void assertExecuteSavepointWithSupportedDatabase() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SAVEPOINT sp_1"));
        
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(actual.body()).get("message")), is("Savepoint created."));
    }
    
    @Test
    void assertExecuteSavepointWithUnsupportedDatabase() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "warehouse", "schema", "warehouse", "sql", "SAVEPOINT sp_1"));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("unsupported"));
    }
    
    @Test
    void assertExecuteCrossDatabaseTransactionSwitchRejected() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "analytics_db", "schema", "public", "sql", "BEGIN"));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("transaction_state_error"));
    }
    
    @Test
    void assertExecuteSingleStatementValidation() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1; SELECT 2"));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("invalid_request"));
    }
}
