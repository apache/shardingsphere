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

package org.apache.shardingsphere.test.e2e.mcp.functionality;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.DockerRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.MySQLRuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
abstract class AbstractMySQLRuntimeE2ETest extends AbstractTransportParameterizedE2ETest {
    
    protected static final String LOGICAL_DATABASE_NAME = "logic_db";
    
    protected static final String PHYSICAL_DATABASE_NAME = "orders";
    
    @Getter(AccessLevel.PROTECTED)
    private GenericContainer<?> container;
    
    @Getter(AccessLevel.PROTECTED)
    private String physicalSchemaName;
    
    private MySQLRuntimeFixture sharedRuntimeFixture;
    
    @AfterEach
    void tearDownContainer() {
        if (useSharedRuntimeFixture()) {
            container = null;
            physicalSchemaName = null;
            return;
        }
        if (null != container) {
            container.stop();
            container = null;
        }
        physicalSchemaName = null;
    }
    
    @AfterAll
    void tearDownSharedContainer() {
        if (null != sharedRuntimeFixture) {
            sharedRuntimeFixture.close();
            sharedRuntimeFixture = null;
        }
    }
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        DockerRuntimeTestSupport.requireAvailable("Docker is required for the MySQL-backed MCP Functionality E2E test.");
        if (useSharedRuntimeFixture()) {
            prepareSharedRuntimeFixture();
            return;
        }
        applyRuntimeFixture(createRuntimeFixture());
    }
    
    private void prepareSharedRuntimeFixture() throws IOException {
        if (null == sharedRuntimeFixture) {
            sharedRuntimeFixture = createRuntimeFixture();
        }
        applyRuntimeFixture(sharedRuntimeFixture);
    }
    
    private MySQLRuntimeFixture createRuntimeFixture() throws IOException {
        GenericContainer<?> result = MySQLRuntimeTestSupport.createContainer();
        boolean success = false;
        try {
            result.start();
            MySQLRuntimeTestSupport.initializeDatabase(result);
            String detectedSchemaName = MySQLRuntimeTestSupport.detectSchema(result);
            success = true;
            return new MySQLRuntimeFixture(result, detectedSchemaName.isEmpty() ? PHYSICAL_DATABASE_NAME : detectedSchemaName);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        } finally {
            if (!success) {
                result.stop();
            }
        }
    }
    
    private void applyRuntimeFixture(final MySQLRuntimeFixture fixture) {
        container = fixture.container();
        physicalSchemaName = fixture.physicalSchemaName();
    }
    
    protected boolean useSharedRuntimeFixture() {
        return false;
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return MySQLRuntimeTestSupport.createRuntimeDatabases(container, LOGICAL_DATABASE_NAME);
    }
    
    protected static Stream<Arguments> singleMetadataResourceCases() {
        return Stream.of(
                Arguments.of("database detail", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME, "database", LOGICAL_DATABASE_NAME),
                Arguments.of("schema detail", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME, "schema", LOGICAL_DATABASE_NAME),
                Arguments.of("table column detail",
                        "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME + "/tables/orders/columns/status", "column", "status"),
                Arguments.of("view detail", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME
                        + "/views/active_orders", "view", "active_orders"),
                Arguments.of("view column detail", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME
                        + "/views/active_orders/columns/status", "column", "status"),
                Arguments.of("index detail", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME
                        + "/tables/orders/indexes/idx_orders_status", "index", "idx_orders_status"));
    }
    
    protected static Stream<Arguments> collectionMetadataResourceCases() {
        return Stream.of(
                Arguments.of("schemas list", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas", "schema", List.of(LOGICAL_DATABASE_NAME)),
                Arguments.of("tables list", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME + "/tables", "table",
                        List.of("order_items", "orders")),
                Arguments.of("table columns list", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME
                        + "/tables/orders/columns", "column", List.of("amount", "order_id", "status")),
                Arguments.of("view columns list", "shardingsphere://databases/" + LOGICAL_DATABASE_NAME + "/schemas/" + LOGICAL_DATABASE_NAME
                        + "/views/active_orders/columns", "column", List.of("order_id", "status")));
    }
    
    protected void assertRecoveryResponse(final Map<String, Object> actual) {
        assertThat(String.valueOf(actual.get("response_mode")), is("recovery"));
        assertFalse(String.valueOf(actual.get("summary")).isBlank());
    }
    
    protected void assertRecoveryResponse(final Map<String, Object> actual, final String expectedMessage) {
        assertRecoveryResponse(actual);
        assertThat(String.valueOf(actual.get("summary")), is(expectedMessage));
    }
    
    protected Map<String, Object> getObjectOrEmpty(final Object value) {
        return value instanceof Map ? MCPInteractionPayloads.getRequiredObjectValue(value, "payload") : Map.of();
    }
    
    protected List<Map<String, Object>> getRequiredObjectList(final Object value) {
        return MCPInteractionPayloads.getRequiredObjectList(value, "payload");
    }
    
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class MySQLRuntimeFixture implements AutoCloseable {
        
        private final GenericContainer<?> container;
        
        private final String physicalSchemaName;
        
        private GenericContainer<?> container() {
            return container;
        }
        
        private String physicalSchemaName() {
            return physicalSchemaName;
        }
        
        @Override
        public void close() {
            container.stop();
        }
    }
}
