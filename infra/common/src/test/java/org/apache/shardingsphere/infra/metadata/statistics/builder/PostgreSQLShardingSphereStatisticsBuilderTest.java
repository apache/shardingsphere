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

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.dialect.PostgreSQLShardingSphereStatisticsBuilder;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Map<String, ShardingSphereDatabase> databaseMap = mockDatabaseMap();
        when(result.getDatabases()).thenReturn(databaseMap);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabaseMap() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        Map<String, ShardingSphereSchema> schemaMap = mockSchemaMap();
        when(database.getSchemas()).thenReturn(schemaMap);
        return Collections.singletonMap("logic_db", database);
    }
    
    private Map<String, ShardingSphereSchema> mockSchemaMap() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        Map<String, ShardingSphereTable> tableMap = mockTableMap();
        when(schema.getTables()).thenReturn(tableMap);
        return Collections.singletonMap("pg_catalog", schema);
    }
    
    private Map<String, ShardingSphereTable> mockTableMap() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("pg_class");
        return Collections.singletonMap("pg_class", table);
    }
}
