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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.event.algorithm.AlterShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DeleteShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.config.AddShadowConfigurationEvent;
import org.apache.shardingsphere.shadow.event.config.AlterShadowConfigurationEvent;
import org.apache.shardingsphere.shadow.event.config.DeleteShadowConfigurationEvent;
import org.apache.shardingsphere.shadow.event.table.AddShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.AlterShadowTableEvent;
import org.apache.shardingsphere.shadow.event.table.DeleteShadowTableEvent;
import org.apache.shardingsphere.shadow.metadata.converter.ShadowNodeConverter;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

import java.util.Optional;

/**
 * Shadow rule configuration event builder.
 */
public final class ShadowRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!ShadowNodeConverter.isShadowPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> dataSourceName = ShadowNodeConverter.getDataSourceName(event.getKey());
        if (dataSourceName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShadowConfigEvent(databaseName, dataSourceName.get(), event);
        }
        Optional<String> tableName = ShadowNodeConverter.getTableName(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShadowTableConfigEvent(databaseName, tableName.get(), event);
        }
        Optional<String> algorithmName = ShadowNodeConverter.getAlgorithmName(event.getKey());
        if (algorithmName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createShadowAlgorithmEvent(databaseName, algorithmName.get(), event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createShadowConfigEvent(final String databaseName, final String dataSourceName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShadowConfigurationEvent(databaseName, swapShadowDataSourceRuleConfig(dataSourceName, event.getValue())));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShadowConfigurationEvent(databaseName, dataSourceName, swapShadowDataSourceRuleConfig(dataSourceName, event.getValue())));
        }
        return Optional.of(new DeleteShadowConfigurationEvent(databaseName, dataSourceName));
    }
    
    private ShadowDataSourceConfiguration swapShadowDataSourceRuleConfig(final String dataSourceName, final String yamlContext) {
        YamlShadowDataSourceConfiguration yamlConfig = YamlEngine.unmarshal(yamlContext, YamlShadowDataSourceConfiguration.class);
        return new ShadowDataSourceConfiguration(dataSourceName, yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
    }
    
    private Optional<GovernanceEvent> createShadowTableConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddShadowTableEvent(databaseName, tableName, swapToTableConfig(event.getValue())));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShadowTableEvent(databaseName, tableName, swapToTableConfig(event.getValue())));
        }
        return Optional.of(new DeleteShadowTableEvent(databaseName, tableName));
    }
    
    private ShadowTableConfiguration swapToTableConfig(final String yamlContext) {
        return new YamlShadowTableConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlShadowTableConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createShadowAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterShadowAlgorithmEvent(databaseName, algorithmName, swapToAlgorithmConfig(event.getValue())));
        }
        return Optional.of(new DeleteShadowAlgorithmEvent(databaseName, algorithmName));
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
