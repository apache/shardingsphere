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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder.ArrayDecoding;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.ShardingSpherePgArrayUtils;

/**
 * Binary protocol value for string array for PostgreSQL.
 */
public final class PostgreSQLStringArrayBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        return -1;
    }
    
    @SneakyThrows
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        byte[] data = new byte[parameterValueLength];
        payload.getByteBuf().readBytes(data);
        return ArrayDecoding.readBinaryArray(1, 0, data, payload.getCharset());
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        byte[] result = ShardingSpherePgArrayUtils.getBinaryBytes(value, payload.getCharset());
        payload.writeInt4(result.length);
        payload.writeBytes(result);
    }
}
