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

package org.apache.shardingsphere.mask.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.ral.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.distsql.handler.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Mask convert rule configuration provider.
 */
public final class MaskConvertRuleConfigurationProvider implements ConvertRuleConfigurationProvider {
    
    @Override
    public String convert(final RuleConfiguration ruleConfig) {
        return getMaskDistSQL((MaskRuleConfiguration) ruleConfig);
    }
    
    private String getMaskDistSQL(final MaskRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(DistSQLScriptConstants.CREATE_MASK);
        Iterator<MaskTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            MaskTableRuleConfiguration tableRuleConfig = iterator.next();
            result.append(String.format(DistSQLScriptConstants.MASK, tableRuleConfig.getName(), getMaskColumns(tableRuleConfig.getColumns(), ruleConfig.getMaskAlgorithms())));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private String getMaskColumns(final Collection<MaskColumnRuleConfiguration> columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        StringBuilder result = new StringBuilder();
        Iterator<MaskColumnRuleConfiguration> iterator = columnRuleConfig.iterator();
        if (iterator.hasNext()) {
            MaskColumnRuleConfiguration column = iterator.next();
            result.append(String.format(DistSQLScriptConstants.MASK_COLUMN, column.getLogicColumn(), getMaskAlgorithms(column, maskAlgorithms)));
        }
        return result.toString();
    }
    
    private String getMaskAlgorithms(final MaskColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> maskAlgorithms) {
        return getAlgorithmType(maskAlgorithms.get(columnRuleConfig.getMaskAlgorithm()));
    }
    
    private String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        if (null == algorithmConfig) {
            return result.toString();
        }
        String type = algorithmConfig.getType().toLowerCase();
        if (algorithmConfig.getProps().isEmpty()) {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE_WITHOUT_PROPS, type));
        } else {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE, type, getAlgorithmProperties(algorithmConfig.getProps())));
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
            result.append(String.format(DistSQLScriptConstants.PROPERTY, key, value));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(' ');
            }
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return MaskRuleConfiguration.class.getName();
    }
}
