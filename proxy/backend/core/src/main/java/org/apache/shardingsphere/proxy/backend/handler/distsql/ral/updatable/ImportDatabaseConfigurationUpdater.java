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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.swapper.YamlDatabaseDiscoveryRuleConfigurationSwapper;
import org.apache.shardingsphere.distsql.handler.exception.DistSQLException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.YamlEncryptRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.YamlMaskRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.FileIOException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.DatabaseDiscoveryRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.EncryptRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.MaskRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ReadwriteSplittingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShadowRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShardingRuleConfigurationImportChecker;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.YamlReadwriteSplittingRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.YamlShadowRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Import database configuration updater.
 */
public final class ImportDatabaseConfigurationUpdater implements RALUpdater<ImportDatabaseConfigurationStatement> {
    
    private final ShardingRuleConfigurationImportChecker shardingRuleConfigurationImportChecker = new ShardingRuleConfigurationImportChecker();
    
    private final ReadwriteSplittingRuleConfigurationImportChecker readwriteSplittingRuleConfigurationImportChecker = new ReadwriteSplittingRuleConfigurationImportChecker();
    
    private final DatabaseDiscoveryRuleConfigurationImportChecker databaseDiscoveryRuleConfigurationImportChecker = new DatabaseDiscoveryRuleConfigurationImportChecker();
    
    private final EncryptRuleConfigurationImportChecker encryptRuleConfigurationImportChecker = new EncryptRuleConfigurationImportChecker();
    
    private final ShadowRuleConfigurationImportChecker shadowRuleConfigurationImportChecker = new ShadowRuleConfigurationImportChecker();
    
    private final MaskRuleConfigurationImportChecker maskRuleConfigurationImportChecker = new MaskRuleConfigurationImportChecker();
    
    private final YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlProxyDataSourceConfigurationSwapper();
    
    private final DataSourcePropertiesValidateHandler validateHandler = new DataSourcePropertiesValidateHandler();
    
    @Override
    public void executeUpdate(final String databaseName, final ImportDatabaseConfigurationStatement sqlStatement) throws SQLException {
        File file = new File(sqlStatement.getFilePath());
        YamlProxyDatabaseConfiguration yamlConfig;
        try {
            yamlConfig = YamlEngine.unmarshal(file, YamlProxyDatabaseConfiguration.class);
        } catch (final IOException ex) {
            throw new FileIOException(ex);
        }
        String yamlDatabaseName = yamlConfig.getDatabaseName();
        checkDatabase(yamlDatabaseName, file);
        checkDataSource(yamlConfig.getDataSources(), file);
        addDatabase(yamlDatabaseName);
        addResources(yamlDatabaseName, yamlConfig.getDataSources());
        try {
            addRules(yamlDatabaseName, yamlConfig.getRules());
        } catch (final DistSQLException ex) {
            dropDatabase(yamlDatabaseName);
            throw ex;
        }
    }
    
    private void checkDatabase(final String databaseName, final File file) {
        Preconditions.checkNotNull(databaseName, "Property `databaseName` in file `%s` is required", file.getName());
        if (ProxyContext.getInstance().databaseExists(databaseName)) {
            Preconditions.checkState(ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources().isEmpty(), "Database `%s` exists and is not empty", databaseName);
        }
    }
    
    private void checkDataSource(final Map<String, YamlProxyDataSourceConfiguration> dataSources, final File file) {
        Preconditions.checkState(!dataSources.isEmpty(), "Data source configurations in file `%s` is required", file.getName());
    }
    
    private void addDatabase(final String databaseName) {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().createDatabase(databaseName);
    }
    
    private void addResources(final String databaseName, final Map<String, YamlProxyDataSourceConfiguration> yamlDataSourceMap) {
        Map<String, DataSourceProperties> dataSourcePropsMap = new LinkedHashMap<>(yamlDataSourceMap.size(), 1);
        for (Entry<String, YamlProxyDataSourceConfiguration> entry : yamlDataSourceMap.entrySet()) {
            dataSourcePropsMap.put(entry.getKey(), DataSourcePropertiesCreator.create(HikariDataSource.class.getName(), dataSourceConfigSwapper.swap(entry.getValue())));
        }
        validateHandler.validate(dataSourcePropsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().registerStorageUnits(databaseName, dataSourcePropsMap);
        } catch (final SQLException ex) {
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
    }
    
    private void addRules(final String databaseName, final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        if (yamlRuleConfigs.isEmpty()) {
            return;
        }
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        InstanceContext instanceContext = ProxyContext.getInstance().getContextManager().getInstanceContext();
        ShardingSphereDatabase database = metaDataContexts.getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = database.getRuleMetaData().getRules();
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            if (each instanceof YamlShardingRuleConfiguration) {
                ShardingRuleConfiguration shardingRuleConfig = new YamlShardingRuleConfigurationSwapper().swapToObject((YamlShardingRuleConfiguration) each);
                shardingRuleConfigurationImportChecker.check(database, shardingRuleConfig);
                ruleConfigs.add(shardingRuleConfig);
                rules.add(new ShardingRule(shardingRuleConfig, database.getResourceMetaData().getDataSources().keySet(), instanceContext));
            } else if (each instanceof YamlReadwriteSplittingRuleConfiguration) {
                ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig = new YamlReadwriteSplittingRuleConfigurationSwapper().swapToObject((YamlReadwriteSplittingRuleConfiguration) each);
                readwriteSplittingRuleConfigurationImportChecker.check(database, readwriteSplittingRuleConfig);
                ruleConfigs.add(readwriteSplittingRuleConfig);
                rules.add(new ReadwriteSplittingRule(databaseName, readwriteSplittingRuleConfig, rules, instanceContext));
            } else if (each instanceof YamlDatabaseDiscoveryRuleConfiguration) {
                DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = new YamlDatabaseDiscoveryRuleConfigurationSwapper().swapToObject((YamlDatabaseDiscoveryRuleConfiguration) each);
                databaseDiscoveryRuleConfigurationImportChecker.check(database, databaseDiscoveryRuleConfig);
                ruleConfigs.add(databaseDiscoveryRuleConfig);
                rules.add(new DatabaseDiscoveryRule(databaseName, database.getResourceMetaData().getDataSources(), databaseDiscoveryRuleConfig, instanceContext));
            } else if (each instanceof YamlEncryptRuleConfiguration) {
                EncryptRuleConfiguration encryptRuleConfig = new YamlEncryptRuleConfigurationSwapper().swapToObject((YamlEncryptRuleConfiguration) each);
                encryptRuleConfigurationImportChecker.check(database, encryptRuleConfig);
                ruleConfigs.add(encryptRuleConfig);
                rules.add(new EncryptRule(encryptRuleConfig));
            } else if (each instanceof YamlShadowRuleConfiguration) {
                ShadowRuleConfiguration shadowRuleConfig = new YamlShadowRuleConfigurationSwapper().swapToObject((YamlShadowRuleConfiguration) each);
                shadowRuleConfigurationImportChecker.check(database, shadowRuleConfig);
                ruleConfigs.add(shadowRuleConfig);
                rules.add(new ShadowRule(shadowRuleConfig));
            } else if (each instanceof YamlMaskRuleConfiguration) {
                MaskRuleConfiguration maskRuleConfig = new YamlMaskRuleConfigurationSwapper().swapToObject((YamlMaskRuleConfiguration) each);
                maskRuleConfigurationImportChecker.check(database, maskRuleConfig);
                ruleConfigs.add(maskRuleConfig);
                rules.add(new MaskRule(maskRuleConfig));
            }
        }
        metaDataContexts.getPersistService().getDatabaseRulePersistService().persist(metaDataContexts.getMetaData().getActualDatabaseName(databaseName), ruleConfigs);
    }
    
    private void dropDatabase(final String databaseName) {
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().dropDatabase(databaseName);
    }
    
    @Override
    public String getType() {
        return ImportDatabaseConfigurationStatement.class.getName();
    }
}
