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

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.common.collect.Lists;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacket;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * MySQL packet query result.
 *
 * @author wangkai
 * @author linjiaqi
 */

@Slf4j
public final class MySQLQueryResult implements QueryResult {
    @Getter
    private final CommandResponsePackets commandResponsePackets;
    
    private final int columnCount;
    
    private final Map<Integer, String> columnIndexAndLabelMap;
    
    private final Map<String, Integer> columnLabelAndIndexMap;
    
    private final List<ColumnDefinition41Packet> columnDefinitions;
    
    private final BlockingQueue<MySQLPacket> resultSet;
    
    @Getter
    private int currentSequenceId;
    
    private TextResultSetRowPacket currentRow;
    
    @Getter
    private boolean columnFinished;
    
    public MySQLQueryResult() {
        commandResponsePackets = new CommandResponsePackets();
        columnCount = 0;
        columnIndexAndLabelMap = null;
        columnLabelAndIndexMap = null;
        columnDefinitions = null;
        resultSet = null;
    }
    
    public MySQLQueryResult(final MySQLPacketPayload mysqlPacketPayload) {
        FieldCountPacket fieldCountPacket = new FieldCountPacket(mysqlPacketPayload);
        commandResponsePackets = new CommandResponsePackets(fieldCountPacket);
        columnCount = fieldCountPacket.getColumnCount();
        columnIndexAndLabelMap = new HashMap<>(fieldCountPacket.getColumnCount(), 1);
        columnLabelAndIndexMap = new HashMap<>(fieldCountPacket.getColumnCount(), 1);
        columnDefinitions = Lists.newArrayListWithCapacity(fieldCountPacket.getColumnCount());
        currentSequenceId = fieldCountPacket.getSequenceId();
        resultSet = new LinkedBlockingQueue<>();
    }
    
    /**
     * Set GenericResponse to the CommandResponsePackets.
     * @param mysqlPacket mysqlPacket
     */
    public void setGenericResponse(final MySQLPacket mysqlPacket) {
        commandResponsePackets.addPacket(mysqlPacket);
    }
    
    /**
     * Whether the QueryResult is needed to add ColumnDefinition.
     * @return whether the columnCount is larger than columnDefinitions's size
     */
    public boolean needColumnDefinition() {
        return columnCount > columnDefinitions.size();
    }
    
    /**
     * Add ColumnDefinition to the QueryResult.
     * @param columnDefinition columnDefinition
     */
    public void addColumnDefinition(final ColumnDefinition41Packet columnDefinition) {
        commandResponsePackets.addPacket(columnDefinition);
        columnDefinitions.add(columnDefinition);
        columnIndexAndLabelMap.put(columnDefinitions.indexOf(columnDefinition) + 1, columnDefinition.getName());
        columnLabelAndIndexMap.put(columnDefinition.getName(), columnDefinitions.indexOf(columnDefinition) + 1);
        currentSequenceId++;
    }
    
    /**
     * Add TextResultSetRow to the QueryResult.
     * @param textResultSetRow textResultSetRow
     */
    public void addTextResultSetRow(final TextResultSetRowPacket textResultSetRow) {
        put(textResultSetRow);
    }
    
    /**
     * Set Column Finished.
     * @param eofPacket eofPacket
     */
    public void setColumnFinished(final EofPacket eofPacket) {
        commandResponsePackets.addPacket(eofPacket);
        currentSequenceId++;
        columnFinished = true;
    }
    
    /**
     * Set Row Finished.
     * @param eofPacket eofPacket
     */
    public void setRowFinished(final EofPacket eofPacket) {
        put(eofPacket);
    }
    
    private void put(final MySQLPacket mysqlPacket) {
        try {
            resultSet.put(mysqlPacket);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean next() {
        try {
            MySQLPacket mysqlPacket = resultSet.take();
            currentRow = (mysqlPacket instanceof TextResultSetRowPacket) ? (TextResultSetRowPacket) mysqlPacket : null;
            return currentRow == null ? false : true;
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
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
