package org.apache.shardingsphere.authority.provider.custom.builder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.custom.CustomPrivilegesPermittedAuthorityProviderAlgorithm;
import org.apache.shardingsphere.authority.provider.custom.model.privilege.CustomPrivilegesPermittedShardingSpherePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomPrivilegeBuilder {
    /**
     * Build privileges.
     *
     * @param users users
     * @param props props
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivileges> build(final Collection<ShardingSphereUser> users, final Properties props) {

        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>();

        Map<String, Set<String>> userSchemaMappings = new HashMap<>();
        String[] mappings = StringUtils.split(props.getProperty(CustomPrivilegesPermittedAuthorityProviderAlgorithm.PROP_USER_SCHEMA_MAPPINGS, ""), ",");
        for (String mapping : mappings) {
            String[] userSchemaPair = StringUtils.split(mapping.trim(), "=");
            Set<String> schemas = userSchemaMappings.getOrDefault(userSchemaPair[0], new HashSet<>());
            schemas.add(userSchemaPair[1]);
            userSchemaMappings.putIfAbsent(userSchemaPair[0], schemas);
        }
        users.forEach(each -> result.put(each, new CustomPrivilegesPermittedShardingSpherePrivileges(userSchemaMappings.getOrDefault(each.getGrantee().getUsername(), Collections.emptySet()))));
        return result;
    }

}
