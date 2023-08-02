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

package org.apache.shardingsphere.encrypt.distsql.handler.provider;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.handler.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.handler.constant.EncryptDistSQLConstants;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Encrypt convert rule configuration provider.
 */
public final class EncryptConvertRuleConfigurationProvider implements ConvertRuleConfigurationProvider {
    
    @Override
    public String convert(final RuleConfiguration ruleConfig) {
        return getEncryptDistSQL((EncryptRuleConfiguration) ruleConfig);
    }
    
    private String getEncryptDistSQL(final EncryptRuleConfiguration ruleConfig) {
        if (ruleConfig.getTables().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(EncryptDistSQLConstants.CREATE_ENCRYPT);
        Iterator<EncryptTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            EncryptTableRuleConfiguration tableRuleConfig = iterator.next();
            result.append(String.format(EncryptDistSQLConstants.ENCRYPT, tableRuleConfig.getName(), getEncryptColumns(tableRuleConfig.getColumns(), ruleConfig.getEncryptors())));
            if (iterator.hasNext()) {
                result.append(EncryptDistSQLConstants.COMMA).append(System.lineSeparator());
            }
        }
        result.append(EncryptDistSQLConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private String getEncryptColumns(final Collection<EncryptColumnRuleConfiguration> ruleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        Iterator<EncryptColumnRuleConfiguration> iterator = ruleConfigs.iterator();
        while (iterator.hasNext()) {
            EncryptColumnRuleConfiguration columnRuleConfig = iterator.next();
            result.append(String.format(EncryptDistSQLConstants.ENCRYPT_COLUMN, columnRuleConfig.getName(), getColumns(columnRuleConfig), getEncryptAlgorithms(columnRuleConfig, encryptors)));
            if (iterator.hasNext()) {
                result.append(EncryptDistSQLConstants.COMMA).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    private String getColumns(final EncryptColumnRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        String cipherColumnName = ruleConfig.getCipher().getName();
        if (!Strings.isNullOrEmpty(cipherColumnName)) {
            result.append(String.format(EncryptDistSQLConstants.CIPHER, cipherColumnName));
        }
        if (ruleConfig.getAssistedQuery().isPresent()) {
            result.append(EncryptDistSQLConstants.COMMA).append(' ').append(String.format(EncryptDistSQLConstants.ASSISTED_QUERY_COLUMN, ruleConfig.getAssistedQuery().get().getName()));
        }
        if (ruleConfig.getLikeQuery().isPresent()) {
            result.append(EncryptDistSQLConstants.COMMA).append(' ').append(String.format(EncryptDistSQLConstants.LIKE_QUERY_COLUMN, ruleConfig.getLikeQuery().get().getName()));
        }
        return result.toString();
    }
    
    private String getEncryptAlgorithms(final EncryptColumnRuleConfiguration ruleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        String cipherEncryptorName = ruleConfig.getCipher().getEncryptorName();
        String assistedQueryEncryptorName = ruleConfig.getAssistedQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).orElse("");
        String likeQueryEncryptorName = ruleConfig.getLikeQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).orElse("");
        if (!Strings.isNullOrEmpty(cipherEncryptorName)) {
            result.append(String.format(EncryptDistSQLConstants.ENCRYPT_ALGORITHM, getAlgorithmType(encryptors.get(cipherEncryptorName))));
        }
        if (!Strings.isNullOrEmpty(assistedQueryEncryptorName)) {
            result.append(EncryptDistSQLConstants.COMMA).append(' ')
                    .append(String.format(EncryptDistSQLConstants.ASSISTED_QUERY_ALGORITHM, getAlgorithmType(encryptors.get(assistedQueryEncryptorName))));
        }
        if (!Strings.isNullOrEmpty(likeQueryEncryptorName)) {
            result.append(EncryptDistSQLConstants.COMMA).append(' ').append(String.format(EncryptDistSQLConstants.LIKE_QUERY_ALGORITHM, getAlgorithmType(encryptors.get(likeQueryEncryptorName))));
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return EncryptRuleConfiguration.class.getName();
    }
}
