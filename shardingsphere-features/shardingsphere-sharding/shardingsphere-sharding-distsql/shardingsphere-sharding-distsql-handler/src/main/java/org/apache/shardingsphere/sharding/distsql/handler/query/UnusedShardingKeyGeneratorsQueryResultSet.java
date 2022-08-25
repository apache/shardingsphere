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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.ShowUnusedShardingKeyGeneratorsStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Query result set for show unused sharding key generators.
 */
public final class UnusedShardingKeyGeneratorsQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Entry<String, AlgorithmConfiguration>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<ShardingRule> rule = database.getRuleMetaData().findSingleRule(ShardingRule.class);
        rule.ifPresent(optional -> getUnusedKeyGenerators((ShardingRuleConfiguration) optional.getConfiguration()));
    }
    
    private void getUnusedKeyGenerators(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> inUsedKeyGenerators = getUsedKeyGenerators(shardingRuleConfig);
        Map<String, AlgorithmConfiguration> map = new HashMap<>();
        for (Entry<String, AlgorithmConfiguration> each : shardingRuleConfig.getKeyGenerators().entrySet()) {
            if (!inUsedKeyGenerators.contains(each.getKey())) {
                map.put(each.getKey(), each.getValue());
            }
        }
        data = map.entrySet().iterator();
    }
    
    private Collection<String> getUsedKeyGenerators(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().stream().filter(each -> Objects.nonNull(each.getKeyGenerateStrategy())).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        shardingRuleConfig.getAutoTables().stream().filter(each -> Objects.nonNull(each.getKeyGenerateStrategy())).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        KeyGenerateStrategyConfiguration keyGenerateStrategy = shardingRuleConfig.getDefaultKeyGenerateStrategy();
        if (Objects.nonNull(keyGenerateStrategy) && !Strings.isNullOrEmpty(keyGenerateStrategy.getKeyGeneratorName())) {
            result.add(keyGenerateStrategy.getKeyGeneratorName());
        }
        return result;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "type", "props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return buildTableRowData(data.next());
    }
    
    private Collection<Object> buildTableRowData(final Entry<String, AlgorithmConfiguration> data) {
        Collection<Object> result = new LinkedList<>();
        result.add(data.getKey());
        result.add(data.getValue().getType());
        result.add(buildProps(data.getValue().getProps()));
        return result;
    }
    
    private Object buildProps(final Properties props) {
        return Objects.nonNull(props) ? PropertiesConverter.convert(props) : "";
    }
    
    @Override
    public String getType() {
        return ShowUnusedShardingKeyGeneratorsStatement.class.getName();
    }
}
