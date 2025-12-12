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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * MySQL TIMESTAMP2 binlog protocol value.
 * Stored as a 4 byte UNIX timestamp (number of seconds since 00:00, Jan 1 1970 UTC) followed by the fractional second parts.
 * The number of decimals for the fractional part is stored in the table metadata as a one byte value.
 * The number of bytes that follow the 4 byte timestamp can be calculated with the following formula: (decimals + 1) / 2
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLTimestamp2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        int seconds = payload.getByteBuf().readInt();
        if (0 == seconds) {
            return MySQLTimeValueUtils.DATETIME_OF_ZERO;
        }
        int nanos = columnDef.getColumnMeta() > 0 ? new MySQLFractionalSeconds(columnDef.getColumnMeta(), payload).getNanos() : 0;
        Timestamp result = new Timestamp(seconds * 1000L);
        result.setNanos(nanos);
        return result;
    }
}
