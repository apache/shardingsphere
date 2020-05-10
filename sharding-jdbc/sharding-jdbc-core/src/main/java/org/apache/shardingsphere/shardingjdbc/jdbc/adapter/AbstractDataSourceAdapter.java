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

package org.apache.shardingsphere.shardingjdbc.jdbc.adapter;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.shardingjdbc.jdbc.unsupported.AbstractUnsupportedOperationDataSource;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseType;
import org.apache.shardingsphere.underlying.common.database.type.DatabaseTypes;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Adapter for {@code Datasource}.
 */
@Getter
public abstract class AbstractDataSourceAdapter extends AbstractUnsupportedOperationDataSource implements AutoCloseable {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final DatabaseType databaseType;
    
    private final RuntimeContext runtimeContext;
    
    @Setter
    private PrintWriter logWriter = new PrintWriter(System.out);
    
    public AbstractDataSourceAdapter(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> configurations, final Properties props) throws SQLException {
        this.dataSourceMap = dataSourceMap;
        databaseType = createDatabaseType();
        runtimeContext = new RuntimeContext(dataSourceMap, databaseType, configurations, props);
    }
    
    private DatabaseType createDatabaseType() throws SQLException {
        DatabaseType result = null;
        for (DataSource each : dataSourceMap.values()) {
            DatabaseType databaseType = createDatabaseType(each);
            Preconditions.checkState(null == result || result == databaseType, String.format("Database type inconsistent with '%s' and '%s'", result, databaseType));
            result = databaseType;
        }
        return result;
    }
    
    private DatabaseType createDatabaseType(final DataSource dataSource) throws SQLException {
        if (dataSource instanceof AbstractDataSourceAdapter) {
            return ((AbstractDataSourceAdapter) dataSource).databaseType;
        }
        try (Connection connection = dataSource.getConnection()) {
            return DatabaseTypes.getDatabaseTypeByURL(connection.getMetaData().getURL());
        }
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
    public final void close() throws Exception {
        close(dataSourceMap.keySet());
    }
    
    /**
     * Close dataSources.
     * 
     * @param dataSourceNames data source names
     * @throws Exception exception
     */
    public void close(final Collection<String> dataSourceNames) throws Exception {
        dataSourceNames.forEach(each -> close(dataSourceMap.get(each)));
        runtimeContext.close();
    }
    
    private void close(final DataSource dataSource) {
        try {
            Method method = dataSource.getClass().getDeclaredMethod("close");
            method.setAccessible(true);
            method.invoke(dataSource);
        } catch (final ReflectiveOperationException ignored) {
        }
    }
}
