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

package io.shardingsphere.proxy.backend.common.jdbc;

import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Connection manager.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConnectionManager {
    
    private static final ThreadLocal<Map<DataSource, Connection>> RESOURCE = new ThreadLocal<Map<DataSource, Connection>>() {
        
        @Override
        protected Map<DataSource, Connection> initialValue() {
            return new HashMap<>();
        }
    };
    
    /**
     * Get connection of current thread datasource.
     *
     * @param dataSource Datasource
     * @return Connection
     * @throws SQLException SQL exception
     */
    public static Connection getConnection(final DataSource dataSource) throws SQLException {
        if (ProxyMode.MEMORY_STRICTLY == RuleRegistry.getInstance().getProxyMode()) {
            return dataSource.getConnection();
        }
        return RESOURCE.get().containsKey(dataSource) ? RESOURCE.get().get(dataSource) : RESOURCE.get().put(dataSource, dataSource.getConnection());
    }
    
    /**
     * Clear Connection resource for current thread-local.
     */
    public static void clear() {
        RESOURCE.remove();
    }
}
