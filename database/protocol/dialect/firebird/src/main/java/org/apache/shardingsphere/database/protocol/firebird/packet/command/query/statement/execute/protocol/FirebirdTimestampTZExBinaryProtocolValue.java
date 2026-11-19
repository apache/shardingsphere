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

import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;

import java.time.OffsetDateTime;

/**
 * Binary protocol value for extended timestamp with timezone for Firebird.
 *
 * <p>Firebird writes a {@code dtype_ex_timestamp_tz} datum as two {@code xdr_long} values of the UTC date and time
 * fractions followed by two {@code xdr_short} values, the Firebird time zone identifier and the extended offset, see the
 * {@code dtype_ex_timestamp_tz} case of src/common/xdr.cpp of the Firebird server. The extended offset is the absolute
 * time zone offset in minutes at the instant of the value, so {@code +02:00} is 120. Because {@code xdr_short} is
 * transported in a longword the value occupies 16 bytes on the wire although its descriptor storage length is 12 bytes.</p>
 */
public final class FirebirdTimestampTZExBinaryProtocolValue implements FirebirdBinaryProtocolValue {
    
    private final FirebirdTimestampTZBinaryProtocolValue delegate = new FirebirdTimestampTZBinaryProtocolValue();
    
    @Override
    public Object read(final FirebirdPacketPayload payload) {
        Object result = delegate.read(payload);
        payload.readInt4();
        return result;
    }
    
    @Override
    public void write(final FirebirdPacketPayload payload, final Object value) {
        delegate.write(payload, value);
        payload.writeInt4(((OffsetDateTime) value).getOffset().getTotalSeconds() / 60);
    }
    
    @Override
    public int getLength(final FirebirdPacketPayload payload) {
        return 16;
    }
}
