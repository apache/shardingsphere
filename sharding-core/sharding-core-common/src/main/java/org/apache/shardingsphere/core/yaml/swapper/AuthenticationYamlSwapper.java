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

package org.apache.shardingsphere.core.yaml.swapper;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.rule.ProxyUser;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.config.common.YamlProxyUserConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.swapper.YamlSwapper;

/**
 * Authentication YAML swapper.
 *
 * @author zhangliang
 */
public final class AuthenticationYamlSwapper implements YamlSwapper<YamlAuthenticationConfiguration, Authentication> {
    
    private final ProxyUserYamlSwapper proxyUserYamlSwapper = new ProxyUserYamlSwapper();
    
    @Override
    public YamlAuthenticationConfiguration swap(final Authentication data) {
        YamlAuthenticationConfiguration result = new YamlAuthenticationConfiguration();
        result.getUsers().putAll(Maps.transformValues(data.getUsers(), new Function<ProxyUser, YamlProxyUserConfiguration>() {
    
            @Override
            public YamlProxyUserConfiguration apply(final ProxyUser input) {
                return proxyUserYamlSwapper.swap(input);
            }
        }));
        return result;
    }
    
    @Override
    public Authentication swap(final YamlAuthenticationConfiguration yamlConfiguration) {
        Authentication result = new Authentication();
        result.getUsers().putAll(Maps.transformValues(yamlConfiguration.getUsers(), new Function<YamlProxyUserConfiguration, ProxyUser>() {
            
            @Override
            public ProxyUser apply(final YamlProxyUserConfiguration input) {
                return proxyUserYamlSwapper.swap(input);
            }
        }));
        return result;
    }
}
