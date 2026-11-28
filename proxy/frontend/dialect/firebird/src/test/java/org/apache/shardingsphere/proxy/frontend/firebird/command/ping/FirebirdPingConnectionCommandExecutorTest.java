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

package org.apache.shardingsphere.proxy.frontend.firebird.command.ping;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdPingConnectionCommandExecutorTest {
    
    @Test
    void assertExecute() {
        Collection<DatabasePacket> actual = new FirebirdPingConnectionCommandExecutor().execute();
        assertThat(actual.size(), is(1));
        ByteBuf buffer = Unpooled.buffer();
        try {
            actual.iterator().next().write(new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8));
            assertThat(buffer.readInt(), is(FirebirdCommandPacketType.RESPONSE.getValue()));
            assertThat(buffer.readInt(), is(0));
            assertThat(buffer.readLong(), is(0L));
            assertThat(buffer.readInt(), is(0));
            assertThat(buffer.readInt(), is(ISCConstants.isc_arg_end));
            assertThat(buffer.readableBytes(), is(0));
        } finally {
            buffer.release();
        }
    }
}
