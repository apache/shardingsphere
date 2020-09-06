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

package org.apache.shardingsphere.proxy.backend.text;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.admin.BroadcastBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.RDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.ShowTablesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.UnicastBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.UseDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.ShardingCTLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.sctl.utils.SCTLUtils;
import org.apache.shardingsphere.proxy.backend.text.transaction.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.transaction.TransactionBackendHandler;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngineFactory;
import org.apache.shardingsphere.rdl.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.transaction.core.TransactionOperationType;

/**
 * Text protocol backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProtocolBackendHandlerFactory {
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param databaseType database type
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @return instance of text protocol backend handler
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final String sql, final BackendConnection backendConnection) {
        if (Strings.isNullOrEmpty(sql)) {
            return new SkipBackendHandler();
        }
        // TODO Parse sctl SQL with ANTLR
        String trimSQL = SCTLUtils.trimComment(sql);
        if (trimSQL.toUpperCase().startsWith(ShardingCTLBackendHandlerFactory.SCTL)) {
            return ShardingCTLBackendHandlerFactory.newInstance(trimSQL, backendConnection);
        }
        SQLStatement sqlStatement = ShardingSphereSQLParserEngineFactory.getSQLParserEngine(databaseType.getName()).parse(sql, false);
        if (sqlStatement instanceof RDLStatement || sqlStatement instanceof CreateDatabaseStatement || sqlStatement instanceof DropDatabaseStatement) {
            return new RDLBackendHandler(backendConnection, sqlStatement);
        }
        if (sqlStatement instanceof TCLStatement) {
            return createTCLBackendHandler(sql, (TCLStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof DALStatement) {
            return createDALBackendHandler(sql, (DALStatement) sqlStatement, backendConnection);
        }
        return new QueryBackendHandler(sql, sqlStatement, backendConnection);
    }
    
    private static TextProtocolBackendHandler createTCLBackendHandler(final String sql, final TCLStatement tclStatement, final BackendConnection backendConnection) {
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
        return new BroadcastBackendHandler(sql, tclStatement, backendConnection);
    }
    
    private static TextProtocolBackendHandler createDALBackendHandler(final String sql, final DALStatement dalStatement, final BackendConnection backendConnection) {
        if (dalStatement instanceof UseStatement) {
            return new UseDatabaseBackendHandler((UseStatement) dalStatement, backendConnection);
        }
        if (dalStatement instanceof ShowDatabasesStatement) {
            return new ShowDatabasesBackendHandler(backendConnection);
        }
        if (dalStatement instanceof ShowTablesStatement) {
            return new ShowTablesBackendHandler(sql, dalStatement, backendConnection);
        }
        // FIXME: There are three SetStatement classes.
        if (dalStatement instanceof SetStatement) {
            return new BroadcastBackendHandler(sql, dalStatement, backendConnection);
        }
        return new UnicastBackendHandler(sql, dalStatement, backendConnection);
    }
}
