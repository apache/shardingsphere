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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit;

import com.google.common.base.Optional;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.CommandTransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.TransportResponse;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.generic.MySQLOKPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLComQuitPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertExecute() {
        Optional<TransportResponse> actual = new MySQLComQuitPacket(1).execute();
        assertTrue(actual.isPresent());
        assertThat(((CommandTransportResponse) actual.get()).getPackets().size(), is(1));
        assertThat(((MySQLPacket) ((CommandTransportResponse) actual.get()).getHeadPacket()).getSequenceId(), is(2));
        assertThat(((MySQLOKPacket) ((CommandTransportResponse) actual.get()).getHeadPacket()).getAffectedRows(), is(0L));
        assertThat(((MySQLOKPacket) ((CommandTransportResponse) actual.get()).getHeadPacket()).getLastInsertId(), is(0L));
        assertThat(((MySQLOKPacket) ((CommandTransportResponse) actual.get()).getHeadPacket()).getWarnings(), is(0));
        assertThat(((MySQLOKPacket) ((CommandTransportResponse) actual.get()).getHeadPacket()).getInfo(), is(""));
    }
    
    @Test
    public void assertWrite() {
        MySQLComQuitPacket actual = new MySQLComQuitPacket(1);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_QUIT.getValue());
    }
}
