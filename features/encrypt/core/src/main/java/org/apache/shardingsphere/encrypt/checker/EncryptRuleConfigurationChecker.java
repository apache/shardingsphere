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
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptAssistedQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptAssistedQueryEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptCipherColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptEncryptorNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryColumnNotFoundException;
import org.apache.shardingsphere.encrypt.exception.metadata.EncryptLikeQueryEncryptorNotFoundException;
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
        checkTableConfiguration(databaseName, config.getTables(), config.getEncryptors().keySet());
    }
    
    private void checkTableConfiguration(final String databaseName, final Collection<EncryptTableRuleConfiguration> tables, final Collection<String> encryptors) {
        for (EncryptTableRuleConfiguration each : tables) {
            for (EncryptColumnRuleConfiguration column : each.getColumns()) {
                checkCipherColumnConfiguration(databaseName, encryptors, column);
                checkAssistColumnConfiguration(databaseName, encryptors, column);
                checkLikeColumnConfiguration(databaseName, encryptors, column);
            }
        }
    }
    
    private void checkCipherColumnConfiguration(final String databaseName, final Collection<String> encryptors, final EncryptColumnRuleConfiguration column) {
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(column.getCipherColumn()), () -> new EncryptCipherColumnNotFoundException(column.getLogicColumn(), databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(column.getEncryptorName()),
                () -> new EncryptEncryptorNotFoundException(String.format("Encryptor name of `%s` can not be null in database `%s`.", column.getLogicColumn(), databaseName)));
        ShardingSpherePreconditions.checkState(encryptors.contains(column.getEncryptorName()),
                () -> new EncryptEncryptorNotFoundException(String.format("Can not find encryptor `%s` in database `%s`.", column.getEncryptorName(), databaseName)));
    }
    
    private void checkAssistColumnConfiguration(final String databaseName, final Collection<String> encryptors, final EncryptColumnRuleConfiguration column) {
        if (Strings.isNullOrEmpty(column.getAssistedQueryColumn()) && Strings.isNullOrEmpty(column.getAssistedQueryEncryptorName())) {
            return;
        }
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(column.getAssistedQueryColumn()), () -> new EncryptAssistedQueryColumnNotFoundException(column.getLogicColumn(), databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(column.getAssistedQueryEncryptorName()), () -> new EncryptAssistedQueryEncryptorNotFoundException(
                String.format("Assisted query encryptor name of `%s` can not be null in database `%s`.", column.getLogicColumn(), databaseName)));
        ShardingSpherePreconditions.checkState(encryptors.contains(column.getAssistedQueryEncryptorName()), () -> new EncryptAssistedQueryEncryptorNotFoundException(
                String.format("Can not find assisted query encryptor `%s` in database `%s`.", column.getAssistedQueryEncryptorName(), databaseName)));
    }
    
    private void checkLikeColumnConfiguration(final String databaseName, final Collection<String> encryptors, final EncryptColumnRuleConfiguration column) {
        if (Strings.isNullOrEmpty(column.getLikeQueryColumn()) && Strings.isNullOrEmpty(column.getLikeQueryEncryptorName())) {
            return;
        }
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(column.getLikeQueryColumn()), () -> new EncryptLikeQueryColumnNotFoundException(column.getLogicColumn(), databaseName));
        ShardingSpherePreconditions.checkState(!Strings.isNullOrEmpty(column.getLikeQueryEncryptorName()),
                () -> new EncryptLikeQueryEncryptorNotFoundException(String.format("Like query encryptor name of `%s` can not be null in database `%s`.", column.getLogicColumn(), databaseName)));
        ShardingSpherePreconditions.checkState(encryptors.contains(column.getLikeQueryEncryptorName()),
                () -> new EncryptLikeQueryEncryptorNotFoundException(String.format("Can not find like query encryptor `%s` in database `%s`.", column.getLikeQueryEncryptorName(), databaseName)));
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
