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

package org.apache.shardingsphere.infra.auth.privilege;

import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Instance privilege.
 */
@Getter
public final class InstancePrivilege {
    
    private final Collection<PrivilegeType> privileges = new LinkedHashSet<>();
    
    /**
     * Has privileges.
     *
     * @param privileges privileges
     * @return has privileges or not
     */
    public boolean hasPrivileges(final Collection<PrivilegeType> privileges) {
        if (this.privileges.contains(PrivilegeType.ALL)) {
            return true;
        }
        return this.privileges.containsAll(privileges);
    }
}
