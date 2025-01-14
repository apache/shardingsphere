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
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

/**
 * Global configuration manager.
 */
@Slf4j
public final class GlobalConfigurationManager {
    
    private final MetaDataContexts metaDataContexts;
    
    private final MetaDataPersistService metaDataPersistService;
    
    public GlobalConfigurationManager(final MetaDataContexts metaDataContexts, final PersistRepository repository) {
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
        Collection<ShardingSphereRule> rules = new LinkedList<>(metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules());
        rules.removeIf(each -> each.getConfiguration().getClass().isAssignableFrom(ruleConfig.getClass()));
        rules.addAll(GlobalRulesBuilder.buildSingleRules(ruleConfig, metaDataContexts.getMetaData().getAllDatabases(), metaDataContexts.getMetaData().getProps()));
        metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().clear();
        metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().addAll(rules);
        metaDataContexts.update(new ShardingSphereMetaData(metaDataContexts.getMetaData().getAllDatabases(),
                metaDataContexts.getMetaData().getGlobalResourceMetaData(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps()),
                metaDataPersistService);
    }
    
    // Optimize string comparison rule type.
    @SneakyThrows(Exception.class)
    private void closeStaleTransactionRule(final RuleConfiguration ruleConfig) {
        YamlRuleConfiguration yamlRuleConfig = new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(ruleConfig);
        if (!"transaction".equals(Objects.requireNonNull(yamlRuleConfig.getClass().getAnnotation(RepositoryTupleEntity.class)).value())) {
            return;
        }
        metaDataContexts.getMetaData().getGlobalRuleMetaData().findSingleRule(TransactionRule.class).ifPresent(TransactionRule::close);
    }
    
    /**
     * Alter properties.
     *
     * @param props properties to be altered
     */
    public synchronized void alterProperties(final Properties props) {
        metaDataContexts.update(new ShardingSphereMetaData(metaDataContexts.getMetaData().getAllDatabases(),
                metaDataContexts.getMetaData().getGlobalResourceMetaData(), metaDataContexts.getMetaData().getGlobalRuleMetaData(), new ConfigurationProperties(props)), metaDataPersistService);
    }
}
