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

package org.apache.shardingsphere.database.protocol.opengauss.packet.command;

import org.apache.shardingsphere.database.protocol.packet.command.CommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

class OpenGaussCommandPacketTypeTest {
    
    @Test
    void assertValueOfBatchBindCommand() {
        assertThat(OpenGaussCommandPacketType.valueOf('U'), is(OpenGaussCommandPacketType.BATCH_BIND_COMMAND));
    }
    
    @Test
    void assertValueOfPostgreSQLCommand() {
        CommandPacketType actual = OpenGaussCommandPacketType.valueOf('Q');
        assertThat(actual, is(PostgreSQLCommandPacketType.SIMPLE_QUERY));
        assertThat(actual, isA(PostgreSQLCommandPacketType.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("isExtendedProtocolPacketTypeArguments")
    void assertIsExtendedProtocolPacketType(final String name, final CommandPacketType commandPacketType, final boolean expectedIsExtendedProtocolPacketType) {
        assertThat(OpenGaussCommandPacketType.isExtendedProtocolPacketType(commandPacketType), is(expectedIsExtendedProtocolPacketType));
    }
    
    private static Stream<Arguments> isExtendedProtocolPacketTypeArguments() {
        return Stream.of(
                Arguments.of("batch_bind_command", OpenGaussCommandPacketType.BATCH_BIND_COMMAND, true),
                Arguments.of("postgresql_extended_command", PostgreSQLCommandPacketType.PARSE_COMMAND, true),
                Arguments.of("postgresql_non_extended_command", PostgreSQLCommandPacketType.SIMPLE_QUERY, false));
    }
}
