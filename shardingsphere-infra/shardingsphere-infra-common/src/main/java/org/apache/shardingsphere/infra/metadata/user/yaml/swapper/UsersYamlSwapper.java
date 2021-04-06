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

package org.apache.shardingsphere.infra.metadata.user.yaml.swapper;

import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUserConfiguration;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.yaml.swapper.YamlConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * User YAML swapper.
 */
public final class UsersYamlSwapper implements YamlConfigurationSwapper<YamlUsersConfiguration, Collection<ShardingSphereUser>> {
    
    @Override
    public YamlUsersConfiguration swapToYamlConfiguration(final Collection<ShardingSphereUser> data) {
        YamlUsersConfiguration result = new YamlUsersConfiguration();
        Map<String, YamlUserConfiguration> users = new LinkedHashMap<>(data.size(), 1);
        for (ShardingSphereUser each : data) {
            users.put(each.getGrantee().getUsername(), swapToYamlConfiguration(each));
        }
        result.setUsers(users);
        return result;
    }
    
    private YamlUserConfiguration swapToYamlConfiguration(final ShardingSphereUser data) {
        YamlUserConfiguration result = new YamlUserConfiguration();
        result.setHostname(data.getGrantee().getHostname());
        result.setPassword(data.getPassword());
        return result;
    }
    
    @Override
    public Collection<ShardingSphereUser> swapToObject(final YamlUsersConfiguration yamlConfig) {
        Collection<ShardingSphereUser> result = new LinkedHashSet<>();
        if (null == yamlConfig) {
            return result;
        }
        for (Entry<String, YamlUserConfiguration> entry : yamlConfig.getUsers().entrySet()) {
            result.add(swapToObject(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private ShardingSphereUser swapToObject(final String username, final YamlUserConfiguration yamlConfig) {
        return new ShardingSphereUser(username, yamlConfig.getPassword(), (null == yamlConfig.getHostname() || "%".equals(yamlConfig.getHostname())) ? "%" : yamlConfig.getHostname());
    }
}
