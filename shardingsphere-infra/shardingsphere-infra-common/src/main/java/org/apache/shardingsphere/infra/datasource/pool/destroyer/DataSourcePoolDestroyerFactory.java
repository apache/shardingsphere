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

package org.apache.shardingsphere.infra.datasource.pool.destroyer;

import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Data source pool destroyer factory.
 */
public final class DataSourcePoolDestroyerFactory {
    
    static {
        ShardingSphereServiceLoader.register(DataSourcePoolDestroyer.class);
    }
    
    /**
     * Destroy data source pool.
     *
     * @param dataSource data source pool to be destroyed
     * @throws SQLException SQL exception
     */
    public static void destroy(final DataSource dataSource) throws SQLException {
        newInstance(dataSource.getClass().getCanonicalName()).destroy(dataSource);
    }
    
    private static DataSourcePoolDestroyer newInstance(final String dataSourceClassName) {
        return TypedSPIRegistry.findRegisteredService(DataSourcePoolDestroyer.class, dataSourceClassName, new Properties())
                .orElse(RequiredSPIRegistry.getRegisteredService(DataSourcePoolDestroyer.class));
    }
}
