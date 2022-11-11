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

package org.apache.shardingsphere.data.pipeline.core.execute;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereDataCollectorTest {
    
    @Test
    public void assertCollect() throws InterruptedException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereData shardingSphereData = mockShardingSphereData();
        when(contextManager.getMetaDataContexts().getShardingSphereData()).thenReturn(shardingSphereData);
        ShardingSphereMetaData metaData = mockMetaData();
        when(contextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        new ShardingSphereDataScheduleCollector(contextManager).start();
        Thread.sleep(100L);
        verify(contextManager, atLeastOnce()).getInstanceContext();
    }
    
    private ShardingSphereData mockShardingSphereData() {
        ShardingSphereData shardingSphereData = new ShardingSphereData();
        ShardingSphereDatabaseData shardingSphereDatabaseData = new ShardingSphereDatabaseData();
        shardingSphereData.getDatabaseData().put("logic_db", shardingSphereDatabaseData);
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereDatabaseData.getSchemaData().put("logic_schema", shardingSphereSchemaData);
        ShardingSphereTableData shardingSphereTableData = new ShardingSphereTableData("test_table", Collections.emptyList());
        shardingSphereSchemaData.getTableData().put("test_table", shardingSphereTableData);
        return shardingSphereData;
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("logic_db");
        when(result.getDatabase("logic_db")).thenReturn(database);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(database.getSchema("logic_schema")).thenReturn(schema);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(schema.getTable("test_table")).thenReturn(table);
        when(table.getName()).thenReturn("test_table");
        return result;
    }
}
