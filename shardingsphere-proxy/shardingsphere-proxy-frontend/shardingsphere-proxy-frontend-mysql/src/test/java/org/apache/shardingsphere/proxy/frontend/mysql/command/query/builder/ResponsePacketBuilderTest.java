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

import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.MySQLFieldCountPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLEofPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ResponsePacketBuilderTest {
    
    @Test
    public void assertBuildQueryResponsePackets() {
        QueryHeader queryHeader1 = new QueryHeader("schema1", "table1", "columnLabel1", "columnName1", 5, "VARCHAR", 4, 6, false, true, false, true);
        QueryHeader queryHeader2 = new QueryHeader("schema2", "table2", "columnLabel2", "columnName2", 8, "VARCHAR", 7, 9, false, true, true, true);
        List<QueryHeader> queryHeaders = Arrays.asList(queryHeader1, queryHeader2);
        QueryResponseHeader queryResponseHeader = new QueryResponseHeader(queryHeaders);
        Collection<DatabasePacket<?>> actual = ResponsePacketBuilder.buildQueryResponsePackets(queryResponseHeader);
        assertThat(actual.stream().findAny().get(), anyOf(instanceOf(MySQLFieldCountPacket.class), instanceOf(MySQLColumnDefinition41Packet.class), instanceOf(MySQLEofPacket.class)));
    }
    
    @Test
    public void assertBuildUpdateResponsePackets() {
        UpdateResponseHeader updateResponseHeader = mock(UpdateResponseHeader.class);
        when(updateResponseHeader.getUpdateCount()).thenReturn(10L);
        when(updateResponseHeader.getLastInsertId()).thenReturn(100L);
        Collection<DatabasePacket<?>> actual = ResponsePacketBuilder.buildUpdateResponsePackets(updateResponseHeader);
        MySQLOKPacket actualItem = (MySQLOKPacket) actual.stream().findAny().get();
        assertThat(actualItem, instanceOf(MySQLOKPacket.class));
        assertThat(actualItem.getAffectedRows(), is(10L));
        assertThat(actualItem.getLastInsertId(), is(100L));
    }
}
