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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.query;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryHeartbeatsStatement;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Show database discovery heartbeat executor.
 */
public final class ShowDatabaseDiscoveryHeartbeatExecutor implements RQLExecutor<ShowDatabaseDiscoveryHeartbeatsStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowDatabaseDiscoveryHeartbeatsStatement sqlStatement) {
        DatabaseDiscoveryRule rule = database.getRuleMetaData().getSingleRule(DatabaseDiscoveryRule.class);
        DatabaseDiscoveryRuleConfiguration ruleConfig = (DatabaseDiscoveryRuleConfiguration) rule.getConfiguration();
        Iterator<Entry<String, DatabaseDiscoveryHeartBeatConfiguration>> data = ruleConfig.getDiscoveryHeartbeats().entrySet().iterator();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            Entry<String, DatabaseDiscoveryHeartBeatConfiguration> entry = data.next();
            result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getProps().toString()));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "props");
    }
    
    @Override
    public String getType() {
        return ShowDatabaseDiscoveryHeartbeatsStatement.class.getName();
    }
}
