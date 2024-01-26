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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.DatabaseRuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowUnusedShardingAuditorsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Show unused sharding auditors executor.
 */
@Setter
public final class ShowUnusedShardingAuditorsExecutor implements DatabaseRuleAwareRQLExecutor<ShowUnusedShardingAuditorsStatement, ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowUnusedShardingAuditorsStatement sqlStatement) {
        ShardingRuleConfiguration shardingRuleConfig = rule.getConfiguration();
        Collection<String> inUsedAuditors = getUsedAuditors(shardingRuleConfig);
        return shardingRuleConfig.getAuditors().entrySet().stream().filter(entry -> !inUsedAuditors.contains(entry.getKey()))
                .map(entry -> new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), PropertiesConverter.convert(entry.getValue().getProps()))).collect(Collectors.toList());
    }
    
    private Collection<String> getUsedAuditors(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().stream().filter(each -> null != each.getAuditStrategy()).forEach(each -> result.addAll(each.getAuditStrategy().getAuditorNames()));
        shardingRuleConfig.getAutoTables().stream().filter(each -> null != each.getAuditStrategy()).forEach(each -> result.addAll(each.getAuditStrategy().getAuditorNames()));
        ShardingAuditStrategyConfiguration auditStrategy = shardingRuleConfig.getDefaultAuditStrategy();
        if (null != auditStrategy && !auditStrategy.getAuditorNames().isEmpty()) {
            result.addAll(auditStrategy.getAuditorNames());
        }
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowUnusedShardingAuditorsStatement> getType() {
        return ShowUnusedShardingAuditorsStatement.class;
    }
}
