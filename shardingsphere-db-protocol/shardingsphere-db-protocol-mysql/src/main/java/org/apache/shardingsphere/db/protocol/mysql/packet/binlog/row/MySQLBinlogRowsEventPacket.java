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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.AbstractMySQLBinlogEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLNullBitmap;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL binlog rows event packet.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/rows-event.html">ROWS_EVENT</a>
 */
@Getter
public final class MySQLBinlogRowsEventPacket extends AbstractMySQLBinlogEventPacket {
    
    private final long tableId;
    
    private final int flags;
    
    private final int columnNumber;
    
    private final MySQLNullBitmap columnsPresentBitmap;
    
    private final MySQLNullBitmap columnsPresentBitmap2;
    
    private final List<Serializable[]> rows = new LinkedList<>();
    
    private final List<Serializable[]> rows2 = new LinkedList<>();
    
    public MySQLBinlogRowsEventPacket(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        super(binlogEventHeader);
        tableId = payload.readInt6();
        flags = payload.readInt2();
        skipExtraData(payload);
        columnNumber = (int) payload.readIntLenenc();
        columnsPresentBitmap = new MySQLNullBitmap(columnNumber, payload);
        columnsPresentBitmap2 = readUpdateColumnsPresentBitmap(payload);
    }
    
    private void skipExtraData(final MySQLPacketPayload payload) {
        if (isRowsEventVersion2(getBinlogEventHeader().getEventType())) {
            int extraDataLength = payload.readInt2() - 2;
            payload.skipReserved(extraDataLength);
        }
    }
    
    private boolean isRowsEventVersion2(final int eventType) {
        return MySQLBinlogEventType.WRITE_ROWS_EVENTv2.getValue() == eventType || MySQLBinlogEventType.UPDATE_ROWS_EVENTv2.getValue() == eventType
            || MySQLBinlogEventType.DELETE_ROWS_EVENTv2.getValue() == eventType;
    }
    
    private MySQLNullBitmap readUpdateColumnsPresentBitmap(final MySQLPacketPayload payload) {
        return isUpdateRowsEvent(getBinlogEventHeader().getEventType()) ? new MySQLNullBitmap(columnNumber, payload) : null;
    }
    
    private boolean isUpdateRowsEvent(final int eventType) {
        return MySQLBinlogEventType.UPDATE_ROWS_EVENTv2.getValue() == eventType || MySQLBinlogEventType.UPDATE_ROWS_EVENTv1.getValue() == eventType;
    }
    
    /**
     * Read rows in binlog.
     *
     * @param tableMapEventPacket TABLE_MAP_EVENT packet before this ROWS_EVENT
     * @param payload ROWS_EVENT packet payload
     */
    public void readRows(final MySQLBinlogTableMapEventPacket tableMapEventPacket, final MySQLPacketPayload payload) {
        List<MySQLBinlogColumnDef> columnDefs = tableMapEventPacket.getColumnDefs();
        while (hasNextRow(payload)) {
            rows.add(readRow(columnDefs, payload));
            if (isUpdateRowsEvent(getBinlogEventHeader().getEventType())) {
                rows2.add(readRow(columnDefs, payload));
            }
        }
    }
    
    private boolean hasNextRow(final MySQLPacketPayload payload) {
        return payload.getByteBuf().isReadable();
    }
    
    private Serializable[] readRow(final List<MySQLBinlogColumnDef> columnDefs, final MySQLPacketPayload payload) {
        MySQLNullBitmap nullBitmap = new MySQLNullBitmap(columnNumber, payload);
        Serializable[] result = new Serializable[columnNumber];
        for (int i = 0; i < columnNumber; i++) {
            MySQLBinlogColumnDef columnDef = columnDefs.get(i);
            result[i] = nullBitmap.isNullParameter(i) ? null : MySQLBinlogProtocolValueFactory.getBinlogProtocolValue(columnDef.getColumnType()).read(columnDef, payload);
        }
        return result;
    }
    
    @Override
    protected void writeEvent(final MySQLPacketPayload payload) {
        // TODO
    }
}
