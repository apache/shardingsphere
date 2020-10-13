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

package org.apache.shardingsphere.scaling.postgresql.wal;

import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceFactory;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.scaling.core.metadata.MetaDataManager;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.metadata.model.physical.model.table.TableMetaData;

import java.util.List;

/**
 * Convert wal event to {@code Record}.
 */
public final class WalEventConverter {
    
    private final DumperConfiguration dumperConfig;
    
    private final MetaDataManager metaDataManager;
    
    public WalEventConverter(final DumperConfiguration dumperConfig) {
        this.dumperConfig = dumperConfig;
        metaDataManager = new MetaDataManager(new DataSourceFactory().newInstance(dumperConfig.getDataSourceConfiguration()));
    }
    
    /**
     * Convert wal event to {@code Record}.
     *
     * @param event of wal
     * @return record
     */
    public Record convert(final AbstractWalEvent event) {
        JdbcUri uri = new JdbcUri(((JDBCScalingDataSourceConfiguration) dumperConfig.getDataSourceConfiguration()).getJdbcUrl());
        if (filter(uri.getDatabase(), event)) {
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
    
    private boolean filter(final String database, final AbstractWalEvent event) {
        if (isRowEvent(event)) {
            AbstractRowEvent rowEvent = (AbstractRowEvent) event;
            return !rowEvent.getSchemaName().equals(database) || !dumperConfig.getTableNameMap().containsKey(rowEvent.getTableName());
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
        result.setType(ScalingConstant.INSERT);
        putColumnsIntoDataRecord(result, metaDataManager.getTableMetaData(writeRowEvent.getTableName()), writeRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleUpdateRowsEvent(final UpdateRowEvent updateRowEvent) {
        DataRecord result = createDataRecord(updateRowEvent, updateRowEvent.getAfterRow().size());
        result.setType(ScalingConstant.UPDATE);
        putColumnsIntoDataRecord(result, metaDataManager.getTableMetaData(updateRowEvent.getTableName()), updateRowEvent.getAfterRow());
        return result;
    }
    
    private DataRecord handleDeleteRowsEvent(final DeleteRowEvent event) {
        //TODO completion columns
        DataRecord result = createDataRecord(event, event.getPrimaryKeys().size());
        result.setType(ScalingConstant.DELETE);
        List<String> primaryKeyColumns = metaDataManager.getTableMetaData(event.getTableName()).getPrimaryKeyColumns();
        for (int i = 0; i < event.getPrimaryKeys().size(); i++) {
            result.addColumn(new Column(primaryKeyColumns.get(i), event.getPrimaryKeys().get(i), true, true));
        }
        return result;
    }
    
    private DataRecord createDataRecord(final AbstractRowEvent rowsEvent, final int columnCount) {
        DataRecord result = new DataRecord(new WalPosition(rowsEvent.getLogSequenceNumber()), columnCount);
        result.setTableName(dumperConfig.getTableNameMap().get(rowsEvent.getTableName()));
        return result;
    }
    
    private void putColumnsIntoDataRecord(final DataRecord dataRecord, final TableMetaData tableMetaData, final List<Object> values) {
        for (int i = 0; i < values.size(); i++) {
            dataRecord.addColumn(new Column(tableMetaData.getColumnMetaData(i).getName(), values.get(i), true, tableMetaData.isPrimaryKey(i)));
        }
    }
}
