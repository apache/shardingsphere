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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

/**
 * Binary protocol value for string for PostgreSQL.
 */
public final class PostgreSQLStringBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    @Override
    public int getColumnLength(final Object value) {
        return value.toString().length();
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload) {
        payload.getByteBuf().readerIndex(payload.getByteBuf().readerIndex() - 4);
        byte[] result = new byte[payload.readInt4()];
        payload.getByteBuf().readBytes(result);
        return new String(result);
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        if (value instanceof byte[]) {
            payload.writeBytes((byte[]) value);
        } else {
            payload.writeStringEOF(value.toString());
        }
    }
}
