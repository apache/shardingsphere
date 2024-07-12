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

package org.apache.shardingsphere.mode.metadata.manager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.mode.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global configuration manager.
 */
@Slf4j
public final class GlobalConfigurationManager {
    
    private final AtomicReference<MetaDataContexts> metaDataContexts;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public GlobalConfigurationManager(final AtomicReference<MetaDataContexts> metaDataContexts, final PersistRepository repository) {
        this.metaDataContexts = metaDataContexts;
        metaDataPersistService = new MetaDataPersistService(repository);
    }
    
    /**
     * Alter global rule configuration.
     *
     * @param ruleConfig global rule configuration
     */
    public synchronized void alterGlobalRuleConfiguration(final RuleConfiguration ruleConfig) {
        if (null == ruleConfig) {
            return;
        }
        closeStaleTransactionRule(ruleConfig);
        Collection<ShardingSphereRule> rules = new LinkedList<>(metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules());
        rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
        rules.addAll(GlobalRulesBuilder.buildSingleRules(ruleConfig, metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getProps()));
        metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules().clear();
        metaDataContexts.get().getMetaData().getGlobalRuleMetaData().getRules().addAll(rules);
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                metaDataContexts.get().getMetaData().getGlobalRuleMetaData(), metaDataContexts.get().getMetaData().getProps());
        metaDataContexts.set(newMetaDataContexts(toBeChangedMetaData));
    }
    
    // Optimize string comparison rule type.
    @SneakyThrows(Exception.class)
    private void closeStaleTransactionRule(final RuleConfiguration ruleConfig) {
        YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(ruleConfig);
        if (!"transaction".equals(Objects.requireNonNull(yamlRuleConfig.getClass().getAnnotation(RepositoryTupleEntity.class)).value())) {
            return;
        }
        Optional<TransactionRule> transactionRule = metaDataContexts.get().getMetaData().getGlobalRuleMetaData().findSingleRule(TransactionRule.class);
        if (transactionRule.isPresent()) {
            ((AutoCloseable) transactionRule.get()).close();
        }
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        ShardingSphereMetaData toBeChangedMetaData = new ShardingSphereMetaData(metaDataContexts.get().getMetaData().getDatabases(), metaDataContexts.get().getMetaData().getGlobalResourceMetaData(),
                metaDataContexts.get().getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props));
        metaDataContexts.set(newMetaDataContexts(toBeChangedMetaData));
    }
    
    private MetaDataContexts newMetaDataContexts(final ShardingSphereMetaData metaData) {
        return MetaDataContextsFactory.create(metaDataPersistService, metaData);
    }
}
