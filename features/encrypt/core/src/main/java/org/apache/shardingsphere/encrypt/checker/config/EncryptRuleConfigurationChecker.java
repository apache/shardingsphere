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

package org.apache.shardingsphere.encrypt.checker.config;

import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingRequiredEncryptColumnException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encrypt rule configuration checker.
 */
public final class EncryptRuleConfigurationChecker implements DatabaseRuleConfigurationChecker<EncryptRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final EncryptRuleConfiguration ruleConfig, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkEncryptors(ruleConfig.getEncryptors());
        checkTables(databaseName, ruleConfig.getTables(), ruleConfig.getEncryptors());
    }
    
    private void checkEncryptors(final Map<String, AlgorithmConfiguration> encryptors) {
        encryptors.values().forEach(each -> TypedSPILoader.checkService(EncryptAlgorithm.class, each.getType(), each.getProps()));
    }
    
    private void checkTables(final String databaseName, final Collection<EncryptTableRuleConfiguration> tableRuleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        tableRuleConfigs.forEach(each -> checkColumns(databaseName, each, encryptors));
    }
    
    private void checkColumns(final String databaseName, final EncryptTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        tableRuleConfig.getColumns().forEach(each -> checkColumn(databaseName, tableRuleConfig.getName(), each, encryptors));
    }
    
    private void checkColumn(final String databaseName, final String tableName, final EncryptColumnRuleConfiguration columnRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        checkEncryptColumnItem(databaseName, tableName, columnRuleConfig.getName(), columnRuleConfig.getCipher(), encryptors, "Cipher");
        columnRuleConfig.getAssistedQuery().ifPresent(optional -> checkEncryptColumnItem(databaseName, tableName, columnRuleConfig.getName(), optional, encryptors, "Assist Query"));
        columnRuleConfig.getLikeQuery().ifPresent(optional -> checkEncryptColumnItem(databaseName, tableName, columnRuleConfig.getName(), optional, encryptors, "Like Query"));
    }
    
    private void checkEncryptColumnItem(final String databaseName, final String tableName, final String logicColumnName,
                                        final EncryptColumnItemRuleConfiguration columnItem, final Map<String, AlgorithmConfiguration> encryptors, final String itemType) {
        ShardingSpherePreconditions.checkNotEmpty(columnItem.getName(),
                () -> new MissingRequiredEncryptColumnException(itemType, new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkNotEmpty(columnItem.getEncryptorName(),
                () -> new MissingRequiredAlgorithmException(itemType + " encrypt", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkContainsKey(encryptors, columnItem.getEncryptorName(),
                () -> new UnregisteredAlgorithmException(itemType + " encrypt", columnItem.getEncryptorName(), new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
    }
    
    @Override
    public Collection<String> getTableNames(final EncryptRuleConfiguration ruleConfig) {
        return ruleConfig.getTables().stream().map(EncryptTableRuleConfiguration::getName).collect(Collectors.toList());
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getTypeClass() {
        return EncryptRuleConfiguration.class;
    }
}
