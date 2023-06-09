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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.StringUtil;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgreSQLAggregatedCommandPacketTest {
    
    @Test
    void testPostgreSqlIdentifierTag() {
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(new ArrayList<>());
        PostgreSQLIdentifierTag identifier = postgreSQLAggregatedCommandPacket.getIdentifier();
        assertEquals('?', identifier.getValue());
    }
    
    @Test
    void testPostgreSqlParseCommandPacket() {
        PostgreSQLPacketPayload payload = getPostgreSQLPacketPayload();
        PostgreSQLComParsePacket postgreSQLComParsePacket = new PostgreSQLComParsePacket(payload);
        List<PostgreSQLCommandPacket> packets = Collections.singletonList(postgreSQLComParsePacket);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    @Test
    void testMultiplePostgreSqlParseCommandPacket() {
        PostgreSQLPacketPayload payload = getPostgreSQLPacketPayload();
        PostgreSQLComParsePacket postgreSQLComParsePacket = new PostgreSQLComParsePacket(payload);
        PostgreSQLComParsePacket postgreSQLComParsePacket1 = new PostgreSQLComParsePacket(payload);
        List<PostgreSQLCommandPacket> packets = Arrays.asList(postgreSQLComParsePacket, postgreSQLComParsePacket1);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    @Test
    void testPostgreSqlExecCommandPacket() {
        PostgreSQLPacketPayload payload = getPostgreSQLPacketPayload();
        PostgreSQLComExecutePacket postgreSQLComExecutePacket = new PostgreSQLComExecutePacket(payload);
        List<PostgreSQLCommandPacket> packets = Collections.singletonList(postgreSQLComExecutePacket);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(0, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    @Test
    void testPostgreSqlBindCommandPacket() {
        PostgreSQLPacketPayload payload = getPostgreSQLPacketPayload();
        PostgreSQLComBindPacket postgreSQLComBindPacket = new PostgreSQLComBindPacket(payload);
        List<PostgreSQLCommandPacket> packets = Collections.singletonList(postgreSQLComBindPacket);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(0, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    @Test
    void testPostgreSqlBindAndParseCombinedCommandPacket() {
        PostgreSQLPacketPayload parsePayload = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload bindPayload = getPostgreSQLPacketPayload();
        PostgreSQLComBindPacket postgreSQLComBindPacket = new PostgreSQLComBindPacket(bindPayload);
        PostgreSQLComParsePacket postgreSQLComParsePacket = new PostgreSQLComParsePacket(parsePayload);
        List<PostgreSQLCommandPacket> packets = Arrays.asList(postgreSQLComBindPacket, postgreSQLComParsePacket);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(0, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(-1, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    @Test
    void testPostgreSqlBindAndExecCombinedCommandPacket() {
        PostgreSQLPacketPayload execPayload = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload execPayload1 = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload execPayload2 = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload bindPayload = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload bindPayload1 = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload bindPayload2 = getPostgreSQLPacketPayload();
        PostgreSQLComBindPacket postgreSQLComBindPacket = new PostgreSQLComBindPacket(bindPayload);
        PostgreSQLComBindPacket postgreSQLComBindPacket1 = new PostgreSQLComBindPacket(bindPayload1);
        PostgreSQLComBindPacket postgreSQLComBindPacket2 = new PostgreSQLComBindPacket(bindPayload2);
        PostgreSQLComExecutePacket postgreSQLComExecutePacket = new PostgreSQLComExecutePacket(execPayload);
        PostgreSQLComExecutePacket postgreSQLComExecutePacket1 = new PostgreSQLComExecutePacket(execPayload1);
        PostgreSQLComExecutePacket postgreSQLComExecutePacket2 = new PostgreSQLComExecutePacket(execPayload2);
        List<PostgreSQLCommandPacket> packets = Arrays.asList(postgreSQLComBindPacket, postgreSQLComBindPacket1, postgreSQLComBindPacket2,
                postgreSQLComExecutePacket, postgreSQLComExecutePacket1, postgreSQLComExecutePacket2);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(0, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(5, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    @Test
    void testPostgreSqlAllCombinedCommandPacket() {
        PostgreSQLPacketPayload execPayload = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload bindPayload = getPostgreSQLPacketPayload();
        PostgreSQLPacketPayload parsePayload = getPostgreSQLPacketPayload();
        PostgreSQLComBindPacket postgreSQLComBindPacket = new PostgreSQLComBindPacket(bindPayload);
        PostgreSQLComExecutePacket postgreSQLComExecutePacket = new PostgreSQLComExecutePacket(execPayload);
        PostgreSQLComParsePacket postgreSQLComParsePacket = new PostgreSQLComParsePacket(parsePayload);
        List<PostgreSQLCommandPacket> packets = Arrays.asList(postgreSQLComParsePacket, postgreSQLComExecutePacket, postgreSQLComBindPacket);
        PostgreSQLAggregatedCommandPacket postgreSQLAggregatedCommandPacket = new PostgreSQLAggregatedCommandPacket(packets);
        assertEquals(2, postgreSQLAggregatedCommandPacket.getFirstBindIndex());
        assertEquals(1, postgreSQLAggregatedCommandPacket.getLastExecuteIndex());
    }
    
    private static PostgreSQLPacketPayload getPostgreSQLPacketPayload() {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(StringUtil.decodeHexDump("000000000004010000002c0000000000000020001a9100000000000062696e6c6f672e3030303032394af65c24"));
        return new PostgreSQLPacketPayload(buffer, StandardCharsets.ISO_8859_1);
    }
}
