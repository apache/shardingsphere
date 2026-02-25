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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLDecimalBinlogProtocolValueTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertReadArguments")
    void assertRead(final String name, final int columnMeta, final String binaryValue, final String expected) {
        MySQLBinlogColumnDef columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.NEWDECIMAL);
        columnDef.setColumnMeta(columnMeta);
        MySQLPacketPayload payload = mock(MySQLPacketPayload.class);
        byte[] decimalBytes = ByteBufUtil.decodeHexDump(binaryValue);
        when(payload.readStringFixByBytes(decimalBytes.length)).thenReturn(decimalBytes);
        assertThat(new MySQLDecimalBinlogProtocolValue().read(columnDef, payload).toString(), is(expected));
    }
    
    private static Stream<Arguments> assertReadArguments() {
        return Stream.of(
                Arguments.of("positive with extra integer and extra scale", (14 << 8) + 4, "810DFB38D204D2", "1234567890.1234"),
                Arguments.of("negative with extra integer and extra scale", (14 << 8) + 4, "7EF204C72DFB2D", "-1234567890.1234"),
                Arguments.of("positive with full integer and full scale chunks", (18 << 8) + 9, "875BCD15075BCD15", "123456789.123456789"));
    }
}
