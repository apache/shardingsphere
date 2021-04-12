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

package org.apache.shardingsphere.authority.provider.natived.model.privilege.admin;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.shardingsphere.authority.model.PrivilegeType;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Administrative privileges.
 */
@Getter
@EqualsAndHashCode
public final class AdministrativePrivileges {
    
    private final Collection<PrivilegeType> privileges = new CopyOnWriteArraySet<>();
    
    /**
     * Has privileges.
     *
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final Collection<PrivilegeType> privileges) {
        return this.privileges.contains(PrivilegeType.SUPER) || this.privileges.containsAll(privileges);
    }
}
