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

package org.apache.shardingsphere.sharding.distsql.handler.query;

import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowShardingTableRulesUsedKeyGeneratorStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Result set for show sharding table rules used key generator.
 */
public final class ShardingTableRulesUsedKeyGeneratorQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        ShowShardingTableRulesUsedKeyGeneratorStatement statement = (ShowShardingTableRulesUsedKeyGeneratorStatement) sqlStatement;
        List<Collection<Object>> result = new ArrayList<>();
        Collection<ShardingRuleConfiguration> shardingTableRules = metaData.getRuleMetaData().findRuleConfiguration(ShardingRuleConfiguration.class);
        shardingTableRules.forEach(each -> requireResult(statement, metaData.getName(), result, each));
        data = result.iterator();
    }
    
    private void requireResult(final ShowShardingTableRulesUsedKeyGeneratorStatement statement, final String schemaName, final List<Collection<Object>> result,
                               final ShardingRuleConfiguration shardingRuleConfiguration) {
        if (!statement.getKeyGeneratorName().isPresent()) {
            return;
        }
        shardingRuleConfiguration.getTables().forEach(each -> {
            if (null != each.getKeyGenerateStrategy() && statement.getKeyGeneratorName().get().equals(each.getKeyGenerateStrategy().getKeyGeneratorName())) {
                result.add(Arrays.asList(schemaName, "table", each.getLogicTable()));
            }
        });
        shardingRuleConfiguration.getAutoTables().forEach(each -> {
            if (null != each.getKeyGenerateStrategy() && statement.getKeyGeneratorName().get().equals(each.getKeyGenerateStrategy().getKeyGeneratorName())) {
                result.add(Arrays.asList(schemaName, "auto_table", each.getLogicTable()));
            }
        });
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("schema", "type", "name");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowShardingTableRulesUsedKeyGeneratorStatement.class.getName();
    }
}
