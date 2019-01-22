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

package org.apache.shardingsphere.shardingproxy.backend;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.SQLType;
import org.apache.shardingsphere.core.parsing.SQLJudgeEngine;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import org.apache.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dal.set.SetStatement;
import org.apache.shardingsphere.shardingproxy.backend.handler.BackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.handler.BackendHandlerFactory;
import org.apache.shardingsphere.shardingproxy.backend.handler.SchemaBroadcastBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.handler.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.handler.SkipBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.handler.TransactionBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.handler.UnicastSchemaBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.handler.UseSchemaBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.sctl.ShardingCTLSetBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.sctl.ShardingCTLShowBackendHandler;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

/**
 * Com query backend handler factory.
 *
 * @author zhaojun
 */
public class ComQueryBackendHandlerFactory {
    
    private static final String SCTL_SET = "SCTL:SET";
    
    private static final String SCTL_SHOW = "SCTL:SHOW";
    
    private static final String SKIP_SQL = "SET AUTOCOMMIT=1";
    
    /**
     * Create new com query backend handler instance.
     *
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of backend handler
     */
    public static BackendHandler createBackendHandler(final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        Optional<TransactionOperationType> transactionOperationType = TransactionOperationType.getOperationType(sql.toUpperCase());
        if (transactionOperationType.isPresent()) {
            return new TransactionBackendHandler(transactionOperationType.get(), backendConnection);
        }
        if (sql.toUpperCase().startsWith(SCTL_SET)) {
            return new ShardingCTLSetBackendHandler(sql, backendConnection);
        } else if (sql.toUpperCase().startsWith(SCTL_SHOW)) {
            return new ShardingCTLShowBackendHandler(sql, backendConnection);
        } else if (sql.toUpperCase().contains(SKIP_SQL)) {
            return new SkipBackendHandler();
        }
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        if (sqlStatement instanceof SetStatement) {
            return new SchemaBroadcastBackendHandler(sequenceId, sql, backendConnection, databaseType);
        } else if (sqlStatement instanceof UseStatement) {
            return new UseSchemaBackendHandler((UseStatement) sqlStatement, backendConnection);
        } else if (sqlStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesBackendHandler();
        } else if (SQLType.DAL == sqlStatement.getType()) {
            return new UnicastSchemaBackendHandler(sequenceId, sql, backendConnection, BackendHandlerFactory.getInstance());
        } else {
            return BackendHandlerFactory.getInstance().newTextProtocolInstance(sequenceId, sql, backendConnection, databaseType);
        }
    }
}
