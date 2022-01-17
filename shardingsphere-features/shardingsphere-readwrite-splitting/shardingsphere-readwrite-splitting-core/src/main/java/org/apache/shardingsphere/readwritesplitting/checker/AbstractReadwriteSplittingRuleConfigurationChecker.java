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

package org.apache.shardingsphere.readwritesplitting.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;

/**
 * Abstract readwrite-splitting rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractReadwriteSplittingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String schemaName, final T config) {
        checkDataSources(schemaName, getDataSources(config));
    }
    
    private void checkDataSources(final String schemaName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources) {
        dataSources.forEach(each -> {
            Preconditions.checkState(!each.getType().isEmpty(), "No available readwrite-splitting rule configuration in schema `%s`.", schemaName);
            Preconditions.checkState(!each.getProps().isEmpty(), "No available readwrite-splitting rule configuration in schema `%s`.", schemaName);
        });
    }
    
    protected abstract Collection<ReadwriteSplittingDataSourceRuleConfiguration> getDataSources(T config);
}
