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
import java.time.LocalTime;

/**
 * TIME2 type value of MySQL binlog protocol.
 * Stored as 3-byte value The number of decimals for the fractional part is stored in the table metadata as a one byte value.
 * The number of bytes that follow the 3 byte time value can be calculated with the following formula: (decimals + 1) / 2
 *
 * <p>
 * TIME2 type applied after MySQL 5.6.4.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLTime2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        int time = payload.getByteBuf().readUnsignedMedium();
        if (0x800000 == time) {
            return MySQLTimeValueUtils.ZERO_OF_TIME;
        }
        MySQLFractionalSeconds fractionalSeconds = new MySQLFractionalSeconds(columnDef.getColumnMeta(), payload);
        int hour = (time >> 12) % (1 << 10);
        int minute = (time >> 6) % (1 << 6);
        int second = time % (1 << 6);
        return LocalTime.of(hour, minute, second).withNano(fractionalSeconds.getNanos());
    }
}
