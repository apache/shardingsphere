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

class SchemaStatisticsTest {
    
    private static final String TEST_TABLE_NAME = "TEST_TABLE_NAME";
    
    private static final String TEST_TABLE_2 = "TEST_TABLE_2";
    
    private static final String NON_EXISTENT_TABLE = "NON_EXISTENT_TABLE";
    
    @Test
    void assertGetTableStatistics() {
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics(TEST_TABLE_NAME, new TableStatistics(TEST_TABLE_NAME));
        TableStatistics tableStatistics = schemaStatistics.getTableStatistics(TEST_TABLE_NAME);
        assertTrue(TEST_TABLE_NAME.equalsIgnoreCase(tableStatistics.getName()));
        assertNull(schemaStatistics.getTableStatistics(NON_EXISTENT_TABLE));
    }
    
    @Test
    void assertPutTableStatistics() {
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics(TEST_TABLE_NAME, new TableStatistics(TEST_TABLE_NAME));
        assertThat(schemaStatistics.getTableStatisticsMap().size(), is(1));
        assertFalse(schemaStatistics.getTableStatisticsMap().containsKey(TEST_TABLE_2));
        TableStatistics newTableStatistics = new TableStatistics(TEST_TABLE_2);
        schemaStatistics.putTableStatistics(TEST_TABLE_2, newTableStatistics);
        assertThat(schemaStatistics.getTableStatisticsMap().size(), is(2));
        assertTrue(schemaStatistics.containsTableStatistics(TEST_TABLE_2));
    }
    
    @Test
    void assertRemoveTableStatistics() {
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics(TEST_TABLE_NAME, new TableStatistics(TEST_TABLE_NAME));
        assertTrue(schemaStatistics.containsTableStatistics(TEST_TABLE_NAME));
        schemaStatistics.removeTableStatistics(TEST_TABLE_NAME);
        assertTrue(schemaStatistics.getTableStatisticsMap().isEmpty());
    }
    
    @Test
    void assertContainsTableStatistics() {
        SchemaStatistics schemaStatistics = new SchemaStatistics();
        schemaStatistics.putTableStatistics(TEST_TABLE_NAME, new TableStatistics(TEST_TABLE_NAME));
        assertTrue(schemaStatistics.containsTableStatistics(TEST_TABLE_NAME));
        assertFalse(schemaStatistics.containsTableStatistics(NON_EXISTENT_TABLE));
    }
}
