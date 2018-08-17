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
import io.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Data source meta data builder.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataFactory {
    
    /**
     * Create new instance of data source meta data.
     *
     * @param databaseType database type
     * @param url data source URL
     * @return data source meta data
     */
    public static DataSourceMetaData newInstance(final DatabaseType databaseType, final String url) {
        switch (databaseType) {
            case H2:
                return new H2DataSourceMetaData(url);
            case MySQL:
                return new MySQLDataSourceMetaData(url);
            case Oracle:
                return new OracleDataSourceMetaData(url);
            case PostgreSQL:
                return new PostgreSQLDataSourceMetaData(url);
            case SQLServer:
                return new SQLServerDataSourceMetaData(url);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", databaseType));
        }
    }
}
