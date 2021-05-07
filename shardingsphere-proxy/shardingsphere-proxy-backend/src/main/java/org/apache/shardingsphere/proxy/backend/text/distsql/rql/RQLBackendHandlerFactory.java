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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.DataSourcesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ReadwriteSplittingRuleQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.RuleQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingRuleQueryBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * RQL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RQLBackendHandlerFactory {
    
    /**
     * Create new instance of RDL backend handler.
     * 
     * @param sqlStatement SQL statement
     * @param backendConnection backend connection
     * @return RDL backend handler
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final SQLStatement sqlStatement, final BackendConnection backendConnection) {
        if (sqlStatement instanceof ShowRuleStatement) {
            String ruleType = ((ShowRuleStatement) sqlStatement).getRuleType();
            switch (ruleType.toUpperCase()) {
                case "SHARDING":
                    return Optional.of(new ShardingRuleQueryBackendHandler((ShowRuleStatement) sqlStatement, backendConnection));
                case "REPLICA_QUERY":
                    return Optional.of(new ReadwriteSplittingRuleQueryBackendHandler((ShowRuleStatement) sqlStatement, backendConnection));
                case "ENCRYPT":
                    return Optional.of(new RuleQueryBackendHandler((ShowRuleStatement) sqlStatement, backendConnection));
                case "SHADOW":
                    return Optional.of(new RuleQueryBackendHandler((ShowRuleStatement) sqlStatement, backendConnection));
                default:
                    throw new UnsupportedOperationException(ruleType);
            }
        }
        if (sqlStatement instanceof ShowResourcesStatement) {
            return Optional.of(new DataSourcesQueryBackendHandler((ShowResourcesStatement) sqlStatement, backendConnection));
        }
        return Optional.empty();
    }
}
