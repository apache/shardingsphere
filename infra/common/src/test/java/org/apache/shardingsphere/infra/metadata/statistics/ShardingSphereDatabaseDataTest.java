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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDatabaseDataTest {
    
    private final ShardingSphereDatabaseData databaseData = new ShardingSphereDatabaseData();
    
    @BeforeEach
    void setUp() {
        databaseData.putSchema("foo_schema", new ShardingSphereSchemaData());
    }
    
    @Test
    void assertContainsSchema() {
        assertTrue(databaseData.containsSchema("foo_schema"));
        assertFalse(databaseData.containsSchema("bar_schema"));
    }
    
    @Test
    void assertGetSchema() {
        assertTrue(databaseData.getSchema("foo_schema").getTableData().isEmpty());
        assertNull(databaseData.getSchema("bar_schema"));
    }
    
    @Test
    void assertPutSchema() {
        databaseData.putSchema("bar_schema", new ShardingSphereSchemaData());
        assertTrue(databaseData.containsSchema("bar_schema"));
    }
    
    @Test
    void assertRemoveSchema() {
        databaseData.removeSchema("foo_schema");
        assertFalse(databaseData.containsSchema("foo_schema"));
    }
}
