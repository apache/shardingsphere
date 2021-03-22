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

package org.apache.shardingsphere.infra.metadata.auth.model.privilege;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.admin.AdministrativePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.database.DatabasePrivilege;

import java.util.Collection;

/**
 * ShardingSphere privilege.
 */
@Getter
@EqualsAndHashCode
public final class ShardingSpherePrivilege {
    
    private final AdministrativePrivilege administrativePrivilege = new AdministrativePrivilege();
    
    private final DatabasePrivilege databasePrivilege = new DatabasePrivilege();
    
    /**
     * Set super privilege.
     */
    public void setSuperPrivilege() {
        administrativePrivilege.getPrivileges().add(PrivilegeType.SUPER);
    }
    
    /**
     * Is empty.
     *
     * @return is empty or not
     */
    public boolean isEmpty() {
        return administrativePrivilege.getPrivileges().isEmpty()
                && databasePrivilege.getGlobalPrivileges().isEmpty() && databasePrivilege.getSpecificPrivileges().isEmpty();
    }
    
    /**
     * Has privileges.
     *
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final Collection<PrivilegeType> privileges) {
        return administrativePrivilege.hasPrivileges(privileges);
    }
    
    /**
     * Has privileges.
     *
     * @param schema schema
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final String schema, final Collection<PrivilegeType> privileges) {
        return hasPrivileges(privileges) || databasePrivilege.hasPrivileges(schema, privileges);
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
        return hasPrivileges(privileges) || databasePrivilege.hasPrivileges(schema, table, privileges);
    }
}
