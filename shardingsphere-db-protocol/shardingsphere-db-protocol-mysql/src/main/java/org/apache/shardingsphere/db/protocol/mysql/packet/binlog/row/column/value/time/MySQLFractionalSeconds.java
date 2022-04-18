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

import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * Fractional seconds of MySQL time2 type.
 *
 * @see <a href="https://dev.mysql.com/doc/internals/en/date-and-time-data-type-representation.html">Date and Time Data Type Representation</a>
 */
public final class MySQLFractionalSeconds {

    private final int fraction;
    
    private final int fractionalSecondsPrecision;
    
    public MySQLFractionalSeconds(final int columnMeta, final MySQLPacketPayload payload) {
        fractionalSecondsPrecision = columnMeta;
        fraction = readFraction(payload);
    }
    
    private int readFraction(final MySQLPacketPayload payload) {
        switch (fractionalSecondsPrecision) {
            case 1:
            case 2:
                return payload.readInt1() * 10000;
            case 3:
            case 4:
                return payload.getByteBuf().readUnsignedShort() * 100;
            case 5:
            case 6:
                return payload.getByteBuf().readUnsignedMedium();
            default:
                return 0;
        }
    }
    
    @Override
    public String toString() {
        if (0 == fractionalSecondsPrecision) {
            return "";
        }
        StringBuilder result = new StringBuilder(Integer.toString(fraction));
        for (int i = result.length(); i < fractionalSecondsPrecision; i++) {
            result.append("0");
        }
        result.setLength(fractionalSecondsPrecision);
        return "." + result;
    }
}
