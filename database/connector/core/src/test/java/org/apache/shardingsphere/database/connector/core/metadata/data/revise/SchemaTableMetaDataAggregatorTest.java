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
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    
    private TableMetaData createTableMetaData(final String tableName, final String columnName) {
        ColumnMetaData columnMetaData = new ColumnMetaData(columnName, Types.INTEGER, true, false,"", true, true, false, false);
        return new TableMetaData(tableName, Collections.singleton(columnMetaData), Collections.emptyList(), Collections.emptyList());
    }
}
