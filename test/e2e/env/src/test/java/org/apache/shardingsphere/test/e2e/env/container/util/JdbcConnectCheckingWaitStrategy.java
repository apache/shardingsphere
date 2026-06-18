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

package org.apache.shardingsphere.test.e2e.env.container.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import java.sql.Connection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * JDBC connect checking wait strategy.
 */
@RequiredArgsConstructor
@Slf4j
public final class JdbcConnectCheckingWaitStrategy extends AbstractWaitStrategy {
    
    private final Callable<Connection> connectionSupplier;
    
    @Override
    protected void waitUntilReady() {
        Awaitility.await().ignoreExceptions().atMost(startupTimeout.getSeconds(), TimeUnit.SECONDS).pollInterval(1L, TimeUnit.SECONDS).until(this::checkConnection);
    }
    
    private boolean checkConnection() throws Exception {
        try (Connection ignored = connectionSupplier.call()) {
            log.info("Container ready.");
            return true;
        }
    }
}
