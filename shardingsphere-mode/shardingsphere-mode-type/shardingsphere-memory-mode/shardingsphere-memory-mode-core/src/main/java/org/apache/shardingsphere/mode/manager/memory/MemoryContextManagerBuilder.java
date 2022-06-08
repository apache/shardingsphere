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

package org.apache.shardingsphere.mode.manager.memory;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.memory.lock.MemoryLockContext;
import org.apache.shardingsphere.mode.manager.memory.workerid.generator.MemoryWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGenerator;
import org.apache.shardingsphere.transaction.spi.TransactionConfigurationFileGeneratorFactory;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Memory context manager builder.
 */
public final class MemoryContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(
                parameter.getDatabaseConfigs(), parameter.getGlobalRuleConfigs(), new ConfigurationProperties(parameter.getProps())).build(null);
        InstanceContext instanceContext = buildInstanceContext(parameter);
        generateTransactionConfigurationFile(instanceContext, metaDataContexts);
        ContextManager result = new ContextManager(metaDataContexts, buildInstanceContext(parameter));
        setInstanceContext(result);
        return result;
    }
    
    private InstanceContext buildInstanceContext(final ContextManagerBuilderParameter parameter) {
        ComputeNodeInstance instance = new ComputeNodeInstance(parameter.getInstanceDefinition());
        instance.setLabels(parameter.getLabels());
        return new InstanceContext(instance, new MemoryWorkerIdGenerator(), buildMemoryModeConfiguration(parameter.getModeConfig()), new MemoryLockContext());
    }
    
    private void generateTransactionConfigurationFile(final InstanceContext instanceContext, final MetaDataContexts metaDataContexts) {
        Optional<TransactionRule> transactionRule =
                metaDataContexts.getMetaData().getGlobalRuleMetaData().getRules().stream().filter(each -> each instanceof TransactionRule).map(each -> (TransactionRule) each).findFirst();
        if (transactionRule.isPresent()) {
            Optional<TransactionConfigurationFileGenerator> fileGenerator = TransactionConfigurationFileGeneratorFactory.findInstance(transactionRule.get().getProviderType());
            fileGenerator.ifPresent(optional -> optional.generateFile(transactionRule.get().getProps(), instanceContext));
        }
    }
    
    private void setInstanceContext(final ContextManager contextManager) {
        contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findRules(InstanceAwareRule.class).forEach(each -> each.setInstanceContext(contextManager.getInstanceContext()));
        contextManager.getMetaDataContexts().getMetaData().getDatabases()
                .forEach((key, value) -> value.getRuleMetaData().findRules(InstanceAwareRule.class).forEach(each -> each.setInstanceContext(contextManager.getInstanceContext())));
    }
    
    private ModeConfiguration buildMemoryModeConfiguration(final ModeConfiguration modeConfiguration) {
        return Optional.ofNullable(modeConfiguration).orElseGet(() -> new ModeConfiguration(getType(), null, false));
    }
    
    @Override
    public String getType() {
        return "Memory";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
