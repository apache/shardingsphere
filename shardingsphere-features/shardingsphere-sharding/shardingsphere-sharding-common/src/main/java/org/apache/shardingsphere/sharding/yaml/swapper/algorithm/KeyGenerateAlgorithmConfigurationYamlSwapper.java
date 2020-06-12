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

package org.apache.shardingsphere.sharding.yaml.swapper.algorithm;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.sharding.api.config.algorithm.KeyGenerateAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.algorithm.YamlKeyGenerateAlgorithmConfiguration;

/**
 * Key generate algorithm configuration YAML swapper.
 */
public final class KeyGenerateAlgorithmConfigurationYamlSwapper implements YamlSwapper<YamlKeyGenerateAlgorithmConfiguration, KeyGenerateAlgorithmConfiguration> {
    
    static {
        ShardingSphereServiceLoader.register(KeyGenerateAlgorithm.class);
    }
    
    @Override
    public YamlKeyGenerateAlgorithmConfiguration swap(final KeyGenerateAlgorithmConfiguration data) {
        YamlKeyGenerateAlgorithmConfiguration result = new YamlKeyGenerateAlgorithmConfiguration();
        result.setType(data.getType());
        result.setProperties(data.getProperties());
        return result;
    }
    
    @Override
    public KeyGenerateAlgorithmConfiguration swap(final YamlKeyGenerateAlgorithmConfiguration yamlConfiguration) {
        return new KeyGenerateAlgorithmConfiguration(yamlConfiguration.getType(), yamlConfiguration.getProperties());
    }
}
