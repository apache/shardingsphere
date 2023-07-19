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

package org.apache.shardingsphere.proxy.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.proxy.backend.exception.FileIOException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;

/**
 * Export utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExportUtils {
    
    /**
     * Export configuration data to specified file.
     * 
     * @param filePath file path
     * @param exportedData exported configuration data
     * @throws FileIOException file IO exception
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void exportToFile(final String filePath, final String exportedData) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        try (OutputStream output = Files.newOutputStream(Paths.get(file.toURI()))) {
            output.write(exportedData.getBytes());
            output.flush();
        } catch (final IOException ex) {
            throw new FileIOException(ex);
        }
    }
    
    /**
     * Generate configuration data of ShardingSphere database.
     *
     * @param database ShardingSphere database
     * @return configuration data
     */
    public static String generateExportDatabaseData(final ShardingSphereDatabase database) {
        StringBuilder result = new StringBuilder();
        appendDatabaseName(database.getName(), result);
        appendDataSourceConfigurations(database, result);
        appendRuleConfigurations(database.getRuleMetaData().getConfigurations(), result);
        return result.toString();
    }
    
    private static void appendDatabaseName(final String databaseName, final StringBuilder stringBuilder) {
        stringBuilder.append("databaseName: ").append(databaseName).append(System.lineSeparator());
    }
    
    private static void appendDataSourceConfigurations(final ShardingSphereDatabase database, final StringBuilder stringBuilder) {
        if (database.getResourceMetaData().getDataSourcePropsMap().isEmpty()) {
            return;
        }
        stringBuilder.append("dataSources:").append(System.lineSeparator());
        for (Entry<String, DataSourceProperties> entry : database.getResourceMetaData().getDataSourcePropsMap().entrySet()) {
            appendDataSourceConfiguration(entry.getKey(), entry.getValue(), stringBuilder);
        }
    }
    
    private static void appendDataSourceConfiguration(final String name, final DataSourceProperties dataSourceProps, final StringBuilder stringBuilder) {
        stringBuilder.append("  ").append(name).append(':').append(System.lineSeparator());
        dataSourceProps.getConnectionPropertySynonyms().getStandardProperties()
                .forEach((key, value) -> stringBuilder.append("    ").append(key).append(": ").append(value).append(System.lineSeparator()));
        for (Entry<String, Object> entry : dataSourceProps.getPoolPropertySynonyms().getStandardProperties().entrySet()) {
            if (null != entry.getValue()) {
                stringBuilder.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append(System.lineSeparator());
            }
        }
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void appendRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs, final StringBuilder stringBuilder) {
        if (ruleConfigs.isEmpty()) {
            return;
        }
        stringBuilder.append("rules:").append(System.lineSeparator());
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : OrderedSPILoader.getServices(YamlRuleConfigurationSwapper.class, ruleConfigs).entrySet()) {
            if (checkRuleConfigIsEmpty(entry.getKey())) {
                continue;
            }
            stringBuilder.append(YamlEngine.marshal(Collections.singletonList(entry.getValue().swapToYamlConfiguration(entry.getKey()))));
        }
    }
    
    private static boolean checkRuleConfigIsEmpty(final RuleConfiguration ruleConfig) {
        if (ruleConfig instanceof ShardingRuleConfiguration) {
            ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) ruleConfig;
            return shardingRuleConfig.getTables().isEmpty() && shardingRuleConfig.getAutoTables().isEmpty();
        } else if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
            return ((ReadwriteSplittingRuleConfiguration) ruleConfig).getDataSources().isEmpty();
        } else if (ruleConfig instanceof EncryptRuleConfiguration) {
            return ((EncryptRuleConfiguration) ruleConfig).getTables().isEmpty();
        } else if (ruleConfig instanceof CompatibleEncryptRuleConfiguration) {
            return ((CompatibleEncryptRuleConfiguration) ruleConfig).getTables().isEmpty();
        } else if (ruleConfig instanceof ShadowRuleConfiguration) {
            return ((ShadowRuleConfiguration) ruleConfig).getTables().isEmpty();
        } else if (ruleConfig instanceof MaskRuleConfiguration) {
            return ((MaskRuleConfiguration) ruleConfig).getTables().isEmpty();
        } else if (ruleConfig instanceof SingleRuleConfiguration) {
            return !((SingleRuleConfiguration) ruleConfig).getDefaultDataSource().isPresent();
        }
        return false;
    }
}
