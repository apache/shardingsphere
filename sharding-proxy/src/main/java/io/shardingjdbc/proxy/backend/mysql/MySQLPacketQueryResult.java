/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.backend.mysql;

import io.shardingjdbc.core.merger.QueryResult;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * MySQL packet query result.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MySQLPacketQueryResult implements QueryResult {
    
    private final int columnCount;
    
    private final Map<Integer, String> columnIndexAndLabelMap;
    
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    private final Iterator<DatabaseProtocolPacket> data;
    
    private TextResultSetRowPacket currentRow;
    
    public MySQLPacketQueryResult(final List<DatabaseProtocolPacket> packets) {
        Iterator<DatabaseProtocolPacket> packetIterator = packets.iterator();
        columnCount = Long.valueOf(((FieldCountPacket) packetIterator.next()).getColumnCount()).intValue();
        columnIndexAndLabelMap = new HashMap<>(columnCount, 1);
        columnLabelAndIndexMap = new HashMap<>(columnCount, 1);
        for (int i = 1; i <= columnCount; i++) {
            ColumnDefinition41Packet columnDefinition41Packet = (ColumnDefinition41Packet) packetIterator.next();
            columnIndexAndLabelMap.put(i, columnDefinition41Packet.getName());
            columnLabelAndIndexMap.put(columnDefinition41Packet.getName(), i);
        }
        packetIterator.next();
        data = packetIterator;
    }
    
    @Override
    public boolean next() {
        DatabaseProtocolPacket databaseProtocolPacket = data.next();
        if (databaseProtocolPacket instanceof TextResultSetRowPacket) {
            currentRow = (TextResultSetRowPacket) databaseProtocolPacket;
            return true;
        }
        return false;
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
