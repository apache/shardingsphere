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

import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.swapper.YamlProxyConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

/**
 * Standalone bootstrap initializer.
 */
public final class StandaloneBootstrapInitializer extends AbstractBootstrapInitializer {
    
    private final boolean isOverwrite;
    
    public StandaloneBootstrapInitializer(final ShardingSphereMode mode, final boolean isOverwrite) {
        super(mode);
        this.isOverwrite = isOverwrite;
    }
    
    @Override
    protected ProxyConfiguration getProxyConfiguration(final YamlProxyConfiguration yamlConfig) {
        persistConfigurations(yamlConfig, isOverwrite);
        // TODO remove isEmpty judge after LocalDistMetaDataPersistRepository finished
        ProxyConfiguration result = loadProxyConfiguration();
        return (result.getSchemaDataSources().isEmpty()) ? new YamlProxyConfigurationSwapper().swap(yamlConfig) : result;
    }
    
    @Override
    protected MetaDataContexts decorateMetaDataContexts(final MetaDataContexts metaDataContexts) {
        return metaDataContexts;
    }
    
    @Override
    protected TransactionContexts decorateTransactionContexts(final TransactionContexts transactionContexts, final String xaTransactionMangerType) {
        return transactionContexts;
    }
    
    @Override
    protected void initScaling(final YamlProxyConfiguration yamlConfig) {
        getScalingConfiguration(yamlConfig).ifPresent(optional -> ScalingContext.getInstance().init(optional));
    }
}
