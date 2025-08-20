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
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLJsonValueDecoder.JsonValueTypes;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MySQLJsonBinlogProtocolValueTest {
    
    private static final String KEY = "key";
    
    private static final String EXPECTED_JSON = "{\"key\":true}";
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private ByteBuf jsonValueByteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.JSON);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        mockJsonValue();
    }
    
    private void mockJsonValue() {
        when(jsonValueByteBuf.slice()).thenReturn(jsonValueByteBuf);
        when(jsonValueByteBuf.readUnsignedByte()).thenReturn((short) JsonValueTypes.SMALL_JSON_OBJECT, (short) JsonValueTypes.LITERAL);
        when(jsonValueByteBuf.readUnsignedShortLE()).thenReturn(1, 0, 0, 3, JsonValueTypes.LITERAL_TRUE & 0xff);
        when(jsonValueByteBuf.getBytes(0, new byte[3], 0, 3)).then(invocationOnMock -> {
            byte[] bytes = invocationOnMock.getArgument(1);
            System.arraycopy(KEY.getBytes(), 0, bytes, 0, KEY.length());
            return jsonValueByteBuf;
        });
    }
    
    @Test
    void assertReadJsonValueWithMeta1() {
        columnDef.setColumnMeta(1);
        when(byteBuf.readUnsignedByte()).thenReturn((short) 1);
        when(byteBuf.readBytes(1)).thenReturn(jsonValueByteBuf);
        assertThat(new MySQLJsonBinlogProtocolValue().read(columnDef, payload), is(EXPECTED_JSON));
    }
    
    @Test
    void assertReadJsonValueWithMeta2() {
        columnDef.setColumnMeta(2);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        when(byteBuf.readBytes(2)).thenReturn(jsonValueByteBuf);
        assertThat(new MySQLJsonBinlogProtocolValue().read(columnDef, payload), is(EXPECTED_JSON));
    }
    
    @Test
    void assertReadJsonValueWithMeta3() {
        columnDef.setColumnMeta(3);
        when(byteBuf.readUnsignedMediumLE()).thenReturn(3);
        when(byteBuf.readBytes(3)).thenReturn(jsonValueByteBuf);
        assertThat(new MySQLJsonBinlogProtocolValue().read(columnDef, payload), is(EXPECTED_JSON));
    }
    
    @Test
    void assertReadJsonValueWithMeta4() {
        columnDef.setColumnMeta(4);
        when(payload.readInt4()).thenReturn(4);
        when(byteBuf.readBytes(4)).thenReturn(jsonValueByteBuf);
        assertThat(new MySQLJsonBinlogProtocolValue().read(columnDef, payload), is(EXPECTED_JSON));
    }
    
    @Test
    void assertReadJsonValueWithIllegalMeta() {
        columnDef.setColumnMeta(5);
        assertThrows(UnsupportedSQLOperationException.class, () -> new MySQLJsonBinlogProtocolValue().read(columnDef, payload));
    }
}
