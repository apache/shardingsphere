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

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionMultiDatabaseE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private String firstJdbcUrl;
    
    private String secondJdbcUrl;
    
    private boolean mixedDatabaseTypes;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-multi-first");
            secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-multi-second");
            H2RuntimeTestSupport.initializeDatabase(firstJdbcUrl);
            H2RuntimeTestSupport.initializeDatabase(secondJdbcUrl);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>();
        result.put("logic_db", new RuntimeDatabaseConfiguration(mixedDatabaseTypes ? "MySQL" : "H2", firstJdbcUrl, "", "", "org.h2.Driver"));
        result.put("analytics_db", new RuntimeDatabaseConfiguration(mixedDatabaseTypes ? "PostgreSQL" : "H2", secondJdbcUrl, "", "", "org.h2.Driver"));
        return result;
    }
    
    @Test
    void assertListDatabasesWithMultipleRuntimeDatabases() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases");
        
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getResourcePayload(actual.body()));
        assertThat(items.stream().map(each -> String.valueOf(each.get("database"))).toList(), hasItems("logic_db", "analytics_db"));
    }
    
    @Test
    void assertRejectCrossDatabaseTransactionSwitch() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "analytics_db", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id"));
        
        assertThat(actual.statusCode(), is(200));
        assertFalse(Boolean.parseBoolean(String.valueOf(getStructuredContent(actual.body()).get("ok"))));
        assertThat(String.valueOf(getStructuredContent(actual.body()).get("error_code")), is("transaction_state_error"));
    }
    
    @Test
    void assertRefreshMetadataVisibleAcrossSessionsForTargetDatabaseOnly() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String firstSessionId = initializeSession(httpClient);
        
        sendToolCallRequest(httpClient, firstSessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
        HttpResponse<String> firstSessionTables = sendResourceReadRequest(httpClient, firstSessionId, "shardingsphere://databases/logic_db/schemas/public/tables");
        String secondSessionId = initializeSession(httpClient);
        HttpResponse<String> secondSessionTables = sendResourceReadRequest(httpClient, secondSessionId, "shardingsphere://databases/logic_db/schemas/public/tables");
        HttpResponse<String> analyticsDatabaseTables = sendResourceReadRequest(httpClient, secondSessionId, "shardingsphere://databases/analytics_db/schemas/public/tables");
        List<String> firstSessionTableNames = getPayloadItems(getResourcePayload(firstSessionTables.body())).stream().map(each -> String.valueOf(each.get("table"))).toList();
        List<String> secondSessionTableNames = getPayloadItems(getResourcePayload(secondSessionTables.body())).stream().map(each -> String.valueOf(each.get("table"))).toList();
        List<String> analyticsDatabaseTableNames = getPayloadItems(getResourcePayload(analyticsDatabaseTables.body())).stream().map(each -> String.valueOf(each.get("table"))).toList();
        
        assertTrue(firstSessionTableNames.contains("orders_archive"));
        assertTrue(secondSessionTableNames.contains("orders_archive"));
        assertFalse(analyticsDatabaseTableNames.contains("orders_archive"));
        assertTrue(analyticsDatabaseTableNames.contains("orders"));
    }
    
    @Test
    void assertGetCapabilitiesForMixedDatabaseTypes() throws IOException, InterruptedException {
        mixedDatabaseTypes = true;
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> firstResponse = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic_db/capabilities");
        HttpResponse<String> secondResponse = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/analytics_db/capabilities");
        
        assertThat(firstResponse.statusCode(), is(200));
        assertThat(secondResponse.statusCode(), is(200));
        assertThat(String.valueOf(getResourcePayload(firstResponse.body()).get("databaseType")), is("MySQL"));
        assertThat(String.valueOf(getResourcePayload(secondResponse.body()).get("databaseType")), is("PostgreSQL"));
    }
}
