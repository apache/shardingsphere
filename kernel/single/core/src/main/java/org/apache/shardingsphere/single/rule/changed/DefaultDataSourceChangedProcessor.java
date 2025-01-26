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

package org.apache.shardingsphere.single.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.metadata.nodepath.SingleRuleNodePathProvider;
import org.apache.shardingsphere.single.rule.SingleRule;

/**
 * Default data source changed processor.
 */
public final class DefaultDataSourceChangedProcessor implements RuleItemConfigurationChangedProcessor<SingleRuleConfiguration, String> {
    
    @Override
    public String swapRuleItemConfiguration(final AlterRuleItem alterRuleItem, final String yamlContent) {
        return yamlContent;
    }
    
    @Override
    public SingleRuleConfiguration findRuleConfiguration(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().findSingleRule(SingleRule.class).map(SingleRule::getConfiguration).orElseGet(SingleRuleConfiguration::new);
    }
    
    @Override
    public void changeRuleItemConfiguration(final AlterRuleItem alterRuleItem, final SingleRuleConfiguration currentRuleConfig, final String toBeChangedItemConfig) {
        currentRuleConfig.setDefaultDataSource(toBeChangedItemConfig);
    }
    
    @Override
    public void dropRuleItemConfiguration(final DropRuleItem dropRuleItem, final SingleRuleConfiguration currentRuleConfig) {
        currentRuleConfig.setDefaultDataSource(null);
    }
    
    @Override
    public String getType() {
        return SingleRuleNodePathProvider.RULE_TYPE + "." + SingleRuleNodePathProvider.DEFAULT_DATA_SOURCE;
    }
}
