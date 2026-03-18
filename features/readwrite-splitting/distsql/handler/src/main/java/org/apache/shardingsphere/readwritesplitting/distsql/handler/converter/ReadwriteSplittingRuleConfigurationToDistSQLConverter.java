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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.converter;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.constant.DistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration to DistSQL converter.
 */
public final class ReadwriteSplittingRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public String convert(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        return ruleConfig.getDataSourceGroups().isEmpty()
                ? ""
                : ReadwriteSplittingConvertDistSQLConstants.CREATE_READWRITE_SPLITTING_RULE + convertReadWriteSplittingRules(ruleConfig) + DistSQLConstants.SEMI;
    }
    
    private String convertReadWriteSplittingRules(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        return ruleConfig.getDataSourceGroups().stream().map(each -> convertReadWriteSplittingRule(each, ruleConfig.getLoadBalancers())).collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    private String convertReadWriteSplittingRule(final ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupRuleConfig, final Map<String, AlgorithmConfiguration> loadBalancers) {
        String readDataSourceNames = convertReadDataSourceNames(dataSourceGroupRuleConfig.getReadDataSourceNames());
        String transactionalReadQueryStrategy = dataSourceGroupRuleConfig.getTransactionalReadQueryStrategy().name();
        String loadBalancerType = convertLoadBalancerType(loadBalancers.get(dataSourceGroupRuleConfig.getLoadBalancerName()));
        return String.format(ReadwriteSplittingConvertDistSQLConstants.READWRITE_SPLITTING_RULE,
                dataSourceGroupRuleConfig.getName(), dataSourceGroupRuleConfig.getWriteDataSourceName(), readDataSourceNames, transactionalReadQueryStrategy, loadBalancerType);
    }
    
    private String convertReadDataSourceNames(final Collection<String> readDataSourceNames) {
        return readDataSourceNames.stream().map(each -> String.format(ReadwriteSplittingConvertDistSQLConstants.READ_DATA_SOURCE, each)).collect(Collectors.joining(DistSQLConstants.COMMA));
    }
    
    private String convertLoadBalancerType(final AlgorithmConfiguration algorithmConfig) {
        String loadBalancerType = AlgorithmDistSQLConverter.getAlgorithmType(algorithmConfig);
        return Strings.isNullOrEmpty(loadBalancerType) ? "" : DistSQLConstants.COMMA + System.lineSeparator() + loadBalancerType;
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
