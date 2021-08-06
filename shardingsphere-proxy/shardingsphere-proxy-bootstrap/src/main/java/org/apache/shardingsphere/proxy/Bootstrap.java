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

package org.apache.shardingsphere.proxy;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.core.rule.GovernanceRule;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.condition.PreConditionRuleConfiguration;
import org.apache.shardingsphere.infra.config.persist.repository.DistMetaDataPersistRepositoryFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.rule.persist.DistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.arguments.BootstrapArguments;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.proxy.initializer.impl.GovernanceBootstrapInitializer;
import org.apache.shardingsphere.proxy.initializer.impl.StandardBootstrapInitializer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ShardingSphere-Proxy Bootstrap.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Bootstrap {
    
    /**
     * Main entrance.
     *
     * @param args startup arguments
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public static void main(final String[] args) throws IOException, SQLException {
        BootstrapArguments bootstrapArgs = new BootstrapArguments(args);
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(bootstrapArgs.getConfigurationPath());
        BootstrapInitializer initializer = createBootstrapInitializer(yamlConfig);
        initializer.init(yamlConfig);
        initializer.afterInit(yamlConfig);
        new ShardingSphereProxy().start(bootstrapArgs.getPort());
    }
    
    private static BootstrapInitializer createBootstrapInitializer(final YamlProxyConfiguration yamlConfig) {
        PreConditionRuleConfiguration preConditionRuleConfig = getPreConditionRuleConfiguration(yamlConfig);
        // TODO split to pluggable SPI
        if (preConditionRuleConfig instanceof DistMetaDataPersistRuleConfiguration) {
            return new StandardBootstrapInitializer(preConditionRuleConfig, DistMetaDataPersistRepositoryFactory.newInstance((DistMetaDataPersistRuleConfiguration) preConditionRuleConfig));
        }
        ShardingSphereRule rule = ShardingSphereRulesBuilder.buildGlobalRules(Collections.singleton(preConditionRuleConfig), Collections.emptyMap()).iterator().next();
        Preconditions.checkState(rule instanceof GovernanceRule);
        return new GovernanceBootstrapInitializer(preConditionRuleConfig, (GovernanceRule) rule);
    }
    
    // TODO split to pluggable SPI
    private static PreConditionRuleConfiguration getPreConditionRuleConfiguration(final YamlProxyConfiguration yamlConfig) {
        Collection<RuleConfiguration> globalRuleConfigs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yamlConfig.getServerConfiguration().getRules());
        Collection<PreConditionRuleConfiguration> preConditionRuleConfigs = globalRuleConfigs.stream().filter(
            each -> each instanceof PreConditionRuleConfiguration).map(each -> (PreConditionRuleConfiguration) each).collect(Collectors.toList());
        if (preConditionRuleConfigs.isEmpty()) {
            return new DistMetaDataPersistRuleConfiguration("Local", true, new Properties());
        }
        // TODO resolve conflict of dist meta data persist rule and governance rule
        return preConditionRuleConfigs.iterator().next();
    }
}
