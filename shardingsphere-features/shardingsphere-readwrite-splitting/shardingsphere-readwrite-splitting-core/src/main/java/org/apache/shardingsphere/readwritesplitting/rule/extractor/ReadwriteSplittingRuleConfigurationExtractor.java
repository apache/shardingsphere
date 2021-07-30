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
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration extractor.
 */
public final class ReadwriteSplittingRuleConfigurationExtractor {
    
    /**
     * Get logic data sources.
     *
     * @param ruleConfiguration readwrite-splitting rule configuration
     * @return logic data sources
     */
    public static Set<String> extractLogicDataSources(final ReadwriteSplittingRuleConfiguration ruleConfiguration) {
        Set<String> result = ruleConfiguration.getDataSources().stream().map(each -> getEffectualDataSourceName(each)).collect(Collectors.toSet());
        return result;
    }
    
    private static String getEffectualDataSourceName(final ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfiguration) {
        String autoAwareDataSourceName = dataSourceRuleConfiguration.getAutoAwareDataSourceName();
        return Strings.isNullOrEmpty(autoAwareDataSourceName) ? dataSourceRuleConfiguration.getName() : autoAwareDataSourceName;
    }
}
