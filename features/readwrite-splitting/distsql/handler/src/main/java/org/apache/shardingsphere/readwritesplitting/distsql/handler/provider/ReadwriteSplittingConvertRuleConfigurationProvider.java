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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.provider;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.ral.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.distsql.handler.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Readwrite-splitting convert rule configuration provider.
 */
public final class ReadwriteSplittingConvertRuleConfigurationProvider implements ConvertRuleConfigurationProvider {
    
    @Override
    public String convert(final RuleConfiguration ruleConfig) {
        return getReadWriteSplittingDistSQL((ReadwriteSplittingRuleConfiguration) ruleConfig);
    }
    
    private String getReadWriteSplittingDistSQL(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        if (ruleConfig.getDataSources().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(DistSQLScriptConstants.CREATE_READWRITE_SPLITTING_RULE);
        Iterator<ReadwriteSplittingDataSourceRuleConfiguration> iterator = ruleConfig.getDataSources().iterator();
        while (iterator.hasNext()) {
            appendStaticReadWriteSplittingRule(ruleConfig.getLoadBalancers(), iterator.next(), result);
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private void appendStaticReadWriteSplittingRule(final Map<String, AlgorithmConfiguration> loadBalancers,
                                                    final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final StringBuilder stringBuilder) {
        String readDataSourceNames = getReadDataSourceNames(dataSourceRuleConfig.getReadDataSourceNames());
        String transactionalReadQueryStrategy = dataSourceRuleConfig.getTransactionalReadQueryStrategy().name();
        String loadBalancerType = getLoadBalancerType(loadBalancers.get(dataSourceRuleConfig.getLoadBalancerName()));
        stringBuilder.append(String.format(DistSQLScriptConstants.READWRITE_SPLITTING_FOR_STATIC,
                dataSourceRuleConfig.getName(), dataSourceRuleConfig.getWriteDataSourceName(), readDataSourceNames, transactionalReadQueryStrategy, loadBalancerType));
    }
    
    private String getReadDataSourceNames(final Collection<String> readDataSourceNames) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = readDataSourceNames.iterator();
        while (iterator.hasNext()) {
            result.append(String.format(DistSQLScriptConstants.READ_RESOURCE, iterator.next()));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA);
            }
        }
        return result.toString();
    }
    
    private String getLoadBalancerType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        String loadBalancerType = getAlgorithmType(algorithmConfig);
        if (!Strings.isNullOrEmpty(loadBalancerType)) {
            result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator()).append(loadBalancerType);
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
        return ReadwriteSplittingRuleConfiguration.class.getName();
    }
}
