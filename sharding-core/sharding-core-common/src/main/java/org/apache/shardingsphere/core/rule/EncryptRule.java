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

package org.apache.shardingsphere.core.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.api.config.encrypt.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encrypt.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptEngine;

import java.util.Collection;

/**
 * Encrypt rule.
 *
 * @author panjuan
 */
@Getter
public final class EncryptRule implements BaseRule {
    
    private final EncryptEngine encryptEngine;
    
    private EncryptRuleConfiguration encryptRuleConfig;
    
    public EncryptRule() {
        encryptEngine = new EncryptEngine();
        encryptRuleConfig = new EncryptRuleConfiguration();
    }
    
    public EncryptRule(final EncryptRuleConfiguration encryptRuleConfiguration) {
        this.encryptRuleConfig = encryptRuleConfiguration;
        Preconditions.checkArgument(isValidEncryptRuleConfig(), "Invalid encrypt column configurations in EncryptTableRuleConfigurations.");
        encryptEngine = new EncryptEngine(encryptRuleConfiguration);
    }
    
    private boolean isValidEncryptRuleConfig() {
        return (encryptRuleConfig.getEncryptors().isEmpty() && encryptRuleConfig.getTables().isEmpty()) || isValidEncryptTableConfig();
    }
    
    private boolean isValidEncryptTableConfig() {
        for (EncryptTableRuleConfiguration table : encryptRuleConfig.getTables().values()) {
            for (EncryptColumnRuleConfiguration column : table.getColumns().values()) {
                if (!isValidColumnConfig(column)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isValidColumnConfig(final EncryptColumnRuleConfiguration column) {
        return !Strings.isNullOrEmpty(column.getEncryptor()) && !Strings.isNullOrEmpty(column.getCipherColumn()) && encryptRuleConfig.getEncryptors().keySet().contains(column.getEncryptor());
    }
    
    /**
     * Get encrypt table names.
     * 
     * @return encrypt table names
     */
    public Collection<String> getEncryptTableNames() {
        return encryptEngine.getEncryptTableNames();
    }
}
