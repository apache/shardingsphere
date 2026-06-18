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
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database permitted privilege provider.
 */
public final class DatabasePermittedPrivilegeProvider implements PrivilegeProvider {
    
    private static final String USER_DATABASE_MAPPINGS_KEY = "user-database-mappings";
    
    private String userDatabasesMappings;
    
    @Override
    public void init(final Properties props) {
        userDatabasesMappings = props.getProperty(USER_DATABASE_MAPPINGS_KEY, "");
        checkUserDatabasesMappings();
    }
    
    private void checkUserDatabasesMappings() {
        Preconditions.checkArgument(!"".equals(userDatabasesMappings), "user-database-mappings configuration `%s` can not be null", userDatabasesMappings);
        Arrays.stream(userDatabasesMappings.split(",")).forEach(each -> Preconditions.checkArgument(each.contains("@") && each.contains("="),
                "user-database-mappings configuration `%s` is invalid, the configuration format should be like `username@hostname=database`", each));
    }
    
    @Override
    public ShardingSpherePrivileges build(final AuthorityRuleConfiguration ruleConfig, final Grantee grantee) {
        Map<ShardingSphereUser, Collection<String>> userDatabasesMappings = convertToUserDatabasesMappings();
        return new DatabasePermittedPrivileges(getUserDatabases(grantee, userDatabasesMappings));
    }
    
    private Map<ShardingSphereUser, Collection<String>> convertToUserDatabasesMappings() {
        String[] mappings = userDatabasesMappings.split(",");
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
    
    private Collection<String> getUserDatabases(final Grantee grantee, final Map<ShardingSphereUser, Collection<String>> userDatabasesMappings) {
        return userDatabasesMappings.entrySet().stream().filter(entry -> entry.getKey().getGrantee().accept(grantee)).flatMap(entry -> entry.getValue().stream()).collect(Collectors.toSet());
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
