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
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Abstract readwrite-splitting rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractReadwriteSplittingRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String databaseName, final T config) {
        checkDataSources(databaseName, getDataSources(config));
    }
    
    private void checkDataSources(final String databaseName, final Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources) {
        Collection<String> writeDataSourceNames = new HashSet<>();
        Collection<String> readDataSourceNames = new HashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : dataSources) {
            Preconditions.checkState(null != each.getStaticStrategy() || null != each.getDynamicStrategy(),
                    "No available readwrite-splitting rule configuration in database `%s`.", databaseName);
            Optional.ofNullable(each.getStaticStrategy()).ifPresent(optional -> checkStaticStrategy(databaseName, writeDataSourceNames, readDataSourceNames, optional));
        }
    }
    
    private void checkStaticStrategy(final String databaseName, final Collection<String> writeDataSourceNames,
                                     final Collection<String> readDataSourceNames, final StaticReadwriteSplittingStrategyConfiguration strategyConfig) {
        Preconditions.checkState(writeDataSourceNames.add(strategyConfig.getWriteDataSourceName()),
                "Can not config duplicate write dataSource `%s` in database `%s`.", strategyConfig.getWriteDataSourceName(), databaseName);
        Preconditions.checkState(readDataSourceNames.addAll(strategyConfig.getReadDataSourceNames()),
                "Can not config duplicate read dataSources `%s` in database `%s`.", strategyConfig.getReadDataSourceNames(), databaseName);
    }
    
    protected abstract Collection<ReadwriteSplittingDataSourceRuleConfiguration> getDataSources(T config);
}
