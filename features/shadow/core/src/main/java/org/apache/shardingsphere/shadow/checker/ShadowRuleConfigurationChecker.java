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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Shadow rule configuration checker.
 */
public final class ShadowRuleConfigurationChecker extends AbstractShadowRuleConfigurationChecker<ShadowRuleConfiguration> {
    
    @Override
    protected void checkShadowRuleConfiguration(final ShadowRuleConfiguration config, final Map<String, DataSource> dataSourceMap) {
        Map<String, ShadowDataSourceConfiguration> dataSources = config.getDataSources();
        checkDataSources(dataSources, dataSourceMap);
        Map<String, ShadowTableConfiguration> shadowTables = config.getTables();
        shadowTableDataSourcesAutoReferences(shadowTables, dataSources);
        shadowTableDataSourcesReferencesCheck(shadowTables, dataSources);
        Map<String, AlgorithmConfiguration> shadowAlgorithmConfigs = config.getShadowAlgorithms();
        String defaultShadowAlgorithmName = config.getDefaultShadowAlgorithmName();
        defaultShadowAlgorithmConfigurationCheck(defaultShadowAlgorithmName, shadowAlgorithmConfigs);
        shadowTableAlgorithmsAutoReferences(shadowTables, shadowAlgorithmConfigs.keySet(), defaultShadowAlgorithmName);
        shadowTableAlgorithmsReferencesCheck(shadowTables);
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getTypeClass() {
        return ShadowRuleConfiguration.class;
    }
}
