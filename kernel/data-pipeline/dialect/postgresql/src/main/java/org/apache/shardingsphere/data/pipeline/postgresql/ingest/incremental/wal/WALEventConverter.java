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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.core.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.List;

/**
 * WAL event converter.
 */
@RequiredArgsConstructor
public final class WALEventConverter {
    
    private final IncrementalDumperContext dumperContext;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    /**
     * Convert WAL event to {@code Record}.
     *
     * @param event WAL event
     * @return record
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    public Record convert(final AbstractWALEvent event) {
        if (filter(event)) {
            return createPlaceholderRecord(event);
        }
        if (!(event instanceof AbstractRowEvent)) {
            return createPlaceholderRecord(event);
        }
        PipelineTableMetaData tableMetaData = getPipelineTableMetaData(((AbstractRowEvent) event).getTableName());
        if (event instanceof WriteRowEvent) {
            return handleWriteRowEvent((WriteRowEvent) event, tableMetaData);
        }
        if (event instanceof UpdateRowEvent) {
            return handleUpdateRowEvent((UpdateRowEvent) event, tableMetaData);
        }
        if (event instanceof DeleteRowEvent) {
            return handleDeleteRowEvent((DeleteRowEvent) event, tableMetaData);
        }
        throw new UnsupportedSQLOperationException("");
    }
    
    private boolean filter(final AbstractWALEvent event) {
        if (event instanceof AbstractRowEvent) {
            AbstractRowEvent rowEvent = (AbstractRowEvent) event;
            return !dumperContext.getCommonContext().getTableNameMapper().containsTable(rowEvent.getTableName());
        }
        return false;
    }
    
    private PlaceholderRecord createPlaceholderRecord(final AbstractWALEvent event) {
        return new PlaceholderRecord(new WALPosition(event.getLogSequenceNumber()));
    }
    
    private PipelineTableMetaData getPipelineTableMetaData(final String actualTableName) {
        ShardingSphereIdentifier logicTableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(actualTableName);
        return metaDataLoader.getTableMetaData(dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(logicTableName), actualTableName);
    }
    
    private DataRecord handleWriteRowEvent(final WriteRowEvent writeRowEvent, final PipelineTableMetaData tableMetaData) {
        DataRecord result = createDataRecord(PipelineSQLOperationType.INSERT, writeRowEvent, writeRowEvent.getAfterRow().size());
        putColumnsIntoDataRecord(result, tableMetaData, writeRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleUpdateRowEvent(final UpdateRowEvent updateRowEvent, final PipelineTableMetaData tableMetaData) {
        DataRecord result = createDataRecord(PipelineSQLOperationType.UPDATE, updateRowEvent, updateRowEvent.getAfterRow().size());
        putColumnsIntoDataRecord(result, tableMetaData, updateRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleDeleteRowEvent(final DeleteRowEvent event, final PipelineTableMetaData tableMetaData) {
        // TODO completion columns
        DataRecord result = createDataRecord(PipelineSQLOperationType.DELETE, event, event.getPrimaryKeys().size());
        // TODO Unique key may be a column within unique index
        List<String> primaryKeyColumns = tableMetaData.getPrimaryKeyColumns();
        for (int i = 0; i < event.getPrimaryKeys().size(); i++) {
            result.addColumn(new NormalColumn(primaryKeyColumns.get(i), event.getPrimaryKeys().get(i), null, true, true));
        }
        return result;
    }
    
    private DataRecord createDataRecord(final PipelineSQLOperationType type, final AbstractRowEvent rowsEvent, final int columnCount) {
        String tableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(rowsEvent.getTableName()).toString();
        DataRecord result = new DataRecord(type, rowsEvent.getSchemaName(), tableName, new WALPosition(rowsEvent.getLogSequenceNumber()), columnCount);
        result.setActualTableName(rowsEvent.getTableName());
        result.setCsn(rowsEvent.getCsn());
        return result;
    }
    
    private void putColumnsIntoDataRecord(final DataRecord dataRecord, final PipelineTableMetaData tableMetaData, final List<Object> values) {
        for (int i = 0, count = values.size(); i < count; i++) {
            PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
            boolean isUniqueKey = columnMetaData.isUniqueKey();
            Object uniqueKeyOldValue = isUniqueKey && PipelineSQLOperationType.UPDATE == dataRecord.getType() ? values.get(i) : null;
            Column column = new NormalColumn(columnMetaData.getName(), uniqueKeyOldValue, values.get(i), true, isUniqueKey);
            dataRecord.addColumn(column);
        }
    }
}
