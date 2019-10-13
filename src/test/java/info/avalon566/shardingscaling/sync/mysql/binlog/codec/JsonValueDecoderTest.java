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

package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JsonValueDecoderTest {

    @Test
    public void assertDecodeSmallJsonObject() {
        ByteBuf payload = Unpooled.buffer();
        payload.writeByte(JsonValueDecoder.JsonValueTypes.SMALL_JSON_OBJECT);
        // element count
        payload.writeShortLE(1);
        // size
        payload.writeShortLE(0);
        // key offset
        payload.writeShortLE(11);
        // key length
        payload.writeShortLE(4);
        // value type
        payload.writeByte(JsonValueDecoder.JsonValueTypes.INT32);
        // value offset
        payload.writeShortLE(15);
        // key
        payload.writeBytes("key1".getBytes());
        // value
        payload.writeIntLE(111);
        String actual = (String) JsonValueDecoder.decode(payload);
        assertThat(actual, is("{\"key1\":111}"));
    }
}
