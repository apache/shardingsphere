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

public interface CrossDatabaseTransactionContractTest {
    
    void launchContractRuntime() throws IOException;
    
    HttpClient createContractHttpClient();
    
    String initializeContractSession(HttpClient httpClient) throws IOException, InterruptedException;
    
    HttpResponse<String> callContractTool(HttpClient httpClient, String sessionId, String toolName, Map<String, Object> arguments) throws IOException, InterruptedException;
    
    Map<String, Object> getContractStructuredContent(String responseBody);
    
    String getPrimaryDatabaseName();
    
    String getSecondaryDatabaseName();
    
    String getSecondaryDatabaseSwitchSql();
    
    default void assertCrossDatabaseTransactionPayload(final Map<String, Object> payload) {
    }
    
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
