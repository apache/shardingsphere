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

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPMetadataObjectType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

class MCPJdbcMetadataLoaderTest extends AbstractMCPJdbcMetadataLoaderTest {
    
    @Test
    void assertLoad() throws SQLException {
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(createStandardPostgreSQLMetadataConnection())));
        Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
        assertThat(schemas.size(), is(1));
        assertThat(schemas.iterator().next().getName(), is("PUBLIC"));
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
        Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.VIEW, "active_orders"));
        assertThat(schemas.size(), is(1));
    }
    
    @Test
    void assertLoadWithoutIndexMetadataSupport() throws SQLException {
        Connection connection = createStandardPostgreSQLMetadataConnection();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        when(databaseMetaData.getIndexInfo(isNull(), nullable(String.class), anyString(), eq(false), eq(false)))
                .thenThrow(new SQLFeatureNotSupportedException("unsupported"));
        LoadedMetadataCatalog actual = load(Map.of("logic_db", createMockRuntimeDatabaseConfiguration(connection)));
        Collection<ShardingSphereSchema> schemas = actual.findMetadata("logic_db").orElseThrow();
        assertTrue(containsMetadata(schemas, SupportedMCPMetadataObjectType.TABLE, "orders"));
        assertFalse(containsMetadata(schemas, SupportedMCPMetadataObjectType.INDEX, "idx_orders_status"));
    }
}
