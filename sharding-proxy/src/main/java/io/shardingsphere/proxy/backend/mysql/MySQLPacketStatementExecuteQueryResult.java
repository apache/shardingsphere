/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend.mysql;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.backend.common.ResultList;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.BinaryResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MySQL packet statement execute query result.
 *
 * @author zhangyonglun
 */
@RequiredArgsConstructor
@Setter
public final class MySQLPacketStatementExecuteQueryResult implements QueryResult {
    
    private final int columnCount;
    
    private final List<ColumnType> columnTypes;
    
    private final Map<Integer, String> columnIndexAndLabelMap;
    
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    private ResultSet resultSet;
    
    private ResultList resultList;
    
    private int currentSequenceId;
    
    private BinaryResultSetRowPacket currentRow;
    
    public MySQLPacketStatementExecuteQueryResult(final CommandResponsePackets packets, final List<ColumnType> columnTypes) {
        Iterator<DatabaseProtocolPacket> packetIterator = packets.getDatabaseProtocolPackets().iterator();
        columnCount = ((FieldCountPacket) packetIterator.next()).getColumnCount();
        columnIndexAndLabelMap = new HashMap<>(columnCount, 1);
        columnLabelAndIndexMap = new HashMap<>(columnCount, 1);
        for (int i = 1; i <= columnCount; i++) {
            ColumnDefinition41Packet columnDefinition41Packet = (ColumnDefinition41Packet) packetIterator.next();
            columnIndexAndLabelMap.put(i, columnDefinition41Packet.getName());
            columnLabelAndIndexMap.put(columnDefinition41Packet.getName(), i);
        }
        this.columnTypes = columnTypes;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (ProxyMode.MEMORY_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            return nextForStreamResultSet();
        } else {
            return nextForNoneStreamResultList();
        }
    }
    
    private boolean nextForStreamResultSet() throws SQLException {
        if (resultSet.next()) {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(resultSet.getObject(i));
            }
            currentRow = new BinaryResultSetRowPacket(++currentSequenceId, columnCount, data, columnTypes);
            return true;
        }
        return false;
    }
    
    private boolean nextForNoneStreamResultList() {
        if (!resultList.hasNext()) {
            return false;
        }
        List<Object> data = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            if (resultList.hasNext()) {
                data.add(resultList.next());
            }
        }
        currentRow = new BinaryResultSetRowPacket(++currentSequenceId, columnCount, data, columnTypes);
        return true;
    }
    
    @Override
    public int getColumnCount() {
        return columnCount;
    }
    
    @Override
    public String getColumnLabel(final int columnIndex) {
        return columnIndexAndLabelMap.get(columnIndex);
    }
    
    @Override
    public Object getValue(final int columnIndex, final Class<?> type) {
        return currentRow.getData().get(columnIndex - 1);
    }
    
    @Override
    public Object getValue(final String columnLabel, final Class<?> type) {
        return currentRow.getData().get(columnLabelAndIndexMap.get(columnLabel));
    }
    
    @Override
    public Object getCalendarValue(final int columnIndex, final Class<?> type, final Calendar calendar) {
        return currentRow.getData().get(columnIndex - 1);
    }
    
    @Override
    public Object getCalendarValue(final String columnLabel, final Class<?> type, final Calendar calendar) {
        return currentRow.getData().get(columnLabelAndIndexMap.get(columnLabel));
    }
    
    @Override
    public InputStream getInputStream(final int columnIndex, final String type) {
        return (InputStream) currentRow.getData().get(columnIndex - 1);
    }
    
    @Override
    public InputStream getInputStream(final String columnLabel, final String type) {
        return (InputStream) currentRow.getData().get(columnLabelAndIndexMap.get(columnLabel));
    }
    
    // TODO
    @Override
    public boolean wasNull() {
        return false;
    }
}
