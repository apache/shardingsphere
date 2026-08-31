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

package org.apache.shardingsphere.infra.metadata.statistics.builder;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingSphereStatisticsFactoryTest {
    
    private final DatabaseType postgreSQLDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType openGaussDatabaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private final DatabaseType h2DatabaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private final DatabaseType sqlServerDatabaseType = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereStatistics statistics;
    
    @Test
    void assertCreateWithEmptyDatabases() {
        when(metaData.getAllDatabases()).thenReturn(Collections.emptyList());
        assertTrue(ShardingSphereStatisticsFactory.create(metaData, statistics).getDatabaseStatisticsMap().isEmpty());
    }
    
    @Test
    void assertCreateWithPostgreSQLDatabaseAndDialectAppender() {
        ShardingSphereDatabase database = mockDatabaseWithPgCatalogSchema(postgreSQLDatabaseType);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        ShardingSphereStatistics actual = ShardingSphereStatisticsFactory.create(metaData, statistics);
        assertThat(actual.getDatabaseStatisticsMap().size(), is(1));
        assertTrue(actual.getDatabaseStatisticsMap().containsKey("foo_db"));
    }
    
    @Test
    void assertCreateWithOpenGaussDatabaseAndDialectAppender() {
        ShardingSphereDatabase database = mockDatabaseWithPgCatalogSchema(openGaussDatabaseType);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        ShardingSphereStatistics actual = ShardingSphereStatisticsFactory.create(metaData, statistics);
        assertThat(actual.getDatabaseStatisticsMap().size(), is(1));
        assertTrue(actual.getDatabaseStatisticsMap().containsKey("foo_db"));
    }
    
    @Test
    void assertCreateWithPostgreSQLDatabaseWithShardingSphereSchema() {
        ShardingSphereDatabase database = mockPostgreSQLDatabaseWithShardingSphereSchema();
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        ShardingSphereStatistics actual = ShardingSphereStatisticsFactory.create(metaData, statistics);
        assertThat(actual.getDatabaseStatisticsMap().size(), is(1));
        assertTrue(actual.getDatabaseStatisticsMap().containsKey("foo_db"));
    }
    
    @Test
    void assertCreateWithH2DatabaseNoDialectAppender() {
        ShardingSphereDatabase database = mockH2Database();
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        assertTrue(ShardingSphereStatisticsFactory.create(metaData, statistics).getDatabaseStatisticsMap().isEmpty());
    }
    
    @Test
    void assertCreateWithSQLServerDatabaseNoDialectAppender() {
        ShardingSphereDatabase database = mockDatabaseWithPgCatalogSchema(sqlServerDatabaseType);
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        assertTrue(ShardingSphereStatisticsFactory.create(metaData, statistics).getDatabaseStatisticsMap().isEmpty());
    }
    
    @Test
    void assertCreateWithStatisticsMerging() {
        ShardingSphereDatabase database = mockPostgreSQLDatabaseWithShardingSphereSchema();
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(statistics.containsDatabaseStatistics("foo_db")).thenReturn(true);
        DatabaseStatistics existingStats = new DatabaseStatistics();
        existingStats.putSchemaStatistics("public", createSchemaStatistics());
        when(statistics.getDatabaseStatisticsMap()).thenReturn(Collections.singletonMap("foo_db", existingStats));
        ShardingSphereStatistics actual = ShardingSphereStatisticsFactory.create(metaData, statistics);
        assertThat(actual.getDatabaseStatisticsMap().size(), is(1));
        DatabaseStatistics actualDatabaseStatistics = actual.getDatabaseStatisticsMap().get("foo_db");
        assertTrue(actualDatabaseStatistics.containsSchemaStatistics("public"));
    }
    
    @Test
    void assertCreateWithTableLevelMerging() {
        ShardingSphereDatabase database = mockPostgreSQLDatabaseWithShardingSphereSchema();
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(statistics.containsDatabaseStatistics("foo_db")).thenReturn(true);
        DatabaseStatistics existingStats = new DatabaseStatistics();
        existingStats.putSchemaStatistics("shardingsphere", createSchemaStatistics());
        when(statistics.getDatabaseStatisticsMap()).thenReturn(Collections.singletonMap("foo_db", existingStats));
        ShardingSphereStatistics actual = ShardingSphereStatisticsFactory.create(metaData, statistics);
        assertThat(actual.getDatabaseStatisticsMap().size(), is(1));
        assertTrue(actual.getDatabaseStatisticsMap().containsKey("foo_db"));
        DatabaseStatistics actualDatabaseStatistics = actual.getDatabaseStatisticsMap().get("foo_db");
        assertTrue(actualDatabaseStatistics.containsSchemaStatistics("shardingsphere"));
        SchemaStatistics actualSchemaStatistics = actualDatabaseStatistics.getSchemaStatistics("shardingsphere");
        assertTrue(actualSchemaStatistics.containsTableStatistics("foo_tbl"));
        assertTrue(actualSchemaStatistics.containsTableStatistics("cluster_information"));
    }
    
    @Test
    void assertCreateWithTableLevelMergingSkipExistingTables() {
        ShardingSphereDatabase database = mockPostgreSQLDatabaseWithShardingSphereSchema();
        when(metaData.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(statistics.containsDatabaseStatistics("foo_db")).thenReturn(true);
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics("cluster_information", new TableStatistics("cluster_information"));
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        databaseStatistics.putSchemaStatistics("shardingsphere", schemaStatistics);
        when(statistics.getDatabaseStatisticsMap()).thenReturn(Collections.singletonMap("foo_db", databaseStatistics));
        ShardingSphereStatistics actual = ShardingSphereStatisticsFactory.create(metaData, statistics);
        assertThat(actual.getDatabaseStatisticsMap().size(), is(1));
        assertTrue(actual.getDatabaseStatisticsMap().containsKey("foo_db"));
        DatabaseStatistics actualDatabaseStatistics = actual.getDatabaseStatisticsMap().get("foo_db");
        assertTrue(actualDatabaseStatistics.containsSchemaStatistics("shardingsphere"));
        SchemaStatistics actualSchemaStatistics = actualDatabaseStatistics.getSchemaStatistics("shardingsphere");
        assertTrue(actualSchemaStatistics.containsTableStatistics("cluster_information"));
        assertThat(actualSchemaStatistics.getTableStatisticsMap().size(), is(1));
    }
    
    private ShardingSphereDatabase mockDatabaseWithPgCatalogSchema(final DatabaseType protocolType) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(protocolType);
        ShardingSphereSchema schema = new ShardingSphereSchema("pg_catalog", mock(DatabaseType.class));
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.containsSchema(anyString())).thenAnswer(invocation -> "pg_catalog".equals(invocation.getArgument(0)));
        return result;
    }
    
    private ShardingSphereDatabase mockPostgreSQLDatabaseWithShardingSphereSchema() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(postgreSQLDatabaseType);
        ShardingSphereSchema schema = new ShardingSphereSchema("shardingsphere", mock(DatabaseType.class));
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.containsSchema("shardingsphere")).thenReturn(true);
        when(result.getSchema("shardingsphere")).thenReturn(schema);
        lenient().when(result.containsSchema("pg_catalog")).thenReturn(false);
        return result;
    }
    
    private ShardingSphereDatabase mockH2Database() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(h2DatabaseType);
        ShardingSphereSchema schema = new ShardingSphereSchema("public_schema", mock(DatabaseType.class));
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        return result;
    }
    
    private SchemaStatistics createSchemaStatistics() {
        SchemaStatistics result = new SchemaStatistics();
        result.putTableStatistics("foo_tbl", new TableStatistics("foo_tbl"));
        return result;
    }
}
