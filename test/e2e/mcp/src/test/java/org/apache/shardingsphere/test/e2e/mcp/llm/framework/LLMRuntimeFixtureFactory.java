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

package org.apache.shardingsphere.test.e2e.mcp.llm.framework;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

/**
 * Create runtime fixtures for LLM E2E tests.
 */
public final class LLMRuntimeFixtureFactory {
    
    /**
     * Runtime backend.
     */
    public enum Backend {
        
        H2,
        
        MYSQL
    }
    
    /**
     * Create one single-database H2 runtime fixture.
     *
     * @param tempDir temp directory
     * @param databaseName database name
     * @param logicalDatabase logical database
     * @param transport runtime transport
     * @return runtime fixture
     * @throws IOException IO exception
     */
    public Fixture createSingleDatabaseH2Fixture(final Path tempDir, final String databaseName,
                                                 final String logicalDatabase, final RuntimeTransport transport) throws IOException {
        try {
            H2RuntimeTestSupport.LLMH2RuntimeFixture actualFixture = H2RuntimeTestSupport.createLLMRuntimeFixture(tempDir, databaseName, logicalDatabase, transport);
            return new Fixture("public", actualFixture.totalOrders(), actualFixture.runtimeDatabases(), () -> {
            });
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Create one multi-database H2 runtime fixture.
     *
     * @param tempDir temp directory
     * @param logicalDatabase logical database
     * @param analyticsDatabase analytics database
     * @param transport runtime transport
     * @return runtime fixture
     * @throws IOException IO exception
     */
    public Fixture createMultiDatabaseH2Fixture(final Path tempDir, final String logicalDatabase,
                                                final String analyticsDatabase, final RuntimeTransport transport) throws IOException {
        try {
            H2RuntimeTestSupport.LLMH2RuntimeFixture actualFixture = H2RuntimeTestSupport.createMultiDatabaseLLMRuntimeFixture(
                    tempDir, logicalDatabase, analyticsDatabase, transport);
            return new Fixture("public", actualFixture.totalOrders(), actualFixture.runtimeDatabases(), () -> {
            });
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Create one MySQL runtime fixture.
     *
     * @param logicalDatabase logical database
     * @param assumptionMessage assumption message when Docker is unavailable
     * @return runtime fixture
     * @throws IOException IO exception
     */
    public Fixture createMySQLFixture(final String logicalDatabase, final String assumptionMessage) throws IOException {
        Assumptions.assumeTrue(MySQLRuntimeTestSupport.isDockerAvailable(), assumptionMessage);
        try {
            MySQLRuntimeTestSupport.LLMMySQLRuntimeFixture actualFixture = MySQLRuntimeTestSupport.createLLMRuntimeFixture(logicalDatabase);
            return new Fixture(actualFixture.getSchemaName(), actualFixture.getTotalOrders(), actualFixture.getRuntimeDatabases(), actualFixture::close);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Runtime fixture.
     *
     * @param schemaName schema name
     * @param totalOrders total orders
     * @param runtimeDatabases runtime databases
     * @param closeAction close action
     */
    public record Fixture(String schemaName, int totalOrders, Map<String, RuntimeDatabaseConfiguration> runtimeDatabases,
                          Runnable closeAction) implements AutoCloseable {
        
        @Override
        public void close() {
            closeAction.run();
        }
    }
}
