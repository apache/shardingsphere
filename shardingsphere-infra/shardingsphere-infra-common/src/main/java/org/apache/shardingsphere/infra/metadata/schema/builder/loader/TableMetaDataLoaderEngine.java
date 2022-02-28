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

package org.apache.shardingsphere.infra.metadata.schema.builder.loader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.common.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.spi.singleton.SingletonSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class TableMetaDataLoaderEngine {

    private static final Map<String, DialectTableMetaDataLoader> DIALECT_METADATA_LOADER_MAP = SingletonSPIRegistry.getSingletonInstancesMap(
            DialectTableMetaDataLoader.class, DialectTableMetaDataLoader::getDatabaseType);

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-TableMetaDataLoaderEngine-%d").build());

    /**
     * Load table meta data.
     *
     * @param materials    table meta data load material
     * @param databaseType database type
     * @return table meta data collection
     * @throws SQLException SQL exception
     */
    public static Collection<TableMetaData> load(final Collection<TableMetaDataLoaderMaterial> materials, final DatabaseType databaseType) throws SQLException {
        Optional<DialectTableMetaDataLoader> dialectTableMetaDataLoader = findDialectTableMetaDataLoader(databaseType);
        if (dialectTableMetaDataLoader.isPresent()) {
            try {
                return loadByDialect(dialectTableMetaDataLoader.get(), materials);
            } catch (final SQLException | ShardingSphereException ex) {
                log.error("Dialect load table meta data error", ex);
                return loadByDefault(materials, databaseType);
            }
        }
        return loadByDefault(materials, databaseType);
    }

    private static Collection<TableMetaData> loadByDefault(final Collection<TableMetaDataLoaderMaterial> materials, final DatabaseType databaseType) throws SQLException {
        Collection<TableMetaData> result = new LinkedList<>();
        Collection<Future<SQLException>> futures = new LinkedList<>();
        for (TableMetaDataLoaderMaterial each : materials) {
            List<String> tableNames = (List<String>) each.getTableNames();
            List<List<String>> tableNameList = averageAssign(tableNames, Runtime.getRuntime().availableProcessors() * 2);
            for (int i = 0; i < tableNameList.size(); i++) {
                List<String> subTableNames = tableNameList.get(i);
                DataSource dataSource = each.getDataSource();
                futures.add(EXECUTOR_SERVICE.submit(() -> loadDefaultTable(databaseType, result, subTableNames, dataSource)));
            }
        }
        try {
            for (Future<SQLException> exceptionFuture : futures) {
                if (exceptionFuture != null) {
                    throw exceptionFuture.get();
                }
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result;
    }

    private static SQLException loadDefaultTable(final DatabaseType databaseType, final Collection<TableMetaData> result, final Collection<String> tableNames, final DataSource dataSource) {
        try {
            for (String tableName : tableNames) {
                TableMetaDataLoader.load(dataSource, tableName, databaseType).ifPresent(result::add);
            }
            return null;
        } catch (SQLException e) {
            return e;
        }
    }

    private static Collection<TableMetaData> loadByDialect(final DialectTableMetaDataLoader loader, final Collection<TableMetaDataLoaderMaterial> materials) throws SQLException {
        Collection<TableMetaData> result = new LinkedList<>();
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (TableMetaDataLoaderMaterial each : materials) {
            List<String> tableNames = (List<String>) each.getTableNames();
            List<List<String>> tableNameList = averageAssign(tableNames, Runtime.getRuntime().availableProcessors() * 2);
            for (int i = 0; i < tableNameList.size(); i++) {
                List<String> subTableNames = tableNameList.get(i);
                futures.add(EXECUTOR_SERVICE.submit(() -> loader.load(each.getDataSource(), subTableNames)));
            }
        }
        try {
            for (Future<Map<String, TableMetaData>> each : futures) {
                result.addAll(each.get().values());
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result;
    }

    private static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final DatabaseType databaseType) {
        return Optional.ofNullable(DIALECT_METADATA_LOADER_MAP.get(databaseType.getName()));
    }

    /**
     * Divide a list equally.
     *
     * @param source list
     * @param n number
     * @param <T> Object
     * @return equallyList
     */
    public static <T> List<List<T>> averageAssign(final List<T> source, final int n) {
        List<List<T>> result = new ArrayList<>();
        int remainder = source.size() % n;
        int number = source.size() / n;
        int offset = 0;
        for (int i = 0; i < n; i++) {
            List<T> value = null;
            if (remainder > 0) {
                value = source.subList(i * number + offset, (i + 1) * number + offset + 1);
                remainder--;
                offset++;
            } else {
                value = source.subList(i * number + offset, (i + 1) * number + offset);
            }
            result.add(value);
        }
        return result;
    }

}
