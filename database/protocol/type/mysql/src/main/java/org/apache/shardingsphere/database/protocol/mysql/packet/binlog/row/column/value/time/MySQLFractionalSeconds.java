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

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Fractional seconds of MySQL time2 type.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/field__types_8h.html">field type</a>
 */
public final class MySQLFractionalSeconds {
    
    @Getter
    private final int nanos;
    
    private final int fractionalSecondsPrecision;
    
    public MySQLFractionalSeconds(final int columnMeta, final MySQLPacketPayload payload) {
        fractionalSecondsPrecision = columnMeta;
        nanos = convertFractionalSecondsToNanos(payload);
    }
    
    private int convertFractionalSecondsToNanos(final MySQLPacketPayload payload) {
        switch (fractionalSecondsPrecision) {
            case 1:
            case 2:
                return payload.readInt1() * 10000 * 1000;
            case 3:
            case 4:
                return payload.getByteBuf().readUnsignedShort() * 100 * 1000;
            case 5:
            case 6:
                return payload.getByteBuf().readUnsignedMedium() * 1000;
            default:
                return 0;
        }
    }
}
