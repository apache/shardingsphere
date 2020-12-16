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

package org.apache.shardingsphere.proxy.backend.text.distsql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowDataSourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.DataSourcesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.RDLBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.util.Optional;

/**
 * DistSQL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLBackendHandlerFactory {
    
    /**
     * Create new instance of DistSQL backend handler.
     *
     * @param sqlStatement SQL statement
     * @param backendConnection backend connection
     * @return text protocol backend handler
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final SQLStatement sqlStatement, final BackendConnection backendConnection) {
        if (sqlStatement instanceof ShowRuleStatement) {
            return Optional.of(new RuleQueryBackendHandler((ShowRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowDataSourcesStatement) {
            return Optional.of(new DataSourcesQueryBackendHandler((ShowDataSourcesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof RDLStatement || sqlStatement instanceof CreateDatabaseStatement || sqlStatement instanceof DropDatabaseStatement) {
            return Optional.of(new RDLBackendHandler(sqlStatement, backendConnection));
        }
        return Optional.empty();
    }
}
