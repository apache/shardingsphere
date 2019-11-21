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

package io.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.bootstrap.ShardingBootstrap;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import io.shardingsphere.transaction.core.datasource.ShardingTransactionalDataSource;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Adapter for {@code Datasource}.
 * 
 * @author zhangliang
 * @author panjuan
 * @author zhaojun
 */
@Getter
@Setter
public abstract class AbstractDataSourceAdapter extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    static {
        ShardingBootstrap.init();
    }
    
    private final DatabaseType databaseType;
    
    private final ShardingTransactionalDataSource shardingTransactionalDataSources;
    
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    public AbstractDataSourceAdapter(final Map<String, DataSource> dataSourceMap) throws SQLException {
        databaseType = getDatabaseType(dataSourceMap.values());
        shardingTransactionalDataSources = new ShardingTransactionalDataSource(databaseType, dataSourceMap);
    }
    
    protected final DatabaseType getDatabaseType(final Collection<DataSource> dataSources) throws SQLException {
        DatabaseType result = null;
        for (DataSource each : dataSources) {
            DatabaseType databaseType = getDatabaseType(each);
            Preconditions.checkState(null == result || result.equals(databaseType), String.format("Database type inconsistent with '%s' and '%s'", result, databaseType));
            result = databaseType;
        }
        return result;
    }
    
    private DatabaseType getDatabaseType(final DataSource dataSource) throws SQLException {
        if (dataSource instanceof AbstractDataSourceAdapter) {
            return ((AbstractDataSourceAdapter) dataSource).databaseType;
        }
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseType.valueFrom(connection.getMetaData().getDatabaseProductName());
        }
    }
    
    /**
     * Get data source map.
     * 
     * @return data source map
     */
    public final Map<String, DataSource> getDataSourceMap() {
        return shardingTransactionalDataSources.getOriginalDataSourceMap();
    }
    
    @Override
    public final Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }
    
    @Override
    public final Connection getConnection(final String username, final String password) throws SQLException {
        return getConnection();
    }
    
    @Override
    public void close() {
        shardingTransactionalDataSources.close();
    }
}
