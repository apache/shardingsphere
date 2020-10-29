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

package org.apache.shardingsphere.db.protocol.mysql.packet.generic;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLErrPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNewErrPacketWithServerErrorCode() {
        MySQLErrPacket actual = new MySQLErrPacket(1, MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR, "root", "localhost", "root");
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
    }
    
    @Test
    public void assertNewErrPacketWithPayload() {
        when(payload.readInt1()).thenReturn(1, MySQLErrPacket.HEADER);
        when(payload.readInt2()).thenReturn(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode());
        when(payload.readStringFix(1)).thenReturn("#");
        when(payload.readStringFix(5)).thenReturn(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState());
        when(payload.readStringEOF()).thenReturn(String.format(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root"));
        MySQLErrPacket actual = new MySQLErrPacket(payload);
        assertThat(actual.getSequenceId(), is(1));
        assertThat(actual.getErrorCode(), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorCode()));
        assertThat(actual.getSqlState(), is(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getSqlState()));
        assertThat(actual.getErrorMessage(), is(String.format(MySQLServerErrorCode.ER_ACCESS_DENIED_ERROR.getErrorMessage(), "root", "localhost", "root")));
    }
    
    @Test
    public void assertWrite() {
        new MySQLErrPacket(1, MySQLServerErrorCode.ER_NO_DB_ERROR).write(payload);
        verify(payload).writeInt1(MySQLErrPacket.HEADER);
        verify(payload).writeInt2(1046);
        verify(payload).writeStringFix("#");
        verify(payload).writeStringFix("3D000");
        verify(payload).writeStringEOF("No database selected");
    }
}
