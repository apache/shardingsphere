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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.binlog;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
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
public final class MySQLComBinlogDumpCommandPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertNew() {
        when(payload.readInt2()).thenReturn(0);
        when(payload.readStringEOF()).thenReturn("binlog-000001");
        when(payload.readInt4()).thenReturn(4, 123456);
        MySQLComBinlogDumpCommandPacket actual = new MySQLComBinlogDumpCommandPacket(payload);
        assertThat(actual.getBinlogPos(), is(4));
        assertThat(actual.getFlags(), is(0));
        assertThat(actual.getServerId(), is(123456));
        assertThat(actual.getBinlogFilename(), is("binlog-000001"));
    }
    
    @Test
    public void assertWrite() {
        new MySQLComBinlogDumpCommandPacket(4, 123456, "binlog-000001").write(payload);
        verify(payload).writeInt1(MySQLCommandPacketType.COM_BINLOG_DUMP.getValue());
        verify(payload).writeInt4(4);
        verify(payload).writeInt2(0);
        verify(payload).writeInt4(123456);
        verify(payload).writeStringEOF("binlog-000001");
    }
}
