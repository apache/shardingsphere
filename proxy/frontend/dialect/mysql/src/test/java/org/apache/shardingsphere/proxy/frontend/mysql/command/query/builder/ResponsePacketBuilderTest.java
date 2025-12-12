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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.builder;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResponsePacketBuilderTest {
    
    @Test
    void assertBuildQueryResponsePackets() {
        QueryHeader queryHeader1 = new QueryHeader("schema1", "table1", "columnLabel1", "columnName1", 5, "VARCHAR", 4, 6, false, true, false, true);
        QueryHeader queryHeader2 = new QueryHeader("schema2", "table2", "columnLabel2", "columnName2", 8, "VARCHAR", 7, 9, false, true, true, true);
        List<QueryHeader> queryHeaders = Arrays.asList(queryHeader1, queryHeader2);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(queryHeaders);
        Collection<DatabasePacket> actual = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, 255, 0);
        assertTrue(actual.stream().findAny().isPresent());
        assertThat(actual.stream().findAny().get(), anyOf(isA(MySQLFieldCountPacket.class), isA(MySQLColumnDefinition41Packet.class), isA(MySQLEofPacket.class)));
    }
    
    @Test
    void assertBuildUpdateResponsePackets() {
        UpdateResponseHeader updateResponseHeader = mock(UpdateResponseHeader.class);
        when(updateResponseHeader.getUpdateCount()).thenReturn(10L);
        when(updateResponseHeader.getLastInsertId()).thenReturn(100L);
        Collection<DatabasePacket> actual = ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader, 0);
        assertTrue(actual.stream().findAny().isPresent());
        MySQLOKPacket actualItem = (MySQLOKPacket) actual.stream().findAny().get();
        assertThat(actualItem, isA(MySQLOKPacket.class));
        assertThat(actualItem.getAffectedRows(), is(10L));
        assertThat(actualItem.getLastInsertId(), is(100L));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void assertBuildQueryResponsePacketsWithBinaryColumnType() {
        QueryHeader nonBinaryHeader = new QueryHeader("s", "t", "columnLabel1", "columnName1", 5, "VARCHAR", 1, 1, false, false, false, false);
        QueryHeader binaryHeader = new QueryHeader("s", "t", "columnLabel2", "columnName2", 8, "VARBINARY", 1, 1, false, false, false, false);
        List<QueryHeader> queryHeaders = Arrays.asList(nonBinaryHeader, binaryHeader);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(queryHeaders);
        List<DatabasePacket> actual = new ArrayList(ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, 255, 0));
        assertThat(actual.size(), is(4));
        byte[] actualNonBinaryData = new byte[48];
        actual.get(1).write(new MySQLPacketPayload(Unpooled.wrappedBuffer(actualNonBinaryData).writerIndex(0), StandardCharsets.UTF_8));
        assertThat(actualNonBinaryData[43] & 0x80, is(0));
        byte[] actualBinaryData = new byte[48];
        actual.get(2).write(new MySQLPacketPayload(Unpooled.wrappedBuffer(actualBinaryData).writerIndex(0), StandardCharsets.UTF_8));
        assertThat(actualBinaryData[43] & 0x80, is(0x80));
    }
}
