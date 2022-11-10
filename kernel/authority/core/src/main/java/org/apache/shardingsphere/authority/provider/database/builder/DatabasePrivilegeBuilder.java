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

package org.apache.shardingsphere.authority.provider.database.builder;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.database.DatabasePermittedPrivilegesProviderAlgorithm;
import org.apache.shardingsphere.authority.provider.database.model.privilege.DatabasePermittedPrivileges;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Database privilege builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabasePrivilegeBuilder {
    
    /**
     * Build privileges.
     *
     * @param users users
     * @param props props
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivileges> build(final Collection<ShardingSphereUser> users, final Properties props) {
        String mappingProp = props.getProperty(DatabasePermittedPrivilegesProviderAlgorithm.PROP_USER_DATABASE_MAPPINGS, "");
        checkDatabases(mappingProp);
        return buildPrivileges(users, mappingProp);
    }
    
    /**
     * Check databases.
     *
     * @param mappingProp user database mapping props
     */
    private static void checkDatabases(final String mappingProp) {
        Preconditions.checkArgument(!"".equals(mappingProp), "user-database-mappings configuration `%s` can not be null", mappingProp);
        Arrays.stream(mappingProp.split(",")).forEach(each -> Preconditions.checkArgument(0 < each.indexOf("@") && 0 < each.indexOf("="),
                "user-database-mappings configuration `%s` is invalid, the configuration format should be like `username@hostname=database`", each));
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivileges> buildPrivileges(final Collection<ShardingSphereUser> users, final String mappingProp) {
        Map<ShardingSphereUser, Collection<String>> userDatabaseMappings = convertDatabases(mappingProp);
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>(users.size(), 1);
        users.forEach(each -> result.put(each, new DatabasePermittedPrivileges(new HashSet<>(getUserDatabases(each, userDatabaseMappings)))));
        return result;
    }
    
    /**
     * Convert databases.
     *
     * @param mappingProp user database mapping props
     * @return user database mapping map
     */
    private static Map<ShardingSphereUser, Collection<String>> convertDatabases(final String mappingProp) {
        String[] mappings = mappingProp.split(",");
        Map<ShardingSphereUser, Collection<String>> result = new HashMap<>(mappings.length, 1);
        Arrays.asList(mappings).forEach(each -> {
            String[] userDatabasePair = each.trim().split("=");
            String yamlUser = userDatabasePair[0];
            String username = yamlUser.substring(0, yamlUser.indexOf("@"));
            String hostname = yamlUser.substring(yamlUser.indexOf("@") + 1);
            ShardingSphereUser shardingSphereUser = new ShardingSphereUser(username, "", hostname);
            Collection<String> databases = result.getOrDefault(shardingSphereUser, new HashSet<>());
            databases.add(userDatabasePair[1]);
            result.putIfAbsent(shardingSphereUser, databases);
        });
        return result;
    }
    
    private static Collection<String> getUserDatabases(final ShardingSphereUser shardingSphereUser, final Map<ShardingSphereUser, Collection<String>> userDatabaseMappings) {
        Set<String> result = new HashSet<>();
        for (Entry<ShardingSphereUser, Collection<String>> entry : userDatabaseMappings.entrySet()) {
            boolean isAnyOtherHost = checkAnyOtherHost(entry.getKey().getGrantee(), shardingSphereUser);
            if (isAnyOtherHost || shardingSphereUser == entry.getKey() || shardingSphereUser.equals(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
    
    private static boolean checkAnyOtherHost(final Grantee grantee, final ShardingSphereUser shardingSphereUser) {
        return ("%".equals(grantee.getHostname())
                || grantee.getHostname().equals(shardingSphereUser.getGrantee().getHostname())) && grantee.getUsername().equals(shardingSphereUser.getGrantee().getUsername());
    }
}
