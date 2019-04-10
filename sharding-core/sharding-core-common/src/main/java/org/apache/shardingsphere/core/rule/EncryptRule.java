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

import lombok.Getter;
import org.apache.shardingsphere.api.config.encryptor.EncryptRuleConfiguration;
import org.apache.shardingsphere.core.strategy.encrypt.ShardingEncryptorEngine;

import java.util.Collection;

/**
 * Encrypt rule.
 *
 * @author panjuan
 */
public final class EncryptRule implements BaseRule {
    
    @Getter
    private final ShardingEncryptorEngine encryptorEngine;
    
    public EncryptRule(final EncryptRuleConfiguration encryptRuleConfiguration) {
        encryptorEngine = new ShardingEncryptorEngine(encryptRuleConfiguration);
    }
    
    /**
     * Get encrypt table names.
     * 
     * @return encrypt table names
     */
    public Collection<String> getEncryptTableNames() {
        return encryptorEngine.getEncryptTableNames();
    }
}
