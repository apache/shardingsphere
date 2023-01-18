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

package org.apache.shardingsphere.test.e2e.transaction.cases.cursor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.BaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionBaseE2EIT;
import org.apache.shardingsphere.test.e2e.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.test.e2e.transaction.engine.command.CursorSQLCommand;
import org.apache.shardingsphere.test.e2e.transaction.engine.constants.TransactionTestConstants;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * OpenGauss cursor transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.OPENGAUSS}, adapters = TransactionTestConstants.PROXY, scenario = "cursor")
@Slf4j
public final class OpenGaussCursorTestCase extends BaseTransactionTestCase {
    
    private final CursorSQLCommand cursorSQLCommand;
    
    public OpenGaussCursorTestCase(final TransactionBaseE2EIT baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
        this.cursorSQLCommand = loadCursorSQLCommand();
    }
    
    private CursorSQLCommand loadCursorSQLCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(BaseE2EIT.class.getClassLoader().getResource("env/common/cursor-command.xml")), CursorSQLCommand.class);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        Connection connection = getDataSource().getConnection();
        assertTableRowCount(connection, "t_order", 4);
    }
    
    @Override
    public void executeTest() throws SQLException {
        Connection connection = getDataSource().getConnection();
        singleTableCursorTest(connection);
        singleTableCursorOrderByTest(connection);
        broadcastTableCursorTest(connection);
        broadcastTableCursorTest2(connection);
        broadcastAndSingleTablesCursorTest(connection);
        broadcastAndSingleTablesCursorTest2(connection);
        viewCursorTest(connection);
    }
    
    private void singleTableCursorTest(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getSingleTableCursor());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getSingleTableCursor());
        fetch(connection, 1);
        fetch(connection, 2);
        fetch(connection, 3);
        fetch(connection, 4);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void fetchOverTest(final Connection connection) throws SQLException {
        fetchOver(connection);
        fetchOver(connection);
        fetchForwardOver(connection);
        fetchForwardAllOver(connection);
        fetchForwardOver(connection);
    }
    
    private void singleTableCursorOrderByTest(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getSingleTableCursorOrderBy());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getSingleTableCursorOrderBy());
        fetch(connection, 4);
        fetch(connection, 3);
        fetch(connection, 2);
        fetch(connection, 1);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastTableCursorTest(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastTablesCursor());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastTablesCursor());
        fetch(connection, 10101);
        fetch(connection, 10102);
        fetch(connection, 10201);
        fetch(connection, 10202);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastTableCursorTest2(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastTablesCursor2());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastTablesCursor2());
        fetch(connection, 10101);
        fetch(connection, 10102);
        fetch(connection, 10201);
        fetch(connection, 10202);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastAndSingleTablesCursorTest(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastAndSingleTablesCursor());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastAndSingleTablesCursor());
        fetch(connection, 1);
        fetch(connection, 2);
        fetch(connection, 3);
        fetch(connection, 4);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void broadcastAndSingleTablesCursorTest2(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastTablesCursor2());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getBroadcastTablesCursor2());
        fetch(connection, 10101);
        fetch(connection, 10102);
        fetch(connection, 10201);
        fetch(connection, 10202);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void viewCursorTest(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getViewCursor());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getViewCursor());
        fetch(connection, 1);
        fetch(connection, 1);
        fetch(connection, 2);
        fetch(connection, 2);
        fetchOverTest(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void fetch(final Connection connection, final int expectedId) throws SQLException {
        ResultSet resultSet = executeQueryWithLog(connection, "fetch test;");
        if (resultSet.next()) {
            assertThat(resultSet.getInt("id"), is(expectedId));
        } else {
            fail("Expected has result.");
        }
    }
    
    private void fetchOver(final Connection connection) throws SQLException {
        assertFalse(executeQueryWithLog(connection, "fetch test;").next());
    }
    
    private void fetchForwardOver(final Connection connection) throws SQLException {
        assertFalse(executeQueryWithLog(connection, "fetch forward from test;").next());
    }
    
    private void fetchForwardAllOver(final Connection connection) throws SQLException {
        assertFalse(executeQueryWithLog(connection, "fetch forward all from test;").next());
    }
}
