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

package org.apache.shardingsphere.test.e2e.mcp.runtime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Cross database transaction contract test.
 */
public interface CrossDatabaseTransactionContractTest {
    
    /**
     * Launch contract runtime.
     *
     * @throws IOException IO exception
     */
    void launchContractRuntime() throws IOException;
    
    /**
     * Create contract HTTP client.
     *
     * @return HTTP client
     */
    HttpClient createContractHttpClient();
    
    /**
     * Initialize contract session.
     *
     * @param httpClient http client
     * @return session ID
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    String initializeContractSession(HttpClient httpClient) throws IOException, InterruptedException;
    
    /**
     * Call contract tool.
     *
     * @param httpClient HTTP client
     * @param sessionId session ID 
     * @param toolName tool name
     * @param arguments arguments
     * @return HTTP response
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    HttpResponse<String> callContractTool(HttpClient httpClient, String sessionId, String toolName, Map<String, Object> arguments) throws IOException, InterruptedException;
    
    /**
     * Get contract structured content.
     *
     * @param responseBody response body
     * @return contract structured content
     */
    Map<String, Object> getContractStructuredContent(String responseBody);
    
    /**
     * Get primary database name.
     *
     * @return primary database name
     */
    String getPrimaryDatabaseName();
    
    /**
     * Get secondary database name.
     *
     * @return secondary database name
     */
    String getSecondaryDatabaseName();
    
    /**
     * Get secondary database switch SQL.
     *
     * @return secondary database switch SQL
     */
    String getSecondaryDatabaseSwitchSql();
    
    /**
     * Assert cross database transaction payload.
     * \
     * @param payload payload
     */
    default void assertCrossDatabaseTransactionPayload(final Map<String, Object> payload) {
    }
    
    /**
     * Assert reject cross database transaction switch.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    @Test
    default void assertRejectCrossDatabaseTransactionSwitch() throws IOException, InterruptedException {
        launchContractRuntime();
        HttpClient httpClient = createContractHttpClient();
        String sessionId = initializeContractSession(httpClient);
        callContractTool(httpClient, sessionId, "execute_query", Map.of("database", getPrimaryDatabaseName(), "schema", "public", "sql", "BEGIN"));
        HttpResponse<String> actual = callContractTool(httpClient, sessionId, "execute_query",
                Map.of("database", getSecondaryDatabaseName(), "schema", "public", "sql", getSecondaryDatabaseSwitchSql()));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getContractStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("transaction_state_error"));
        assertCrossDatabaseTransactionPayload(payload);
    }
}
