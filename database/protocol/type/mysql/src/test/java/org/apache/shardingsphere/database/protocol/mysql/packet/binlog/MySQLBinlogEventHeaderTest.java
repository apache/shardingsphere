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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinlogEventFlag;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinlogEventType;
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
class MySQLBinlogEventHeaderTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    void assertNew() {
        when(payload.readInt4()).thenReturn(1234567890, 123456, 19, 4);
        when(payload.readInt1()).thenReturn(MySQLBinlogEventType.UNKNOWN_EVENT.getValue());
        when(payload.readInt2()).thenReturn(MySQLBinlogEventFlag.LOG_EVENT_BINLOG_IN_USE_F.getValue());
        MySQLBinlogEventHeader actual = new MySQLBinlogEventHeader(payload, 4);
        assertThat(actual.getTimestamp(), is(1234567890));
        assertThat(actual.getEventType(), is(MySQLBinlogEventType.UNKNOWN_EVENT.getValue()));
        assertThat(actual.getServerId(), is(123456));
        assertThat(actual.getEventSize(), is(19));
        assertThat(actual.getLogPos(), is(4));
        assertThat(actual.getFlags(), is(MySQLBinlogEventFlag.LOG_EVENT_BINLOG_IN_USE_F.getValue()));
    }
    
    @Test
    void assertWrite() {
        new MySQLBinlogEventHeader(1234567890, MySQLBinlogEventType.UNKNOWN_EVENT.getValue(), 123456, 19, 4, MySQLBinlogEventFlag.LOG_EVENT_BINLOG_IN_USE_F.getValue(), 4).write(payload);
        verify(payload).writeInt4(1234567890);
        verify(payload).writeInt1(MySQLBinlogEventType.UNKNOWN_EVENT.getValue());
        verify(payload).writeInt4(123456);
        verify(payload).writeInt4(19);
        verify(payload).writeInt4(4);
        verify(payload).writeInt2(MySQLBinlogEventFlag.LOG_EVENT_BINLOG_IN_USE_F.getValue());
    }
}
