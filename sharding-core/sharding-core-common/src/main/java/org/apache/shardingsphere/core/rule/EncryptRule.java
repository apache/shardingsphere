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

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import lombok.Getter;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.encryptor.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorStrategy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Encrypt rule.
 *
 * @author panjuan
 */
public final class EncryptRule implements SQLStatementFillerRule {
    
    private final Collection<EncryptTableRule> tableRules;
    
    private final ShardingEncryptorStrategy defaultEncryptorStrategy;
    
    @Getter
    private final ShardingEncryptorEngine encryptorEngine;
    
    public EncryptRule(final EncryptRuleConfiguration encryptRuleConfiguration) {
        tableRules = new LinkedList<>();
        Map<String, ShardingEncryptorStrategy> shardingEncryptorStrategies = new LinkedHashMap<>();
        for (EncryptTableRuleConfiguration each : encryptRuleConfiguration.getTableRuleConfigs()) {
            EncryptTableRule tableRule = new EncryptTableRule(each);
            tableRules.add(tableRule);
            shardingEncryptorStrategies.put(tableRule.getTable(), tableRule.getShardingEncryptorStrategy());
        }
        defaultEncryptorStrategy = null != encryptRuleConfiguration.getDefaultEncryptorConfig() ? new ShardingEncryptorStrategy(encryptRuleConfiguration.getDefaultEncryptorConfig()) : null;
        encryptorEngine = new ShardingEncryptorEngine(shardingEncryptorStrategies, defaultEncryptorStrategy);
    }
    
    /**
     * Get encrypt table names.
     * 
     * @return encrypt table names
     */
    public Collection<String> getEncryptTableNames() {
        return Collections2.transform(tableRules, new Function<EncryptTableRule, String>() {
            
            @Override
            public String apply(final EncryptTableRule input) {
                return input.getTable();
            }
        });
    }
    
    /**
     * Get all actual table names.
     *
     * @return all actual table names
     */
    public Map<String, Collection<String>> getAllEncryptTableNames() {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (EncryptTableRule each : tableRules) {
            result.put(each.getTable(), Collections.singletonList(each.getTable()));
        }
        return result;
    }
}
