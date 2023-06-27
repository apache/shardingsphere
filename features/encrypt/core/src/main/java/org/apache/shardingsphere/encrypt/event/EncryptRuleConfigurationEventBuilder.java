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
import org.apache.shardingsphere.encrypt.event.encryptor.AlterEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.encryptor.DeleteEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.table.AddEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.AlterEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.DeleteEncryptTableEvent;
import org.apache.shardingsphere.encrypt.metadata.converter.EncryptNodeConverter;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;

import java.util.Optional;

/**
 * Encrypt rule configuration event builder.
 */
public final class EncryptRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private final RuleNodePath encryptRuleNodePath = EncryptNodeConverter.getInstance();
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!encryptRuleNodePath.getRootNodePath().isValidatedPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = encryptRuleNodePath.getNamedRuleItemNodePath(EncryptNodeConverter.TABLES).getNameByActiveVersion(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createEncryptConfigEvent(databaseName, tableName.get(), event);
        }
        Optional<String> encryptorName = encryptRuleNodePath.getNamedRuleItemNodePath(EncryptNodeConverter.ENCRYPTORS).getNameByActiveVersion(event.getKey());
        if (encryptorName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createEncryptorEvent(databaseName, encryptorName.get(), event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createEncryptConfigEvent(final String databaseName, final String groupName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddEncryptTableEvent(databaseName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterEncryptTableEvent(databaseName, groupName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteEncryptTableEvent(databaseName, groupName, event.getKey(), event.getValue()));
    }
    
    private Optional<GovernanceEvent> createEncryptorEvent(final String databaseName, final String encryptorName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterEncryptorEvent(databaseName, encryptorName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteEncryptorEvent(databaseName, encryptorName, event.getKey(), event.getValue()));
    }
}
