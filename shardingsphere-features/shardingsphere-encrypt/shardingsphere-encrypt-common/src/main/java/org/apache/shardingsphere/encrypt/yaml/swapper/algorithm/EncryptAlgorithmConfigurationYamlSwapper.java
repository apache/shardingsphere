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

package org.apache.shardingsphere.encrypt.yaml.swapper.algorithm;

import org.apache.shardingsphere.encrypt.api.config.algorithm.EncryptAlgorithmConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.algorithm.YamlEncryptAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Encrypt algorithm configuration YAML swapper.
 */
public final class EncryptAlgorithmConfigurationYamlSwapper implements YamlSwapper<YamlEncryptAlgorithmConfiguration, EncryptAlgorithmConfiguration> {
    
    @Override
    public YamlEncryptAlgorithmConfiguration swap(final EncryptAlgorithmConfiguration data) {
        YamlEncryptAlgorithmConfiguration result = new YamlEncryptAlgorithmConfiguration();
        result.setType(data.getType());
        result.setProps(data.getProperties());
        return result;
    }
    
    @Override
    public EncryptAlgorithmConfiguration swap(final YamlEncryptAlgorithmConfiguration yamlConfiguration) {
        return new EncryptAlgorithmConfiguration(yamlConfiguration.getType(), yamlConfiguration.getProps());
    }
}
