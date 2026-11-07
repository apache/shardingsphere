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

package org.apache.shardingsphere.database.connector.core.metadata.data.revise;

import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTableMetaDataAggregatorTest {
    
    @Test
    void assertAggregate() {
        SchemaTableMetaDataAggregator aggregator = new SchemaTableMetaDataAggregator(false);
        Map<String, Collection<TableMetaData>> input = new LinkedHashMap<>(2, 1F);
        input.put("foo_tbl", Arrays.asList(createTableMetaData("foo_tbl_a", "foo_col_a"), createTableMetaData("foo_tbl_b", "foo_col_b")));
        input.put("bar_tbl", Collections.singletonList(createTableMetaData("bar_tbl_a", "bar_col_a")));
        Collection<TableMetaData> actual = aggregator.aggregate(input);
        Collection<String> actualNames = actual.stream().map(TableMetaData::getName).collect(Collectors.toList());
        assertThat(actualNames.size(), is(2));
        assertThat(actualNames, hasItems("foo_tbl_a", "bar_tbl_a"));
    }
    
    @Test
    void assertAggregateWithViolationThrowsException() {
        SchemaTableMetaDataAggregator aggregator = new SchemaTableMetaDataAggregator(true);
        Map<String, Collection<TableMetaData>> input = Collections.singletonMap("foo_tbl",
                Arrays.asList(createTableMetaData("foo_tbl_a", "foo_col_a"), createTableMetaData("foo_tbl_b", "foo_col_b")));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class, () -> aggregator.aggregate(input));
        assertTrue(actual.getMessage().contains("foo_tbl"));
        assertTrue(actual.getMessage().contains("foo_tbl_b"));
    }
    
    @Test
    void assertAggregateWithSameStructureDifferentStorageUnitNames() {
        SchemaTableMetaDataAggregator aggregator = new SchemaTableMetaDataAggregator(true);
        TableMetaData firstTableMetaData = createTableMetaData("foo_tbl", "foo_col", "ds_0");
        TableMetaData secondTableMetaData = createTableMetaData("foo_tbl", "foo_col", "ds_1");
        Map<String, Collection<TableMetaData>> input = Collections.singletonMap("foo_tbl", Arrays.asList(firstTableMetaData, secondTableMetaData));
        Collection<TableMetaData> actual = aggregator.aggregate(input);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getName(), is("foo_tbl"));
    }
    
    @Test
    void assertAggregateWithDifferentIndexOrder() {
        IndexMetaData firstIndex = new IndexMetaData("foo_index", Collections.singletonList("foo_col"));
        IndexMetaData secondIndex = new IndexMetaData("bar_index", Collections.singletonList("bar_col"));
        TableMetaData sample = createIndexedTableMetaData(Arrays.asList(firstIndex, secondIndex));
        TableMetaData equivalent = createIndexedTableMetaData(Arrays.asList(secondIndex, firstIndex));
        Collection<TableMetaData> actual = new SchemaTableMetaDataAggregator(true).aggregate(Collections.singletonMap("foo_tbl", Arrays.asList(sample, equivalent)));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(sample));
        assertThat(actual.iterator().next().getIndexes(), is(Arrays.asList(firstIndex, secondIndex)));
        assertThat(equivalent.getIndexes(), is(Arrays.asList(secondIndex, firstIndex)));
    }
    
    private TableMetaData createIndexedTableMetaData(final Collection<IndexMetaData> indexes) {
        return new TableMetaData("foo_tbl", Arrays.asList(new ColumnMetaData("foo_col", Types.INTEGER, true, false, true, true, false, false),
                new ColumnMetaData("bar_col", Types.INTEGER, false, false, true, true, false, false)), indexes, Collections.emptyList());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInconsistentIndexes")
    void assertAggregateWithInconsistentIndexes(final String scenario, final Collection<IndexMetaData> indexes) {
        TableMetaData sample = createIndexedTableMetaData(Collections.singletonList(new IndexMetaData("foo_index", Arrays.asList("foo_col", "bar_col"))));
        TableMetaData changed = createIndexedTableMetaData(indexes);
        assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> new SchemaTableMetaDataAggregator(true).aggregate(Collections.singletonMap("foo_tbl", Arrays.asList(sample, changed))));
    }
    
    private static Stream<Arguments> getInconsistentIndexes() {
        IndexMetaData uniqueIndex = new IndexMetaData("foo_index", Arrays.asList("foo_col", "bar_col"));
        uniqueIndex.setUnique(true);
        return Stream.of(
                Arguments.of("column order", Collections.singletonList(new IndexMetaData("foo_index", Arrays.asList("bar_col", "foo_col")))),
                Arguments.of("uniqueness", Collections.singletonList(uniqueIndex)),
                Arguments.of("index name", Collections.singletonList(new IndexMetaData("bar_index", Arrays.asList("foo_col", "bar_col")))),
                Arguments.of("missing index", Collections.emptyList()));
    }
    
    @Test
    void assertAggregateWithDifferentConstraintOrder() {
        ConstraintMetaData firstConstraint = new ConstraintMetaData("foo_fk", "foo_ref");
        ConstraintMetaData secondConstraint = new ConstraintMetaData("bar_fk", "bar_ref");
        TableMetaData sample = createConstrainedTableMetaData(Arrays.asList(firstConstraint, secondConstraint));
        TableMetaData equivalent = createConstrainedTableMetaData(Arrays.asList(secondConstraint, firstConstraint));
        Collection<TableMetaData> actual = new SchemaTableMetaDataAggregator(true).aggregate(Collections.singletonMap("foo_tbl", Arrays.asList(sample, equivalent)));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), sameInstance(sample));
        assertThat(sample.getConstraints(), is(Arrays.asList(firstConstraint, secondConstraint)));
        assertThat(equivalent.getConstraints(), is(Arrays.asList(secondConstraint, firstConstraint)));
    }
    
    private TableMetaData createConstrainedTableMetaData(final Collection<ConstraintMetaData> constraints) {
        return new TableMetaData("foo_tbl", Collections.singletonList(new ColumnMetaData("foo_col", Types.INTEGER, true, false, true, true, false, false)),
                Collections.emptyList(), constraints);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getInconsistentConstraints")
    void assertAggregateWithInconsistentConstraints(final String scenario, final Collection<ConstraintMetaData> constraints) {
        TableMetaData sample = createConstrainedTableMetaData(Collections.singletonList(new ConstraintMetaData("foo_fk", "foo_ref")));
        TableMetaData changed = createConstrainedTableMetaData(constraints);
        assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> new SchemaTableMetaDataAggregator(true).aggregate(Collections.singletonMap("foo_tbl", Arrays.asList(sample, changed))));
    }
    
    private static Stream<Arguments> getInconsistentConstraints() {
        return Stream.of(
                Arguments.of("constraint name", Collections.singletonList(new ConstraintMetaData("bar_fk", "foo_ref"))),
                Arguments.of("referenced table", Collections.singletonList(new ConstraintMetaData("foo_fk", "bar_ref"))),
                Arguments.of("missing constraint", Collections.emptyList()));
    }
    
    private TableMetaData createTableMetaData(final String tableName, final String columnName) {
        return createTableMetaData(tableName, columnName, null);
    }
    
    private TableMetaData createTableMetaData(final String tableName, final String columnName, final String storageUnitName) {
        ColumnMetaData columnMetaData = new ColumnMetaData(columnName, Types.INTEGER, true, false, true, true, false, false);
        TableMetaData result = new TableMetaData(tableName, Collections.singleton(columnMetaData), Collections.emptyList(), Collections.emptyList());
        result.setStorageUnitName(storageUnitName);
        return result;
    }
}
