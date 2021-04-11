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

package org.apache.shardingsphere.authority.privilege;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.authority.privilege.admin.AdministrativePrivileges;
import org.apache.shardingsphere.authority.privilege.database.DatabasePrivileges;

import java.util.Collection;

/**
 * ShardingSphere Privileges.
 */
@Getter
@EqualsAndHashCode
public final class ShardingSpherePrivileges {
    
    private final AdministrativePrivileges administrativePrivileges = new AdministrativePrivileges();
    
    private final DatabasePrivileges databasePrivileges = new DatabasePrivileges();
    
    /**
     * Set super privilege.
     */
    public void setSuperPrivilege() {
        administrativePrivileges.getPrivileges().add(PrivilegeType.SUPER);
    }
    
    /**
     * Has privileges.
     *
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final Collection<PrivilegeType> privileges) {
        return administrativePrivileges.hasPrivileges(privileges);
    }
    
    /**
     * Has privileges.
     *
     * @param schema schema
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final String schema, final Collection<PrivilegeType> privileges) {
        return hasPrivileges(privileges) || databasePrivileges.hasPrivileges(schema, privileges);
    }
    
    /**
     * Has privilege for login and use db.
     *
     * @param schema schema
     * @return has or not
     */
    public boolean hasPrivileges(final String schema) {
        return administrativePrivileges.getPrivileges().contains(PrivilegeType.SUPER)
                || !databasePrivileges.getGlobalPrivileges().isEmpty() || databasePrivileges.getSpecificPrivileges().containsKey(schema);
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
        return hasPrivileges(privileges) || databasePrivileges.hasPrivileges(schema, table, privileges);
    }
}
