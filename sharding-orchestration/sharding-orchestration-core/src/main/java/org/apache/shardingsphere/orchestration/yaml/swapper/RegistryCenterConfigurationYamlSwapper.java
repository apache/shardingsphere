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

package org.apache.shardingsphere.orchestration.yaml.swapper;

import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.orchestration.yaml.config.YamlRegistryCenterConfiguration;

/**
 * Registry center configuration yaml swapper.
 *
 * @author zhaojun
 */
public final class RegistryCenterConfigurationYamlSwapper implements YamlSwapper<YamlRegistryCenterConfiguration, RegistryCenterConfiguration> {
    
    @Override
    public YamlRegistryCenterConfiguration swap(final RegistryCenterConfiguration data) {
        YamlRegistryCenterConfiguration result = new YamlRegistryCenterConfiguration();
        result.setType(data.getType());
        result.setServerLists(data.getServerLists());
        result.setNamespace(data.getNamespace());
        result.setDigest(data.getDigest());
        result.setMaxRetries(data.getMaxRetries());
        result.setOperationTimeoutMilliseconds(data.getOperationTimeoutMilliseconds());
        result.setRetryIntervalMilliseconds(data.getRetryIntervalMilliseconds());
        result.setTimeToLiveSeconds(data.getTimeToLiveSeconds());
        result.setProps(data.getProperties());
        return result;
    }
    
    @Override
    public RegistryCenterConfiguration swap(final YamlRegistryCenterConfiguration yamlConfiguration) {
        RegistryCenterConfiguration result = new RegistryCenterConfiguration(yamlConfiguration.getType(), yamlConfiguration.getProps());
        result.setServerLists(yamlConfiguration.getServerLists());
        result.setNamespace(yamlConfiguration.getNamespace());
        result.setDigest(yamlConfiguration.getDigest());
        result.setMaxRetries(yamlConfiguration.getMaxRetries());
        result.setOperationTimeoutMilliseconds(yamlConfiguration.getOperationTimeoutMilliseconds());
        result.setRetryIntervalMilliseconds(yamlConfiguration.getRetryIntervalMilliseconds());
        result.setTimeToLiveSeconds(yamlConfiguration.getTimeToLiveSeconds());
        return result;
    }
}
