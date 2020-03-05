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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DateAndTimeValueDecoderTest {
    
    @Test
    public void assertDecodeTime() {
        assertDecodeTime(0, DateAndTimeValueDecoder.ZERO_OF_TIME);
        assertDecodeTime(-8385959, "-838:59:59");
        assertDecodeTime(8385959, "838:59:59");
    }
    
    private void assertDecodeTime(final int value, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeMediumLE(value);
        String actual = (String) DateAndTimeValueDecoder.decodeTime(0, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeTime2() {
        assertDecodeTime2(0, 0x800000, 0, DateAndTimeValueDecoder.ZERO_OF_TIME);
        assertDecodeTime2(0, 4952325, 0, "-838:59:59");
        assertDecodeTime2(0, 11824891, 0, "838:59:59");
    }
    
    private void assertDecodeTime2(final int meta, final int value, final int value2, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeMedium(value);
        writeMillisecondValue(meta, value2, byteBuf);
        String actual = (String) DateAndTimeValueDecoder.decodeTime2(meta, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeTime2Millisecond() {
        assertDecodeTime2(1, 4952325, 0, "-838:59:59.0");
        assertDecodeTime2(1, 4952325, 166, "-838:59:58.9");
        assertDecodeTime2(1, 11824891, 0, "838:59:59.0");
        assertDecodeTime2(1, 11824890, 90, "838:59:58.9");
        assertDecodeTime2(2, 4952325, 0, "-838:59:59.00");
        assertDecodeTime2(2, 4952325, 157, "-838:59:58.99");
        assertDecodeTime2(2, 11824891, 0, "838:59:59.00");
        assertDecodeTime2(2, 11824890, 99, "838:59:58.99");
        assertDecodeTime2(3, 4952325, 0, "-838:59:59.000");
        assertDecodeTime2(3, 4952325, 55546, "-838:59:58.999");
        assertDecodeTime2(3, 11824891, 0, "838:59:59.000");
        assertDecodeTime2(3, 11824890, 9990, "838:59:58.999");
        assertDecodeTime2(4, 4952325, 0, "-838:59:59.0000");
        assertDecodeTime2(4, 4952325, 55537, "-838:59:58.9999");
        assertDecodeTime2(4, 11824891, 0, "838:59:59.0000");
        assertDecodeTime2(4, 11824890, 9999, "838:59:58.9999");
        assertDecodeTime2(5, 4952325, 0, "-838:59:59.00000");
        assertDecodeTime2(5, 4952325, 15777226, "-838:59:58.99999");
        assertDecodeTime2(5, 11824891, 0, "838:59:59.00000");
        assertDecodeTime2(5, 11824890, 999990, "838:59:58.99999");
        assertDecodeTime2(6, 4952325, 0, "-838:59:59.000000");
        assertDecodeTime2(6, 4952325, 15777217, "-838:59:58.999999");
        assertDecodeTime2(6, 11824891, 0, "838:59:59.000000");
        assertDecodeTime2(6, 11824890, 999999, "838:59:58.999999");
    }
    
    @Test
    public void assertDecodeDate() {
        assertDecodeDate(0, DateAndTimeValueDecoder.ZERO_OF_DATE);
        assertDecodeDate(512033, "1000-01-01");
        assertDecodeDate(5119903, "9999-12-31");
    }
    
    private void assertDecodeDate(final int value, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeMediumLE(value);
        String actual = (String) DateAndTimeValueDecoder.decodeDate(0, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeYear() {
        assertDecodeYear(0, "0000");
        assertDecodeYear(1, "1901");
        assertDecodeYear(255, "2155");
    }
    
    private void assertDecodeYear(final int value, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeMediumLE(value);
        String actual = (String) DateAndTimeValueDecoder.decodeYear(0, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeTimestamp() {
        assertDecodeTimestamp(0, DateAndTimeValueDecoder.DATETIME_OF_ZERO);
        assertDecodeTimestamp(1, "1970-01-01 00:00:01");
        assertDecodeTimestamp(2147483647, "2038-01-19 03:14:07");
    }
    
    private void assertDecodeTimestamp(final int value, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeIntLE(value);
        String actual = (String) DateAndTimeValueDecoder.decodeTimestamp(0, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeTimestamp2() {
        assertDecodeTimestamp2(0, 0, 0, DateAndTimeValueDecoder.DATETIME_OF_ZERO);
        assertDecodeTimestamp2(0, 1, 0, "1970-01-01 00:00:01");
        assertDecodeTimestamp2(0, 2147483647, 0, "2038-01-19 03:14:07");
    }
    
    private void assertDecodeTimestamp2(final int meta, final int value, final int value2, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(value);
        writeMillisecondValue(meta, value2, byteBuf);
        String actual = (String) DateAndTimeValueDecoder.decodeTimestamp2(meta, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeTimestamp2Millisecond() {
        assertDecodeTimestamp2(1, 1, 0, "1970-01-01 00:00:01.0");
        assertDecodeTimestamp2(1, 1, 90, "1970-01-01 00:00:01.9");
        assertDecodeTimestamp2(2, 1, 0, "1970-01-01 00:00:01.00");
        assertDecodeTimestamp2(2, 1, 99, "1970-01-01 00:00:01.99");
        assertDecodeTimestamp2(3, 1, 0, "1970-01-01 00:00:01.000");
        assertDecodeTimestamp2(3, 1, 9990, "1970-01-01 00:00:01.999");
        assertDecodeTimestamp2(4, 1, 0, "1970-01-01 00:00:01.0000");
        assertDecodeTimestamp2(4, 1, 9999, "1970-01-01 00:00:01.9999");
        assertDecodeTimestamp2(5, 1, 0, "1970-01-01 00:00:01.00000");
        assertDecodeTimestamp2(5, 1, 999990, "1970-01-01 00:00:01.99999");
        assertDecodeTimestamp2(6, 1, 0, "1970-01-01 00:00:01.000000");
        assertDecodeTimestamp2(6, 1, 999999, "1970-01-01 00:00:01.999999");
    }
    
    @Test
    public void assertDecodeDatetime() {
        assertDecodeDatetime(0, DateAndTimeValueDecoder.DATETIME_OF_ZERO);
        assertDecodeDatetime(10000101000000L, "1000-01-01 00:00:00");
        assertDecodeDatetime(99991231235959L, "9999-12-31 23:59:59");
    }
    
    private void assertDecodeDatetime(final long value, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeLongLE(value);
        String actual = (String) DateAndTimeValueDecoder.decodeDateTime(0, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeDatetime2() {
        assertDecodeDatetime2(0, 0x8000000000L, 0, DateAndTimeValueDecoder.DATETIME_OF_ZERO);
        assertDecodeDatetime2(0, 604286091264L, 0, "1000-01-01 00:00:00");
        assertDecodeDatetime2(0, 1095015300859L, 0, "9999-12-31 23:59:59");
    }
    
    private void assertDecodeDatetime2(final int meta, final long value, final int value2, final String expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        DataTypesCodec.writeInt5(value, byteBuf);
        writeMillisecondValue(meta, value2, byteBuf);
        String actual = (String) DateAndTimeValueDecoder.decodeDatetime2(meta, byteBuf);
        assertThat(actual, is(expect));
    }
    
    @Test
    public void assertDecodeDatetime2Millisecond() {
        assertDecodeDatetime2(1, 604286091264L, 0, "1000-01-01 00:00:00.0");
        assertDecodeDatetime2(1, 604286091264L, 90, "1000-01-01 00:00:00.9");
        assertDecodeDatetime2(2, 604286091264L, 0, "1000-01-01 00:00:00.00");
        assertDecodeDatetime2(2, 604286091264L, 99, "1000-01-01 00:00:00.99");
        assertDecodeDatetime2(3, 604286091264L, 0, "1000-01-01 00:00:00.000");
        assertDecodeDatetime2(3, 604286091264L, 9990, "1000-01-01 00:00:00.999");
        assertDecodeDatetime2(4, 604286091264L, 0, "1000-01-01 00:00:00.0000");
        assertDecodeDatetime2(4, 604286091264L, 9999, "1000-01-01 00:00:00.9999");
        assertDecodeDatetime2(5, 604286091264L, 0, "1000-01-01 00:00:00.00000");
        assertDecodeDatetime2(5, 604286091264L, 999990, "1000-01-01 00:00:00.99999");
        assertDecodeDatetime2(6, 604286091264L, 0, "1000-01-01 00:00:00.000000");
        assertDecodeDatetime2(6, 604286091264L, 999999, "1000-01-01 00:00:00.999999");
    }
    
    private void writeMillisecondValue(final int meta, final int value2, final ByteBuf byteBuf) {
        switch (meta) {
            case 0:
                break;
            case 1:
            case 2:
                byteBuf.writeByte(value2);
                break;
            case 3:
            case 4:
                byteBuf.writeShort(value2);
                break;
            case 5:
            case 6:
                byteBuf.writeMedium(value2);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
