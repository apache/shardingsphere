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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;

/**
 * STRING type value of MySQL binlog protocol.
 */
public final class MySQLStringBinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        int type = columnDef.getColumnMeta() >> 8;
        int length = columnDef.getColumnMeta() & 0xff;
        // unpack type & length, see https://bugs.mysql.com/bug.php?id=37426.
        if ((type & 0x30) != 0x30) {
            length += ((type & 0x30) ^ 0x30) << 4;
            type |= 0x30;
        }
        switch (MySQLColumnType.valueOf(type)) {
            case MYSQL_TYPE_ENUM:
                return readEnumValue(length, payload);
            case MYSQL_TYPE_SET:
                return payload.getByteBuf().readByte();
            case MYSQL_TYPE_STRING:
                return payload.readStringFix(readActualLength(payload, length));
            default:
                throw new UnsupportedOperationException("");
        }
    }
    
    private int readActualLength(final MySQLPacketPayload payload, final int length) {
        if (length < 256) {
            return payload.getByteBuf().readUnsignedByte();
        }
        return payload.getByteBuf().readUnsignedShortLE();
    }
    
    private Serializable readEnumValue(final int meta, final MySQLPacketPayload payload) {
        switch (meta) {
            case 1:
                return payload.readInt1();
            case 2:
                return payload.readInt2();
            default:
                throw new UnsupportedOperationException("MySQL Enum meta in binlog only include value 1 or 2, but actual is " + meta);
        }
    }
}
