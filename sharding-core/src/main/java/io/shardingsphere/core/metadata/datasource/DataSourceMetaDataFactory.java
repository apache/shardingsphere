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

package io.shardingsphere.core.metadata.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaDataParser;
import io.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaDataParser;
import io.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaDataParser;
import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaDataParser;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaDataParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Data source meta data factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataFactory {
    
    /**
     * Get data source meta data.
     *
     * @param databaseType database type
     * @param dataSource data source
     * @return data source meta data
     */
    public static DataSourceMetaData getDataSourceMetaData(final DatabaseType databaseType, final DataSource dataSource) {
        return createDataSourceMetaDataParser(databaseType).getDataSourceMetaData(getDataSourceURL(dataSource));
    }
    
    private static String getDataSourceURL(final DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (final SQLException ex) {
            throw new ShardingException(ex);
        }
    }
    
    private static DataSourceMetaDataParser createDataSourceMetaDataParser(final DatabaseType databaseType) {
        switch (databaseType) {
            case H2:
                return new H2DataSourceMetaDataParser();
            case MySQL:
                return new MySQLDataSourceMetaDataParser();
            case Oracle:
                return new OracleDataSourceMetaDataParser();
            case PostgreSQL:
                return new PostgreSQLDataSourceMetaDataParser();
            case SQLServer:
                return new SQLServerDataSourceMetaDataParser();
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
}
