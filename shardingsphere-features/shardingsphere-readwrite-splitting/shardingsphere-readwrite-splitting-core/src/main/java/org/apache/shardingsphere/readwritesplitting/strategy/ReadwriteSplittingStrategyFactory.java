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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.datasource.strategy.DynamicDataSourceStrategy;
import org.apache.shardingsphere.infra.datasource.strategy.DynamicDataSourceStrategyFactory;
import org.apache.shardingsphere.readwritesplitting.strategy.type.DynamicReadwriteSplittingStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.type.StaticReadwriteSplittingStrategy;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Readwrite splitting strategy factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingStrategyFactory {
    
    /**
     * Create new instance of readwrite splitting strategy.
     * 
     * @param type type of readwrite splitting strategy
     * @param props properties of readwrite splitting strategy
     * @return created instance
     */
    public static ReadwriteSplittingStrategy newInstance(final String type, final Properties props) {
        return "STATIC".equalsIgnoreCase(type) ? createStaticReadwriteSplittingStrategy(props) : createDynamicReadwriteSplittingStrategy(props);
    }
    
    private static StaticReadwriteSplittingStrategy createStaticReadwriteSplittingStrategy(final Properties props) {
        String writeDataSourceName = props.getProperty("write-data-source-name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(writeDataSourceName), "Write data source name is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(props.getProperty("read-data-source-names")), "Read data source names are required.");
        List<String> readDataSourceNames = Splitter.on(",").trimResults().splitToList(props.getProperty("read-data-source-names"));
        return new StaticReadwriteSplittingStrategy(writeDataSourceName, readDataSourceNames);
    }
    
    private static DynamicReadwriteSplittingStrategy createDynamicReadwriteSplittingStrategy(final Properties props) {
        String autoAwareDataSourceName = props.getProperty("auto-aware-data-source-name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(autoAwareDataSourceName), "Auto aware data source name is required.");
        Optional<DynamicDataSourceStrategy> dynamicDataSourceStrategy = DynamicDataSourceStrategyFactory.findInstance();
        Preconditions.checkArgument(dynamicDataSourceStrategy.isPresent(), "Dynamic data source strategy is required.");
        boolean allowWriteDataSourceQuery = Boolean.parseBoolean(props.getOrDefault("write-data-source-query-enabled", String.valueOf(Boolean.TRUE)).toString());
        return new DynamicReadwriteSplittingStrategy(autoAwareDataSourceName, allowWriteDataSourceQuery, dynamicDataSourceStrategy.get());
    }
}
