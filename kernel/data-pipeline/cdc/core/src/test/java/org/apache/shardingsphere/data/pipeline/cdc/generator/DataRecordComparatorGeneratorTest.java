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

package org.apache.shardingsphere.data.pipeline.cdc.generator;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.junit.Test;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class DataRecordComparatorGeneratorTest {
    
    @Test
    public void assertGeneratorIncrementalComparator() {
        Comparator<DataRecord> dataRecordComparator = DataRecordComparatorGenerator.generatorIncrementalComparator(new OpenGaussDatabaseType());
        List<DataRecord> dataRecords = new LinkedList<>();
        dataRecords.add(generateDataRecord(1L));
        dataRecords.add(generateDataRecord(100L));
        dataRecords.add(generateDataRecord(0L));
        dataRecords.add(generateDataRecord(null));
        dataRecords.sort(dataRecordComparator);
        assertNull(dataRecords.get(0).getCsn());
        assertThat(dataRecords.get(1).getCsn(), is(0L));
        assertThat(dataRecords.get(2).getCsn(), is(1L));
        assertThat(dataRecords.get(3).getCsn(), is(100L));
    }
    
    private DataRecord generateDataRecord(final Long csn) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 0);
        result.setCsn(csn);
        return result;
    }
}
