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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.swapper.DatabaseDiscoveryRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.ImportSchemaConfigurationStatement;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxySchemaConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Import schema configuration handler.
 */
public final class ImportSchemaConfigurationHandler extends UpdatableRALBackendHandler<ImportSchemaConfigurationStatement, ImportSchemaConfigurationHandler> {
    
    private final DataSourcePropertiesValidator validator = new DataSourcePropertiesValidator();
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private ConnectionSession connectionSession;
    
    @Override
    public ImportSchemaConfigurationHandler init(final HandlerParameter<ImportSchemaConfigurationStatement> parameter) {
        initStatement(parameter.getStatement());
        connectionSession = parameter.getConnectionSession();
        return this;
    }
    
    private void alterResourcesConfig(final String schemaName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) throws DistSQLException {
        if (null == yamlDataSourceMap || yamlDataSourceMap.isEmpty()) {
            return;
        }
        Map<String, DataSourceProperties> toBeUpdatedResourcePropsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> each : yamlDataSourceMap.entrySet()) {
            DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(each.getValue()));
            toBeUpdatedResourcePropsMap.put(each.getKey(), dataSourceProps);
        }
        try {
            validator.validate(toBeUpdatedResourcePropsMap);
            Collection<String> currentResourceNames = new LinkedList<>(ProxyContext.getInstance().getMetaData(schemaName).getResource().getDataSources().keySet());
            Collection<String> toBeDroppedResourceNames = currentResourceNames.stream().filter(each -> !toBeUpdatedResourcePropsMap.containsKey(each)).collect(Collectors.toSet());
            ProxyContext.getInstance().getContextManager().addResource(schemaName, toBeUpdatedResourcePropsMap);
            if (toBeDroppedResourceNames.size() > 0) {
                ProxyContext.getInstance().getContextManager().dropResource(schemaName, toBeDroppedResourceNames);
            }
        } catch (final SQLException ex) {
            throw new InvalidResourcesException(toBeUpdatedResourcePropsMap.keySet());
        }
    }
    
    private void alterRulesConfig(final String schemaName, final Collection<YamlRuleConfiguration> yamlRuleConfigurations) {
        if (null == yamlRuleConfigurations || yamlRuleConfigurations.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> toBeUpdatedRuleConfigs = new LinkedList<>();
        yamlRuleConfigurations.forEach(each -> {
            if (each instanceof YamlShardingRuleConfiguration) {
                toBeUpdatedRuleConfigs.add(new ShardingRuleConfigurationYamlSwapper().swapToObject((YamlShardingRuleConfiguration) each));
            } else if (each instanceof YamlReadwriteSplittingRuleConfiguration) {
                toBeUpdatedRuleConfigs.add(new ReadwriteSplittingRuleConfigurationYamlSwapper().swapToObject((YamlReadwriteSplittingRuleConfiguration) each));
            } else if (each instanceof YamlDatabaseDiscoveryRuleConfiguration) {
                toBeUpdatedRuleConfigs.add(new DatabaseDiscoveryRuleConfigurationYamlSwapper().swapToObject((YamlDatabaseDiscoveryRuleConfiguration) each));
            } else if (each instanceof YamlEncryptRuleConfiguration) {
                toBeUpdatedRuleConfigs.add(new EncryptRuleConfigurationYamlSwapper().swapToObject((YamlEncryptRuleConfiguration) each));
            } else if (each instanceof YamlShadowRuleConfiguration) {
                toBeUpdatedRuleConfigs.add(new ShadowRuleConfigurationYamlSwapper().swapToObject((YamlShadowRuleConfiguration) each));
            }
        });
        ProxyContext.getInstance().getContextManager().alterRuleConfiguration(schemaName, toBeUpdatedRuleConfigs);
    }
    
    private String getSchemaName(final YamlProxySchemaConfiguration yamlConfig) {
        String result = !Strings.isNullOrEmpty(yamlConfig.getSchemaName()) ? yamlConfig.getSchemaName() : connectionSession.getSchemaName();
        if (null == result) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(result)) {
            throw new SchemaNotExistedException(result);
        }
        return result;
    }
    
    @Override
    protected void update(final ContextManager contextManager, final ImportSchemaConfigurationStatement sqlStatement) throws DistSQLException {
        if (!sqlStatement.getFilePath().isPresent()) {
            return;
        }
        File yamlFile = new File(sqlStatement.getFilePath().get());
        YamlProxySchemaConfiguration yamlConfig;
        try {
            yamlConfig = YamlEngine.unmarshal(yamlFile, YamlProxySchemaConfiguration.class);
            if (null == yamlConfig) {
                return;
            }
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
        String schemaName = getSchemaName(yamlConfig);
        alterResourcesConfig(schemaName, yamlConfig.getDataSources());
        alterRulesConfig(schemaName, yamlConfig.getRules());
    }
}
