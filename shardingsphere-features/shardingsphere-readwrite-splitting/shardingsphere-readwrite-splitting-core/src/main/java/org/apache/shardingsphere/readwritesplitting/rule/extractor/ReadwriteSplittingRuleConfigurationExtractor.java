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

package org.apache.shardingsphere.readwritesplitting.rule.extractor;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.rule.extractor.RuleConfigurationExtractor;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration extractor.
 */
public final class ReadwriteSplittingRuleConfigurationExtractor implements RuleConfigurationExtractor<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public Collection<String> extractLogicDataSources(final ReadwriteSplittingRuleConfiguration ruleConfiguration) {
        return ruleConfiguration.getDataSources().stream().map(each -> getEffectualDataSourceName(each)).collect(Collectors.toSet());
    }
    
    private static String getEffectualDataSourceName(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfiguration) {
        String autoAwareDataSourceName = dataSourceRuleConfiguration.getAutoAwareDataSourceName();
        return Strings.isNullOrEmpty(autoAwareDataSourceName) ? dataSourceRuleConfiguration.getName() : autoAwareDataSourceName;
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
