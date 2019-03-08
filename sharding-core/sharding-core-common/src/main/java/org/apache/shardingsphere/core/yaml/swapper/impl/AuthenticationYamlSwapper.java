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

package org.apache.shardingsphere.core.yaml.swapper.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthentication;
import org.apache.shardingsphere.core.yaml.swapper.YamlSwapper;

/**
 * Authentication YAML swapper.
 *
 * @author zhangliang
 */
public final class AuthenticationYamlSwapper implements YamlSwapper<YamlAuthentication, Authentication> {
    
    @Override
    public YamlAuthentication swap(final Authentication data) {
        YamlAuthentication result = new YamlAuthentication();
        result.setUsername(data.getUsername());
        result.setPassword(data.getPassword());
        return result;
    }
    
    @Override
    public Authentication swap(final YamlAuthentication yamlConfiguration) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(yamlConfiguration.getUsername()), "Username is required.");
        return new Authentication(yamlConfiguration.getUsername(), yamlConfiguration.getPassword());
    }
}
