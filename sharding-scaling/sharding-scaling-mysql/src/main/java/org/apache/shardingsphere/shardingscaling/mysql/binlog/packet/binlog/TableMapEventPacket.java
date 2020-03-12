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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * Table map event.
 *
 * <p>
 *     https://dev.mysql.com/doc/internals/en/table-map-event.html
 *
 *     Refactor by extends {@link org.apache.shardingsphere.database.protocol.mysql.packet.command.MySQLCommandPacket}.
 * </p>
 */
@Getter
public final class TableMapEventPacket {
    
    private long tableId;
    
    private int flags;
    
    private String schemaName;
    
    private String tableName;
    
    private ColumnDef[] columnDefs;
    
    /**
     * Parse post header.
     *
     * @param in byte buff
     */
    public void parsePostHeader(final ByteBuf in) {
        tableId = DataTypesCodec.readUnsignedInt6LE(in);
        flags = DataTypesCodec.readUnsignedInt2LE(in);
    }
    
    /**
     * Parse payload.
     *
     * @param in byte buffer
     */
    public void parsePayload(final ByteBuf in) {
        schemaName = DataTypesCodec.readFixedLengthString(DataTypesCodec.readUnsignedInt1(in), in);
        DataTypesCodec.readNul(in);
        tableName = DataTypesCodec.readFixedLengthString(DataTypesCodec.readUnsignedInt1(in), in);
        DataTypesCodec.readNul(in);
        long columnCount = DataTypesCodec.readLengthCodedIntLE(in);
        initColumnDefs((int) columnCount);
        decodeColumnType(in);
        long columnMetaDefDataLength = DataTypesCodec.readLengthCodedIntLE(in);
        decodeColumnMeta(in);
        // skip null bitmap
        DataTypesCodec.readBitmap((int) columnCount, in);
    }
    
    private void initColumnDefs(final int columnCount) {
        columnDefs = new ColumnDef[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnDefs[i] = new ColumnDef();
        }
    }
    
    private void decodeColumnType(final ByteBuf in) {
        for (ColumnDef columnDef : columnDefs) {
            columnDef.setType(DataTypesCodec.readUnsignedInt1(in));
        }
    }
    
    private void decodeColumnMeta(final ByteBuf in) {
        for (ColumnDef columnDef : columnDefs) {
            switch (columnDef.getType()) {
                case ColumnTypes.MYSQL_TYPE_TINY_BLOB:
                case ColumnTypes.MYSQL_TYPE_BLOB:
                case ColumnTypes.MYSQL_TYPE_MEDIUM_BLOB:
                case ColumnTypes.MYSQL_TYPE_LONG_BLOB:
                case ColumnTypes.MYSQL_TYPE_DOUBLE:
                case ColumnTypes.MYSQL_TYPE_FLOAT:
                case ColumnTypes.MYSQL_TYPE_GEOMETRY:
                case ColumnTypes.MYSQL_TYPE_JSON:
                case ColumnTypes.MYSQL_TYPE_TIME2:
                case ColumnTypes.MYSQL_TYPE_DATETIME2:
                case ColumnTypes.MYSQL_TYPE_TIMESTAMP2:
                    columnDef.setMeta(DataTypesCodec.readUnsignedInt1(in));
                    break;
                case ColumnTypes.MYSQL_TYPE_STRING:
                case ColumnTypes.MYSQL_TYPE_NEWDECIMAL:
                    columnDef.setMeta(DataTypesCodec.readUnsignedInt2BE(in));
                    break;
                case ColumnTypes.MYSQL_TYPE_BIT:
                case ColumnTypes.MYSQL_TYPE_VARCHAR:
                    columnDef.setMeta(DataTypesCodec.readUnsignedInt2LE(in));
                    break;
                default:
                    columnDef.setMeta(0);
                    break;
            }
        }
    }
}
