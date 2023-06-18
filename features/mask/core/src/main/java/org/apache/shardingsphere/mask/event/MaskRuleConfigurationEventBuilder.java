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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.event.algorithm.AlterMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.algorithm.DeleteMaskAlgorithmEvent;
import org.apache.shardingsphere.mask.event.config.AddMaskConfigurationEvent;
import org.apache.shardingsphere.mask.event.config.AlterMaskConfigurationEvent;
import org.apache.shardingsphere.mask.event.config.DeleteMaskConfigurationEvent;
import org.apache.shardingsphere.mask.metadata.converter.MaskNodeConverter;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.rule.YamlMaskTableRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;

import java.util.Optional;

/**
 * Mask rule configuration event builder.
 */
public final class MaskRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!MaskNodeConverter.isMaskPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        Optional<String> tableName = MaskNodeConverter.getTableName(event.getKey());
        if (tableName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createMaskConfigEvent(databaseName, tableName.get(), event);
        }
        Optional<String> algorithmName = MaskNodeConverter.getAlgorithmName(event.getKey());
        if (algorithmName.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createMaskAlgorithmEvent(databaseName, algorithmName.get(), event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createMaskConfigEvent(final String databaseName, final String tableName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddMaskConfigurationEvent(databaseName, swapMaskTableRuleConfig(event.getValue())));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterMaskConfigurationEvent(databaseName, tableName, swapMaskTableRuleConfig(event.getValue())));
        }
        return Optional.of(new DeleteMaskConfigurationEvent(databaseName, tableName));
    }
    
    private MaskTableRuleConfiguration swapMaskTableRuleConfig(final String yamlContext) {
        return new YamlMaskTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlMaskTableRuleConfiguration.class));
    }
    
    private Optional<GovernanceEvent> createMaskAlgorithmEvent(final String databaseName, final String algorithmName, final DataChangedEvent event) {
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterMaskAlgorithmEvent(databaseName, algorithmName, swapToAlgorithmConfig(event.getValue())));
        }
        return Optional.of(new DeleteMaskAlgorithmEvent(databaseName, algorithmName));
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
