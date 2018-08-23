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

package io.shardingsphere.proxy.transport.mysql.packet.generic;

import io.shardingsphere.proxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ErrPacketTest {
    
    @Mock
    private MySQLPacketPayload packetPayload;
    
    @Test
    public void assertNewEofPacketWithServerErrorCode() {
        ErrPacket actual = new ErrPacket(1, ServerErrorCode.ER_ACCESS_DENIED_ERROR, "root", "localhost", "root");
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
    }
    
    @Test
    public void assertNewEofPacketWithException() {
        ErrPacket actual = new ErrPacket(1, new SQLException("no reason", "X999", -1));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(-1));
        assertThat(actual.getSqlState(), is("X999"));
        assertThat(actual.getErrorMessage(), is("no reason"));
    }
    
    @Test
    public void assertNewEofPacketWithMySQLPacketPayload() {
        when(packetPayload.readInt1()).thenReturn(1, ErrPacket.HEADER);
        when(packetPayload.readInt2()).thenReturn(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode());
        when(packetPayload.readStringFix(1)).thenReturn("#");
        when(packetPayload.readStringFix(5)).thenReturn(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState());
        when(packetPayload.readStringEOF()).thenReturn(String.format(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root"));
        ErrPacket actual = new ErrPacket(packetPayload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
        verify(packetPayload, times(2)).readInt1();
        verify(packetPayload).readInt2();
        verify(packetPayload).readStringFix(1);
        verify(packetPayload).readStringFix(5);
        verify(packetPayload).readStringEOF();
    }
    
    @Test
    public void assertWrite() {
        new ErrPacket(1, new SQLException("no reason", "X999", -1)).write(packetPayload);
        verify(packetPayload).writeInt1(ErrPacket.HEADER);
        verify(packetPayload).writeInt2(-1);
        verify(packetPayload).writeStringFix("#");
        verify(packetPayload).writeStringFix("X999");
        verify(packetPayload).writeStringEOF("no reason");
    }
}