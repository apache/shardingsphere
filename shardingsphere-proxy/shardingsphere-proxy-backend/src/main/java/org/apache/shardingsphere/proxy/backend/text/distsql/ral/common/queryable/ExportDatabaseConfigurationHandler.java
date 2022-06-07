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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import com.google.common.base.Strings;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.swapper.DatabaseDiscoveryRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.exception.DatabaseNotExistedException;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.ShardingRuleConfigurationYamlSwapper;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Export database configuration handler.
 */
public final class ExportDatabaseConfigurationHandler extends QueryableRALBackendHandler<ExportDatabaseConfigurationStatement, ExportDatabaseConfigurationHandler> {
    
    private static final String RESULT = "result";
    
    private static final String COLON = ":";
    
    private static final String SPACE = " ";
    
    private static final String NEWLINE = "\n";
    
    private static final String COLON_SPACE = COLON + SPACE;
    
    private static final String COLON_NEWLINE = COLON + NEWLINE;
    
    private static final String INDENT = SPACE + SPACE;
    
    private static final int ZERO = 0;
    
    private static final int ONE = 1;
    
    private static final int TWO = 2;
    
    private static final String SHARDING = "sharding";
    
    private static final String READWRITE_SPLITTING = "readwrite_splitting";
    
    private static final String DB_DISCOVERY = "db_discovery";
    
    private static final String ENCRYPT = "encrypt";
    
    private static final String SHADOW = "shadow";
    
    private static final Map<String, Class<? extends RuleConfiguration>> FEATURE_MAP = new HashMap<>(5, 1);
    
    private ConnectionSession connectionSession;
    
    static {
        FEATURE_MAP.put(SHARDING, ShardingRuleConfiguration.class);
        FEATURE_MAP.put(READWRITE_SPLITTING, ReadwriteSplittingRuleConfiguration.class);
        FEATURE_MAP.put(DB_DISCOVERY, DatabaseDiscoveryRuleConfiguration.class);
        FEATURE_MAP.put(ENCRYPT, EncryptRuleConfiguration.class);
        FEATURE_MAP.put(SHADOW, ShadowRuleConfiguration.class);
    }
    
    @Override
    public ExportDatabaseConfigurationHandler init(final HandlerParameter<ExportDatabaseConfigurationStatement> parameter) {
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singletonList(RESULT);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        String databaseName = getDatabaseName();
        ShardingSphereDatabase database = ProxyContext.getInstance().getDatabase(databaseName);
        StringBuilder result = new StringBuilder();
        configItem(ZERO, "databaseName", databaseName, result);
        getDataSourcesConfig(database, result);
        getRuleConfigurations(database.getRuleMetaData().getConfigurations(), result);
        if (!sqlStatement.getFilePath().isPresent()) {
            return Collections.singleton(Collections.singletonList(result.toString()));
        }
        File outFile = new File(sqlStatement.getFilePath().get());
        if (!outFile.exists()) {
            outFile.getParentFile().mkdirs();
        }
        try (FileOutputStream stream = new FileOutputStream(outFile)) {
            stream.write(result.toString().getBytes());
            stream.flush();
        } catch (final IOException ex) {
            throw new ShardingSphereException(ex);
        }
        return Collections.singleton(Collections.singletonList(String.format("Successfully exported to：'%s'", sqlStatement.getFilePath().get())));
    }
    
    private void getDataSourcesConfig(final ShardingSphereDatabase database, final StringBuilder result) {
        if (null == database.getResource().getDataSources() || database.getResource().getDataSources().isEmpty()) {
            return;
        }
        configItem(ZERO, "dataSources", result);
        for (Entry<String, DataSource> each : database.getResource().getDataSources().entrySet()) {
            configItem(ONE, each.getKey(), result);
            DataSourceProperties dataSourceProps = DataSourcePropertiesCreator.create(each.getValue());
            dataSourceProps.getConnectionPropertySynonyms().getStandardProperties().forEach((key, value) -> configItem(TWO, key, value, result));
            dataSourceProps.getPoolPropertySynonyms().getStandardProperties().forEach((key, value) -> configItem(TWO, key, value, result));
        }
    }
    
    private void getRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs, final StringBuilder result) {
        if (null == ruleConfigs || ruleConfigs.isEmpty()) {
            return;
        }
        configItem(ZERO, "rules", result);
        ruleConfigs.forEach(each -> {
            getRulesConfigForSharding(each, result);
            getRulesConfigForReadwriteSplitting(each, result);
            getRulesConfigForDBDiscovery(each, result);
            getRulesConfigForEncrypt(each, result);
            getRulesConfigForShadow(each, result);
        });
    }
    
    private boolean matchFeature(final RuleConfiguration ruleConfig, final String feature) {
        return null != ruleConfig && ruleConfig.getClass().getCanonicalName().equalsIgnoreCase(FEATURE_MAP.get(feature).getCanonicalName());
    }
    
    private void getRulesConfigForSharding(final RuleConfiguration ruleConfig, final StringBuilder result) {
        if (matchFeature(ruleConfig, SHARDING)) {
            result.append(YamlEngine.marshal(Collections.singletonList(new ShardingRuleConfigurationYamlSwapper().swapToYamlConfiguration((ShardingRuleConfiguration) ruleConfig))));
        }
    }
    
    private void getRulesConfigForReadwriteSplitting(final RuleConfiguration ruleConfig, final StringBuilder result) {
        if (matchFeature(ruleConfig, READWRITE_SPLITTING)) {
            result.append(YamlEngine.marshal(
                    Collections.singletonList(new ReadwriteSplittingRuleConfigurationYamlSwapper().swapToYamlConfiguration((ReadwriteSplittingRuleConfiguration) ruleConfig))));
        }
    }
    
    private void getRulesConfigForDBDiscovery(final RuleConfiguration ruleConfig, final StringBuilder result) {
        if (matchFeature(ruleConfig, DB_DISCOVERY)) {
            result.append(YamlEngine.marshal(Collections.singletonList(new DatabaseDiscoveryRuleConfigurationYamlSwapper().swapToYamlConfiguration((DatabaseDiscoveryRuleConfiguration) ruleConfig))));
        }
    }
    
    private void getRulesConfigForEncrypt(final RuleConfiguration ruleConfig, final StringBuilder result) {
        if (matchFeature(ruleConfig, ENCRYPT)) {
            result.append(YamlEngine.marshal(Collections.singletonList(new EncryptRuleConfigurationYamlSwapper().swapToYamlConfiguration((EncryptRuleConfiguration) ruleConfig))));
        }
    }
    
    private void getRulesConfigForShadow(final RuleConfiguration ruleConfig, final StringBuilder result) {
        if (matchFeature(ruleConfig, SHADOW)) {
            result.append(YamlEngine.marshal(Collections.singletonList(new ShadowRuleConfigurationYamlSwapper().swapToYamlConfiguration((ShadowRuleConfiguration) ruleConfig))));
        }
    }
    
    private void configItem(final int indent, final Object key, final StringBuilder result) {
        result.append(indent(indent)).append(key).append(COLON_NEWLINE);
    }
    
    private void configItem(final int indent, final Object key, final Object value, final StringBuilder result) {
        result.append(indent(indent)).append(key).append(COLON_SPACE).append(value).append(NEWLINE);
    }
    
    private String indent(final int count) {
        if (count <= 0) {
            return "";
        }
        if (1 == count) {
            return INDENT;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(INDENT);
        }
        return result.toString();
    }
    
    private String getDatabaseName() {
        String result = sqlStatement.getDatabase().isPresent() ? sqlStatement.getDatabase().get().getIdentifier().getValue() : connectionSession.getDatabaseName();
        if (Strings.isNullOrEmpty(result)) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllDatabaseNames().contains(result)) {
            throw new DatabaseNotExistedException(result);
        }
        return result;
    }
}
