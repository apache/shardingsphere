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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedClusterInfo;
import org.apache.shardingsphere.proxy.backend.distsql.export.ExportedMetaData;
import org.apache.shardingsphere.proxy.backend.exception.FileIOException;
import org.apache.shardingsphere.proxy.backend.util.YamlDatabaseConfigurationImportExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Import meta data updater.
 */
public final class ImportMetaDataUpdater implements RALUpdater<ImportMetaDataStatement> {
    
    private final YamlRuleConfigurationSwapperEngine ruleConfigSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    
    private final YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor = new YamlDatabaseConfigurationImportExecutor();
    
    @Override
    public void executeUpdate(final String databaseName, final ImportMetaDataStatement sqlStatement) throws SQLException {
        String jsonMetaDataConfig;
        if (sqlStatement.getFilePath().isPresent()) {
            File file = new File(sqlStatement.getFilePath().get());
            try {
                jsonMetaDataConfig = FileUtils.readFileToString(file, Charset.defaultCharset());
            } catch (final IOException ex) {
                throw new FileIOException(ex);
            }
        } else {
            jsonMetaDataConfig = new String(Base64.decodeBase64(sqlStatement.getMetaDataValue()));
        }
        ExportedClusterInfo exportedClusterInfo = JsonUtils.readValue(jsonMetaDataConfig, ExportedClusterInfo.class);
        ExportedMetaData exportedMetaData = exportedClusterInfo.getMetaData();
        importServerConfig(exportedMetaData);
        importDatabase(exportedMetaData);
    }
    
    private void importServerConfig(final ExportedMetaData exportedMetaData) {
        YamlProxyServerConfiguration yamlServerConfig = YamlEngine.unmarshal(exportedMetaData.getRules() + System.lineSeparator() + exportedMetaData.getProps(), YamlProxyServerConfiguration.class);
        if (null == yamlServerConfig) {
            return;
        }
        Collection<RuleConfiguration> rules = ruleConfigSwapperEngine.swapToRuleConfigurations(yamlServerConfig.getRules());
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterGlobalRuleConfiguration(rules);
        ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().alterProperties(yamlServerConfig.getProps());
    }
    
    private void importDatabase(final ExportedMetaData exportedMetaData) {
        for (final String each : exportedMetaData.getDatabases().values()) {
            YamlProxyDatabaseConfiguration yamlDatabaseConfig = YamlEngine.unmarshal(each, YamlProxyDatabaseConfiguration.class);
            databaseConfigImportExecutor.importDatabaseConfiguration(yamlDatabaseConfig);
        }
    }
    
    @Override
    public Class<ImportMetaDataStatement> getType() {
        return ImportMetaDataStatement.class;
    }
}
