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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;

import java.io.Serializable;

/**
 * JSON type value of MySQL binlog protocol.
 *
 * <p>
 *     There are no detail document about JSON type in MySQL replication.
 *     Also no detail document in MariaDB.
 *     Decoding implementation is referred to MySQL source code.
 * </p>
 *
 * @see <a href="https://github.com/mysql/mysql-server/blob/5.7/sql/json_binary.h">json_binary</a>
 */
public final class MySQLJsonBinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        ByteBuf newlyByteBuf = payload.getByteBuf().readBytes(readLengthFromMeta(columnDef.getColumnMeta(), payload));
        try {
            return MySQLJsonValueDecoder.decode(newlyByteBuf);
        } finally {
            newlyByteBuf.release();
        }
    }
    
    private int readLengthFromMeta(final int columnMeta, final MySQLPacketPayload payload) {
        switch (columnMeta) {
            case 1:
                return payload.getByteBuf().readUnsignedByte();
            case 2:
                return payload.getByteBuf().readUnsignedShortLE();
            case 3:
                return payload.getByteBuf().readUnsignedMediumLE();
            case 4:
                return payload.readInt4();
            default:
                throw new UnsupportedSQLOperationException(String.format("MySQL JSON type meta in binlog should be range 1 to 4, but actual value is: %s", columnMeta));
        }
    }
}
