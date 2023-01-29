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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryTypesStatement;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * Show database discovery type executor.
 */
public final class ShowDatabaseDiscoveryTypeExecutor implements RQLExecutor<ShowDatabaseDiscoveryTypesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowDatabaseDiscoveryTypesStatement sqlStatement) {
        DatabaseDiscoveryRule rule = database.getRuleMetaData().getSingleRule(DatabaseDiscoveryRule.class);
        Iterator<Entry<String, AlgorithmConfiguration>> data = ((DatabaseDiscoveryRuleConfiguration) rule.getConfiguration()).getDiscoveryTypes().entrySet().iterator();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            Entry<String, AlgorithmConfiguration> entry = data.next();
            result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), entry.getValue().getProps()));
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    @Override
    public String getType() {
        return ShowDatabaseDiscoveryTypesStatement.class.getName();
    }
}
