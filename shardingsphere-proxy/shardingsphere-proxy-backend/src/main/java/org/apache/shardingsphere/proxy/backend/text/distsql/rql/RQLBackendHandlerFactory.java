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
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowEncryptRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowShardingTableRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.DataSourcesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.DatabaseDiscoveryRulesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.EncryptRulesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ReadwriteSplittingRulesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingBindingTableRulesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingBroadcastTableRulesQueryBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingTableRulesQueryBackendHandler;
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
        if (sqlStatement instanceof ShowResourcesStatement) {
            return Optional.of(new DataSourcesQueryBackendHandler((ShowResourcesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowShardingBindingTableRulesStatement) {
            return Optional.of(new ShardingBindingTableRulesQueryBackendHandler((ShowShardingBindingTableRulesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowShardingBroadcastTableRulesStatement) {
            return Optional.of(new ShardingBroadcastTableRulesQueryBackendHandler((ShowShardingBroadcastTableRulesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowReadwriteSplittingRulesStatement) {
            return Optional.of(new ReadwriteSplittingRulesQueryBackendHandler((ShowReadwriteSplittingRulesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowDatabaseDiscoveryRulesStatement) {
            return Optional.of(new DatabaseDiscoveryRulesQueryBackendHandler((ShowDatabaseDiscoveryRulesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowEncryptRulesStatement) {
            return Optional.of(new EncryptRulesQueryBackendHandler((ShowEncryptRulesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof ShowShardingTableRulesStatement) {
            return Optional.of(new ShardingTableRulesQueryBackendHandler((ShowShardingTableRulesStatement) sqlStatement, backendConnection));
        }
        return Optional.empty();
    }
}
