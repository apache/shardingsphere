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

public final class BlobValueDecoderTest {
    
    @Test
    public void assertDecodeBlob() {
        byte[] value = new byte[(1 << 8) - 1];
        assertDecodeBlob(1, value, value);
        value = new byte[(1 << 16) - 1];
        assertDecodeBlob(2, value, value);
        value = new byte[(1 << 24) - 1];
        assertDecodeBlob(3, value, value);
        value = new byte[(1 << 32) - 1];
        assertDecodeBlob(4, value, value);
    }
    
    private void assertDecodeBlob(final int meta, final byte[] value, final byte[] expect) {
        ByteBuf byteBuf = Unpooled.buffer();
        DataTypesCodec.writeIntNLE(meta, value.length, byteBuf);
        byteBuf.writeBytes(value);
        byte[] actual = (byte[]) BlobValueDecoder.decodeBlob(meta, byteBuf);
        assertThat(actual.length, is(expect.length));
    }
}
