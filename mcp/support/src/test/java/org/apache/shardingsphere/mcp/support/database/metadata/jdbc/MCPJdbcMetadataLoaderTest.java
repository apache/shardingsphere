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

package org.apache.shardingsphere.mcp.support.database.metadata.jdbc;

import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPJdbcMetadataLoaderTest extends AbstractMCPJdbcMetadataLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        assertThat(actual.findMetadata("logic_db").map(MCPDatabaseMetadata::getDatabaseType).orElseThrow(), is("PostgreSQL"));
        assertThat(actual.findMetadata("logic_db").orElseThrow().getDatabaseVersion(), is("16.2"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("loadTypedMetadataArguments")
    void assertLoadWithTypedMetadata(final String name, final SupportedMCPMetadataObjectType objectType, final String objectName) throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        assertTrue(containsMetadata(actual.findMetadata("logic_db").orElseThrow(), objectType, objectName));
    }
    
    @Test
    void assertLoadWithMultipleLogicalDatabases() throws SQLException {
        Map<String, RuntimeDatabaseConfiguration> connectionConfigs = Map.of(
                "logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection()),
                "analytics_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection()));
        LoadedMetadataCatalog actual = load(connectionConfigs);
        assertThat(actual.getDatabaseMetadataMap().size(), is(2));
        assertTrue(actual.findMetadata("analytics_db").isPresent());
    }
    
    @Test
    void assertLoadWithSchemaRegisteredOnce() throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        MCPDatabaseMetadata databaseMetadata = actual.findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(databaseMetadata, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(databaseMetadata, SupportedMCPMetadataObjectType.VIEW, "active_orders"));
        assertThat(databaseMetadata.getSchemas().size(), is(1));
    }
}
