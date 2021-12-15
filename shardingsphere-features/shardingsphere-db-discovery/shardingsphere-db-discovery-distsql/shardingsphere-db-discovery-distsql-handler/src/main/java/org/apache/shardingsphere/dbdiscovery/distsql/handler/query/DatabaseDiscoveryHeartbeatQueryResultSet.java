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
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Result set for show database discovery heartbeat.
 */
public final class DatabaseDiscoveryHeartbeatQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Entry<String, DatabaseDiscoveryHeartBeatConfiguration>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Collection<DatabaseDiscoveryRuleConfiguration> ruleConfiguration = metaData.getRuleMetaData().findRuleConfiguration(DatabaseDiscoveryRuleConfiguration.class);
        data = ruleConfiguration.stream().map(DatabaseDiscoveryRuleConfiguration::getDiscoveryHeartbeats)
                .flatMap(each -> each.entrySet().stream()).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).entrySet().iterator();
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, DatabaseDiscoveryHeartBeatConfiguration> entry = data.next();
        return Arrays.asList(entry.getKey(), entry.getValue().getProps());
    }
    
    @Override
    public String getType() {
        return ShowDatabaseDiscoveryHeartbeatsStatement.class.getCanonicalName();
    }
}
