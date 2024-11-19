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

package org.apache.shardingsphere.encrypt.distsql.handler.converter;

import org.apache.shardingsphere.distsql.handler.constant.DistSQLConstants;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.AlgorithmDistSQLConverter;
import org.apache.shardingsphere.distsql.handler.engine.query.ral.convert.RuleConfigurationToDistSQLConverter;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encrypt rule configuration to DistSQL converter.
 */
public final class EncryptRuleConfigurationToDistSQLConverter implements RuleConfigurationToDistSQLConverter<EncryptRuleConfiguration> {
    
    @Override
    public String convert(final EncryptRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().isEmpty() ? "" : EncryptConvertDistSQLConstants.CREATE_ENCRYPT_RULE + convertEncryptTables(ruleConfig) + DistSQLConstants.SEMI;
    }
    
    private String convertEncryptTables(final EncryptRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().stream().map(each -> convertEncryptTable(ruleConfig, each)).collect(Collectors.joining(DistSQLConstants.COMMA + System.lineSeparator()));
    }
    
    private String convertEncryptTable(final EncryptRuleConfiguration ruleConfig, final EncryptTableRuleConfiguration tableRuleConfig) {
        return String.format(EncryptConvertDistSQLConstants.ENCRYPT_TABLE, tableRuleConfig.getName(), convertEncryptColumns(tableRuleConfig.getColumns(), ruleConfig.getEncryptors()));
    }
    
    private String convertEncryptColumns(final Collection<EncryptColumnRuleConfiguration> columnRuleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        return columnRuleConfigs.stream().map(each -> convertEncryptColumn(each, encryptors)).collect(Collectors.joining(DistSQLConstants.COMMA + System.lineSeparator()));
    }
    
    private String convertEncryptColumn(final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        return String.format(EncryptConvertDistSQLConstants.ENCRYPT_COLUMN, columnRuleConfig.getName(), convertColumns(columnRuleConfig), convertEncryptAlgorithms(columnRuleConfig, encryptors));
    }
    
    private String convertColumns(final EncryptColumnRuleConfiguration columnRuleConfig) {
        StringBuilder result = new StringBuilder();
        result.append(String.format(EncryptConvertDistSQLConstants.CIPHER, columnRuleConfig.getCipher().getName()));
        columnRuleConfig.getAssistedQuery()
                .ifPresent(optional -> result.append(DistSQLConstants.COMMA).append(' ').append(String.format(EncryptConvertDistSQLConstants.ASSISTED_QUERY_COLUMN, optional.getName())));
        columnRuleConfig.getLikeQuery()
                .ifPresent(optional -> result.append(DistSQLConstants.COMMA).append(' ').append(String.format(EncryptConvertDistSQLConstants.LIKE_QUERY_COLUMN, optional.getName())));
        return result.toString();
    }
    
    private String convertEncryptAlgorithms(final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        result.append(String.format(EncryptConvertDistSQLConstants.ENCRYPT_ALGORITHM, AlgorithmDistSQLConverter.getAlgorithmType(encryptors.get(columnRuleConfig.getCipher().getEncryptorName()))));
        columnRuleConfig.getAssistedQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).ifPresent(optional -> result.append(DistSQLConstants.COMMA).append(' ')
                .append(String.format(EncryptConvertDistSQLConstants.ASSISTED_QUERY_ALGORITHM, AlgorithmDistSQLConverter.getAlgorithmType(encryptors.get(optional)))));
        columnRuleConfig.getLikeQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).ifPresent(optional -> result.append(DistSQLConstants.COMMA).append(' ')
                .append(String.format(EncryptConvertDistSQLConstants.LIKE_QUERY_ALGORITHM, AlgorithmDistSQLConverter.getAlgorithmType(encryptors.get(optional)))));
        return result.toString();
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getType() {
        return EncryptRuleConfiguration.class;
    }
}
