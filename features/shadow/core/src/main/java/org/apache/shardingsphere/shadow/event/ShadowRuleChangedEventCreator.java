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

package org.apache.shardingsphere.shadow.event;

import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleChangedEventCreator;
import org.apache.shardingsphere.shadow.event.algorithm.AlterDefaultShadowAlgorithmNameEvent;
import org.apache.shardingsphere.shadow.event.algorithm.AlterShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DeleteDefaultShadowAlgorithmNameEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DeleteShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.datasource.AddShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.datasource.AlterShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.datasource.DeleteShadowDataSourceEvent;
import org.apache.shardingsphere.shadow.event.table.AddShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.AlterShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.DeleteShadowTableEvent;
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowRuleNodePathProvider;

/**
 * Shadow rule changed event creator.
 */
public final class ShadowRuleChangedEventCreator implements RuleChangedEventCreator {
    
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType, final String itemName) {
        switch (itemType) {
            case ShadowRuleNodePathProvider.DATA_SOURCES:
                return createShadowDataSourceEvent(databaseName, itemName, event);
            case ShadowRuleNodePathProvider.TABLES:
                return createShadowTableEvent(databaseName, itemName, event);
            case ShadowRuleNodePathProvider.ALGORITHMS:
                return createShadowAlgorithmEvent(databaseName, itemName, event);
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    @Override
    public GovernanceEvent create(final String databaseName, final DataChangedEvent event, final String itemType) {
        switch (itemType) {
            case ShadowRuleNodePathProvider.DEFAULT_ALGORITHM:
                return createDefaultShadowAlgorithmNameEvent(databaseName, event);
            default:
                throw new UnsupportedOperationException(itemType);
        }
    }
    
    private GovernanceEvent createShadowDataSourceEvent(final String databaseName, final String dataSourceName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShadowDataSourceEvent(databaseName, dataSourceName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShadowDataSourceEvent(databaseName, dataSourceName, event.getKey(), event.getValue());
        }
        return new DeleteShadowDataSourceEvent(databaseName, dataSourceName);
    }
    
    private GovernanceEvent createShadowTableEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return new AddShadowTableEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        if (Type.UPDATED == event.getType()) {
            return new AlterShadowTableEvent(databaseName, tableName, event.getKey(), event.getValue());
        }
        return new DeleteShadowTableEvent(databaseName, tableName);
    }
    
    private GovernanceEvent createShadowAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterShadowAlgorithmEvent(databaseName, algorithmName, event.getKey(), event.getValue());
        }
        return new DeleteShadowAlgorithmEvent(databaseName, algorithmName);
    }
    
    private GovernanceEvent createDefaultShadowAlgorithmNameEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return new AlterDefaultShadowAlgorithmNameEvent(databaseName, event.getKey(), event.getValue());
        }
        return new DeleteDefaultShadowAlgorithmNameEvent(databaseName);
    }
    
    @Override
    public String getType() {
        return "shadow";
    }
}
