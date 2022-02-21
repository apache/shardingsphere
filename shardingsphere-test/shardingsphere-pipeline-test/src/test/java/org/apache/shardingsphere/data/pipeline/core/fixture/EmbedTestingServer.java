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

package org.apache.shardingsphere.data.pipeline.core.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class EmbedTestingServer {
    
    private static final int PORT = 3181;
    
    private static volatile TestingServer testingServer;
    
    private static final Object INIT_LOCK = new Object();
    
    /**
     * Start embed zookeeper server.
     */
    public static void start() {
        if (null != testingServer) {
            log.info("Embed zookeeper server already exists 1, on {}", testingServer.getConnectString());
            return;
        }
        log.info("Starting embed zookeeper server...");
        synchronized (INIT_LOCK) {
            if (null != testingServer) {
                log.info("Embed zookeeper server already exists 2, on {}", testingServer.getConnectString());
                return;
            }
            start0();
            waitTestingServerReady();
        }
    }
    
    private static void start0() {
        try {
            testingServer = new TestingServer(PORT, new File(String.format("target/test_zk_data/%s/", System.nanoTime())));
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            if (!isIgnoredException(ex)) {
                throw new RuntimeException(ex);
            } else {
                log.warn("Start embed zookeeper server got exception: {}", ex.getMessage());
            }
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    testingServer.close();
                } catch (final IOException ignored) {
                }
                log.info("Close embed zookeeper server done");
            }));
        }
    }
    
    private static void waitTestingServerReady() {
        int maxRetries = 60;
        try (CuratorFramework client = buildCuratorClient()) {
            client.start();
            int round = 0;
            while (round < maxRetries) {
                try {
                    if (client.getZookeeperClient().isConnected()) {
                        log.info("client is connected");
                        break;
                    }
                    if (client.blockUntilConnected(500, TimeUnit.MILLISECONDS)) {
                        CuratorFrameworkState state = client.getState();
                        Collection<String> childrenKeys = client.getChildren().forPath("/");
                        log.info("TestingServer connected, state={}, childrenKeys={}", state, childrenKeys);
                        break;
                    }
                    // CHECKSTYLE:OFF
                } catch (final Exception ignored) {
                    // CHECKSTYLE:ON
                }
                ++round;
            }
        }
    }
    
    private static CuratorFramework buildCuratorClient() {
        Builder builder = CuratorFrameworkFactory.builder();
        int retryIntervalMilliseconds = 500;
        int maxRetries = 3;
        builder.connectString(getConnectionString())
                .retryPolicy(new ExponentialBackoffRetry(retryIntervalMilliseconds, maxRetries, retryIntervalMilliseconds * maxRetries))
                .namespace("test");
        builder.sessionTimeoutMs(60 * 1000);
        builder.connectionTimeoutMs(500);
        return builder.build();
    }
    
    private static boolean isIgnoredException(final Throwable cause) {
        return cause instanceof ConnectionLossException || cause instanceof NoNodeException || cause instanceof NodeExistsException;
    }
    
    /**
     * Get the connection string.
     *
     * @return connection string
     */
    public static String getConnectionString() {
        return "localhost:" + PORT;
    }
}
