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
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.AbstractMySQLBinlogEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLNullBitmap;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.LinkedList;
import java.util.List;

/**
 * MySQL binlog table map event packet.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/table-map-event.html">TABLE_MAP_EVENT</a>
 */
@Getter
public final class MySQLBinlogTableMapEventPacket extends AbstractMySQLBinlogEventPacket {
    
    private final long tableId;
    
    private final int flags;
    
    private final String schemaName;
    
    private final String tableName;
    
    private final int columnCount;
    
    private final List<MySQLBinlogColumnDef> columnDefs;
    
    private final MySQLNullBitmap nullBitMap;
    
    public MySQLBinlogTableMapEventPacket(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        super(binlogEventHeader);
        tableId = payload.readInt6();
        flags = payload.readInt2();
        schemaName = payload.readStringFix(payload.readInt1());
        payload.skipReserved(1);
        tableName = payload.readStringFix(payload.readInt1());
        payload.skipReserved(1);
        columnCount = (int) payload.readIntLenenc();
        columnDefs = new LinkedList<>();
        readColumnDefs(payload);
        readColumnMetaDefs(payload);
        nullBitMap = new MySQLNullBitmap(columnCount, payload);
    }
    
    private void readColumnDefs(final MySQLPacketPayload payload) {
        for (int i = 0; i < columnCount; i++) {
            columnDefs.add(new MySQLBinlogColumnDef(MySQLBinaryColumnType.valueOf(payload.readInt1())));
        }
    }
    
    private void readColumnMetaDefs(final MySQLPacketPayload payload) {
        payload.readIntLenenc();
        for (MySQLBinlogColumnDef each : columnDefs) {
            each.setColumnMeta(readColumnMetaDef(each.getColumnType(), payload));
        }
    }
    
    private int readColumnMetaDef(final MySQLBinaryColumnType columnType, final MySQLPacketPayload payload) {
        switch (columnType) {
            case MYSQL_TYPE_STRING:
            case MYSQL_TYPE_DECIMAL:
            case MYSQL_TYPE_NEWDECIMAL:
                return payload.getByteBuf().readUnsignedShort();
            case MYSQL_TYPE_BIT:
            case MYSQL_TYPE_VAR_STRING:
            case MYSQL_TYPE_VARCHAR:
            case MYSQL_TYPE_ENUM:
                return payload.readInt2();
            case MYSQL_TYPE_BLOB:
            case MYSQL_TYPE_TINY_BLOB:
            case MYSQL_TYPE_DOUBLE:
            case MYSQL_TYPE_FLOAT:
            case MYSQL_TYPE_TIME2:
            case MYSQL_TYPE_TIMESTAMP2:
            case MYSQL_TYPE_DATETIME2:
            case MySQL_TYPE_JSON:
                return payload.readInt1();
            default:
                return 0;
        }
    }
    
    @Override
    protected void writeEvent(final MySQLPacketPayload payload) {
        // TODO
    }
}
