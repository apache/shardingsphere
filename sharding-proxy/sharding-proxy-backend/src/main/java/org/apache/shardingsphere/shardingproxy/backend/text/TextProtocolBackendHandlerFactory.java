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

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parse.SQLJudgeEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql.statement.ShowDatabasesStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql.statement.UseStatement;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UnicastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UseDatabaseBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.ShardingCTLBackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.SkipBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Text protocol backend handler factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProtocolBackendHandlerFactory {
    
    private static final Set<String> AUTO_COMMIT = new HashSet<>(Arrays.asList("SET AUTOCOMMIT=1", "SET @@SESSION.AUTOCOMMIT = ON"));
    
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
        // TODO use sql parser engine instead of string compare
        Optional<TransactionOperationType> transactionOperationType = TransactionOperationType.getOperationType(sql.toUpperCase());
        if (transactionOperationType.isPresent()) {
            return new TransactionBackendHandler(transactionOperationType.get(), backendConnection);
        }
        if (AUTO_COMMIT.contains(sql.toUpperCase())) {
            return backendConnection.getStateHandler().isInTransaction() ? new TransactionBackendHandler(TransactionOperationType.COMMIT, backendConnection) : new SkipBackendHandler();
        }
        SQLStatement sqlStatement = new SQLJudgeEngine(databaseType, sql).judge();
        return SQLType.DAL == sqlStatement.getType() ? createDALBackendHandler(sqlStatement, sql, backendConnection) : new QueryBackendHandler(sql, backendConnection);
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
