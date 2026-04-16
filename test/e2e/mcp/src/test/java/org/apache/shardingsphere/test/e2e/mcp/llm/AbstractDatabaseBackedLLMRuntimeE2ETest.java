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

package org.apache.shardingsphere.test.e2e.mcp.llm;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public abstract class AbstractDatabaseBackedLLMRuntimeE2ETest extends AbstractLLMRuntimeE2ETest {
    
    private RuntimeFixture runtimeFixture;
    
    @AfterEach
    void closeRuntimeFixture() {
        if (null != runtimeFixture) {
            runtimeFixture.close();
            runtimeFixture = null;
        }
    }
    
    @Override
    protected final void prepareRuntimeFixture() throws IOException {
        runtimeFixture = createRuntimeFixture();
    }
    
    @Override
    protected final Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return runtimeFixture.runtimeDatabases();
    }
    
    protected abstract RuntimeTransport getTransport();
    
    protected abstract LLMRuntimeBackend getRuntimeBackend();
    
    protected abstract RuntimeFixture createH2RuntimeFixture() throws IOException;
    
    protected abstract RuntimeFixture createMySQLRuntimeFixture() throws IOException;
    
    protected final String getRuntimeSchemaName() {
        return runtimeFixture.schemaName();
    }
    
    protected final int getRuntimeTotalOrders() {
        return runtimeFixture.totalOrders();
    }
    
    protected final String getRuntimeKind() {
        return LLMRuntimeBackend.H2 == getRuntimeBackend() ? "h2" : "mysql";
    }
    
    protected final RuntimeFixture createSingleDatabaseH2RuntimeFixture(final String databaseName) throws IOException {
        try {
            H2RuntimeTestSupport.LLMH2RuntimeFixture actualFixture = H2RuntimeTestSupport.createLLMRuntimeFixture(getTempDir(), databaseName, "logic_db", getTransport());
            return new RuntimeFixture("public", actualFixture.totalOrders(), actualFixture.runtimeDatabases(), () -> {
            });
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    protected final RuntimeFixture createMultiDatabaseH2RuntimeFixture(final String logicalDatabase, final String analyticsDatabase) throws IOException {
        try {
            H2RuntimeTestSupport.LLMH2RuntimeFixture actualFixture = H2RuntimeTestSupport.createMultiDatabaseLLMRuntimeFixture(
                    getTempDir(), logicalDatabase, analyticsDatabase, getTransport());
            return new RuntimeFixture("public", actualFixture.totalOrders(), actualFixture.runtimeDatabases(), () -> {
            });
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    protected final RuntimeFixture createMySQLDatabaseRuntimeFixture(final String logicalDatabase, final String assumptionMessage) throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(), assumptionMessage);
        try {
            MySQLRuntimeTestSupport.LLMMySQLRuntimeFixture actualFixture = MySQLRuntimeTestSupport.createLLMRuntimeFixture(logicalDatabase);
            return new RuntimeFixture(actualFixture.getSchemaName(), actualFixture.getTotalOrders(), actualFixture.getRuntimeDatabases(), actualFixture::close);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    private RuntimeFixture createRuntimeFixture() throws IOException {
        return LLMRuntimeBackend.H2 == getRuntimeBackend() ? createH2RuntimeFixture() : createMySQLRuntimeFixture();
    }
    
    protected enum LLMRuntimeBackend {
        
        H2,
        
        MYSQL
    }
    
    protected record RuntimeFixture(String schemaName, int totalOrders, Map<String, RuntimeDatabaseConfiguration> runtimeDatabases, Runnable closeAction) {
        
        private void close() {
            closeAction.run();
        }
    }
}
