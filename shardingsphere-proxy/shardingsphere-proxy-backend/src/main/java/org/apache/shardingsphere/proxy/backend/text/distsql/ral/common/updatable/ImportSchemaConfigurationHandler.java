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
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.swapper.DatabaseDiscoveryRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.ImportSchemaConfigurationStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.ImportResourceNotExistedException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.exception.ImportSchemaNotExistedException;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxySchemaConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker.ReadwriteSplittingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker.ShardingRuleConfigurationImportChecker;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Import schema configuration handler.
 */
public final class ImportSchemaConfigurationHandler extends UpdatableRALBackendHandler<ImportSchemaConfigurationStatement, ImportSchemaConfigurationHandler> {

    private final DataSourcePropertiesValidator validator = new DataSourcePropertiesValidator();

    private final ShardingRuleConfigurationImportChecker shardingRuleConfigurationImportChecker = new ShardingRuleConfigurationImportChecker();
    
    private final ReadwriteSplittingRuleConfigurationImportChecker readwriteSplittingRuleConfigurationImportChecker = new ReadwriteSplittingRuleConfigurationImportChecker();
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();

    private void alterResourcesConfig(final String schemaName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) throws DistSQLException {
        Map<String, DataSourceProperties> toBeUpdatedResourcePropsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> each : yamlDataSourceMap.entrySet()) {
            DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(each.getValue(), schemaName, each.getKey(),
                    false));
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

    private void alterRulesConfig(final String schemaName, final Collection<YamlRuleConfiguration> yamlRuleConfigurations) throws DistSQLException {
        if (null == yamlRuleConfigurations || yamlRuleConfigurations.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> toBeUpdatedRuleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        ShardingSphereMetaData shardingSphereMetaData = metaDataContexts.getMetaData(schemaName);
        for (YamlRuleConfiguration each : yamlRuleConfigurations) {
            if (each instanceof YamlShardingRuleConfiguration) {
                ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfigurationYamlSwapper().swapToObject((YamlShardingRuleConfiguration) each);
                shardingRuleConfigurationImportChecker.check(shardingSphereMetaData, shardingRuleConfiguration);
                toBeUpdatedRuleConfigs.add(shardingRuleConfiguration);
            } else if (each instanceof YamlReadwriteSplittingRuleConfiguration) {
                ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = new ReadwriteSplittingRuleConfigurationYamlSwapper()
                        .swapToObject((YamlReadwriteSplittingRuleConfiguration) each);
                readwriteSplittingRuleConfigurationImportChecker.check(shardingSphereMetaData, readwriteSplittingRuleConfiguration);
                toBeUpdatedRuleConfigs.add(readwriteSplittingRuleConfiguration);
            } else if (each instanceof YamlDatabaseDiscoveryRuleConfiguration) {
                DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration = new DatabaseDiscoveryRuleConfigurationYamlSwapper().swapToObject((YamlDatabaseDiscoveryRuleConfiguration) each);
                // TODO check
                toBeUpdatedRuleConfigs.add(databaseDiscoveryRuleConfiguration);
            } else if (each instanceof YamlEncryptRuleConfiguration) {
                EncryptRuleConfiguration encryptRuleConfiguration = new EncryptRuleConfigurationYamlSwapper().swapToObject((YamlEncryptRuleConfiguration) each);
                // TODO check
                toBeUpdatedRuleConfigs.add(encryptRuleConfiguration);
            } else if (each instanceof YamlShadowRuleConfiguration) {
                ShadowRuleConfiguration shadowRuleConfiguration = new ShadowRuleConfigurationYamlSwapper().swapToObject((YamlShadowRuleConfiguration) each);
                // TODO check
                toBeUpdatedRuleConfigs.add(shadowRuleConfiguration);
            }
        }
        shardingSphereMetaData.getRuleMetaData().getConfigurations().clear();
        shardingSphereMetaData.getRuleMetaData().getConfigurations().addAll(toBeUpdatedRuleConfigs);
        ProxyContext.getInstance().getContextManager().renewMetaDataContexts(metaDataContexts);
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        metaDataPersistService.ifPresent(op -> op.getSchemaRuleService().persist(schemaName, toBeUpdatedRuleConfigs));
    }

    private void checkSchemaName(final String schemaName) {
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
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
        String schemaName = yamlConfig.getSchemaName();
        DistSQLException.predictionThrow(!Strings.isNullOrEmpty(schemaName), () -> new ImportSchemaNotExistedException(yamlFile.getName()));
        checkSchemaName(schemaName);
        DistSQLException.predictionThrow(null != yamlConfig.getDataSources() && !yamlConfig.getDataSources().isEmpty(), () -> new ImportResourceNotExistedException(yamlFile.getName()));
        alterResourcesConfig(schemaName, yamlConfig.getDataSources());
        alterRulesConfig(schemaName, yamlConfig.getRules());
    }
}
