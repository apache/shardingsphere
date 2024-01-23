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

import com.google.common.base.Strings;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.type.rql.aware.DatabaseRuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.ShowUnusedShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show unused sharding key generator executor.
 */
@Setter
public final class ShowUnusedShardingKeyGeneratorExecutor implements DatabaseRuleAwareRQLExecutor<ShowUnusedShardingKeyGeneratorsStatement, ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowUnusedShardingKeyGeneratorsStatement sqlStatement) {
        Collection<String> inUsedKeyGenerators = getUsedKeyGenerators(rule.getConfiguration());
        return rule.getConfiguration().getKeyGenerators().entrySet().stream().filter(entry -> !inUsedKeyGenerators.contains(entry.getKey()))
                .map(entry -> new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), buildProps(entry.getValue().getProps()))).collect(Collectors.toList());
    }
    
    private Collection<String> getUsedKeyGenerators(final ShardingRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        ruleConfig.getTables().stream().filter(each -> null != each.getKeyGenerateStrategy()).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        ruleConfig.getAutoTables().stream().filter(each -> null != each.getKeyGenerateStrategy()).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        KeyGenerateStrategyConfiguration keyGenerateStrategy = ruleConfig.getDefaultKeyGenerateStrategy();
        if (null != keyGenerateStrategy && !Strings.isNullOrEmpty(keyGenerateStrategy.getKeyGeneratorName())) {
            result.add(keyGenerateStrategy.getKeyGeneratorName());
        }
        return result;
    }
    
    private String buildProps(final Properties props) {
        return null == props ? "" : PropertiesConverter.convert(props);
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<ShowUnusedShardingKeyGeneratorsStatement> getType() {
        return ShowUnusedShardingKeyGeneratorsStatement.class;
    }
}
