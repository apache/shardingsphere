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

package org.apache.shardingsphere.encrypt.api.config.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/**
 * Encrypt column rule configuration.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class EncryptColumnRuleConfiguration {
    
    private final String name;
    
    private final EncryptColumnItemRuleConfiguration cipher;
    
    private EncryptColumnItemRuleConfiguration assistedQuery;
    
    private EncryptColumnItemRuleConfiguration likeQuery;
    
    /**
     * Get assisted query.
     *
     * @return assisted query column item rule configuration
     */
    public Optional<EncryptColumnItemRuleConfiguration> getAssistedQuery() {
        return Optional.ofNullable(assistedQuery);
    }
    
    /**
     * Get like query.
     *
     * @return like query column item rule configuration
     */
    public Optional<EncryptColumnItemRuleConfiguration> getLikeQuery() {
        return Optional.ofNullable(likeQuery);
    }
}
