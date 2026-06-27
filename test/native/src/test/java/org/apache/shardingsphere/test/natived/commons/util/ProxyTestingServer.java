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

package org.apache.shardingsphere.test.natived.commons.util;

import lombok.Getter;
import org.apache.curator.test.InstanceSpec;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.arguments.BootstrapArguments;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.frontend.CDCServer;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * This class is designed to start ShardingSphere Proxy directly in the current process,
 * whether it is HotSpot VM or GraalVM Native Image,
 * so this class intentionally uses fewer than a few dozen JVM parameters.
 * It is necessary to avoid creating multiple ShardingSphere Proxy instances in parallel in Junit5 unit tests.
 * Currently, Junit5 unit tests are all executed serially.
 */
public final class ProxyTestingServer {
    
    @Getter
    private final int proxyPort;
    
    private final ShardingSphereProxy proxy;
    
    /**
     * Call this method to start the Server side of ShardingSphere Proxy.
     *
     * @param configAbsolutePath The absolute path to the directory where {@code global.yaml} is located.
     * @see org.apache.shardingsphere.proxy.Bootstrap
     */
    public ProxyTestingServer(final String configAbsolutePath) {
        proxyPort = InstanceSpec.getRandomPort();
        String[] args = new String[]{String.valueOf(proxyPort), configAbsolutePath, "0.0.0.0"};
        try {
            BootstrapArguments bootstrapArgs = new BootstrapArguments(args);
            YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(bootstrapArgs.getConfigurationPath());
            int port = bootstrapArgs.getPort().orElseThrow(() -> new IllegalStateException("Check `org.apache.curator.test.InstanceSpec#getRandomPort`."));
            List<String> addresses = bootstrapArgs.getAddresses();
            checkPort(addresses, port);
            new BootstrapInitializer().init(yamlConfig, port);
            Optional.ofNullable((Integer) yamlConfig.getServerConfiguration().getProps().get(ConfigurationPropertyKey.CDC_SERVER_PORT.getKey()))
                    .ifPresent(optional -> new Thread(new CDCServer(addresses, optional)).start());
            ProxySSLContext.init();
            proxy = new ShardingSphereProxy();
            bootstrapArgs.getSocketPath().ifPresent(proxy::start);
            proxy.startInternal(port, addresses);
        } catch (final SQLException | IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void checkPort(final List<String> addresses, final int port) throws IOException {
        for (String each : addresses) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.bind(new InetSocketAddress(each, port));
            }
        }
    }
    
    /**
     * Close ShardingSphere Proxy.
     *
     */
    public void close() {
        proxy.close();
    }
}
