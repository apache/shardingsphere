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

package org.apache.shardingsphere.mode.repository.cluster.nacos.lock;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryException;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;
import org.apache.shardingsphere.mode.repository.cluster.nacos.constant.JdbcConstants;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Nacos internal lock.
 */
@Slf4j
public class NacosInternalLock implements InternalLock {
    
    private final DataSource dataSource;
    
    private final ThreadLocal<Connection> connectionThreadLocal;
    
    private final Map<Thread, AtomicInteger> threadCount = new ConcurrentHashMap<>();
    
    private final String lockName;
    
    public NacosInternalLock(final String lockName, final DataSource dataSource) {
        this.lockName = lockName;
        this.dataSource = dataSource;
        connectionThreadLocal = ThreadLocal.withInitial(() -> {
            try {
                return this.dataSource.getConnection();
                // CHECKSTYLE:OFF
            } catch (Exception cause) {
                // CHECKSTYLE:ON
                throw new ClusterPersistRepositoryException(cause);
            }
        });
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        try {
            return acquire(timeoutMillis, TimeUnit.MILLISECONDS);
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    @Override
    public void unlock() {
        try {
            release();
            // CHECKSTYLE:OFF
        } catch (Exception cause) {
            // CHECKSTYLE:ON
            throw new ClusterPersistRepositoryException(cause);
        }
    }
    
    private boolean acquire(final long time, final TimeUnit unit) throws SQLException, InterruptedException {
        Long millisToWait = (unit != null) ? unit.toMillis(time) : null;
        long startMillis = System.currentTimeMillis();
        boolean haveTheLock = false;
        Thread currentThread = Thread.currentThread();
        AtomicInteger count = threadCount.get(currentThread);
        if (count != null) {
            count.incrementAndGet();
            return true;
        }
        Connection conn = connectionThreadLocal.get();
        while (!haveTheLock) {
            try (PreparedStatement ps = conn.prepareStatement(JdbcConstants.INSERT_NACOS_INTERNAL_LOCK)) {
                ps.setString(1, lockName);
                int updateCount = ps.executeUpdate();
                if (updateCount == 1) {
                    AtomicInteger newThreadCount = new AtomicInteger(1);
                    threadCount.put(currentThread, newThreadCount);
                    haveTheLock = true;
                }
            } catch (SQLException cause) {
                log.info("Failed to get lock: {}, msg: {}", lockName, cause.getMessage());
            } finally {
                long interval = 1000L;
                if (Objects.nonNull(millisToWait)) {
                    interval = millisToWait / 10L;
                }
                Thread.sleep(interval);
                if (millisToWait != null) {
                    millisToWait -= System.currentTimeMillis() - startMillis;
                    startMillis = System.currentTimeMillis();
                    if (millisToWait <= 0) {
                        break;
                    }
                }
            }
        }
        connectionThreadLocal.remove();
        conn.close();
        return haveTheLock;
    }
    
    private void release() throws Exception {
        Thread currentThread = Thread.currentThread();
        AtomicInteger count = threadCount.get(currentThread);
        if (count == null) {
            throw new IllegalStateException("You do not own the lock: " + lockName);
        }
        count.decrementAndGet();
        if (count.get() > 0) {
            return;
        }
        if (count.get() < 0) {
            throw new IllegalStateException("Lock count has gone negative for lock: " + lockName);
        }
        Connection conn = connectionThreadLocal.get();
        try (PreparedStatement ps = conn.prepareStatement(JdbcConstants.DELETE_NACOS_INTERNAL_LOCK)) {
            ps.setString(1, lockName);
            ps.executeUpdate();
        }
        threadCount.remove(currentThread);
        connectionThreadLocal.remove();
        conn.close();
    }
}
