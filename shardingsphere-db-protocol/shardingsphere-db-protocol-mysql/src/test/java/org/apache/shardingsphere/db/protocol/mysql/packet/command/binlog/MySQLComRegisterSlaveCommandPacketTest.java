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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComRegisterSlaveCommandPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNew() {
        when(payload.readInt4()).thenReturn(123456, 654321);
        when(payload.readInt1()).thenReturn(4, 4, 8);
        when(payload.readStringFix(4)).thenReturn("host", "user");
        when(payload.readStringFix(8)).thenReturn("password");
        when(payload.readInt2()).thenReturn(3307);
        MySQLComRegisterSlaveCommandPacket actual = new MySQLComRegisterSlaveCommandPacket(payload);
        assertThat(actual.getServerId(), is(123456));
        assertThat(actual.getSlaveHostname(), is("host"));
        assertThat(actual.getSlaveUser(), is("user"));
        assertThat(actual.getSlavePassword(), is("password"));
        assertThat(actual.getSlavePort(), is(3307));
        assertThat(actual.getMasterId(), is(654321));
    }
    
    @Test
    public void assertWrite() {
        new MySQLComRegisterSlaveCommandPacket(123456, "host", "user", "password", 3307).write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_REGISTER_SLAVE.getValue());
        verify(payload).writeInt4(123456);
        verify(payload).writeStringFix("host");
        verify(payload, times(2)).writeInt1(4);
        verify(payload).writeStringFix("user");
        verify(payload).writeInt1(8);
        verify(payload).writeStringFix("password");
        verify(payload).writeInt2(3307);
        verify(payload).writeBytes(new byte[4]);
        verify(payload).writeInt4(0);
    }
}
