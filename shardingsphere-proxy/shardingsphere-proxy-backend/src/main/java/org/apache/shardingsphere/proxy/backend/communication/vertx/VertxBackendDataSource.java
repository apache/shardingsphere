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
import com.zaxxer.hikari.HikariDataSource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import lombok.Getter;
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
public final class VertxBackendDataSource implements BackendDataSource {
    
    private static final VertxBackendDataSource INSTANCE = new VertxBackendDataSource();
    
    private final Map<String, Map<String, Pool>> schemaVertxPools = new ConcurrentHashMap<>();
    
    @Getter
    private final Vertx vertx;
    
    private VertxBackendDataSource() {
        vertx = Vertx.vertx(new VertxOptions().setPreferNativeTransport(true).setEventLoopPoolSize(Runtime.getRuntime().availableProcessors()));
    }
    
    /**
     * Get instance of VertxBackendDataSource.
     *
     * @return instance of VertxBackendDataSource
     */
    public static VertxBackendDataSource getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get Vert.x sql connection future.
     *
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @param connectionSize connection size
     * @return futures of sql connections
     */
    public List<Future<SqlConnection>> getConnections(final String schemaName, final String dataSourceName, final int connectionSize) {
        Pool pool = getPool(schemaName, dataSourceName);
        List<Future<SqlConnection>> result = new ArrayList<>(connectionSize);
        for (int i = 0; i < connectionSize; i++) {
            result.add(pool.getConnection());
        }
        return result;
    }
    
    /**
     * Get Vert.x pool.
     *
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @return Vert.x pool
     */
    public Pool getPool(final String schemaName, final String dataSourceName) {
        Map<String, Pool> vertxPools = schemaVertxPools.get(schemaName);
        if (null == vertxPools) {
            vertxPools = schemaVertxPools.computeIfAbsent(schemaName, unused -> new ConcurrentHashMap<>());
        }
        Pool result = vertxPools.get(dataSourceName);
        if (null == result) {
            result = vertxPools.computeIfAbsent(dataSourceName, unused -> createPoolFromSchemaDataSource(schemaName, dataSourceName));
        }
        return result;
    }
    
    private Pool createPoolFromSchemaDataSource(final String schemaName, final String dataSourceName) {
        DataSource dataSource = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getResource().getDataSources().get(dataSourceName);
        Preconditions.checkNotNull(dataSource, "Can not get connection from datasource %s.", dataSourceName);
        HikariDataSource value = (HikariDataSource) dataSource;
        URI uri = URI.create(value.getJdbcUrl().replace("jdbc:", ""));
        switch (uri.getScheme()) {
            case "mysql":
                return createMySQLPool(value, uri);
            case "postgresql":
                throw new UnsupportedOperationException("For now");
            case "opengauss":
                throw new UnsupportedOperationException("For now");
            default:
                throw new UnsupportedOperationException("Database " + uri.getScheme() + " unsupported");
        }
    }
    
    private MySQLPool createMySQLPool(final HikariDataSource value, final URI uri) {
        MySQLConnectOptions options = new MySQLConnectOptions().setHost(uri.getHost()).setPort(uri.getPort()).setDatabase(uri.getPath().replace("/", ""))
                .setUser(value.getUsername()).setPassword(value.getPassword()).setCachePreparedStatements(true).setPreparedStatementCacheMaxSize(16384);
        PoolOptions poolOptions = new PoolOptions().setMaxSize(value.getMaximumPoolSize()).setIdleTimeout((int) value.getIdleTimeout()).setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
                .setConnectionTimeout((int) value.getConnectionTimeout()).setConnectionTimeoutUnit(TimeUnit.MILLISECONDS);
        return MySQLPool.pool(vertx, options, poolOptions);
    }
}
