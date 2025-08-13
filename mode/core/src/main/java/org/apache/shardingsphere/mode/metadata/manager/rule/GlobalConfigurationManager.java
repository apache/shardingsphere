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

package org.apache.shardingsphere.mode.metadata.manager.rule;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.listener.StatisticsCollectJobCronUpdateListener;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Global configuration manager.
 */
@RequiredArgsConstructor
public final class GlobalConfigurationManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final MetaDataPersistFacade metaDataPersistFacade;
    
    /**
     * Alter global rule configuration.
     *
     * @param ruleConfig global rule configuration
     */
    public synchronized void alterGlobalRuleConfiguration(final RuleConfiguration ruleConfig) {
        if (null == ruleConfig) {
            return;
        }
        Collection<ShardingSphereRule> rules = removeGlobalRule(ruleConfig, metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules());
        rules.addAll(GlobalRulesBuilder.buildSingleRules(ruleConfig, metaDataContexts.getMetaData().getAllDatabases(), metaDataContexts.getMetaData().getProps()));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().clear();
        metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().addAll(rules);
        metaDataContexts.update(new ShardingSphereMetaData(metaDataContexts.getMetaData().getAllDatabases(),
                metaDataContexts.getMetaData().getGlobalResourceMetaData(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps()), metaDataPersistFacade);
    }
    
    @SneakyThrows(Exception.class)
    private Collection<ShardingSphereRule> removeGlobalRule(final RuleConfiguration ruleConfig, final Collection<ShardingSphereRule> rules) {
        Collection<ShardingSphereRule> result = new LinkedList<>();
        for (ShardingSphereRule each : rules) {
            if (!each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass())) {
                result.add(each);
                continue;
            }
            if (each instanceof AutoCloseable) {
                ((AutoCloseable) each).close();
            }
        }
        return result;
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        boolean isProxyMetaDataCollectorCronChanged = isProxyMetaDataCollectorCronChanged(props);
        metaDataContexts.update(new ShardingSphereMetaData(metaDataContexts.getMetaData().getAllDatabases(),
                metaDataContexts.getMetaData().getGlobalResourceMetaData(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props)), metaDataPersistFacade);
        if (isProxyMetaDataCollectorCronChanged) {
            for (StatisticsCollectJobCronUpdateListener each : ShardingSphereServiceLoader.getServiceInstances(StatisticsCollectJobCronUpdateListener.class)) {
                each.updated();
            }
        }
    }
    
    private boolean isProxyMetaDataCollectorCronChanged(final Properties props) {
        String currentValue = metaDataContexts.getMetaData().getTemporaryProps().getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON);
        String newValue = new TemporaryConfigurationProperties(props).getValue(TemporaryConfigurationPropertyKey.PROXY_META_DATA_COLLECTOR_CRON);
        return !currentValue.equalsIgnoreCase(newValue);
    }
}
