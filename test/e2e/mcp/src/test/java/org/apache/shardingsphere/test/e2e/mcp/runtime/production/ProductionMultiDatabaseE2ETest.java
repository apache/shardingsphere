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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.runtime.CrossDatabaseTransactionContractTest;
import org.apache.shardingsphere.test.e2e.mcp.runtime.support.H2RuntimeTestSupport;
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

class ProductionMultiDatabaseE2ETest extends AbstractProductionRuntimeE2ETest implements CrossDatabaseTransactionContractTest {
    
    private static final String LOGIC_DATABASE_NAME = "logic_db";
    
    private static final String ANALYTICS_DATABASE_NAME = "analytics_db";
    
    private static final String H2_DRIVER_CLASS_NAME = "org.h2.Driver";
    
    private String firstJdbcUrl;
    
    private String secondJdbcUrl;
    
    private String firstDatabaseType = "H2";
    
    private String secondDatabaseType = "H2";
    
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
        result.put(LOGIC_DATABASE_NAME, createRuntimeDatabaseConfiguration(firstDatabaseType, firstJdbcUrl));
        result.put(ANALYTICS_DATABASE_NAME, createRuntimeDatabaseConfiguration(secondDatabaseType, secondJdbcUrl));
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
        assertThat(items.stream().map(each -> String.valueOf(each.get("database"))).toList(), hasItems(LOGIC_DATABASE_NAME, ANALYTICS_DATABASE_NAME));
    }
    
    @Test
    void assertRefreshMetadataVisibleAcrossSessionsForTargetDatabaseOnly() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String firstSessionId = initializeSession(httpClient);
        
        sendToolCallRequest(httpClient, firstSessionId, "execute_query",
                Map.of("database", LOGIC_DATABASE_NAME, "schema", "public", "sql", "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
        List<String> firstSessionTableNames = readTableNames(httpClient, firstSessionId, LOGIC_DATABASE_NAME);
        String secondSessionId = initializeSession(httpClient);
        List<String> secondSessionTableNames = readTableNames(httpClient, secondSessionId, LOGIC_DATABASE_NAME);
        List<String> analyticsDatabaseTableNames = readTableNames(httpClient, secondSessionId, ANALYTICS_DATABASE_NAME);
        
        assertTrue(firstSessionTableNames.contains("orders_archive"));
        assertTrue(secondSessionTableNames.contains("orders_archive"));
        assertFalse(analyticsDatabaseTableNames.contains("orders_archive"));
        assertTrue(analyticsDatabaseTableNames.contains("orders"));
    }
    
    @Test
    void assertGetCapabilitiesForMixedDatabaseTypes() throws IOException, InterruptedException {
        firstDatabaseType = "MySQL";
        secondDatabaseType = "PostgreSQL";
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
    
    private RuntimeDatabaseConfiguration createRuntimeDatabaseConfiguration(final String databaseType, final String jdbcUrl) {
        return new RuntimeDatabaseConfiguration(databaseType, jdbcUrl, "", "", H2_DRIVER_CLASS_NAME);
    }
    
    private List<String> readTableNames(final HttpClient httpClient, final String sessionId, final String databaseName) throws IOException, InterruptedException {
        HttpResponse<String> response = sendResourceReadRequest(httpClient, sessionId,
                String.format("shardingsphere://databases/%s/schemas/public/tables", databaseName));
        return getPayloadItems(getResourcePayload(response.body())).stream().map(each -> String.valueOf(each.get("table"))).toList();
    }
    
    @Override
    public void launchContractRuntime() throws IOException {
        launchProductionRuntime();
    }
    
    @Override
    public HttpClient createContractHttpClient() {
        return createHttpClient();
    }
    
    @Override
    public String initializeContractSession(final HttpClient httpClient) throws IOException, InterruptedException {
        return initializeSession(httpClient);
    }
    
    @Override
    public HttpResponse<String> callContractTool(final HttpClient httpClient, final String sessionId,
                                                 final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return sendToolCallRequest(httpClient, sessionId, toolName, arguments);
    }
    
    @Override
    public Map<String, Object> getContractStructuredContent(final String responseBody) {
        return getStructuredContent(responseBody);
    }
    
    @Override
    public String getPrimaryDatabaseName() {
        return LOGIC_DATABASE_NAME;
    }
    
    @Override
    public String getSecondaryDatabaseName() {
        return ANALYTICS_DATABASE_NAME;
    }
    
    @Override
    public String getSecondaryDatabaseSwitchSql() {
        return "SELECT status FROM orders ORDER BY order_id";
    }
    
    @Override
    public void assertCrossDatabaseTransactionPayload(final Map<String, Object> payload) {
        assertFalse(Boolean.parseBoolean(String.valueOf(payload.get("ok"))));
    }
}
