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

import org.apache.shardingsphere.infra.config.condition.PreConditionRuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.persist.config.DistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.infra.persist.rule.DistMetaDataPersistRule;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.transaction.context.TransactionContexts;

/**
 * Standard bootstrap initializer.
 */
public final class StandardBootstrapInitializer extends AbstractBootstrapInitializer {
    
    public StandardBootstrapInitializer(final PreConditionRuleConfiguration preConditionRuleConfig, final DistMetaDataPersistRule persistRule) {
        super(preConditionRuleConfig, persistRule);
    }
    
    @Override
    protected boolean isOverwrite(final PreConditionRuleConfiguration ruleConfig) {
        return ((DistMetaDataPersistRuleConfiguration) ruleConfig).isOverwrite();
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
