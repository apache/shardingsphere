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

package org.apache.shardingsphere.authority.provider.fixture;

import org.apache.shardingsphere.authority.provider.natived.builder.StoragePrivilegeHandler;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public final class FixtureStoragePrivilegeHandler implements StoragePrivilegeHandler {
    
    @Override
    public Collection<ShardingSphereUser> diff(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        return null;
    }
    
    @Override
    public void create(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
    }
    
    @Override
    public void grantAll(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
    }
    
    @Override
    public Map<ShardingSphereUser, NativePrivileges> load(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        return null;
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
