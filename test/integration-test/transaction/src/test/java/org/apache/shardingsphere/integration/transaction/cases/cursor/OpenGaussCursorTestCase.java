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

package org.apache.shardingsphere.integration.transaction.cases.cursor;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.command.CursorSQLCommand;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.test.integration.env.container.atomic.constants.AdapterContainerConstants;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * OpenGauss cursor transaction integration test.
 */
@TransactionTestCase(dbTypes = {TransactionTestConstants.OPENGAUSS}, adapters = {AdapterContainerConstants.PROXY}, scenario = "cursor")
@Slf4j
public final class OpenGaussCursorTestCase extends BaseTransactionTestCase {
    
    private final CursorSQLCommand cursorSQLCommand;
    
    public OpenGaussCursorTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
        this.cursorSQLCommand = loadCursorSQLCommand();
    }
    
    private CursorSQLCommand loadCursorSQLCommand() {
        return JAXB.unmarshal(Objects.requireNonNull(BaseITCase.class.getClassLoader().getResource("env/common/cursor-command.xml")), CursorSQLCommand.class);
    }
    
    @Override
    protected void beforeTest() throws SQLException {
        super.beforeTest();
        Connection connection = getDataSource().getConnection();
        executeWithLog(connection, "CREATE OR REPLACE VIEW t_order_view AS SELECT * FROM t_order;");
    }
    
    @Override
    public void executeTest() throws SQLException {
        Connection connection = getDataSource().getConnection();
        broadcastTableCursorTest(connection);
        broadcastTableCursorTest2(connection);
        broadcastAndSingleTablesCursorTest(connection);
        broadcastAndSingleTablesCursorTest2(connection);
        viewCursorTest(connection);
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
        fetchOver(connection);
        fetchOver(connection);
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
        fetchOver(connection);
        fetchOver(connection);
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
        fetchOver(connection);
        fetchOver(connection);
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
        fetchOver(connection);
        fetchOver(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void viewCursorTest(final Connection connection) throws SQLException {
        executeWithLog(connection, "start transaction;");
        executeWithLog(connection, cursorSQLCommand.getViewCursor());
        executeWithLog(connection, "close test;");
        executeWithLog(connection, cursorSQLCommand.getViewCursor());
        fetch(connection, 1);
        fetch(connection, 2);
        fetch(connection, 3);
        fetch(connection, 4);
        fetchOver(connection);
        fetchOver(connection);
        executeWithLog(connection, "rollback;");
    }
    
    private void fetch(final Connection connection, final int expectedId) throws SQLException {
        ResultSet resultSet = executeQueryWithLog(connection, "fetch test;");
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            assertThat(id, is(expectedId));
        }
    }
    
    private void fetchOver(final Connection connection) throws SQLException {
        ResultSet resultSet = executeQueryWithLog(connection, "fetch test;");
        while (resultSet.next()) {
            fail("Expected fetch nothing.");
        }
    }
}
