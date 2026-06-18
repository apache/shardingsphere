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
import java.time.LocalDateTime;
import java.util.Date;

/**
 * MySQL DATETIME binlog protocol value.
 * Stored value is in the format YYYYMMDDHHMMSS and can be easily extracted by repeatedly calculating the remainder of dividing the value by 100 and dividing the value by 100
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLDatetimeBinlogProtocolValue implements MySQLBinlogProtocolValue {
    
    @Override
    public Serializable read(final MySQLBinlogColumnDef columnDef, final MySQLPacketPayload payload) {
        long datetime = payload.readInt8();
        return 0L == datetime ? MySQLTimeValueUtils.DATETIME_OF_ZERO : readDateTime(datetime);
    }
    
    private Date readDateTime(final long datetime) {
        int date = (int) (datetime / 1000000L);
        int year = date / 10000;
        int month = (date % 10000) / 100;
        int day = date % 100;
        int time = (int) (datetime % 1000000L);
        int hour = time / 10000;
        int minute = (time % 10000) / 100;
        int second = time % 100;
        return Timestamp.valueOf(LocalDateTime.of(year, month, day, hour, minute, second));
    }
}
