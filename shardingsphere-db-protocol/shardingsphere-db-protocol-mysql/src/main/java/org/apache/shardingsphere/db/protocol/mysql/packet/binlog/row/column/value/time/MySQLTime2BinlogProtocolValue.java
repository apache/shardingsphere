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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.time;

import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.MySQLBinlogProtocolValue;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.io.Serializable;

/**
 * TIME2 type value of MySQL binlog protocol.
 *
 * <p>
 *     TIME2 type applied after MySQL 5.6.4.
 * </p>
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/date-and-time-data-type-representation.html">Date and Time Data Type Representation</a>
 */
public final class MySQLTime2BinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        int time = payload.getByteBuf().readUnsignedMedium();
        if (0x800000 == time) {
            return MySQLTimeValueUtil.ZERO_OF_TIME;
        }
        MySQLFractionalSeconds fractionalSeconds = new MySQLFractionalSeconds(columnDef.getColumnMeta(), payload);
        return String.format("%02d:%02d:%02d%s", (time >> 12) % (1 << 10), (time >> 6) % (1 << 6), time % (1 << 6), fractionalSeconds);
    }
}
