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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Abstract encrypt rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractEncryptRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String databaseName, final T config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        checkTableConfiguration(databaseName, getTables(config), getEncryptors(config));
    }
    
    private void checkTableConfiguration(final String databaseName, final Collection<EncryptTableRuleConfiguration> tables, final Collection<String> encryptors) {
        for (EncryptTableRuleConfiguration each : tables) {
            for (EncryptColumnRuleConfiguration column : each.getColumns()) {
                checkCipherColumnConfiguration(databaseName, encryptors, column);
                checkAssistColumnConfiguration(databaseName, encryptors, column);
            }
        }
    }
    
    private void checkCipherColumnConfiguration(final String databaseName, final Collection<String> encryptors, final EncryptColumnRuleConfiguration column) {
        Preconditions.checkState(!Strings.isNullOrEmpty(column.getCipherColumn()),
                "Cipher column of `%s` can not be null in database `%s`", column.getLogicColumn(), databaseName);
        Preconditions.checkState(!Strings.isNullOrEmpty(column.getEncryptorName()),
                "Encryptor name of `%s` can not be null in database `%s`", column.getLogicColumn(), databaseName);
        Preconditions.checkState(encryptors.contains(column.getEncryptorName()),
                "Can not find encryptor `%s` in database `%s`", column.getEncryptorName(), databaseName);
    }
    
    private void checkAssistColumnConfiguration(final String databaseName, final Collection<String> encryptors, final EncryptColumnRuleConfiguration column) {
        if (Strings.isNullOrEmpty(column.getAssistedQueryColumn()) && Strings.isNullOrEmpty(column.getAssistedQueryEncryptorName())) {
            return;
        }
        Preconditions.checkState(!Strings.isNullOrEmpty(column.getAssistedQueryColumn()),
                "Assisted query column of `%s` can not be null in database `%s`", column.getLogicColumn(), databaseName);
        Preconditions.checkState(!Strings.isNullOrEmpty(column.getAssistedQueryEncryptorName()),
                "Assisted query encryptor name of `%s` can not be null in database `%s`", column.getLogicColumn(), databaseName);
        Preconditions.checkState(encryptors.contains(column.getAssistedQueryEncryptorName()),
                "Can not find assisted query encryptor `%s` in database `%s`", column.getAssistedQueryEncryptorName(), databaseName);
    }
    
    protected abstract Collection<String> getEncryptors(T config);
    
    protected abstract Collection<EncryptTableRuleConfiguration> getTables(T config);
}
