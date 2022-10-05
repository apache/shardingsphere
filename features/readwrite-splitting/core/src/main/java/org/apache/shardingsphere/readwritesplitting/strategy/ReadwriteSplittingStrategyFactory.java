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

package org.apache.shardingsphere.readwritesplitting.strategy;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.strategy.type.DynamicReadwriteSplittingStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.type.StaticReadwriteSplittingStrategy;

import java.util.Collection;
import java.util.Optional;

/**
 * Readwrite splitting strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingStrategyFactory {
    
    /**
     * Create new instance of readwrite splitting strategy.
     * 
     * @param readwriteSplittingConfig readwrite-splitting rule config
     * @param builtRules built rules
     * @return created instance
     */
    public static ReadwriteSplittingStrategy newInstance(final ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingConfig, final Collection<ShardingSphereRule> builtRules) {
        return null == readwriteSplittingConfig.getStaticStrategy()
                ? createDynamicReadwriteSplittingStrategy(readwriteSplittingConfig.getDynamicStrategy(), builtRules)
                : createStaticReadwriteSplittingStrategy(readwriteSplittingConfig.getStaticStrategy());
    }
    
    private static StaticReadwriteSplittingStrategy createStaticReadwriteSplittingStrategy(final StaticReadwriteSplittingStrategyConfiguration staticConfig) {
        return new StaticReadwriteSplittingStrategy(staticConfig.getWriteDataSourceName(), staticConfig.getReadDataSourceNames());
    }
    
    private static DynamicReadwriteSplittingStrategy createDynamicReadwriteSplittingStrategy(final DynamicReadwriteSplittingStrategyConfiguration dynamicConfig,
                                                                                             final Collection<ShardingSphereRule> builtRules) {
        Optional<ShardingSphereRule> dynamicDataSourceStrategy = builtRules.stream().filter(each -> each instanceof DynamicDataSourceContainedRule).findFirst();
        boolean allowWriteDataSourceQuery = Strings.isNullOrEmpty(dynamicConfig.getWriteDataSourceQueryEnabled()) ? Boolean.TRUE : Boolean.parseBoolean(dynamicConfig.getWriteDataSourceQueryEnabled());
        return new DynamicReadwriteSplittingStrategy(dynamicConfig.getAutoAwareDataSourceName(), allowWriteDataSourceQuery, (DynamicDataSourceContainedRule) dynamicDataSourceStrategy.get());
    }
}
