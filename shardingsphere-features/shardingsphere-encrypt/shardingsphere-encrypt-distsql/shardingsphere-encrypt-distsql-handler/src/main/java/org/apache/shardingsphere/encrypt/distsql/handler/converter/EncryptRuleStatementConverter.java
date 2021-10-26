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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptColumnSegment;
import org.apache.shardingsphere.encrypt.distsql.parser.segment.EncryptRuleSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encrypt rule statement converter.
 */
public final class EncryptRuleStatementConverter {
    
    /**
     * Convert encrypt rule segments to encrypt rule configuration.
     *
     * @param ruleSegments encrypt rule segments
     * @return encrypt rule configuration
     */
    public static EncryptRuleConfiguration convert(final Collection<EncryptRuleSegment> ruleSegments) {
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, ShardingSphereAlgorithmConfiguration> encryptors = new HashMap<>();
        for (EncryptRuleSegment each : ruleSegments) {
            tables.add(createEncryptTableRuleConfiguration(each));
            encryptors.putAll(createEncryptorConfigurations(each));
        }
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    private static EncryptTableRuleConfiguration createEncryptTableRuleConfiguration(final EncryptRuleSegment ruleSegment) {
        Collection<EncryptColumnRuleConfiguration> columns = new LinkedList<>();
        for (EncryptColumnSegment each : ruleSegment.getColumns()) {
            columns.add(createEncryptColumnRuleConfiguration(ruleSegment.getTableName(), each));
        }
        return new EncryptTableRuleConfiguration(ruleSegment.getTableName(), columns, ruleSegment.getQueryWithCipherColumn());
    }
    
    private static EncryptColumnRuleConfiguration createEncryptColumnRuleConfiguration(final String tableName, final EncryptColumnSegment columnSegment) {
        return new EncryptColumnRuleConfiguration(columnSegment.getName(), columnSegment.getCipherColumn(), columnSegment.getAssistedQueryColumn(),
                columnSegment.getPlainColumn(), getEncryptorName(tableName, columnSegment.getName()));
    }
    
    private static Map<String, ShardingSphereAlgorithmConfiguration> createEncryptorConfigurations(final EncryptRuleSegment ruleSegment) {
        return ruleSegment.getColumns().stream().collect(Collectors
                .toMap(each -> getEncryptorName(ruleSegment.getTableName(), each.getName()), EncryptRuleStatementConverter::createEncryptorConfiguration));
    }
    
    private static ShardingSphereAlgorithmConfiguration createEncryptorConfiguration(final EncryptColumnSegment columnSegment) {
        return new ShardingSphereAlgorithmConfiguration(columnSegment.getEncryptor().getName(), columnSegment.getEncryptor().getProps());
    }
    
    private static String getEncryptorName(final String tableName, final String columnName) {
        return String.format("%s_%s", tableName, columnName);
    }
}
