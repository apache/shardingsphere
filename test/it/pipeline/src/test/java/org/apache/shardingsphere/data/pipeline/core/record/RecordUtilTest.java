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

package org.apache.shardingsphere.data.pipeline.core.record;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public final class RecordUtilTest {
    
    @Test
    public void assertExtractPrimaryColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        Collection<Column> actual = RecordUtil.extractPrimaryColumns(dataRecord);
        assertThat(actual.size(), is(2));
        assertThat(Arrays.asList("sc", "id"), hasItems(actual.iterator().next().getName()));
    }
    
    @Test
    public void assertExtractConditionalColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        Collection<Column> actual = RecordUtil.extractConditionColumns(dataRecord, Collections.singleton("c1"));
        assertThat(actual.size(), is(3));
        assertThat(Arrays.asList("sc", "id", "c1"), hasItems(actual.iterator().next().getName()));
    }
    
    @Test
    public void assertExtractUpdatedColumns() {
        DataRecord dataRecord = mockDataRecord("t2");
        Collection<Column> actual = RecordUtil.extractUpdatedColumns(dataRecord);
        assertThat(actual.size(), is(3));
        assertThat(Arrays.asList("c2", "c3", "c1"), hasItems(actual.iterator().next().getName()));
    }
    
    private DataRecord mockDataRecord(final String tableName) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 4);
        result.setTableName(tableName);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, true));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
}
