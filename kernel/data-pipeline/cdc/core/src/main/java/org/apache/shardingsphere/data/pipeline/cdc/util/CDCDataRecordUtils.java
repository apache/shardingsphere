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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckPosition;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.SocketSinkImporter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

/**
 * CDC data record utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CDCDataRecordUtils {
    
    /**
     * Find minimum data record and save position.
     *
     * @param incrementalRecordMap CDC ack position map.
     * @param dataRecordComparator CDC ack position map.
     * @param cdcAckPositionMap CDC ack position map.
     * @return minimum data record
     */
    public static List<DataRecord> findMinimumDataRecordsAndSavePosition(final Map<SocketSinkImporter, BlockingQueue<List<DataRecord>>> incrementalRecordMap,
                                                                         final Comparator<DataRecord> dataRecordComparator, final Map<SocketSinkImporter, CDCAckPosition> cdcAckPositionMap) {
        if (null == dataRecordComparator) {
            return findMinimumDataRecordWithoutComparator(incrementalRecordMap, cdcAckPositionMap);
        } else {
            return findMinimumDataRecordWithComparator(incrementalRecordMap, cdcAckPositionMap, dataRecordComparator);
        }
    }
    
    private static List<DataRecord> findMinimumDataRecordWithoutComparator(final Map<SocketSinkImporter, BlockingQueue<List<DataRecord>>> incrementalRecordMap,
                                                                           final Map<SocketSinkImporter, CDCAckPosition> cdcAckPositionMap) {
        for (Entry<SocketSinkImporter, BlockingQueue<List<DataRecord>>> entry : incrementalRecordMap.entrySet()) {
            List<DataRecord> records = entry.getValue().poll();
            if (null == records || records.isEmpty()) {
                continue;
            }
            DataRecord lastRecord = records.get(records.size() - 1);
            saveAckPosition(cdcAckPositionMap, entry.getKey(), lastRecord);
            return records;
        }
        return Collections.emptyList();
    }
    
    private static void saveAckPosition(final Map<SocketSinkImporter, CDCAckPosition> cdcAckPositionMap, final SocketSinkImporter socketSinkImporter, final Record record) {
        CDCAckPosition cdcAckPosition = cdcAckPositionMap.get(socketSinkImporter);
        if (null == cdcAckPosition) {
            cdcAckPositionMap.put(socketSinkImporter, new CDCAckPosition(record, 1));
        } else {
            cdcAckPosition.setLastRecord(record);
            cdcAckPosition.addDataRecordCount(cdcAckPosition.getDataRecordCount());
        }
    }
    
    private static List<DataRecord> findMinimumDataRecordWithComparator(final Map<SocketSinkImporter, BlockingQueue<List<DataRecord>>> incrementalRecordMap,
                                                                        final Map<SocketSinkImporter, CDCAckPosition> cdcAckPositionMap, final Comparator<DataRecord> dataRecordComparator) {
        Map<SocketSinkImporter, List<DataRecord>> waitSortedMap = new HashMap<>();
        for (Entry<SocketSinkImporter, BlockingQueue<List<DataRecord>>> entry : incrementalRecordMap.entrySet()) {
            List<DataRecord> peek = entry.getValue().peek();
            if (null == peek) {
                continue;
            }
            waitSortedMap.put(entry.getKey(), peek);
        }
        if (waitSortedMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataRecord> result = null;
        SocketSinkImporter belongImporter = null;
        for (Entry<SocketSinkImporter, List<DataRecord>> entry : waitSortedMap.entrySet()) {
            if (null == result) {
                result = entry.getValue();
                belongImporter = entry.getKey();
                continue;
            }
            if (dataRecordComparator.compare(result.get(0), entry.getValue().get(0)) > 0) {
                result = entry.getValue();
                belongImporter = entry.getKey();
            }
        }
        if (null == result) {
            return Collections.emptyList();
        }
        incrementalRecordMap.get(belongImporter).poll();
        saveAckPosition(cdcAckPositionMap, belongImporter, result.get(result.size() - 1));
        return result;
    }
}
