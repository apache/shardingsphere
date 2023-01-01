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

package org.apache.shardingsphere.data.pipeline.core.util;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

public final class RecordUtilTest {
    
    @Test
    public void assertGetLastNormalRecord() {
        List<Record> actual = Arrays.asList(new DataRecord(new IntegerPrimaryKeyPosition(0, 1), 0), new PlaceholderRecord(new PlaceholderPosition()));
        Record expected = RecordUtil.getLastNormalRecord(actual);
        assertThat(expected, instanceOf(DataRecord.class));
        actual = Arrays.asList(new DataRecord(new IntegerPrimaryKeyPosition(0, 1), 0), new PlaceholderRecord(new PlaceholderPosition()), new FinishedRecord(new FinishedPosition()));
        expected = RecordUtil.getLastNormalRecord(actual);
        assertThat(expected, instanceOf(FinishedRecord.class));
    }
}
