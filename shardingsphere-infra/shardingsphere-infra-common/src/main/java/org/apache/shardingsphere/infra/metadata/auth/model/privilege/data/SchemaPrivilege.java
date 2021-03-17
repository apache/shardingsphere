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

package org.apache.shardingsphere.infra.metadata.auth.model.privilege.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.PrivilegeType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Schema privilege.
 */
@RequiredArgsConstructor
@Getter
public final class SchemaPrivilege {
    
    private final String name;
    
    private final Collection<PrivilegeType> globalPrivileges = new LinkedHashSet<>();
    
    private final Map<String, TablePrivilege> specificPrivileges = new LinkedHashMap<>();
    
    /**
     * Has privileges.
     *
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final Collection<PrivilegeType> privileges) {
        return globalPrivileges.containsAll(privileges);
    }
    
    /**
     * Has privileges.
     *
     * @param table table
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final String table, final Collection<PrivilegeType> privileges) {
        return hasGlobalPrivileges(privileges) || hasSpecificPrivileges(table, privileges);
    }
    
    private boolean hasGlobalPrivileges(final Collection<PrivilegeType> privileges) {
        return !globalPrivileges.isEmpty() && globalPrivileges.containsAll(privileges);
    }
    
    private boolean hasSpecificPrivileges(final String table, final Collection<PrivilegeType> privileges) {
        Collection<PrivilegeType> targets = privileges.stream().filter(each -> !globalPrivileges.contains(each)).collect(Collectors.toList());
        return specificPrivileges.containsKey(table) && specificPrivileges.get(table).hasPrivileges(targets);
    }
    
    /**
     * Set super privilege.
     */
    public void setSuperPrivilege() {
        globalPrivileges.add(PrivilegeType.SUPER);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SchemaPrivilege)) {
            return false;
        }
        if (!name.equals(((SchemaPrivilege) obj).name)) {
            return false;
        }
        if (!globalPrivileges.equals(((SchemaPrivilege) obj).globalPrivileges)) {
            return false;
        }
        if (!specificPrivileges.equals(((SchemaPrivilege) obj).specificPrivileges)) {
            return false;
        }
        return true;
    }
}
