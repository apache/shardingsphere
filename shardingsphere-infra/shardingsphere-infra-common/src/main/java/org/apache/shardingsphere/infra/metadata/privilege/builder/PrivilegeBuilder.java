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

package org.apache.shardingsphere.infra.metadata.privilege.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.privilege.loader.PrivilegeLoader;
import org.apache.shardingsphere.infra.metadata.privilege.loader.PrivilegeLoaderEngine;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Privilege builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrivilegeBuilder {
    
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    private static final int FUTURE_GET_TIME_OUT_SECOND = 5;
    
    /**
     * Build privileges.
     *
     * @param metaDatas metadatas
     * @param users users
     * @param props props
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivilege> build(final Collection<ShardingSphereMetaData> metaDatas,
                                                                         final Collection<ShardingSphereUser> users, final ConfigurationProperties props) {
        Optional<PrivilegeLoader> loader = PrivilegeLoaderEngine.getPrivilegeLoader();
        if (!loader.isPresent()) {
            return getDefaultShardingSpherePrivileges(users);
        }
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new LinkedHashMap<>();
        for (ShardingSphereMetaData each : metaDatas) {
            result.putAll(build0(each, users, loader.get(), props));
        }
        return result;
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivilege> getDefaultShardingSpherePrivileges(final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new LinkedHashMap<>();
        ShardingSpherePrivilege privilege = new ShardingSpherePrivilege();
        privilege.setSuper();
        users.forEach(each -> result.put(each, privilege));
        return result;
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivilege> build0(final ShardingSphereMetaData metaData, final Collection<ShardingSphereUser> users,
                                                                                final PrivilegeLoader loader, final ConfigurationProperties props) {
        Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> result =
                build0(metaData.getResource().getDataSources(), users, loader, props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY));
        return PrivilegeMerger.merge(result, metaData.getName(), metaData.getRuleMetaData().getRules());
    }
    
    private static Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> build0(final Map<String, DataSource> dataSources, final Collection<ShardingSphereUser> users,
                                                                   final PrivilegeLoader loader, final int maxConnectionsSizePerQuery) {
        Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> result = new LinkedHashMap<>();
        for (ShardingSphereUser each : users) {
            Collection<ShardingSpherePrivilege> privileges = parallelLoadPrivileges(dataSources, each, loader, maxConnectionsSizePerQuery);
            result.put(each, privileges);
        }
        return result;
    }
    
    private static Collection<ShardingSpherePrivilege> parallelLoadPrivileges(final Map<String, DataSource> dataSources,
                                                                       final ShardingSphereUser user, final PrivilegeLoader loader, final int maxConnectionsSizePerQuery) {
        Collection<ShardingSpherePrivilege> result = new LinkedHashSet<>(dataSources.size(), 1);
        Collection<Future<Optional<ShardingSpherePrivilege>>> futures = new LinkedHashSet<>(dataSources.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CPU_CORES * 2, dataSources.size() * maxConnectionsSizePerQuery));
        for (DataSource each : dataSources.values()) {
            futures.add(executorService.submit(() -> loader.load(user, each)));
        }
        futures.forEach(each -> {
            try {
                each.get(FUTURE_GET_TIME_OUT_SECOND, TimeUnit.SECONDS).ifPresent(result::add);
            } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IllegalStateException(String.format("Error while fetching privilege with %s", each), ex);
            }
        });
        executorService.shutdownNow();
        return result;
    }
}
