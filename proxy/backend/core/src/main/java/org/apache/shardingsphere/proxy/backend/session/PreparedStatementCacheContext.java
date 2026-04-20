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

package org.apache.shardingsphere.proxy.backend.session;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepared statement cache context.
 */
public final class PreparedStatementCacheContext {
    
    private static final int DEFAULT_MAX_SIZE = 128;
    
    @Getter
    private final int maxSize;
    
    private final Map<CacheKey, PreparedStatement> cachedPreparedStatements = new LinkedHashMap<>(16, 0.75F, true);
    
    public PreparedStatementCacheContext() {
        this(DEFAULT_MAX_SIZE);
    }
    
    public PreparedStatementCacheContext(final int maxSize) {
        this.maxSize = Math.max(1, maxSize);
    }
    
    /**
     * Get or create prepared statement.
     *
     * @param connection connection
     * @param sql SQL
     * @param returnGeneratedKeys return generated keys flag
     * @param supplier prepared statement supplier
     * @return prepared statement
     * @throws SQLException SQL exception
     */
    public synchronized PreparedStatement getOrCreate(final Connection connection, final String sql, final boolean returnGeneratedKeys,
                                                      final PreparedStatementSupplier supplier) throws SQLException {
        CacheKey cacheKey = new CacheKey(connection, sql, returnGeneratedKeys);
        PreparedStatement cachedPreparedStatement = cachedPreparedStatements.get(cacheKey);
        if (null != cachedPreparedStatement && !isClosed(cachedPreparedStatement)) {
            return cachedPreparedStatement;
        }
        if (null != cachedPreparedStatement) {
            cachedPreparedStatements.remove(cacheKey);
            closeQuietly(cachedPreparedStatement);
        }
        PreparedStatement result = supplier.get();
        cachedPreparedStatements.put(cacheKey, result);
        evictIfNecessary();
        return result;
    }
    
    /**
     * Check whether statement is cached.
     *
     * @param statement statement
     * @return cached or not
     */
    public synchronized boolean contains(final Statement statement) {
        return cachedPreparedStatements.containsValue(statement);
    }
    
    /**
     * Invalidate cached statement.
     *
     * @param statement statement
     */
    public synchronized void invalidate(final Statement statement) {
        Iterator<Entry<CacheKey, PreparedStatement>> iterator = cachedPreparedStatements.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<CacheKey, PreparedStatement> entry = iterator.next();
            if (entry.getValue() == statement) {
                iterator.remove();
                closeQuietly(entry.getValue());
                break;
            }
        }
    }
    
    /**
     * Close all cached statements.
     */
    public synchronized void closeAll() {
        for (PreparedStatement each : cachedPreparedStatements.values()) {
            closeQuietly(each);
        }
        cachedPreparedStatements.clear();
    }
    
    /**
     * Get cache size.
     *
     * @return cache size
     */
    public synchronized int size() {
        return cachedPreparedStatements.size();
    }
    
    private void evictIfNecessary() {
        while (cachedPreparedStatements.size() > maxSize) {
            Iterator<Entry<CacheKey, PreparedStatement>> iterator = cachedPreparedStatements.entrySet().iterator();
            if (!iterator.hasNext()) {
                break;
            }
            Entry<CacheKey, PreparedStatement> eldest = iterator.next();
            iterator.remove();
            closeQuietly(eldest.getValue());
        }
    }
    
    private boolean isClosed(final PreparedStatement preparedStatement) {
        try {
            return preparedStatement.isClosed();
        } catch (final SQLException ignored) {
            return true;
        }
    }
    
    private void closeQuietly(final PreparedStatement preparedStatement) {
        try {
            preparedStatement.close();
        } catch (final SQLException ignored) {
        }
    }
    
    @FunctionalInterface
    public interface PreparedStatementSupplier {
        
        /**
         * Get prepared statement.
         *
         * @return prepared statement
         * @throws SQLException SQL exception
         */
        PreparedStatement get() throws SQLException;
    }
    
    private static final class CacheKey {
        
        private final Connection connection;
        
        private final String sql;
        
        private final boolean returnGeneratedKeys;
        
        private final int connectionIdentityHash;
        
        private CacheKey(final Connection connection, final String sql, final boolean returnGeneratedKeys) {
            this.connection = connection;
            this.sql = sql;
            this.returnGeneratedKeys = returnGeneratedKeys;
            connectionIdentityHash = System.identityHashCode(connection);
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof CacheKey)) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return connection == other.connection && returnGeneratedKeys == other.returnGeneratedKeys && sql.equals(other.sql);
        }
        
        @Override
        public int hashCode() {
            int result = connectionIdentityHash;
            result = 31 * result + sql.hashCode();
            result = 31 * result + Boolean.hashCode(returnGeneratedKeys);
            return result;
        }
    }
}
