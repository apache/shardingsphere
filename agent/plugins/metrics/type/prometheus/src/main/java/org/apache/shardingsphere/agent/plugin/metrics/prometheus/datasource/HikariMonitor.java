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

package org.apache.shardingsphere.agent.plugin.metrics.prometheus.datasource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.type.GaugeMetricsCollector;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * HikariCP connection pool monitor via JMX.
 */
@Slf4j
public final class HikariMonitor {
    
    private static final Pattern POOL_PATTERN = Pattern.compile("^Pool\\s*\\(.*\\)$");
    
    private static final Pattern POOL_CONFIG_PATTERN = Pattern.compile("^PoolConfig\\s*\\(.*\\)$");
    
    private static final int MIN_VALID_PORT = 1;
    
    private static final int MAX_VALID_PORT = 65535;
    
    private static final int SCHEDULED_INITIAL_DELAY = 15;
    
    private static final int SCHEDULED_PERIOD = 15;
    
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 5;
    
    private static final AtomicBoolean SHUTDOWN_HOOK_REGISTERED = new AtomicBoolean(false);
    
    private static final String HIKARI_TYPE = "hikari";
    
    private final MetricConfiguration activeConnectionsMetric = new MetricConfiguration("hikaricp_active_connections",
            MetricCollectorType.GAUGE, "the shardingsphere proxy hikaricp active connections",
            Collections.emptyList(), Collections.emptyMap());
    
    private final MetricConfiguration idleConnectionsMetric = new MetricConfiguration("hikaricp_idle_connections",
            MetricCollectorType.GAUGE, "the shardingsphere proxy hikaricp idle connections",
            Collections.emptyList(), Collections.emptyMap());
    
    private final MetricConfiguration totalConnectionsMetric = new MetricConfiguration("hikaricp_total_connections",
            MetricCollectorType.GAUGE, "the shardingsphere proxy hikaricp total connections",
            Collections.emptyList(), Collections.emptyMap());
    
    private final MetricConfiguration threadsAwaitingConnectionMetric = new MetricConfiguration(
            "hikaricp_threads_awaiting_connection", MetricCollectorType.GAUGE,
            "the shardingsphere proxy hikaricp threads awaiting connection",
            Collections.emptyList(), Collections.emptyMap());
    
    private final MetricConfiguration maximumPoolSizeMetric = new MetricConfiguration("hikaricp_maximum_pool_size",
            MetricCollectorType.GAUGE, "the shardingsphere proxy hikaricp maximum pool size",
            Collections.emptyList(), Collections.emptyMap());
    
    private ScheduledExecutorService scheduler;
    
    private final AtomicBoolean started = new AtomicBoolean(false);
    
    /**
     * Start scheduled monitoring of HikariCP connection pool.
     *
     * @param hikaricpJmxPort JMX port for HikariCP monitoring
     */
    public void startScheduleMonitor(final String hikaricpJmxPort) {
        log.info("startScheduleMonitor");
        if (isBlank(hikaricpJmxPort)) {
            log.warn("HIKARICP_JMX_PORT is null or empty, HikariCP monitoring disabled");
            return;
        }
        
        if (!isValidPort(hikaricpJmxPort)) {
            return;
        }
        
        // Check if already started
        if (!started.compareAndSet(false, true)) {
            log.warn("HikariCP monitor is already started, ignore duplicate call");
            return;
        }
        
        try {
            // Register shutdown hook only once
            if (SHUTDOWN_HOOK_REGISTERED.compareAndSet(false, true)) {
                Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownScheduler));
            }
            
            // Create scheduled task executor
            scheduler = Executors.newSingleThreadScheduledExecutor();
            
            // Start scheduled task
            scheduler.scheduleAtFixedRate(() -> fetchJMXDataSafely(hikaricpJmxPort),
                    SCHEDULED_INITIAL_DELAY, SCHEDULED_PERIOD, TimeUnit.SECONDS);
            
            log.info("HikariCP monitoring started on JMX port: {}", hikaricpJmxPort);
        } catch (final IllegalStateException ex) {
            // Reset started state if initialization fails
            started.set(false);
            log.error("Failed to start HikariCP monitor", ex);
            throw ex;
        }
    }
    
    /**
     * Safely fetch JMX data without catching generic exceptions.
     *
     * @param hikaricpJmxPort JMX port for HikariCP monitoring
     */
    private void fetchJMXDataSafely(final String hikaricpJmxPort) {
        try {
            fetchJMXData(hikaricpJmxPort);
        } catch (final MalformedURLException ex) {
            log.error("Malformed JMX URL for port: {}", hikaricpJmxPort, ex);
        } catch (final IOException ex) {
            log.error("IO error while fetching JMX data for port: {}", hikaricpJmxPort, ex);
        } catch (final MalformedObjectNameException ex) {
            log.error("Malformed object name while fetching JMX data for port: {}", hikaricpJmxPort, ex);
        }
    }
    
    /**
     * Check if string is blank.
     *
     * @param str the string to check
     * @return true if string is null or empty after trim
     */
    private boolean isBlank(final String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Check if port string is valid.
     *
     * @param portStr the port string to validate
     * @return true if port is valid
     */
    private boolean isValidPort(final String portStr) {
        try {
            int port = Integer.parseInt(portStr);
            if (port < MIN_VALID_PORT || port > MAX_VALID_PORT) {
                log.error("HIKARICP_JMX_PORT is not a valid port number: {}", port);
                return false;
            }
            return true;
        } catch (final NumberFormatException ex) {
            log.error("HIKARICP_JMX_PORT is not a valid number: {}", portStr, ex);
            return false;
        }
    }
    
    /**
     * Shutdown scheduler gracefully.
     */
    private void shutdownScheduler() {
        if (scheduler != null) {
            log.info("Shutting down HikariCP monitor scheduler...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown of HikariCP monitor scheduler...");
                    scheduler.shutdownNow();
                }
            } catch (final InterruptedException ex) {
                log.warn("Interrupted while shutting down HikariCP monitor scheduler");
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            log.info("HikariCP monitor scheduler shutdown completed");
        }
    }
    
    private void fetchJMXData(final String hikaricpJmxPort) throws IOException, MalformedObjectNameException {
        // Use try-with-resources to automatically close JMXConnector
        try (
                JMXConnector connector = JMXConnectorFactory.connect(
                        new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:"
                                + hikaricpJmxPort + "/jmxrmi"),
                        null)) {
            
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            
            // Find all HikariCP MBeans
            Set<ObjectName> hikariMBeans = connection.queryNames(
                    new ObjectName("com.zaxxer.hikari:type=*"), null);
            
            if (hikariMBeans.isEmpty()) {
                log.debug("No HikariCP MBeans found on port: {}", hikaricpJmxPort);
                return;
            }
            
            log.info("Found {} HikariCP MBeans on port: {}", hikariMBeans.size(), hikaricpJmxPort);
            
            for (ObjectName name : hikariMBeans) {
                String typeName = name.getKeyProperty("type");
                if (typeName != null && POOL_PATTERN.matcher(typeName).matches()) {
                    monitorPoolMetrics(connection, name);
                } else if (typeName != null && POOL_CONFIG_PATTERN.matcher(typeName).matches()) {
                    monitorPoolConfig(connection, name);
                }
            }
        }
    }
    
    private void monitorPoolMetrics(final MBeanServerConnection connection, final ObjectName poolName) {
        String poolNameStr = poolName.getKeyProperty("type");
        // Extract connection pool name for better logging
        String poolDisplayName = extractPoolName(poolNameStr);
        
        // Declare variables close to their usage
        final double active;
        final double idle;
        final double total;
        final double awaiting;
        
        try {
            // Connection pool runtime metrics
            final Object activeConnections = connection.getAttribute(poolName, "ActiveConnections");
            final Object idleConnections = connection.getAttribute(poolName, "IdleConnections");
            final Object totalConnections = connection.getAttribute(poolName, "TotalConnections");
            final Object threadsAwaiting = connection.getAttribute(poolName, "ThreadsAwaitingConnection");
            
            // Safe type conversion
            active = toDoubleSafely(activeConnections);
            idle = toDoubleSafely(idleConnections);
            total = toDoubleSafely(totalConnections);
            awaiting = toDoubleSafely(threadsAwaiting);
        } catch (final IOException | ReflectionException
                | InstanceNotFoundException | AttributeNotFoundException
                | MBeanException ex) {
            log.error("Failed to get pool metrics for: {}", poolDisplayName, ex);
            return;
        }
        
        // Update metrics
        MetricsCollectorRegistry.<GaugeMetricsCollector>get(activeConnectionsMetric, HIKARI_TYPE).inc();
        MetricsCollectorRegistry.<GaugeMetricsCollector>get(idleConnectionsMetric, HIKARI_TYPE).inc();
        MetricsCollectorRegistry.<GaugeMetricsCollector>get(totalConnectionsMetric, HIKARI_TYPE).inc();
        MetricsCollectorRegistry.<GaugeMetricsCollector>get(threadsAwaitingConnectionMetric, HIKARI_TYPE).inc();
        log.info("HikariCP Metrics - Pool: {}, Active: {}, Idle: {}, Total: {}, Awaiting: {}",
                poolDisplayName, active, idle, total, awaiting);
    }
    
    private void monitorPoolConfig(final MBeanServerConnection connection, final ObjectName configName) {
        String configNameStr = configName.getKeyProperty("type");
        // Extract connection pool name for better logging
        String poolDisplayName = extractPoolName(configNameStr);
        
        final double maxPoolSize;
        
        try {
            // Connection pool configuration information
            Object maximumPoolSize = connection.getAttribute(configName, "MaximumPoolSize");
            maxPoolSize = toDoubleSafely(maximumPoolSize);
        } catch (final IOException | ReflectionException
                | InstanceNotFoundException | AttributeNotFoundException
                | MBeanException ex) {
            log.error("Failed to get pool config for: {}", poolDisplayName, ex);
            return;
        }
        
        MetricsCollectorRegistry.<GaugeMetricsCollector>get(maximumPoolSizeMetric, HIKARI_TYPE).inc();
        log.info("HikariCP Config - Pool: {}, MaxPoolSize: {}", poolDisplayName, maxPoolSize);
    }
    
    /**
     * Extract connection pool name from MBean type string.
     *
     * @param typeName the MBean type name
     * @return extracted pool name
     */
    private String extractPoolName(final String typeName) {
        if (typeName == null) {
            return "unknown";
        }
        return typeName.replaceAll("^Pool\\s*\\((.*)\\)$", "$1")
                .replaceAll("^PoolConfig\\s*\\((.*)\\)$", "$1");
    }
    
    /**
     * Safely convert to double type.
     *
     * @param value the value to convert
     * @return converted double value, 0.0 if conversion fails
     */
    private double toDoubleSafely(final Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (final NumberFormatException ex) {
            log.warn("Cannot convert to double: {}", value);
            return 0.0;
        }
    }
}
