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

package org.apache.shardingsphere.authority.provider.natived.builder;

import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.typed.TypedSPI;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Storage privilege handler.
 */
public interface StoragePrivilegeHandler extends TypedSPI {
    
    /**
     * Differentiate users between storage and exterior.
     *
     * @param users users from exterior
     * @param dataSource target data source
     * @return users non-existing in storage
     * @throws SQLException SQL exception
     */
    Collection<ShardingSphereUser> diff(Collection<ShardingSphereUser> users, DataSource dataSource) throws SQLException;
    
    /**
     * Create users in storage.
     *
     * @param users users to be created
     * @param dataSource target data source
     * @throws SQLException SQL exception
     */
    void create(Collection<ShardingSphereUser> users, DataSource dataSource) throws SQLException;
    
    /**
     * Grant all privileges to users.
     *
     * @param users users to be granted
     * @param dataSource target data source
     * @throws SQLException SQL exception
     */
    void grantAll(Collection<ShardingSphereUser> users, DataSource dataSource) throws SQLException;
    
    /**
     * Load privileges from storage.
     *
     * @param users users to be loaded
     * @param dataSource target data source
     * @return map of user and  privilege
     * @throws SQLException SQL exception
     */
    Map<ShardingSphereUser, NativePrivileges> load(Collection<ShardingSphereUser> users, DataSource dataSource) throws SQLException;
}
