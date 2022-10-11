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

package org.apache.shardingsphere.infra.metadata.user.yaml.config;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.yaml.swapper.YamlUserSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML Users content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlUsersConfigurationConverter {
    
    private static final YamlUserSwapper SWAPPER = new YamlUserSwapper();
    
    /**
     * Convert to users YAML content.
     *
     * @param users ShardingSphere users
     * @return users YAML content
     */
    public static Collection<String> convertYamlUserConfigurations(final Collection<ShardingSphereUser> users) {
        Collection<String> result = new LinkedList<>();
        users.stream().map(SWAPPER::swapToYamlConfiguration).forEach(each -> result.add(each.toString()));
        return result;
    }
    
    /**
     * Convert to ShardingSphere users.
     *
     * @param users users YAML content
     * @return ShardingSphere users
     */
    public static Collection<ShardingSphereUser> convertShardingSphereUser(final Collection<String> users) {
        Collection<YamlUserConfiguration> yamlUsers = convertYamlUserConfiguration(users);
        return yamlUsers.stream().map(SWAPPER::swapToObject).collect(Collectors.toList());
    }
    
    /**
     * Convert to YAML user configurations.
     *
     * @param users users YAML content
     * @return YAML user configurations
     */
    public static Collection<YamlUserConfiguration> convertYamlUserConfiguration(final Collection<String> users) {
        return users.stream().map(YamlUsersConfigurationConverter::convertYamlUserConfiguration).collect(Collectors.toList());
    }
    
    /**
     * Convert to YAML user configuration.
     *
     * @param yamlUser user YAML content
     * @return YAML user configuration
     */
    private static YamlUserConfiguration convertYamlUserConfiguration(final String yamlUser) {
        Preconditions.checkArgument(0 < yamlUser.indexOf("@") && 0 < yamlUser.indexOf(":") && yamlUser.indexOf(":") <= yamlUser.length() - 1,
                "user configuration `%s` is invalid, the configuration format should be like `username@hostname:password`", yamlUser);
        Preconditions.checkArgument(yamlUser.indexOf("@") < yamlUser.indexOf(":"),
                "user configuration `%s` is invalid, the configuration format should be like `username@hostname:password`", yamlUser);
        String username = yamlUser.substring(0, yamlUser.indexOf("@"));
        String hostname = yamlUser.substring(yamlUser.indexOf("@") + 1, yamlUser.indexOf(":"));
        String password = yamlUser.substring(yamlUser.indexOf(":") + 1);
        YamlUserConfiguration result = new YamlUserConfiguration();
        result.setUsername(username);
        result.setHostname(hostname);
        result.setPassword(password);
        return result;
    }
}
