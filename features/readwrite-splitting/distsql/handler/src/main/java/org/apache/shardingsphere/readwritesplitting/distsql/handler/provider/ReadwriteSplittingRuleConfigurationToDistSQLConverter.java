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
import org.apache.shardingsphere.distsql.handler.constant.DistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.constant.ReadwriteSplittingDistSQLConstants;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Readwrite-splitting rule configuration to DistSQL converter.
 */
public final class ReadwriteSplittingRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public String convert(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        if (ruleConfig.getDataSourceGroups().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(ReadwriteSplittingDistSQLConstants.CREATE_READWRITE_SPLITTING_RULE);
        Iterator<ReadwriteSplittingDataSourceGroupRuleConfiguration> iterator = ruleConfig.getDataSourceGroups().iterator();
        while (iterator.hasNext()) {
            appendStaticReadWriteSplittingRule(ruleConfig.getLoadBalancers(), iterator.next(), result);
            if (iterator.hasNext()) {
                result.append(DistSQLConstants.COMMA);
            }
        }
        result.append(DistSQLConstants.SEMI);
        return result.toString();
    }
    
    private void appendStaticReadWriteSplittingRule(final Map<String, AlgorithmConfiguration> loadBalancers,
                                                    final ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupRuleConfig, final StringBuilder stringBuilder) {
        String readDataSourceNames = getReadDataSourceNames(dataSourceGroupRuleConfig.getReadDataSourceNames());
        String transactionalReadQueryStrategy = dataSourceGroupRuleConfig.getTransactionalReadQueryStrategy().name();
        String loadBalancerType = getLoadBalancerType(loadBalancers.get(dataSourceGroupRuleConfig.getLoadBalancerName()));
        stringBuilder.append(String.format(ReadwriteSplittingDistSQLConstants.READWRITE_SPLITTING_FOR_STATIC,
                dataSourceGroupRuleConfig.getName(), dataSourceGroupRuleConfig.getWriteDataSourceName(), readDataSourceNames, transactionalReadQueryStrategy, loadBalancerType));
    }
    
    private String getReadDataSourceNames(final Collection<String> readDataSourceNames) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = readDataSourceNames.iterator();
        while (iterator.hasNext()) {
            result.append(String.format(ReadwriteSplittingDistSQLConstants.READ_RESOURCE, iterator.next()));
            if (iterator.hasNext()) {
                result.append(DistSQLConstants.COMMA);
            }
        }
        return result.toString();
    }
    
    private String getLoadBalancerType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        String loadBalancerType = AlgorithmDistSQLConverter.getAlgorithmType(algorithmConfig);
        if (!Strings.isNullOrEmpty(loadBalancerType)) {
            result.append(DistSQLConstants.COMMA).append(System.lineSeparator()).append(loadBalancerType);
        }
        return result.toString();
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
