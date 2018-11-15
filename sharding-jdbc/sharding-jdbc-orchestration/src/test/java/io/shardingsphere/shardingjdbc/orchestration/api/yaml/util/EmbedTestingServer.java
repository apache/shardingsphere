/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.orchestration.api.yaml.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException.ConnectionLossException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;

import java.io.File;
import java.io.IOException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EmbedTestingServer {
    
    private static final int PORT = 3181;
    
    private static volatile TestingServer testingServer;
    
    /**
     * Start embed zookeeper server.
     */
    public static void start() {
        if (null != testingServer) {
            return;
        }
        try {
            testingServer = new TestingServer(PORT, new File(String.format("target/test_zk_data/%s/", System.nanoTime())));
        } catch (final Exception ex) {
            if (!isIgnoredException(ex)) {
                throw new RuntimeException(ex);
            }
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                
                @Override
                public void run() {
                    try {
                        testingServer.close();
                    } catch (final IOException ignored) {
                    }
                }
            });
        } 
    }
    
    private static boolean isIgnoredException(final Throwable cause) {
        return cause instanceof ConnectionLossException || cause instanceof NoNodeException || cause instanceof NodeExistsException;
    }
}
