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

package org.apache.shardingsphere.shardingscaling.postgresql.wal;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingscaling.core.config.JdbcDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.shardingscaling.postgresql.WalPosition;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.WriteRowEvent;

/**
 * Convert wal event to {@code Record}.
 *
 * @author avalon566
 */
@RequiredArgsConstructor
public final class WalEventConverter {
    
    private final RdbmsConfiguration rdbmsConfiguration;
    
    /**
     * Convert wal event to {@code Record}.
     *
     * @param event of wal
     * @return record
     */
    public Record convert(final AbstractWalEvent event) {
        final JdbcUri uri = new JdbcUri(((JdbcDataSourceConfiguration) rdbmsConfiguration.getDataSourceConfiguration()).getJdbcUrl());
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
        throw new UnsupportedOperationException();
    }
    
    private boolean filter(final String database, final AbstractWalEvent event) {
        if (isRowEvent(event)) {
            AbstractRowEvent rowEvent = (AbstractRowEvent) event;
            return !rowEvent.getSchemaName().equals(database) || !rdbmsConfiguration.getTableNameMap().containsKey(rowEvent.getTableName());
        }
        return false;
    }
    
    private boolean isRowEvent(final AbstractWalEvent event) {
        return event instanceof WriteRowEvent
                || event instanceof UpdateRowEvent
                || event instanceof DeleteRowEvent;
    }
    
    private DataRecord handleWriteRowsEvent(final WriteRowEvent event) {
        DataRecord record = createDataRecord(event, event.getAfterRows().size());
        record.setType("insert");
        for (Object eachColumnValue : event.getAfterRows()) {
            record.addColumn(new Column(eachColumnValue, true));
        }
        return record;
    }
    
    private DataRecord handleUpdateRowsEvent(final UpdateRowEvent event) {
        DataRecord record = createDataRecord(event, event.getAfterRows().size());
        record.setType("update");
        for (Object eachColumnValue : event.getAfterRows()) {
            record.addColumn(new Column(eachColumnValue, true));
        }
        return record;
    }
    
    private DataRecord handleDeleteRowsEvent(final DeleteRowEvent event) {
        //TODO completion columns
        DataRecord record = createDataRecord(event, event.getPrimaryKeyRows().size());
        record.setType("delete");
        for (Object eachColumnValue : event.getPrimaryKeyRows()) {
            record.addColumn(new Column(eachColumnValue, true));
        }
        return record;
    }
    
    private DataRecord createDataRecord(final AbstractRowEvent rowsEvent, final int columnCount) {
        DataRecord result = new DataRecord(new WalPosition(rowsEvent.getLogSequenceNumber()), columnCount);
        result.setTableName(rdbmsConfiguration.getTableNameMap().get(rowsEvent.getTableName()));
        return result;
    }

    private PlaceholderRecord createPlaceholderRecord(final AbstractWalEvent event) {
        return new PlaceholderRecord(new WalPosition(event.getLogSequenceNumber()));
    }
}
