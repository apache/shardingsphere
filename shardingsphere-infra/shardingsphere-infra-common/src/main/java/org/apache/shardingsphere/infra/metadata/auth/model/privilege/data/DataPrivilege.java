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
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.PrivilegeType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data privilege.
 */
@Getter
public final class DataPrivilege {
    
    private final Collection<PrivilegeType> globalPrivileges = new LinkedHashSet<>();
    
    private final Map<String, SchemaPrivilege> specificPrivileges = new LinkedHashMap<>();
    
    /**
     * Has privileges.
     *
     * @param schema schema
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final String schema, final Collection<PrivilegeType> privileges) {
        return hasGlobalPrivileges(privileges) || hasSpecificPrivileges(schema, privileges);
    }
    
    /**
     * Has privileges.
     *
     * @param schema schema
     * @param table table
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final String schema, final String table, final Collection<PrivilegeType> privileges) {
        return hasGlobalPrivileges(privileges) || hasSpecificPrivileges(schema, table, privileges);
    }
    
    private boolean hasGlobalPrivileges(final Collection<PrivilegeType> privileges) {
        return globalPrivileges.contains(PrivilegeType.SUPER) || !globalPrivileges.isEmpty() && globalPrivileges.containsAll(privileges);
    }
    
    private boolean hasSpecificPrivileges(final String schema, final Collection<PrivilegeType> privileges) {
        Collection<PrivilegeType> targets = privileges.stream().filter(each -> !globalPrivileges.contains(each)).collect(Collectors.toList());
        return specificPrivileges.containsKey(schema) && specificPrivileges.get(schema).hasPrivileges(targets);
    }
    
    private boolean hasSpecificPrivileges(final String schema, final String table, final Collection<PrivilegeType> privileges) {
        Collection<PrivilegeType> targets = privileges.stream().filter(each -> !globalPrivileges.contains(each)).collect(Collectors.toList());
        return specificPrivileges.containsKey(schema) && specificPrivileges.get(schema).hasPrivileges(table, targets);
    }
    
    /**
     * Set super privilege.
     */
    public void setSuperPrivilege() {
        globalPrivileges.add(PrivilegeType.SUPER);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DataPrivilege)) {
            return false;
        }
        if (!globalPrivileges.equals(((DataPrivilege) obj).getGlobalPrivileges())) {
            return false;
        }
        if (!specificPrivileges.equals(((DataPrivilege) obj).specificPrivileges)) {
            return false;
        }
        return true;
    }
}
