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

package io.shardingsphere.proxy.backend.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Hold the connection when proxy mode is CONNECTION_STRICTLY.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyConnectionHolder {
    
    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCE = new ThreadLocal<Map<DataSource, Connection>>() {
        
        @Override
        protected Map<DataSource, Connection> initialValue() {
            return new HashMap<>();
        }
    };
    
    /**
     * Set connection associate with datasource into Thread.
     *
     * @param dataSource DataSource
     * @param connection Connection
     */
    public static void setConnection(final DataSource dataSource, final Connection connection) {
        RESOURCE.get().put(dataSource, connection);
    }
    
    /**
     * Get connection of current thread datasource.
     *
     * @param dataSource Datasource
     * @return Connection
     */
    public static Connection getConnection(final DataSource dataSource) {
        return RESOURCE.get().get(dataSource);
    }
    
    /**
     * Clear Connection resource for current thread-local.
     */
    public static void clear() {
        RESOURCE.remove();
    }
}
