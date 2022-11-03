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

/**
 * Encrypt column rule configuration.
 */
@RequiredArgsConstructor
@Getter
public final class EncryptColumnRuleConfiguration {
    
    private final String logicColumn;
    
    private final String cipherColumn;
    
    private final String assistedQueryColumn;
    
    private final String fuzzyQueryColumn;
    
    private final String plainColumn;
    
    private final String encryptorName;
    
    private final String assistedQueryEncryptorName;
    
    private final String fuzzyQueryEncryptorName;
    
    private final Boolean queryWithCipherColumn;
    
    public EncryptColumnRuleConfiguration(final String logicColumn, final String cipherColumn, final String assistedQueryColumn, final String fuzzyQueryColumn,
                                          final String plainColumn, final String encryptorName, final Boolean queryWithCipherColumn) {
        this(logicColumn, cipherColumn, assistedQueryColumn, fuzzyQueryColumn, plainColumn, encryptorName, null, null, queryWithCipherColumn);
    }
}
