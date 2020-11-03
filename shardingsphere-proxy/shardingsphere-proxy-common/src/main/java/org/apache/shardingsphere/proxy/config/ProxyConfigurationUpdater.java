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

package org.apache.shardingsphere.proxy.config;

import org.apache.shardingsphere.governance.core.facade.GovernanceFacade;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;

import java.util.Collection;
import java.util.Map;

/**
 * Proxy configuration updater.
 */
public final class ProxyConfigurationUpdater {
    
    private static final GovernanceFacade GOVERNANCE_FACADE = new GovernanceFacade();
    
    /**
     * Get governance facade.
     *
     * @return governance facade
     */
    public static GovernanceFacade getGovernanceFacade() {
        return GOVERNANCE_FACADE;
    }
    
    /**
     * Update configurations.
     *
     * @param dataSourceConfigMap data source config map
     * @param schemaRuleMap schema rule map
     */
    public static void update(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigMap, final Map<String, Collection<RuleConfiguration>> schemaRuleMap) {
        GOVERNANCE_FACADE.updateConfigurations(dataSourceConfigMap, schemaRuleMap);
    }
}
