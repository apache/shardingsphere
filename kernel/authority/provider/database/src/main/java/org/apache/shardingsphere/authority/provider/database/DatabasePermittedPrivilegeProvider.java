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

package org.apache.shardingsphere.authority.provider.database;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.spi.PrivilegeProvider;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database permitted privilege provider.
 */
public final class DatabasePermittedPrivilegeProvider implements PrivilegeProvider {
    
    private static final String USER_DATABASE_MAPPINGS_KEY = "user-database-mappings";
    
    private String userDatabaseMappings;
    
    @Override
    public void init(final Properties props) {
        userDatabaseMappings = props.getProperty(DatabasePermittedPrivilegeProvider.USER_DATABASE_MAPPINGS_KEY, "");
        checkUserDatabaseMappings();
    }
    
    private void checkUserDatabaseMappings() {
        Preconditions.checkArgument(!"".equals(userDatabaseMappings), "user-database-mappings configuration `%s` can not be null", userDatabaseMappings);
        Arrays.stream(userDatabaseMappings.split(",")).forEach(each -> Preconditions.checkArgument(each.contains("@") && each.contains("="),
                "user-database-mappings configuration `%s` is invalid, the configuration format should be like `username@hostname=database`", each));
    }
    
    @Override
    public Map<Grantee, ShardingSpherePrivileges> build(final AuthorityRuleConfiguration ruleConfig) {
        Map<ShardingSphereUser, Collection<String>> userDatabaseMappings = convertUserDatabases();
        return ruleConfig.getUsers().stream().collect(Collectors.toMap(ShardingSphereUser::getGrantee, each -> new DatabasePermittedPrivileges(getUserDatabases(each, userDatabaseMappings))));
    }
    
    private Map<ShardingSphereUser, Collection<String>> convertUserDatabases() {
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
    
    private Collection<String> getUserDatabases(final ShardingSphereUser user, final Map<ShardingSphereUser, Collection<String>> userDatabaseMappings) {
        Collection<String> result = new HashSet<>();
        for (Entry<ShardingSphereUser, Collection<String>> entry : userDatabaseMappings.entrySet()) {
            boolean isAnyOtherHost = entry.getKey().getGrantee().accept(user.getGrantee());
            if (isAnyOtherHost || user.equals(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "DATABASE_PERMITTED";
    }
    
    @Override
    public Collection<Object> getTypeAliases() {
        return Collections.singleton("SCHEMA_PRIVILEGES_PERMITTED");
    }
}
