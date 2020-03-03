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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class FieldPacketTest {
    
    private static final String CATALOG = "catalog";
    
    private static final String SCHEMA = "schema";
    
    private static final String TABLE = "table";
    
    private static final String ORIGINAL_TABLE = "original_table";
    
    private static final String FIELD_NAME = "field_name";
    
    private static final String ORIGIN_FIELD_NAME = "origin_field_name";
    
    private static final String DEFINITION = "definition";
    
    @Test
    public void assertFromByteBuf() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(CATALOG.length());
        byteBuf.writeBytes(CATALOG.getBytes());
        byteBuf.writeByte(SCHEMA.length());
        byteBuf.writeBytes(SCHEMA.getBytes());
        byteBuf.writeByte(TABLE.length());
        byteBuf.writeBytes(TABLE.getBytes());
        byteBuf.writeByte(ORIGINAL_TABLE.length());
        byteBuf.writeBytes(ORIGINAL_TABLE.getBytes());
        byteBuf.writeByte(FIELD_NAME.length());
        byteBuf.writeBytes(FIELD_NAME.getBytes());
        byteBuf.writeByte(ORIGIN_FIELD_NAME.length());
        byteBuf.writeBytes(ORIGIN_FIELD_NAME.getBytes());
        byteBuf.writeShortLE(Short.MIN_VALUE);
        byteBuf.writeIntLE(Integer.MIN_VALUE);
        byteBuf.writeByte(0x80);
        byteBuf.writeShortLE(Short.MIN_VALUE);
        byteBuf.writeByte(0x80);
        byteBuf.writeBytes(new byte[2]);
        byteBuf.writeByte(DEFINITION.length());
        byteBuf.writeBytes(DEFINITION.getBytes());
        FieldPacket actual = new FieldPacket();
        actual.fromByteBuf(byteBuf);
        assertThat(actual.getCatalog(), is(CATALOG));
        assertThat(actual.getSchema(), is(SCHEMA));
        assertThat(actual.getTable(), is(TABLE));
        assertThat(actual.getOriginalTable(), is(ORIGINAL_TABLE));
        assertThat(actual.getFieldName(), is(FIELD_NAME));
        assertThat(actual.getOriginalFieldName(), is(ORIGIN_FIELD_NAME));
        assertThat(actual.getCharacter(), is(32768));
        assertThat(actual.getLength(), is(2147483648L));
        assertThat(actual.getType(), is((short) 128));
        assertThat(actual.getFlags(), is(32768));
        assertThat(actual.getDecimals(), is((short) 128));
        assertThat(actual.getDefinition(), is(DEFINITION));
    }
}
