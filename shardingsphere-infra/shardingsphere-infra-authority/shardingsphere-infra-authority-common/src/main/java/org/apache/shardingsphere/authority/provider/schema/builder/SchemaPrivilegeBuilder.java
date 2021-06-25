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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.provider.schema.SchemaPrivilegesPermittedAuthorityProviderAlgorithm;
import org.apache.shardingsphere.authority.provider.schema.model.privilege.SchemaPrivilegesPermittedShardingSpherePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import com.google.common.base.Preconditions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
        Preconditions.checkArgument(!"".equals(mappingProp), "user-schema-mappings configuration `%s` can not be null", mappingProp);
        String[] mappings = mappingProp.split(",");
        Arrays.asList(mappings).stream().forEach(each -> Preconditions.checkArgument(0 < each.indexOf("@") && 0 < each.indexOf("="), 
                "user-schema-mappings configuration `%s` is invalid, the configuration format should be like `username@hostname=schema`", each));
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>();
        Map<ShardingSphereUser, Set<String>> userSchemaMappings = new HashMap<>();
        Arrays.asList(mappings).stream().map(SchemaPrivilegeBuilder::convertSchemas).forEach(each -> merge(each, userSchemaMappings));
        users.forEach(each -> result.put(each, new SchemaPrivilegesPermittedShardingSpherePrivileges(userSchemaMappings.getOrDefault(each, Collections.emptySet()))));
        return result;
    }
    
    /**
     * Convert schemas.
     * @param userSchemaMapping user shcema mapping
     * @return schemas
     */
    private static Map<ShardingSphereUser, String> convertSchemas(final String userSchemaMapping) {
        String[] userSchemaPair = userSchemaMapping.trim().split("=");
        Map<ShardingSphereUser, String> result = new HashMap<>();

        String yamlUser = userSchemaPair[0];
        String username = yamlUser.substring(0, yamlUser.indexOf("@"));
        String hostname = yamlUser.substring(yamlUser.indexOf("@") + 1);

        result.put(new ShardingSphereUser(username, "", hostname), userSchemaPair[1]);
        return result;
    }
    
    /**
     * Merge user schema to result.
     * @param userSchema user schema
     * @param result result
     */
    private static void merge(final Map<ShardingSphereUser, String> userSchema, final Map<ShardingSphereUser, Set<String>> result) {
        for (Entry<ShardingSphereUser, String> entry : userSchema.entrySet()) {
            Set<String> schemas = result.getOrDefault(entry.getKey(), new HashSet<>());
            schemas.add(entry.getValue());
            result.putIfAbsent(entry.getKey(), schemas);
        }
    }
}
