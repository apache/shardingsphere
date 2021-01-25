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

package org.apache.shardingsphere.encrypt.algorithm.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.config.RuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Algorithm provided encrypt rule configuration.
 */
@Getter
@Setter
public final class AlgorithmProvidedEncryptRuleConfiguration implements RuleConfiguration {
    
    private Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
    
    private Map<String, EncryptAlgorithm> encryptors = new LinkedHashMap<>();
    
    public AlgorithmProvidedEncryptRuleConfiguration() {
    }
    
    public AlgorithmProvidedEncryptRuleConfiguration(final Collection<EncryptTableRuleConfiguration> tables, final Map<String, EncryptAlgorithm> encryptors) {
        this.tables = tables;
        this.encryptors = encryptors;
    }
}
