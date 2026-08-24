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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierNormalizeEngine;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

import java.util.List;

/**
 * WAL event converter.
 */
public final class WALEventConverter {
    
    private final IncrementalDumperContext dumperContext;
    
    private final PipelineTableMetaDataLoader metaDataLoader;
    
    private final IdentifierCasePolicy schemaIdentifierCasePolicy;
    
    /**
     * Create a WAL event converter.
     *
     * @param dumperContext incremental dumper context
     * @param metaDataLoader pipeline table metadata loader
     */
    public WALEventConverter(final IncrementalDumperContext dumperContext, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperContext = dumperContext;
        this.metaDataLoader = metaDataLoader;
        schemaIdentifierCasePolicy = IdentifierNormalizeEngine.resolvePolicy(
                dumperContext.getCommonContext().getDataSourceConfig().getDatabaseType(), null, IdentifierScope.SCHEMA);
    }
    
    /**
     * Convert WAL event to {@code Record}.
     *
     * @param event WAL event
     * @return record
     * @throws UnsupportedSQLOperationException unsupported SQL operation exception
     */
    public Record convert(final AbstractWALEvent event) {
        if (!(event instanceof AbstractRowEvent)) {
            return createPlaceholderRecord(event);
        }
        AbstractRowEvent rowEvent = (AbstractRowEvent) event;
        String actualTableName = rowEvent.getTableName();
        ShardingSphereIdentifier logicTableName = dumperContext.getCommonContext().getTableNameMapper().getLogicTableName(actualTableName);
        if (null == logicTableName) {
            return createPlaceholderRecord(event);
        }
        String expectedSchemaName = dumperContext.getCommonContext().getTableAndSchemaNameMapper().getSchemaName(logicTableName);
        if (isDifferentSchemaName(rowEvent, expectedSchemaName)) {
            return createPlaceholderRecord(event);
        }
        PipelineTableMetaData tableMetaData = metaDataLoader.getTableMetaData(expectedSchemaName, actualTableName);
        if (event instanceof WriteRowEvent) {
            return handleWriteRowEvent((WriteRowEvent) event, tableMetaData, logicTableName);
        }
        if (event instanceof UpdateRowEvent) {
            return handleUpdateRowEvent((UpdateRowEvent) event, tableMetaData, logicTableName);
        }
        if (event instanceof DeleteRowEvent) {
            return handleDeleteRowEvent((DeleteRowEvent) event, tableMetaData, logicTableName);
        }
        throw new UnsupportedSQLOperationException("");
    }
    
    private boolean isDifferentSchemaName(final AbstractRowEvent event, final String expectedSchemaName) {
        if (!isConcreteSchemaName(expectedSchemaName)) {
            return false;
        }
        String actualSchemaName = event.getSchemaName();
        if (!isConcreteSchemaName(actualSchemaName)) {
            return false;
        }
        // The mapper stores a canonical schema name, while the WAL decoders preserve the event identifier's quote characters.
        QuoteCharacter quoteCharacter = QuoteCharacter.getQuoteCharacter(actualSchemaName);
        return !schemaIdentifierCasePolicy.matches(expectedSchemaName, quoteCharacter.unwrap(actualSchemaName), quoteCharacter);
    }
    
    private static boolean isConcreteSchemaName(final String schemaName) {
        return null != schemaName && !schemaName.isEmpty() && !"*".equals(schemaName);
    }
    
    private PlaceholderRecord createPlaceholderRecord(final AbstractWALEvent event) {
        return new PlaceholderRecord(new WALPosition(event.getLogSequenceNumber()));
    }
    
    private DataRecord handleWriteRowEvent(final WriteRowEvent writeRowEvent, final PipelineTableMetaData tableMetaData, final ShardingSphereIdentifier logicTableName) {
        DataRecord result = createDataRecord(PipelineSQLOperationType.INSERT, writeRowEvent, logicTableName, writeRowEvent.getAfterRow().size());
        putColumnsIntoDataRecord(result, tableMetaData, writeRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleUpdateRowEvent(final UpdateRowEvent updateRowEvent, final PipelineTableMetaData tableMetaData, final ShardingSphereIdentifier logicTableName) {
        DataRecord result = createDataRecord(PipelineSQLOperationType.UPDATE, updateRowEvent, logicTableName, updateRowEvent.getAfterRow().size());
        putColumnsIntoDataRecord(result, tableMetaData, updateRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleDeleteRowEvent(final DeleteRowEvent event, final PipelineTableMetaData tableMetaData, final ShardingSphereIdentifier logicTableName) {
        // TODO completion columns
        DataRecord result = createDataRecord(PipelineSQLOperationType.DELETE, event, logicTableName, event.getPrimaryKeys().size());
        // TODO Unique key may be a column within unique index
        List<String> primaryKeyColumns = tableMetaData.getPrimaryKeyColumns();
        for (int i = 0; i < event.getPrimaryKeys().size(); i++) {
            result.addColumn(new NormalColumn(primaryKeyColumns.get(i), event.getPrimaryKeys().get(i), null, true, true));
        }
        return result;
    }
    
    private DataRecord createDataRecord(final PipelineSQLOperationType type, final AbstractRowEvent rowsEvent, final ShardingSphereIdentifier logicTableName, final int columnCount) {
        DataRecord result = new DataRecord(type, rowsEvent.getSchemaName(), logicTableName.toString(), new WALPosition(rowsEvent.getLogSequenceNumber()), columnCount);
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
