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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
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
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowNodePath;

import java.util.Optional;

/**
 * Shadow rule configuration event builder.
 */
public final class ShadowRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    private final RuleNodePath shadowRuleNodePath = ShadowNodePath.getInstance();
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!shadowRuleNodePath.getRoot().isValidatedPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> dataSourceName = shadowRuleNodePath.getNamedItem(ShadowNodePath.DATA_SOURCES).getNameByActiveVersion(event.getKey());
        if (dataSourceName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShadowConfigEvent(databaseName, dataSourceName.get(), event);
        }
        Optional<String> tableName = shadowRuleNodePath.getNamedItem(ShadowNodePath.TABLES).getNameByActiveVersion(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShadowTableConfigEvent(databaseName, tableName.get(), event);
        }
        Optional<String> algorithmName = shadowRuleNodePath.getNamedItem(ShadowNodePath.ALGORITHMS).getNameByActiveVersion(event.getKey());
        if (algorithmName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShadowAlgorithmEvent(databaseName, algorithmName.get(), event);
        }
        if (shadowRuleNodePath.getUniqueItem(ShadowNodePath.DEFAULT_ALGORITHM).isActiveVersionPath(event.getKey()) && !Strings.isNullOrEmpty(event.getValue())) {
            return createDefaultShadowAlgorithmNameEvent(databaseName, event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createShadowConfigEvent(final String databaseName, final String dataSourceName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShadowDataSourceEvent(databaseName, dataSourceName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShadowDataSourceEvent(databaseName, dataSourceName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShadowDataSourceEvent(databaseName, dataSourceName));
    }
    
    private Optional<GovernanceEvent> createShadowTableConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShadowTableEvent(databaseName, tableName, event.getKey(), event.getValue()));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShadowTableEvent(databaseName, tableName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShadowTableEvent(databaseName, tableName));
    }
    
    private Optional<GovernanceEvent> createShadowAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShadowAlgorithmEvent(databaseName, algorithmName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteShadowAlgorithmEvent(databaseName, algorithmName));
    }
    
    private Optional<GovernanceEvent> createDefaultShadowAlgorithmNameEvent(final String databaseName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterDefaultShadowAlgorithmNameEvent(databaseName, event.getKey(), event.getValue()));
        }
        return Optional.of(new DeleteDefaultShadowAlgorithmNameEvent(databaseName));
    }
}
