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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLTime2BinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.TIME2);
        columnDef.setColumnMeta(0);
    }
    
    @Test
    void assertRead() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4)));
    }
    
    @Test
    void assertReadWithFraction1() {
        columnDef.setColumnMeta(1);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(payload.readInt1()).thenReturn(90);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4).withNano(900000000)));
    }
    
    @Test
    void assertReadWithFraction3() {
        columnDef.setColumnMeta(3);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedShort()).thenReturn(9000);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4).withNano(900000000)));
    }
    
    @Test
    void assertReadWithFraction6() {
        columnDef.setColumnMeta(6);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (0x10 << 12) | (0x08 << 6) | 0x04, 10123);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(LocalTime.of(16, 8, 4).withNano(10123000)));
    }
    
    @Test
    void assertReadNullTime() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtils.ZERO_OF_TIME));
    }
    
    @Test
    void assertReadHourBeyondLocalTimeRange() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (100 << 12) | (8 << 6) | 4);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is("100:08:04"));
    }
    
    @Test
    void assertReadMaxHour() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000 | (838 << 12) | (59 << 6) | 59);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is("838:59:59"));
    }
    
    @Test
    void assertReadNegativeTime() {
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x1000000 - (0x800000 | (16 << 12) | (8 << 6) | 4));
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is("-16:08:04"));
    }
    
    @Test
    void assertReadNegativeTimeWithOneByteFraction() {
        assertThat(readFully(2, 0x7f, 0xff, 0xfe, 0xf6), is("-00:00:01.100000"));
        assertThat(readFully(1, 0x7e, 0xfd, 0xfb, 0xf6), is("-16:08:04.100000"));
    }
    
    @Test
    void assertReadNegativeTimeWithOneByteFractionUnderOneSecond() {
        assertThat(readFully(2, 0x7f, 0xff, 0xff, 0xff), is("-00:00:00.010000"));
    }
    
    @Test
    void assertReadNegativeWholeSecondTimeWithOneByteFraction() {
        assertThat(readFully(2, 0x7f, 0xff, 0xff, 0x00), is("-00:00:01"));
    }
    
    @Test
    void assertReadNegativeTimeWithTwoByteFraction() {
        assertThat(readFully(4, 0x7e, 0xfd, 0xfb, 0xff, 0x9b), is("-16:08:04.010100"));
        assertThat(readFully(3, 0x7e, 0xfd, 0xfb, 0xff, 0x9c), is("-16:08:04.010000"));
    }
    
    @Test
    void assertReadNegativeTimeWithThreeByteFraction() {
        assertThat(readFully(6, 0x7e, 0xfd, 0xfb, 0xff, 0xd8, 0x75), is("-16:08:04.010123"));
        assertThat(readFully(5, 0x4b, 0x91, 0x04, 0xf0, 0xbd, 0xca), is("-838:59:59.999990"));
    }
    
    @Test
    void assertReadNegativeWholeSecondTimeWithThreeByteFraction() {
        assertThat(readFully(6, 0x7e, 0xfd, 0xfc, 0x00, 0x00, 0x00), is("-16:08:04"));
    }
    
    @Test
    void assertReadPositiveTimeWithThreeByteFraction() {
        assertThat(readFully(6, 0x81, 0x02, 0x04, 0x00, 0x27, 0x8b), is(LocalTime.of(16, 8, 4, 10123000)));
    }
    
    @Test
    void assertReadFractionOfZeroSeconds() {
        assertThat(readFully(1, 0x80, 0x00, 0x00, 0x32), is(LocalTime.of(0, 0, 0, 500000000)));
    }
    
    @Test
    void assertReadNegativeTimeWithNonLatinDefaultFormatLocale() {
        Locale originalLocale = Locale.getDefault(Locale.Category.FORMAT);
        Locale.setDefault(Locale.Category.FORMAT, Locale.forLanguageTag("th-TH-u-nu-thai"));
        try {
            assertThat(readFully(6, 0x7e, 0xfd, 0xfb, 0xff, 0xd8, 0x75), is("-16:08:04.010123"));
            assertThat(readFully(0, 0x86, 0x42, 0x04), is("100:08:04"));
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, originalLocale);
        }
    }
    
    @Test
    void assertReadNullTimeWithFraction() {
        columnDef.setColumnMeta(4);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readUnsignedMedium()).thenReturn(0x800000);
        when(byteBuf.readUnsignedShort()).thenReturn(0);
        assertThat(new MySQLTime2BinlogProtocolValue().read(columnDef, payload), is(MySQLTimeValueUtils.ZERO_OF_TIME));
        verify(byteBuf).readUnsignedShort();
    }
    
    private Object readFully(final int fractionalSecondsPrecision, final int... bytes) {
        ByteBuf buffer = Unpooled.buffer(bytes.length);
        for (int each : bytes) {
            buffer.writeByte(each);
        }
        columnDef.setColumnMeta(fractionalSecondsPrecision);
        Object result = new MySQLTime2BinlogProtocolValue().read(columnDef, new MySQLPacketPayload(buffer, StandardCharsets.UTF_8));
        assertThat(buffer.readableBytes(), is(0));
        return result;
    }
}
