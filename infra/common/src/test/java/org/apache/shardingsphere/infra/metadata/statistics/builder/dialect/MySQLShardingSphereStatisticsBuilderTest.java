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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.junit.jupiter.api.Test;

class MySQLShardingSphereStatisticsBuilderTest {
    
    @Test
    void assertBuild() {
        ShardingSphereDatabase database = mockDatabase();
        ShardingSphereDatabaseData databaseData = new MySQLShardingSphereStatisticsBuilder().build(database);
        assertTrue(databaseData.getSchemaData().containsKey("shardingsphere"));
        assertTrue(databaseData.getSchemaData().get("shardingsphere").getTableData().containsKey("cluster_information"));
        assertTrue(databaseData.getSchemaData().get("shardingsphere").getTableData().containsKey("sharding_table_statistics"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsSchema("shardingsphere")).thenReturn(true);
        return database;
    }
}
