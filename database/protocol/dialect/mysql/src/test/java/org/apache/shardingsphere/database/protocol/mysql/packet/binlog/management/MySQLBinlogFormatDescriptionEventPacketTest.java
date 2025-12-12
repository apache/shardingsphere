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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.management;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBinlogFormatDescriptionEventPacketTest {
    
    private static final String MYSQL_SERVER_VERSION = "5.7.14-log" + new String(new byte[40]);
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private MySQLBinlogEventHeader binlogEventHeader;
    
    @Test
    void assertNew() {
        when(payload.readInt2()).thenReturn(4);
        when(payload.readStringFixByBytes(50)).thenReturn(MYSQL_SERVER_VERSION.getBytes());
        when(payload.readInt4()).thenReturn(1234567890);
        when(payload.readInt1()).thenReturn(19);
        MySQLBinlogFormatDescriptionEventPacket actual = new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload);
        assertThat(actual.getBinlogVersion(), is(4));
        assertThat(actual.getMysqlServerVersion(), is(MYSQL_SERVER_VERSION.getBytes()));
        assertThat(actual.getCreateTimestamp(), is(1234567890));
        assertThat(actual.getEventHeaderLength(), is(19));
        assertThat(actual.getBinlogEventHeader(), is(binlogEventHeader));
    }
}
