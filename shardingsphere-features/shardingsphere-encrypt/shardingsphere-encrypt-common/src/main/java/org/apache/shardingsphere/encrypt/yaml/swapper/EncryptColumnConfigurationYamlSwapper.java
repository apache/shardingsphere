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

package org.apache.shardingsphere.encrypt.yaml.swapper;

import org.apache.shardingsphere.encrypt.api.config.EncryptColumnConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptColumnConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Encrypt column configuration YAML swapper.
 */
public final class EncryptColumnConfigurationYamlSwapper implements YamlSwapper<YamlEncryptColumnConfiguration, EncryptColumnConfiguration> {
    
    @Override
    public YamlEncryptColumnConfiguration swap(final EncryptColumnConfiguration data) {
        YamlEncryptColumnConfiguration result = new YamlEncryptColumnConfiguration();
        result.setPlainColumn(data.getPlainColumn());
        result.setCipherColumn(data.getCipherColumn());
        result.setAssistedQueryColumn(data.getAssistedQueryColumn());
        result.setEncryptor(data.getEncryptorName());
        return result;
    }
    
    @Override
    public EncryptColumnConfiguration swap(final YamlEncryptColumnConfiguration yamlConfiguration) {
        return new EncryptColumnConfiguration(yamlConfiguration.getLogicName(), 
                yamlConfiguration.getPlainColumn(), yamlConfiguration.getCipherColumn(), yamlConfiguration.getAssistedQueryColumn(), yamlConfiguration.getEncryptor());
    }
}
