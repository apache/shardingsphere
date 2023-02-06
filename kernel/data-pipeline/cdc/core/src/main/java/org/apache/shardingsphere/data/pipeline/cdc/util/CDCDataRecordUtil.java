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

import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.cdc.core.ack.CDCAckPosition;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

/**
 * CDC data record util.
 */
public final class CDCDataRecordUtil {
    
    /**
     * Find minimum data record and save position.
     *
     * @param incrementalRecordMap CDC ack position map.
     * @param dataRecordComparator CDC ack position map.
     * @param cdcAckPositionMap CDC ack position map.
     * @return minimum data record
     */
    public static DataRecord findMinimumDataRecordAndSavePosition(final Map<CDCImporter, BlockingQueue<Record>> incrementalRecordMap, final Comparator<DataRecord> dataRecordComparator,
                                                                  final Map<CDCImporter, CDCAckPosition> cdcAckPositionMap) {
        if (null == dataRecordComparator) {
            return findMinimumDataRecordWithoutComparator(incrementalRecordMap, cdcAckPositionMap);
        } else {
            return findMinimumDataRecordWithComparator(incrementalRecordMap, cdcAckPositionMap, dataRecordComparator);
        }
    }
    
    private static DataRecord findMinimumDataRecordWithoutComparator(final Map<CDCImporter, BlockingQueue<Record>> incrementalRecordMap, final Map<CDCImporter, CDCAckPosition> cdcAckPositionMap) {
        for (Entry<CDCImporter, BlockingQueue<Record>> entry : incrementalRecordMap.entrySet()) {
            Record record = entry.getValue().poll();
            if (!(record instanceof DataRecord)) {
                continue;
            }
            saveAckPosition(cdcAckPositionMap, entry.getKey(), record);
            return (DataRecord) record;
        }
        return null;
    }
    
    private static void saveAckPosition(final Map<CDCImporter, CDCAckPosition> cdcAckPositionMap, final CDCImporter cdcImporter, final Record record) {
        CDCAckPosition cdcAckPosition = cdcAckPositionMap.get(cdcImporter);
        if (null == cdcAckPosition) {
            cdcAckPositionMap.put(cdcImporter, new CDCAckPosition(record, 1));
        } else {
            cdcAckPosition.setLastRecord(record);
            cdcAckPosition.setDataRecordCount(cdcAckPosition.getDataRecordCount());
        }
    }
    
    private static DataRecord findMinimumDataRecordWithComparator(final Map<CDCImporter, BlockingQueue<Record>> incrementalRecordMap, final Map<CDCImporter, CDCAckPosition> cdcAckPositionMap,
                                                                  final Comparator<DataRecord> dataRecordComparator) {
        Map<CDCImporter, DataRecord> waitSortedMap = new HashMap<>();
        for (Entry<CDCImporter, BlockingQueue<Record>> entry : incrementalRecordMap.entrySet()) {
            Record peek = entry.getValue().peek();
            if (null == peek) {
                continue;
            }
            if (peek instanceof DataRecord) {
                waitSortedMap.put(entry.getKey(), (DataRecord) peek);
            }
        }
        if (waitSortedMap.isEmpty()) {
            return null;
        }
        DataRecord minRecord = null;
        CDCImporter belongImporter = null;
        for (Entry<CDCImporter, DataRecord> entry : waitSortedMap.entrySet()) {
            if (null == minRecord) {
                minRecord = entry.getValue();
                belongImporter = entry.getKey();
                continue;
            }
            if (dataRecordComparator.compare(minRecord, entry.getValue()) > 0) {
                minRecord = entry.getValue();
                belongImporter = entry.getKey();
            }
        }
        if (null == minRecord) {
            return null;
        }
        incrementalRecordMap.get(belongImporter).poll();
        saveAckPosition(cdcAckPositionMap, belongImporter, minRecord);
        return minRecord;
    }
}
