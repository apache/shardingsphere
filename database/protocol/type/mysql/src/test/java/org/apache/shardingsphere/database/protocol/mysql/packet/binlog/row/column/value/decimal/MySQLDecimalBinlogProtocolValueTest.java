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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.decimal;

import io.netty.buffer.ByteBufUtil;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDecimalBinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.NEWDECIMAL);
        columnDef.setColumnMeta((14 << 8) + 4);
    }
    
    @Test
    void assertDecodePositiveNewDecimal() {
        byte[] newDecimalBytes = ByteBufUtil.decodeHexDump("810DFB38D204D2");
        when(payload.readStringFixByBytes(newDecimalBytes.length)).thenReturn(newDecimalBytes);
        BigDecimal actual = (BigDecimal) new MySQLDecimalBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual.toString(), is("1234567890.1234"));
    }
    
    @Test
    void assertDecodeNegativeNewDecimal() {
        byte[] newDecimalBytes = ByteBufUtil.decodeHexDump("7EF204C72DFB2D");
        when(payload.readStringFixByBytes(newDecimalBytes.length)).thenReturn(newDecimalBytes);
        BigDecimal actual = (BigDecimal) new MySQLDecimalBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual.toString(), is("-1234567890.1234"));
    }
    
    @Test
    void assertDecodeNegativeNewDecimalWithLargeNumber() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.NEWDECIMAL);
        columnDef.setColumnMeta(32 << 8 | 6);
        byte[] newDecimalBytes = ByteBufUtil.decodeHexDump("7DFEFDB5CC2741EFDEBE4154FD52E7");
        when(payload.readStringFixByBytes(newDecimalBytes.length)).thenReturn(newDecimalBytes);
        BigDecimal actual = (BigDecimal) new MySQLDecimalBinlogProtocolValue().read(columnDef, payload);
        assertThat(actual.toString(), is("-33620554869842448557956779.175384"));
    }
}
