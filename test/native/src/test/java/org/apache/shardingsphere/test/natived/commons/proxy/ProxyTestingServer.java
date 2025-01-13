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

package org.apache.shardingsphere.test.natived.commons.proxy;

import lombok.Getter;
import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.proxy.Bootstrap;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * This class is designed to start ShardingSphere Proxy directly in the current process,
 * whether it is HotSpot VM or GraalVM Native Image,
 * so this class intentionally uses fewer than a few dozen JVM parameters.
 * It is necessary to avoid creating multiple ShardingSphere Proxy instances in parallel in Junit5 unit tests.
 * Currently, Junit5 unit tests are all executed serially.
 */
@Getter
public final class ProxyTestingServer {
    
    private final int proxyPort = InstanceSpec.getRandomPort();
    
    private final CompletableFuture<Void> completableFuture;
    
    /**
     * Call this method to start the Server side of ShardingSphere Proxy in a separate thread.
     *
     * @param configAbsolutePath The absolute path to the directory where {@code global.yaml} is located.
     */
    public ProxyTestingServer(final String configAbsolutePath) {
        completableFuture = CompletableFuture.runAsync(() -> {
            try {
                Bootstrap.main(new String[]{String.valueOf(proxyPort), configAbsolutePath, "0.0.0.0", "false"});
            } catch (final IOException | SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    
    /**
     * Force close ShardingSphere Proxy. See {@link org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy#close}.
     */
    public void close() {
        ProxyContext.getInstance().getContextManager().close();
        completableFuture.cancel(false);
    }
}
