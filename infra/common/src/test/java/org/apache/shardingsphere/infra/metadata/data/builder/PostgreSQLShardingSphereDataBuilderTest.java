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

package org.apache.shardingsphere.infra.metadata.data.builder;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.builder.dialect.PostgreSQLShardingSphereDataBuilder;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class PostgreSQLShardingSphereDataBuilderTest {
    
    @Test
    public void assertBuild() {
        ShardingSphereMetaData metaData = mockMetaData();
        ShardingSphereData shardingSphereData = new PostgreSQLShardingSphereDataBuilder().build(metaData);
        assertTrue(shardingSphereData.getDatabaseData().containsKey("logic_db"));
        assertTrue(shardingSphereData.getDatabaseData().get("logic_db").getSchemaData().containsKey("pg_catalog"));
        assertTrue(shardingSphereData.getDatabaseData().get("logic_db").getSchemaData().get("pg_catalog").getTableData().containsKey("pg_class"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        Map<String, ShardingSphereDatabase> databaseMap = mockDatabaseMap();
        when(result.getDatabases()).thenReturn(databaseMap);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabaseMap() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        Map<String, ShardingSphereSchema> schemaMap = mockSchemaMap();
        when(database.getSchemas()).thenReturn(schemaMap);
        result.put("logic_db", database);
        return result;
    }
    
    private Map<String, ShardingSphereSchema> mockSchemaMap() {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(1);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        Map<String, ShardingSphereTable> tableMap = mockTableMap();
        when(schema.getTables()).thenReturn(tableMap);
        result.put("pg_catalog", schema);
        return result;
    }
    
    private Map<String, ShardingSphereTable> mockTableMap() {
        Map<String, ShardingSphereTable> result = new LinkedHashMap<>(1);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("pg_class");
        result.put("pg_class", table);
        return result;
    }
}
