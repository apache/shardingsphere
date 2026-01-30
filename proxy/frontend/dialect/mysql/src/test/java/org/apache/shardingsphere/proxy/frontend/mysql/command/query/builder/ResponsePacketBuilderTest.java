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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.sql.Types;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResponsePacketBuilderTest {
    
    private static final int SESSION_CHARACTER_SET = MySQLCharacterSets.UTF8_GENERAL_CI.getId();
    
    @Test
    void assertBuildQueryResponsePacketsForNonBinaryColumn() {
        QueryHeader queryHeader = new QueryHeader("schema1", "table1", "columnLabel1", "columnName1", Types.INTEGER, "INTEGER", 6, 2, false, true, false, true);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(Collections.singletonList(queryHeader));
        Collection<DatabasePacket> actual = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, SESSION_CHARACTER_SET, 1);
        List<DatabasePacket> actualPackets = new ArrayList<>(actual);
        assertThat(actualPackets.size(), is(3));
        MySQLFieldCountPacket fieldCountPacket = (MySQLFieldCountPacket) actualPackets.get(0);
        assertThat(fieldCountPacket.getColumnCount(), is(1));
        MySQLColumnDefinition41Packet columnDefinitionPacket = (MySQLColumnDefinition41Packet) actualPackets.get(1);
        ByteBuf buffer = Unpooled.buffer();
        columnDefinitionPacket.write(new MySQLPacketPayload(buffer, StandardCharsets.UTF_8));
        buffer.readerIndex(0);
        MySQLPacketPayload payload = new MySQLPacketPayload(buffer, StandardCharsets.UTF_8);
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readIntLenenc();
        int actualCharacterSet = payload.readInt2();
        payload.readInt4();
        payload.readInt1();
        int actualFlags = payload.readInt2();
        int actualDecimals = payload.readInt1();
        int expectedFlags = MySQLColumnDefinitionFlag.PRIMARY_KEY.getValue()
                + MySQLColumnDefinitionFlag.UNSIGNED.getValue()
                + MySQLColumnDefinitionFlag.AUTO_INCREMENT.getValue();
        assertThat(actualCharacterSet, is(SESSION_CHARACTER_SET));
        assertThat(actualFlags, is(expectedFlags));
        assertThat(actualDecimals, is(queryHeader.getDecimals()));
        MySQLEofPacket eofPacket = (MySQLEofPacket) actualPackets.get(2);
        assertThat(eofPacket.getStatusFlags(), is(1));
    }
    
    @Test
    void assertBuildUpdateResponsePackets() {
        UpdateResponseHeader updateResponseHeader = mock(UpdateResponseHeader.class);
        when(updateResponseHeader.getUpdateCount()).thenReturn(10L);
        when(updateResponseHeader.getLastInsertId()).thenReturn(100L);
        Collection<DatabasePacket> actual = ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader, 3);
        List<DatabasePacket> actualPackets = new ArrayList<>(actual);
        assertThat(actualPackets.size(), is(1));
        MySQLOKPacket actualItem = (MySQLOKPacket) actualPackets.get(0);
        assertThat(actualItem, isA(MySQLOKPacket.class));
        assertThat(actualItem.getAffectedRows(), is(10L));
        assertThat(actualItem.getLastInsertId(), is(100L));
        assertThat(actualItem.getStatusFlag(), is(3));
    }
    
    @Test
    void assertBuildQueryResponsePacketsWithBinaryColumnType() {
        QueryHeader queryHeader = new QueryHeader("schema2", "table2", "columnLabel2", "columnName2", Types.BINARY, "BLOB", 9, 1, true, false, true, false);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(Collections.singletonList(queryHeader));
        Collection<DatabasePacket> actual = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader, SESSION_CHARACTER_SET, 2);
        List<DatabasePacket> actualPackets = new ArrayList<>(actual);
        assertThat(actualPackets.size(), is(3));
        MySQLColumnDefinition41Packet columnDefinitionPacket = (MySQLColumnDefinition41Packet) actualPackets.get(1);
        ByteBuf buffer = Unpooled.buffer();
        columnDefinitionPacket.write(new MySQLPacketPayload(buffer, StandardCharsets.UTF_8));
        buffer.readerIndex(0);
        MySQLPacketPayload payload = new MySQLPacketPayload(buffer, StandardCharsets.UTF_8);
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readIntLenenc();
        int actualCharacterSet = payload.readInt2();
        payload.readInt4();
        payload.readInt1();
        int actualFlags = payload.readInt2();
        int expectedFlags = MySQLColumnDefinitionFlag.NOT_NULL.getValue()
                + MySQLColumnDefinitionFlag.BINARY_COLLATION.getValue()
                + MySQLColumnDefinitionFlag.BLOB.getValue();
        assertThat(actualCharacterSet, is(MySQLCharacterSets.BINARY.getId()));
        assertThat(actualFlags, is(expectedFlags));
    }
}
