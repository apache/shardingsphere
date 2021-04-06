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

package org.apache.shardingsphere.authority.loader;

import org.apache.shardingsphere.authority.spi.PrivilegeLoadAlgorithm;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.authority.loader.builder.PrivilegeBuilder;
import org.apache.shardingsphere.authority.loader.builder.loader.PrivilegeLoader;
import org.apache.shardingsphere.authority.loader.builder.loader.PrivilegeLoaderEngine;
import org.apache.shardingsphere.authority.model.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Storage privilege load algorithm.
 */
public final class StoragePrivilegeLoadAlgorithm implements PrivilegeLoadAlgorithm {
    
    @Override
    public Map<ShardingSphereUser, ShardingSpherePrivilege> load(final String schemaName, final DatabaseType databaseType, final Collection<DataSource> dataSources,
                                                                 final Collection<ShardingSphereRule> rules, final Collection<ShardingSphereUser> users) {
        Optional<PrivilegeLoader> loader = PrivilegeLoaderEngine.findPrivilegeLoader(databaseType);
        return loader.map(
            optional -> PrivilegeBuilder.build(schemaName, groupDataSourcesByInstance(dataSources), rules, users, optional)).orElseGet(() -> PrivilegeBuilder.buildDefaultPrivileges(users));
    }
    
    private Collection<DataSource> groupDataSourcesByInstance(final Collection<DataSource> dataSources) {
        // TODO recognize and merge data sources from same instance
        return dataSources;
    }
    
    @Override
    public String getType() {
        return "STORAGE";
    }
}
