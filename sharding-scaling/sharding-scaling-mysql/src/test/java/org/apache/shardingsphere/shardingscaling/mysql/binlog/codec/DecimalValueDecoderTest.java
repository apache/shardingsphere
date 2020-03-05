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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DecimalValueDecoderTest {
    
    private int meta;
    
    @Before
    public void setUp() {
        meta = 14 << 8;
        meta += 4;
    }
    
    @Test
    public void assertDecodePositiveNewDecimal() {
        byte[] newDecimalBytes = ByteBufUtil.decodeHexDump("810DFB38D204D2");
        ByteBuf newDecimalByteBuf = Unpooled.buffer(newDecimalBytes.length);
        newDecimalByteBuf.writeBytes(newDecimalBytes);
        BigDecimal actual = (BigDecimal) DecimalValueDecoder.decodeNewDecimal(meta, newDecimalByteBuf);
        assertThat(actual.toString(), is("1234567890.1234"));
    }
    
    @Test
    public void assertDecodeNegativeNewDecimal() {
        byte[] newDecimalBytes = ByteBufUtil.decodeHexDump("7EF204C72DFB2D");
        ByteBuf newDecimalByteBuf = Unpooled.buffer(newDecimalBytes.length);
        newDecimalByteBuf.writeBytes(newDecimalBytes);
        BigDecimal actual = (BigDecimal) DecimalValueDecoder.decodeNewDecimal(meta, newDecimalByteBuf);
        assertThat(actual.toString(), is("-1234567890.1234"));

        newDecimalBytes = ByteBufUtil.decodeHexDump("7DFEFDB5CC2741EFDEBE4154FD52E7");
        newDecimalByteBuf = Unpooled.buffer(newDecimalBytes.length);
        newDecimalByteBuf.writeBytes(newDecimalBytes);
        actual = (BigDecimal) DecimalValueDecoder.decodeNewDecimal(32 << 8 | 6, newDecimalByteBuf);
        assertThat(actual.toString(), is("-33620554869842448557956779.175384"));
    }
}
