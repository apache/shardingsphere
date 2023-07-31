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
import org.apache.shardingsphere.data.pipeline.api.metadata.ColumnName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;

import java.util.List;
import java.util.Set;

/**
 * WAL event converter.
 */
public final class WALEventConverter {
    
    private final DumperConfiguration dumperConfig;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    public WALEventConverter(final DumperConfiguration dumperConfig, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperConfig = dumperConfig;
        this.metaDataLoader = metaDataLoader;
    }
    
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
            return !dumperConfig.containsTable(rowEvent.getTableName());
        }
        return false;
    }
    
    private PlaceholderRecord createPlaceholderRecord(final AbstractWALEvent event) {
        return new PlaceholderRecord(new WALPosition(event.getLogSequenceNumber()));
    }
    
    private PipelineTableMetaData getPipelineTableMetaData(final String actualTableName) {
        return metaDataLoader.getTableMetaData(dumperConfig.getSchemaName(new ActualTableName(actualTableName)), actualTableName);
    }
    
    private DataRecord handleWriteRowEvent(final WriteRowEvent writeRowEvent, final PipelineTableMetaData tableMetaData) {
        DataRecord result = createDataRecord(IngestDataChangeType.INSERT, writeRowEvent, writeRowEvent.getAfterRow().size());
        putColumnsIntoDataRecord(result, tableMetaData, writeRowEvent.getTableName(), writeRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleUpdateRowEvent(final UpdateRowEvent updateRowEvent, final PipelineTableMetaData tableMetaData) {
        DataRecord result = createDataRecord(IngestDataChangeType.UPDATE, updateRowEvent, updateRowEvent.getAfterRow().size());
        String actualTableName = updateRowEvent.getTableName();
        putColumnsIntoDataRecord(result, tableMetaData, actualTableName, updateRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleDeleteRowEvent(final DeleteRowEvent event, final PipelineTableMetaData tableMetaData) {
        // TODO completion columns
        DataRecord result = createDataRecord(IngestDataChangeType.DELETE, event, event.getPrimaryKeys().size());
        // TODO Unique key may be a column within unique index
        List<String> primaryKeyColumns = tableMetaData.getPrimaryKeyColumns();
        for (int i = 0; i < event.getPrimaryKeys().size(); i++) {
            result.addColumn(new Column(primaryKeyColumns.get(i), event.getPrimaryKeys().get(i), null, true, true));
        }
        return result;
    }
    
    private DataRecord createDataRecord(final String type, final AbstractRowEvent rowsEvent, final int columnCount) {
        String tableName = dumperConfig.getLogicTableName(rowsEvent.getTableName()).getOriginal();
        DataRecord result = new DataRecord(type, tableName, new WALPosition(rowsEvent.getLogSequenceNumber()), columnCount);
        result.setCsn(rowsEvent.getCsn());
        return result;
    }
    
    private void putColumnsIntoDataRecord(final DataRecord dataRecord, final PipelineTableMetaData tableMetaData, final String actualTableName, final List<Object> values) {
        Set<ColumnName> columnNameSet = dumperConfig.getColumnNameSet(actualTableName).orElse(null);
        for (int i = 0, count = values.size(); i < count; i++) {
            PipelineColumnMetaData columnMetaData = tableMetaData.getColumnMetaData(i + 1);
            if (isColumnUnneeded(columnNameSet, columnMetaData.getName())) {
                continue;
            }
            boolean isUniqueKey = columnMetaData.isUniqueKey();
            Object uniqueKeyOldValue = isUniqueKey && IngestDataChangeType.UPDATE.equals(dataRecord.getType()) ? values.get(i) : null;
            Column column = new Column(columnMetaData.getName(), uniqueKeyOldValue, values.get(i), true, isUniqueKey);
            dataRecord.addColumn(column);
        }
    }
    
    private boolean isColumnUnneeded(final Set<ColumnName> columnNameSet, final String columnName) {
        return null != columnNameSet && !columnNameSet.contains(new ColumnName(columnName));
    }
}
