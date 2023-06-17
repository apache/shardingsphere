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

package org.apache.shardingsphere.encrypt.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.encryptor.AddEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.encryptor.AlterEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.encryptor.DeleteEncryptorEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;

import java.util.Map;

/**
 * Encrypt encryptor subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class EncryptorSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add encryptor.
     *
     * @param event add encryptor event
     */
    @Subscribe
    public synchronized void renew(final AddEncryptorEvent<AlgorithmConfiguration> event) {
        renew(event.getDatabaseName(), event.getEncryptorName(), event.getConfig());
    }
    
    /**
     * Renew with alter encryptor.
     *
     * @param event alter encryptor event
     */
    @Subscribe
    public synchronized void renew(final AlterEncryptorEvent<AlgorithmConfiguration> event) {
        renew(event.getDatabaseName(), event.getEncryptorName(), event.getConfig());
    }
    
    private void renew(final String databaseName, final String encryptorName, final AlgorithmConfiguration encryptorConfig) {
        ShardingSphereDatabase database = databases.get(databaseName);
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getEncryptors().put(encryptorName, encryptorConfig);
    }
    
    /**
     * Renew with delete encryptor.
     *
     * @param event delete encryptor event
     */
    @Subscribe
    public synchronized void renew(final DeleteEncryptorEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        EncryptRuleConfiguration config = (EncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
        config.getEncryptors().remove(event.getEncryptorName());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
