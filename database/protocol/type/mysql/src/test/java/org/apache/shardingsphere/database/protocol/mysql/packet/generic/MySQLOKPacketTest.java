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

package org.apache.shardingsphere.database.protocol.mysql.packet.generic;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLStatusFlag;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLOKPacketTest {
    
    @Mock
    private MySQLPacketPayload packetPayload;
    
    @Test
    void assertNewOKPacketWithStatusFlag() {
        MySQLOKPacket actual = new MySQLOKPacket(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        assertThat(actual.getAffectedRows(), is(0L));
        assertThat(actual.getLastInsertId(), is(0L));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    void assertNewOKPacketWithAffectedRowsAndLastInsertId() {
        MySQLOKPacket actual = new MySQLOKPacket(100L, 9999L, MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        assertThat(actual.getAffectedRows(), is(100L));
        assertThat(actual.getLastInsertId(), is(9999L));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    void assertNewOKPacketWithPayload() {
        when(packetPayload.readInt1()).thenReturn(MySQLOKPacket.HEADER);
        when(packetPayload.readInt2()).thenReturn(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0);
        when(packetPayload.readIntLenenc()).thenReturn(100L, 9999L);
        when(packetPayload.readStringEOF()).thenReturn("");
        MySQLOKPacket actual = new MySQLOKPacket(packetPayload);
        assertThat(actual.getAffectedRows(), is(100L));
        assertThat(actual.getLastInsertId(), is(9999L));
        assertThat(actual.getStatusFlag(), is(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        assertThat(actual.getWarnings(), is(0));
        assertThat(actual.getInfo(), is(""));
    }
    
    @Test
    void assertWrite() {
        new MySQLOKPacket(100L, 9999L, MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()).write(packetPayload);
        verify(packetPayload).writeInt1(MySQLOKPacket.HEADER);
        verify(packetPayload).writeIntLenenc(100L);
        verify(packetPayload).writeIntLenenc(9999L);
        verify(packetPayload).writeInt2(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue());
        verify(packetPayload).writeInt2(0);
        verify(packetPayload).writeStringEOF("");
    }
}
