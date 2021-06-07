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

package org.apache.shardingsphere.readwritesplitting.rule.checker;

import org.apache.shardingsphere.readwritesplitting.algorithm.config.AlgorithmProvidedReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;

/**
 * Algorithm provided readwrite-splitting ruleConfiguration checker.
 */
public final class AlgorithmProvidedReadwriteSplittingRuleConfigurationChecker extends AbstractReadwriteSplittingRuleConfigurationChecker<AlgorithmProvidedReadwriteSplittingRuleConfiguration> { 
    
    @Override
    public void check(final String schemaName, final AlgorithmProvidedReadwriteSplittingRuleConfiguration config) {
        checkDataSources(schemaName, config.getDataSources());
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ALGORITHM_PROVIDER_ORDER;
    }
    
    @Override
    public Class<AlgorithmProvidedReadwriteSplittingRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedReadwriteSplittingRuleConfiguration.class;
    }
}
