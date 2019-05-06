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

import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ErrPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNewErrPacketWithServerErrorCode() {
        ErrPacket actual = new ErrPacket(1, ServerErrorCode.ER_ACCESS_DENIED_ERROR, "root", "localhost", "root");
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
    }
    
    @Test
    public void assertNewErrPacketWithException() {
        ErrPacket actual = new ErrPacket(1, new SQLException("no reason", "X999", -1));
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(-1));
        assertThat(actual.getSqlState(), is("X999"));
        assertThat(actual.getErrorMessage(), is("no reason"));
    }
    
    @Test
    public void assertNewErrPacketWithMySQLPacketPayload() {
        when(payload.readInt1()).thenReturn(1, ErrPacket.HEADER);
        when(payload.readInt2()).thenReturn(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode());
        when(payload.readStringFix(1)).thenReturn("#");
        when(payload.readStringFix(5)).thenReturn(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState());
        when(payload.readStringEOF()).thenReturn(String.format(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root"));
        ErrPacket actual = new ErrPacket(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(ServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
    }
    
    @Test
    public void assertWrite() {
        new ErrPacket(1, new SQLException("no reason", "X999", -1)).write(payload);
        verify(payload).writeInt1(ErrPacket.HEADER);
        verify(payload).writeInt2(-1);
        verify(payload).writeStringFix("#");
        verify(payload).writeStringFix("X999");
        verify(payload).writeStringEOF("no reason");
    }
}
