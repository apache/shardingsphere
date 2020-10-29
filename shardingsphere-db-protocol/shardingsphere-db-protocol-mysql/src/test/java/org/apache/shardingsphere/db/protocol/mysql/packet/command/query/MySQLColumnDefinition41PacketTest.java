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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.query;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerInfo;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLColumnDefinition41PacketTest {
    
    @Mock
    private ResultSetMetaData resultSetMetaData;
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertWriteWithResultSetMetaData() throws SQLException {
        when(resultSetMetaData.getSchemaName(1)).thenReturn("logic_db");
        when(resultSetMetaData.getTableName(1)).thenReturn("tbl");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("id");
        when(resultSetMetaData.getColumnName(1)).thenReturn("id");
        when(resultSetMetaData.getColumnDisplaySize(1)).thenReturn(10);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        MySQLColumnDefinition41Packet actual = new MySQLColumnDefinition41Packet(1, resultSetMetaData, 1);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verifyWrite();
    }
    
    @Test
    public void assertWriteWithPayload() {
        when(payload.readInt1()).thenReturn(1, MySQLColumnType.MYSQL_TYPE_LONG.getValue(), 0);
        when(payload.readInt2()).thenReturn(MySQLServerInfo.CHARSET, 0);
        when(payload.readInt4()).thenReturn(10);
        when(payload.readIntLenenc()).thenReturn(0x0cL);
        when(payload.readStringLenenc()).thenReturn("def", "logic_db", "tbl", "tbl", "id", "id");
        MySQLColumnDefinition41Packet actual = new MySQLColumnDefinition41Packet(payload);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verifyWrite();
    }
    
    private void verifyWrite() {
        verify(payload).writeStringLenenc("def");
        verify(payload).writeStringLenenc("logic_db");
        verify(payload, times(2)).writeStringLenenc("tbl");
        verify(payload, times(2)).writeStringLenenc("id");
        verify(payload).writeIntLenenc(0x0c);
        verify(payload).writeInt2(MySQLServerInfo.CHARSET);
        verify(payload).writeInt4(10);
        verify(payload).writeInt1(MySQLColumnType.MYSQL_TYPE_LONG.getValue());
        verify(payload).writeInt2(0);
        verify(payload).writeInt1(0);
        verify(payload).writeReserved(2);
    }
}
