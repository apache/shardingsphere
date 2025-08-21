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

import io.netty.buffer.ByteBufAllocator;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.codec.decoder.PgBinaryObj;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.postgresql.util.ByteConverter;

import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.TimeZone;

class PostgreSQLTimeBinaryProtocolValueTest {
    
    @Test
    void test() {
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(ByteBufAllocator.DEFAULT.buffer(), StandardCharsets.UTF_8);
        PostgreSQLTimeBinaryProtocolValue actual = new PostgreSQLTimeBinaryProtocolValue();
        Time timestamp = new Time(-TimeZone.getDefault().getRawOffset());
        actual.write(payload, timestamp);
        
        PgBinaryObj read = (PgBinaryObj) actual.read(payload, 8);
        Assertions.assertEquals("time", read.getType());
        byte[] target = new byte[8];
        read.toBytes(target, 0);
        long l = ByteConverter.int8(target, 0);
        Assertions.assertEquals(0L, l);
        payload.getByteBuf().release();
    }
}
