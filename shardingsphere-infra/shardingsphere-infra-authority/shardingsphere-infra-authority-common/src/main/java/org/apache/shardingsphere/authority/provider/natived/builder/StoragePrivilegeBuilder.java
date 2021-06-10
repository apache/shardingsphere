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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRecognizer;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Storage privilege builder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StoragePrivilegeBuilder {
    
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    private static final long FUTURE_GET_TIME_OUT_MILLISECONDS = 5000L;
    
    static {
        ShardingSphereServiceLoader.register(StoragePrivilegeHandler.class);
    }
    
    /**
     * Build privileges.
     *
     * @param metaDataList meta data list
     * @param users users
     * @return privileges
     */
    public static Map<ShardingSphereUser, NativePrivileges> build(final Collection<ShardingSphereMetaData> metaDataList, final Collection<ShardingSphereUser> users) {
        return metaDataList.isEmpty() ? buildPrivilegesInCache(users) : buildPrivilegesInStorage(metaDataList, users);
    }
    
    private static Map<ShardingSphereUser, NativePrivileges> buildPrivilegesInCache(final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, NativePrivileges> result = new LinkedHashMap<>(users.size(), 1);
        NativePrivileges privileges = new NativePrivileges();
        privileges.setSuperPrivilege();
        users.forEach(each -> result.put(each, privileges));
        return result;
    }
    
    private static Map<ShardingSphereUser, NativePrivileges> buildPrivilegesInStorage(final Collection<ShardingSphereMetaData> metaDataList, final Collection<ShardingSphereUser> users) {
        Map<ShardingSphereUser, NativePrivileges> result = new LinkedHashMap<>(users.size(), 1);
        metaDataList.stream().map(each -> buildPrivilegesInStorage(each, users)).forEach(result::putAll);
        return result;
    }
    
    private static Map<ShardingSphereUser, NativePrivileges> buildPrivilegesInStorage(final ShardingSphereMetaData metaData, final Collection<ShardingSphereUser> users) {
        DatabaseType databaseType = DatabaseTypeRecognizer.getDatabaseType(metaData.getResource().getAllInstanceDataSources());
        Optional<StoragePrivilegeHandler> handler = TypedSPIRegistry.findRegisteredService(StoragePrivilegeHandler.class, databaseType.getName(), new Properties());
        if (!handler.isPresent()) {
            return buildPrivilegesInCache(users);
        }
        save(metaData.getResource().getAllInstanceDataSources(), users, handler.get());
        Map<ShardingSphereUser, Collection<NativePrivileges>> result = load(metaData.getResource().getAllInstanceDataSources(), users, handler.get());
        checkConsistent(result);
        return StoragePrivilegeMerger.merge(result, metaData.getName(), metaData.getRuleMetaData().getRules());
    }
    
    private static void save(final Collection<DataSource> dataSources,
                             final Collection<ShardingSphereUser> users, final StoragePrivilegeHandler handler) {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CPU_CORES * 2, dataSources.isEmpty() ? 1 : dataSources.size()));
        Collection<Future> tasks = new HashSet<>();
        for (DataSource each : dataSources) {
            tasks.add(executorService.submit(() -> save(each, users, handler)));
        }
        tasks.forEach(each -> {
            try {
                each.get(FUTURE_GET_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IllegalStateException(String.format("Error while loading privilege with %s", each), ex);
            }
        });
        executorService.shutdownNow();
    }
    
    private static void save(final DataSource dataSource, final Collection<ShardingSphereUser> users, final StoragePrivilegeHandler handler) {
        try {
            Collection<ShardingSphereUser> noneExisted = handler.diff(users, dataSource);
            if (!noneExisted.isEmpty()) {
                handler.create(noneExisted, dataSource);
                handler.grantAll(noneExisted, dataSource);
            }
        } catch (final SQLException ex) {
            throw new ShardingSphereException(ex);
        }
    }
    
    private static Map<ShardingSphereUser, Collection<NativePrivileges>> load(final Collection<DataSource> dataSources, 
                                                                              final Collection<ShardingSphereUser> users, final StoragePrivilegeHandler handler) {
        Map<ShardingSphereUser, Collection<NativePrivileges>> result = new LinkedHashMap<>(users.size(), 1);
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(CPU_CORES * 2, dataSources.isEmpty() ? 1 : dataSources.size()));
        Collection<Future<Map<ShardingSphereUser, NativePrivileges>>> futures = new HashSet<>(dataSources.size(), 1);
        for (DataSource each : dataSources) {
            futures.add(executorService.submit(() -> handler.load(users, each)));
        }
        futures.forEach(each -> {
            try {
                fillPrivileges(result, each);
            } catch (final InterruptedException | ExecutionException | TimeoutException ex) {
                throw new IllegalStateException(String.format("Error while loading privilege with %s", each), ex);
            }
        });
        executorService.shutdownNow();
        return result;
    }
    
    private static void fillPrivileges(final Map<ShardingSphereUser, Collection<NativePrivileges>> userPrivilegeMap,
                                       final Future<Map<ShardingSphereUser, NativePrivileges>> future) throws InterruptedException, ExecutionException, TimeoutException {
        for (Entry<ShardingSphereUser, NativePrivileges> entry : future.get(FUTURE_GET_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS).entrySet()) {
            if (!userPrivilegeMap.containsKey(entry.getKey())) {
                userPrivilegeMap.put(entry.getKey(), new LinkedHashSet<>());
            }
            userPrivilegeMap.get(entry.getKey()).add(entry.getValue());
        }
    }
    
    private static void checkConsistent(final Map<ShardingSphereUser, Collection<NativePrivileges>> userPrivilegeMap) {
        userPrivilegeMap.forEach(StoragePrivilegeBuilder::checkConsistent);
    }
    
    private static void checkConsistent(final ShardingSphereUser user, final Collection<NativePrivileges> privileges) {
        NativePrivileges sample = privileges.iterator().next();
        Preconditions.checkState(
                privileges.stream().allMatch(each -> each.equals(sample)), "Different physical instances have different privileges for user %s", user.getGrantee().toString().replaceAll("%", "%%"));
    }
}
