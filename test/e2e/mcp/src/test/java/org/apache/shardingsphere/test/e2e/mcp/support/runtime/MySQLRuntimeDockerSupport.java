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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.testcontainers.DockerClientFactory;

import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MySQLRuntimeDockerSupport {
    
    static String getMySQLImage() {
        return getMySQLImage(EnvironmentPropertiesLoader.loadProperties());
    }
    
    static String getMySQLImage(final Properties props) {
        String result = props.getProperty("mcp.e2e.mysql.image", "").trim();
        if (result.isEmpty()) {
            throw new IllegalStateException("MCP E2E MySQL image property `mcp.e2e.mysql.image` is required.");
        }
        return result;
    }
    
    static boolean isDockerAvailable() {
        return getDockerUnavailableReason().isEmpty();
    }
    
    static Optional<String> getDockerUnavailableReason() {
        try {
            return DockerClientFactory.instance().isDockerAvailable()
                    ? Optional.empty()
                    : Optional.of("Testcontainers Docker client reported Docker unavailable.");
        } catch (final IllegalStateException ex) {
            return Optional.of(createDockerUnavailableReason(ex));
        }
    }
    
    static String createDockerRequiredMessage(final String scenarioMessage) {
        return createDockerRequiredMessage(scenarioMessage, getDockerUnavailableReason().orElse(""));
    }
    
    static String createDockerRequiredMessage(final String scenarioMessage, final String unavailableReason) {
        return unavailableReason.isEmpty() ? scenarioMessage : scenarioMessage + " Docker readiness diagnostic: " + unavailableReason;
    }
    
    static long getJdbcReadyTimeoutMillis(final Duration defaultReadyTimeout) {
        String configuredJdbcReadyTimeoutSeconds = EnvironmentPropertiesLoader.loadProperties().getProperty("mcp.e2e.mysql.ready-timeout-seconds", "").trim();
        try {
            if (configuredJdbcReadyTimeoutSeconds.isEmpty()) {
                return defaultReadyTimeout.toMillis();
            }
            int parsedJdbcReadyTimeoutSeconds = Integer.parseInt(configuredJdbcReadyTimeoutSeconds);
            if (0 >= parsedJdbcReadyTimeoutSeconds) {
                throw new IllegalArgumentException("MCP E2E MySQL JDBC readiness timeout must be positive.");
            }
            return Duration.ofSeconds(parsedJdbcReadyTimeoutSeconds).toMillis();
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("MCP E2E MySQL JDBC readiness timeout must be an integer.", ex);
        }
    }
    
    private static String createDockerUnavailableReason(final IllegalStateException ex) {
        return null == ex.getMessage() || ex.getMessage().isBlank()
                ? "Testcontainers Docker availability check failed without a message."
                : ex.getMessage();
    }
}
