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

package org.apache.shardingsphere.test.e2e.operation.transaction.cases.cursor;

import org.apache.shardingsphere.test.e2e.operation.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionContainerComposer;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.command.CursorSQLCommand;
import org.apache.shardingsphere.test.e2e.operation.transaction.engine.constants.TransactionTestConstants;

import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * OpenGauss cursor transaction integration test.
 */
@TransactionTestCase(dbTypes = TransactionTestConstants.OPENGAUSS, adapters = TransactionTestConstants.PROXY, scenario = "cursor")
public final class OpenGaussCursorTestCase extends BaseTransactionTestCase {
    
    private final CursorSQLCommand cursorSQLCommand;
    
    public OpenGaussCursorTestCase(final TransactionTestCaseParameter testCaseParam) {
        super(testCaseParam);
        cursorSQLCommand = loadCursorSQLCommand();
    }
    
    private CursorSQLCommand loadCursorSQLCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("env/common/cursor-command.xml")), CursorSQLCommand.class);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        try (Connection connection = getDataSource().getConnection()) {
            assertTableRowCount(connection, "t_order", 4);
        }
    }
    
    @Override
    public void executeTest(final TransactionContainerComposer containerComposer) throws SQLException {
        // TODO fix #25236
        try (Connection connection = getDataSource().getConnection()) {
            singleTableCursorTest(connection);
            singleTableCursorOrderByTest(connection);
            broadcastTableCursorTest(connection);
            broadcastTableCursorTest2(connection);
            broadcastAndSingleTablesCursorTest(connection);
            broadcastAndSingleTablesCursorTest2(connection);
            viewCursorTest(connection);
        }
    }
    
    private void singleTableCursorTest(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getSingleTableCursor().getCursorName();
        String sql = cursorSQLCommand.getSingleTableCursor().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 1, cursorName);
        fetch(connection, 2, cursorName);
        fetch(connection, 3, cursorName);
        fetch(connection, 4, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void fetchOverTest(final Connection connection, final String cursorName) throws SQLException {
        fetchOver(connection, cursorName);
        fetchOver(connection, cursorName);
        fetchForwardOver(connection, cursorName);
        fetchForwardAllOver(connection, cursorName);
        fetchForwardOver(connection, cursorName);
    }
    
    private void singleTableCursorOrderByTest(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getSingleTableCursorOrderBy().getCursorName();
        String sql = cursorSQLCommand.getSingleTableCursorOrderBy().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 4, cursorName);
        fetch(connection, 3, cursorName);
        fetch(connection, 2, cursorName);
        fetch(connection, 1, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastTableCursorTest(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getBroadcastTablesCursor().getCursorName();
        String sql = cursorSQLCommand.getBroadcastTablesCursor().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 10101, cursorName);
        fetch(connection, 10102, cursorName);
        fetch(connection, 10201, cursorName);
        fetch(connection, 10202, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastTableCursorTest2(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getBroadcastTablesCursor2().getCursorName();
        String sql = cursorSQLCommand.getBroadcastTablesCursor2().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 10101, cursorName);
        fetch(connection, 10102, cursorName);
        fetch(connection, 10201, cursorName);
        fetch(connection, 10202, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastAndSingleTablesCursorTest(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getBroadcastAndSingleTablesCursor().getCursorName();
        String sql = cursorSQLCommand.getBroadcastAndSingleTablesCursor().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 1, cursorName);
        fetch(connection, 2, cursorName);
        fetch(connection, 3, cursorName);
        fetch(connection, 4, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastAndSingleTablesCursorTest2(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getBroadcastTablesCursor2().getCursorName();
        String sql = cursorSQLCommand.getBroadcastTablesCursor2().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 10101, cursorName);
        fetch(connection, 10102, cursorName);
        fetch(connection, 10201, cursorName);
        fetch(connection, 10202, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void viewCursorTest(final Connection connection) throws SQLException {
        String cursorName = cursorSQLCommand.getViewCursor().getCursorName();
        String sql = cursorSQLCommand.getViewCursor().getSql();
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, sql);
        executeWithLog(connection, String.format("close %s;", cursorName));
        executeWithLog(connection, sql);
        fetch(connection, 1, cursorName);
        fetch(connection, 1, cursorName);
        fetch(connection, 2, cursorName);
        fetch(connection, 2, cursorName);
        fetchOverTest(connection, cursorName);
        executeWithLog(connection, "rollback;");
    }
    
    private void fetch(final Connection connection, final int expectedId, final String cursorName) throws SQLException {
        ResultSet resultSet = executeQueryWithLog(connection, String.format("fetch %s;", cursorName));
        if (resultSet.next()) {
            assertThat(resultSet.getInt("id"), is(expectedId));
        } else {
            fail("Expected has result.");
        }
    }
    
    private void fetchOver(final Connection connection, final String cursorName) throws SQLException {
        assertFalse(executeQueryWithLog(connection, String.format("fetch %s;", cursorName)).next());
    }
    
    private void fetchForwardOver(final Connection connection, final String cursorName) throws SQLException {
        assertFalse(executeQueryWithLog(connection, String.format("fetch forward from %s;", cursorName)).next());
    }
    
    private void fetchForwardAllOver(final Connection connection, final String cursorName) throws SQLException {
        assertFalse(executeQueryWithLog(connection, String.format("fetch forward all from %s;", cursorName)).next());
    }
}
