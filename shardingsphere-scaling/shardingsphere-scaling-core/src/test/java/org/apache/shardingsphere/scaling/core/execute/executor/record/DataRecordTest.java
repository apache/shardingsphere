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

package org.apache.shardingsphere.scaling.core.execute.executor.record;

import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataRecordTest {
    
    private DataRecord beforeDataRecord;
    
    private DataRecord afterDataRecord;
    
    @Test
    public void assertKeyEqual() {
        beforeDataRecord = new DataRecord(new PlaceholderPosition(), 2);
        beforeDataRecord.setTableName("t1");
        beforeDataRecord.addColumn(new Column("id", 1, true, true));
        beforeDataRecord.addColumn(new Column("name", "1", true, false));
        afterDataRecord = new DataRecord(new PlaceholderPosition(), 2);
        afterDataRecord.setTableName("t1");
        afterDataRecord.addColumn(new Column("id", 1, true, true));
        afterDataRecord.addColumn(new Column("name", "2", true, false));
        assertThat(beforeDataRecord.getKey(), is(afterDataRecord.getKey()));
    }
    
    @Test
    public void assertOldKeyEqual() {
        beforeDataRecord = new DataRecord(new PlaceholderPosition(), 2);
        beforeDataRecord.setTableName("t1");
        beforeDataRecord.addColumn(new Column("id", 1, true, true));
        beforeDataRecord.addColumn(new Column("name", "1", true, false));
        afterDataRecord = new DataRecord(new PlaceholderPosition(), 2);
        afterDataRecord.setTableName("t1");
        afterDataRecord.addColumn(new Column("id", 1, 2, true, true));
        afterDataRecord.addColumn(new Column("name", "2", true, false));
        assertThat(beforeDataRecord.getKey(), is(afterDataRecord.getOldKey()));
    }
}
