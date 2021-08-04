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

package org.apache.shardingsphere.proxy.initializer.impl;

import org.apache.shardingsphere.governance.context.metadata.GovernanceMetaDataContexts;
import org.apache.shardingsphere.governance.context.transaction.GovernanceTransactionContexts;
import org.apache.shardingsphere.governance.core.rule.GovernanceRule;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.scaling.core.api.ScalingWorker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Governance bootstrap initializer.
 */
public final class GovernanceBootstrapInitializer extends AbstractBootstrapInitializer {
    
    private final GovernanceRule governanceRule;
    
    public GovernanceBootstrapInitializer(final GovernanceRule governanceRule) {
        super(governanceRule.getRegistryCenter().getRepository());
        this.governanceRule = governanceRule;
    }
    
    @Override
    protected boolean isOverwrite(final YamlProxyConfiguration yamlConfig) {
        return yamlConfig.getServerConfiguration().getGovernance().isOverwrite();
    }
    
    @Override
    protected MetaDataContexts decorateMetaDataContexts(final MetaDataContexts metaDataContexts) {
        return new GovernanceMetaDataContexts((StandardMetaDataContexts) metaDataContexts, getDistMetaDataPersistService(), governanceRule.getRegistryCenter());
    }
    
    @Override
    protected TransactionContexts decorateTransactionContexts(final TransactionContexts transactionContexts, final String xaTransactionMangerType) {
        return new GovernanceTransactionContexts(transactionContexts, xaTransactionMangerType);
    }
    
    @Override
    protected void initScalingWorker(final YamlProxyConfiguration yamlConfig) {
        Optional<ServerConfiguration> scalingConfig = getScalingConfiguration(yamlConfig);
        scalingConfig.ifPresent(optional -> initScaling(yamlConfig.getServerConfiguration().getGovernance(), optional));
    }
    
    private void initScaling(final YamlGovernanceConfiguration governanceConfig, final ServerConfiguration scalingConfig) {
        scalingConfig.setGovernanceConfig(new GovernanceConfigurationYamlSwapper().swapToObject(governanceConfig));
        ScalingContext.getInstance().init(scalingConfig);
        ScalingWorker.init();
    }
    
    @Override
    public void afterInit(final YamlProxyConfiguration yamlConfig) {
        governanceRule.getRegistryCenter().onlineInstance(getSchemaNames(yamlConfig));
    }
    
    private Set<String> getSchemaNames(final YamlProxyConfiguration yamlConfig) {
        return Stream.of(getDistMetaDataPersistService().getSchemaMetaDataService().loadAllNames(), yamlConfig.getRuleConfigurations().keySet()).flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
