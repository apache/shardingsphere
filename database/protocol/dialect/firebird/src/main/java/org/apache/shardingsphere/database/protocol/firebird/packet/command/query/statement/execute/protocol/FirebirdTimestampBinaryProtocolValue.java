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
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Binary protocol value for timestamp for Firebird.
 */
public final class FirebirdTimestampBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        return FirebirdDateTimeUtils.getDateTime(payload.readInt4(), payload.readInt4());
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        LocalDateTime localDateTime = toLocalDateTime(value);
        payload.writeInt4(FirebirdDateTimeUtils.getEncodedDate(localDateTime));
        payload.writeInt4(new FirebirdDateTimeUtils(localDateTime).getEncodedTime());
    }
    
    @HighFrequencyInvocation
    private LocalDateTime toLocalDateTime(final Object value) {
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof Timestamp) {
            return ((Timestamp) value).toLocalDateTime();
        }
        return new Timestamp(((Date) value).getTime()).toLocalDateTime();
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 8;
    }
}
