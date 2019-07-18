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

package org.apache.shardingsphere.api.config.encrypt;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import lombok.Getter;
import org.apache.shardingsphere.api.config.RuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encrypt rule configuration.
 *
 * @author panjuan
 */
@Getter
public final class EncryptRuleConfigurationBak implements RuleConfiguration {
    
    private final Map<String, EncryptorRuleConfiguration> encryptors;
    
    private final Map<String, EncryptTableRuleConfiguration> tables;
    
    public EncryptRuleConfigurationBak() {
        this(new LinkedHashMap<String, EncryptorRuleConfiguration>(), new LinkedHashMap<String, EncryptTableRuleConfiguration>());
    }
    
    public EncryptRuleConfigurationBak(final Map<String, EncryptorRuleConfiguration> encryptors, final Map<String, EncryptTableRuleConfiguration> tables) {
        this.encryptors = encryptors;
        this.tables = tables;
        Preconditions.checkArgument(isValidConfigurations(), "Invalid encryptorNames are used in EncryptTableRuleConfigurations.");
    }
    
    private boolean isValidConfigurations() {
        return (encryptors.isEmpty() && tables.isEmpty()) || isUsingValidEncryptNames();
    }
    
    private boolean isUsingValidEncryptNames() {
        for (EncryptTableRuleConfiguration each : tables.values()) {
            Collection<String> encryptors = Collections2.transform(each.getColumns().values(), new Function<EncryptColumnRuleConfiguration, String>() {
                
                @Override
                public String apply(final EncryptColumnRuleConfiguration input) {
                    return input.getEncryptor();
                }
            });
            if (!this.encryptors.keySet().containsAll(encryptors)) {
                return false;
            }
        }
        return true;
    }
}
