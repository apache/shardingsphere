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

package org.apache.shardingsphere.single.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.constant.SingleOrder;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Single rule configuration checker.
 */
public final class SingleRuleConfigurationChecker implements RuleConfigurationChecker<SingleRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final SingleRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        if (config.getDefaultDataSource().isPresent()) {
            Preconditions.checkArgument(dataSourceMap.containsKey(config.getDefaultDataSource().get()), "Can not find default dataSource %s in dataSourceMap.", config.getDefaultDataSource());
        }
    }
    
    @Override
    public int getOrder() {
        return SingleOrder.ORDER;
    }
    
    @Override
    public Class<SingleRuleConfiguration> getTypeClass() {
        return SingleRuleConfiguration.class;
    }
}
