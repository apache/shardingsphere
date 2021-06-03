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

package org.apache.shardingsphere.sharding.rule.checker;

import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

/**
 * Abstract sharding rule configuration checker.
 * 
 * @param <T> rule configuration
 */
public abstract class AbstractShardingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    protected final boolean hasAvailableTableConfigurations(final AlgorithmProvidedShardingRuleConfiguration config) {
        return !config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy() || !config.getAutoTables().isEmpty();
    }
    
    protected final boolean hasAvailableTableConfigurations(final ShardingRuleConfiguration config) {
        return !config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy() || !config.getAutoTables().isEmpty();
    }
}
