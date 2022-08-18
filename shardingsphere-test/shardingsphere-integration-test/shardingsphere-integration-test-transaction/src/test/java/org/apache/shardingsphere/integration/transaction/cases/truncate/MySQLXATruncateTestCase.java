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

package org.apache.shardingsphere.integration.transaction.cases.truncate;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.integration.transaction.cases.base.BaseTransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.base.BaseTransactionITCase;
import org.apache.shardingsphere.integration.transaction.engine.base.TransactionTestCase;
import org.apache.shardingsphere.integration.transaction.engine.constants.TransactionTestConstants;
import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.fail;

/**
 * MySQL truncate XA transaction integration test.
 */
@Slf4j
@TransactionTestCase(dbTypes = {TransactionTestConstants.MYSQL}, adapters = {TransactionTestConstants.PROXY}, transactionTypes = {TransactionType.XA})
public final class MySQLXATruncateTestCase extends BaseTransactionTestCase {
    
    public MySQLXATruncateTestCase(final BaseTransactionITCase baseTransactionITCase, final DataSource dataSource) {
        super(baseTransactionITCase, dataSource);
    }
    
    @Override
    @SneakyThrows(SQLException.class)
    public void executeTest() {
        assertTruncateInMySQLXATransaction();
    }
    
    private void assertTruncateInMySQLXATransaction() throws SQLException {
        // TODO This test case may cause bad effects to other test cases in JDBC adapter
        prepare();
        Connection conn = getDataSource().getConnection();
        conn.setAutoCommit(false);
        assertAccountRowCount(conn, 8);
        try {
            conn.createStatement().execute("truncate account;");
            fail("Expect exception, but no exception report.");
        } catch (TableModifyInTransactionException ex) {
            log.info("Exception for expected in Proxy: {}", ex.getMessage());
        } catch (SQLException ex) {
            log.info("Exception for expected in JDBC: {}", ex.getMessage());
        } finally {
            conn.rollback();
            conn.close();
        }
    }
    
    private void prepare() throws SQLException {
        Connection conn = getDataSource().getConnection();
        executeWithLog(conn, "delete from account;");
        executeWithLog(conn, "insert into account(id, balance, transaction_id) values(1, 1, 1),(2, 2, 2),(3, 3, 3),(4, 4, 4),(5, 5, 5),(6, 6, 6),(7, 7, 7),(8, 8, 8);");
        conn.close();
    }
}
