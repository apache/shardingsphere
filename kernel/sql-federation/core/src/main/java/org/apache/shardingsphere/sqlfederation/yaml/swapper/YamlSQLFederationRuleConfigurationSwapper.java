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

package org.apache.shardingsphere.sqlfederation.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationCacheOption;
import org.apache.shardingsphere.sqlfederation.config.SQLFederationRuleConfiguration;
import org.apache.shardingsphere.sqlfederation.constant.SQLFederationOrder;
import org.apache.shardingsphere.sqlfederation.yaml.config.YamlSQLFederationRuleConfiguration;

/**
 * YAML SQL federation rule configuration swapper.
 */
public final class YamlSQLFederationRuleConfigurationSwapper implements YamlRuleConfigurationSwapper<YamlSQLFederationRuleConfiguration, SQLFederationRuleConfiguration> {
    
    private final YamlSQLFederationExecutionPlanCacheConfigurationSwapper executionPlanCacheConfigSwapper = new YamlSQLFederationExecutionPlanCacheConfigurationSwapper();
    
    @Override
    public YamlSQLFederationRuleConfiguration swapToYamlConfiguration(final SQLFederationRuleConfiguration data) {
        YamlSQLFederationRuleConfiguration result = new YamlSQLFederationRuleConfiguration();
        result.setSqlFederationEnabled(data.isSqlFederationEnabled());
        result.setAllQueryUseSQLFederation(data.isAllQueryUseSQLFederation());
        result.setExecutionPlanCache(executionPlanCacheConfigSwapper.swapToYamlConfiguration(data.getExecutionPlanCache()));
        return result;
    }
    
    @Override
    public SQLFederationRuleConfiguration swapToObject(final YamlSQLFederationRuleConfiguration yamlConfig) {
        SQLFederationCacheOption executionPlanCacheConfig = executionPlanCacheConfigSwapper.swapToObject(yamlConfig.getExecutionPlanCache());
        return new SQLFederationRuleConfiguration(yamlConfig.isSqlFederationEnabled(), yamlConfig.isAllQueryUseSQLFederation(), executionPlanCacheConfig);
    }
    
    @Override
    public Class<SQLFederationRuleConfiguration> getTypeClass() {
        return SQLFederationRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SQL_FEDERATION";
    }
    
    @Override
    public int getOrder() {
        return SQLFederationOrder.ORDER;
    }
}
