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

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.data.DataPrivilege;

/**
 * ShardingSphere privilege.
 */
@Getter
public final class ShardingSpherePrivilege {
    
    private final AdministrationPrivilege administrationPrivilege = new AdministrationPrivilege();
    
    private final DataPrivilege dataPrivilege = new DataPrivilege();
    
    /**
     * Set super privilege.
     */
    public void setSuperPrivilege() {
        administrationPrivilege.setSuperPrivilege();
        dataPrivilege.setSuperPrivilege();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ShardingSpherePrivilege)) {
            return false;
        }
        if (!administrationPrivilege.getPrivileges().equals(((ShardingSpherePrivilege) obj).administrationPrivilege.getPrivileges())) {
            return false;
        }
        if (!dataPrivilege.equals(((ShardingSpherePrivilege) obj).dataPrivilege)) {
            return false;
        }
        return true;
    }
}
