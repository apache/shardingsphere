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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLAggregatedCommandPacketTest {
    
    @Mock
    private PostgreSQLComParsePacket parsePacket;
    
    @Mock
    private PostgreSQLComBindPacket bindPacket;
    
    @Mock
    private PostgreSQLComExecutePacket executePacket;
    
    @Test
    void assertNewInstanceWithMultipleParsePackets() {
        when(parsePacket.getStatementId()).thenReturn("parse_statement");
        PostgreSQLAggregatedCommandPacket actual = new PostgreSQLAggregatedCommandPacket(Arrays.asList(parsePacket, mock(PostgreSQLComParsePacket.class)));
        assertThat(actual.getPackets().size(), is(2));
        assertFalse(actual.isContainsBatchedStatements());
        assertThat(actual.getBatchPacketBeginIndex(), is(-1));
        assertThat(actual.getBatchPacketEndIndex(), is(-1));
    }
    
    @Test
    void assertNewInstanceWithDifferentStatementIdsInParseAndBind() {
        when(parsePacket.getStatementId()).thenReturn("parse_statement");
        when(bindPacket.getStatementId()).thenReturn("bind_statement");
        PostgreSQLAggregatedCommandPacket actual = new PostgreSQLAggregatedCommandPacket(Arrays.asList(parsePacket, bindPacket));
        assertThat(actual.getPackets().size(), is(2));
        assertFalse(actual.isContainsBatchedStatements());
        assertThat(actual.getBatchPacketBeginIndex(), is(1));
        assertThat(actual.getBatchPacketEndIndex(), is(-1));
    }
    
    @Test
    void assertNewInstanceWithDifferentPortalsInBindAndExecute() {
        when(bindPacket.getStatementId()).thenReturn("parse_statement");
        when(bindPacket.getPortal()).thenReturn("portal_1");
        when(executePacket.getPortal()).thenReturn("portal_2");
        PostgreSQLAggregatedCommandPacket actual = new PostgreSQLAggregatedCommandPacket(Arrays.asList(bindPacket, executePacket));
        assertThat(actual.getPackets().size(), is(2));
        assertFalse(actual.isContainsBatchedStatements());
        assertThat(actual.getBatchPacketBeginIndex(), is(0));
        assertThat(actual.getBatchPacketEndIndex(), is(1));
    }
    
    @Test
    void assertNewInstanceWithValidBatchPackets() {
        when(parsePacket.getStatementId()).thenReturn("parse_statement");
        when(bindPacket.getStatementId()).thenReturn("parse_statement");
        when(bindPacket.getPortal()).thenReturn("portal_1");
        when(executePacket.getPortal()).thenReturn("portal_1");
        PostgreSQLComBindPacket mockBindPacket2 = mock(PostgreSQLComBindPacket.class);
        PostgreSQLComBindPacket mockBindPacket3 = mock(PostgreSQLComBindPacket.class);
        PostgreSQLComExecutePacket mockExecutePacket2 = mock(PostgreSQLComExecutePacket.class);
        PostgreSQLComExecutePacket mockExecutePacket3 = mock(PostgreSQLComExecutePacket.class);
        when(mockBindPacket2.getStatementId()).thenReturn("parse_statement");
        when(mockBindPacket2.getPortal()).thenReturn("portal_1");
        when(mockBindPacket3.getStatementId()).thenReturn("parse_statement");
        when(mockBindPacket3.getPortal()).thenReturn("portal_1");
        when(mockExecutePacket2.getPortal()).thenReturn("portal_1");
        when(mockExecutePacket3.getPortal()).thenReturn("portal_1");
        PostgreSQLAggregatedCommandPacket actual = new PostgreSQLAggregatedCommandPacket(
                Arrays.asList(parsePacket, bindPacket, mockBindPacket2, mockBindPacket3, executePacket, mockExecutePacket2, mockExecutePacket3));
        assertThat(actual.getPackets().size(), is(7));
        assertTrue(actual.isContainsBatchedStatements());
        assertThat(actual.getBatchPacketBeginIndex(), is(1));
        assertThat(actual.getBatchPacketEndIndex(), is(6));
    }
    
    @Test
    void assertNewInstanceWithDifferentPortalsInBinds() {
        when(bindPacket.getStatementId()).thenReturn("parse_statement");
        when(bindPacket.getPortal()).thenReturn("portal_1");
        PostgreSQLComBindPacket mockBindPacket2 = mock(PostgreSQLComBindPacket.class);
        when(mockBindPacket2.getStatementId()).thenReturn("parse_statement");
        when(mockBindPacket2.getPortal()).thenReturn("portal_2");
        PostgreSQLAggregatedCommandPacket actual = new PostgreSQLAggregatedCommandPacket(Arrays.asList(bindPacket, mockBindPacket2));
        assertThat(actual.getPackets().size(), is(2));
        assertFalse(actual.isContainsBatchedStatements());
        assertThat(actual.getBatchPacketBeginIndex(), is(0));
        assertThat(actual.getBatchPacketEndIndex(), is(-1));
    }
    
    @Test
    void assertNewInstanceWithBindThenParseWithSameStatementId() {
        when(bindPacket.getStatementId()).thenReturn("parse_statement");
        when(bindPacket.getPortal()).thenReturn("portal_1");
        when(parsePacket.getStatementId()).thenReturn("parse_statement");
        PostgreSQLAggregatedCommandPacket actual = new PostgreSQLAggregatedCommandPacket(Arrays.asList(bindPacket, parsePacket));
        assertThat(actual.getPackets().size(), is(2));
        assertFalse(actual.isContainsBatchedStatements());
        assertThat(actual.getBatchPacketBeginIndex(), is(0));
        assertThat(actual.getBatchPacketEndIndex(), is(-1));
    }
    
    @Test
    void assertWrite() {
        assertDoesNotThrow(() -> new PostgreSQLAggregatedCommandPacket(Arrays.asList(bindPacket, parsePacket)).write(mock()));
    }
    
    @Test
    void assertGetIdentifier() {
        assertThat(new PostgreSQLAggregatedCommandPacket(Arrays.asList(bindPacket, parsePacket)).getIdentifier().getValue(), is('?'));
    }
}
