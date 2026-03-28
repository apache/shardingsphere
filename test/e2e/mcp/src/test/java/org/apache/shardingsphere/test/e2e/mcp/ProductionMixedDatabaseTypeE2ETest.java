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

import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.jdbc.runtime.H2RuntimeTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProductionMixedDatabaseTypeE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private String firstJdbcUrl;
    
    private String secondJdbcUrl;
    
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> result = new LinkedHashMap<>(2, 1F);
        result.put("logic_db", new RuntimeDatabaseConfiguration("MySQL", firstJdbcUrl, "", "", "org.h2.Driver"));
        result.put("analytics_db", new RuntimeDatabaseConfiguration("PostgreSQL", secondJdbcUrl, "", "", "org.h2.Driver"));
        return result;
    }
    
    @Override
    protected void prepareRuntimeFixture() {
        firstJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "mixed-type-first");
        secondJdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "mixed-type-second");
        initializeDatabase(firstJdbcUrl);
        initializeDatabase(secondJdbcUrl);
    }
    
    @Test
    void assertGetCapabilitiesForMixedDatabaseTypes() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> firstResponse = sendToolCallRequest(httpClient, sessionId, "get_capabilities", Map.of("database", "logic_db"));
        HttpResponse<String> secondResponse = sendToolCallRequest(httpClient, sessionId, "get_capabilities", Map.of("database", "analytics_db"));
        
        assertThat(firstResponse.statusCode(), is(200));
        assertThat(secondResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(firstResponse.body()).get("databaseType")), is("MySQL"));
        assertThat(String.valueOf(getStructuredContent(secondResponse.body()).get("databaseType")), is("PostgreSQL"));
    }
    
    private void initializeDatabase(final String jdbcUrl) {
        try {
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
