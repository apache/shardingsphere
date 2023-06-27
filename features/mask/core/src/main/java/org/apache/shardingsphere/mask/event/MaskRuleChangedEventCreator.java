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

package org.apache.shardingsphere.mask.event;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mask.event.algorithm.AlterMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.algorithm.DeleteMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.table.AddMaskTableEvent;
import org.apache.shardingsphere.mask.event.table.AlterMaskTableEvent;
import org.apache.shardingsphere.mask.event.table.DeleteMaskTableEvent;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleChangedEventCreator;

/**
 * Mask rule changed event creator.
 */
public final class MaskRuleChangedEventCreator implements RuleChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType, final String itemName) {
        switch (itemType) {
            case MaskRuleNodePathProvider.TABLES:
                return createTableEvent(databaseName, itemName, event);
            case MaskRuleNodePathProvider.ALGORITHMS:
                return createAlgorithmEvent(databaseName, itemName, event);
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType) {
        throw new UnsupportedOperationException(itemType);
    }
    
    private GovernanceEvent createTableEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddMaskTableEvent(databaseName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterMaskTableEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteMaskTableEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterMaskAlgorithmEvent(databaseName, algorithmName, event.getKey(), event.getValue());
        }
        return new DeleteMaskAlgorithmEvent(databaseName, algorithmName);
    }
    
    @Override
    public String getType() {
        return "mask";
    }
}
