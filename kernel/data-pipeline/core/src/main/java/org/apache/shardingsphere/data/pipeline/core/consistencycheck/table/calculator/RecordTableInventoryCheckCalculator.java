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

package org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.calculator;

import org.apache.shardingsphere.data.pipeline.core.consistencycheck.DataConsistencyCheckUtils;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.RecordTableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableInventoryCheckCalculatedResult;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.InventoryColumnValueReaderEngine;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.StreamingRangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.calculator.AbstractRecordTableInventoryCalculator;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Record table inventory check calculator.
 */
public final class RecordTableInventoryCheckCalculator extends AbstractRecordTableInventoryCalculator<TableInventoryCheckCalculatedResult, Map<String, Object>> {
    
    public RecordTableInventoryCheckCalculator(final int chunkSize, final int streamingChunkCount, final StreamingRangeType streamingRangeType) {
        super(chunkSize, streamingChunkCount, streamingRangeType);
    }
    
    public RecordTableInventoryCheckCalculator(final int chunkSize, final StreamingRangeType streamingRangeType) {
        super(chunkSize, streamingRangeType);
    }
    
    @Override
    protected Map<String, Object> readRecord(final ResultSet resultSet, final ResultSetMetaData resultSetMetaData, final InventoryColumnValueReaderEngine columnValueReaderEngine) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int columnIndex = 1, columnCount = resultSetMetaData.getColumnCount(); columnIndex <= columnCount; columnIndex++) {
            result.put(resultSetMetaData.getColumnLabel(columnIndex), columnValueReaderEngine.read(resultSet, resultSetMetaData, columnIndex));
        }
        return result;
    }
    
    @Override
    protected Object getFirstUniqueKeyValue(final Map<String, Object> record, final String firstUniqueKey) {
        return DataConsistencyCheckUtils.getFirstUniqueKeyValue(record, firstUniqueKey);
    }
    
    @Override
    protected TableInventoryCheckCalculatedResult convertRecordsToResult(final List<Map<String, Object>> records, final Object maxUniqueKeyValue) {
        return new RecordTableInventoryCheckCalculatedResult(maxUniqueKeyValue, records);
    }
}
