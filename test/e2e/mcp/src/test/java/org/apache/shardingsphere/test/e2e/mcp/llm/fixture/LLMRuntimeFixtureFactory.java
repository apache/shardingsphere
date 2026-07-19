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

package org.apache.shardingsphere.test.e2e.mcp.llm.fixture;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Create runtime fixtures for LLM E2E tests.
 */
public final class LLMRuntimeFixtureFactory {
    
    /**
     * Create one MySQL runtime fixture.
     *
     * @param logicalDatabase logical database
     * @param dockerRequiredMessage failure message when Docker is unavailable
     * @return runtime fixture
     * @throws IOException IO exception
     * @throws IllegalStateException Docker is unavailable
     */
    public Fixture createMySQLFixture(final String logicalDatabase, final String dockerRequiredMessage) throws IOException {
        if (!MySQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException(MySQLRuntimeTestSupport.createDockerRequiredMessage(dockerRequiredMessage));
        }
        try {
            MySQLRuntimeTestSupport.LLMMySQLRuntimeFixture actualFixture = MySQLRuntimeTestSupport.createLLMRuntimeFixture(logicalDatabase);
            return new Fixture(actualFixture.getSchemaName(), actualFixture.getTotalOrders(), actualFixture.getRuntimeDatabases(), actualFixture::close);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    /**
     * Runtime fixture.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Fixture implements AutoCloseable {
        
        private final String schemaName;
        
        private final int totalOrders;
        
        private final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases;
        
        private final Runnable closeAction;
        
        /**
         * Get schema name.
         *
         * @return schema name
         */
        public String schemaName() {
            return schemaName;
        }
        
        /**
         * Get total orders.
         *
         * @return total orders
         */
        public int totalOrders() {
            return totalOrders;
        }
        
        /**
         * Get runtime databases.
         *
         * @return runtime databases
         */
        public Map<String, RuntimeDatabaseConfiguration> runtimeDatabases() {
            return runtimeDatabases;
        }
        
        @Override
        public void close() {
            closeAction.run();
        }
    }
}
