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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import com.google.common.base.Strings;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperFactory;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.FileIOException;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * Export database configuration handler.
 */
public final class ExportDatabaseConfigurationHandler extends QueryableRALBackendHandler<ExportDatabaseConfigurationStatement> {
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singleton("result");
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        String exportedData = generateExportData(getDatabaseName());
        if (getSqlStatement().getFilePath().isPresent()) {
            String filePath = getSqlStatement().getFilePath().get();
            exportToFile(filePath, exportedData);
            return Collections.singleton(new LocalDataQueryResultRow(String.format("Successfully exported to：'%s'", filePath)));
        }
        return Collections.singleton(new LocalDataQueryResultRow(exportedData));
    }
    
    private String getDatabaseName() {
        String result = getSqlStatement().getDatabase().isPresent() ? getSqlStatement().getDatabase().get().getIdentifier().getValue() : getConnectionSession().getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(result)) {
            throw new UnknownDatabaseException(result);
        }
        return result;
    }
    
    private String generateExportData(final String databaseName) {
        StringBuilder result = new StringBuilder();
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        appendDatabaseName(databaseName, result);
        appendDataSourceConfigurations(database, result);
        appendRuleConfigurations(database.getRuleMetaData().getConfigurations(), result);
        return result.toString();
    }
    
    private void appendDatabaseName(final String databaseName, final StringBuilder stringBuilder) {
        stringBuilder.append("databaseName: ").append(databaseName).append(System.lineSeparator());
    }
    
    private void appendDataSourceConfigurations(final ShardingSphereDatabase database, final StringBuilder stringBuilder) {
        if (database.getResourceMetaData().getDataSources().isEmpty()) {
            return;
        }
        stringBuilder.append("dataSources:").append(System.lineSeparator());
        for (Entry<String, DataSource> entry : database.getResourceMetaData().getDataSources().entrySet()) {
            appendDataSourceConfiguration(entry.getKey(), entry.getValue(), stringBuilder);
        }
    }
    
    private void appendDataSourceConfiguration(final String name, final DataSource dataSource, final StringBuilder stringBuilder) {
        stringBuilder.append("  ").append(name).append(":").append(System.lineSeparator());
        DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(dataSource);
        dataSourceProps.getConnectionPropertySynonyms().getStandardProperties()
                .forEach((key, value) -> stringBuilder.append("    ").append(key).append(": ").append(value).append(System.lineSeparator()));
        dataSourceProps.getPoolPropertySynonyms().getStandardProperties().forEach((key, value) -> stringBuilder.append("    ").append(key).append(": ").append(value).append(System.lineSeparator()));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void appendRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs, final StringBuilder stringBuilder) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        stringBuilder.append("rules:").append(System.lineSeparator());
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : YamlRuleConfigurationSwapperFactory.getInstanceMapByRuleConfigurations(ruleConfigs).entrySet()) {
            if (checkRuleConfigIsEmpty(entry.getKey())) {
                continue;
            }
            stringBuilder.append(YamlEngine.marshal(Collections.singletonList(entry.getValue().swapToYamlConfiguration(entry.getKey()))));
        }
    }
    
    private boolean checkRuleConfigIsEmpty(final RuleConfiguration ruleConfig) {
        if (ruleConfig instanceof ShardingRuleConfiguration) {
            ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) ruleConfig;
            return shardingRuleConfig.getTables().isEmpty() && shardingRuleConfig.getAutoTables().isEmpty();
        } else if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
            return ((ReadwriteSplittingRuleConfiguration) ruleConfig).getDataSources().isEmpty();
        } else if (ruleConfig instanceof DatabaseDiscoveryRuleConfiguration) {
            return ((DatabaseDiscoveryRuleConfiguration) ruleConfig).getDataSources().isEmpty();
        } else if (ruleConfig instanceof EncryptRuleConfiguration) {
            return ((EncryptRuleConfiguration) ruleConfig).getTables().isEmpty();
        } else if (ruleConfig instanceof ShadowRuleConfiguration) {
            return ((ShadowRuleConfiguration) ruleConfig).getTables().isEmpty();
        } else if (ruleConfig instanceof SingleTableRuleConfiguration) {
            return !((SingleTableRuleConfiguration) ruleConfig).getDefaultDataSource().isPresent();
        }
        return false;
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void exportToFile(final String filePath, final String exportedData) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(exportedData.getBytes());
            output.flush();
        } catch (final IOException ex) {
            throw new FileIOException(ex);
        }
    }
}
