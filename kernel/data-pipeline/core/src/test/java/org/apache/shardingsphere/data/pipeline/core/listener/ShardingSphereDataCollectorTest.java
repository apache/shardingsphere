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

package org.apache.shardingsphere.data.pipeline.core.listener;

import org.apache.shardingsphere.data.pipeline.core.listener.ShardingSphereStatisticsScheduleCollector.ShardingSphereDataCollectorRunnable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
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
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShardingSphereDataCollectorTest {
    
    @Test
    void assertCollect() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereStatistics statistics = mockStatistics();
        when(contextManager.getMetaDataContexts().getStatistics()).thenReturn(statistics);
        ShardingSphereMetaData metaData = mockMetaData();
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        new ShardingSphereDataCollectorRunnable(contextManager).run();
        verify(contextManager).getInstanceContext();
    }
    
    private ShardingSphereStatistics mockStatistics() {
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        ShardingSphereDatabaseData shardingSphereDatabaseData = new ShardingSphereDatabaseData();
        result.getDatabaseData().put("logic_db", shardingSphereDatabaseData);
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereDatabaseData.getSchemaData().put("logic_schema", shardingSphereSchemaData);
        ShardingSphereTableData shardingSphereTableData = new ShardingSphereTableData("test_table");
        shardingSphereSchemaData.getTableData().put("test_table", shardingSphereTableData);
        return result;
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("logic_db");
        when(result.getDatabases()).thenReturn(Collections.singletonMap("logic_db", database));
        when(result.getDatabase("logic_db")).thenReturn(database);
        when(result.containsDatabase("logic_db")).thenReturn(true);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("logic_schema")).thenReturn(schema);
        when(database.containsSchema("logic_schema")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("test_table")).thenReturn(table);
        when(schema.containsTable("test_table")).thenReturn(true);
        when(table.getName()).thenReturn("test_table");
        Collection<ShardingSphereColumn> columns = new LinkedList<>();
        columns.add(new ShardingSphereColumn("column1", Types.INTEGER, false, false, false, true, false, false));
        columns.add(new ShardingSphereColumn("column2", Types.INTEGER, false, false, false, true, false, false));
        when(table.getColumnValues()).thenReturn(columns);
        return result;
    }
}
