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

import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.exception.metadata.MissingRequiredProductionDataSourceException;
import org.apache.shardingsphere.shadow.exception.metadata.MissingRequiredShadowDataSourceException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Shadow rule configuration checker.
 */
public final class ShadowRuleConfigurationChecker implements DatabaseRuleConfigurationChecker<ShadowRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ShadowRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkDataSources(ruleConfig.getDataSources(), dataSourceMap, databaseName);
    }
    
    private void checkDataSources(final Collection<ShadowDataSourceConfiguration> shadowDataSources, final Map<String, DataSource> dataSourceMap, final String databaseName) {
        for (ShadowDataSourceConfiguration each : shadowDataSources) {
            ShardingSpherePreconditions.checkContainsKey(dataSourceMap, each.getProductionDataSourceName(), () -> new MissingRequiredProductionDataSourceException(databaseName));
            ShardingSpherePreconditions.checkContainsKey(dataSourceMap, each.getShadowDataSourceName(), () -> new MissingRequiredShadowDataSourceException(databaseName));
        }
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ShadowRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        for (ShadowDataSourceConfiguration each : ruleConfig.getDataSources()) {
            result.add(each.getShadowDataSourceName());
            result.add(each.getProductionDataSourceName());
        }
        return result;
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
