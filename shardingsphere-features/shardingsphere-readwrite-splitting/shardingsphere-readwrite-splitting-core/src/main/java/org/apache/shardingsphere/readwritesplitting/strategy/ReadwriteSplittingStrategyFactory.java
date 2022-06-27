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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.strategy.DynamicDataSourceStrategy;
import org.apache.shardingsphere.infra.datasource.strategy.DynamicDataSourceStrategyFactory;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.ReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.strategy.type.DynamicReadwriteSplittingStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.type.StaticReadwriteSplittingStrategy;

import java.util.Optional;

/**
 * Readwrite splitting strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingStrategyFactory {
    
    /**
     * Create new instance of readwrite splitting strategy.
     * 
     * @param dataSourceStrategy data source strategy
     * @return created instance
     */
    public static ReadwriteSplittingStrategy newInstance(final ReadwriteSplittingStrategyConfiguration dataSourceStrategy) {
        return dataSourceStrategy instanceof StaticReadwriteSplittingStrategyConfiguration ? createStaticReadwriteSplittingStrategy((StaticReadwriteSplittingStrategyConfiguration) dataSourceStrategy)
                : createDynamicReadwriteSplittingStrategy((DynamicReadwriteSplittingStrategyConfiguration) dataSourceStrategy);
    }
    
    private static StaticReadwriteSplittingStrategy createStaticReadwriteSplittingStrategy(final StaticReadwriteSplittingStrategyConfiguration staticConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(staticConfig.getWriteDataSourceName()), "Write data source name is required.");
        Preconditions.checkArgument(!staticConfig.getReadDataSourceNames().isEmpty(), "Read data source names are required.");
        return new StaticReadwriteSplittingStrategy(staticConfig.getWriteDataSourceName(), staticConfig.getReadDataSourceNames());
    }
    
    private static DynamicReadwriteSplittingStrategy createDynamicReadwriteSplittingStrategy(final DynamicReadwriteSplittingStrategyConfiguration dynamicConfig) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dynamicConfig.getAutoAwareDataSourceName()), "Auto aware data source name is required.");
        Optional<DynamicDataSourceStrategy> dynamicDataSourceStrategy = DynamicDataSourceStrategyFactory.findInstance();
        Preconditions.checkArgument(dynamicDataSourceStrategy.isPresent(), "Dynamic data source strategy is required.");
        boolean allowWriteDataSourceQuery = dynamicConfig.isWriteDataSourceQueryEnabled();
        return new DynamicReadwriteSplittingStrategy(dynamicConfig.getAutoAwareDataSourceName(), dynamicConfig.isWriteDataSourceQueryEnabled(), dynamicDataSourceStrategy.get());
    }
}
