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

package org.apache.shardingsphere.shadow.checker;

import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Map;

/**
 * Algorithm provided shadow rule configuration checker.
 */
public final class AlgorithmProvidedShadowRuleConfigurationChecker extends AbstractShadowRuleConfigurationChecker<AlgorithmProvidedShadowRuleConfiguration> {
    
    @Override
    protected void checkShadowRuleConfiguration(final AlgorithmProvidedShadowRuleConfiguration config) {
        Map<String, ShadowDataSourceConfiguration> dataSources = config.getDataSources();
        Map<String, ShadowTableConfiguration> shadowTables = config.getTables();
        Map<String, ShadowAlgorithm> shadowAlgorithms = config.getShadowAlgorithms();
        String defaultShadowAlgorithmName = config.getDefaultShadowAlgorithmName();
        sizeCheck(dataSources, shadowTables, defaultShadowAlgorithmName);
        shadowAlgorithmsSizeCheck(shadowAlgorithms);
        shadowTableDataSourcesAutoReferences(shadowTables, dataSources);
        shadowTableDataSourcesReferencesCheck(shadowTables, dataSources);
        defaultShadowAlgorithmCheck(defaultShadowAlgorithmName, shadowAlgorithms);
        shadowTableAlgorithmsAutoReferences(shadowTables, shadowAlgorithms.keySet(), defaultShadowAlgorithmName);
        shadowTableAlgorithmsReferencesCheck(shadowTables);
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ALGORITHM_PROVIDER_ORDER;
    }
    
    @Override
    public Class<AlgorithmProvidedShadowRuleConfiguration> getTypeClass() {
        return AlgorithmProvidedShadowRuleConfiguration.class;
    }
}
