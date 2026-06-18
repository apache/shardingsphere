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

package org.apache.shardingsphere.shadow.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.constant.DistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Shadow rule configuration to DistSQL converter.
 */
public final class ShadowRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<ShadowRuleConfiguration> {
    
    @Override
    public String convert(final ShadowRuleConfiguration ruleConfig) {
        return ruleConfig.getDataSources().isEmpty() ? "" : ShadowConvertDistSQLConstants.CREATE_SHADOW_RULE + convertShadowDataSources(ruleConfig) + DistSQLConstants.SEMI;
    }
    
    private String convertShadowDataSources(final ShadowRuleConfiguration ruleConfig) {
        return ruleConfig.getDataSources().stream().map(each -> convertShadowDataSource(each, ruleConfig)).collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    private String convertShadowDataSource(final ShadowDataSourceConfiguration dataSourceConfig, final ShadowRuleConfiguration ruleConfig) {
        String shadowTables = convertShadowTables(dataSourceConfig.getName(), ruleConfig.getTables(), ruleConfig.getShadowAlgorithms());
        return String.format(ShadowConvertDistSQLConstants.SHADOW_DATA_SOURCE,
                dataSourceConfig.getName(), dataSourceConfig.getProductionDataSourceName(), dataSourceConfig.getShadowDataSourceName(), shadowTables);
    }
    
    private String convertShadowTables(final String shadowRuleName, final Map<String, ShadowTableConfiguration> tableConfigs, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        return tableConfigs.entrySet().stream().filter(entry -> entry.getValue().getDataSourceNames().contains(shadowRuleName))
                .map(entry -> convertShadowTable(entry.getKey(), entry.getValue(), algorithmConfigs)).collect(Collectors.joining(DistSQLConstants.COMMA + System.lineSeparator()));
    }
    
    private String convertShadowTable(final String shadowTableName, final ShadowTableConfiguration shadowTableConfig, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        String shadowTableTypes = convertShadowTableTypes(shadowTableConfig.getShadowAlgorithmNames(), algorithmConfigs);
        return String.format(ShadowConvertDistSQLConstants.SHADOW_TABLE, shadowTableName, shadowTableTypes);
    }
    
    private String convertShadowTableTypes(final Collection<String> shadowAlgorithmNames, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        return shadowAlgorithmNames.stream().map(each -> AlgorithmDistSQLConverter.getAlgorithmType(algorithmConfigs.get(each))).collect(Collectors.joining(DistSQLConstants.COMMA + ' '));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getType() {
        return ShadowRuleConfiguration.class;
    }
}
