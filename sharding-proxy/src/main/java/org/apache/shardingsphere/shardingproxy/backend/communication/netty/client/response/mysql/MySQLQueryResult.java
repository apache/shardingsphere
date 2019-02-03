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

package org.apache.shardingsphere.shardingproxy.backend.communication.netty.client.response.mysql;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.CommandResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.MySQLTextResultSetRowPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLEofPacket;

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    
    private final List<MySQLColumnDefinition41Packet> columnDefinitions;
    
    private final BlockingQueue<MySQLPacket> resultSet;
    
    @Getter
    private int currentSequenceId;
    
    private MySQLTextResultSetRowPacket currentRow;
    
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
    
    public MySQLQueryResult(final MySQLPacketPayload payload) {
        MySQLFieldCountPacket mySQLFieldCountPacket = new MySQLFieldCountPacket(payload);
        commandResponsePackets = new CommandResponsePackets(mySQLFieldCountPacket);
        columnCount = mySQLFieldCountPacket.getColumnCount();
        columnIndexAndLabelMap = new HashMap<>(mySQLFieldCountPacket.getColumnCount(), 1);
        columnLabelAndIndexMap = new HashMap<>(mySQLFieldCountPacket.getColumnCount(), 1);
        columnDefinitions = Lists.newArrayListWithCapacity(mySQLFieldCountPacket.getColumnCount());
        currentSequenceId = mySQLFieldCountPacket.getSequenceId();
        resultSet = new LinkedBlockingQueue<>();
    }
    
    /**
     * Set generic response to command response packets.
     * 
     * @param mysqlPacket MySQL packet
     */
    public void setGenericResponse(final MySQLPacket mysqlPacket) {
        commandResponsePackets.getPackets().add(mysqlPacket);
    }
    
    /**
     * Whether query result is needed to add column definition.
     * 
     * @return whether the column count is larger than column definitions's size
     */
    public boolean needColumnDefinition() {
        return columnCount > columnDefinitions.size();
    }
    
    /**
     * Add column definition.
     * 
     * @param columnDefinition column definition
     */
    public void addColumnDefinition(final MySQLColumnDefinition41Packet columnDefinition) {
        commandResponsePackets.getPackets().add(columnDefinition);
        columnDefinitions.add(columnDefinition);
        columnIndexAndLabelMap.put(columnDefinitions.indexOf(columnDefinition) + 1, columnDefinition.getName());
        columnLabelAndIndexMap.put(columnDefinition.getName(), columnDefinitions.indexOf(columnDefinition) + 1);
        currentSequenceId++;
    }
    
    /**
     * Add text result set row.
     * 
     * @param textResultSetRow text result set row
     */
    public void addTextResultSetRow(final MySQLTextResultSetRowPacket textResultSetRow) {
        put(textResultSetRow);
    }
    
    /**
     * Set column finished.
     * @param mySQLEofPacket eof packet
     */
    public void setColumnFinished(final MySQLEofPacket mySQLEofPacket) {
        commandResponsePackets.getPackets().add(mySQLEofPacket);
        currentSequenceId++;
        columnFinished = true;
    }
    
    /**
     * Set row finished.
     * @param mySQLEofPacket eof packet
     */
    public void setRowFinished(final MySQLEofPacket mySQLEofPacket) {
        put(mySQLEofPacket);
    }
    
    private void put(final MySQLPacket mysqlPacket) {
        try {
            resultSet.put(mysqlPacket);
        } catch (final InterruptedException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    @Override
    public boolean next() {
        try {
            MySQLPacket mysqlPacket = resultSet.take();
            currentRow = (mysqlPacket instanceof MySQLTextResultSetRowPacket) ? (MySQLTextResultSetRowPacket) mysqlPacket : null;
            return null != currentRow;
        } catch (final InterruptedException ex) {
            log.error(ex.getMessage(), ex);
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
