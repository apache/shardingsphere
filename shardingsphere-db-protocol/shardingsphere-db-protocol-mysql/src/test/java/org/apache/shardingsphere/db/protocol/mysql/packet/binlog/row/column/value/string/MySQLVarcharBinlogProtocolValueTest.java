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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.value.string;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLVarcharBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @Before
    public void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLColumnType.MYSQL_TYPE_VARCHAR);
    }
    
    @Test
    public void assertReadVarcharValueWithMeta1() {
        String expected = "test_value";
        columnDef.setColumnMeta(10);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedByte()).thenReturn((short) expected.length());
        when(payload.readStringFix(expected.length())).thenReturn(expected);
        assertThat(new MySQLVarcharBinlogProtocolValue().read(columnDef, payload), is(expected));
    }
    
    @Test
    public void assertReadVarcharValueWithMeta2() {
        StringBuilder expectedStringBuilder = new StringBuilder("test string for length more than 256");
        for (int i = 0; i < 256; i++) {
            expectedStringBuilder.append(i);
        }
        String expected = expectedStringBuilder.toString();
        columnDef.setColumnMeta(expected.length());
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShortLE()).thenReturn(expected.length());
        when(payload.readStringFix(expected.length())).thenReturn(expected);
        assertThat(new MySQLVarcharBinlogProtocolValue().read(columnDef, payload), is(expected));
    }
}
