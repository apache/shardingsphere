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
import org.apache.shardingsphere.distsql.handler.type.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.constant.ReadwriteSplittingDistSQLConstants;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
        StringBuilder result = new StringBuilder(ReadwriteSplittingDistSQLConstants.CREATE_READWRITE_SPLITTING_RULE);
        Iterator<ReadwriteSplittingDataSourceRuleConfiguration> iterator = ruleConfig.getDataSources().iterator();
        while (iterator.hasNext()) {
            appendStaticReadWriteSplittingRule(ruleConfig.getLoadBalancers(), iterator.next(), result);
            if (iterator.hasNext()) {
                result.append(ReadwriteSplittingDistSQLConstants.COMMA);
            }
        }
        result.append(ReadwriteSplittingDistSQLConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private void appendStaticReadWriteSplittingRule(final Map<String, AlgorithmConfiguration> loadBalancers,
                                                    final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig, final StringBuilder stringBuilder) {
        String readDataSourceNames = getReadDataSourceNames(dataSourceRuleConfig.getReadDataSourceNames());
        String transactionalReadQueryStrategy = dataSourceRuleConfig.getTransactionalReadQueryStrategy().name();
        String loadBalancerType = getLoadBalancerType(loadBalancers.get(dataSourceRuleConfig.getLoadBalancerName()));
        stringBuilder.append(String.format(ReadwriteSplittingDistSQLConstants.READWRITE_SPLITTING_FOR_STATIC,
                dataSourceRuleConfig.getName(), dataSourceRuleConfig.getWriteDataSourceName(), readDataSourceNames, transactionalReadQueryStrategy, loadBalancerType));
    }
    
    private String getReadDataSourceNames(final Collection<String> readDataSourceNames) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = readDataSourceNames.iterator();
        while (iterator.hasNext()) {
            result.append(String.format(ReadwriteSplittingDistSQLConstants.READ_RESOURCE, iterator.next()));
            if (iterator.hasNext()) {
                result.append(ReadwriteSplittingDistSQLConstants.COMMA);
            }
        }
        return result.toString();
    }
    
    private String getLoadBalancerType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        String loadBalancerType = getAlgorithmType(algorithmConfig);
        if (!Strings.isNullOrEmpty(loadBalancerType)) {
            result.append(ReadwriteSplittingDistSQLConstants.COMMA).append(System.lineSeparator()).append(loadBalancerType);
        }
        return result.toString();
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
