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

package org.apache.shardingsphere.shadow.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.constant.ShadowDistSQLConstants;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Shadow convert rule configuration provider.
 */
public final class ShadowConvertRuleConfigurationProvider implements ConvertRuleConfigurationProvider {
    
    @Override
    public String convert(final RuleConfiguration ruleConfig) {
        return getShadowDistSQL((ShadowRuleConfiguration) ruleConfig);
    }
    
    private String getShadowDistSQL(final ShadowRuleConfiguration ruleConfig) {
        if (ruleConfig.getDataSources().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(ShadowDistSQLConstants.CREATE_SHADOW);
        Iterator<ShadowDataSourceConfiguration> iterator = ruleConfig.getDataSources().iterator();
        while (iterator.hasNext()) {
            ShadowDataSourceConfiguration dataSourceConfig = iterator.next();
            String shadowRuleName = dataSourceConfig.getName();
            String shadowTables = getShadowTables(shadowRuleName, ruleConfig.getTables(), ruleConfig.getShadowAlgorithms());
            result.append(String.format(ShadowDistSQLConstants.SHADOW, shadowRuleName, dataSourceConfig.getProductionDataSourceName(), dataSourceConfig.getShadowDataSourceName(), shadowTables));
            if (iterator.hasNext()) {
                result.append(ShadowDistSQLConstants.COMMA);
            }
        }
        result.append(ShadowDistSQLConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private String getShadowTables(final String shadowRuleName, final Map<String, ShadowTableConfiguration> ruleConfig, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        StringBuilder result = new StringBuilder();
        Iterator<Map.Entry<String, ShadowTableConfiguration>> iterator = ruleConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ShadowTableConfiguration> shadowTableConfig = iterator.next();
            if (shadowTableConfig.getValue().getDataSourceNames().contains(shadowRuleName)) {
                String shadowTableTypes = getShadowTableTypes(shadowTableConfig.getValue().getShadowAlgorithmNames(), algorithmConfigs);
                result.append(String.format(ShadowDistSQLConstants.SHADOW_TABLE, shadowTableConfig.getKey(), shadowTableTypes));
            }
            if (iterator.hasNext()) {
                result.append(ShadowDistSQLConstants.COMMA).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    private String getShadowTableTypes(final Collection<String> shadowAlgorithmNames, final Map<String, AlgorithmConfiguration> algorithmConfigs) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = shadowAlgorithmNames.iterator();
        while (iterator.hasNext()) {
            result.append(getAlgorithmType(algorithmConfigs.get(iterator.next())));
            if (iterator.hasNext()) {
                result.append(ShadowDistSQLConstants.COMMA).append(' ');
            }
        }
        return result.toString();
    }
    
    private String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        if (null == algorithmConfig) {
            return result.toString();
        }
        String type = algorithmConfig.getType().toLowerCase();
        if (algorithmConfig.getProps().isEmpty()) {
            result.append(String.format(ShadowDistSQLConstants.ALGORITHM_TYPE_WITHOUT_PROPS, type));
        } else {
            result.append(String.format(ShadowDistSQLConstants.ALGORITHM_TYPE, type, getAlgorithmProperties(algorithmConfig.getProps())));
        }
        return result.toString();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String getAlgorithmProperties(final Properties props) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = new TreeMap(props).keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = props.get(key);
            if (null == value) {
                continue;
            }
            result.append(String.format(ShadowDistSQLConstants.PROPERTY, key, value));
            if (iterator.hasNext()) {
                result.append(ShadowDistSQLConstants.COMMA).append(' ');
            }
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return ShadowRuleConfiguration.class.getName();
    }
}
