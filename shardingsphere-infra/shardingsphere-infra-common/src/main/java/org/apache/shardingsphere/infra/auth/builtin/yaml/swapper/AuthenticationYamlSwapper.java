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

package org.apache.shardingsphere.infra.auth.builtin.yaml.swapper;

import com.google.common.collect.Maps;
import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.auth.builtin.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

/**
 * Authentication YAML swapper.
 */
public final class AuthenticationYamlSwapper implements YamlSwapper<YamlAuthenticationConfiguration, DefaultAuthentication> {
    
    private final UserYamlSwapper userYamlSwapper = new UserYamlSwapper();
    
    @Override
    public YamlAuthenticationConfiguration swapToYamlConfiguration(final DefaultAuthentication data) {
        YamlAuthenticationConfiguration result = new YamlAuthenticationConfiguration();
        result.setUsers(Maps.transformValues(data.getUsers(), userYamlSwapper::swapToYamlConfiguration));
        return result;
    }
    
    @Override
    public DefaultAuthentication swapToObject(final YamlAuthenticationConfiguration yamlConfig) {
        DefaultAuthentication result = new DefaultAuthentication();
        if (null == yamlConfig) {
            return result;
        }
        result.getUsers().putAll(Maps.transformValues(yamlConfig.getUsers(), userYamlSwapper::swapToObject));
        return result;
    }
}
