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

package org.apache.shardingsphere.infra.metadata.statistics;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereSchemaDataTest {
    
    private static final String TEST_TABLE_NAME = "TEST_TABLE_NAME";
    
    private static final String TEST_TABLE_2 = "TEST_TABLE_2";
    
    private static final String NON_EXISTENT_TABLE = "NON_EXISTENT_TABLE";
    
    @Test
    void assertGetTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));
        ShardingSphereTableData tableData = shardingSphereSchemaData.getTable(TEST_TABLE_NAME);
        assertTrue(TEST_TABLE_NAME.equalsIgnoreCase(tableData.getName()));
        assertNull(shardingSphereSchemaData.getTable(NON_EXISTENT_TABLE));
    }
    
    @Test
    void assertPutTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));
        assertThat(shardingSphereSchemaData.getTableData().size(), is(1));
        assertFalse(shardingSphereSchemaData.getTableData().containsKey(TEST_TABLE_2));
        ShardingSphereTableData newTable = new ShardingSphereTableData(TEST_TABLE_2);
        shardingSphereSchemaData.putTable(TEST_TABLE_2, newTable);
        assertThat(shardingSphereSchemaData.getTableData().size(), is(2));
        assertTrue(shardingSphereSchemaData.containsTable(TEST_TABLE_2));
    }
    
    @Test
    void assertRemoveTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));
        assertTrue(shardingSphereSchemaData.containsTable(TEST_TABLE_NAME));
        shardingSphereSchemaData.removeTable(TEST_TABLE_NAME);
        assertTrue(shardingSphereSchemaData.getTableData().isEmpty());
    }
    
    @Test
    void assertContainsTable() {
        ShardingSphereSchemaData shardingSphereSchemaData = new ShardingSphereSchemaData();
        shardingSphereSchemaData.putTable(TEST_TABLE_NAME, new ShardingSphereTableData(TEST_TABLE_NAME));
        assertTrue(shardingSphereSchemaData.containsTable(TEST_TABLE_NAME));
        assertFalse(shardingSphereSchemaData.containsTable(NON_EXISTENT_TABLE));
    }
}
