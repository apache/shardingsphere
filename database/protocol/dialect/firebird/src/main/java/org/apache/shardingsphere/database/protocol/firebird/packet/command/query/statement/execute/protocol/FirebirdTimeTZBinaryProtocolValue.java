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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.util.FirebirdDateTimeUtils;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

/**
 * Binary protocol value for time with timezone for Firebird.
 *
 * <p>Firebird writes a {@code dtype_sql_time_tz} datum as {@code xdr_long} of the UTC time fractions followed by
 * {@code xdr_short} of the Firebird time zone identifier, see the {@code dtype_sql_time_tz} case of src/common/xdr.cpp
 * of the Firebird server. The identifier is not an offset, it is the offset in minutes shifted by {@code 1439}, so
 * {@code UTC} is 1439 and {@code +02:00} is 1559, which keeps the whole supported range positive. Because
 * {@code xdr_short} is transported in a longword the value occupies 8 bytes on the wire although its descriptor
 * storage length is 6 bytes.</p>
 */
public final class FirebirdTimeTZBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    private static final int TIME_ZONE_ID_CORRECTION = 1439;
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        Object result = FirebirdDateTimeUtils.getTime(payload.readInt4());
        payload.readInt4();
        return result;
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        OffsetTime offsetTime = (OffsetTime) value;
        LocalTime utcTime = offsetTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalTime();
        payload.writeInt4(new FirebirdDateTimeUtils(LocalDateTime.of(LocalDate.ofEpochDay(0L), utcTime)).getEncodedTime());
        payload.writeInt4(offsetTime.getOffset().getTotalSeconds() / 60 + TIME_ZONE_ID_CORRECTION);
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 8;
    }
}
