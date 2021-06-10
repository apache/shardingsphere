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
import org.apache.shardingsphere.infra.metadata.user.yaml.swapper.UserYamlSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML Users content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlUsersConfigurationConverter {

    private static final UserYamlSwapper USER_YAML_SWAPPER = new UserYamlSwapper();

    /**
     * Convert to users yaml content.
     *
     * @param users sharding sphere users
     * @return users yaml content
     */
    public static Collection<String> convertYamlUserConfigurations(final Collection<ShardingSphereUser> users) {
        Collection<String> result = new LinkedList<>();
        users.stream().map(USER_YAML_SWAPPER::swapToYamlConfiguration).forEach(user -> result.add(user.toString()));
        return result;
    }

    /**
     * Convert to sharding sphere users.
     *
     * @param users users yaml content
     * @return sharding sphere users
     */
    public static Collection<ShardingSphereUser> convertShardingSphereUser(final Collection<String> users) {
        Collection<YamlUserConfiguration> yamlUsers = convertYamlUserConfiguration(users);
        return yamlUsers.stream().map(USER_YAML_SWAPPER::swapToObject).collect(Collectors.toList());
    }

    /**
     * Convert to yaml user configurations.
     *
     * @param users users yaml content
     * @return yaml user configurations
     */
    public static Collection<YamlUserConfiguration> convertYamlUserConfiguration(final Collection<String> users) {
        return users.stream().map(YamlUsersConfigurationConverter::convertYamlUserConfiguration).collect(Collectors.toList());
    }

    /**
     * Convert to yaml user configuration.
     *
     * @param yamlUser user yaml content
     * @return yaml user configuration
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
