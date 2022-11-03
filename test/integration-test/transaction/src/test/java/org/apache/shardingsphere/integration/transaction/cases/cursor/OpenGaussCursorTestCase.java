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
import org.junit.Assert;

import javax.sql.DataSource;
import javax.xml.bind.JAXB;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import static org.junit.Assert.fail;

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
        Connection conn = getDataSource().getConnection();
        executeWithLog(conn, "CREATE OR REPLACE VIEW t_order_view AS SELECT * FROM t_order;");
    }
    
    @Override
    public void executeTest() throws SQLException {
        Connection conn = getDataSource().getConnection();
        broadcastTableCursorTest(conn);
        broadcastTableCursorTest2(conn);
        broadcastAndSingleTablesCursorTest(conn);
        broadcastAndSingleTablesCursorTest2(conn);
        viewCursorTest(conn);
    }
    
    private void broadcastTableCursorTest(final Connection conn) throws SQLException {
        executeWithLog(conn, "start transaction;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastTablesCursor());
        executeWithLog(conn, "close test;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastTablesCursor());
        fetch(conn, 10101);
        fetch(conn, 10102);
        fetch(conn, 10201);
        fetch(conn, 10202);
        fetchOver(conn);
        fetchOver(conn);
        executeWithLog(conn, "rollback;");
    }
    
    private void broadcastTableCursorTest2(final Connection conn) throws SQLException {
        executeWithLog(conn, "start transaction;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastTablesCursor2());
        executeWithLog(conn, "close test;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastTablesCursor2());
        fetch(conn, 10101);
        fetch(conn, 10102);
        fetch(conn, 10201);
        fetch(conn, 10202);
        fetchOver(conn);
        fetchOver(conn);
        executeWithLog(conn, "rollback;");
    }
    
    private void broadcastAndSingleTablesCursorTest(final Connection conn) throws SQLException {
        executeWithLog(conn, "start transaction;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastAndSingleTablesCursor());
        executeWithLog(conn, "close test;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastAndSingleTablesCursor());
        fetch(conn, 1);
        fetch(conn, 2);
        fetch(conn, 3);
        fetch(conn, 4);
        fetchOver(conn);
        fetchOver(conn);
        executeWithLog(conn, "rollback;");
    }
    
    private void broadcastAndSingleTablesCursorTest2(final Connection conn) throws SQLException {
        executeWithLog(conn, "start transaction;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastTablesCursor2());
        executeWithLog(conn, "close test;");
        executeWithLog(conn, cursorSQLCommand.getBroadcastTablesCursor2());
        fetch(conn, 10101);
        fetch(conn, 10102);
        fetch(conn, 10201);
        fetch(conn, 10202);
        fetchOver(conn);
        fetchOver(conn);
        executeWithLog(conn, "rollback;");
    }
    
    private void viewCursorTest(final Connection conn) throws SQLException {
        executeWithLog(conn, "start transaction;");
        executeWithLog(conn, cursorSQLCommand.getViewCursor());
        executeWithLog(conn, "close test;");
        executeWithLog(conn, cursorSQLCommand.getViewCursor());
        fetch(conn, 1);
        fetch(conn, 2);
        fetch(conn, 3);
        fetch(conn, 4);
        fetchOver(conn);
        fetchOver(conn);
        executeWithLog(conn, "rollback;");
    }
    
    private void fetch(final Connection conn, final int expectedId) throws SQLException {
        ResultSet rs = executeQueryWithLog(conn, "fetch test;");
        while (rs.next()) {
            int id = rs.getInt("id");
            Assert.assertEquals(String.format("Expected fetch id is %s, actual is %s.", expectedId, id), expectedId, id);
        }
    }
    
    private void fetchOver(final Connection conn) throws SQLException {
        ResultSet rs = executeQueryWithLog(conn, "fetch test;");
        while (rs.next()) {
            fail("Expected fetch nothing.");
        }
    }
}
