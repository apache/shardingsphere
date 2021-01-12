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

package org.apache.shardingsphere.test.integration.env.props;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import java.util.Properties;

/**
 * Database scenario properties.
 */
@RequiredArgsConstructor
public final class DatabaseScenarioProperties {

    private final String scenario;
    
    private final Properties props;
    
    /**
     * Get database host.
     * 
     * @param databaseType database type
     * @return database host
     */
    public String getDatabaseHost(final DatabaseType databaseType) {
        return props.getProperty(String.format("it.%s.%s.host", scenario, databaseType.getName().toLowerCase()));
    }
    
    /**
     * Get database port.
     *
     * @param databaseType database type
     * @return database port
     */
    public int getDatabasePort(final DatabaseType databaseType) {
        return Integer.parseInt(props.getProperty(String.format("it.%s.%s.port", scenario, databaseType.getName().toLowerCase())));
    }
    
    /**
     * Get database username.
     *
     * @param databaseType database type
     * @return database username
     */
    public String getDatabaseUsername(final DatabaseType databaseType) {
        return props.getProperty(String.format("it.%s.%s.username", scenario, databaseType.getName().toLowerCase()));
    }
    
    /**
     * Get database password.
     *
     * @param databaseType database type
     * @return database password
     */
    public String getDatabasePassword(final DatabaseType databaseType) {
        return props.getProperty(String.format("it.%s.%s.password", scenario, databaseType.getName().toLowerCase()));
    }
    
    /**
     * Get proxy host.
     *
     * @return proxy host
     */
    public String getProxyHost() {
        return props.getProperty(String.format("it.%s.proxy.host", scenario));
    }
    
    /**
     * Get proxy port.
     *
     * @return proxy port
     */
    public int getProxyPort() {
        return Integer.parseInt(props.getProperty(String.format("it.%s.proxy.port", scenario)));
    }
    
    /**
     * Get proxy username.
     *
     * @return proxy username
     */
    public String getProxyUsername() {
        return props.getProperty(String.format("it.%s.proxy.username", scenario));
    }
    
    /**
     * Get proxy password.
     *
     * @return proxy password
     */
    public String getProxyPassword() {
        return props.getProperty(String.format("it.%s.proxy.password", scenario));
    }
}
