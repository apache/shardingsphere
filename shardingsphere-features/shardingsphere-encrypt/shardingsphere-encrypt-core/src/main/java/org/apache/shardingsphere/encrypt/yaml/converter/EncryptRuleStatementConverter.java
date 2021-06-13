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

package org.apache.shardingsphere.encrypt.yaml.converter;

import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptColumnSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encrypt rule statement converter.
 */
public final class EncryptRuleStatementConverter {

    /**
     * Convert collection of encrypt rule segments to YAML encrypt rule configuration.
     *
     * @param encryptRules collection of encrypt rule segments
     * @return YAML encrypt rule configuration
     */
    public static YamlEncryptRuleConfiguration convert(final Collection<EncryptRuleSegment> encryptRules) {
        YamlEncryptRuleConfiguration result = new YamlEncryptRuleConfiguration();
        result.getTables().putAll(encryptRules.stream().map(EncryptRuleStatementConverter::buildYamlEncryptTableRuleConfiguration)
                .collect(Collectors.toMap(YamlEncryptTableRuleConfiguration::getName, each -> each)));
        encryptRules.forEach(each -> result.getEncryptors().putAll(buildYamlShardingSphereAlgorithmConfigurations(each)));
        return result;
    }

    private static YamlEncryptTableRuleConfiguration buildYamlEncryptTableRuleConfiguration(final EncryptRuleSegment encryptRuleSegment) {
        YamlEncryptTableRuleConfiguration result = new YamlEncryptTableRuleConfiguration();
        result.setName(encryptRuleSegment.getTableName());
        result.getColumns().putAll(encryptRuleSegment.getColumns().stream()
                .map(each -> buildYamlEncryptColumnRuleConfiguration(encryptRuleSegment.getTableName(), each))
                .collect(Collectors.toMap(YamlEncryptColumnRuleConfiguration::getLogicColumn, each -> each)));
        return result;
    }

    private static YamlEncryptColumnRuleConfiguration buildYamlEncryptColumnRuleConfiguration(final String tableName, final EncryptColumnSegment encryptColumnSegment) {
        YamlEncryptColumnRuleConfiguration result = new YamlEncryptColumnRuleConfiguration();
        result.setLogicColumn(encryptColumnSegment.getName());
        result.setCipherColumn(encryptColumnSegment.getCipherColumn());
        result.setPlainColumn(encryptColumnSegment.getPlainColumn());
        result.setEncryptorName(getEncryptorName(tableName, encryptColumnSegment.getName()));
        return result;
    }

    private static Map<String, YamlShardingSphereAlgorithmConfiguration> buildYamlShardingSphereAlgorithmConfigurations(final EncryptRuleSegment encryptRuleSegment) {
        return encryptRuleSegment.getColumns().stream().collect(Collectors
                .toMap(each -> getEncryptorName(encryptRuleSegment.getTableName(), each.getName()), each -> buildYamlShardingSphereAlgorithmConfiguration(each)));
    }

    private static YamlShardingSphereAlgorithmConfiguration buildYamlShardingSphereAlgorithmConfiguration(final EncryptColumnSegment encryptColumnSegment) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(encryptColumnSegment.getEncryptor().getAlgorithmName());
        result.setProps(encryptColumnSegment.getEncryptor().getAlgorithmProps());
        return result;
    }

    private static String getEncryptorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
}
