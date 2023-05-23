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

package org.apache.shardingsphere.data.pipeline.cdc.util;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckPosition;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.SocketSinkImporter;
import org.apache.shardingsphere.data.pipeline.cdc.generator.DataRecordComparatorGenerator;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CDCDataRecordUtilsTest {
    
    @Test
    void assertFindMinimumDataRecordAndSavePosition() throws InterruptedException {
        final Map<SocketSinkImporter, BlockingQueue<List<DataRecord>>> actualIncrementalRecordMap = new HashMap<>();
        ArrayBlockingQueue<List<DataRecord>> queueFirst = new ArrayBlockingQueue<>(5);
        queueFirst.put(Collections.singletonList(generateDataRecord(0)));
        queueFirst.put(Collections.singletonList(generateDataRecord(2)));
        queueFirst.put(Collections.singletonList(generateDataRecord(4)));
        SocketSinkImporter mockSocketSinkImporterFirst = mock(SocketSinkImporter.class);
        actualIncrementalRecordMap.put(mockSocketSinkImporterFirst, queueFirst);
        ArrayBlockingQueue<List<DataRecord>> queueSecond = new ArrayBlockingQueue<>(5);
        queueSecond.put(Collections.singletonList(generateDataRecord(1)));
        queueSecond.put(Collections.singletonList(generateDataRecord(3)));
        queueSecond.put(Collections.singletonList(generateDataRecord(5)));
        SocketSinkImporter mockSocketSinkImporterSecond = mock(SocketSinkImporter.class);
        actualIncrementalRecordMap.put(mockSocketSinkImporterSecond, queueSecond);
        Comparator<DataRecord> dataRecordComparator = DataRecordComparatorGenerator.generatorIncrementalComparator(new OpenGaussDatabaseType());
        final Map<SocketSinkImporter, CDCAckPosition> cdcAckPositionMap = new HashMap<>();
        for (long i = 0; i <= 5; i++) {
            List<DataRecord> minimumDataRecord = CDCDataRecordUtils.findMinimumDataRecordsAndSavePosition(actualIncrementalRecordMap, dataRecordComparator, cdcAckPositionMap);
            assertThat(minimumDataRecord.size(), is(1));
            assertThat(minimumDataRecord.get(0).getCsn(), is(i));
        }
        assertTrue(CDCDataRecordUtils.findMinimumDataRecordsAndSavePosition(actualIncrementalRecordMap, dataRecordComparator, cdcAckPositionMap).isEmpty());
    }
    
    private DataRecord generateDataRecord(final long csn) {
        DataRecord dataRecord = new DataRecord(new PlaceholderPosition(), 0);
        dataRecord.setCsn(csn);
        return dataRecord;
    }
}
