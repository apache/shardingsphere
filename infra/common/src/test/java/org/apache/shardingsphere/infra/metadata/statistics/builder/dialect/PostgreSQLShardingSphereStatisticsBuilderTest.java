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

package org.apache.shardingsphere.infra.metadata.statistics.builder.dialect;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLShardingSphereStatisticsBuilderTest {
    
    @Test
    void assertBuild() {
        ShardingSphereMetaData metaData = mockMetaData();
        ShardingSphereStatistics statistics = new PostgreSQLShardingSphereStatisticsBuilder().build(metaData);
        assertTrue(statistics.getDatabaseData().containsKey("logic_db"));
        assertTrue(statistics.getDatabaseData().get("logic_db").getSchemaData().containsKey("pg_catalog"));
        assertTrue(statistics.getDatabaseData().get("logic_db").getSchemaData().get("pg_catalog").getTableData().containsKey("pg_class"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        Collection<ShardingSphereDatabase> databases = mockDatabases();
        when(result.getAllDatabases()).thenReturn(databases);
        return result;
    }
    
    private Collection<ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("logic_db");
        ShardingSphereSchema schema = mockSchema();
        when(database.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(database.getSchema("pg_catalog")).thenReturn(schema);
        return Collections.singleton(database);
    }
    
    private ShardingSphereSchema mockSchema() {
        ShardingSphereSchema result = mock(ShardingSphereSchema.class);
        when(result.getName()).thenReturn("pg_catalog");
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("pg_class");
        when(result.getAllTables()).thenReturn(Collections.singleton(table));
        return result;
    }
}
