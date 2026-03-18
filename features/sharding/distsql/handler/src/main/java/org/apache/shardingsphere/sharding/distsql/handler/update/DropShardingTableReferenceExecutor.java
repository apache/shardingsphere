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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingTableReferenceRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Drop sharding table reference executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class DropShardingTableReferenceExecutor implements DatabaseRuleDropExecutor<DropShardingTableReferenceRuleStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropShardingTableReferenceRuleStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkCurrentRuleConfiguration();
            checkToBeDroppedShardingTableReferenceRules(sqlStatement);
        }
    }
    
    private void checkCurrentRuleConfiguration() {
        ShardingSpherePreconditions.checkNotEmpty(rule.getConfiguration().getBindingTableGroups(), () -> new MissingRequiredRuleException("Sharding table reference", database.getName()));
    }
    
    private void checkToBeDroppedShardingTableReferenceRules(final DropShardingTableReferenceRuleStatement sqlStatement) {
        Collection<String> currentRuleNames = getCurrentShardingTableReferenceRuleNames();
        Collection<String> notExistedRuleNames = sqlStatement.getNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedRuleNames, () -> new MissingRequiredRuleException("Sharding table reference", database.getName(), notExistedRuleNames));
    }
    
    private Collection<String> getCurrentShardingTableReferenceRuleNames() {
        return rule.getConfiguration().getBindingTableGroups().stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toCollection(CaseInsensitiveSet::new));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropShardingTableReferenceRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration(each, ""));
        }
        return result;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingTableReferenceRuleStatement sqlStatement) {
        return !Collections.disjoint(getCurrentShardingTableReferenceRuleNames(), sqlStatement.getNames());
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<DropShardingTableReferenceRuleStatement> getType() {
        return DropShardingTableReferenceRuleStatement.class;
    }
}
