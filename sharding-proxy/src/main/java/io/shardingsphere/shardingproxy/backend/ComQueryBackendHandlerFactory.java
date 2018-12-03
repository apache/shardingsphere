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

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.parsing.SQLJudgeEngine;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.SetStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowDatabasesStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.ShowOtherStatement;
import io.shardingsphere.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;

/**
 * Com query backend handler factory.
 *
 * @author zhaojun
 */
public class ComQueryBackendHandlerFactory {
    
    /**
     * Create new com query backend handler instance by SQL judge.
     *
     * @param sequenceId sequence ID of SQL packet
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @param databaseType database type
     * @return instance of backend handler
     */
    public static BackendHandler createBackendHandler(final int sequenceId, final String sql, final BackendConnection backendConnection, final DatabaseType databaseType) {
        SQLStatement sqlStatement = new SQLJudgeEngine(sql).judge();
        if (SQLType.DCL == sqlStatement.getType() || sqlStatement instanceof SetStatement) {
            return new SchemaBroadcastBackendHandler(sequenceId, sql, backendConnection, databaseType);
        }
        if (sqlStatement instanceof UseStatement || sqlStatement instanceof ShowDatabasesStatement) {
            return new SchemaIgnoreBackendHandler(sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof ShowOtherStatement) {
            return new SchemaUnicastBackendHandler(sequenceId, sql, backendConnection, DatabaseType.MySQL);
        }
        return BackendHandlerFactory.newTextProtocolInstance(sequenceId, sql, backendConnection, DatabaseType.MySQL);
    }
}
