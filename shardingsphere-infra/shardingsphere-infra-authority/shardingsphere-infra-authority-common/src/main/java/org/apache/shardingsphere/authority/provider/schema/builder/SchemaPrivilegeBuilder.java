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

package org.apache.shardingsphere.authority.provider.schema.builder;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.schema.SchemaPrivilegesPermittedAuthorityProviderAlgorithm;
import org.apache.shardingsphere.authority.provider.schema.model.privilege.SchemaPrivilegesPermittedShardingSpherePrivileges;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaPrivilegeBuilder {
    
    /**
     * Build privileges.
     *
     * @param users users
     * @param props props
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivileges> build(final Collection<ShardingSphereUser> users, final Properties props) {
        String mappingProp = props.getProperty(SchemaPrivilegesPermittedAuthorityProviderAlgorithm.PROP_USER_SCHEMA_MAPPINGS, "");
        checkSchemas(mappingProp);
        return buildPrivileges(users, mappingProp);
    }
    
    /**
     * Check schemas.
     *
     * @param mappingProp user schema mapping props
     */
    private static void checkSchemas(final String mappingProp) {
        Preconditions.checkArgument(!"".equals(mappingProp), "user-schema-mappings configuration `%s` can not be null", mappingProp);
        Arrays.asList(mappingProp.split(",")).stream().forEach(each -> Preconditions.checkArgument(0 < each.indexOf("@") && 0 < each.indexOf("="),
                "user-schema-mappings configuration `%s` is invalid, the configuration format should be like `username@hostname=schema`", each));
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivileges> buildPrivileges(final Collection<ShardingSphereUser> users, final String mappingProp) {
        Map<ShardingSphereUser, Set<String>> userSchemaMappings = convertSchemas(mappingProp);
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>(users.size(), 1);
        users.forEach(each -> result.put(each, new SchemaPrivilegesPermittedShardingSpherePrivileges(getUserSchemas(each, userSchemaMappings))));
        return result;
    }
    
    /**
     * Convert schemas.
     *
     * @param mappingProp user schema mapping props
     * @return user schema mapping map
     */
    private static Map<ShardingSphereUser, Set<String>> convertSchemas(final String mappingProp) {
        String[] mappings = mappingProp.split(",");
        Map<ShardingSphereUser, Set<String>> result = new HashMap<>(mappings.length, 1);
        Arrays.asList(mappings).stream().forEach(each -> {
            String[] userSchemaPair = each.trim().split("=");
            String yamlUser = userSchemaPair[0];
            String username = yamlUser.substring(0, yamlUser.indexOf("@"));
            String hostname = yamlUser.substring(yamlUser.indexOf("@") + 1);
            ShardingSphereUser shardingSphereUser = new ShardingSphereUser(username, "", hostname);
            Set<String> schemas = result.getOrDefault(shardingSphereUser, new HashSet<>());
            schemas.add(userSchemaPair[1]);
            result.putIfAbsent(shardingSphereUser, schemas);
        });
        return result;
    }
    
    private static Set<String> getUserSchemas(final ShardingSphereUser shardingSphereUser, final Map<ShardingSphereUser, Set<String>> userSchemaMappings) {
        Set<String> result = new HashSet<>();
        for (Map.Entry<ShardingSphereUser, Set<String>> entry : userSchemaMappings.entrySet()) {
            boolean isAnyOtherHost = checkAnyOtherHost(entry.getKey().getGrantee(), shardingSphereUser);
            if (isAnyOtherHost || shardingSphereUser == entry.getKey() || shardingSphereUser.equals(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
    
    private static boolean checkAnyOtherHost(final Grantee grantee, final ShardingSphereUser shardingSphereUser) {
        return ("%".equalsIgnoreCase(grantee.getHostname())
                || grantee.getHostname().equals(shardingSphereUser.getGrantee().getHostname()))
                && grantee.getUsername().equals(shardingSphereUser.getGrantee().getUsername());
    }
}
