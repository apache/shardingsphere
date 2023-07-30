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
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptCipherColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.MissingEncryptorException;
import org.apache.shardingsphere.encrypt.exception.metadata.UnregisteredEncryptorException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Encrypt rule configuration checker.
 */
public final class EncryptRuleConfigurationChecker implements RuleConfigurationChecker<EncryptRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final EncryptRuleConfiguration config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> builtRules) {
        checkTableConfiguration(databaseName, config.getTables(), config.getEncryptors());
    }
    
    private void checkTableConfiguration(final String databaseName, final Collection<EncryptTableRuleConfiguration> tableRuleConfigs, final Map<String, AlgorithmConfiguration> encryptors) {
        for (EncryptTableRuleConfiguration each : tableRuleConfigs) {
            checkColumnConfiguration(databaseName, each, encryptors);
        }
    }
    
    private void checkColumnConfiguration(final String databaseName, final EncryptTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        for (EncryptColumnRuleConfiguration each : tableRuleConfig.getColumns()) {
            checkCipherColumnConfiguration(databaseName, tableRuleConfig.getName(), each.getName(), each.getCipher(), encryptors);
            each.getAssistedQuery().ifPresent(optional -> checkAssistColumnConfiguration(databaseName, tableRuleConfig.getName(), each.getName(), optional, encryptors));
            each.getLikeQuery().ifPresent(optional -> checkLikeColumnConfiguration(databaseName, tableRuleConfig.getName(), each.getName(), optional, encryptors));
        }
    }
    
    private void checkCipherColumnConfiguration(final String databaseName, final String tableName, final String logicColumnName,
                                                final EncryptColumnItemRuleConfiguration cipherColumnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumnConfig.getName()), () -> new EncryptCipherColumnNotFoundException(logicColumnName, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(cipherColumnConfig.getEncryptorName()), () -> new MissingEncryptorException(tableName, logicColumnName, "STANDARD"));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(cipherColumnConfig.getEncryptorName()),
                () -> new UnregisteredEncryptorException(databaseName, cipherColumnConfig.getEncryptorName()));
    }
    
    private void checkAssistColumnConfiguration(final String databaseName, final String tableName, final String logicColumnName,
                                                final EncryptColumnItemRuleConfiguration assistedQueryColumnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumnConfig.getName()), () -> new EncryptAssistedQueryColumnNotFoundException(logicColumnName, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(assistedQueryColumnConfig.getEncryptorName()), () -> new MissingEncryptorException(tableName, logicColumnName, "ASSIST_QUERY"));
        ShardingSpherePreconditions.checkState(
                encryptors.containsKey(assistedQueryColumnConfig.getEncryptorName()), () -> new UnregisteredEncryptorException(databaseName, assistedQueryColumnConfig.getEncryptorName()));
    }
    
    private void checkLikeColumnConfiguration(final String databaseName, final String tableName, final String logicColumnName,
                                              final EncryptColumnItemRuleConfiguration likeQueryColumnConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumnConfig.getName()), () -> new EncryptLikeQueryColumnNotFoundException(logicColumnName, databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(likeQueryColumnConfig.getEncryptorName()), () -> new MissingEncryptorException(tableName, logicColumnName, "LIKE_QUERY"));
        ShardingSpherePreconditions.checkState(encryptors.containsKey(likeQueryColumnConfig.getEncryptorName()),
                () -> new UnregisteredEncryptorException(databaseName, likeQueryColumnConfig.getEncryptorName()));
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
