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

import org.apache.shardingsphere.encrypt.event.encryptor.AlterEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.encryptor.DeleteEncryptorEvent;
import org.apache.shardingsphere.encrypt.event.table.AddEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.AlterEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.DeleteEncryptTableEvent;
import org.apache.shardingsphere.encrypt.metadata.nodepath.EncryptRuleNodePathProvider;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleChangedEventCreator;

/**
 * Encrypt rule changed event creator.
 */
public final class EncryptRuleChangedEventCreator implements RuleChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType, final String itemName) {
        switch (itemType) {
            case EncryptRuleNodePathProvider.TABLES:
                return createTableEvent(databaseName, itemName, event);
            case EncryptRuleNodePathProvider.ENCRYPTORS:
                return createEncryptorEvent(databaseName, itemName, event);
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    private GovernanceEvent createTableEvent(final String databaseName, final String groupName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddEncryptTableEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterEncryptTableEvent(databaseName, groupName, event.getKey(), event.getValue());
        }
        return new DeleteEncryptTableEvent(databaseName, groupName);
    }
    
    private GovernanceEvent createEncryptorEvent(final String databaseName, final String encryptorName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterEncryptorEvent(databaseName, encryptorName, event.getKey(), event.getValue());
        }
        return new DeleteEncryptorEvent(databaseName, encryptorName);
    }
    
    @Override
    public String getType() {
        return "encrypt";
    }
}
