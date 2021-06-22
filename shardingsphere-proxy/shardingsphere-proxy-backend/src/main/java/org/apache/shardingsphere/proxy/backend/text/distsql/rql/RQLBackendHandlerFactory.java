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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.DataSourcesQueryResultSet;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.DatabaseDiscoveryRuleQueryResultSet;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.EncryptRuleQueryResultSet;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ReadwriteSplittingRuleQueryResultSet;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingBindingTableRuleQueryResultSet;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingBroadcastTableRuleQueryResultSet;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl.ShardingTableRuleQueryResultSet;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesStatement;

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
    public static TextProtocolBackendHandler newInstance(final RQLStatement sqlStatement, final BackendConnection backendConnection) {
        return new RuleQueryBackendHandler(sqlStatement, backendConnection, getRuleQueryResultSet(sqlStatement));
    }
    
    private static RuleQueryResultSet getRuleQueryResultSet(final RQLStatement sqlStatement) {
        if (sqlStatement instanceof ShowResourcesStatement) {
            return new DataSourcesQueryResultSet();
        }
        if (sqlStatement instanceof ShowShardingBindingTableRulesStatement) {
            return new ShardingBindingTableRuleQueryResultSet();
        }
        if (sqlStatement instanceof ShowShardingBroadcastTableRulesStatement) {
            return new ShardingBroadcastTableRuleQueryResultSet();
        }
        if (sqlStatement instanceof ShowReadwriteSplittingRulesStatement) {
            return new ReadwriteSplittingRuleQueryResultSet();
        }
        if (sqlStatement instanceof ShowDatabaseDiscoveryRulesStatement) {
            return new DatabaseDiscoveryRuleQueryResultSet();
        }
        if (sqlStatement instanceof ShowEncryptRulesStatement) {
            return new EncryptRuleQueryResultSet();
        }
        if (sqlStatement instanceof ShowShardingTableRulesStatement) {
            return new ShardingTableRuleQueryResultSet();
        }
        throw new UnsupportedOperationException(String.format("Cannot support SQL statement %s", sqlStatement.getClass().getCanonicalName()));
    }
}
