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

package org.apache.shardingsphere.database.protocol.mysql.packet.handshake;

import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLAuthMoreDataPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNewWithInvalidHeader() {
        assertThrows(IllegalArgumentException.class, () -> new MySQLAuthMoreDataPacket(payload));
    }
    
    @Test
    void assertNewWithValidHeader() {
        when(payload.readInt1()).thenReturn(MySQLAuthMoreDataPacket.HEADER);
        when(payload.readStringEOFByBytes()).thenReturn(new byte[0]);
        MySQLAuthMoreDataPacket packet = new MySQLAuthMoreDataPacket(payload);
        assertThat(packet.getPluginData(), is(new byte[0]));
    }
    
    @Test
    void assertWrite() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new MySQLAuthMoreDataPacket(new byte[0]).write(payload));
    }
}
