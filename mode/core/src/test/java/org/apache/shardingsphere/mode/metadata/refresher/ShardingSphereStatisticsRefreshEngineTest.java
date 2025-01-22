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

package org.apache.shardingsphere.mode.metadata.refresher;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereTableData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereStatisticsRefreshEngineTest {
    
    @Test
    void assertRefresh() {
        ContextManager contextManager = mockContextManager();
        new ShardingSphereStatisticsRefreshEngine(contextManager).refresh();
        verify(contextManager.getPersistServiceFacade().getMetaDataPersistService().getShardingSphereDataPersistService()).update(any());
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        ShardingSphereStatistics statistics = mockStatistics();
        ShardingSphereMetaData metaData = mockMetaData();
        when(metaDataContexts.getStatistics()).thenReturn(statistics);
        when(metaDataContexts.getMetaData()).thenReturn(metaData);
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getComputeNodeInstanceContext().getLockContext().tryLock(any(), anyLong())).thenReturn(true);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        ShardingSphereTable table = mockTable();
        when(result.getMetaDataContexts().getMetaData().getDatabase(any()).getSchema(any()).getTable(any())).thenReturn(table);
        return result;
    }
    
    private ShardingSphereStatistics mockStatistics() {
        ShardingSphereTableData tableData = new ShardingSphereTableData("test_table");
        ShardingSphereSchemaData schemaData = new ShardingSphereSchemaData();
        schemaData.putTable("test_table", tableData);
        ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
        databaseData.putSchema("foo_schema", schemaData);
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        result.putDatabase("foo_db", databaseData);
        return result;
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereDatabase database = mockShardingSphereDatabase();
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.getAllDatabases()).thenReturn(Collections.singleton(database));
        when(result.getDatabase(any())).thenReturn(database);
        when(result.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(result.getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(
                PropertiesBuilder.build(new Property(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_ENABLED.getKey(), Boolean.TRUE.toString()))));
        return result;
    }
    
    private ShardingSphereDatabase mockShardingSphereDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        ShardingSphereTable table = mockTable();
        when(schema.getName()).thenReturn("foo_schema");
        when(schema.getTable(any())).thenReturn(table);
        when(schema.getAllTables()).thenReturn(Collections.singletonList(table));
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn("foo_database");
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.getSchema(any())).thenReturn(schema);
        when(result.getAllSchemas()).thenReturn(Collections.singletonList(schema));
        return result;
    }
    
    private static ShardingSphereTable mockTable() {
        ShardingSphereTable result = mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("test_table");
        ShardingSphereColumn column1 = new ShardingSphereColumn("col_1", Types.INTEGER, false, false, false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("col_2", Types.INTEGER, false, false, false, true, false, false);
        when(result.getAllColumns()).thenReturn(Arrays.asList(column1, column2));
        return result;
    }
}
