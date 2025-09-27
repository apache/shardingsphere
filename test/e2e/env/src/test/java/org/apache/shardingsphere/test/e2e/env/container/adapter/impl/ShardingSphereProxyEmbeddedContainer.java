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

package org.apache.shardingsphere.test.e2e.env.container.adapter.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.ShardingSphereProxy;
import org.apache.shardingsphere.proxy.frontend.ssl.ProxySSLContext;
import org.apache.shardingsphere.proxy.initializer.BootstrapInitializer;
import org.apache.shardingsphere.test.e2e.env.container.EmbeddedE2EContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.AdapterContainer;
import org.apache.shardingsphere.test.e2e.env.container.adapter.config.AdaptorContainerConfiguration;
import org.apache.shardingsphere.test.e2e.env.container.constants.ProxyContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.constants.StorageContainerConstants;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerConnectOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.option.StorageContainerOption;
import org.apache.shardingsphere.test.e2e.env.container.storage.type.NativeStorageContainer;
import org.apache.shardingsphere.test.e2e.env.container.util.JdbcConnectCheckingWaitStrategy;
import org.apache.shardingsphere.test.e2e.env.container.util.StorageContainerUtils;
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
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * ShardingSphere proxy embedded container.
 */
@RequiredArgsConstructor
@Slf4j
// TODO Reset static properties when closing the class., like PipelineAPIFactory#GOVERNANCE_FACADE_MAP
public final class ShardingSphereProxyEmbeddedContainer implements EmbeddedE2EContainer, AdapterContainer {
    
    private static final String OS_MAC_TMP_DIR = "/tmp";
    
    private static final String E2E_PROXY_CONFIG_TMP_DIR_PREFIX = "e2e-shardingsphere-proxy-config";
    
    private final DatabaseType databaseType;
    
    private final AdaptorContainerConfiguration config;
    
    private final AtomicReference<DataSource> targetDataSourceProvider = new AtomicReference<>();
    
    @Getter
    private final Set<Startable> dependencies = new HashSet<>();
    
    private ShardingSphereProxy proxy;
    
    /**
     * Depends on.
     *
     * @param dependencies dependencies
     */
    public void dependsOn(final Startable... dependencies) {
        Collections.addAll(this.dependencies, dependencies);
    }
    
    @Override
    public void start() {
        dependencies.forEach(Startable::start);
        startProxy();
        new JdbcConnectCheckingWaitStrategy(() -> getTargetDataSource(null).getConnection()).waitUntilReady(null);
    }
    
    @SneakyThrows({SQLException.class, IOException.class, InterruptedException.class})
    private void startProxy() {
        YamlProxyConfiguration yamlConfig = ProxyConfigurationLoader.load(getTempConfigurationDirectory().toString());
        int port = Integer.parseInt(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getDefaultValue());
        new BootstrapInitializer().init(yamlConfig, port);
        ProxySSLContext.init();
        proxy = new ShardingSphereProxy();
        proxy.startInternal(port, Collections.singletonList("0.0.0.0"));
        log.info("ShardingSphere-Proxy {} mode started successfully", ProxyContext.getInstance().getContextManager().getComputeNodeInstanceContext().getModeConfiguration().getType());
    }
    
    private Path getTempConfigurationDirectory() throws IOException {
        Map<String, String> networkAliasAndHostLinkMap = getNetworkAliasAndHostLinkMap();
        Map<String, String> storageConnectionInfoMap = getStorageConnectionInfoMap();
        Path result = createTempDirectory().toPath();
        for (Entry<String, String> each : config.getMountedResources().entrySet()) {
            File file = new File(Objects.requireNonNull(ShardingSphereProxyEmbeddedContainer.class.getResource(each.getKey())).getFile());
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
            result.putAll(genericContainer.getExposedPorts().stream()
                    .collect(Collectors.toMap(exposedPort -> each + ":" + exposedPort, exposedPort -> "127.0.0.1:" + genericContainer.getMappedPort(exposedPort))));
        }
        return result;
    }
    
    private Map<String, String> getStorageConnectionInfoMap() {
        Map<String, String> result = new HashMap<>();
        for (Startable each : dependencies) {
            if (each instanceof NativeStorageContainer) {
                result.putAll(getStorageConnectionInfoMap((NativeStorageContainer) each));
            }
        }
        result.put("username: " + StorageContainerConstants.OPERATION_USER, "username: " + E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getUser());
        result.put("password: " + StorageContainerConstants.OPERATION_PASSWORD, "password: " + E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getPassword());
        return result;
    }
    
    private Map<String, String> getStorageConnectionInfoMap(final NativeStorageContainer container) {
        return container.getNetworkAliases().stream().collect(Collectors.toMap(
                each -> each + ":" + container.getExposedPort(),
                each -> E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getHost() + ":" + E2ETestEnvironment.getInstance().getNativeDatabaseEnvironment().getPort(databaseType)));
    }
    
    private File createTempDirectory() {
        try {
            Path result = SystemUtils.IS_OS_MAC ? Files.createTempDirectory(Paths.get(OS_MAC_TMP_DIR), E2E_PROXY_CONFIG_TMP_DIR_PREFIX) : Files.createTempDirectory(E2E_PROXY_CONFIG_TMP_DIR_PREFIX);
            return result.toFile();
        } catch (final IOException ex) {
            return new File(E2E_PROXY_CONFIG_TMP_DIR_PREFIX + Base58.randomString(5));
        }
    }
    
    private void writeDirectoryToTempFile(final String originalKey, final File file,
                                          final Map<String, String> aliasAndHostLinkMap, final Map<String, String> storageConnectionInfoMap, final Path tempDirectory) throws IOException {
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
    
    @Override
    public DataSource getTargetDataSource(final String serverLists) {
        DataSource dataSource = targetDataSourceProvider.get();
        if (null == dataSource) {
            StorageContainerConnectOption storageContainerConnectOption = DatabaseTypedSPILoader.getService(StorageContainerOption.class, databaseType).getConnectOption();
            targetDataSourceProvider.set(StorageContainerUtils.generateDataSource(storageContainerConnectOption.getURL(
                    "127.0.0.1", 3307, config.getProxyDataSourceName()), ProxyContainerConstants.USER, ProxyContainerConstants.PASSWORD, 2));
        }
        return targetDataSourceProvider.get();
    }
    
    @Override
    public String getAbbreviation() {
        return "proxy";
    }
    
    @Override
    public void stop() {
        proxy.close();
    }
}
