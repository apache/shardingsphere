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

package org.apache.shardingsphere.data.pipeline.common.ingest.record;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class RecordUtilsTest {
    
    @Test
    void assertExtractConditionalColumns() {
        DataRecord dataRecord = mockDataRecord();
        Collection<Column> actual = RecordUtils.extractConditionColumns(dataRecord, Collections.singleton("c1"));
        assertThat(actual.size(), is(3));
        assertThat(Arrays.asList("sc", "id", "c1"), hasItems(actual.iterator().next().getName()));
    }
    
    private DataRecord mockDataRecord() {
        DataRecord result = new DataRecord(IngestDataChangeType.INSERT, "t2", new PlaceholderPosition(), 4);
        result.addColumn(new Column("id", "", false, true));
        result.addColumn(new Column("sc", "", false, true));
        result.addColumn(new Column("c1", "", true, false));
        result.addColumn(new Column("c2", "", true, false));
        result.addColumn(new Column("c3", "", true, false));
        return result;
    }
}
