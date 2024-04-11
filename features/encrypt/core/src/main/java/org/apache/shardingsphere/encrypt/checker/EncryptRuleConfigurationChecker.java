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

package org.apache.shardingsphere.encrypt.checker;

import com.google.common.base.Strings;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingRequiredEncryptColumnException;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.exception.EmptyAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encrypt rule configuration checker.
 */
public final class EncryptRuleConfigurationChecker implements RuleConfigurationChecker<EncryptRuleConfiguration> {
    
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
        checkCipherColumn(databaseName, tableName, columnRuleConfig.getName(), columnRuleConfig.getCipher(), encryptors);
        columnRuleConfig.getAssistedQuery().ifPresent(optional -> checkAssistColumn(databaseName, tableName, columnRuleConfig.getName(), optional, encryptors));
        columnRuleConfig.getLikeQuery().ifPresent(optional -> checkLikeColumn(databaseName, tableName, columnRuleConfig.getName(), optional, encryptors));
    }
    
    private void checkCipherColumn(final String databaseName, final String tableName, final String logicColumnName,
                                   final EncryptColumnItemRuleConfiguration cipherColumnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumnConfig.getName()),
                () -> new MissingRequiredEncryptColumnException("Cipher", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumnConfig.getEncryptorName()),
                () -> new EmptyAlgorithmException("Standard encrypt", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(cipherColumnConfig.getEncryptorName()),
                () -> new UnregisteredAlgorithmException("Standard encrypt", cipherColumnConfig.getEncryptorName(), new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
    }
    
    private void checkAssistColumn(final String databaseName, final String tableName, final String logicColumnName,
                                   final EncryptColumnItemRuleConfiguration assistedQueryColumnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumnConfig.getName()),
                () -> new MissingRequiredEncryptColumnException("Assisted query", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumnConfig.getEncryptorName()),
                () -> new EmptyAlgorithmException("Assist query encrypt", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(assistedQueryColumnConfig.getEncryptorName()),
                () -> new UnregisteredAlgorithmException("Assist query encrypt", assistedQueryColumnConfig.getEncryptorName(), new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
    }
    
    private void checkLikeColumn(final String databaseName, final String tableName, final String logicColumnName,
                                 final EncryptColumnItemRuleConfiguration likeQueryColumnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumnConfig.getName()),
                () -> new MissingRequiredEncryptColumnException("Like", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumnConfig.getEncryptorName()),
                () -> new EmptyAlgorithmException("Like query", new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(likeQueryColumnConfig.getEncryptorName()),
                () -> new UnregisteredAlgorithmException("Like query encrypt", likeQueryColumnConfig.getEncryptorName(), new SQLExceptionIdentifier(databaseName, tableName, logicColumnName)));
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
