/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.set.SetStatement;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.backend.sctl.ShardingCTLSetBackendHandler;
import io.shardingsphere.shardingproxy.backend.sctl.ShardingCTLShowBackendHandler;
import io.shardingsphere.transaction.core.TransactionOperationType;

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
        if (SQLType.DCL == sqlStatement.getType() || sqlStatement instanceof SetStatement) {
            return new SchemaBroadcastBackendHandler(sequenceId, sql, backendConnection, databaseType, BackendHandlerFactory.getInstance());
        } else if (sqlStatement instanceof UseStatement) {
            return new UseSchemaBackendHandler((UseStatement) sqlStatement, backendConnection);
        } else if (sqlStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesBackendHandler();
        } else if (SQLType.DAL == sqlStatement.getType()) {
            return new UnicastSchemaBackendHandler(sequenceId, sql, backendConnection, BackendHandlerFactory.getInstance());
        } else {
            return BackendHandlerFactory.getInstance().newTextProtocolInstance(sequenceId, sql, backendConnection, DatabaseType.MySQL);
        }
    }
}
