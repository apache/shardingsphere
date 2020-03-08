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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TableMapEventPacketTest {
    
    private static final String TEST_SCHEMA = "test_schema";
    
    private static final String TEST_TABLE = "test_table";
    
    @Test
    public void assertParsePostHeader() {
        ByteBuf headerBuffer = Unpooled.buffer(8);
        headerBuffer.writeBytes(new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06});
        headerBuffer.writeShortLE(0);
        TableMapEventPacket actual = new TableMapEventPacket();
        actual.parsePostHeader(headerBuffer);
        assertThat(actual.getTableId(), is(0x0000060504030201L));
        assertThat(actual.getFlags(), is(0));
    }
    
    @Test
    public void assertParsePayload() {
        byte[] schemaBytes = TEST_SCHEMA.getBytes();
        byte[] tableBytes = TEST_TABLE.getBytes();
        int bitmapSize = (4 + 8) / 7;
        ByteBuf payload = Unpooled.buffer(1 + schemaBytes.length + 1 + 1 + tableBytes.length + 1 + 5 + 6 + bitmapSize);
        payload.writeByte(schemaBytes.length);
        payload.writeBytes(schemaBytes);
        payload.writeByte(0x00);
        payload.writeByte(tableBytes.length);
        payload.writeBytes(tableBytes);
        payload.writeByte(0x00);
        payload.writeByte(0x04);
        payload.writeByte(ColumnTypes.MYSQL_TYPE_INT24);
        payload.writeByte(ColumnTypes.MYSQL_TYPE_DATETIME2);
        payload.writeByte(ColumnTypes.MYSQL_TYPE_NEWDECIMAL);
        payload.writeByte(ColumnTypes.MYSQL_TYPE_VARCHAR);
        payload.writeByte(0x04);
        payload.writeByte(0x01);
        payload.writeShort(2);
        payload.writeShortLE(3);
        payload.writeBytes(new byte[bitmapSize]);
        TableMapEventPacket actual = new TableMapEventPacket();
        actual.parsePayload(payload);
        assertThat(actual.getSchemaName(), is(TEST_SCHEMA));
        assertThat(actual.getTableName(), is(TEST_TABLE));
        ColumnDef[] actualColumnDefs = actual.getColumnDefs();
        assertThat(actualColumnDefs.length, is(4));
        assertThat(actualColumnDefs[0].getType(), is(ColumnTypes.MYSQL_TYPE_INT24));
        assertThat(actualColumnDefs[1].getType(), is(ColumnTypes.MYSQL_TYPE_DATETIME2));
        assertThat(actualColumnDefs[2].getType(), is(ColumnTypes.MYSQL_TYPE_NEWDECIMAL));
        assertThat(actualColumnDefs[3].getType(), is(ColumnTypes.MYSQL_TYPE_VARCHAR));
        assertThat(actualColumnDefs[0].getMeta(), is(0));
        assertThat(actualColumnDefs[1].getMeta(), is(1));
        assertThat(actualColumnDefs[2].getMeta(), is(2));
        assertThat(actualColumnDefs[3].getMeta(), is(3));
    }
}
