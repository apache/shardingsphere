package org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.config;

/**
 * Configuration converter for YAML User content.
 */

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.swapper.UserYamlSwapper;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import java.util.Collection;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlUserConfigurationConverter {
    private static UserYamlSwapper userYamlSwapper = new UserYamlSwapper();

    public static Collection<YamlUserConfiguration> convertYamlUserConfigurations(Collection<ShardingSphereUser> users){
        return users.stream().map(user -> userYamlSwapper.swapToYamlConfiguration(user)).collect(Collectors.toList());
    }

    public static Collection<ShardingSphereUser> convertShardingSphereUsers(Collection<YamlUserConfiguration> yamlUsers) {
        return yamlUsers.stream().map(yamlUser -> userYamlSwapper.swapToObject(yamlUser)).collect(Collectors.toList());
    }

    public static Collection<ShardingSphereUser> convertShardingSphereUser(Collection<String> users) {
        Collection<YamlUserConfiguration> yamlUsers = convertYamlUserConfiguration(users);
        return yamlUsers.stream().map(yamlUser -> userYamlSwapper.swapToObject(yamlUser)).collect(Collectors.toList());
    }

    public static Collection<YamlUserConfiguration> convertYamlUserConfiguration(Collection<String> users) {
        return users.stream().map(user -> convertYamlUserConfiguration(user)).collect(Collectors.toList());
    }

    private static YamlUserConfiguration convertYamlUserConfiguration(String yamlUser) {
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
