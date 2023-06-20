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

package org.apache.shardingsphere.encrypt.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.table.AddEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.AlterEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.DeleteEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.encryptor.AlterEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.encryptor.DeleteEncryptorEvent;
import org.apache.shardingsphere.encrypt.metadata.converter.EncryptNodeConverter;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;

import java.util.Optional;

/**
 * Encrypt rule configuration event builder.
 */
public final class EncryptRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!EncryptNodeConverter.isEncryptPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = EncryptNodeConverter.getTableName(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> encryptTableVersion = EncryptNodeConverter.getEncryptTableVersion(event.getKey());
            if (encryptTableVersion.isPresent()) {
                return createEncryptConfigEvent(databaseName, tableName.get(), Integer.parseInt(encryptTableVersion.get()), event);
            }
        }
        Optional<String> encryptorName = EncryptNodeConverter.getEncryptorName(event.getKey());
        if (encryptorName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            Optional<String> encryptorVersion = EncryptNodeConverter.getEncryptorVersion(event.getKey());
            if (encryptorVersion.isPresent()) {
                return createEncryptorEvent(databaseName, encryptorName.get(), Integer.parseInt(encryptorVersion.get()), event);
            }
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createEncryptConfigEvent(final String databaseName, final String groupName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddEncryptTableEvent(databaseName, swapEncryptTableRuleConfig(event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterEncryptTableEvent(databaseName, groupName, swapEncryptTableRuleConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteEncryptTableEvent(databaseName, groupName, event.getKey(), version));
    }
    
    private EncryptTableRuleConfiguration swapEncryptTableRuleConfig(final String yamlContext) {
        return new YamlEncryptTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlEncryptTableRuleConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createEncryptorEvent(final String databaseName, final String encryptorName, final int version, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterEncryptorEvent(databaseName, encryptorName, swapToAlgorithmConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteEncryptorEvent(databaseName, encryptorName, event.getKey(), version));
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
