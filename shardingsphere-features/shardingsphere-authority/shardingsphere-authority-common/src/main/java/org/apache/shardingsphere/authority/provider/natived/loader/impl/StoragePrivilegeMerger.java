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

package org.apache.shardingsphere.authority.provider.natived.loader.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivileges;
import org.apache.shardingsphere.authority.model.database.SchemaPrivileges;
import org.apache.shardingsphere.authority.model.database.TablePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Storage privilege merger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoragePrivilegeMerger {
    
    /**
     * Merge privileges.
     * 
     * @param privileges privileges
     * @param schemaName schema name
     * @param rules ShardingSphere rules
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivileges> merge(final Map<ShardingSphereUser, Collection<ShardingSpherePrivileges>> privileges,
                                                                         final String schemaName, final Collection<ShardingSphereRule> rules) {
        Map<ShardingSphereUser, ShardingSpherePrivileges> result = new HashMap<>(privileges.size(), 1);
        for (Entry<ShardingSphereUser, Collection<ShardingSpherePrivileges>> entry : privileges.entrySet()) {
            result.put(entry.getKey(), merge(entry.getValue(), schemaName, rules));
        }
        return result;
    }
    
    private static ShardingSpherePrivileges merge(final Collection<ShardingSpherePrivileges> privileges, final String schemaName, final Collection<ShardingSphereRule> rules) {
        return privileges.isEmpty() ? new ShardingSpherePrivileges() : getMergedPrivileges(privileges.iterator().next(), schemaName, rules);
    }
    
    private static ShardingSpherePrivileges getMergedPrivileges(final ShardingSpherePrivileges privilege, final String schemaName, final Collection<ShardingSphereRule> rules) {
        ShardingSpherePrivileges result = new ShardingSpherePrivileges();
        result.getAdministrativePrivileges().getPrivileges().addAll(privilege.getAdministrativePrivileges().getPrivileges());
        result.getDatabasePrivileges().getGlobalPrivileges().addAll(privilege.getDatabasePrivileges().getGlobalPrivileges());
        result.getDatabasePrivileges().getSpecificPrivileges().putAll(getMergedSchemaPrivileges(privilege, schemaName, rules));
        return result;
    }
    
    private static Map<String, SchemaPrivileges> getMergedSchemaPrivileges(final ShardingSpherePrivileges privilege, final String schemaName, final Collection<ShardingSphereRule> rules) {
        Map<String, SchemaPrivileges> result = new HashMap<>(privilege.getDatabasePrivileges().getSpecificPrivileges().size(), 1);
        for (Entry<String, SchemaPrivileges> entry : privilege.getDatabasePrivileges().getSpecificPrivileges().entrySet()) {
            if (!result.containsKey(schemaName)) {
                SchemaPrivileges schemaPrivileges = new SchemaPrivileges(schemaName);
                schemaPrivileges.getGlobalPrivileges().addAll(entry.getValue().getGlobalPrivileges());
                schemaPrivileges.getSpecificPrivileges().putAll(getMergedTablePrivileges(entry.getValue(), rules));
                result.put(schemaName, schemaPrivileges);
            }
        }
        return result;
    }
    
    private static Map<String, TablePrivileges> getMergedTablePrivileges(final SchemaPrivileges privilege, final Collection<ShardingSphereRule> rules) {
        Map<String, TablePrivileges> result = new HashMap<>(privilege.getSpecificPrivileges().size(), 1);
        for (Entry<String, TablePrivileges> entry : privilege.getSpecificPrivileges().entrySet()) {
            Optional<String> logicalTable = getLogicalTable(entry, rules);
            if (logicalTable.isPresent() && !result.containsKey(logicalTable.get())) {
                result.put(logicalTable.get(), new TablePrivileges(logicalTable.get(), entry.getValue().getPrivileges()));
            }
        }
        return result;
    }
    
    private static Optional<String> getLogicalTable(final Entry<String, TablePrivileges> privilege, final Collection<ShardingSphereRule> rules) {
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
