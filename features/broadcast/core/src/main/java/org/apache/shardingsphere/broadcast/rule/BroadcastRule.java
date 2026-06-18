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

package org.apache.shardingsphere.broadcast.rule;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Getter;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.constant.BroadcastOrder;
import org.apache.shardingsphere.broadcast.rule.attribute.BroadcastDataNodeRuleAttribute;
import org.apache.shardingsphere.broadcast.rule.attribute.BroadcastTableNamesRuleAttribute;
import org.apache.shardingsphere.broadcast.rule.attribute.BroadcastUnregisterStorageUnitRuleAttribute;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Broadcast rule.
 */
@Getter
public final class BroadcastRule implements DatabaseRule {
    
    private final BroadcastRuleConfiguration configuration;
    
    private final Collection<String> dataSourceNames;
    
    private final Collection<String> tables;
    
    private final RuleAttributes attributes;
    
    public BroadcastRule(final BroadcastRuleConfiguration ruleConfig, final Map<String, DataSource> dataSources, final Collection<ShardingSphereRule> builtRules) {
        configuration = ruleConfig;
        Map<String, DataSource> aggregatedDataSources = new RuleMetaData(builtRules).findAttribute(AggregatedDataSourceRuleAttribute.class)
                .map(AggregatedDataSourceRuleAttribute::getAggregatedDataSources).orElseGet(() -> PhysicalDataSourceAggregator.getAggregatedDataSources(dataSources, builtRules));
        dataSourceNames = new CaseInsensitiveSet<>(aggregatedDataSources.keySet());
        tables = new CaseInsensitiveSet<>(ruleConfig.getTables());
        attributes = new RuleAttributes(new BroadcastDataNodeRuleAttribute(dataSourceNames, tables),
                new BroadcastTableNamesRuleAttribute(tables), new AggregatedDataSourceRuleAttribute(aggregatedDataSources), new BroadcastUnregisterStorageUnitRuleAttribute());
    }
    
    /**
     * Get broadcast table names.
     *
     * @param logicTableNames all logic table names
     * @return broadcast table names
     */
    @HighFrequencyInvocation
    public Collection<String> getBroadcastTableNames(final Collection<String> logicTableNames) {
        Collection<String> result = new CaseInsensitiveSet<>();
        for (String each : logicTableNames) {
            if (tables.contains(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    @Override
    public int getOrder() {
        return BroadcastOrder.ORDER;
    }
}
