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

package org.apache.shardingsphere.test.e2e.env.container.wait;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Wait strategy implemented via JDBC connection checking.
 */
@RequiredArgsConstructor
@Slf4j
public final class JdbcConnectionWaitStrategy extends AbstractWaitStrategy {
    
    private final Callable<Connection> connectionSupplier;
    
    @Override
    protected void waitUntilReady() {
        Unreliables.retryUntilSuccess((int) startupTimeout.getSeconds(), TimeUnit.SECONDS, this::mockRateLimiter);
    }
    
    private boolean mockRateLimiter() {
        getRateLimiter().doWhenReady(() -> {
            try (Connection ignored = connectionSupplier.call()) {
                log.info("Container ready.");
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                throw new RuntimeException("Not Ready yet.", ex);
            }
        });
        return true;
    }
}
