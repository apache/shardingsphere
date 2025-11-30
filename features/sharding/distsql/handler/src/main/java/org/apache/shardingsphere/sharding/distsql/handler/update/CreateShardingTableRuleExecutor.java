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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Create sharding table rule executor.
 */
@Setter
public final class CreateShardingTableRuleExecutor implements DatabaseRuleCreateExecutor<CreateShardingTableRuleStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateShardingTableRuleStatement sqlStatement) {
        ShardingTableRuleStatementChecker.checkCreation(database, sqlStatement.getRules(), sqlStatement.isIfNotExists(), null == rule ? null : rule.getConfiguration());
        checkUniqueActualDataNodes(sqlStatement);
    }
    
    private void checkUniqueActualDataNodes(final CreateShardingTableRuleStatement sqlStatement) {
        if (null == rule) {
            ShardingTableRuleStatementChecker.checkToBeAddedDataNodes(ShardingTableRuleStatementConverter.convertDataNodes(sqlStatement.getRules()));
            return;
        }
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement);
            sqlStatement.getRules().removeIf(each -> duplicatedRuleNames.contains(each.getLogicTable()));
        }
        rule.getShardingRuleChecker().checkToBeAddedDataNodes(ShardingTableRuleStatementConverter.convertDataNodes(sqlStatement.getRules()), false);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingTableRuleStatement sqlStatement) {
        return ShardingTableRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateShardingTableRuleStatement sqlStatement) {
        Collection<String> currentShardingTables = null == rule ? Collections.emptyList() : getCurrentShardingTables();
        return sqlStatement.getRules().stream().map(AbstractTableRuleSegment::getLogicTable).filter(currentShardingTables::contains).collect(Collectors.toSet());
    }
    
    private Collection<String> getCurrentShardingTables() {
        Collection<String> result = new CaseInsensitiveSet<>();
        result.addAll(rule.getConfiguration().getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(rule.getConfiguration().getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<CreateShardingTableRuleStatement> getType() {
        return CreateShardingTableRuleStatement.class;
    }
}
