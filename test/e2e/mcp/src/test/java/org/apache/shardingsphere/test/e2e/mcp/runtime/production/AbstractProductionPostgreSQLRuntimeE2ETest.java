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

import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.PostgreSQLRuntimeTestSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractProductionPostgreSQLRuntimeE2ETest extends AbstractTransportParameterizedProductionRuntimeE2ETest {
    
    protected static final String LOGICAL_DATABASE_NAME = "postgres_db";
    
    private GenericContainer<?> container;
    
    @AfterAll
    void tearDownContainer() {
        if (null != container) {
            container.stop();
            container = null;
        }
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        if (!PostgreSQLRuntimeTestSupport.isDockerAvailable()) {
            throw new IllegalStateException("Docker is required for the PostgreSQL-backed production runtime E2E test.");
        }
        if (null != container) {
            return;
        }
        GenericContainer<?> result = PostgreSQLRuntimeTestSupport.createContainer();
        boolean success = false;
        try {
            result.start();
            PostgreSQLRuntimeTestSupport.initializeDatabase(result);
            container = result;
            success = true;
        } catch (final SQLException ex) {
            throw new IOException("Failed to initialize PostgreSQL runtime fixture.", ex);
        } finally {
            if (!success) {
                result.stop();
            }
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return PostgreSQLRuntimeTestSupport.createRuntimeDatabases(container, LOGICAL_DATABASE_NAME);
    }
    
    protected static Map<String, Object> createExecuteUpdateArguments(final String schema, final String sql) {
        return Map.of("database", LOGICAL_DATABASE_NAME, "schema", schema, "sql", sql, "execution_mode", "execute");
    }
    
    protected static Stream<Arguments> dualTransports() {
        return ProductionRuntimeTransportCases.transports();
    }
}
