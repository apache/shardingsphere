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

package org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.impl;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.EmbeddedITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.atomic.storage.impl.NativeStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.util.StorageContainerUtils;
import org.apache.shardingsphere.test.e2e.env.container.wait.JdbcConnectionWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.runtime.DataSourceEnvironment;
import org.apache.shardingsphere.test.e2e.env.runtime.E2ETestEnvironment;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.Base58;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ShardingSphere proxy embedded container.
 * todo Reset static properties when closing the class., like PipelineAPIFactory#GOVERNANCE_FACADE_MAP
 */
@Slf4j
public final class ShardingSphereProxyEmbeddedContainer implements AdapterContainer, EmbeddedITContainer {
    
    private static final String OS_MAC_TMP_DIR = "/tmp";
    
    private static final String E2E_PROXY_CONFIG_TMP_DIR_PREFIX = "e2e-shardingsphere-proxy-config";
    
    private final DatabaseType databaseType;
    
    private final AdaptorContainerConfiguration config;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    private final Set<Startable> dependencies = new HashSet<>();
    
    @Setter
    private String abbreviation = ProxyContainerConstants.PROXY_CONTAINER_ABBREVIATION;
    
    private ShardingSphereProxy proxy;
    
    public ShardingSphereProxyEmbeddedContainer(final DatabaseType databaseType, final AdaptorContainerConfiguration config) {
        this.databaseType = databaseType;
        this.config = config;
    }
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (null == dataSource) {
            targetDataSourceProvider.set(StorageContainerUtils.generateDataSource(DataSourceEnvironment.getURL(databaseType, "127.0.0.1", 3307, config.getProxyDataSourceName()),
                    ProxyContainerConstants.USERNAME, ProxyContainerConstants.PASSWORD));
        }
        return targetDataSourceProvider.get();
    }
    
    @Override
    public String getAbbreviation() {
        return abbreviation;
    }
    
    @Override
    public Set<Startable> getDependencies() {
        return dependencies;
    }
    
    /**
     * Depends on.
     *
     * @param dependencies dependencies
     */
    public void dependsOn(final Startable... dependencies) {
        Collections.addAll(this.dependencies, dependencies);
    }
    
    private void startDependencies() {
        getDependencies().forEach(Startable::start);
    }
    
    @SneakyThrows
    @Override
    public void start() {
        startDependencies();
        startInternalProxy();
        new JdbcConnectionWaitStrategy(() -> getTargetDataSource(null).getConnection()).waitUntilReady(null);
    }
    
    @SneakyThrows
    private void startInternalProxy() {
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(getTempConfigDirectory().toString());
        int port = Integer.parseInt(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getDefaultValue());
        new BootstrapInitializer().init(yamlConfig, port);
        ProxySSLContext.init();
        proxy = new ShardingSphereProxy();
        proxy.startInternal(port, Collections.singletonList("0.0.0.0"));
        log.info("ShardingSphere-Proxy {} mode started successfully", ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getModeConfiguration().getType());
    }
    
    private Path getTempConfigDirectory() throws IOException {
        Map<String, String> networkAliasAndHostLinkMap = getNetworkAliasAndHostLinkMap();
        Map<String, String> storageConnectionInfoMap = getStorageConnectionInfoMap();
        Path result = createTempDirectory().toPath();
        for (Entry<String, String> each : config.getMountedResources().entrySet()) {
            File file = new File(ShardingSphereProxyEmbeddedContainer.class.getResource(each.getKey()).getFile());
            if (file.isDirectory()) {
                writeDirectoryToTempFile(each.getKey(), file, networkAliasAndHostLinkMap, storageConnectionInfoMap, result);
            } else {
                String content = IOUtils.toString(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8);
                content = replaceContent(networkAliasAndHostLinkMap, storageConnectionInfoMap, content);
                writeToTempFile(each.getKey(), file, result, content);
            }
        }
        return result;
    }
    
    private void writeDirectoryToTempFile(final String originalKey, final File file, final Map<String, String> aliasAndHostLinkMap, final Map<String, String> storageConnectionInfoMap,
                                          final Path tempDirectory) throws IOException {
        for (File each : file.listFiles()) {
            String content = IOUtils.toString(Files.newInputStream(each.toPath()), StandardCharsets.UTF_8);
            content = replaceContent(aliasAndHostLinkMap, storageConnectionInfoMap, content);
            writeToTempFile(originalKey, each, tempDirectory, content);
        }
    }
    
    private String replaceContent(final Map<String, String> aliasAndHostLinkMap, final Map<String, String> storageConnectionInfoMap, final String content) {
        String result = content;
        for (Entry<String, String> entry : aliasAndHostLinkMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        for (Entry<String, String> entry : storageConnectionInfoMap.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    private void writeToTempFile(final String originalKey, final File file, final Path tempDirectory, final String content) throws IOException {
        File tempFile = tempDirectory.resolve(Paths.get(originalKey + "/" + file.getName()).getFileName()).toFile();
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(content);
        }
    }
    
    private Map<String, String> getStorageConnectionInfoMap() {
        Map<String, String> result = new HashMap<>();
        for (Startable each : dependencies) {
            if (!(each instanceof NativeStorageContainer)) {
                continue;
            }
            NativeStorageContainer storageContainer = (NativeStorageContainer) each;
            for (String network : storageContainer.getNetworkAliases()) {
                result.put(network + ":" + storageContainer.getExposedPort(), E2ETestEnvironment.getInstance().getNativeStorageHost() + ":" + E2ETestEnvironment.getInstance().getNativeStoragePort());
            }
        }
        result.put("username: " + StorageContainerConstants.USERNAME, "username: " + E2ETestEnvironment.getInstance().getNativeStorageUsername());
        result.put("password: " + StorageContainerConstants.PASSWORD, "password: " + E2ETestEnvironment.getInstance().getNativeStoragePassword());
        return result;
    }
    
    private Map<String, String> getNetworkAliasAndHostLinkMap() {
        Map<String, String> result = new HashMap<>();
        for (Startable each : dependencies) {
            if (each instanceof GenericContainer) {
                result.putAll(getNetworkAliasAndHostLinkMap((GenericContainer<?>) each));
            }
        }
        return result;
    }
    
    private Map<String, String> getNetworkAliasAndHostLinkMap(final GenericContainer<?> genericContainer) {
        Map<String, String> result = new HashMap<>();
        for (String each : genericContainer.getNetworkAliases()) {
            for (Integer exposedPort : genericContainer.getExposedPorts()) {
                result.put(each + ":" + exposedPort, "127.0.0.1:" + genericContainer.getMappedPort(exposedPort));
            }
        }
        return result;
    }
    
    private File createTempDirectory() {
        try {
            if (SystemUtils.IS_OS_MAC) {
                return Files.createTempDirectory(Paths.get(OS_MAC_TMP_DIR), E2E_PROXY_CONFIG_TMP_DIR_PREFIX).toFile();
            }
            return Files.createTempDirectory(E2E_PROXY_CONFIG_TMP_DIR_PREFIX).toFile();
        } catch (final IOException ex) {
            return new File(E2E_PROXY_CONFIG_TMP_DIR_PREFIX + Base58.randomString(5));
        }
    }
    
    @Override
    public void stop() {
        proxy.close();
    }
}
