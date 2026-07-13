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

package org.apache.shardingsphere.test.e2e.env.container.storage.option;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPI;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.Collections;
import java.util.Map;

/**
 * Native storage container option.
 */
@SingletonSPI
public interface NativeStorageContainerOption extends DatabaseTypedSPI {
    
    /**
     * Get native storage major version for initialization resource selection.
     *
     * @return major version
     */
    int getMajorVersion();
    
    /**
     * Get native storage initialization user.
     *
     * @param configuredUser configured user
     * @return initialization user
     */
    default String getInitUser(final String configuredUser) {
        return configuredUser;
    }
    
    /**
     * Configure native storage initialization data source.
     *
     * @param dataSource data source
     */
    default void configureInitDataSource(final HikariDataSource dataSource) {
    }
    
    /**
     * Get native storage initialization URL.
     *
     * @param connectOption storage connect option
     * @param host database host
     * @param port database port
     * @return initialization URL
     */
    default String getInitURL(final StorageContainerConnectOption connectOption, final String host, final int port) {
        return connectOption.getURL(host, port);
    }
    
    /**
     * Get native storage access URL.
     *
     * @param connectOption storage connect option
     * @param host database host
     * @param port database port
     * @param dataSourceName data source name
     * @return access URL
     */
    default String getAccessURL(final StorageContainerConnectOption connectOption, final String host, final int port, final String dataSourceName) {
        return null == dataSourceName || dataSourceName.isEmpty() ? connectOption.getURL(host, port) : connectOption.getURL(host, port, dataSourceName);
    }
    
    /**
     * Configure native storage access data source.
     *
     * @param dataSource data source
     * @param dataSourceName data source name
     */
    default void configureAccessDataSource(final HikariDataSource dataSource, final String dataSourceName) {
    }
    
    /**
     * Get native storage link replacements.
     *
     * <p>The iteration order defines replacement precedence, so implementations should put more specific replacements before general replacements.</p>
     *
     * @param connectOption storage connect option
     * @param networkAlias network alias
     * @param host database host
     * @param port database port
     * @param exposedPort exposed port in configuration resources
     * @return link replacements
     */
    default Map<String, String> getLinkReplacements(final StorageContainerConnectOption connectOption, final String networkAlias, final String host, final int port, final int exposedPort) {
        return Collections.singletonMap(networkAlias + ":" + exposedPort, host + ":" + port);
    }
}
