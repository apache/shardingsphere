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
import org.apache.shardingsphere.proxy.backend.text.admin.DALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.RDLBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.query.QueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.ShardingCTLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.sctl.utils.SCTLUtils;
import org.apache.shardingsphere.proxy.backend.text.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.transaction.TransactionBackendHandlerFactory;
import org.apache.shardingsphere.distsql.parser.engine.ShardingSphereSQLStatementParserEngine;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

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
     * @return text protocol backend handler
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
        SQLStatement sqlStatement = new ShardingSphereSQLStatementParserEngine(databaseType.getName()).parse(sql, false);
        if (sqlStatement instanceof RDLStatement || sqlStatement instanceof CreateDatabaseStatement || sqlStatement instanceof DropDatabaseStatement) {
            return new RDLBackendHandler(backendConnection, sqlStatement);
        }
        if (sqlStatement instanceof TCLStatement) {
            return TransactionBackendHandlerFactory.newInstance(sql, (TCLStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof DALStatement) {
            return DALBackendHandlerFactory.newInstance(sql, (DALStatement) sqlStatement, backendConnection);
        }
        return new QueryBackendHandler(sql, sqlStatement, backendConnection);
    }
}
