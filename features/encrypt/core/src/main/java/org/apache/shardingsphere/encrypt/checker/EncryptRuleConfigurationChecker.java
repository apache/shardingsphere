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
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptAssistedQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptAssistedQueryEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptCipherColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryEncryptorNotFoundException;
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
            checkCipherColumnConfiguration(databaseName, each.getCipher(), each.getName(), encryptors);
            each.getAssistedQuery().ifPresent(optional -> checkAssistColumnConfiguration(databaseName, optional, each.getName(), encryptors));
            each.getLikeQuery().ifPresent(optional -> checkLikeColumnConfiguration(databaseName, optional, each.getName(), encryptors));
        }
    }
    
    private void checkCipherColumnConfiguration(final String databaseName, final EncryptColumnItemRuleConfiguration cipherColumn, final String logicColumn,
                                                final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumn.getName()), () -> new EncryptCipherColumnNotFoundException(logicColumn, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumn.getEncryptorName()),
                () -> new EncryptEncryptorNotFoundException(String.format("Encryptor name of `%s` can not be null in database `%s`.", logicColumn, databaseName)));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(cipherColumn.getEncryptorName()),
                () -> new EncryptEncryptorNotFoundException(String.format("Can not find encryptor `%s` in database `%s`.", cipherColumn.getEncryptorName(), databaseName)));
    }
    
    private void checkAssistColumnConfiguration(final String databaseName, final EncryptColumnItemRuleConfiguration assistedQueryColumn, final String logicColumn,
                                                final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumn.getName()), () -> new EncryptAssistedQueryColumnNotFoundException(logicColumn, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumn.getEncryptorName()), () -> new EncryptAssistedQueryEncryptorNotFoundException(
                String.format("Assisted query encryptor name of `%s` can not be null in database `%s`.", logicColumn, databaseName)));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(assistedQueryColumn.getEncryptorName()), () -> new EncryptAssistedQueryEncryptorNotFoundException(
                String.format("Can not find assisted query encryptor `%s` in database `%s`.", assistedQueryColumn.getEncryptorName(), databaseName)));
    }
    
    private void checkLikeColumnConfiguration(final String databaseName, final EncryptColumnItemRuleConfiguration likeQueryColumn, final String logicColumn,
                                              final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumn.getName()), () -> new EncryptLikeQueryColumnNotFoundException(logicColumn, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumn.getEncryptorName()),
                () -> new EncryptLikeQueryEncryptorNotFoundException(String.format("Like query encryptor name of `%s` can not be null in database `%s`.", logicColumn, databaseName)));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(likeQueryColumn.getEncryptorName()),
                () -> new EncryptLikeQueryEncryptorNotFoundException(String.format("Can not find like query encryptor `%s` in database `%s`.", likeQueryColumn.getEncryptorName(), databaseName)));
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
