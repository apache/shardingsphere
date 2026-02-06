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

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.PostgreSQLBinaryTimestampUtils;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder.PgBinaryObj;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.sql.Timestamp;

/**
 * Binary protocol value for time for PostgreSQL.
 */
public final class PostgreSQLTimeStampBinaryProtocolValue implements PostgreSQLBinaryProtocolValue {
    
    @Override
    public int getColumnLength(final PostgreSQLPacketPayload payload, final Object value) {
        return 8;
    }
    
    @Override
    public Object read(final PostgreSQLPacketPayload payload, final int parameterValueLength) {
        // 获取pg 时间戳
        byte[] bytes = new byte[8];
        payload.getByteBuf().readBytes(bytes);
        PgBinaryObj result = new PgBinaryObj(bytes);
        result.setType("timestamp");
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload, final Object value) {
        payload.writeInt8(PostgreSQLBinaryTimestampUtils.toPostgreSQLTime((Timestamp) value));
    }
}
