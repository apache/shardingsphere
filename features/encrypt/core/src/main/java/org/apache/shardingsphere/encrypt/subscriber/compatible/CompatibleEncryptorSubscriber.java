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

package org.apache.shardingsphere.encrypt.subscriber.compatible;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.compatible.encryptor.AlterCompatibleEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.compatible.encryptor.DeleteCompatibleEncryptorEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Compatible encrypt encryptor subscriber.
 * @deprecated compatible support will remove in next version.
 */
@Deprecated
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class CompatibleEncryptorSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with alter encryptor.
     *
     * @param event alter encryptor event
     */
    @Subscribe
    public synchronized void renew(final AlterCompatibleEncryptorEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), getCompatibleEncryptRuleConfiguration(database, event)));
    }
    
    /**
     * Renew with delete encryptor.
     *
     * @param event delete encryptor event
     */
    @Subscribe
    public synchronized void renew(final DeleteCompatibleEncryptorEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        CompatibleEncryptRuleConfiguration config = (CompatibleEncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getEncryptors().remove(event.getEncryptorName());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private CompatibleEncryptRuleConfiguration getCompatibleEncryptRuleConfiguration(final ShardingSphereDatabase database, final AlterCompatibleEncryptorEvent event) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        CompatibleEncryptRuleConfiguration config = rule.map(encryptRule -> getEncryptRuleConfiguration((CompatibleEncryptRuleConfiguration) encryptRule.getConfiguration()))
                .orElseGet(() -> new CompatibleEncryptRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
        config.getEncryptors().put(event.getEncryptorName(), swapToAlgorithmConfig(
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion())));
        return config;
    }
    
    private CompatibleEncryptRuleConfiguration getEncryptRuleConfiguration(final CompatibleEncryptRuleConfiguration config) {
        if (null == config.getEncryptors()) {
            return new CompatibleEncryptRuleConfiguration(config.getTables(), new LinkedHashMap<>());
        }
        return config;
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
