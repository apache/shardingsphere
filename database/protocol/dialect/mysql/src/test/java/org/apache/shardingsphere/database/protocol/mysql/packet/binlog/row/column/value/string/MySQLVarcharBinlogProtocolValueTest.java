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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLVarcharBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.VARCHAR);
    }
    
    @Test
    void assertReadVarcharValueWithMeta1() {
        assertReadVarcharValueWithMeta("test_value".getBytes());
        assertReadVarcharValueWithMeta(new byte[]{-1, 0, 1});
    }
    
    private void assertReadVarcharValueWithMeta(final byte[] expected) {
        columnDef.setColumnMeta(expected.length);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedByte()).thenReturn((short) expected.length);
        when(payload.readStringFixByBytes(expected.length)).thenReturn(expected);
        Serializable actual = new MySQLVarcharBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual, isA(MySQLBinaryString.class));
        assertThat(((MySQLBinaryString) actual).getBytes(), is(expected));
    }
    
    @Test
    void assertReadVarcharValueWithMeta2() {
        StringBuilder expectedStringBuilder = new StringBuilder("test string for length more than 256");
        for (int i = 0; i < 256; i++) {
            expectedStringBuilder.append(i);
        }
        byte[] expected = expectedStringBuilder.toString().getBytes();
        columnDef.setColumnMeta(expected.length);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShortLE()).thenReturn(expected.length);
        when(payload.readStringFixByBytes(expected.length)).thenReturn(expected);
        Serializable actual = new MySQLVarcharBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual, isA(MySQLBinaryString.class));
        assertThat(((MySQLBinaryString) actual).getBytes(), is(expected));
    }
}
