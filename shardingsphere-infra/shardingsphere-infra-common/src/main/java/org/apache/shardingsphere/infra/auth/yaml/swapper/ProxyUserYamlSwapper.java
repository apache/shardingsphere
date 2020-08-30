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

package org.apache.shardingsphere.infra.auth.yaml.swapper;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

import java.util.Collections;

/**
 * Proxy user YAML swapper.
 */
public final class ProxyUserYamlSwapper implements YamlSwapper<YamlProxyUserConfiguration, ProxyUser> {
    
    @Override
    public YamlProxyUserConfiguration swapToYamlConfiguration(final ProxyUser data) {
        YamlProxyUserConfiguration result = new YamlProxyUserConfiguration();
        result.setPassword(data.getPassword());
        String authorizedSchemas = null == data.getAuthorizedSchemas() ? "" : Joiner.on(',').join(data.getAuthorizedSchemas());
        result.setAuthorizedSchemas(authorizedSchemas);
        return result;
    }
    
    @Override
    public ProxyUser swapToObject(final YamlProxyUserConfiguration yamlConfig) {
        if (Strings.isNullOrEmpty(yamlConfig.getAuthorizedSchemas())) {
            return new ProxyUser(yamlConfig.getPassword(), Collections.emptyList());
        }
        return new ProxyUser(yamlConfig.getPassword(), Splitter.on(',').trimResults().splitToList(yamlConfig.getAuthorizedSchemas()));
    }
}
