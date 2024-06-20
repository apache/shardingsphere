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

package org.apache.shardingsphere.encrypt.config;

import lombok.Getter;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithmMetaData;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.function.EnhancedRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Encrypt rule configuration.
 */
@Getter
public final class EncryptRuleConfiguration implements DatabaseRuleConfiguration, EnhancedRuleConfiguration {
    
    private final Collection<EncryptTableRuleConfiguration> tables;
    
    private final Map<String, AlgorithmConfiguration> encryptors;
    
    public EncryptRuleConfiguration(final Collection<EncryptTableRuleConfiguration> tables, final Map<String, AlgorithmConfiguration> encryptors) {
        this.tables = tables;
        this.encryptors = rebuildEncryptorsWithDefaultProperties(encryptors);
    }
    
    private Map<String, AlgorithmConfiguration> rebuildEncryptorsWithDefaultProperties(final Map<String, AlgorithmConfiguration> encryptors) {
        Map<String, AlgorithmConfiguration> result = new HashMap<>(encryptors.size(), 1F);
        for (Entry<String, AlgorithmConfiguration> entry : encryptors.entrySet()) {
            // todo Replace with MultiSourceProperties, MultiSourceProperties need support marshal.
            Properties props = new Properties();
            props.putAll(entry.getValue().getProps());
            Properties defaultProps = TypedSPILoader.findUninitedService(EncryptAlgorithm.class, entry.getValue().getType()).map(EncryptAlgorithm::getMetaData)
                    .map(EncryptAlgorithmMetaData::getDefaultProps).orElseGet(Properties::new);
            defaultProps.forEach(props::putIfAbsent);
            result.put(entry.getKey(), new AlgorithmConfiguration(entry.getValue().getType(), props));
        }
        return result;
    }
    
    @Override
    public boolean isEmpty() {
        return tables.isEmpty();
    }
}
