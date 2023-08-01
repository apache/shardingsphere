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

import org.apache.shardingsphere.infra.exception.mysql.vendor.MySQLVendorError;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLErrPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewErrPacketWithServerErrorCode() {
        MySQLErrPacket actual = new MySQLErrPacket(MySQLVendorError.ER_ACCESS_DENIED_ERROR, "root", "localhost", "root");
        assertThat(actual.getErrorCode(), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getVendorCode()));
        assertThat(actual.getSqlState(), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getSqlState().getValue()));
        assertThat(actual.getErrorMessage(), is(String.format(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getReason(), "root", "localhost", "root")));
    }
    
    @Test
    void assertNewErrPacketWithPayload() {
        when(payload.readInt1()).thenReturn(MySQLErrPacket.HEADER);
        when(payload.readInt2()).thenReturn(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getVendorCode());
        when(payload.readStringFix(1)).thenReturn("#");
        when(payload.readStringFix(5)).thenReturn(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getSqlState().getValue());
        when(payload.readStringEOF()).thenReturn(String.format(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getReason(), "root", "localhost", "root"));
        MySQLErrPacket actual = new MySQLErrPacket(payload);
        assertThat(actual.getErrorCode(), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getVendorCode()));
        assertThat(actual.getSqlState(), is(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getSqlState().getValue()));
        assertThat(actual.getErrorMessage(), is(String.format(MySQLVendorError.ER_ACCESS_DENIED_ERROR.getReason(), "root", "localhost", "root")));
    }
    
    @Test
    void assertWrite() {
        new MySQLErrPacket(MySQLVendorError.ER_NO_DB_ERROR).write(payload);
        verify(payload).writeInt1(MySQLErrPacket.HEADER);
        verify(payload).writeInt2(1046);
        verify(payload).writeStringFix("#");
        verify(payload).writeStringFix("3D000");
        verify(payload).writeStringEOF("No database selected");
    }
}
