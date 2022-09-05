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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;

import java.util.List;

/**
 * Convert wal event to {@code Record}.
 */
public final class WalEventConverter {
    
    private final DumperConfiguration dumperConfig;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    public WalEventConverter(final DumperConfiguration dumperConfig, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperConfig = dumperConfig;
        this.metaDataLoader = metaDataLoader;
    }
    
    /**
     * Convert wal event to {@code Record}.
     *
     * @param event of wal
     * @return record
     */
    public Record convert(final AbstractWalEvent event) {
        if (filter(event)) {
            return createPlaceholderRecord(event);
        } else if (event instanceof WriteRowEvent) {
            return handleWriteRowsEvent((WriteRowEvent) event);
        } else if (event instanceof UpdateRowEvent) {
            return handleUpdateRowsEvent((UpdateRowEvent) event);
        } else if (event instanceof DeleteRowEvent) {
            return handleDeleteRowsEvent((DeleteRowEvent) event);
        } else if (event instanceof PlaceholderEvent) {
            return createPlaceholderRecord(event);
        }
        throw new UnsupportedOperationException("");
    }
    
    private boolean filter(final AbstractWalEvent event) {
        if (isRowEvent(event)) {
            AbstractRowEvent rowEvent = (AbstractRowEvent) event;
            return !dumperConfig.containsTable(rowEvent.getTableName());
        }
        return false;
    }
    
    private boolean isRowEvent(final AbstractWalEvent event) {
        return event instanceof WriteRowEvent
                || event instanceof UpdateRowEvent
                || event instanceof DeleteRowEvent;
    }
    
    private PlaceholderRecord createPlaceholderRecord(final AbstractWalEvent event) {
        return new PlaceholderRecord(new WalPosition(event.getLogSequenceNumber()));
    }
    
    private DataRecord handleWriteRowsEvent(final WriteRowEvent writeRowEvent) {
        DataRecord result = createDataRecord(writeRowEvent, writeRowEvent.getAfterRow().size());
        result.setType(IngestDataChangeType.INSERT);
        putColumnsIntoDataRecord(result, getPipelineTableMetaData(writeRowEvent.getTableName()), writeRowEvent.getAfterRow());
        return result;
    }
    
    private PipelineTableMetaData getPipelineTableMetaData(final String actualTableName) {
        return metaDataLoader.getTableMetaData(dumperConfig.getSchemaName(new ActualTableName(actualTableName)), actualTableName);
    }
    
    private DataRecord handleUpdateRowsEvent(final UpdateRowEvent updateRowEvent) {
        DataRecord result = createDataRecord(updateRowEvent, updateRowEvent.getAfterRow().size());
        result.setType(IngestDataChangeType.UPDATE);
        putColumnsIntoDataRecord(result, getPipelineTableMetaData(updateRowEvent.getTableName()), updateRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleDeleteRowsEvent(final DeleteRowEvent event) {
        // TODO completion columns
        DataRecord result = createDataRecord(event, event.getPrimaryKeys().size());
        result.setType(IngestDataChangeType.DELETE);
        // TODO Unique key may be a column within unique index
        List<String> primaryKeyColumns = getPipelineTableMetaData(event.getTableName()).getPrimaryKeyColumns();
        for (int i = 0; i < event.getPrimaryKeys().size(); i++) {
            result.addColumn(new Column(primaryKeyColumns.get(i), event.getPrimaryKeys().get(i), true, true));
        }
        return result;
    }
    
    private DataRecord createDataRecord(final AbstractRowEvent rowsEvent, final int columnCount) {
        DataRecord result = new DataRecord(new WalPosition(rowsEvent.getLogSequenceNumber()), columnCount);
        result.setTableName(dumperConfig.getLogicTableName(rowsEvent.getTableName()).getLowercase());
        return result;
    }
    
    private void putColumnsIntoDataRecord(final DataRecord dataRecord, final PipelineTableMetaData tableMetaData, final List<Object> values) {
        for (int i = 0, count = values.size(); i < count; i++) {
            boolean isUniqueKey = tableMetaData.isUniqueKey(i);
            Object uniqueKeyOldValue = isUniqueKey ? values.get(i) : null;
            Column column = new Column(tableMetaData.getColumnMetaData(i).getName(), uniqueKeyOldValue, values.get(i), true, isUniqueKey);
            dataRecord.addColumn(column);
        }
    }
}
