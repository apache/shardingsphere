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

class DatabaseStatisticsTest {
    
    private final DatabaseStatistics databaseStatistics = new DatabaseStatistics();
    
    @BeforeEach
    void setUp() {
        databaseStatistics.putSchemaStatistics("foo_schema", new SchemaStatistics());
    }
    
    @Test
    void assertContainsSchemaStatistics() {
        assertTrue(databaseStatistics.containsSchemaStatistics("foo_schema"));
        assertFalse(databaseStatistics.containsSchemaStatistics("bar_schema"));
    }
    
    @Test
    void assertGetSchemaStatistics() {
        assertTrue(databaseStatistics.getSchemaStatistics("foo_schema").getTableStatisticsMap().isEmpty());
        assertNull(databaseStatistics.getSchemaStatistics("bar_schema"));
    }
    
    @Test
    void assertPutSchemaStatistics() {
        databaseStatistics.putSchemaStatistics("bar_schema", new SchemaStatistics());
        assertTrue(databaseStatistics.containsSchemaStatistics("bar_schema"));
    }
    
    @Test
    void assertRemoveSchemaStatistics() {
        databaseStatistics.removeSchemaStatistics("foo_schema");
        assertFalse(databaseStatistics.containsSchemaStatistics("foo_schema"));
    }
}
