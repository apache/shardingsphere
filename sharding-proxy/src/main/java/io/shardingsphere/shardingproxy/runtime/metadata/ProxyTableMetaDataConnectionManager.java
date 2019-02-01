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

package io.shardingsphere.shardingproxy.runtime.metadata;

import io.shardingsphere.core.metadata.table.executor.TableMetaDataConnectionManager;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSource;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Manager of connection which for table meta data loader of proxy.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ProxyTableMetaDataConnectionManager implements TableMetaDataConnectionManager {
    
    private final JDBCBackendDataSource backendDataSource;
    
    @Override
    public Connection getConnection(final String dataSourceName) throws SQLException {
        return backendDataSource.getConnection(dataSourceName);
    }
}
