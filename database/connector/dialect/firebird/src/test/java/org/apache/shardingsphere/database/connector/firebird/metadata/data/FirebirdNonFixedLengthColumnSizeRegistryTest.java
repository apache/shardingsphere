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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdNonFixedLengthColumnSizeRegistryTest {
    
    private Map<String, Map<String, Integer>> columnSizes;
    
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws ReflectiveOperationException {
        columnSizes = (Map<String, Map<String, Integer>>) Plugins.getMemberAccessor()
                .get(FirebirdNonFixedLengthColumnSizeRegistry.class.getDeclaredField("COLUMN_SIZES"), FirebirdNonFixedLengthColumnSizeRegistry.class);
        columnSizes.clear();
    }
    
    @AfterEach
    void tearDown() {
        columnSizes.clear();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshTableRemoveCases")
    void assertRefreshTableRemovesEntry(final String name, final String schemaName, final String tableName, final Map<String, Integer> newColumnSizes, final String tableKey) {
        columnSizes.put(tableKey, Collections.singletonMap("EXISTING_COL", 9));
        FirebirdNonFixedLengthColumnSizeRegistry.refreshTable(schemaName, tableName, newColumnSizes);
        assertFalse(columnSizes.containsKey(tableKey));
    }
    
    @Test
    void assertRefreshTableWhenTableNameIsNull() {
        columnSizes.put("SENTINEL", Collections.singletonMap("EXISTING_COL", 9));
        FirebirdNonFixedLengthColumnSizeRegistry.refreshTable("schema_a", null, Collections.singletonMap("size_col", 16));
        assertTrue(columnSizes.containsKey("SENTINEL"));
        assertThat(columnSizes.get("SENTINEL").get("EXISTING_COL"), is(9));
    }
    
    @Test
    void assertRefreshTable() {
        FirebirdNonFixedLengthColumnSizeRegistry.refreshTable("schema_c", "table_x", createColumnSizesWithMixedNames());
        Map<String, Integer> actual = columnSizes.get("SCHEMA_C.TABLE_X");
        assertTrue(actual.containsKey("SIZE_COL"));
        assertThat(actual.get("SIZE_COL"), is(64));
        assertThat(actual.size(), is(1));
        assertThrows(UnsupportedOperationException.class, () -> actual.put("ANOTHER", 32));
    }
    
    private static Map<String, Integer> createColumnSizesWithMixedNames() {
        Map<String, Integer> result = new HashMap<>(2, 1F);
        result.put("size_col", 64);
        result.put(null, 128);
        return result;
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("findColumnSizeEmptyCases")
    void assertFindColumnSizeWhenAbsent(final String name, final String tableName, final String columnName, final boolean registerTable, final boolean registerColumn) {
        if (registerTable) {
            columnSizes.put("SCHEMA_A.TABLE_A", registerColumn ? Collections.singletonMap("SIZE_COL", 12) : Collections.singletonMap("OTHER_COL", 12));
        }
        assertFalse(FirebirdNonFixedLengthColumnSizeRegistry.findColumnSize("schema_a", tableName, columnName).isPresent());
    }
    
    @Test
    void assertFindColumnSize() {
        columnSizes.put("SCHEMA_A.123", Collections.singletonMap("SIZE_COL", 24));
        OptionalInt actual = FirebirdNonFixedLengthColumnSizeRegistry.findColumnSize("schema_a", "123", "size_col");
        assertTrue(actual.isPresent());
        assertThat(actual.getAsInt(), is(24));
    }
    
    private static Stream<Arguments> refreshTableRemoveCases() {
        return Stream.of(
                Arguments.of("empty_column_sizes", "schema_a", "table_a", Collections.emptyMap(), "SCHEMA_A.TABLE_A"),
                Arguments.of("empty_column_sizes_with_trimmed_table", null, "table_1", Collections.emptyMap(), ".TABLE"),
                Arguments.of("all_column_names_are_null", "schema_b", "123", Collections.singletonMap(null, 9), "SCHEMA_B.123"));
    }
    
    private static Stream<Arguments> findColumnSizeEmptyCases() {
        return Stream.of(
                Arguments.of("null_table_name", null, "size_col", false, false),
                Arguments.of("null_column_name", "table_a", null, true, true),
                Arguments.of("table_not_registered", "table_a", "size_col", false, false),
                Arguments.of("column_not_registered", "table_a", "size_col", true, false));
    }
}
