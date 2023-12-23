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

package org.apache.shardingsphere.data.pipeline.core.ingest.record;

import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DataRecordTest {
    
    @Test
    void assertGetKeyWithUpdate() {
        DataRecord beforeDataRecord = new DataRecord(IngestDataChangeType.UPDATE, "foo_tbl", new IngestPlaceholderPosition(), 1);
        beforeDataRecord.addColumn(new Column("id", 1, true, true));
        DataRecord afterDataRecord = new DataRecord(IngestDataChangeType.UPDATE, "foo_tbl", new IngestPlaceholderPosition(), 1);
        afterDataRecord.addColumn(new Column("id", 2, 1, true, true));
        assertThat(beforeDataRecord.getKey(), is(afterDataRecord.getKey()));
    }
    
    @Test
    void assertGetKeyWithDelete() {
        DataRecord beforeDataRecord = new DataRecord(IngestDataChangeType.DELETE, "foo_tbl", new IngestPlaceholderPosition(), 1);
        beforeDataRecord.addColumn(new Column("id", 1, 2, true, true));
        DataRecord afterDataRecord = new DataRecord(IngestDataChangeType.DELETE, "foo_tbl", new IngestPlaceholderPosition(), 1);
        afterDataRecord.addColumn(new Column("id", 1, 3, true, true));
        assertThat(beforeDataRecord.getKey(), is(afterDataRecord.getKey()));
    }
}
