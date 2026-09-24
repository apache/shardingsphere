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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary;

import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary.MySQLComStmtSendLongDataPacket;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.ServerPreparedStatementRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLServerPreparedStatementTest {
    
    private static final int MAX_LONG_DATA_LENGTH = 64 * 1024 * 1024;
    
    @Test
    void assertGetLongDataIndexesWhenEmpty() throws SQLException {
        assertTrue(createPreparedStatement(Collections.emptyList()).getLongDataIndexes().isEmpty());
    }
    
    @Test
    void assertGetLongDataIndexes() throws SQLException {
        MySQLServerPreparedStatement preparedStatement = createPreparedStatement(Collections.emptyList());
        sendLongData(preparedStatement, 0, new byte[0]);
        sendLongData(preparedStatement, 1, new byte[0]);
        assertThat(preparedStatement.getLongDataIndexes(), containsInAnyOrder(0, 1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getLongDataTypeArguments")
    void assertApplyLongData(final String name, final List<MySQLBinaryColumnType> columnTypes, final Object expectedValue) {
        MySQLServerPreparedStatement preparedStatement = createPreparedStatement(columnTypes);
        sendLongData(preparedStatement, 0, "foo_data".getBytes(StandardCharsets.UTF_8));
        List<Object> actualParams = new ArrayList<>(Collections.singletonList(null));
        preparedStatement.applyLongData(actualParams, StandardCharsets.UTF_8);
        assertThat(actualParams.get(0), is(expectedValue));
    }
    
    private static Stream<Arguments> getLongDataTypeArguments() {
        byte[] binaryValue = "foo_data".getBytes(StandardCharsets.UTF_8);
        return Stream.of(
                Arguments.of("string", Collections.singletonList(MySQLBinaryColumnType.STRING), "foo_data"),
                Arguments.of("var string", Collections.singletonList(MySQLBinaryColumnType.VAR_STRING), "foo_data"),
                Arguments.of("varchar", Collections.singletonList(MySQLBinaryColumnType.VARCHAR), "foo_data"),
                Arguments.of("blob", Collections.singletonList(MySQLBinaryColumnType.BLOB), binaryValue),
                Arguments.of("null", Collections.singletonList(MySQLBinaryColumnType.NULL), binaryValue),
                Arguments.of("unknown", Collections.emptyList(), binaryValue));
    }
    
    @Test
    void assertApplyMultipleChunks() {
        MySQLServerPreparedStatement preparedStatement = createPreparedStatement(Collections.singletonList(MySQLBinaryColumnType.BLOB));
        sendLongData(preparedStatement, 0, new byte[0]);
        sendLongData(preparedStatement, 0, "foo_".getBytes(StandardCharsets.UTF_8));
        sendLongData(preparedStatement, 0, new byte[0]);
        sendLongData(preparedStatement, 0, "bar_".getBytes(StandardCharsets.UTF_8));
        sendLongData(preparedStatement, 0, "data".getBytes(StandardCharsets.UTF_8));
        List<Object> actualParams = new ArrayList<>(Collections.singletonList(null));
        preparedStatement.applyLongData(actualParams, StandardCharsets.UTF_8);
        assertThat(actualParams.get(0), is("foo_bar_data".getBytes(StandardCharsets.UTF_8)));
    }
    
    @Test
    void assertDecodeCharacterAfterCombiningChunks() {
        MySQLServerPreparedStatement preparedStatement = createPreparedStatement(Collections.singletonList(MySQLBinaryColumnType.VAR_STRING));
        byte[] value = "中".getBytes(StandardCharsets.UTF_8);
        sendLongData(preparedStatement, 0, Arrays.copyOfRange(value, 0, 1));
        sendLongData(preparedStatement, 0, Arrays.copyOfRange(value, 1, value.length));
        List<Object> actualParams = new ArrayList<>(Collections.singletonList(null));
        preparedStatement.applyLongData(actualParams, StandardCharsets.UTF_8);
        assertThat(actualParams.get(0), is("中"));
    }
    
    @Test
    void assertAcceptMaximumLongData() throws SQLException {
        MySQLServerPreparedStatement preparedStatement = createPreparedStatement(Collections.emptyList());
        appendRepeatedLongData(preparedStatement, MAX_LONG_DATA_LENGTH / 1024 / 1024);
        assertThat(preparedStatement.getLongDataIndexes(), contains(0));
    }
    
    @Test
    void assertRejectTooLargeLongDataUntilClear() throws SQLException {
        MySQLServerPreparedStatement preparedStatement = createPreparedStatement(Collections.emptyList());
        appendRepeatedLongData(preparedStatement, MAX_LONG_DATA_LENGTH / 1024 / 1024);
        sendLongData(preparedStatement, 0, new byte[1]);
        sendLongData(preparedStatement, 1, "ignored".getBytes(StandardCharsets.UTF_8));
        SQLException actual = assertThrows(SQLException.class, preparedStatement::getLongDataIndexes);
        assertThat(actual.getErrorCode(), is(1105));
        assertThat(actual.getSQLState(), is(XOpenSQLState.GENERAL_ERROR.getValue()));
        assertThat(actual.getMessage(), is("Parameter of prepared statement which is set through mysql_send_long_data() is longer than 'max_allowed_packet' bytes"));
        assertThrows(SQLException.class, preparedStatement::getLongDataIndexes);
        preparedStatement.clearLongData();
        assertTrue(preparedStatement.getLongDataIndexes().isEmpty());
    }
    
    private void appendRepeatedLongData(final MySQLServerPreparedStatement preparedStatement, final int count) {
        byte[] chunk = new byte[1024 * 1024];
        for (int i = 0; i < count; i++) {
            sendLongData(preparedStatement, 0, chunk);
        }
    }
    
    private MySQLServerPreparedStatement createPreparedStatement(final List<MySQLBinaryColumnType> columnTypes) {
        MySQLServerPreparedStatement result = new MySQLServerPreparedStatement("INSERT INTO t VALUES (?)", mock(), new HintValueContext());
        result.getParameterColumnTypes().addAll(columnTypes);
        return result;
    }
    
    private void sendLongData(final MySQLServerPreparedStatement preparedStatement, final int paramIndex, final byte[] data) {
        MySQLComStmtSendLongDataPacket packet = mock(MySQLComStmtSendLongDataPacket.class);
        when(packet.getStatementId()).thenReturn(1);
        when(packet.getParamId()).thenReturn(paramIndex);
        when(packet.getData()).thenReturn(data);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ServerPreparedStatementRegistry registry = new ServerPreparedStatementRegistry();
        registry.addPreparedStatement(1, preparedStatement);
        when(connectionSession.getServerPreparedStatementRegistry()).thenReturn(registry);
        new MySQLComStmtSendLongDataExecutor(packet, connectionSession).execute();
    }
}
