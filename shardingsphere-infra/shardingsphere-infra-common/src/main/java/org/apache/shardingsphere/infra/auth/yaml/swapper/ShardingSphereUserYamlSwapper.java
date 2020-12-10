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
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlShardingSphereUserConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlSwapper;

import java.util.Collections;

/**
 * ShardingSphere user YAML swapper.
 */
public final class ShardingSphereUserYamlSwapper implements YamlSwapper<YamlShardingSphereUserConfiguration, ShardingSphereUser> {
    
    @Override
    public YamlShardingSphereUserConfiguration swapToYamlConfiguration(final ShardingSphereUser data) {
        YamlShardingSphereUserConfiguration result = new YamlShardingSphereUserConfiguration();
        result.setPassword(data.getPassword());
        String authorizedSchemas = null == data.getAuthorizedSchemas() ? "" : Joiner.on(',').join(data.getAuthorizedSchemas());
        result.setAuthorizedSchemas(authorizedSchemas);
        return result;
    }
    
    @Override
    public ShardingSphereUser swapToObject(final YamlShardingSphereUserConfiguration yamlConfig) {
        if (Strings.isNullOrEmpty(yamlConfig.getAuthorizedSchemas())) {
            return new ShardingSphereUser(yamlConfig.getPassword(), Collections.emptyList());
        }
        return new ShardingSphereUser(yamlConfig.getPassword(), Splitter.on(',').trimResults().splitToList(yamlConfig.getAuthorizedSchemas()));
    }
}
