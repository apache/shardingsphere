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

import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;
import org.apache.shardingsphere.mode.repository.cluster.nacos.constant.JdbcConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DistributedLockTest {
    
    private static NacosInternalLockHolder nacosInternalLockHolder;
    
    @SneakyThrows
    @BeforeClass
    public static void initInternalLockHolder() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSource.setUsername("sa");
        dataSource.setMaximumPoolSize(10);
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute(JdbcConstants.CREATE_TABLE);
        }
        nacosInternalLockHolder = new NacosInternalLockHolder(dataSource);
    }
    
    @SneakyThrows
    @Test
    public void assertGetLockConcurrently() {
        String lockKey = "/concurrentLock";
        int threadCount = 3;
        int timeoutMillis = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        for (int count = threadCount; count > 0; count--) {
            new Thread(() -> {
                try {
                    InternalLock lock = nacosInternalLockHolder.getInternalLock(lockKey);
                    if (lock.tryLock(timeoutMillis)) {
                        success.getAndIncrement();
                        lock.unlock();
                    } else {
                        fail.getAndIncrement();
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertThat(success.get() + fail.get(), is(threadCount));
    }
    
    @Test
    public void assertReentrantLock() {
        String lockKey = "/reentrantLock";
        int total = 3;
        int timeoutMillis = 1000;
        int success = 0;
        for (int count = total; count > 0; count--) {
            InternalLock lock = nacosInternalLockHolder.getInternalLock(lockKey);
            if (lock.tryLock(timeoutMillis)) {
                success++;
            }
        }
        assertThat(success, is(total));
    }
    
    @SneakyThrows
    @Test
    public void assertExclusiveLock() {
        String lockKey = "/exclusiveLock";
        int threadCount = 3;
        int timeoutMillis = 1000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        for (int count = threadCount; count > 0; count--) {
            new Thread(() -> {
                try {
                    InternalLock lock = nacosInternalLockHolder.getInternalLock(lockKey);
                    if (lock.tryLock(timeoutMillis)) {
                        success.getAndIncrement();
                    } else {
                        fail.getAndIncrement();
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertThat(success.get(), is(1));
        assertThat(fail.get(), is(threadCount - 1));
    }
}
