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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

class PostgreSQLStringBinaryProtocolValueTest {
    
    @Test
    void assertNewInstance() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        String source = "abc哈哈";
        byte[] bytes = source.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeBytes(bytes);
        PostgreSQLStringBinaryProtocolValue instance = new PostgreSQLStringBinaryProtocolValue();
        Object read = instance.read(payload, bytes.length);
        Assertions.assertEquals(source, read);
        payload.getByteBuf().clear();
        instance.write(payload, read);
        Assertions.assertEquals(bytes.length, byteBuf.readInt());
        Assertions.assertEquals(source, instance.read(payload, bytes.length));
    }
}
