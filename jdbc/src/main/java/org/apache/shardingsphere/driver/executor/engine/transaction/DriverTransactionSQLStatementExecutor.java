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

package org.apache.shardingsphere.driver.executor.engine.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.savepoint.ShardingSphereSavepoint;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;

import java.sql.SQLException;

/**
 * Driver transaction statement executor.
 */
@RequiredArgsConstructor
public final class DriverTransactionSQLStatementExecutor {
    
    private final ShardingSphereConnection connection;
    
    /**
     * Decide whether to execute TCL statement.
     *
     * @param queryContext query context
     * @return whether to execute TCL statement or not
     */
    public boolean decide(final QueryContext queryContext) {
        if (!(queryContext.getSqlStatementContext().getSqlStatement() instanceof TCLStatement)) {
            return false;
        }
        SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
        return sqlStatement instanceof SavepointStatement || sqlStatement instanceof ReleaseSavepointStatement;
        // TODO support more TCL statements
    }
    
    /**
     * Execute TCL statement.
     *
     * @param sqlStatement SQL statement
     * @return whether to execute TCL statement or not
     * @throws SQLException SQL exception
     */
    public boolean execute(final TCLStatement sqlStatement) throws SQLException {
        if (sqlStatement instanceof SavepointStatement) {
            connection.setSavepoint(((SavepointStatement) sqlStatement).getSavepointName());
            return true;
        }
        if (sqlStatement instanceof ReleaseSavepointStatement) {
            connection.releaseSavepoint(new ShardingSphereSavepoint(((ReleaseSavepointStatement) sqlStatement).getSavepointName()));
            return true;
        }
        return false;
    }
}
