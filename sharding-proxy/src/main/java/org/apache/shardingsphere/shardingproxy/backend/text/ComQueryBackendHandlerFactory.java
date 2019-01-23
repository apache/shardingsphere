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
import org.apache.shardingsphere.core.parsing.SQLJudgeEngine;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.set.SetStatement;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.sctl.ShardingCTLSetBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.sctl.ShardingCTLShowBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UnicastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UseDatabaseBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

/**
 * Com query backend handler factory.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComQueryBackendHandlerFactory {
    
    private static final String SCTL_SET = "SCTL:SET";
    
    private static final String SCTL_SHOW = "SCTL:SHOW";
    
    private static final String SET_AUTOCOMMIT_1 = "SET AUTOCOMMIT=1";
    
    /**
     * Create new text protocol backend handler instance.
     *
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of text protocol backend handler
     */
    public static TextProtocolBackendHandler createTextProtocolBackendHandler(final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        Optional<TransactionOperationType> transactionOperationType = TransactionOperationType.getOperationType(sql.toUpperCase());
        if (transactionOperationType.isPresent()) {
            return new TransactionBackendHandler(transactionOperationType.get(), backendConnection);
        }
        if (sql.toUpperCase().contains(SET_AUTOCOMMIT_1) && backendConnection.getStateHandler().isInTransaction()) {
            return new TransactionBackendHandler(TransactionOperationType.COMMIT, backendConnection);
        }
        if (sql.toUpperCase().startsWith(SCTL_SET)) {
            return new ShardingCTLSetBackendHandler(sql, backendConnection);
        } else if (sql.toUpperCase().startsWith(SCTL_SHOW)) {
            return new ShardingCTLShowBackendHandler(sql, backendConnection);
        }
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        return SQLType.DAL == sqlStatement.getType()
                ? createDALBackendHandler(sqlStatement, sequenceId, sql, backendConnection, databaseType) : new QueryBackendHandler(sequenceId, sql, backendConnection, databaseType);
    }
    
    private static TextProtocolBackendHandler createDALBackendHandler(
            final SQLStatement sqlStatement, final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        if (sqlStatement instanceof SetStatement) {
            return new BroadcastBackendHandler(sequenceId, sql, backendConnection, databaseType);
        }
        if (sqlStatement instanceof UseStatement) {
            return new UseDatabaseBackendHandler((UseStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesBackendHandler();
        }
        return new UnicastBackendHandler(sequenceId, sql, backendConnection, databaseType);
    }
}
