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

package org.apache.shardingsphere.infra.metadata.auth.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.database.SchemaPrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.database.TablePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Privilege merger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeMerger {
    
    /**
     * Merge privilege.
     * 
     * @param authentication authentication
     * @param schemaName schema name
     * @param rules ShardingSphere rules
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivilege> merge(final Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> authentication,
                                                                         final String schemaName, final Collection<ShardingSphereRule> rules) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new HashMap<>(authentication.size(), 1);
        for (Entry<ShardingSphereUser, Collection<ShardingSpherePrivilege>> entry : authentication.entrySet()) {
            result.put(entry.getKey(), merge(entry.getKey(), entry.getValue(), schemaName, rules));
        }
        return result;
    }
    
    private static ShardingSpherePrivilege merge(final ShardingSphereUser user, final Collection<ShardingSpherePrivilege> privileges, final String schemaName,
                                                 final Collection<ShardingSphereRule> rules) {
        if (privileges.isEmpty()) {
            return new ShardingSpherePrivilege();
        }
        Iterator<ShardingSpherePrivilege> iterator = privileges.iterator();
        ShardingSpherePrivilege result = iterator.next();
        while (iterator.hasNext()) {
            ShardingSpherePrivilege each = iterator.next();
            if (!result.equals(each)) {
                throw new ShardingSphereException("Different physical instances have different permissions for user %s@%s", user.getGrantee().getUsername(), user.getGrantee().getHostname());
            }
        }
        merge(result, schemaName, rules);
        return result;
    }
    
    private static void merge(final ShardingSpherePrivilege privilege, final String schemaName, final Collection<ShardingSphereRule> rules) {
        Map<String, SchemaPrivilege> schemaPrivilegeMap = new HashMap<>();
        for (Entry<String, SchemaPrivilege> entry : privilege.getDatabasePrivilege().getSpecificPrivileges().entrySet()) {
            if (!schemaPrivilegeMap.containsKey(schemaName)) {
                SchemaPrivilege newSchemaPrivilege = new SchemaPrivilege(schemaName);
                newSchemaPrivilege.getGlobalPrivileges().addAll(entry.getValue().getGlobalPrivileges());
                newSchemaPrivilege.getSpecificPrivileges().putAll(entry.getValue().getSpecificPrivileges());
                merge(newSchemaPrivilege, rules);
                schemaPrivilegeMap.put(schemaName, newSchemaPrivilege);
            }
        }
        privilege.getDatabasePrivilege().getSpecificPrivileges().clear();
        privilege.getDatabasePrivilege().getSpecificPrivileges().putAll(schemaPrivilegeMap);
    }
    
    private static void merge(final SchemaPrivilege privilege, final Collection<ShardingSphereRule> rules) {
        Map<String, TablePrivilege> tablePrivilegeMap = new HashMap<>();
        for (Entry<String, TablePrivilege> entry : privilege.getSpecificPrivileges().entrySet()) {
            Optional<String> logicalTable = getLogicalTable(entry, rules);
            if (logicalTable.isPresent() && !tablePrivilegeMap.containsKey(logicalTable.get())) {
                tablePrivilegeMap.put(logicalTable.get(), new TablePrivilege(logicalTable.get(), entry.getValue().getPrivileges()));
            }
        }
        privilege.getSpecificPrivileges().clear();
        privilege.getSpecificPrivileges().putAll(tablePrivilegeMap);
    }
    
    private static Optional<String> getLogicalTable(final Entry<String, TablePrivilege> privilege, final Collection<ShardingSphereRule> rules) {
        for (ShardingSphereRule each : rules) {
            if (each instanceof DataNodeContainedRule) {
                Optional<String> logicalTable = ((DataNodeContainedRule) each).findLogicTableByActualTable(privilege.getKey());
                if (logicalTable.isPresent()) {
                    return logicalTable;
                }
            }
        }
        return Optional.empty();
    }
}
