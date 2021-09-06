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

package org.apache.shardingsphere.sharding.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;

/**
 * Abstract sharding rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractShardingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String schemaName, final T config) {
        Preconditions.checkState(hasAvailableTableConfigurations(config), "No available sharding rule configurations in schema `%s`.", schemaName);
    }
    
    protected abstract boolean hasAvailableTableConfigurations(T config);
}
