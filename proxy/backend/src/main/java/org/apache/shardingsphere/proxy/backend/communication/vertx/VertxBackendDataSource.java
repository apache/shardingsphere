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

package org.apache.shardingsphere.proxy.backend.communication.vertx;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zaxxer.hikari.HikariDataSource;
import io.netty.util.NettyRuntime;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.proxy.backend.communication.BackendDataSource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import javax.sql.DataSource;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Vert.x backend data source.
 */
@Slf4j
public final class VertxBackendDataSource implements BackendDataSource {
    
    private static volatile VertxBackendDataSource instance;
    
    private final Map<String, Map<String, Pool>> schemaVertxPools = new ConcurrentHashMap<>();
    
    @Getter
    private final Vertx vertx;
    
    private VertxBackendDataSource() {
        vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(determineEventLoopPoolSize()));
    }
    
    private int determineEventLoopPoolSize() {
        return Math.min(CpuCoreSensor.availableProcessors(), NettyRuntime.availableProcessors());
    }
    
    /**
     * Get instance of VertxBackendDataSource.
     *
     * @return instance of VertxBackendDataSource
     */
    public static VertxBackendDataSource getInstance() {
        if (null == instance) {
            synchronized (VertxBackendDataSource.class) {
                if (null == instance) {
                    logWarningBanner();
                    instance = new VertxBackendDataSource();
                }
            }
        }
        return instance;
    }
    
    private static void logWarningBanner() {
        log.warn("\n██     ██  █████  ██████  ███    ██ ██ ███    ██  ██████  \n"
                + "██     ██ ██   ██ ██   ██ ████   ██ ██ ████   ██ ██       \n"
                + "██  █  ██ ███████ ██████  ██ ██  ██ ██ ██ ██  ██ ██   ███ \n"
                + "██ ███ ██ ██   ██ ██   ██ ██  ██ ██ ██ ██  ██ ██ ██    ██ \n"
                + " ███ ███  ██   ██ ██   ██ ██   ████ ██ ██   ████  ██████  \n"
                + "\n       Experimental reactive backend enabled!\n");
    }
    
    /**
     * Get Vert.x sql connection future.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @param connectionSize connection size
     * @return futures of sql connections
     */
    public List<Future<SqlConnection>> getConnections(final String databaseName, final String dataSourceName, final int connectionSize) {
        Pool pool = getPool(databaseName, dataSourceName);
        List<Future<SqlConnection>> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            result.add(pool.getConnection());
        }
        return result;
    }
    
    /**
     * Get Vert.x pool.
     *
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return Vert.x pool
     */
    public Pool getPool(final String databaseName, final String dataSourceName) {
        Map<String, Pool> vertxPools = schemaVertxPools.get(databaseName);
        if (null == vertxPools) {
            vertxPools = schemaVertxPools.computeIfAbsent(databaseName, unused -> new ConcurrentHashMap<>());
        }
        Pool result = vertxPools.get(dataSourceName);
        if (null == result) {
            result = vertxPools.computeIfAbsent(dataSourceName, unused -> createPoolFromSchemaDataSource(databaseName, dataSourceName));
        }
        return result;
    }
    
    private Pool createPoolFromSchemaDataSource(final String databaseName, final String dataSourceName) {
        DataSource dataSource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getDataSources().get(dataSourceName);
        Preconditions.checkNotNull(dataSource, "Can not get connection from datasource %s.", dataSourceName);
        HikariDataSource value = (HikariDataSource) dataSource;
        URI uri = URI.create(value.getJdbcUrl().replace("jdbc:", ""));
        switch (uri.getScheme()) {
            case "mysql":
                return createMySQLPool(value, uri);
            case "postgresql":
            case "opengauss":
                throw new UnsupportedSQLOperationException("For now");
            default:
                throw new UnsupportedSQLOperationException("Database " + uri.getScheme() + " unsupported");
        }
    }
    
    private MySQLPool createMySQLPool(final HikariDataSource value, final URI uri) {
        MySQLConnectOptions options = new MySQLConnectOptions().setHost(uri.getHost()).setPort(uri.getPort()).setDatabase(uri.getPath().replace("/", ""))
                .setUser(value.getUsername()).setCachePreparedStatements(true).setPreparedStatementCacheMaxSize(16384);
        if (!Strings.isNullOrEmpty(value.getPassword())) {
            options = options.setPassword(value.getPassword());
        }
        PoolOptions poolOptions = new PoolOptions().setMaxSize(value.getMaximumPoolSize()).setIdleTimeout((int) value.getIdleTimeout()).setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                .setConnectionTimeout((int) value.getConnectionTimeout()).setConnectionTimeoutUnit(TimeUnit.MILLISECONDS);
        return MySQLPool.pool(vertx, options, poolOptions);
    }
}
