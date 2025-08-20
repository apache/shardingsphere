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
import java.sql.Date;
import java.time.LocalDate;

/**
 * DATE type value of MySQL binlog protocol.
 * Stored as a 3 byte value where bits 1 to 5 store the day, bits 6 to 9 store the month and the remaining bits store the year.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLDateBinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        int date = payload.getByteBuf().readUnsignedMediumLE();
        int year = date / 16 / 32;
        int month = date / 32 % 16;
        int day = date % 32;
        return 0 == date ? MySQLTimeValueUtils.ZERO_OF_DATE : Date.valueOf(LocalDate.of(year, month, day));
    }
}
