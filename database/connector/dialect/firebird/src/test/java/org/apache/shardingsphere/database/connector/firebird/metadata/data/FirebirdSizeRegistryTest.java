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

package org.apache.shardingsphere.database.connector.firebird.metadata.data;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdSizeRegistryTest {
    
    @Test
    void assertRefreshAndFindColumnSize() {
        Map<String, Integer> columnSizes = Collections.singletonMap("varchar_col", 64);
        FirebirdSizeRegistry.refreshTable("schema_a", "table_a", columnSizes);
        OptionalInt actual = FirebirdSizeRegistry.findColumnSize("schema_a", "table_a", "VARCHAR_COL");
        assertTrue(actual.isPresent());
        assertThat(actual.getAsInt(), is(64));
        FirebirdSizeRegistry.refreshTable("schema_a", "table_a", Collections.emptyMap());
    }
    
    @Test
    void assertRefreshTableRemovesEntryWhenEmptyColumnSizesProvided() {
        FirebirdSizeRegistry.refreshTable("schema_b", "table_b", Collections.singletonMap("col", 32));
        FirebirdSizeRegistry.refreshTable("schema_b", "table_b", Collections.emptyMap());
        assertFalse(FirebirdSizeRegistry.findColumnSize("schema_b", "table_b", "COL").isPresent());
    }
    
    @Test
    void assertRefreshTableSkipsNullColumnNames() {
        Map<String, Integer> columnSizes = new HashMap<>(2, 1F);
        columnSizes.put("valid", 12);
        columnSizes.put(null, 24);
        FirebirdSizeRegistry.refreshTable("schema_c", "table_c", columnSizes);
        OptionalInt actual = FirebirdSizeRegistry.findColumnSize("schema_c", "table_c", "VaLiD");
        assertTrue(actual.isPresent());
        assertThat(actual.getAsInt(), is(12));
        assertFalse(FirebirdSizeRegistry.findColumnSize("schema_c", "table_c", null).isPresent());
        FirebirdSizeRegistry.refreshTable("schema_c", "table_c", Collections.emptyMap());
    }
    
    @Test
    void assertRefreshTableRemovesWhenAllColumnsInvalid() {
        Map<String, Integer> columnSizes = new HashMap<>(1, 1F);
        columnSizes.put(null, 48);
        FirebirdSizeRegistry.refreshTable("schema_d", "table_d", columnSizes);
        assertFalse(FirebirdSizeRegistry.findColumnSize("schema_d", "table_d", "any").isPresent());
    }
}
