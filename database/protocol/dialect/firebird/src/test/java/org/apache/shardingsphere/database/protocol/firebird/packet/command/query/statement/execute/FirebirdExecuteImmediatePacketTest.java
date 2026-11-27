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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirebirdExecuteImmediatePacketTest {
    
    @Test
    void assertConstructor() {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeInt(FirebirdCommandPacketType.EXEC_IMMEDIATE.getValue()).writeInt(42).writeInt(7).writeInt(3);
            FirebirdPacketPayload payload = new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8);
            payload.writeString("/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ INSERT INTO foo_tbl VALUES ('ёж')");
            payload.writeBuffer(new byte[]{1, 2, 3});
            payload.writeInt4(64);
            FirebirdExecuteImmediatePacket actual = new FirebirdExecuteImmediatePacket(payload);
            assertThat(actual.getTransactionId(), is(42));
            assertThat(actual.getSQL(), is("INSERT INTO foo_tbl VALUES ('ёж')"));
            assertTrue(actual.getHintValueContext().isWriteRouteOnly());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }
}
