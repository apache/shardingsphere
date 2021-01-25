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

package org.apache.shardingsphere.db.protocol.postgresql.packet.generic;

import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLErrorResponsePacketTest {

    @Mock
    private PostgreSQLPacketPayload payload;

    @Test
    public void assertWrite() {
        PostgreSQLErrorResponsePacket responsePacket = new PostgreSQLErrorResponsePacket();
        responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY, "FATAL");
        responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE, "3D000");
        responsePacket.addField(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE, "database \"test\" does not exist");
        responsePacket.write(payload);
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_SEVERITY);
        verify(payload).writeStringNul("FATAL");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_CODE);
        verify(payload).writeStringNul("3D000");
        verify(payload).writeInt1(PostgreSQLErrorResponsePacket.FIELD_TYPE_MESSAGE);
        verify(payload).writeStringNul("database \"test\" does not exist");
        verify(payload).writeInt1(0);
    }
}
