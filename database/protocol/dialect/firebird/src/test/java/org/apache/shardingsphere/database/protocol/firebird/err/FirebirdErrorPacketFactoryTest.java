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

package org.apache.shardingsphere.database.protocol.firebird.err;

import org.apache.shardingsphere.database.exception.core.exception.connection.AccessDeniedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.BatchTooBigException;
import org.apache.shardingsphere.database.exception.firebird.exception.protocol.InvalidBatchHandleException;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdErrorPacketFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("newInstanceArguments")
    void assertNewInstance(final String name, final Exception cause, final int expectedErrorCode, final String expectedErrorMessage) {
        FirebirdGenericResponsePacket actual = (FirebirdGenericResponsePacket) FirebirdErrorPacketFactory.newInstance(cause);
        assertThat(actual.getErrorCode(), is(expectedErrorCode));
        assertThat(actual.getErrorMessage(), is(expectedErrorMessage));
    }
    
    private static Stream<Arguments> newInstanceArguments() {
        return Stream.of(
                Arguments.of("unknown_exception", new RuntimeException("No reason"), 335544382, "Unknown exception." + System.lineSeparator() + "More details: java.lang.RuntimeException: No reason"),
                Arguments.of("sql_exception", new SQLException("Table not found", "42000", 335544374), 335544374, "Table not found"),
                Arguments.of("unknown_database_exception", new UnknownDatabaseException("logic_db"), 335544375, "logic_db"),
                Arguments.of("access_denied_exception", new AccessDeniedException("root", "127.0.0.1", true), 335544472, ""),
                Arguments.of("batch_too_big_exception", new BatchTooBigException(42, 1L, 8L, 8L), 335545198, ""),
                Arguments.of("invalid_batch_handle_exception", new InvalidBatchHandleException(42), 335545159, ""),
                Arguments.of("non_firebird_error_code", new SQLException("Invalid error", "HY000", 123), 335544382, "Invalid error"));
    }
}
