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

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLShardingSphereStatisticsBuilderTest {
    
    @Test
    void assertBuild() {
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereDatabaseData databaseData = new PostgreSQLShardingSphereStatisticsBuilder().build(database);
        assertTrue(databaseData.getSchemaData().containsKey("pg_catalog"));
        assertTrue(databaseData.getSchemaData().get("pg_catalog").getTableData().containsKey("pg_class"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("logic_db");
        ShardingSphereSchema schema = mockSchema();
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getSchema("pg_catalog")).thenReturn(schema);
        return result;
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
