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
import org.apache.shardingsphere.encrypt.exception.metadata.MissingAssistedQueryEncryptorException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptAssistedQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptCipherColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingLikeQueryEncryptorException;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingEncryptorException;
import org.apache.shardingsphere.encrypt.exception.metadata.UnregisteredEncryptorException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Encrypt rule configuration checker.
 */
public final class EncryptRuleConfigurationChecker implements RuleConfigurationChecker<EncryptRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final EncryptRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        checkTableConfiguration(databaseName, config.getTables(), config.getEncryptors());
    }
    
    private void checkTableConfiguration(final String databaseName, final Collection<EncryptTableRuleConfiguration> tableRuleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        for (EncryptTableRuleConfiguration each : tableRuleConfigs) {
            checkColumnConfiguration(databaseName, each, encryptors);
        }
    }
    
    private void checkColumnConfiguration(final String databaseName, final EncryptTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        for (EncryptColumnRuleConfiguration each : tableRuleConfig.getColumns()) {
            checkCipherColumnConfiguration(databaseName, tableRuleConfig.getName(), each.getCipher(), each.getName(), encryptors);
            each.getAssistedQuery().ifPresent(optional -> checkAssistColumnConfiguration(databaseName, tableRuleConfig.getName(), optional, each.getName(), encryptors));
            each.getLikeQuery().ifPresent(optional -> checkLikeColumnConfiguration(databaseName, tableRuleConfig.getName(), optional, each.getName(), encryptors));
        }
    }
    
    private void checkCipherColumnConfiguration(final String databaseName, final String tableName, final EncryptColumnItemRuleConfiguration cipherColumn, final String logicColumn,
                                                final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumn.getName()), () -> new EncryptCipherColumnNotFoundException(logicColumn, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumn.getEncryptorName()), () -> new MissingEncryptorException(tableName, logicColumn));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(cipherColumn.getEncryptorName()), () -> new UnregisteredEncryptorException(databaseName, cipherColumn.getEncryptorName()));
    }
    
    private void checkAssistColumnConfiguration(final String databaseName, final String tableName, final EncryptColumnItemRuleConfiguration assistedQueryColumn, final String logicColumn,
                                                final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumn.getName()), () -> new EncryptAssistedQueryColumnNotFoundException(logicColumn, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumn.getEncryptorName()), () -> new MissingAssistedQueryEncryptorException(tableName, logicColumn));
        ShardingSpherePreconditions.checkState(
                encryptors.containsKey(assistedQueryColumn.getEncryptorName()), () -> new UnregisteredEncryptorException(databaseName, assistedQueryColumn.getEncryptorName()));
    }
    
    private void checkLikeColumnConfiguration(final String databaseName, final String tableName, final EncryptColumnItemRuleConfiguration likeQueryColumn, final String logicColumn,
                                              final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumn.getName()), () -> new EncryptLikeQueryColumnNotFoundException(logicColumn, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumn.getEncryptorName()), () -> new MissingLikeQueryEncryptorException(tableName, logicColumn));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(likeQueryColumn.getEncryptorName()), () -> new UnregisteredEncryptorException(databaseName, likeQueryColumn.getEncryptorName()));
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
