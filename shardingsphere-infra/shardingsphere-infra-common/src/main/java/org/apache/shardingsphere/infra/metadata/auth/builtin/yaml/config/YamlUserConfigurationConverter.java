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

package org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.swapper.UserYamlSwapper;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML User content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlUserConfigurationConverter {
    private static UserYamlSwapper userYamlSwapper = new UserYamlSwapper();

    /**
     * convert users Yaml content.
     * @param users collection of ShardingSphereUser
     * @return collection of formatting users content
     */
    public static Collection<String> convertYamlUserConfigurationFormattings(final Collection<ShardingSphereUser> users) {
        Collection<String> result = new LinkedList<>();
        users.stream().map(user -> userYamlSwapper.swapToYamlConfiguration(user)).forEach(user -> result.add(user.toString()));
        return result;
    }

    /**
     * convert users Yaml content.
     * @param users collection of ShardingSphereUser
     * @return collection of formatting users content
     */
    public static Collection<YamlUserConfiguration> convertYamlUserConfigurations(final Collection<ShardingSphereUser> users) {
        return users.stream().map(user -> userYamlSwapper.swapToYamlConfiguration(user)).collect(Collectors.toList());
    }

    /**
     * convert ShardingSphereUser.
     * @param users users yaml content
     * @return collection of ShardingSphereUser
     */
    public static Collection<ShardingSphereUser> convertShardingSphereUser(final Collection<String> users) {
        Collection<YamlUserConfiguration> yamlUsers = convertYamlUserConfiguration(users);
        return yamlUsers.stream().map(yamlUser -> userYamlSwapper.swapToObject(yamlUser)).collect(Collectors.toList());
    }

    /**
     * convert YamlUserConfiguration.
     * @param users users yaml content
     * @return collection of YamlUserConfiguration
     */
    public static Collection<YamlUserConfiguration> convertYamlUserConfiguration(final Collection<String> users) {
        return users.stream().map(user -> convertYamlUserConfiguration(user)).collect(Collectors.toList());
    }

    /**
     * convert YamlUserConfiguration.
     * @param yamlUser users yaml content
     * @return YamlUserConfiguration
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
