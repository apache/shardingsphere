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

package org.apache.shardingsphere.shardingjdbc.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShadowRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShadowDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Shadow data source factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowDataSourceFactory {
    
    /**
     * Create shadow data source.
     *
     * @param actualDataSource actual data source
     * @param shadowDataSource shadow data source
     * @param shadowRuleConfiguration shadow rule configuration
     * @param props properties
     * @return shadow data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final DataSource actualDataSource, final DataSource shadowDataSource,
                                              final ShadowRuleConfiguration shadowRuleConfiguration, final Properties props) throws SQLException {
        return new ShadowDataSource(actualDataSource, shadowDataSource, new ShadowRule(shadowRuleConfiguration), props);
    }
    
    /**
     * Create shadow data source.
     *
     * @param dataSourceMap data sources map
     * @param shadowRuleConfiguration shadow rule configuration
     * @param props properties
     * @return shadow data source
     * @throws SQLException SQL exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap,
                                              final ShadowRuleConfiguration shadowRuleConfiguration, final Properties props) throws SQLException {
        return new ShadowDataSource(createActualDataSource(dataSourceMap, shadowRuleConfiguration, props),
                createShadowDataSource(dataSourceMap, shadowRuleConfiguration, props), new ShadowRule(shadowRuleConfiguration), props);
    }
    
    private static DataSource createActualDataSource(final Map<String, DataSource> dataSourceMap, final ShadowRuleConfiguration shadowRule, final Properties props) {
        Map<String, DataSource> actualDataSource = shadowRule.getShadowMappings().entrySet().stream().collect(Collectors.toMap(Entry::getKey, each -> dataSourceMap.get(each.getKey())));
        return createFacadeDataSource(actualDataSource, shadowRule, props);
    }
    
    private static DataSource createShadowDataSource(final Map<String, DataSource> dataSourceMap, final ShadowRuleConfiguration shadowRule, final Properties props) {
        Map<String, DataSource> shadowDataSource = shadowRule.getShadowMappings().entrySet().stream().collect(Collectors.toMap(Entry::getKey, each -> dataSourceMap.get(each.getValue())));
        return createFacadeDataSource(shadowDataSource, shadowRule, props);
    }
    
    @SneakyThrows
    private static DataSource createFacadeDataSource(final Map<String, DataSource> dataSources, final ShadowRuleConfiguration shadowRule, final Properties props) {
        if (shadowRule.isEncrypt()) {
            return EncryptDataSourceFactory.createDataSource(dataSources.values().iterator().next(), shadowRule.getEncryptRuleConfig(), props);
        } else if (shadowRule.isSharding()) {
            return ShardingDataSourceFactory.createDataSource(dataSources, Collections.singletonList(shadowRule.getShardingRuleConfig()), props);
        } else if (shadowRule.isMasterSlave()) {
            return ShardingDataSourceFactory.createDataSource(dataSources, Collections.singletonList(shadowRule.getMasterSlaveRuleConfig()), props);
        } else {
            return dataSources.values().iterator().next();
        }
    }
}
