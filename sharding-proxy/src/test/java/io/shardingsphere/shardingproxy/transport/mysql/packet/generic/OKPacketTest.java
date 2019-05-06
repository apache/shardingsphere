/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.transport.mysql.packet.generic;

import io.shardingsphere.shardingproxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OKPacketTest {
    
    @Mock
    private MySQLPacketPayload packetPayload;
    
    @Test
    public void assertNewOKPacketWithSequenceId() {
        OKPacket actual = new OKPacket(1);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getAffectedRows(), is(0L));
        assertThat(actual.getLastInsertId(), is(0L));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    public void assertNewOKPacketWithAffectedRowsAndLastInsertId() {
        OKPacket actual = new OKPacket(1, 100, 9999L);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getAffectedRows(), is(100L));
        assertThat(actual.getLastInsertId(), is(9999L));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    public void assertNewOKPacketWithMySQLPacketPayload() {
        when(packetPayload.readInt1()).thenReturn(1, OKPacket.HEADER);
        when(packetPayload.readIntLenenc()).thenReturn(100L, 9999L);
        when(packetPayload.readInt2()).thenReturn(0, 1);
        when(packetPayload.readStringEOF()).thenReturn("no info");
        OKPacket actual = new OKPacket(packetPayload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getAffectedRows(), is(100L));
        assertThat(actual.getLastInsertId(), is(9999L));
        assertThat(actual.getWarnings(), is(1));
        assertThat(actual.getInfo(), is("no info"));
    }
    
    @Test
    public void assertWrite() {
        new OKPacket(1, 100L, 9999L).write(packetPayload);
        verify(packetPayload).writeInt1(OKPacket.HEADER);
        verify(packetPayload).writeIntLenenc(100L);
        verify(packetPayload).writeIntLenenc(9999L);
        verify(packetPayload).writeInt2(StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(packetPayload).writeInt2(0);
        verify(packetPayload).writeStringEOF("");
    }
}
