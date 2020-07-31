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

package org.apache.shardingsphere.proxy.orchestration;

import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.kernel.context.SchemaContextsAware;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.converter.AbstractConfigurationConverter;
import org.apache.shardingsphere.proxy.config.util.DataSourceConverter;
import org.apache.shardingsphere.proxy.orchestration.schema.ProxyOrchestrationSchemaContexts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration configuration converter.
 */
public final class OrchestrationConfigurationConverter extends AbstractConfigurationConverter {
    
    private final OrchestrationFacade orchestrationFacade = OrchestrationFacade.getInstance();
    
    @Override
    public ProxyConfiguration convert(final YamlProxyConfiguration yamlConfig) {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = getDataSourceParametersMap();
        Map<String, Collection<RuleConfiguration>> schemaRules = getSchemaRules();
        Authentication authentication = orchestrationFacade.getConfigCenter().loadAuthentication();
        ClusterConfiguration clusterConfig = orchestrationFacade.getConfigCenter().loadClusterConfiguration();
        MetricsConfiguration metricsConfig = getMetricsConfiguration(yamlConfig.getServerConfiguration().getMetrics());
        Properties props = orchestrationFacade.getConfigCenter().loadProperties();
        return new ProxyConfiguration(schemaDataSources, schemaRules, authentication, clusterConfig, metricsConfig, props);
    }
    
    private Map<String, Map<String, DataSourceParameter>> getDataSourceParametersMap() {
        Map<String, Map<String, DataSourceParameter>> result = new LinkedHashMap<>();
        for (String each : orchestrationFacade.getConfigCenter().getAllSchemaNames()) {
            result.put(each, DataSourceConverter.getDataSourceParameterMap(orchestrationFacade.getConfigCenter().loadDataSourceConfigurations(each)));
        }
        return result;
    }
    
    private Map<String, Collection<RuleConfiguration>> getSchemaRules() {
        Map<String, Collection<RuleConfiguration>> result = new LinkedHashMap<>();
        for (String each : orchestrationFacade.getConfigCenter().getAllSchemaNames()) {
            result.put(each, orchestrationFacade.getConfigCenter().loadRuleConfigurations(each));
        }
        return result;
    }
    
    @Override
    public SchemaContextsAware contextsAware(final SchemaContextsBuilder builder) throws SQLException {
        return new ProxyOrchestrationSchemaContexts(builder.build());
    }
    
    @Override
    public void close() {
        if (null != orchestrationFacade) {
            orchestrationFacade.close();
        }
    }
}
