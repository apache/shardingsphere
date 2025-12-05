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

package org.apache.shardingsphere.infra.metadata.statistics.utils;

import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class RowStatisticsCollectorUtilsTest {
    
    @Test
    void assertCreateRowValues() {
        ShardingSphereTable table = new ShardingSphereTable("foo_tbl", createColumns(), Collections.emptyList(), Collections.emptyList());
        List<Object> actual = RowStatisticsCollectorUtils.createRowValues(createColumnValues(), table);
        assertThat(actual.size(), is(10));
        assertThat(actual.get(0), is(1000L));
        assertThat(actual.get(1), is("test"));
        assertThat(actual.get(2), is('a'));
        assertThat(actual.get(3), is("other_data"));
        assertThat(actual.get(4), is(""));
        assertThat(actual.get(5), is(100));
        assertThat(actual.get(6), is(25));
        assertThat(actual.get(7), is(3.14f));
        assertThat(actual.get(8), is(true));
        assertNull(actual.get(9));
    }
    
    private Collection<ShardingSphereColumn> createColumns() {
        ShardingSphereColumn bigintColumn = new ShardingSphereColumn("bigint_col", Types.BIGINT, false, false, "", false, false, false, false);
        ShardingSphereColumn varcharColumn = new ShardingSphereColumn("varchar_col", Types.VARCHAR, false, false, "", false, false, false, false);
        ShardingSphereColumn charColumn = new ShardingSphereColumn("char_col", Types.CHAR, false, false, "", false, false, false, false);
        ShardingSphereColumn otherColumn = new ShardingSphereColumn("other_col", Types.OTHER, false, false, "", false, false, false, false);
        ShardingSphereColumn arrayColumn = new ShardingSphereColumn("array_col", Types.ARRAY, false, false, "", false, false, false, false);
        ShardingSphereColumn integerColumn = new ShardingSphereColumn("integer_col", Types.INTEGER, true, false, "", false, false, false, false);
        ShardingSphereColumn smallintColumn = new ShardingSphereColumn("smallint_col", Types.SMALLINT, false, false, "", false, false, false, false);
        ShardingSphereColumn realColumn = new ShardingSphereColumn("real_col", Types.REAL, false, false, "", false, false, false, false);
        ShardingSphereColumn bitColumn = new ShardingSphereColumn("bit_col", Types.BIT, false, false, "", false, false, false, false);
        ShardingSphereColumn blobColumn = new ShardingSphereColumn("blob_col", Types.BLOB, false, false, "", false, false, false, false);
        return Arrays.asList(bigintColumn, varcharColumn, charColumn, otherColumn, arrayColumn, integerColumn, smallintColumn, realColumn, bitColumn, blobColumn);
    }
    
    private Map<String, Object> createColumnValues() {
        Map<String, Object> result = new HashMap<>(10, 1F);
        result.put("bigint_col", 1000L);
        result.put("varchar_col", "test");
        result.put("char_col", 'a');
        result.put("other_col", "other_data");
        result.put("array_col", "");
        result.put("integer_col", 100);
        result.put("smallint_col", 25);
        result.put("real_col", 3.14f);
        result.put("bit_col", true);
        result.put("blob_col", null);
        return result;
    }
}
