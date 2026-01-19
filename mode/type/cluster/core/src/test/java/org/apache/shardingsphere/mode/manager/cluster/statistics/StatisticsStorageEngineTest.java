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

package org.apache.shardingsphere.mode.manager.cluster.statistics;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.TableStatistics;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatisticsStorageEngineTest {
    
    @Test
    void assertRefresh() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereStatistics statistics = mockStatistics();
        when(contextManager.getMetaDataContexts().getStatistics()).thenReturn(statistics);
        ShardingSphereTable table = mockTable();
        when(contextManager.getDatabase("foo_db").getSchema("foo_schema").getTable("foo_table")).thenReturn(table);
        new StatisticsStorageEngine(contextManager, "foo_db", "foo_schema", "foo_table", Collections.emptyList()).storage();
        verify(contextManager.getPersistServiceFacade().getMetaDataFacade().getStatisticsService()).update(any());
    }
    
    private ShardingSphereStatistics mockStatistics() {
        TableStatistics tableStatistics = new TableStatistics("foo_table");
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics("foo_table", tableStatistics);
        DatabaseStatistics databaseStatistics = new DatabaseStatistics();
        databaseStatistics.putSchemaStatistics("foo_schema", schemaStatistics);
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        result.getDatabaseStatisticsMap().put("foo_db", databaseStatistics);
        return result;
    }
    
    private static ShardingSphereTable mockTable() {
        ShardingSphereTable result = mock(ShardingSphereTable.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_table");
        ShardingSphereColumn column1 = new ShardingSphereColumn("col_1", Types.INTEGER, false, false, "int", false, true, false, false);
        ShardingSphereColumn column2 = new ShardingSphereColumn("col_2", Types.INTEGER, false, false, "int", false, true, false, false);
        when(result.getAllColumns()).thenReturn(Arrays.asList(column1, column2));
        return result;
    }
}
