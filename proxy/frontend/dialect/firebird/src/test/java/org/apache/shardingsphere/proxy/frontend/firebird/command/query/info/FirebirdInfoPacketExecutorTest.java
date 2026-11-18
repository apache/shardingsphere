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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdInfoPacketExecutorTest {

    @Mock
    private FirebirdInfoPacket packet;

    @Mock
    private ConnectionSession connectionSession;

    @ParameterizedTest
    @MethodSource("infoCommands")
    void assertExecute(final FirebirdCommandPacketType commandType, final Class<?> expectedDataType) {
        if (FirebirdCommandPacketType.INFO_SQL == commandType) {
            when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(new ServerPreparedStatementRegistry());
        }
        Collection<DatabasePacket> responses = new FirebirdInfoPacketExecutor(commandType, packet, connectionSession).execute();
        assertThat(responses.size(), is(1));
        FirebirdGenericResponsePacket response = (FirebirdGenericResponsePacket) responses.iterator().next();
        assertInstanceOf(expectedDataType, response.getData());
    }

    private static Stream<Arguments> infoCommands() {
        return Stream.of(
                Arguments.of(FirebirdCommandPacketType.INFO_BLOB, FirebirdBlobInfoReturnPacket.class),
                Arguments.of(FirebirdCommandPacketType.INFO_DATABASE, FirebirdDatabaseInfoReturnPacket.class),
                Arguments.of(FirebirdCommandPacketType.INFO_SQL, FirebirdSQLInfoReturnPacket.class));
    }

    @Test
    void assertRejectsInvalidCommandType() {
        FirebirdInfoPacketExecutor executor = new FirebirdInfoPacketExecutor(FirebirdCommandPacketType.COMMIT, packet, connectionSession);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executor::execute);
        assertThat(exception.getMessage(), is("Invalid command type for FirebirdInfoPacketExecutor: COMMIT"));
    }
}
