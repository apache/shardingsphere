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

package org.apache.shardingsphere.shardingproxy.backend.text;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.parse.SQLParseEngine;
import org.apache.shardingsphere.core.parse.rule.registry.MasterSlaveParseRuleRegistry;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.CommitStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.core.parse.sql.statement.tcl.TCLStatement;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UnicastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UseDatabaseBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.ShardingCTLBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.SkipBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.util.Arrays;
import java.util.List;

/**
 * Text protocol backend handler factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProtocolBackendHandlerFactory {
    
    private static final List<String> GUI_SQL = Arrays.asList("SET", "SHOW VARIABLES LIKE", "SHOW CHARACTER SET", "SHOW COLLATION");
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param databaseType database type
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @return instance of text protocol backend handler
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final String sql, final BackendConnection backendConnection) {
        if (sql.toUpperCase().startsWith(ShardingCTLBackendHandlerFactory.SCTL)) {
            return ShardingCTLBackendHandlerFactory.newInstance(sql, backendConnection);
        }
        SQLStatement sqlStatement = new SQLParseEngine(MasterSlaveParseRuleRegistry.getInstance(), databaseType, sql, null, null).parse();
        if (sqlStatement instanceof TCLStatement) {
            return createTCLBackendHandler((TCLStatement) sqlStatement, backendConnection);
        }
        return sqlStatement instanceof DALStatement ? createDALBackendHandler(sqlStatement, sql, backendConnection) : new QueryBackendHandler(sql, backendConnection);
    }
    
    private static TextProtocolBackendHandler createTCLBackendHandler(final TCLStatement tclStatement, final BackendConnection backendConnection) {
        if (tclStatement instanceof BeginTransactionStatement) {
            return new TransactionBackendHandler(TransactionOperationType.BEGIN, backendConnection);
        }
        if (tclStatement instanceof SetAutoCommitStatement) {
            if (((SetAutoCommitStatement) tclStatement).isAutoCommit()) {
                return backendConnection.getStateHandler().isInTransaction() ? new TransactionBackendHandler(TransactionOperationType.COMMIT, backendConnection) : new SkipBackendHandler();
            }
            return new TransactionBackendHandler(TransactionOperationType.BEGIN, backendConnection);
        }
        if (tclStatement instanceof CommitStatement) {
            return new TransactionBackendHandler(TransactionOperationType.COMMIT, backendConnection);
        }
        if (tclStatement instanceof RollbackStatement) {
            return new TransactionBackendHandler(TransactionOperationType.ROLLBACK, backendConnection);
        }
        return new BroadcastBackendHandler(tclStatement.getLogicSQL(), backendConnection);
    }
    
    private static TextProtocolBackendHandler createDALBackendHandler(final SQLStatement sqlStatement, final String sql, final BackendConnection backendConnection) {
        // TODO we should refactor the broadcast logic in future, exclude those broadcast SQL temporary.
        for (String each : GUI_SQL) {
            if (sql.toUpperCase().startsWith(each)) {
                return new BroadcastBackendHandler(sql, backendConnection);
            }
        }
        if (sqlStatement instanceof UseStatement) {
            return new UseDatabaseBackendHandler((UseStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesBackendHandler(backendConnection);
        }
        return new UnicastBackendHandler(sql, backendConnection);
    }
}
