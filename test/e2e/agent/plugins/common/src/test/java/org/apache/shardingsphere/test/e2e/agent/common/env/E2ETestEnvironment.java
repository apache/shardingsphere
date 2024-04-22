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

package org.apache.shardingsphere.test.e2e.agent.common.env;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.e2e.agent.common.container.ITContainers;
import org.apache.shardingsphere.test.e2e.agent.common.container.JaegerContainer;
import org.apache.shardingsphere.test.e2e.agent.common.container.MySQLContainer;
import org.apache.shardingsphere.test.e2e.agent.common.container.PrometheusContainer;
import org.apache.shardingsphere.test.e2e.agent.common.container.ShardingSphereJdbcContainer;
import org.apache.shardingsphere.test.e2e.agent.common.container.ShardingSphereProxyContainer;
import org.apache.shardingsphere.test.e2e.agent.common.container.ZipkinContainer;
import org.apache.shardingsphere.test.e2e.agent.common.enums.AdapterType;
import org.apache.shardingsphere.test.e2e.agent.common.enums.PluginType;
import org.apache.shardingsphere.test.e2e.agent.common.request.ProxyRequestExecutor;
import org.apache.shardingsphere.test.e2e.env.container.atomic.DockerITContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainer;
import org.apache.shardingsphere.test.e2e.env.container.atomic.governance.GovernanceContainerFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * E2E test environment.
 */
@Slf4j
public final class E2ETestEnvironment {
    
    private static final E2ETestEnvironment INSTANCE = new E2ETestEnvironment();
    
    @Getter
    private final Collection<String> actualLogs = new LinkedList<>();
    
    @Getter
    private final String adapter;
    
    @Getter
    private final String plugin;
    
    private final long collectDataWaitSeconds;
    
    @Getter
    private String prometheusHttpUrl;
    
    @Getter
    private String zipKinHttpUrl;
    
    @Getter
    private String jaegerHttpUrl;
    
    private PrometheusContainer prometheusContainer;
    
    private ZipkinContainer zipkinContainer;
    
    private JaegerContainer jaegerContainer;
    
    private ITContainers containers;
    
    private ProxyRequestExecutor proxyRequestExecutor;
    
    private boolean initialized;
    
    private String jaegerImage;
    
    private String zipkinImage;
    
    private String mysqlImage;
    
    private String prometheusImage;
    
    private String proxyImage;
    
    private String jdbcProjectImage;
    
    private E2ETestEnvironment() {
        initContainerImage();
        Properties props = EnvironmentProperties.loadProperties("env/engine-env.properties");
        adapter = props.getProperty("it.env.adapter");
        plugin = props.getProperty("it.env.plugin");
        collectDataWaitSeconds = Long.parseLong(props.getProperty("it.env.collect.data.wait.seconds", "0"));
    }
    
    private void initContainerImage() {
        Properties props = EnvironmentProperties.loadProperties("env/image.properties");
        proxyImage = props.getProperty("proxy.image", "apache/shardingsphere-proxy-agent-test:latest");
        jdbcProjectImage = props.getProperty("jdbc.project.image", "apache/shardingsphere-jdbc-agent-test:latest");
        mysqlImage = props.getProperty("mysql.image", "mysql:8.0");
        jaegerImage = props.getProperty("jaeger.image", "jaegertracing/all-in-one:1.41");
        zipkinImage = props.getProperty("zipkin.image", "openzipkin/zipkin:3.2");
        prometheusImage = props.getProperty("prometheus.image", "prom/prometheus:v2.41.0");
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static E2ETestEnvironment getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init environment.
     */
    public void init() {
        if (!containsTestParameter()) {
            return;
        }
        if (AdapterType.PROXY.getValue().equalsIgnoreCase(adapter)) {
            createProxyEnvironment();
        } else if (AdapterType.JDBC.getValue().equalsIgnoreCase(adapter)) {
            createJDBCEnvironment();
        }
        log.info("Waiting to collect data ...");
        if (0 < collectDataWaitSeconds) {
            Awaitility.await().ignoreExceptions().atMost(Duration.ofSeconds(collectDataWaitSeconds + 1)).pollDelay(collectDataWaitSeconds, TimeUnit.SECONDS).until(() -> true);
        }
        initialized = true;
    }
    
    private void createProxyEnvironment() {
        containers = new ITContainers();
        MySQLContainer storageContainer = new MySQLContainer(mysqlImage);
        GovernanceContainer governanceContainer = GovernanceContainerFactory.newInstance("ZooKeeper");
        ShardingSphereProxyContainer proxyContainer = PluginType.FILE.getValue().equalsIgnoreCase(plugin)
                ? new ShardingSphereProxyContainer(proxyImage, plugin, this::collectLogs)
                : new ShardingSphereProxyContainer(proxyImage, plugin);
        proxyContainer.dependsOn(storageContainer);
        proxyContainer.dependsOn(governanceContainer);
        Optional<DockerITContainer> pluginContainer = getPluginContainer();
        pluginContainer.ifPresent(proxyContainer::dependsOn);
        pluginContainer.ifPresent(optional -> containers.registerContainer(optional));
        containers.registerContainer(storageContainer);
        containers.registerContainer(governanceContainer);
        containers.registerContainer(proxyContainer);
        containers.start();
        initHttpUrl();
        try {
            proxyRequestExecutor = new ProxyRequestExecutor(proxyContainer.getConnection());
            proxyRequestExecutor.start();
        } catch (final SQLException ignored) {
        }
    }
    
    private void createJDBCEnvironment() {
        containers = new ITContainers();
        Optional<DockerITContainer> pluginContainer = getPluginContainer();
        MySQLContainer storageContainer = new MySQLContainer(mysqlImage);
        ShardingSphereJdbcContainer jdbcContainer = PluginType.FILE.getValue().equalsIgnoreCase(plugin)
                ? new ShardingSphereJdbcContainer(jdbcProjectImage, plugin, this::collectLogs)
                : new ShardingSphereJdbcContainer(jdbcProjectImage, plugin);
        jdbcContainer.dependsOn(storageContainer);
        pluginContainer.ifPresent(jdbcContainer::dependsOn);
        pluginContainer.ifPresent(optional -> containers.registerContainer(optional));
        containers.registerContainer(storageContainer);
        containers.registerContainer(jdbcContainer);
        containers.start();
        initHttpUrl();
    }
    
    private Optional<DockerITContainer> getPluginContainer() {
        if (PluginType.PROMETHEUS.getValue().equalsIgnoreCase(plugin)) {
            prometheusContainer = new PrometheusContainer(prometheusImage);
            return Optional.of(prometheusContainer);
        } else if (PluginType.ZIPKIN.getValue().equalsIgnoreCase(plugin)) {
            zipkinContainer = new ZipkinContainer(zipkinImage);
            return Optional.of(zipkinContainer);
        } else if (PluginType.JAEGER.getValue().equalsIgnoreCase(plugin)) {
            jaegerContainer = new JaegerContainer(jaegerImage);
            return Optional.of(jaegerContainer);
        }
        return Optional.empty();
    }
    
    private void collectLogs(final OutputFrame outputFrame) {
        if (!initialized) {
            actualLogs.add(outputFrame.getUtf8StringWithoutLineEnding());
        }
    }
    
    private void initHttpUrl() {
        prometheusHttpUrl = null != prometheusContainer ? prometheusContainer.getHttpUrl() : null;
        zipKinHttpUrl = null != zipkinContainer ? zipkinContainer.getHttpUrl() : null;
        jaegerHttpUrl = null != jaegerContainer ? jaegerContainer.getHttpUrl() : null;
    }
    
    /**
     * Destroy environment.
     */
    public void destroy() {
        if (!containsTestParameter()) {
            return;
        }
        if (null != proxyRequestExecutor) {
            proxyRequestExecutor.stop();
        }
        if (null != containers) {
            containers.stop();
        }
    }
    
    /**
     * Judge whether contains test parameter.
     *
     * @return contains or not
     */
    public boolean containsTestParameter() {
        return !Strings.isNullOrEmpty(adapter) && !Strings.isNullOrEmpty(plugin);
    }
}
