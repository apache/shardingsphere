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

package io.shardingsphere.core.property;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.property.dialect.H2DataSourceMetaDataParser;
import io.shardingsphere.core.property.dialect.MySQLDataSourceMetaDataParser;
import io.shardingsphere.core.property.dialect.OracleDataSourceMetaDataParser;
import io.shardingsphere.core.property.dialect.PostgreSQLDataSourceMetaDataParser;
import io.shardingsphere.core.property.dialect.SQLServerDataSourceMetaDataParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Data source meta data parser factory.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSourceMetaDataFactory {
    
    /**
     * Create data source meta data parser.
     *
     * @param databaseType database type
     * @return instance of data source meta data parser
     */
    public static DataSourceMetaDataParser createDataSourceMetaDataParser(final DatabaseType databaseType) {
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
