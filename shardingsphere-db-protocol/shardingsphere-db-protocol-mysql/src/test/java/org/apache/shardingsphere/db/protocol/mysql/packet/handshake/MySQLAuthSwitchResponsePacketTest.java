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

package org.apache.shardingsphere.db.protocol.mysql.packet.handshake;

import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLAuthSwitchResponsePacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private final byte[] authPluginResponse = {0x22, 0x33};
    
    @Test
    public void assertMySQLAuthSwitchResponsePacket() {
        when(payload.readInt1()).thenReturn(0x11);
        when(payload.readStringEOFByBytes()).thenReturn(authPluginResponse);
        MySQLAuthSwitchResponsePacket authSwitchResponsePacket = new MySQLAuthSwitchResponsePacket(payload);
        assertThat(authSwitchResponsePacket.getSequenceId(), is(0x11));
        assertThat(authSwitchResponsePacket.getAuthPluginResponse(), is(authPluginResponse));
    }
}
