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

package org.apache.shardingsphere.proxy.config.converter;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.kernel.context.SchemaContextsAware;
import org.apache.shardingsphere.kernel.context.SchemaContextsBuilder;
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ShardingConfiguration;

/**
 * Default configuration converter.
 */
public final class DefaultConfigurationConverter extends AbstractConfigurationConverter {
    
    @Override
    public ProxyConfiguration convert(final ShardingConfiguration shardingConfiguration) {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        Authentication authentication = new AuthenticationYamlSwapper().swapToObject(shardingConfiguration.getServerConfiguration().getAuthentication());
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = getDataSourceParametersMap(shardingConfiguration.getRuleConfigurationMap());
        Map<String, Collection<RuleConfiguration>> schemaRules = getRuleConfigurations(shardingConfiguration.getRuleConfigurationMap());
        proxyConfiguration.setAuthentication(authentication);
        proxyConfiguration.setProps(shardingConfiguration.getServerConfiguration().getProps());
        proxyConfiguration.setSchemaDataSources(schemaDataSources);
        proxyConfiguration.setSchemaRules(schemaRules);
        proxyConfiguration.setCluster(getClusterConfiguration(shardingConfiguration.getServerConfiguration().getCluster()));
        proxyConfiguration.setMetrics(getMetricsConfiguration(shardingConfiguration.getServerConfiguration().getMetrics()));
        return proxyConfiguration;
    }
    
    @Override
    public SchemaContextsAware contextsAware(final SchemaContextsBuilder builder) throws SQLException {
        return builder.build();
    }
    
    private ClusterConfiguration getClusterConfiguration(final YamlClusterConfiguration yamlClusterConfiguration) {
        return Optional.ofNullable(yamlClusterConfiguration).map(new ClusterConfigurationYamlSwapper()::swapToObject).orElse(null);
    }
    
    @Override
    public void close() {
    }
}
