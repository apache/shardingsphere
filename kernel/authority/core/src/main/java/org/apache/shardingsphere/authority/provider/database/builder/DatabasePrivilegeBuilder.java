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
import org.apache.shardingsphere.authority.provider.database.DatabasePermittedAuthorityRegistryProvider;
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
import java.util.stream.Collectors;

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
     * @return built privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivileges> build(final Collection<ShardingSphereUser> users, final Properties props) {
        String userDatabaseMappings = props.getProperty(DatabasePermittedAuthorityRegistryProvider.PROP_USER_DATABASE_MAPPINGS, "");
        checkDatabases(userDatabaseMappings);
        return buildPrivileges(users, convertUserDatabases(userDatabaseMappings));
    }
    
    private static void checkDatabases(final String userDatabaseMappings) {
        Preconditions.checkArgument(!"".equals(userDatabaseMappings), "user-database-mappings configuration `%s` can not be null", userDatabaseMappings);
        Arrays.stream(userDatabaseMappings.split(",")).forEach(each -> Preconditions.checkArgument(each.contains("@") && each.contains("="),
                "user-database-mappings configuration `%s` is invalid, the configuration format should be like `username@hostname=database`", each));
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivileges> buildPrivileges(final Collection<ShardingSphereUser> users,
                                                                                     final Map<ShardingSphereUser, Collection<String>> userDatabaseMappings) {
        return users.stream().collect(Collectors.toMap(each -> each, each -> new DatabasePermittedPrivileges(getUserDatabases(each, userDatabaseMappings))));
    }
    
    private static Collection<String> getUserDatabases(final ShardingSphereUser user, final Map<ShardingSphereUser, Collection<String>> userDatabaseMappings) {
        Collection<String> result = new HashSet<>();
        for (Entry<ShardingSphereUser, Collection<String>> entry : userDatabaseMappings.entrySet()) {
            boolean isAnyOtherHost = checkAnyOtherHost(entry.getKey().getGrantee(), user);
            if (isAnyOtherHost || user.equals(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
    
    private static boolean checkAnyOtherHost(final Grantee grantee, final ShardingSphereUser user) {
        return ("%".equals(grantee.getHostname())
                || grantee.getHostname().equals(user.getGrantee().getHostname())) && grantee.getUsername().equals(user.getGrantee().getUsername());
    }
    
    private static Map<ShardingSphereUser, Collection<String>> convertUserDatabases(final String userDatabaseMappings) {
        String[] mappings = userDatabaseMappings.split(",");
        Map<ShardingSphereUser, Collection<String>> result = new HashMap<>(mappings.length, 1F);
        for (String each : mappings) {
            String[] userDatabasePair = each.trim().split("=");
            ShardingSphereUser user = new ShardingSphereUser(userDatabasePair[0]);
            Collection<String> databases = result.getOrDefault(user, new HashSet<>());
            databases.add(userDatabasePair[1]);
            result.putIfAbsent(user, databases);
        }
        return result;
    }
}
