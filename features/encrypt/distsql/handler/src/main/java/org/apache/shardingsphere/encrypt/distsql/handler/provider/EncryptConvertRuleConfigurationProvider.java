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
import org.apache.shardingsphere.distsql.handler.ral.constant.DistSQLScriptConstants;
import org.apache.shardingsphere.distsql.handler.ral.query.ConvertRuleConfigurationProvider;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
        StringBuilder result = new StringBuilder();
        result.append(DistSQLScriptConstants.CREATE_ENCRYPT);
        Iterator<EncryptTableRuleConfiguration> iterator = ruleConfig.getTables().iterator();
        while (iterator.hasNext()) {
            EncryptTableRuleConfiguration tableRuleConfig = iterator.next();
            result.append(String.format(DistSQLScriptConstants.ENCRYPT, tableRuleConfig.getName(), getEncryptColumns(tableRuleConfig.getColumns(), ruleConfig.getEncryptors())));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        result.append(DistSQLScriptConstants.SEMI).append(System.lineSeparator()).append(System.lineSeparator());
        return result.toString();
    }
    
    private String getEncryptColumns(final Collection<EncryptColumnRuleConfiguration> ruleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        Iterator<EncryptColumnRuleConfiguration> iterator = ruleConfigs.iterator();
        while (iterator.hasNext()) {
            EncryptColumnRuleConfiguration columnRuleConfig = iterator.next();
            result.append(String.format(DistSQLScriptConstants.ENCRYPT_COLUMN, columnRuleConfig.getName(), getColumns(columnRuleConfig), getEncryptAlgorithms(columnRuleConfig, encryptors)));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(System.lineSeparator());
            }
        }
        return result.toString();
    }
    
    private String getColumns(final EncryptColumnRuleConfiguration ruleConfig) {
        StringBuilder result = new StringBuilder();
        String cipherColumnName = ruleConfig.getCipher().getName();
        if (!Strings.isNullOrEmpty(cipherColumnName)) {
            result.append(String.format(DistSQLScriptConstants.CIPHER, cipherColumnName));
        }
        if (ruleConfig.getAssistedQuery().isPresent()) {
            result.append(DistSQLScriptConstants.COMMA).append(' ').append(String.format(DistSQLScriptConstants.ASSISTED_QUERY_COLUMN, ruleConfig.getAssistedQuery().get().getName()));
        }
        if (ruleConfig.getLikeQuery().isPresent()) {
            result.append(DistSQLScriptConstants.COMMA).append(' ').append(String.format(DistSQLScriptConstants.LIKE_QUERY_COLUMN, ruleConfig.getLikeQuery().get().getName()));
        }
        return result.toString();
    }
    
    private String getEncryptAlgorithms(final EncryptColumnRuleConfiguration ruleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        StringBuilder result = new StringBuilder();
        String cipherEncryptorName = ruleConfig.getCipher().getEncryptorName();
        String assistedQueryEncryptorName = ruleConfig.getAssistedQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).orElse("");
        String likeQueryEncryptorName = ruleConfig.getLikeQuery().map(EncryptColumnItemRuleConfiguration::getEncryptorName).orElse("");
        if (!Strings.isNullOrEmpty(cipherEncryptorName)) {
            result.append(String.format(DistSQLScriptConstants.ENCRYPT_ALGORITHM, getAlgorithmType(encryptors.get(cipherEncryptorName))));
        }
        if (!Strings.isNullOrEmpty(assistedQueryEncryptorName)) {
            result.append(DistSQLScriptConstants.COMMA).append(' ')
                    .append(String.format(DistSQLScriptConstants.ASSISTED_QUERY_ALGORITHM, getAlgorithmType(encryptors.get(assistedQueryEncryptorName))));
        }
        if (!Strings.isNullOrEmpty(likeQueryEncryptorName)) {
            result.append(DistSQLScriptConstants.COMMA).append(' ').append(String.format(DistSQLScriptConstants.LIKE_QUERY_ALGORITHM, getAlgorithmType(encryptors.get(likeQueryEncryptorName))));
        }
        return result.toString();
    }
    
    private String getAlgorithmType(final AlgorithmConfiguration algorithmConfig) {
        StringBuilder result = new StringBuilder();
        if (null == algorithmConfig) {
            return result.toString();
        }
        String type = algorithmConfig.getType().toLowerCase();
        if (algorithmConfig.getProps().isEmpty()) {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE_WITHOUT_PROPS, type));
        } else {
            result.append(String.format(DistSQLScriptConstants.ALGORITHM_TYPE, type, getAlgorithmProperties(algorithmConfig.getProps())));
        }
        return result.toString();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private String getAlgorithmProperties(final Properties props) {
        StringBuilder result = new StringBuilder();
        Iterator<String> iterator = new TreeMap(props).keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = props.get(key);
            if (null == value) {
                continue;
            }
            result.append(String.format(DistSQLScriptConstants.PROPERTY, key, value));
            if (iterator.hasNext()) {
                result.append(DistSQLScriptConstants.COMMA).append(' ');
            }
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return EncryptRuleConfiguration.class.getName();
    }
}
