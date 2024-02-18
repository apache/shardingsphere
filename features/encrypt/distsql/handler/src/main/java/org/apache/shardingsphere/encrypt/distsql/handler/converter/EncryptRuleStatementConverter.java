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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnItemSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.segment.EncryptRuleSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Encrypt rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptRuleStatementConverter {
    
    /**
     * Convert encrypt rule segments to encrypt rule configuration.
     *
     * @param ruleSegments encrypt rule segments
     * @return encrypt rule configuration
     */
    public static EncryptRuleConfiguration convert(final Collection<EncryptRuleSegment> ruleSegments) {
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        for (EncryptRuleSegment each : ruleSegments) {
            tables.add(createEncryptTableRuleConfiguration(each));
            encryptors.putAll(createEncryptorConfigurations(each));
        }
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    private static EncryptTableRuleConfiguration createEncryptTableRuleConfiguration(final EncryptRuleSegment ruleSegment) {
        Collection<EncryptColumnRuleConfiguration> columns = new LinkedList<>();
        for (EncryptColumnSegment each : ruleSegment.getColumns()) {
            columns.add(createEncryptColumnRuleConfiguration(ruleSegment, each));
        }
        return new EncryptTableRuleConfiguration(ruleSegment.getTableName(), columns);
    }
    
    private static EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration(final EncryptRuleSegment ruleSegment, final EncryptColumnSegment columnSegment) {
        EncryptColumnItemRuleConfiguration cipherColumnConfig = new EncryptColumnItemRuleConfiguration(
                columnSegment.getCipher().getName(), getEncryptorName(ruleSegment.getTableName(), columnSegment.getName()));
        EncryptColumnRuleConfiguration result = new EncryptColumnRuleConfiguration(columnSegment.getName(), cipherColumnConfig);
        if (null != columnSegment.getAssistedQuery()) {
            setAssistedQuery(ruleSegment.getTableName(), columnSegment, result);
        }
        if (null != columnSegment.getLikeQuery()) {
            setLikeQuery(ruleSegment.getTableName(), columnSegment, result);
        }
        return result;
    }
    
    private static void setAssistedQuery(final String tableName, final EncryptColumnSegment columnSegment, final EncryptColumnRuleConfiguration columnRuleConfig) {
        String assistedQueryEncryptorName = null == columnSegment.getAssistedQuery().getEncryptor() ? null : getAssistedQueryEncryptorName(tableName, columnSegment.getName());
        EncryptColumnItemRuleConfiguration assistedQueryColumnConfig = new EncryptColumnItemRuleConfiguration(columnSegment.getAssistedQuery().getName(), assistedQueryEncryptorName);
        columnRuleConfig.setAssistedQuery(assistedQueryColumnConfig);
    }
    
    private static void setLikeQuery(final String tableName, final EncryptColumnSegment columnSegment, final EncryptColumnRuleConfiguration columnRuleConfig) {
        String likeQueryEncryptorName = null == columnSegment.getLikeQuery().getEncryptor() ? null : getLikeQueryEncryptorName(tableName, columnSegment.getName());
        EncryptColumnItemRuleConfiguration likeQueryColumnConfig = new EncryptColumnItemRuleConfiguration(columnSegment.getLikeQuery().getName(), likeQueryEncryptorName);
        columnRuleConfig.setLikeQuery(likeQueryColumnConfig);
    }
    
    private static Map<String, AlgorithmConfiguration> createEncryptorConfigurations(final EncryptRuleSegment ruleSegment) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(ruleSegment.getColumns().size(), 1F);
        for (EncryptColumnSegment each : ruleSegment.getColumns()) {
            result.put(getEncryptorName(ruleSegment.getTableName(), each.getName()), createEncryptorConfiguration(each.getCipher()));
            if (null != each.getAssistedQuery() && null != each.getAssistedQuery().getEncryptor()) {
                result.put(getAssistedQueryEncryptorName(ruleSegment.getTableName(), each.getName()), createEncryptorConfiguration(each.getAssistedQuery()));
            }
            if (null != each.getLikeQuery() && null != each.getLikeQuery().getEncryptor()) {
                result.put(getLikeQueryEncryptorName(ruleSegment.getTableName(), each.getName()), createEncryptorConfiguration(each.getLikeQuery()));
            }
        }
        return result;
    }
    
    private static AlgorithmConfiguration createEncryptorConfiguration(final EncryptColumnItemSegment columnSegment) {
        return new AlgorithmConfiguration(columnSegment.getEncryptor().getName(), columnSegment.getEncryptor().getProps());
    }
    
    private static String getEncryptorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
    
    private static String getAssistedQueryEncryptorName(final String tableName, final String columnName) {
        return String.format("assist_%s_%s", tableName, columnName);
    }
    
    private static String getLikeQueryEncryptorName(final String tableName, final String columnName) {
        return String.format("like_%s_%s", tableName, columnName);
    }
}
