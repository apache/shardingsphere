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

package org.apache.shardingsphere.infra.metadata.auth.builder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.auth.builder.loader.PrivilegeLoader;
import org.apache.shardingsphere.infra.metadata.auth.builder.loader.PrivilegeLoaderEngine;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

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
    
    private static final long FUTURE_GET_TIME_OUT_MILLISECONDS = 5000L;
    
    /**
     * Build privileges.
     *
     * @param metaDataList meta data list
     * @param users users
     * @param props configuration properties
     * @return privileges
     */
    public static Map<ShardingSphereUser, ShardingSpherePrivilege> build(final Collection<ShardingSphereMetaData> metaDataList,
                                                                         final Collection<ShardingSphereUser> users, final ConfigurationProperties props) {
        if (metaDataList.isEmpty()) {
            return createDefaultPrivileges(users);
        }
        Optional<PrivilegeLoader> loader = PrivilegeLoaderEngine.findPrivilegeLoader(metaDataList.iterator().next().getResource().getDatabaseType());
        return loader.map(optional -> build(metaDataList, users, props, optional)).orElseGet(() -> createDefaultPrivileges(users));
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivilege> build(final Collection<ShardingSphereMetaData> metaDataList,
                                                                          final Collection<ShardingSphereUser> users, final ConfigurationProperties props, final PrivilegeLoader loader) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new LinkedHashMap<>();
        for (ShardingSphereMetaData each : metaDataList) {
            result.putAll(build(each, users, props, loader));
        }
        return result;
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivilege> build(final ShardingSphereMetaData metaData, final Collection<ShardingSphereUser> users,
                                                                          final ConfigurationProperties props, final PrivilegeLoader loader) {
        int maxConnectionsSizePerQuery = props.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> result = build(metaData.getResource().getDataSources().values(), users, loader, maxConnectionsSizePerQuery);
        return PrivilegeMerger.merge(result, metaData.getName(), metaData.getRuleMetaData().getRules());
    }
    
    private static Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> build(final Collection<DataSource> dataSources, final Collection<ShardingSphereUser> users,
                                                                                      final PrivilegeLoader loader, final int maxConnectionsSizePerQuery) {
        Map<ShardingSphereUser, Collection<ShardingSpherePrivilege>> result = new LinkedHashMap<>(users.size(), 1);
        for (ShardingSphereUser each : users) {
            result.put(each, load(dataSources, each, loader, maxConnectionsSizePerQuery));
        }
        return result;
    }
    
    private static Collection<ShardingSpherePrivilege> load(final Collection<DataSource> dataSources,
                                                            final ShardingSphereUser user, final PrivilegeLoader loader, final int maxConnectionsSizePerQuery) {
        Collection<ShardingSpherePrivilege> result = new LinkedHashSet<>(dataSources.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CPU_CORES * 2, dataSources.size() * maxConnectionsSizePerQuery));
        Collection<Future<Optional<ShardingSpherePrivilege>>> futures = new LinkedHashSet<>(dataSources.size(), 1);
        for (DataSource each : dataSources) {
            futures.add(executorService.submit(() -> loader.load(user, each)));
        }
        futures.forEach(each -> {
            try {
                each.get(FUTURE_GET_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS).ifPresent(result::add);
            } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IllegalStateException(String.format("Error while loading privilege with %s", each), ex);
            }
        });
        executorService.shutdownNow();
        return result;
    }
    
    private static Map<ShardingSphereUser, ShardingSpherePrivilege> createDefaultPrivileges(final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new LinkedHashMap<>(users.size(), 1);
        ShardingSpherePrivilege privilege = new ShardingSpherePrivilege();
        privilege.setSuperPrivilege();
        users.forEach(each -> result.put(each, privilege));
        return result;
    }
}
