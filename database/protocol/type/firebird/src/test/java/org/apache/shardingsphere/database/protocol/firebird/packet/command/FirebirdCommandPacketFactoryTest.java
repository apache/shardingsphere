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

package org.apache.shardingsphere.database.protocol.firebird.packet.command;

import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.admin.FirebirdUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdAllocateStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFetchStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdFreeStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.FirebirdExecuteStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.FirebirdPrepareStatementPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdCommitTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdRollbackTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction.FirebirdStartTransactionPacket;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdCommandPacketFactoryTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private FirebirdPacketPayload payload;
    
    @Test
    void assertNewInstanceWithInfoDatabase() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.INFO_DATABASE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdInfoPacket.class));
    }
    
    @Test
    void assertNewInstanceWithTransaction() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.TRANSACTION, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdStartTransactionPacket.class));
    }
    
    @Test
    void assertNewInstanceWithAllocateStatement() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.ALLOCATE_STATEMENT, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdAllocateStatementPacket.class));
    }
    
    @Test
    void assertNewInstanceWithPrepareStatement() {
        when(payload.readInt4()).thenReturn(0, 0, 0, 0);
        when(payload.readString()).thenReturn("");
        when(payload.readBuffer()).thenReturn(org.mockito.Mockito.mock(io.netty.buffer.ByteBuf.class));
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.PREPARE_STATEMENT, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdPrepareStatementPacket.class));
    }
    
    @Test
    void assertNewInstanceWithExecuteStatement() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.EXECUTE, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdExecuteStatementPacket.class));
    }
    
    @Test
    void assertNewInstanceWithFetch() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.FETCH, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdFetchStatementPacket.class));
    }
    
    @Test
    void assertNewInstanceWithInfoSQL() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.INFO_SQL, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdInfoPacket.class));
    }
    
    @Test
    void assertNewInstanceWithCommit() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.COMMIT, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdCommitTransactionPacket.class));
    }
    
    @Test
    void assertNewInstanceWithRollback() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.ROLLBACK, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdRollbackTransactionPacket.class));
    }
    
    @Test
    void assertNewInstanceWithFreeStatement() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.FREE_STATEMENT, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdFreeStatementPacket.class));
    }
    
    @Test
    void assertNewInstanceWithUnsupportedCommand() {
        assertThat(FirebirdCommandPacketFactory.newInstance(FirebirdCommandPacketType.VOID, payload, FirebirdProtocolVersion.PROTOCOL_VERSION13),
                instanceOf(FirebirdUnsupportedCommandPacket.class));
    }
    
    @Test
    void assertIsValidLength() {
        assertThat(FirebirdCommandPacketFactory.isValidLength(FirebirdCommandPacketType.ALLOCATE_STATEMENT, payload,
                8, FirebirdProtocolVersion.PROTOCOL_VERSION13), CoreMatchers.is(true));
        assertThat(FirebirdCommandPacketFactory.isValidLength(FirebirdCommandPacketType.ALLOCATE_STATEMENT, payload,
                7, FirebirdProtocolVersion.PROTOCOL_VERSION13), CoreMatchers.is(false));
    }
}
