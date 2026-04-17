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
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ProxyEncryptWorkflowRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.ProxyEncryptWorkflowRuntimeTestSupport.ProxyEncryptWorkflowRuntimeFixture;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

abstract class AbstractProductionProxyWorkflowE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private ProxyEncryptWorkflowRuntimeFixture runtimeFixture;
    
    @AfterEach
    void tearDownFixture() {
        if (null != runtimeFixture) {
            runtimeFixture.close();
            runtimeFixture = null;
        }
    }
    
    @Override
    protected final RuntimeTransport getTransport() {
        return RuntimeTransport.HTTP;
    }
    
    @Override
    protected final void prepareRuntimeFixture() throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(), "Docker is required for the Proxy-backed workflow E2E tests.");
        try {
            runtimeFixture = ProxyEncryptWorkflowRuntimeTestSupport.createFixture();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected final Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeFixture.getRuntimeDatabases();
    }
    
    protected final String getLogicalDatabaseName() {
        return runtimeFixture.getLogicalDatabaseName();
    }
    
    protected final int countPhysicalColumn(final String columnName) throws SQLException {
        return MySQLRuntimeTestSupport.querySingleInt(runtimeFixture.getStorageContainer(), String.format(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = '%s' AND table_name = 'orders' AND column_name = '%s'",
                runtimeFixture.getPhysicalDatabaseName(), columnName));
    }
    
    protected final void assertValidationPassed(final Map<String, Object> actualValidationResponse) {
        assertThat(actualValidationResponse.toString(), String.valueOf(actualValidationResponse.get("status")), is("validated"));
        assertThat(actualValidationResponse.toString(), String.valueOf(actualValidationResponse.get("overall_status")), is("passed"));
        assertThat(actualValidationResponse.toString(), getMapList(actualValidationResponse.get("issues")).size(), is(0));
        assertThat(actualValidationResponse.toString(), getMapList(actualValidationResponse.get("mismatches")).size(), is(0));
    }
    
    protected final void assertValidationFailed(final Map<String, Object> actualValidationResponse) {
        assertThat(actualValidationResponse.toString(), String.valueOf(actualValidationResponse.get("status")), is("failed"));
        assertThat(actualValidationResponse.toString(), String.valueOf(actualValidationResponse.get("overall_status")), is("failed"));
    }
    
    protected final void assertValidationEventuallyPassed(final MCPInteractionClient interactionClient, final String planId) throws Exception {
        Map<String, Object> actualValidationResponse = Map.of();
        for (int i = 0; i < 10; i++) {
            actualValidationResponse = interactionClient.call("validate_encrypt_mask_rule", Map.of("plan_id", planId));
            if ("validated".equals(String.valueOf(actualValidationResponse.get("status")))) {
                assertValidationPassed(actualValidationResponse);
                return;
            }
            TimeUnit.SECONDS.sleep(1L);
        }
        assertValidationPassed(actualValidationResponse);
    }
    
    protected final List<String> getIssueCodes(final Map<String, Object> payload) {
        return getMapList(payload.get("issues")).stream().map(each -> String.valueOf(each.get("code"))).toList();
    }
    
    protected final List<String> getStringList(final Object value) {
        return null == value ? List.of() : ((List<?>) value).stream().map(String::valueOf).toList();
    }
    
    @SuppressWarnings("unchecked")
    protected final List<Map<String, Object>> getMapList(final Object value) {
        return null == value ? List.of() : ((List<?>) value).stream().map(each -> (Map<String, Object>) each).toList();
    }
    
    @SuppressWarnings("unchecked")
    protected final Map<String, Object> getMap(final Object value) {
        return null == value ? Map.of() : (Map<String, Object>) value;
    }
}
